% [] = write_csv (filename, values)
%
% Write an array of structures to a comma-separated list
function [] = write_csv (filename, values)
  % open the output file for over-writing, using buffering
  fout = fopen (filename, 'W');
  
  % write the header, a list of the fieldnames in the structure
  names = fieldnames (values);
  line  = '';
  delim = '';
  for i = 1:length (names)
    name = names(i);
    line = sprintf ('%s%s%s', line, delim, name{:});
    delim = ';';
  end;
  fprintf (fout, '%s\n', line);
  
  % write the rest of the array, one by one line
  for i = 1:length (values)
    
    % concatenate each of the fields in this row to a comma-separated
    % string list
    line = '';
    delim = '';
    for j = 1:length (names)
      name = names(j);
      val = getfield (values(i), name{:});
      str = val2str (val);
      line = sprintf ('%s%s%s', line, delim, str);
      delim = ';';
    end;
    
    % write this string to the file
    fprintf (fout, '%s\n', line);
  end;
  
  % return the handle so that others may start reading from it
  fclose (fout);