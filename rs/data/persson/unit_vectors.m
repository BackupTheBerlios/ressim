% [vectors] = unit_vectors (vertices, edges)
%
% Calculate the unit vectors of each edge. An edge is specified by the origin
% and the target index into a list of vertices. Each vertex has the (absolute)
% coordinates in two dimensions. Returned is an n-by-2 matrix that contains the
% x and y components of the unit vector for each edge (in the same order).
function [u] = unit_vectors (vertices, edges)
   % psuedo-constants to address structure fields in the arrays
   x = 1; y = 2; origin = 1; target = 2;

   % calculate the distance along each of the directions for the edges
   dx = vertices(edges(:, target), x) - vertices(edges(:, origin), x);
   dy = vertices(edges(:, target), y) - vertices(edges(:, origin), y);
   
   % find the length of each edge
   lengths = sqrt (dy.^2 + dx.^2);
   
   % divide the vectors by the lengths to get the unit vectors (direction vector
   % with length one)
   u = [dx ./ lengths, dy ./ lengths];