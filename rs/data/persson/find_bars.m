% [bars, triangles] = find_bars (triangles)
%
% Return a list of all bars in a triangulation. A bar is an edge between
% two points. This function does not need to know the coordinates of the
% points. It returns an array of n-by-2 indices from an array of m-by-3
% indices (into the vertex set). 
%
% On output the triangles is rewritten so that they point to the bars instead of 
% the vertices.
function [bars, triangles] = find_bars (triangles)
   % create a set of three bars per triangle; most sides will be duplicated
   % by their neighbour
   bars = [triangles(:, [1, 2]); ...
           triangles(:, [2, 3]); ...
           triangles(:, [3, 1])];
        
   % edges are undirected; sort them so that the point with the lowest
   % index always comes first
   bars = sort (bars, 2);
   
   % remove duplicate rows; since we have sorted the pairs internally
   % first, two bars with the same end-points should now have been
   % collapsed into one row
   [bars, indices, original] = unique (bars, 'rows');
   
   % reconstruct the triangles based on the indices of the bars, not the
   % vertices that was the end-point of those bars
   count = size (triangles, 1);
   for i = 1:count
      for j = 1:3,
         triangles (i, j) = original (count * (j-1) + i);
      end;
   end;