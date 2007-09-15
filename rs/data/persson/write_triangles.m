% [] = write_triangles (stem,  iteration, triangles)
%
% Write a set of output files on the same format as Jonathan Shewchuk's
% Triangle program. These may then be used for further refinement, or for
% import into the simulator.
% 
% stem is the prefix of the files, e.g. 'mesh', iteration is the
% iteration counter, e.g. 1, triangles is an array of three indices
% describing which points that are connected in the triangulation.
function [] = write_triangles (stem, iteration, triangles)
  % open the file for writing
  fid = fopen (make_name (stem, iteration, 'ele'), 'W');
  
  % header contains key information about the structure of the file
  num_of_tris = size (triangles, 1);
  num_of_pnts = 3;    % for each triangle
  num_of_attr = 0;    % no extra data associated with each element
  fprintf (fid, '%d %d %d\n', num_of_tris, num_of_pnts, num_of_attr);
  
  % write the indices for each triangle to the file together with their
  % index. we don't need the coordinates of each point.
  for i = 1:num_of_tris
    a = triangles(i, 1);
    b = triangles(i, 2);
    c = triangles(i, 3);
    
    fprintf (fid, '%d %d %d %d\n', i, a, b, c);
  end;
  
  % make the file accessible for other processes again
  fclose (fid);
