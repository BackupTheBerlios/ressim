% [vertex_use_counts] = tally (edges, num_of_vertices)
%
% Determine the number of times each vertex has been used in the list of edges
% that are passed. The returned vector will have one entry for each vertex.
function [v_use] = tally (edges, num_of_vertices)
   % start out by assuming that each vertex will be marked as unused unless we
   % find some edges that says otherwise. this pre-allocates the return array.
   v_use = zeros (num_of_vertices, 1);
   
   % put all vertex references (from both the origin and the target) onto the
   % same column (we don't care at which end a vertex is referenced)
   v_ref = edges(:, 1:2); v_ref = v_ref(:);
   
   % find the indices of the vertices that are being referred
   [indices, pos] = unique (sort (v_ref), 'first');
   
   % since we sorted the list, the positions determine how many items preceed
   % that particular; if we subtract all the items before, we get the number of
   % items that has this particular number.
   pos = [pos; length(v_ref)+1];
   count = pos(2:end) - pos(1:end-1);
   
   % set the use count for each vertex that we found to the count we calculated
   v_use(indices) = count;