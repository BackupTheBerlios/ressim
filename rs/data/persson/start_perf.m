% [] = start_perf (phase)
%
% Indicate that work has started in a given phase, as indicated by the
% string parameter.
function [] = start_perf (phase)
  % note the current toc value into the phase's current counter
  global perf_cur;
  perf_cur = setfield (perf_cur, phase, toc);