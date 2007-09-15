% [triangles] = read_triangles (stem, iteration)
%
% read from a file the results that Triangle has generated, in the form of 
% indices into an (otherwhere specified) point array. the filename must be 
% given by its stem, i.e. without the extension and iteration counter, e.g.
% 'mesh'.
function [triangles] = read_triangles (stem, iteration)
   % open the file for read access, adding the correct extension
   fid = fopen (make_name (stem, iteration, 'ele'), 'r');
   
   % parse the header row. first entry is the number of elements, second is
   % the number of edges (which should always be three) and the last one is
   % the number of section attributes for each element (we don't have any)
   num_of_triangles = fscanf (fid, '%d', 1);
   num_of_edges = fscanf (fid, '%d', 1); % should always be 3
   num_of_attrs = fscanf (fid, '%d', 1); % should always be 0
   
   % each line will have the index number, then the dimension number of
   % indices and finally the attributes
   entries_per_line = 1 + num_of_edges + num_of_attrs;
   
   % start out with an empty set
   triangles = [];

   % read one triangle from each line. the first entry on the line is
   % always the index (which should be a running number)
   for i = 1:num_of_triangles
      entry = fscanf (fid, '%f', entries_per_line)';
      
      % first entry of the row is the index number
      index = entry(1);
         
      % the number 2 in the line below appears because the indices start
      % at the second element
      triangles(index,:) = [entry(2:2+num_of_edges-1)];
   end;
   
   % return the handle to the operating system
   fclose (fid);
