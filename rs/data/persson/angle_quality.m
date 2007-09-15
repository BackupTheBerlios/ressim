% [qualities] = angle_quality (sides)
%
% Calculate the anisotrophy quality of a set of triangles.
% The quality measure is defined as the ratio between the 
% smallest circle that can circumscribe the triangle and
% the largest circle that can be inscribed in it, adjusted
% so that an equilateral triangle has a quality of 1.0.
% 
% Sides is an m-by-3 vector containing the length of the sides for each
% triangle. The returning vector will by n-by-1 containing the quality 
% measure for each triangle.
function [qualities] = angle_quality (sides)
  % preallocate output buffer
  qualities = zeros (size (sides, 1), 1);

  % quality measure can be calculated locally for each triangle
  for i = 1:size (sides, 1)
    % decompose the array record
    a = sides(i, 1);
    b = sides(i, 2);
    c = sides(i, 3);
    
		% calculate the ratio between the two areas
		qualities(i) = (b+c-a) * (c+a-b) * (a+b-c) ./ (a*b*c);
	end;
