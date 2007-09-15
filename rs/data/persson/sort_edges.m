% [vertices, edges] = sort_edges (vertices, edges)
%
% Sort the vertices on coordinate and the edges on kind and then on indices.
function [vertices, edges] = sort_edges (vertices, edges)
   % pseudo-constants used to identify "fields" in the vector "structures"
   x = 1; y = 2;
   a = 1; b = 2; c = 3;

   % indices is now setup so that it contains the original position of each of
   % the new elements, i.e. if original(i) == j, then the element which is now at
   % position i, was originally at j.
   [vertices, original] = sortrows (vertices, [y, x]);
   
   % invert the index matrix so that we get an indices table which tells us the
   % current position of an old index, i.e. if indices(j) == i, then the element
   % which is now at position i was originally at position j
   indices = zeros (length (original), 1);
   for i = 1:length (indices),
      j = original (i);
      indices(j) = i;      
   end;
   
   % convert our edge map using the new indices for vertices
   for i = 1:length (edges),
      % extract information
      origin = edges(i, a);
      target = edges(i, b);
      color  = edges(i, c);
      
      % remap
      origin = indices(origin);
      target = indices(target);
      
      % recompose this edge, using new indices
      edges(i, :) = [origin, target, color];
   end;
   
   % sort the edges afterwards
   bars  = edges(:, a:b);
   bars  = sort (bars, 2);
   bars  = annotate_bars (bars, edges);
   
   % sort external and internal edges differently
   host_rock = 0;
   external = find (lt (bars(:, c), host_rock));
   internal = find (gt (bars(:, c), host_rock));
   between  = find (eq (bars(:, c), host_rock));
   
   edges = vertcat ( ...
      sortrows (bars(external, :), [-c, a, b]), ...
      sortrows (bars(internal, :), [ c, a, b]), ...
      sortrows (bars(between,  :), [    a, b]));