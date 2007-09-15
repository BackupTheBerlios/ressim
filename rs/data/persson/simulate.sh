#!/bin/sh
# check that a command line argument was specified
if [ -z "$1" ]; then
    echo Synopsis: $0 casefile.csv
    exit
fi

# if we find an argument without double dashes, then this is our
# input file; prepend it with a --input= option
CMDLINE=$(for x in $@; do \
    echo $x | grep "^--" 1> /dev/null 2>&1 && \
    echo $x || echo --input=$x; done | xargs)

# get the name of the directory from the stem of the input file
CASES=$(basename $((for x in $CMDLINE; do \
    echo $x | grep "^--input=" 1>/dev/null 2>&1 && \
    echo $x | sed "s/--input=\(.*\)/\1/"; done) | xargs) .csv)

# remove some special options only understood by the simulate 
# program (i.e. we don't want them forwarded to persson)
MESHONLY=0
CMDLINE=$(for x in $CMDLINE; do \
    echo $x | grep "^--mesh-only" 1>/dev/null 2>&1 && \
    MESHONLY=1 || echo $x; done | xargs)

# backup the previous run into a date-stamped directory
if [ -e $CASES ]; then
    LAST_DATE=$(find . -name $CASES -printf \%TY\%Tm\%Td)
    COUNT=1
    while [ -e $CASES.$LAST_DATE.$COUNT ]; do
	COUNT=$[$COUNT+1];
    done;
    mv $CASES $CASES.$LAST_DATE.$COUNT
fi

# run persson with the case file
mkdir -p $CASES
$(dirname $0)/persson.sh $CMDLINE --output=$CASES/stats.csv --keep=1

# get the list of cases from the mesh files that are generated
LIST=$(ls -1b mesh-*.ele | sed "s/mesh-\(.*\)\.[0-9]\+\.ele/\1/" | sort -u | xargs)

# create a subdirectory that will hold all cases; make sure that the common
# directory is at the same relative place that is usually are for the examples
test -d $CASES/pvt || test -h $CASES/pvt || ln -s $HOME/workspace/rs/ex/pvt $CASES/pvt

# create the directories and setup files
for d in $LIST; do
    mkdir -p $CASES/$d

    # copy the more refined mesh into the case-specific directory. end up with
    # the second refinement level since that is the default
    LATEST=$(ls -1b mesh-$d.*.ele | tail -1 | sed "s/mesh-.*\.\([0-9]\+\)\.ele/\1/")
    for e in ele node poly; do mv -f mesh-$d.$LATEST.$e $CASES/$d/mesh.2.$e; done
    for e in ele node poly; do rm -f mesh-$d*.$e; done

    # copy the latest figure file into the case directory, as a thumbnail
    if [ $(ls -1b fig-$d-*.eps 2>/dev/null | wc -l) -gt 0 ]; then
	LATEST=$(ls -1b fig-$d-*.eps | tail -1 | sed "s/fig-.*-\([0-9]\+\)\.eps/\1/")
	epstopdf --outfile=$CASES/$d.pdf fig-$d-$LATEST.eps
	rm -f fig-$d-*.eps
    fi

    # if there is not a case specific mesh or run file available, use common
    [ $MESHONLY -eq 1 ] && FILES="mesh" || FILES="mesh run"
    for f in $FILES; do cp -f $f$(test -e $f-$d && echo -$d) $CASES/$d/$f; done

    # clean output directories and files
    rm -rf $CASES/$d/gridding $CASES/$d/visualization $CASES/$d/simulation $CASES/$d/nohup.out
done

# clear any old queues
for q in $CASES/q*; do
    [ -f $q ] && rm -f $q
done
[ -e $CASES/done ] || cp /dev/null $CASES/done

# query the system for the capabilities for multiprocessing
CPUS=$(cat /proc/cpuinfo | grep "processor.*:" | wc -l)

# build a list of cases to handle in each worker thread. pick them round robin
# from the problem list -- this gives us good load balance when the problems are
# sorted from easy to hard
i=1
NUM_CASES=0
for p in $LIST; do
    # append to the queue for this processor
    echo $p >> $CASES/q$i
    
    # next case will be assigned to the next processor
    i=$[$i+1]
    [ $i -gt $CPUS ] && i=1

    # keep track of the number of cases
    NUM_CASES=$[$NUM_CASES+1]
done

# don't use more CPUS than necessary
[ $CPUS -gt $NUM_CASES ] && QUEUES=$NUM_CASES || QUEUES=$CPUS

# memory probe (subtract 128MB for the OS), divide on each processor
MAX_MEM=$(cat /proc/meminfo | grep "^MemTotal:" | sed "s@[^:]*:\ *\([0-9]\+\)\ kB@(\1-131072)/$QUEUES@g" | bc)

# configuration probe
JAVA_VERSION=$(java -version 2>&1)
IS_HOTSPOT=$(echo $JAVA_VERSION | grep HotSpot >/dev/null && echo 1 || echo 0)
IS_JROCKIT=$(echo $JAVA_VERSION | grep JRockit >/dev/null && echo 1 || echo 0)
IS_IBMJ9=$(echo $JAVA_VERSION | grep J9 >/dev/null && echo 1 || echo 0)

# alias to run the simulator
rs () {
    # vendor specific configuration ($i is the current queue number we are running in)
    [ $IS_HOTSPOT -eq 1 ] && \
        JAVA_OPTS='-Xbatch -XX:+UseParallelGC -XX:+UseAdaptiveSizePolicy -XX:ReservedCodeCacheSize=32m'
    [ $IS_JROCKIT -eq 1 ] && \
        JAVA_OPTS='-Xmanagement -Djrockit.managementserver.port='$[$i+7091]
    [ $IS_IBMJ9 -eq 1 ] && \
	JAVA_OPTS='-Xj9'

    # profiling; use the link below to analyze java.hprof.txt graphically
    # http://java.sun.com/developer/technicalArticles/Programming/perfanal/PerfAnal.jar
    #JAVA_OPTS='-agentlib:hprof=cpu=samples,depth=8,interval=1,thread=y '$JAVA_OPTS

    # modern JVMs won't use all of the heap if it isn't necessary
    JAVA_OPTS='-Xmx'$MAX_MEM'k '$JAVA_OPTS

    # common options for all machines
    JAVA_OPTS='-server -ea '$JAVA_OPTS
    CLASSPATH=$HOME/workspace/rs/bin:$HOME/workspace/rs/lib/mtj.jar

    /usr/bin/time -f "Seconds: %U" nice -n 5 java $JAVA_OPTS -cp $CLASSPATH no.uib.cipr.rs.Main $@
}

worker () {
    # loop over each item in the queue; process them sequentially
    for p in $(cat $CASES/q$1 | xargs); do
	cd $CASES/$p
	if [ $MESHONLY -eq 1 ]; then
	    (rs mesh && rs mesh_gmv) 1>>nohup.out 2>&1
	else
	    (rs mesh && rs run && rs run_gmv) 1>>nohup.out 2>&1
	fi
	cd ../..
	echo $p >> $CASES/done
    done
}

# spawn the worker thread for each queue in its own subshell
for (( i=1 ; i <= QUEUES ; i++ )); do
    [ -f $CASES/q$i ] && (worker $i) & disown -h %%
done
