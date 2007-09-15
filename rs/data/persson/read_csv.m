% [values] = read_csv (filename)
%
% Parse a comma-separated text-file into an array of structures containing
% the values found in the file. The first row in the file must be a header
% specifying the names of the individual elements.
function [values] = read_csv (filename)
  % regular expression that enable us to split a line up in its elements;
  % each match for this function will be an entry. note that it trims the
  % value by dropping whitespace in front and after
  delims = ',;';
  expression = ['\s*(?<val>[^' delims ']*)\s*[' delims ']?'];
  
  % open the file for reading, starting from the beginning
  fid = fopen (filename, 'r');
  
  % read the first line of the file, which is the header
  line = fgetl (fid);

  % names is a list, meaning that we must address it with curly braces (if 
  % we convert it to an array, then all names will be concatenated into one)
  matches = regexp (line, expression, 'names');
  names = {matches.val};
  
  % remove any leading and trailing spaces and convert all names to
  % lowercase so that they look like identifiers to us. slashes are
  % replaced with underscore to be legal Matlab identifiers
  names = strrep (strtrim (lower (names)), '/', '_');
  
  % we are going to dynamically create the structure based on the fields
  % that we find in the file. each of these structures are going to be put
  % in the resulting array
  values = [];
  
  % read the rest of the file, line by line
  while 1
    % read one more line of input if present; otherwise, stop iterating
    line = fgetl (fid);
    if ~ischar (line)
      break;
    end;
    
    % ignore empty lines
    if length (strtrim (line)) == 0
      continue;
    end;
    
    % structure into which we are going to read the current line;
    % afterwards it will be merged in together with the rest
    current = struct;    
    matches = regexp (line, expression, 'names');
    for i = 1:length (names)
      n = names(i);
      % if the last column is missing then fill it with blanks
      if i > length(matches)
        cell = '';
      else
        cell = matches(i).val;
      end;
      current = setfield (current, n{:}, str2val (cell));
    end;
    
    % merge the current line with all the other lines we have read
    values = [values; current];
  end;
  
  % return the file handle to the operating system, enabling other
  % processes to access it again
  fclose (fid);