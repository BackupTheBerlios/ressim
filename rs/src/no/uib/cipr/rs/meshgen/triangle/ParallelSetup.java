package no.uib.cipr.rs.meshgen.triangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class ParallelSetup implements Source, PointHandler, FractureHandler {
    /**
     * Barrier to which this class will post after completing each stage in the
     * reading sequence. This enable two sources to write to the same handler
     * without advancing it before the other source is finished with the current
     * stage (usually the file is written in separate sections -- objects are
     * interchangable within the section but a section cannot be reopened after
     * it is finished)
     */
    private CyclicBarrier barrier;
    
    /**
     * If one thread crashes because of a failure, then the other should
     * terminate as soon as they get to the synchronization point, because the
     * failed thread will never release its barrier. (An alternative would of
     * course be that they elect a new quorum and continue running with a new
     * barrier consisting of this).
     */
    private AtomicReference<TriExc> failure = new AtomicReference<TriExc>();

    
    private boolean waitOnBarrier() throws TriExc {
        // if another thread has crashed, then don't wait on the barrier
        // but bail out and let the program terminate
        TriExc othersError = failure.get();
        if (othersError != null) {
            throw TriExc.CLUSTER_FAILURE.create(othersError);
        }
        
        try {        
            boolean isFirst = barrier.await() == barrier.getParties() - 1;
            return isFirst;
        } catch (InterruptedException ie) {
            throw TriExc.CLUSTER_FAILURE.create(ie);
        } catch (BrokenBarrierException bbe) {
            throw TriExc.CLUSTER_FAILURE.create(bbe);
        }
    }
    
    /**
     * Signal that this source is finished with the current stage and wants to
     * continue to the next stage. The method won't return until all the other
     * sources that writes to the same sink has completed.
     * 
     * @param action
     *            Action that will be sent to the common sink after all parties
     *            have arrived at the reduction point. This is performed before
     *            anyone is allowed to continue. (It is assumed that the sink is
     *            only allowed to be in one stage at the time -- we cannot risk
     *            that one of the processors continue to send events for the
     *            next stage before we have completed the current one).
     */
    private void await(Callable<Void> action) throws TriExc {
        try {
            if (barrier != null) {                
                // the first party to arrive at the barrier probably has
                // the lowest work-load, so let it perform the common task
                boolean isFirst = waitOnBarrier();

                // the common task is executed only once; everyone must
                // wait until it is finished, though.
                if (isFirst) {
                    action.call();
                }
                
                // start processing the next step when the common action has
                // been processed. terminate in case of failure because then
                // the common task may not have executed.
                waitOnBarrier();
            } else {
                // only one thread -- the action should always run
                action.call();
            }
        }
        // if an application-exception was thrown then pass it on; all
        // other exceptions are translated into a common exception. the
        // generic type model of Java don't allow us to parameterize on
        // the exception(list) that the method is allowed to throw.
        catch (TriExc te) {
            throw te;
        } catch (Exception e) {
            throw TriExc.CLUSTER_FAILURE.create(e);
        }
    }

    /**
     * Shared sink to which all events will be posted.
     */
    PointHandler pointSink;

    FractureHandler fractureSink;

    /**
     * Input sources from which we gather information in parallel. There has to
     * be at least one source for any points to be generated at all.
     */
    private List<Source> sources;

    /**
     * 
     */
    ParallelSetup(List<Source> sources) {
        // store the reference to the barrier in order for all methods
        // (that represents the various stages) to gain access to it
        if (sources.size() > 1) {
            this.barrier = new CyclicBarrier(sources.size());
        } else {
            this.barrier = null;
        }

        // adopt all the sources; if we're closed, then they're closed
        this.sources = sources;
    }

    public void close() throws IOException {
        // success state of the close operation
        IOException exc = null;

        // loop through all sources and attempt to close them
        for (Source source : sources) {
            try {
                source.close();
            } catch (IOException ioe) {
                // what to do? continue closing the other sources, then
                // rethrow afterwards if any errors were found
                exc = ioe;
            }
        }
        if (exc != null) {
            throw exc;
        }
    }

    /**
     * Read from all input sources simultaneously, writing to the common sink.
     */
    public synchronized void readAll(PointHandler pointSink,
            FractureHandler fractureSink) throws TriExc {
        // we are now delivering events to this sink. this method can only
        // be called from one thread at a time, to avoid overwriting these
        // member variables (which would confuse the collector as to which
        // sink the events should be delivered)
        this.pointSink = pointSink;
        this.fractureSink = fractureSink;

        // the collector is ourself, but the we need stable references to
        // use within the inner class. only necessary to satistfy the compiler
        final PointHandler pointCollector = this;
        final FractureHandler fractureCollector = this;

        // create a list of threads that will each ask a given source to
        // start delivering events to us.
        ArrayList<Callable<Void>> processors = new ArrayList<Callable<Void>>(
                sources.size());
        for (Source s : sources) {
            final Source source = s;
            processors.add(new Callable<Void>() {
                public Void call() throws TriExc {
                    try {
                        source.readAll(pointCollector, fractureCollector);
                    }
                    catch(TriExc te) {
                        // signal to the other threads that we have failed, and
                        // ask them to stop. notice that we first set the flag
                        // atomically so that no more barriers will be entered
                        // (per the implementation in waitForBarrier()) and then
                        // we break up any existing threads that wait on the
                        // barrier afterwards.
                        failure.compareAndSet(null, te);
                        barrier.reset();
                        
                        // stop this thread from doing anything further
                        throw te;
                    }
                    return null;
                }
            });
        }

        // execute all tasks in parallel. if any task fails, then the other
        // tasks will be cancelled as well. the exception will be rethrown
        ExecutorService exec = Executors.newFixedThreadPool(sources.size());
        try {
            exec.invokeAll(processors);
            // remember to close the threadpool when we're done with it so
            // that the VM does not appear to hang (when it is really waiting
            // for more threads to be submitted to the queue)
            exec.shutdown();
        } catch (InterruptedException ie) {
            throw TriExc.CLUSTER_FAILURE.create(ie);
        }
        
        // if any of the nodes in the cluster have failed, then consider it as
        // the failure of the entire cluster
        if (failure.get() != null) {
            throw failure.get();
        }
    }

    AtomicInteger numOfPoints = new AtomicInteger();

    AtomicInteger numOfFractures = new AtomicInteger();

    public void prepareForPoints(int count) throws TriExc {
        // sum all the points from the various sources; when all sources
        // are done posting their count, then forward it to the sink
        numOfPoints.addAndGet(count);
        await(new Callable<Void>() {
            public Void call() throws TriExc {
                pointSink.prepareForPoints(numOfPoints.get());
                return null;
            }
        });
    }

    public void prepareForFractures(int count) throws TriExc {
        // sum all the fractures from the various sources; when all
        // sources are done posting their count, then forward it
        numOfFractures.addAndGet(count);
        await(new Callable<Void>() {
            public Void call() throws TriExc {
                fractureSink.prepareForFractures(numOfFractures.get());
                return null;
            }
        });
    }

    public int onPoint(double x, double y, double z) throws TriExc {
        // forward the point event wrapped in a synchronizer. this allows
        // us to do synchronization without having to incur the penalty
        // in all sources/sinks
        // we could use a point cache to avoid writing the same point
        // twice, but the chances of having fractures starting exactly at
        // the partition's boundaries are slim, and they would upset the
        // point count that we have just reported
        synchronized (pointSink) {
            return pointSink.onPoint(x, y, z);
        }
    }

    public void onFracture(int a, int b, Kind kind) throws TriExc {
        synchronized (fractureSink) {
            fractureSink.onFracture(a, b, kind);
        }
    }

    public void closePoints() throws TriExc {
        await(new Callable<Void>() {
            public Void call() throws TriExc {
                pointSink.closePoints();
                return null;
            }
        });
    }

    public void closeFractures() throws TriExc {
        await(new Callable<Void>() {
            public Void call() throws TriExc {
                fractureSink.closeFractures();
                return null;
            }
        });
    }
}
