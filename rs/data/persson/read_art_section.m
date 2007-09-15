% [data] = read_art_section (fid, pattern, cols, rows)
%
% Read a section of line data. Each line should adhere to the pattern specified,
% containing exactly cols number of fields. It is expected to read rows number
% of entries in the section -- an error will be thrown if the count varies.
function [data] = read_art_section (fid, pattern, cols, rows)
   % pre-allocate data array
   data = zeros (rows, cols);

   i = 0;   % start reading at the first entry; none have been read so far
   while true
      % read one more line from the file
      line = fgetl (fid);
      
      % stop reading when the section ends
      if strncmp (line, '$', length ('$')), break; end;
      
      % skip comment lines
      if strncmp (line, '%', length ('%')), continue; end;
      
      % parse the line into fields
      fields = sscanf (line, pattern);
      
      % assign to the appropriate slot in the array
      i = i + 1;
      data(i, :) = fields';
   end;
   % sanity check
   %if i ~= rows,
   %   error (sprintf ('Expected %d entries, but got %d', rows, i));
   %end;
   
   % truncate the dataset to fit the actual number found in the file; the number
   % specified is only a guide (this is especially a problem with .net files,
   % not as much with .art files).
   if i < rows,
      data = data(1:i, :);
   end;
