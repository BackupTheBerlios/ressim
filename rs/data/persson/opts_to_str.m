% [str] = opts_to_str (opts)
%
% Convert an option structure to a string that can be displayed.
function [str] = opts_to_str (opts)
  % base case; there are no options
  str = '';

  % first iteration is not delimited from anything, so we don't need a
  % prefix; only the second iterations have something added in front (that
  % way we get the most compact string -- no unnecessary delimiters).
  delimiter = '';
  
  % loop through each option, adding it to the end of the string
  names = fieldnames (opts);
  for i = 1:length (names)
    name = names(i);
    field = getfield (opts, name{:});
    value = val2str (field);
    str = sprintf ('%s%s%s=%s', str, delimiter, name{:}, value);
    
    delimiter = ', ';
  end;