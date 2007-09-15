% [vertices, edges] = clip_lines (vertices, edges, box, opts)
%
% Clip all lines so that they are within the specified box. The set of vertices
% may be changed as a consequence.
%
% Example:
%  vertices = [0, 4; 0, 2; 0, 0; 2, 2; 2, 0; 4, 0];
%  edges = [1, 4, 1; 2, 4, 2; 3, 4, 3; 5, 4, 4; 6, 4, 5];
%  box = [1, 1; 3, 3];
%  [v, e] = clip_lines (vertices, edges, box, default_options ());
function [new_vertices, new_edges] = clip_lines (vertices, edges, box, opts)
   % psuedo-constants to express the enumeration for each dimension
   x = 1; y = 2; c = 3;
   
   % only keep points that actually are used in the new, clipped line set
   new_vertices = [];
   new_edges = [];

   % process each edge separately
   for i = 1:size (edges, 1),
      % we will keep the color of the line, even if we clip it
      color = edges (i, c);
      
      % convert line coordinates to parameterized polynomials
      param = line_to_param (vertices(edges(i, x:y), :));
      
      % find the intersection with each of the edges of the box; get the x and y
      % dimension of the min and max coordinates
      t = param_match (param, [box; box], [x, x, y, y]);
      
      % add the original constraints of the line to the list of candidate
      % parameters. this allows us to restrict all parameters to be within the
      % original line and not automatically expand to the box
      t = [t; 0.; 1.];
           
      % line will cross two of the edges on entry to the box (one for each 
      % dimension), and two edges when it leaves. get us the last crossing in 
      % (largest of the three smallest) and the first crossing out (smallest of 
      % the three largest).      
      t = sortrows (t, 1);      
      source = t(3);
      target = t(4);
      
      % normalize such that only parts of the original line is included
      source = min (1., max (0., source));
      target = min (1., max (0., target));
      
      % only let the line remain if it has not 
      if abs (source - target) > eps,
      
         % convert the new endpoints to coordinates
         a_coords = point_at (param, source);
         b_coords = point_at (param, target);
      
         % construct a point and a line 
         [new_vertices, a_ndx] = add_point (new_vertices, a_coords, box, opts);
         [new_vertices, b_ndx] = add_point (new_vertices, b_coords, box, opts);
         new_edges = [new_edges; a_ndx, b_ndx, color];
      end;
   end;