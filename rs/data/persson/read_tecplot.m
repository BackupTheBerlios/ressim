% [vertices, edges, box] = read_tecplot (filename)
%
% retrieve lines generated from a drawing program. filename must be the
% full name of the file to read. vertices is an array of points (x,y),
% edges is a list of indices (i,j) into the vertices array and box is the
% minimum and maximum coordinates, respectively
function [vertices, edges, box] = read_tecplot (filename)
   % open the file and start reading from it
   fid = fopen (filename, 'r');
   
   % first line specifies the title of the plot
   regexp (fgets (fid), 'TITLE="(?<title>.*)\w*"', 'names');
   
   % second line is the names of the variables
   regexp (fgets (fid), 'VARIABLES="(?<var>.*)"\w*', 'names');
   
   % third line contains the information about the format of the file
   num_of = regexp (fgets (fid), ...
         'ZONE N=(?<nodes>\d*), E=(?<edges>\d*), F=FEPOINT, ET=TRIANGLE\w*', 'names');
      
   % read all the points first
   points = [];
   for i = 1:str2num(num_of.nodes)
      triple = fscanf (fid, '%f', 3)';
      points = [points; triple];
   end;

   % read all the lines afterwards
   lines = [];
   for i = 1:str2num(num_of.edges)
      triple = fscanf (fid, '%d', 3)';
      lines = [lines; triple];
   end;
   
   % find the index of the one dimension that does not vary
   [value, index] = min (std (points));
   
   % remove this index from both sets (leaving us with two dimensions)
   points = [points(:, 1:(index-1)) points(:, (index+1):size (points, 2))];
   
   % bounding box is written first explicitly as fragments; read the first
   % index and figure out when it occurs to us again, this time as the
   % target of the lines. the border is the lines from 1 to 'border'.
   origo = lines(1, 1);
   border = find (eq (lines(:, 2) - origo, zeros (size (lines, 1), 1)));
   
   % return all the points, even if there are some that are not referenced
   % it may be that the edges crosses onto the border and thus need the
   % points there
   vertices = points;
   
   % currently, all fractures are of type 0. we could map each color to its own
   % type and use that information to decide which multiplier it should be
   % assigned.
   t = 0;   
   
   % return the non-border lines as constraints. don't flatten the array
   % but return both dimensions. none of these lines are borders.
   xy_pairs = lines((border+1):size (lines, 1), 1:2);
   edges = horzcat (xy_pairs, t .* ones (size (xy_pairs, 1), 1));
   
   % expand all the points that are on the border
   border_points = [];
   for i = 1:border
      border_points = [border_points; ...
                       points(lines(i, 1), 1) points(lines(i, 2), 2);
                       points(lines(i, 2), 1) points(lines(i, 2), 2)];
   end;
   
   % find the ultimate coordinates in each dimension
   box = infer_box (border_points);
   
   % make the file available to others again
   fclose (fid);
