% [vertices, edges, box] = read_poly (filename)
%
% Reads input from a file on Triangle's Planar Straight Line Graph format. This
% may be helpful if you have another program that does preprocessing and you
% only want to use Persson's algorithm to smoothen the grid instead of passing
% it directly to Triangle.
function [vertices, edges, box] = read_poly (filename)
   % open the file for read access
   fid = fopen (filename, 'r');
   
   % read first the section of the vertices
   vertices = read_vertex_section (fid);
   
   % then read the edges following further below in the same file
   edges = read_edge_section (fid);

   % ignore holes and regions -- we cannot handle that anyway
   
   % compose the box from the set of nodes
   x = 1; y = 2;
   box = [min(vertices(:,x)), min(vertices(:,y)); ...
          max(vertices(:,x)), max(vertices(:,y))];
   
   % return the handle to the operating system
   fclose (fid);   