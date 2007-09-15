% [edges] = read_edges (stem, iteration)
%
% read from a file the constraints that Triangle has processed. these may
% be different from the input since more points are possibly added. the 
% filename must be given by its stem, i.e. without the extension and
% iteration, e.g. 'mesh'. the third column is an array containing a flag (zero 
% or one respectively) that indicates whether this edge is genuine or a border.
function [edges] = read_edges (stem, iteration)
   % open the file for read access, adding the correct extension
   fid = fopen (make_name (stem, iteration, 'poly'), 'r');
   
   % first row contains the same as the vertices file; however we are only
   % interested in the number of points that should be skipped. also, we
   % must skip the rest of the header line itself (thus adding one).
   num_of_nodes = fscanf (fid, '%d', 1);
   for i = 1:num_of_nodes + 1
      fgets (fid);
   end;

   % read the section from the open file (this is the only section)
   edges = read_edge_section (fid);
   
   % only keep (all column of) those edges that is not boundaries (type 1)
   edges = edges(edges(:, 3) ~= 1, :);
   
   % return the handle to the operating system
   fclose (fid);
