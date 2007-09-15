% [vertices, edges, box] = read_txt (filename, opts)
%
% Read coordinates from a file with a space/tab-delimited format that can be
% loaded by Matlab.
function [vertices, edges, box] = read_txt (filename, opts)
   % all fractures are of type 0, i.e. there is no difference between them
   c = 0;

   % load the entire file as raw coordinates; this is assumed to be four columns
   % containing (x1,y1)-(x2,y2)
   coords = load (filename, '-ASCII');
   
   % sanity check
   num_of_rows = size (coords, 1);
   num_of_cols = size (coords, 2);
   if num_of_cols ~= 4,
      error ('File must have four columns containing pairs of 2D coordinates');
   end;
   
   % pseudo-constants; a is the source coordinate, b the target. x the first
   % axis, while y the second.
   ax = 1; ay = 2; bx = 3; by = 4; num_of_dims = 2;
   
   % initial state of the output data arrays (we know the number of edges, but
   % not the number of unique points)
   vertices = [];
   edges = zeros (num_of_rows, num_of_dims+1);

   % infer the box from the coordinates that were passed
   box = infer_box (vertcat (coords(:, ax:ay), coords(:, bx:by)));

   % process each line, creating a point for both ends and then put their
   % indices into the edge array
   for i = 1:num_of_rows,
      % create a vector for each of the points
      source = horzcat (coords(i, ax), coords(i, ay));
      target = horzcat (coords(i, bx), coords(i, by));
      
      % add to our collection, translating coordinates to indices
      [vertices, a] = add_point (vertices, source, box, opts);
      [vertices, b] = add_point (vertices, target, box, opts);
      
      % put together to an edge
      edges(i, :) = [a, b, c];
   end;