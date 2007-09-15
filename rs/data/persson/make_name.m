% [name] = make_name (stem, iteration, extension)
%
% Setup the filename that adheres to the convention used by the Triangle
% and ShowMe programs.
function [name] = make_name (stem, iteration, extension)
  if iteration == 0
    name = [stem '.' extension];
  else
    name = [stem '.' int2str(iteration) '.' extension];
  end;
  
