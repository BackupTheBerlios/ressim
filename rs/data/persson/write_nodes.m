% [] = write_nodes (stem, iteration, vertices, box)
%
% Write a set of nodes that participates in a triangulation to the file
% format used by Jonathan Shewchuk's Triangle program.
function [] = write_nodes (stem, iteration, vertices, box)
  % request a handle to the file from the operating system
  fid = fopen (make_name (stem, iteration, 'node'), 'W');
  
  % header reveals key information about the file. assume that all nodes
  % that are on the boundary (including the corners) already is a part of
  % the set of vertices.
  num_of_nodes = size (vertices, 1);
  num_of_dims = 2;    % every node is specified in 2D
  num_of_attr = 0;    % no extra data attached to nodes
  boundary_flag = 1;  % include boundary information
  fprintf (fid, '%d %d %d %d\n', ...
           num_of_nodes, num_of_dims, num_of_attr, boundary_flag);
  
  % coordinates from the bounding box
  x_min = box(1, 1);
  y_min = box(1, 2);
  x_max = box(2, 1);
  y_max = box(2, 2);
  
  % each line that follows the header is specification of a node
  for i = 1:num_of_nodes
    % local variables for the coordinates for each point
    x = vertices(i, 1);
    y = vertices(i, 2);
    
    % a node is on the boundary if one of the coordinates is either the
    % minimum or the maximum
    is_boundary = x == x_min || x == x_max || y == y_min || y == y_max;
    
    % write this point to the file
    fprintf (fid, '%d %f %f %d\n', i, x, y, is_boundary);
  end;
  
  % make the file available again to other processes
  fclose (fid);
  
  
