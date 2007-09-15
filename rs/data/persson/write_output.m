% [] = write_output (stem, iteration, vertices, edges, triangles, box)
%
% Write a set of output files on the same format as Jonathan Shewchuk's
% Triangle program. These may then be used for further refinement, or for
% import into the simulator.
% 
% stem is the prefix of the files, e.g. 'mesh', iteration is the
% iteration counter, e.g. 1, vertices is an m-by-2 array containing the
% coordinates of each referenced node, edges is an n-by-2 array of
% indices into the node array above describing the constraints and
% triangles is an array of three indices describing which points that are
% connected in the triangulation.
function [] = write_output (stem, iteration, vertices, edges, triangles, box)
  write_nodes (stem, iteration, vertices, box);
  boundary = recover_boundary (vertices, box);
  write_edges (stem, iteration, vertices, [edges; boundary], box);
  write_triangles (stem, iteration, triangles);
