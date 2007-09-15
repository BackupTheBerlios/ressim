% [stat] = empty_stats (opts)
%
% Generate a structure that can be merged with other statistics, but which
% contain no data (usable for lines that should be skipped but must still
% be present in the output file -- we want to keep commented lines since
% they are usable as input for another iteration)
function [stat] = empty_stats (opts)
  % start out with an empty structure; we'll add fields afterwards
  stat = struct ();

  % name of all the fields that may have been passed to us as "options" if
  % read from an output file
  names = {'count_vertices',  'count_triangles',  ...
           'time_prep',       'time_triangle',    'time_adjust', ...
           'size_mean',       'size_stddev',      'size_pct', ...
           'alt_mean',        'alt_stddev',       'alt_pct', ...
           'angle_mean',      'angle_stddev',     'angle_pct'};
  
  % if a field is present, then copy it to the output structure; otherwise
  % use zero as the default value
  for i = 1:length(names)
    if isfield (opts, names{i})
      value = getfield (opts, names{i});
    else
      value = 0;
    end;
    stat = setfield (stat, names{i}, value);
  end;
