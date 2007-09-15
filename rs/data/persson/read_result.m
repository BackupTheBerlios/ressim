% [vertices, edges, triangles] = read_result (stem, iteration)
%
% read a triangulation from file. the filename must be given as the stem, 
% i.e. without the extension and iteration counter, e.g. 'mesh'.
% vertices is an array of (x,y) coordinates, edges is an array of (i,j) 
% indices into the vertices array, whereas box is an matrix containing the 
% minimal and maximal coordinates on the same format as if they were two 
% points. triangles is an array of (a, b, c) tuples containing indices into 
% the vertices array.
function [vertices, edges, triangles] = read_result (stem, iteration)
   % Triangle dumps the results in three different files, read each of the
   % files and return all the results as a tuple of (related) matrices
   vertices = read_vertices (stem, iteration);
   edges = read_edges (stem, iteration);
   triangles = read_triangles (stem, iteration);