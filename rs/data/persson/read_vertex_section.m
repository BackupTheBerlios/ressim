% [vertices] = read_vertex_section (fid)
%
% Read the node section from an open file. This routine is internal and should
% not be used directly.
function [vertices] = read_vertex_section (fid)
   % parse the header row, reading four integers. first entry in the header 
   % row is the number of points
   num_of_nodes = fscanf (fid, '%d', 1);
   num_of_dims = fscanf (fid, '%d', 1);
   num_of_attrs = fscanf (fid, '%d', 1);
   boundary = fscanf (fid, '%d', 1);
   
   % each line will have the index number, then the dimension number of
   % coordinates and finally the attributes and the boundary marker
   entries_per_line = 1 + num_of_dims + num_of_attrs + boundary;
   
   % start out with an empty set
   vertices = [];
   
   % read each line of the file into the vertices array. note that each
   % line is transposed before being added to the end of vertices array
   for i = 1:num_of_nodes
      entry = fscanf (fid, '%f', entries_per_line)';
      
      % first entry of the line is the index
      index = entry(1);
      
      % add all nodes, even if they are on the boundary (they will be part
      % of the triangle corners). the number 2 in the line below appears 
      % because the indices start at the second element
      vertices(index,:) = [entry(2:2+num_of_dims-1)];
   end;