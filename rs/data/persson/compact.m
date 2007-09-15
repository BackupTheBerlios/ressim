% [new_vertices, new_edges, new_triangles] = ...
%                    compact (old_vertices, old_edges, old_triangles, box, opts)
%
% Make sure that no points are mentioned more than once in the vertex
% set, and that all points are referred to by at least one edge.
function [new_vertices, new_edges, new_triangles] = ...
                     compact (old_vertices, old_edges, old_triangles, box, opts)
  % named constants used to refer to each coordinate for each point
  x = 1; y = 2; t = 3; a = 1; b = 2; c = 3;
  
  % don't keep any points unless they're referred to by someone
  new_vertices = [];
  new_edges = [];
  new_types = [];
  new_triangles = [];
  
  % more through all the old edges, adding them one by one to the new
  % edge set, creating vertices for them as well
  for i = 1:size (old_edges, 1)
    pt = [old_vertices(old_edges(i, a), x) old_vertices(old_edges(i, a), y); ...
          old_vertices(old_edges(i, b), x) old_vertices(old_edges(i, b), y)];
    
    % get the index for each of the two points; this will give us a row
    % of two indices that represents a new line
    this_edge = [];
    for j = 1:size (pt, 1)
      [new_vertices, this_edge(j)] = ...
                                 add_point (new_vertices, pt(j, :), box, opts);
    end;
        
    % check that the edge actually is something; that we don't have edges
    % with zero length (if the original vertex set contained two points
    % with the same values, then they should be identified as the same
    % point here).
    if std(this_edge) ~= 0
      new_edges = [new_edges; this_edge];
      
      % assign the new edge the same type as the old edge. this is assigned to a
      % separate array so that the type is considered when determining if the
      % row is unique or not.
      new_types = [new_types; old_edges(i, t)];
    end;
  end;

  % make sure that we don't have any duplicate rows either
  [new_edges, unique_indices] = unique (new_edges, 'rows');
  
  % merge the types back into the edges; note that the type of the edge will be
  % selected more or less random if there are two identical edges of different
  % types. this means that we may loose fractures that are on the boundary.
  new_edges = horzcat (new_edges, new_types (unique_indices));
  
  % do the same thing for triangles
  for i = 1:size (old_triangles, 1)
     % triangles are simplices in one dimension higher than lines; they
     % have three points instead of two
     pt = [old_vertices(old_triangles(i, a), x) old_vertices(old_triangles(i, a), y); ...
           old_vertices(old_triangles(i, b), x) old_vertices(old_triangles(i, b), y); ...
           old_vertices(old_triangles(i, c), x) old_vertices(old_triangles(i, c), y)];
       
     % get the index for each of the three points; this will give us a row
     % of three indices that represents a new triangle
     this_triangle = [];
     for j = 1:size (pt, 1)
        [new_vertices, this_triangle(j)] = ...
                                 add_point (new_vertices, pt(j, :), box, opts);
     end;
     
     % add the triangle to the new set
     new_triangles = [new_triangles; this_triangle];
  end;
