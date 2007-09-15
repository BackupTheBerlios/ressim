% [vertices, edges, triangles] = make_triangles (vertices, edges, box, opts)
%
% triangularize a constrained area. vertices is an array of (x,y) 
% coordinates, edges is an array of (i,j) indices into the vertices array,
% whereas box is an matrix containing the minimal and maximal coordinates
% on the same format as if they were two points. triangles is an array of
% (a, b, c) tuples containing indices into the vertices array. note that
% the function may change the array of vertices and update the array of
% edges (the old vertices and edges is not relevant in the definition of
% the triangles anymore). opts is a structure containing tunables that are
% passed to the external program.
function [vertices, edges, triangles] = ...
                                make_triangles (vertices, edges, box, opts)
  % name of the case is hard-coded
  stem = 'mesh';
  
  % append the identity of the setup to the name, in case the user want to
  % keep these files laying around
  if isfield (opts, 'id') && ~isempty (getfield (opts, 'id'))
    stem = sprintf ('%s-%s', stem, val2str (getfield (opts, 'id')));
  end;

  % only do one iteration at first, unless otherwise specified. we must
  % have at least one iteration through the triangulation.
  if isfield (opts, 'refine')
    iterations = max (1, getfield (opts, 'refine'));
  else
    iterations = 1;
  end;

  % spill the input to files, run the program and then pickup the output
  % from other files again
  write_initial (stem, vertices, edges, box, opts);
  for i = 1:iterations
    % find command-line parameters from the tunables specified
    args = translate_params (box, i, iterations, opts);    
    
    start_perf ('triangle');
    run_triangle (stem, i, args, opts);
    end_perf ('triangle');

    % read the output from the program back into variables (must store to
    % disk to communicate between programs).
    [vertices, edges, triangles] = read_result (stem, i);
    
    % debugging
    debug_probe (vertices, edges, triangles, box, inf, 2, opts);    
  end;
  
  % make sure that the vertices are conforming to our grid size (triangulation
  % may have pertubated the coordinates slightly when writing the result)
  vertices = snap_to_grid (vertices, box, opts);
    
  % dump the final result to an output file
  if isfield (opts, 'keep') && getfield (opts, 'keep')
    % pretend that we always have two iterations, since that is what the
    % import filter for Triangle into the simulator expects. (replace '2' 
    % with 'iterations' below to get the old behaviour back)
    write_output (stem, 2, vertices, edges, triangles, box);
  else
    % delete intermediate files when we're done
    cleanup_files (stem, iterations);    
  end;
