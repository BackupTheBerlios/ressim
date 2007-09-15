% [new_points] = ...
%       remove_superfluous (vertices, edges, old_points, space, box, opts)
%
% Remove all auxiliary points that are too close to the fixed points
% relative to the optimal line length, since these will only cause slivers.
function [new_points] = ...
        remove_superfluous (vertices, edges, old_points, space, box, opts)
  % tolerance is specified relative to the optimal line length. the value
  % chosen here is two-thirds of the height in an equilateral triangle 
  tol = opts.('proximity'); % sqrt (3) / 6;
  
  % if we remove points on the border, then we get slivers from lines that
  % are normal (and close) to the border; if we don't remove then, then we
  % get slivers from lines that are parallel. we don't see if this is a
  % problem until we have attempted to apply the adjustment forces.
  dont_remove_on_border = opts.('border');
  
  % decompose the box into individual coordinates
  x_min = box(1, 1);
  y_min = box(1, 2);
  x_max = box(2, 1);
  y_max = box(2, 2);

  % array that will contain the points when we have processed them
  new_points = [];

  % process each point against every line
  for i = 1:size (old_points, 1)
    % we haven't considered any lines yet, so it can't be close to anything
    min_d = inf;
    
    % check against all the lines
    for j = 1:size (edges, 1)
      % find the distance from point i to edge j. note that only the first two
      % columns of the edge is used, since we are operating in 2D (the following
      % column describes the type of the edge).
      d = distance_from_line (old_points(i, :), vertices(edges(j, 1:2), :));
      
      % update the current record
      min_d = min (min_d, d);      
    end;
    
    % don't remove points that are on the border; those may typically be
    % near the lines, but will just generate slivers if we remove them
    x = old_points(i, 1);
    y = old_points(i, 2);
    on_border = (x == x_min) || (x == x_max) || (y == y_min) || (y == y_max);

    % only include the point if it is far enough from all lines
    if (min_d > space * tol) || (on_border && dont_remove_on_border)
      new_points = [new_points; old_points(i, :)];
    end;
  end;