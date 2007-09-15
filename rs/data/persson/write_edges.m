% [] = write_edges (stem, iteration, vertices, edges, box)
%
% Write a set of edges that participates as constraints in a
% triangulation to the file format used by Jonathan Shewchuk's
% Triangle program.
function [] = write_edges (stem, iteration, vertices, edges, box)
  % request a handle to the file from the operating system
  fid = fopen (make_name (stem, iteration, 'poly'), 'W');
  
  % no points are stored in this file; as long as there is a .node file
  % present for the same iteration with the same stem, then it will be
  % used instead. it only has be on the valid format
  fprintf (fid, '0 2 0 1\n');
  
  % header reveals key information about the file. we are going to write
  % boundary information to the file since that is used by the simulator
  % import filter to determine extra information
  num_of_edges = size (edges, 1);
  boundary_flag = 1;  % include boundary information
  fprintf (fid, '%d %d\n', ...
           num_of_edges, boundary_flag);
  
  % coordinates from the bounding box
  x_min = box(1, 1);
  y_min = box(1, 2);
  x_max = box(2, 1);
  y_max = box(2, 2);
  
  % each line that follows the header is specification of a node
  for i = 1:num_of_edges
    % indices of the points that are references (one-based)
    a = edges(i, 1);
    b = edges(i, 2);
    
    % local variables for the coordinates for each point
    x1 = vertices(a, 1);
    y1 = vertices(a, 2);
    x2 = vertices(b, 1);
    y2 = vertices(b, 2);
    
    % an edge is a boundary if both points are on the boundary, otherwise
    % it is an interior constraint. explicitly marked boundaries are always
    % included as such.
    is_one_boundary = x1 == x_min || x1 == x_max || ...
        y1 == y_min || y1 == y_max;
    is_other_boundary = x2 == x_min || x2 == x_max || ...
        y2 == y_min || y2 == y_max;
    is_boundary = (is_one_boundary && is_other_boundary) || edges(i, 3);
    
    % write this point to the file
    fprintf (fid, '%d %d %d %d\n', i, a, b, is_boundary);
  end;
  
  % we don't make use of holes
  fprintf (fid, '0');
  
  % make the file available again to other processes
  fclose (fid);
  
  
