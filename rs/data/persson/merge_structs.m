% [opts] = merge_structs (specified, defaults, include_empty)
%
% Fill the resulting set of options from the specified set, using the
% default values if a value for a tunable is not specified. include_empty
% is optional, and specifies whether to include options from the
% 'specified' set, even if they are empty.
function [opts] = merge_structs (specified, defaults, varargin)
  % usually we use this routine to merge two structures that may have
  % overlapping fields, and in those cases we want the default behaviour
  % which is not to include empty ones. however, when we want to replicate
  % input options to the output, then we simply want to put together two
  % disjunct sets. this parameter is specified as a vararg since it reduces 
  % the readability for the places where it is not needed.
  include_empty = (length (varargin) > 0) && (varargin{1} == true);

  % retrieve the list of options that have been specified explicitly 
  names = fieldnames (specified);

  % start out with the default; if nothing else is specified, use the
  % values from this collection.
  opts = defaults;

  % set all those options into the resulting set, overwriting any defaults
  for j = 1:length (names)
    name = names(j);
    value = specified.(name{:});
    if ~isempty (value) || include_empty
      opts.(name{:}) = value;
    end;
  end;
