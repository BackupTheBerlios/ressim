% [border] = hull (vertices, edges, box)
%
% Identify the edges that are part of the border, i.e. the edges that
% have the interior on one side and the exterior on the other. The edges
% are returned in a random order, and neither can anything be assumed
% about the direction of each border. It is assumed that the domain is
% rectangular.
function [border] = hull (vertices, edges, box)
   % psuedo-controls
   x = 1; y = 2; minimal = 1; maximal = 2; a = 1; b = 2;

   % flags for each vertex on whether it is part of the box or not
   flag = or (or (or ( ...
            eq (vertices(:, x), box(minimal, x)),  ...
            eq (vertices(:, x), box(maximal, x))), ...
            eq (vertices(:, y), box(minimal, y))), ...
            eq (vertices(:, y), box(maximal, y)));

   % for each edge, figure out how many of its vertices are on the
   % border (border edges will have both)
   count = flag(edges(:, a)) + flag(edges(:, b));

   % identify those edges that are completely part of the border
   border = find (eq (count, 2));
   
   % transpose to get a row vector (instead of a column vector) so that it
   % counts as one element with multiple entries
   border = border';
