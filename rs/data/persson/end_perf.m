% [] = end_perf (phase)
%
% Notify the performance counter subsystem that the given phase has ended
function [] = end_perf (phase)
  % get the time as soon as possible so that we don't waste time measuring
  % the performance of the profiling code
  finished = toc;
  
  % calculate how much time we have used since the phase was reported to
  % start
  global perf_cur;
  start = getfield (perf_cur, phase);
  elapsed = finished - start;
  
  % write the elapsed time for this session into the global counter for the
  % phase, giving us the total time spent in that part of the code.
  global perf_tot;  
  accumulated = getfield (perf_tot, phase);
  accumulated = accumulated + elapsed;
  perf_tot = setfield (perf_tot, phase, accumulated);