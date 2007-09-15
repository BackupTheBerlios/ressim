% [stats] = main_once (opts, title)
%
% Run through the main program once with a given set of options. Returns
% the statistics for the triangles generated for this case.
function [stats] = main_once (opts, title)
  % get fractures from the source specified in the options; this routine
  % will branch off to the appropriate generator
  [vertices, edges, box] = build_fractures (opts);
  
  % override box if a different domain has been specified on command-line
  if ~isempty (getfield (opts, 'clip')) && ~isempty (getfield (opts, 'size')),
     min = 1; max = 2;
     box(min, :) = getfield (opts, 'clip');
     box(max, :) = box(min, :) + getfield (opts, 'size');     
  end;
  
  % make sure that all points are inside the domain (try using clip_lines if
  % this implementation does not work).
  [vertices, edges] = clip_edges (vertices, edges, box, opts);

  % debugging
  debug_probe (vertices, edges, [], box, inf, 1, opts, title);  

  % make a triangle mesh using the external helper application
  if isfield (opts, 'persson') && opts.('persson')
    [vertices, edges, triangles] = ...
                                  create_mesh (vertices, edges, box, opts);
  else
    [vertices, edges, triangles] = ...
                              make_triangles (vertices, edges, box, opts);
  end;
  
  % write final mesh if a different format is desired
  write_final (vertices, edges, triangles, box, opts);

  % show the triangles on screen afterwards
  %visualize_results (vertices, edges, triangles, box);
  debug_probe (vertices, edges, triangles, box, inf, 8, opts, '');

  % show statistics
  stats = report_statistics (vertices, triangles);
  stats = get_perf_data (stats);
  %display_statistics (stats);
  