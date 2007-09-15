% [vertices, edges, triangles] = create_mesh (fixed, edges, box, opts)
%
% Create mesh with approximately num elements, with no sides crossing the
% lines given in edges (which refers to points in fixed). Box is the entire
% domain for the mesh (given as lower left and upper right point). The
% function returns all the vertices (as an array of coordinates for the
% points), the edges that makes up (dimension-reduced) constraints (as an 
% array of indices to points) and an array of indices to points that are
% the corners of the triangle elements. Options that control the tunables
% may be passed.
function [vertices, edges, triangles] = ...
                                      create_mesh (fixed, edges, box, opts)
  % get the number of elements desired in the mesh; capacity in the flow
  % simulator is the limitation.
  num = max (1, getfield (opts, 'elements'));

  % override this setting anyway; we don't want the Triangle program to do
  % any refinement of the triangles since we'll be generating the ideal
  % number and position of them ourselves.
  opts = setfield (opts, 'steiner', 0);
  opts = setfield (opts, 'refine',  1);
  
  % start the clock to see how much time we are using to prepare mesh
  start_perf ('prep');

  % remove any crossing edges and superfluous points from the initial set;
  % the set that results from this should then be clean for "noise".
  [fixed, edges] = remove_crossings (fixed, edges, box, opts);
  [fixed, edges] = compact (fixed, edges, [], box, opts);

  % put interior nodes throughout the empty space. note that here we do
  % not remove points that get close to the constraints, but rather let
  % them work out the space between them. this scheme will cover all the
  % corners in the box, so it should not be necessary to add any points
  % after this.
  [moveable, space] = evenly_spaced (num, box);

  % take away extra points that are too close to the line to be of any use
  moveable = remove_superfluous (fixed, edges, moveable, space, box, opts);

  % make sure that there is enough points on the lines themselves so that
  % we don't get long slivers along them. flexible is the subset of the
  % fixed points that are on the interior of the lines.
  [fixed, edges, map] = partition_edges (fixed, edges, space, box, opts);

  % consider both sets combined when creating a triangulation. this
  % function will assign constraints to each point. from this point on we
  % don't assume that the vertices array changes since we are going to
  % have more information for each point in the constraints array (each
  % row in those two arrays are connected into an object).
  [vertices, constraints] = grade_edges (fixed, edges, map, moveable);
  
  % write performance counter for initial phase
  end_perf ('prep');

  % force retriangulation the first time
  rel_move = inf;
  dptol = opts.('tolerance');     % 0.05;
  ttol = opts.('retriangulate');  % 0.1;
  max_iter = opts.('maxiter');    % 200;
  
  num_iter = 1;
  while (rel_move > dptol) && (num_iter <= max_iter)
    % create triangles that fits between the edges and points
    % note that the box is already added to the input. only retriangulate
    % if there has been a sufficiently large movement (notice that we have
    % greater or equal, which means that we have to retriangulate once when
    % rel_move is initialized to positive infinite).
    if rel_move >= ttol
      [vertices, edges, triangles] = ...
                               make_triangles (vertices, edges, box, opts);                                                           
    end;

    % calculate the spring force that each point puts on its neighbours
    % and update the position of the vertices accordingly
    start_perf ('adjust');
    bars = find_bars (triangles);
    forces = calc_forces (vertices, bars, space, opts);
    [vertices, max_move] = apply_forces (vertices, forces, ...
                                                   constraints, box, opts);
    rel_move = max_move / space;
    end_perf ('adjust');

    % debugging
    debug_probe (vertices, edges, triangles, box, rel_move, 4, opts, '');
    
    % another iteration accomplished
    num_iter = num_iter + 1;
  end;
