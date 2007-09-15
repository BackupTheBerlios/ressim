% [num_points, num_edges] = read_art_header (fid)
%
% Read the header of an ART input file, which declares the number of vertices
% and the number of edges that are present in that file.
function [num_points, num_edges] = read_art_header (fid)
   % bitstring of the 'variables' we have found so far and that we are expecting 
   % to find in the header, respectively.
   found    = 0;
	expected = bitor (1, 2);
   
   % when we have reached all the expected variables, we stop reading the header 
   % leaving the rest of it as regular comments.   
   while found ~= expected,
      % digest one more line from the input
      line = fgetl (fid);
      
      % if the header doesn't conform to the expected input, then fail fast.
      if ~ischar (line) || ~strncmp (line, '%%', length ('%%')),
         error ('File ended prematurely');
      end;

      % parse the header token into a keyword and a value part.
      pos = strfind (line, ':');
      key = strtrim (line (length ('%%')+1:pos-1));
      val = strtrim (line (pos+1:end));

      % set the properties depending on the keyword, marking which properties
      % that has been read along the way.
      if strcmpi (key, 'VertexNumber'),
         num_points = str2val (val);
         found = bitor (found, 1);
      elseif strcmpi (key, 'EdgeNumber'),         
         num_edges = str2val (val);
         found = bitor (found, 2);
      end;
   end;