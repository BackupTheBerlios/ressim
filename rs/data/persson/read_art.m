% [vertices, edges, box] = read_art (filename, opts)
%
% Read a file on ART (Almost Regular Triangulation) input format. External
% boundaries are in the input file marked with positive tags, whereas internal
% boundaries are given negative tags.
function [vertices, edges, box] = read_art (filename, opts)
   % open the file for reading
   fid = fopen (filename, 'r');
   
   % read header and pre-allocate matrices
   [num_vertices, num_edges] = read_art_header (fid);
   
   % read raw vertices and edges data from the file
   vertices = read_art_section (fid, '%f %f %f',  3, num_vertices);
   edges    = read_art_section (fid, '%d: %d %d', 3, num_edges);
   
   % ditch the last coordinate for 2D
   vertices = vertices (:, 1:2);
   
   % kind of fracture must be the last column in our code. note that we use
   % one-based indices, whereas the ART program uses zero-based indices.
   edges = [edges(:,2)+1, edges(:,3)+1, edges(:,1)];
   
   % we use negative type indicators to signal external boundary and positive to
   % represent internal constraint, so change sign.
   edges(:, 3) = -edges(:, 3);

   % we use zero and one as special designators, so remap all non-negative
   % indices from 1:inf-1 to the range 2:inf instead. negative numbers are left 
   % intact and remains through the program. no type identifier of zero is
   % allowed in the input.
   positive = gt (edges(:, 3), 0);
   edges(positive, 3) = edges(positive, 3) + 1;   
   
   % get the bounding box
   box = infer_box (vertices);
   
   % return the resource handle to the operating system
   fid = fclose (fid);