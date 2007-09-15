% [segments] = determine_spacing (vertices, edge, x_space, y_space)
%
% Find the number of segments in which the line should be divided to
% yield closest to the desired spacing in each direction. One cannot
% achieve perfect spacing unless the line in question is a vector that is
% the same as [x_space y_space].
function [segments] = determine_spacing (vertices, edge, x_space, y_space)
  % Find the width and height of the line based on the coordinates
  width  = abs (vertices(edge(2), 1) - vertices(edge(1), 1));
  height = abs (vertices(edge(2), 2) - vertices(edge(1), 2));
  
  % find the number of segments (fractions allowed) we should ideally
  % divide each side in to get the desired spacing in each dimension
  nx = width  / x_space;
  ny = height / y_space;
  
