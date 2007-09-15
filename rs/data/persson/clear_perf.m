% [] = clear_perf ()
%
% Restart performance counter data.
function [] = clear_perf ()
  % perf_cur is the currently elapsed time into the given phase, whereas
  % perf_tot is the total time worked in each phase.
  clear global perf_cur;
  clear global perf_tot;
  global perf_cur;
  global perf_tot;
  
  % initialize to zero so we don't have to do isfield on every assignment
  perf_cur = struct ('prep', 0, 'triangle', 0, 'adjust', 0);
  perf_tot = struct ('prep', 0, 'triangle', 0, 'adjust', 0);
  
  % restart timer to reduce the risk of overflow in use of toc() later
  tic;