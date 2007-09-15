% [stats] = get_perf_data (stats)
%
% Merge performance data into the statistics
function [stats] = get_perf_data (stats)
  global perf_tot;
  stats = setfield (stats, 'time', perf_tot);