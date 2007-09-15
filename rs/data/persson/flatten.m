% [flat] = flatten (hierarchical, prefix)
%
% Reduce a structure with more than one level to a structure with only
% basic fields. This is used before writing the structure to a tabular
% file.
function [flat] = flatten (hierarchical, varargin)
  % start out with an empty structure, and then add all elements
  flat = struct;
  
  % get a list of all the prefices, using the empty list if none are
  % specified; this prefix will be added to all the names of the resulting
  % structure, allowing us to use this function recursively
  prefix = ['' varargin{:}];
  
  % get the names of the field on this level
  names = fieldnames (hierarchical);
  
  % retrieve a list of field for each of the entries
  for j = 1:length (names)
    name = names(j);
    val = getfield (hierarchical, name{:});
    if isstruct (val)
      % get a list of elements that hide underneath this field
      sub = flatten (val, [prefix name{:} '_']);
      
      % merge all those fields into the main structure
      flat = merge_structs (flat, sub);
    else
      % base case, just add this basic type to the final structure
      flat = setfield (flat, [prefix name{:}], val);
    end;
  end;