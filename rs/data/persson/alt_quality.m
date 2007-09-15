% [qualities] = angle_quality (sides)
%
% Calculate the anisotrophy quality of a set of triangles using the measure
% set forward by R.E. Bank and J. Xu in "An algorithm for coarsening 
% unstructured meshes" (which IMHO lacks a intuitive explanation of it).
% 
% Sides is an m-by-3 vector containing the length of the sides for each
% triangle and areas is an m-by-1 vector containing the element sizes. The 
% returning vector will by n-by-1 containing the quality measure for each 
% triangle.
function [qualities] = alt_quality (sides, areas)
  % preallocate output buffer
  qualities = zeros (size (sides, 1), 1);

  % quality measure can be calculated locally for each triangle
  for i = 1:size (sides, 1)
    % decompose the array record
    l1 = sides(i, 1);
    l2 = sides(i, 2);
    l3 = sides(i, 3);
    a  = areas(i);

    qualities(i) = (4 * sqrt (3) * a) / (l1^2 + l2^2 + l3^2);
	end;
