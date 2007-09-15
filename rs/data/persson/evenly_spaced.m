% [vertices, space] = evenly_spaced (num_of_elements, box)
%
% Create a matrix of evenly spaced points within a bounding box. The
% function's target is to create the number of points necessary to get a
% triangulation consisting of the desired number of elements. The
% algorithm works best if you want a square number of elements. The
% routine also returns the desired spacing between elements in each
% direction (which can be used to decide how to chop up a vector)
function [vertices, space] = evenly_spaced (num_of_elements, box)
   % dissect the bounding box structure
   x_min  = box(1, 1);
   x_size = box(2, 1) - box(1, 1);
   y_min  = box(1, 2);
   y_size = box(2, 2) - box(1, 2);
   
   % partition the area into the given number of elements
   area_per_elem = ideal (num_of_elements, box);
   
   % initially we setup the grid so that every two rows are moved to the
   % middle between the points on the row above. this makes the distance
   % between the projection of the point above to this row and the point
   % itself 1/2*x. the hypotenuse is x, since we want equilateral triangles
   % which makes the last side in the right-angle triangle (which fills
   % half the element) sqrt(3)/2*x. the area of an element thus relates to
   % x such that it is 1/2 * x * sqrt(3)/2*x
   dx = sqrt (4 * area_per_elem / sqrt (3));
   dy = sqrt (3) / 2 * dx;
   
   % adjust so that we get a round nice number of points in each direction
   nx = max (1, round (x_size / dx));
   dx = x_size / nx;
   ny = max (1, round (y_size / dy));
   dy = y_size / ny;
   
   % write vertices at these points; we have one more point than there are
   % sides, which is we we start at zero instead of one
   vertices = [];
   for i = 0:ny
      % every other row is adjusted a half column to the right and the last
      % point disappears.
      if ~mod(i, 2)
         ofs    = 0;
         count  = nx;
      else
         ofs  = dx / 2;
         
         % if we only adjust the row, we'll loose two point along each edge
         % and this will cause a long edge at the boundary. as we prefer
         % shorter ones, we'll setup two points at the edges as well
         vertices = [vertices; x_min         y_min+i*dy];
         vertices = [vertices; x_min+nx*dx   y_min+i*dy];
         
         % since we are adjusting with the offset; don't write the last end 
         % point (thus minus one on the upper bound). we handle the end 
         % point of the line above.         
         count = nx - 1;
      end;
      
      % disperse the points along the line. 
      for j = 0:count
         vertices = [vertices; x_min+j*dx+ofs   y_min+i*dy];
      end;
   end;
   
   % if we are putting constraints in the triangulation, then let them be
   % spaced about the same as the points
   space = dx;
   