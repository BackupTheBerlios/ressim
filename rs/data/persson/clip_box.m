% [vertices] = clip_box (vertices, box)
%
% Make sure that no point is outside of the specified box. Since edges are 
% simply indices into vertices, we only have to fix the vertices, and the 
% edges will be automatically adjusted. This function may be necessary if
% the fracture source does not care about having all points strictly inside
% the boundary.
function [vertices] = clip_box (vertices, box)
  % psuedo-constants to make the code below more readable
  lower = 1; upper = 2; x = 1; y = 2;
  
  % read from the box
  min_x = box (lower, x);
  min_y = box (lower, y);
  max_x = box (upper, x);
  max_y = box (upper, y);

  % loop through each vertex and process them individually
  for i = 1:length (vertices)
    vertices(i, x) = max (min (vertices(i, x), max_x), min_x);
    vertices(i, y) = max (min (vertices(i, y), max_y), min_y);
  end;