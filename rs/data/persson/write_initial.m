% [] = write_initial (stem, vertices, edges, box, opts)
%
% write an input file for Shewchuk's Triangle program, containing the
% problem definition for the triangularization. filename is the name of the
% input file which should be sent to the executable.
function [] = write_initial (stem, vertices, edges, box, opts)
   % we always use the same file for input; if we are going to run more
   % than one test, then we should use a different directory
   fid = fopen (make_name (stem, 0, 'poly'), 'W');
   
   % all points must be specified in the input file, or the program won't
   % be able to figure out the extent of the outer triangles
   
   % exterior points come from the corners of the boundary; we need to
   % combine the coordinates from both points
   exterior_points = zeros (size (box, 1) .^ 2, 1);

   % bounding box; i and j is 1 for minimum, 2 for maximum. we setup the
   % cartesian set of combinations between these two to enumerate all
   % corners. note the calculation of the index, which is reused below to
   % locate the same points again
   for i = 1:size (box, 1)
      for j = 1:size (box, 1)
         % k is the one-based (which is why adding one is the last term) 
         % sequential number of the iteration
         k = (i-1) * size (box, 1) + (j-1) + 1;
         
         % add the corner point to the list of vertices and keep track of
         % the index that was assigned to it. if the point already exists,
         % then it will be reused.
         [vertices, exterior_points(k)] = ...
                        add_point (vertices, [box(i,1), box(j,2)], box, opts);
      end;
   end;   

   % header of the point section. the first parameter is the number of
   % points in the data set, the second is the number of dimensions (which
   % should always be 2 in this case). the third is the number of
   % attributes for each point (in case we want to assign some material
   % attributes to them) and the fourth is whether we elect to assign some
   % boundary marker or not (we don't -- the program is capable of finding
   % them itself)
   fprintf (fid, '%d 2 0 0\n', size (vertices, 1));
   
   % write the interior vertices (some of these may actually be on the
   % boundary -- we don't care about that)
   for i = 1:size (vertices, 1)
      fprintf (fid, '%d %d %d\n', i, vertices(i, 1), vertices(i, 2));
   end;
   
   % we are going to write all edges supplied to us, in addition to four
   % lines which describes the bounding box
   
   % bounding box. we need to write from both minimum and maximum (i) to
   % the corner that has the same x- and then the same y-coordinate
   % respectively (j) that the other one
   bounding_box = [];
   for i = [0 1]
      for j = [0 1]         
         % k is the one-based sequential number of the iteration
         % k = i * size (box, 1) + j + 1;
         
         % combinatorial expression to pick out each of the corners and
         % generate the two points on the path between them (along the
         % boundary; these intermediate points will be between the minimum
         % and the maximum in the array that is written)
         bounding_box = [bounding_box; ...
            exterior_points(i * size (box, 1) ^ (~j) + 0 * size (box, 1) ^ j + 1), ...
            exterior_points(i * size (box, 1) ^ (~j) + 1 * size (box, 1) ^ j + 1)];
      end;
   end;
   
   % type 1 edges is used to designate boundaries. append the bounding box to
   % the specified list of edges.
   t = 1;   
   edges = vertcat (edges, ...
           horzcat (bounding_box, t .* ones (size (bounding_box, 1), 1)));
            
   % header for the lines section. the first entry is the number of edges,
   % the second is a marker that indicates whether the line is a boundary
   % or not (we need the boundary marker to filter out the non-constraining
   % edges after we have triangulated)
   fprintf (fid, '\n%d 1\n', size (edges, 1));
   
   % write out the interior edges, marking them with the kind of fracture (the
   % first two are coordinates, the last parameter is an ordinal describing the
   % kind of fracture -- we need that when we are going to assign a
   % transmissibility multiplier during post-processing of the mesh).
   for i = 1:size (edges, 1)
      fprintf (fid, '%d %d %d %d\n', i, edges(i, 1), edges(i, 2), edges(i, 3));
   end;
   
   % holes in the figure. we simply assume that there aren't any holes
   fprintf (fid, '\n0\n');
   
   % make the file available to other processes (they cannot read it if we
   % have a write-lock on it).
   fclose (fid);
