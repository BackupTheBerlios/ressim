% [vertices, constraints] = grade_edges (fixed, edges, map, moveable)
%
% Determine which vertices that are constrained and which that are free to
% move. vertices is the resulting m-by-2 array containing the coordinates
% of the points, and constraints is a m-by-2 array containing information
% about the mobility of the points. If any of the fixed (or rather, "on a
% line") points are interior to an edge, they will be relaxed to "flexible"
% which means that they can move along that particular line. Note: It is
% important that the set of constraining edges does not change; otherwise
% the constraints array will be rendered useless. Map is a n-by-2 array
% that describes which fixed points that are constrained to which lines.
function [vertices, constraints] = grade_edges (fixed, edges, map, moveable)
  % preallocate output arrays
  num_of_points = size (fixed, 1) + size (moveable, 1);
  vertices      = zeros (num_of_points, size (fixed, 2));
  constraints   = zeros (num_of_points, 3);
  
  % we indicate fixed points by the enumeration value "2;fixed" and free
  % points with "0;free". the rationale behind these numbers are that they
  % are the number of dimensions in which movement is locked. none of these
  % kinds have any auxiliary information (stored in the second and third
  % column) attached, so we initialize those to zero for good measure.
  for i = 1:size (fixed, 1)
    vertices(i, :) = fixed(i, :);
    constraints(i, :) = [2, 0, 0];
  end;
  
  % concatenate the set of points into one set, each point has a
  % corresponding constraint (together with an information column that
  % tells us to which line we are constrained.
  for i = 1:size (moveable, 1)
    vertices(i + size (fixed, 1), :) = moveable(i, :);
    constraints(i + size (fixed, 1), :) = [0, 0, 0];
  end;

  % loosen up on the points that were added to split up lines (these points
  % will be ends of two lines that are adjacent).
  for i = 1:size (map, 1)
      % index into the fixed can be used as an index into vertices since
      % the latter is a superset
      point = map(i, 1);
      line = map(i, 2);
      
      % we have problems with Triangle altering the sequence in which the
      % lines occur, so their indices are not stable during several runs;
      % the points seems to behave better. thus, the constraint information
      % is setup to be the two points whose line in between is the allowed
      % constraint. partition_edges() always sets up the lines in this
      % manner [a b; b c], and we're interested in getting a and c to
      % determine where b can move.
      a = edges(line+0, 2);
      c = edges(line+1, 1);

      % change the point from "2;fixed" to "1;flexible". reference to the 
      % line so that apply_forces can move the point according to it
      constraints(point, :) = [1, a, c];
  end;
