% [vertices] = read_vertices (stem, iteration)
%
% read from a file the nodes that Triangle has generated. the filename must
% be given by its stem, i.e. without the extension and iteration counter,
% e.g. 'mesh'.
function [vertices] = read_vertices (stem, iteration)
   % open the file for read access, adding the correct extension
   fid = fopen (make_name (stem, iteration, 'node'), 'r');
   
   % read the section from the open file (this is the only section)
   vertices = read_vertex_section (fid);

   % return the handle to the operating system
   fclose (fid);
