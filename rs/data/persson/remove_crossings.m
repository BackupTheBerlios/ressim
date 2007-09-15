% [vertices, edges] = remove_crossings (vertices, edges, box, opts)
%
% Make sure that no edges overlap each other. If they do, they are split
% into smaller edges that are adjacent and meet at the intersection.
function [vertices, edges] = remove_crossings (vertices, edges, box, opts)
  % compare each edge to every other. we don't have to compare edges to
  % themselves, and if we have compared e1 and e2, then we don't have to
  % compare e2 to e1 again. note that it follows that j is always larger
  % than i, so if we advance i then we should also advance j but not the
  % other way around. we use unbounded loops since the size of the set
  % may change during the loop (if we split an edge, then we must check
  % both the two new edges against the rest of the edges)
  i = 1; 
  while i <= size (edges, 1)
    j = i+1; 
    while j <= size (edges, 1)
      
      % figure out if these two lines cross each other
      pt = lines_intersect (vertices, edges(i, :), edges(j, :));
      
      % if the two lines intersect, then we should split both of them at
      % the intersection, generating four new lines (growing the size of
      % each collection)
      if size (pt, 2) ~= 0         
        % after we have added a new edge in the place of i, then we need
        % to adjust j correspondingly
        [vertices, edges, delta_j_for_i] = ...
                                 split_edge (vertices, edges, i, pt, box, opts);
        j = j + delta_j_for_i;
                
        % we have already checked if j and j+1 intersect with the current
        % line. thus we skip another one of them.a line cannot intersect 
        % another straight line more than once (unless they are totally 
        % overlapping).
        [vertices, edges, delta_j_for_j] = ...
                                 split_edge (vertices, edges, j, pt, box, opts);
        j = j + delta_j_for_j;
      end;
      
      % advance the iteration to the next edge
      j = j + 1;
    end;
    i = i + 1;
  end;
