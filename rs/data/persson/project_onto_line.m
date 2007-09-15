% [proj_x, proj_y] = project_onto_line (point, line)
%
% Point is a 1-by-2 array containing x- and y-coordinates of the point,
% line is a 2-by-2 array containing the coordinates for the end-points
% (if you have an edges array containing indices to the points, then you
% can specify the line by passing 'vertices(edges(i), :)'). The function
% returns the coordinates of the projected point onto the line.
function [proj_x, proj_y] = project_onto_line (point, line)
  % decompose the point argument into its components
  x = point(1);
  y = point(2);

  % decompose the line argument into its components
  a_x = line(1, 1);
  a_y = line(1, 2);
  b_x = line(2, 1);
  b_y = line(2, 2);
  
  % view the line as a vector from the starting point to the end point;
  % we'll project the vector from the same starting point to the point
  % given as argument to this vector afterwards
  v_x = b_x - a_x;
  v_y = b_y - a_y;
  
  % let us call the original vector v, and the vector from the same
  % starting point to the point for u
  u_x = x - a_x;
  u_y = y - a_y;
  
  % scalar product between two vectors give us the projection relative to
  % the vector's length; by dividing on the latter we get the factor for a
  % parameterized line (k is between 0 and 1 on the line)
  u_dot_v = ( u_x * v_x ) + ( u_y * v_y );
  v_dot_v = ( v_x * v_x ) + ( v_y * v_y );  
  k = u_dot_v / v_dot_v;
  
  % if k is outside of the parameter domain 0.0->1.0 then the point ends
  % up outside of the line; truncate the parameter back so that everything
  % that is outside the line will snap back to the closest endpoint
  k = max (0.0, min (1.0, k));

  % use the parameter form of the line (vector form) to get the new point
  % on the line
  proj_x = a_x + k * v_x;
  proj_y = a_y + k * v_y;