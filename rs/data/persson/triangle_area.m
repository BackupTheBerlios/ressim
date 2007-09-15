% [area] = triangle_area (sides)
% 
% Calculate the area of triangles using the lengths of the individual
% sides in the triangle.
function [area] = triangle_area (sides)
  % preallocate output buffer
  area = zeros (size (sides, 1), 1);

  for i = 1:size (sides, 1)
    % decompose the array record containing the lengths
    a = sides(i, 1);
    b = sides(i, 2);
    c = sides(i, 3);
    
    % using Heron's rule we find the area of the triangle; s is the semi-
    % perimeter of the triangle, i.e. half the length of all the sides
    % combined.
    s = (a+b+c) ./ 2;
    area(i) = sqrt (s*(s-a)*(s-b)*(s-c));
  end;
