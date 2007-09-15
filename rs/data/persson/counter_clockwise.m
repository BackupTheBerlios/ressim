% [ccw] = counter_clockwise (vertices, edges, indices, box)
%
% Returns the list of edges such that they are sorted counter-clockwise around
% the center of the box. Indices is a list that references into edges.
function [ccw] = counter_clockwise (vertices, edges, list, box)
   % psuedo-constants
   x = 1; y = 2;  % indices into vertex fields
   a = 1; b = 2;  % indices into edge fields

   % all edges should be counter-clockwise around the center of the box, so we
   % need to find this pivot point.
   center = horzcat (mean (box(:, x)), mean (box(:, y)));
   
   % find the first (x-) and second (y-) coordinates for the start and the end
   % of the line, respectively
   fst = horzcat (vertices(edges(list, a), x), vertices(edges(list, b), x));
   snd = horzcat (vertices(edges(list, a), y), vertices(edges(list, b), y));
   
   % midpoint is between the start and the end, in each dimension
   midpoints = horzcat (mean (fst')', mean (snd')');
   
   % vectors between the midpoints of each edge and the center of the box
   distances = midpoints - ones (length (midpoints), 1) * center;
   
   % tangent of an angle is the fraction between the y and the x coordinate; we
   % can find the angle (for each edge) by using the arc
   angles = atan2 (distances(:,y), distances(:,x)) + 2 * pi;
   
   % there is one angle for each entry in the original list; sort these angles
   % and use the indices of their original list to pick out the edges that
   % should form the counter-clockwise list
   [dummy, indices] = sortrows (angles);
   ccw = list(indices);