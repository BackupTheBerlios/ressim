% [bars] = annotate_bars (bars, edges)
%
% Annotate a list of bars between points with type information found in edges.
%
% On input, the list of bars is an m-by-2 matrix containing indices of origin
% and destination points. edges is an n-by-3 matrix containing as the first two
% columns the same scheme as for the bars, and then in its last column a type
% identifier.
%
% On output, a matrix with 3 columns is returned, containing the same list of 
% bars as the original input, but if the same entry was found in the list of 
% edges, then the type identifier is copied to the third column of the output 
% array. Otherwise, the type identifier for the bar is set to zero.
function [annotated] = annotate_bars (bars, edges)
   % this number of rows in the merged list will contain edge information
   num_input = size (bars, 1);
   
   % create a merged set which starts out with all the bars and then the indices 
   % of the edges. those in common will thus now be listed twice. make sure that
   % the list of edges is sorted so that the smallest index is listed first, so
   % that it follows the same format as for the bars (see find_bars ()).
   merged = vertcat (bars, sort (edges(:, 1:2), 2));
   
   % coalesce all the tuples, figuring out which index was the first to have
   % such a row in the original
   [tuples, indices, original] = unique (merged, 'rows', 'last');
   
   % resulting number of unique bars found by above grouping
   num_output = size (tuples, 1);
      
   % pre-allocate memory for the annotated list, containing the same index
   % tuples as the unique list, but with one more column
   annotated = zeros (num_output, 3);
   
   % post-process each record to find the type identifier
   for i = 1:num_output,
      % where was this record in the original list of bars?
      j = original (i);
      
      % use the locator information to figure out from where this tuple
      % originated
      ndx = indices(j);
      
      % if the tuple comes from the first part of the merged list, then it was
      % an edge and we should copy the type identifier from the edge; otherwise
      % use zero as the type identifier.
      if ndx > num_input,
         kind = edges(ndx - num_input, 3);
      else
         kind = 0;
      end;
      
      % write an annotated record
      annotated(i, :) = horzcat(tuples(j, :), kind);
   end;