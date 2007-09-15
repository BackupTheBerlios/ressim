% [d] = distance_from_line (point, line)
%
% Distance perpendicular to the line, i.e. if the point is on the line,
% then its distance will be zero.
function [d] = distance_from_line (point, line)
  % project the point onto the line; this will be the point on the line
  % that is closest to the point specified (which may be outside)
  [proj_x, proj_y] = project_onto_line (point, line);

  % decode the point into its components
  x = point(1);
  y = point(2);
  
  % find the distance between the point and its projection; this is the
  % shortest path from the point to the line
  v_x = proj_x - x;
  v_y = proj_y - y;
  d = sqrt (v_x .^ 2 + v_y .^ 2);
  
  %% this is the old implementation which uses a different approach, which
  %% really finds the distance from the point to the extension of the line
  %
  %% decode the line into its components
  %a_x = line(1, 1);
  %a_y = line(1, 2);
  %b_x = line(2, 1);
  %b_y = line(2, 2);
  %
  %% u is the vector that makes up the line
  %u_x = b_x - a_x;
  %u_y = b_y - a_y;
  %
  %% v is the vector that is perpendicular to this one; in two dimensions
  %% this is easy because there is no "front"-side and "back"-side
  %v_x =  u_y;
  %v_y = -u_x;
  %
  %% r is the vector from the point to the starting point of the line
  %r_x = a_x - x;
  %r_y = a_y - y;
  %
  %% distance is now given by projecting the vector from the point onto the
  %% vector that is perpendicular. see formula 11 on the page:
  %% http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
  %d = abs (v_x * r_x + v_y * r_y) / sqrt (v_x ^ 2 + v_y ^ 2);