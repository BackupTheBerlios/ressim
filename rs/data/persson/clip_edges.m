% [vertices, edges] = clip_edges (vertices, edges, box, opts)
%
% Clip all edges so that they are within the specified box. The set of vertices
% may be changed as a consequence.
%  
% Example:
%  vertices = [0, 4; 0, 2; 0, 0; 2, 2; 2, 0; 4, 0];
%  edges = [1, 4, 1; 2, 4, 2; 3, 4, 3; 5, 4, 4; 6, 4, 5];
%  box = [1, 1; 3, 3];
%  [v, e] = clip_edges (vertices, edges, box, default_options ());
function [new_vertices, new_edges] = clip_edges (vertices, edges, box, opts)
   % psuedo-constants (to address structural fields in the matrices)
   x = 1; y = 2; a = 1; b = 2; c = 3; lower = 1; upper = 2;

   new_vertices = [];
   new_edges    = [];

   for i = 1:size (edges, 1),
      % store away the annotation of the line; if we recreate another line in
      % place of this one, we want it to have the same color/type
      color = edges(i, c);
      
      % create a parametric form for the line; 
      % we use the source end-point as the starting value (i.e. when t == 0)
      x_0 = vertices(edges(i, a), x);
      y_0 = vertices(edges(i, a), y);
      
      % calculate the gradient from the source end-point to the target end-point
      % we should be at the end of the segment when t == 1
      x_1 = vertices(edges(i, b), x) - x_0;
      y_1 = vertices(edges(i, b), y) - y_0;
      
      % if the segment is a straight horizontal or vertical line, then only keep
      % the line if it is inside two boundaries in the other dimension
      if x_1 == 0,
         if x_0 < box(lower, x) || x_0 > box(upper, x),
            continue;
         else
            t_min_x = -Inf;
            t_max_x = +Inf;
         end;
      else
         % figure out at which parameters it crosses each of the boundaries in
         % that dimension. there are two horizontal and two vertical clip lines
         t_lower_x = ( box(lower, x) - x_0 ) ./ x_1;
         t_upper_x = ( box(upper, x) - x_0 ) ./ x_1;
         
         % start and end, respectively
         t_min_x = min (t_lower_x, t_upper_x);
         t_max_x = max (t_lower_x, t_upper_x);
      end;
      if y_1 == 0,
         if y_0 < box(lower, y) || y_0 > box(upper, y),
            continue;
         else
            t_min_y = -Inf;
            t_max_y = +Inf;
         end;
      else
         t_lower_y = ( box(lower, y) - y_0 ) ./ y_1;
         t_upper_y = ( box(upper, y) - y_0 ) ./ y_1;
         
         t_min_y = min (t_lower_y, t_upper_y);
         t_max_y = max (t_lower_y, t_upper_y);
      end;
      
      % choose the parameter for the dimension in which the line intersects with
      % the box first (relative to the original line). for lines that are inside
      % the box, this gives us the smallest line
      t_min = max ([t_min_x, t_min_y, 0]);
      t_max = min ([t_max_x, t_max_y, 1]);
      
      % discard lines that are outside of the box
      if t_min > 1 || t_max < 0,
         continue;
      end;

      % convert the parameter values into coordinates and construct the points
      % (the set of vertices may change as the lines are clipped) and then 
      % finally a line for those coordinates in the returned set
      x_a = x_0 + t_min * x_1;
      y_a = y_0 + t_min * y_1;
      x_b = x_0 + t_max * x_1;
      y_b = y_0 + t_max * y_1;
      
      [new_vertices, source] = add_point (new_vertices, [x_a, y_a], box, opts);
      [new_vertices, target] = add_point (new_vertices, [x_b, y_b], box, opts);
      
      new_edges(end+1, :) = [source, target, color];
   end;