% [value] = str2val (string)
%
% Convert a string to a semi-typed value based on its content. Supported
% values are integers, doubles and strings.
function [value] = str2val (string)
  % regular expression to recognize decimal numbers; in Matlab we can use
  % decimal numbers for integers; they will simply be truncated. note that
  % the entire string must be matched
  r = '^(-|\+)?(([0-9]*(\.[0-9]+)?)|([0-9]+(\.[0-9]*)?))([eE](-|\+)?[0-9]+)?$';
  
  % try to get a match; if we get one, then m will be a non-empty string
  string = strtrim (string);
  m = regexp (string, r, 'match');
  
  % return the numerical value of the text, if we found one, otherwise the
  % raw text that we were sent as parameter
  if length (m)
    value = str2num (m{:});
  else
    if length (string) == 0
      value = [];
    else   
      value = string;
    end;
  end;