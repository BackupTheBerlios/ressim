% [x, y, r] = circumcenter (corners)
%
% Finds the circumcenter of a triangle. corners is a matrix with 2 columns
% (one for each dimension) and 3 rows (one for each point). If you have a
% coordinate array vertices and an index array triangles then you'd
% typically specify vertices(triangles(i, :), :) for triangle i as
% parameter to this function.
function [x, y, r] = circumcenter (p)
  % constants to make the code below a little more readable
  a = 1; b = 2; c = 3;  % rows
  x = 1; y = 2;         % columns

  % setup the linear equation system
  % TODO: add explanation of how these equations were deduced
  coeffs = [ 2*(p(b,x) - p(a,x)),   2*(p(b,y) - p(a,y));
             2*(p(c,x) - p(b,x)),   2*(p(c,y) - p(b,y)) ];
            
  target = [ p(b,x)^2 - p(a,x)^2 + p(b,y)^2 - p(a,y)^2;
             p(c,x)^2 - p(b,x)^2 + p(c,y)^2 - p(b,y)^2 ];
            
  % pass it on to our specialized solver
  center = solve_2d (coeffs, target);

  % if the points are colinear, return a point infinitely far out, which
  % is the only (theoretical) circle that could include all of them. this
  % will cause the sweep algorithm to disregard this point (since it will
  % be further out than any candidate).
  if isempty (center)
    x = inf;
    y = inf;
    r = inf;
  else

    % calculate the radius from the distance between one of the points 
    % and the circumcenter (all the distances should be the same)
    r = sqrt ((p(a,x)-center(x))^2 + (p(a,y)-center(y))^2);

    % return the individual components of the point (note that at this
    % point x and y seizes to be constants and takes the meaning of the
    % return variables.
    x = center(x);
    y = center(y);
  end;
