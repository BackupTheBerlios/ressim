% [vertices, max_move] = apply_forces (vertices, force, constraints, box)
%
% Move vertices around according to forces, but constrained by the bounding
% box of the problem. Also returns the maximum movement for interior points
% (since the border points may be snapped back to border). constraints tell
% us whether the points are fixed or not. it is a n-by-2 array with the
% first column being an enumerated value stating if the point is fixed,
% flexible or free. edges are the lines that makes out the constraints.
function [new_vertices, max_move] = ...
                apply_forces (old_vertices, forces, constraints, box, opts)
   % if we got more points than constraints, it's because Triangle has
   % split a line. since we already attempt expand all intersections, these
   % are points that we have missed. most of these errors can be found by
   % visual inspection if we know where to look
   if length (old_vertices) > length (constraints)
     for i = length (constraints)+1:length (old_vertices)
       disp (sprintf ('x=%f, y=%f', old_vertices(i, 1), old_vertices(i, 2)));
     end;
     disp (opts_to_str (opts));
     error (sprintf (['Points above indicate regions where lines are ' ...
              'too close\nTry to rerun with option border=1 set.']));
   end;
                      
   % initial values; more extreme than anything we'll see in practice so
   % that the first data point will replace it
   max_move = 0;
                                 
   % coordinates of the bounding box
   min_x = box(1, 1);
   min_y = box(1, 2);
   max_x = box(2, 1);
   max_y = box(2, 2);
   
   % pre-allocate memory; we won't loose any points
   new_vertices = zeros (size (old_vertices));   

   % process each point independently
   for i = 1:size (old_vertices, 1)
      old_x = old_vertices(i, 1);
      old_y = old_vertices(i, 2);
      
      % only move the point if it is not marked as "2;fixed". if it is
      % fixed then it keeps the same position
      if constraints(i, 1) > 0 % == 2
        new_x = old_x;
        new_y = old_y;
      else      
        % make sure it doesn't go outside of the bounding box; there will
        % be a shorter distance than originally thought, but that will 
        % generate a correspondingly larger force in the next iteration
        new_x = min( max_x, max (min_x, old_x + forces(i, 1)));
        new_y = min( max_y, max (min_y, old_y + forces(i, 2)));
        
        % if we have marked the point as merely "1;flexible" instead of
        % "0;free", then we'll have to project it
        if constraints(i, 1) == 1
          % the two extra auxiliary columns in the constraints table are
          % the indices of the two points which this one can glide between
          % we make up a line from these two points and project the point
          % back to that line
          line = old_vertices(constraints(i, 2:size (constraints, 2)), :);
          [new_x, new_y] = project_onto_line ([new_x, new_y], line);
        end;
      end;
      
      new_vertices(i, :) = [new_x new_y];
      
      % movement is only interesting of the point is not on the border, to
      % avoid oscillations of the solution where snapping to border is the
      % only thing that happens. however, this test is not necessary
      % because we don't check the force but the actual movement
      %is_border = (new_x == min_x) || (new_x == max_x) || ...
      %            (new_y == min_y) || (new_y == max_y);
      %if ~is_border
         % report the movement for this point if we have set a new record
         movement = sqrt ((new_x - old_x) ^ 2 + (new_y - old_y) ^ 2);
         max_move = max (max_move, movement);
      %end;
   end;
