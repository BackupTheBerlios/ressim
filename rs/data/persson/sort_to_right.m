% [u, edges] = sort_to_right (u, edges)
%
% Shift any vectors that is in a decreasing x-direction. The result is a set of
% vectors and edges that all points to the right.
function [u, edges] = sort_to_right (u, edges)
   % psuedo-constants to address structure fields in the data matrices
   x = 1; y = 2; a = 1; b = 2;
   
   % find all those edges that are in need of a transformation
   to_left = find (or (lt (u(:, x), 0.), ...
                  and (eq (u(:, x), 0.), lt (u(:, y), 0.))));
   
   % change the direction vector to the other way
   u(to_left, x:y) = -u(to_left, x:y);
   
   % swap origin and target for those edges
   edges(to_left, a:b) = [edges(to_left, b), edges(to_left, a)];