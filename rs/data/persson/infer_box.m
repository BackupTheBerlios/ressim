% [box] = infer_box (vertices)
%
% Infer the minimal and maximal coordinates from vertices, creating a box. This
% is useful for data formats that does not specify the bounding box.
function [box] = infer_box (vertices)
   % constants used to refer to dimensions
   x = 1; y = 2;

   % pre-allocate matrix size
   box = zeros (2, 2);

   % extract bounding box from vertex data
   box(1, x) = min (vertices(:,x)); % min_x
   box(2, x) = max (vertices(:,x)); % max_x
   box(1, y) = min (vertices(:,y)); % min_y
   box(2, y) = max (vertices(:,y)); % max_y
