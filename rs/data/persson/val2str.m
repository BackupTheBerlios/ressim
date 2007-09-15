% [string] = val2str (value)
% 
% Return a string representation of a value regardless of its type. This
% function is the dual of str2val().
function [string] = val2str (value)
  % we read empty strings as empty values, so we write them back the same
  if isempty (value)
    string = '';
  else
    if isnumeric (value)
      if length (value) > 1,
         string = '[';
         for i = 1:length (value),
            string = sprintf ('%s%s,', string, val2str (value(i)));
         end;
         string(length (string)) = ']'; % overwrite last comma
      else            
         if abs (fix (value) - value) < eps  || isinf (value), % isinteger
           string = sprintf ('%d', value);
         else      
           string = sprintf ('%g', value);

           % remove superfluous zeros. divide the floating point into various 
           % parts to process them separately
           parts = regexp (string, [ ...
             '(?<sign>-?)' ...
             '(?<mantissa>\d+(\.\d+)?)' ...
             '(?<scientific>[eE][\+-]?)?' ...
             '(?<exponent>\d+)?'], 'names');
           p = parts.('sign');
           m = parts.('mantissa');
           s = parts.('scientific');
           e = parts.('exponent');

           % remove any leading and trailing zeros
           m = strrep (strtrim (strrep (m, '0', ' ')), ' ', '0');
           e = strrep (strtrim (strrep (e, '0', ' ')), ' ', '0');

           if m(1) == '.', m = ['0' m]; end;
           if m(length (m)) == '.', m = [m '0']; end;
           if length (e) == 0, s = ''; e = ''; end;

           % put the number back together again
           string = sprintf ('%s%s%s%s', p, m, s, e);
         end;
      end;
    else
      string = sprintf( '%s', value);
    end;
  end;