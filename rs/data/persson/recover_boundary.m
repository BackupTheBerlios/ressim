% [edges] = recover_boundary (vertices, box, opts)
%
% Generate edges that is part of the boundary from the points. The edges
% array that is returned will be an m-by-2 array containing indices into
% the vertices array.
function [edges] = recover_boundary (vertices, box)
  % coordinates from the bounding box
  x_min = box(1, 1);
  y_min = box(1, 2);
  x_max = box(2, 1);
  y_max = box(2, 2);
  
  % start out with no lines found
  left = [];
  bottom = [];
  top = [];
  right = [];
  
  % all edges that are boundaries have type = 1
  t = 1;

  % tolerance for deviation from true value
  tol = 10 * eps;  
  
  % scan through the set of points to find all candidate lines. note that
  % each of the four corners will be part of two arrays. first element on
  % each row is the index of the point, second element is the other
  % coordinate than the one we're testing, which makes us able to walk
  % along the edge afterwards).
  for i = 1:size (vertices)
    % points that are on the left boundary
    if abs (vertices(i, 1) - x_min) < tol,
      left = [left; i vertices(i, 2)];
    end;
    
    % points that are on the right boundary
    if abs (vertices(i, 1) - x_max) < tol,
      right = [right; i vertices(i, 2)];
    end;
    
    % points that are on the lower boundary
    if abs (vertices(i, 2) - y_min) < tol,
      bottom = [bottom; i vertices(i, 1)];
    end;
    
    % points that are on the upper boundary
    if abs(vertices(i, 2) - y_max) < tol,
      top = [top; i vertices(i, 1)];
    end;
  end;
  
  % sort each array so that we walk through the points in a clock-wise
  % direction (doesn't matter which direction as long as we do it the
  % same way for all arrays).
  left = sortrows (left, 2);
  top = sortrows (top, 2);
  right = sortrows (right, -2);
  bottom = sortrows (bottom, -2);
  
  % generate points by walking through each pair of consecutive points in
  % the four arrays. only write the pair of points if they constitute a
  % line with a non-zero length.
  edges = [];
  for i = 1:size (left, 1) - 1
    if left(i, 2) ~= left(i+1, 2)
      edges = [edges; left(i, 1) left(i+1, 1), t];
    end;
  end;
  for i = 1:size (top, 1) - 1
    if top(i, 2) ~= top(i+1, 2)
      edges = [edges; top(i, 1) top(i+1, 1), t];
    end;
  end;
  for i = 1:size (right, 1) - 1
    if right(i, 2) ~= right(i+1, 2)
      edges = [edges; right(i, 1) right(i+1, 1), t];
    end;
  end;
  for i = 1:size (bottom, 1) - 1
    if bottom(i, 2) ~= bottom(i+1, 2)   
      edges = [edges; bottom(i, 1) bottom(i+1, 1), t];
    end;
  end;
