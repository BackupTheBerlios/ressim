% [pt] = point_at (param, t)
%
% Input:
%  param       Polynomial coefficients for the line, created by line_to_param.
%  t           Parameter values for which the line should be evaluated.
%
% Output:
%  pt          Point array with two coordinates for each point. There will be as
%              many points as there were input values (in t).
function [pt] = point_at (param, t)
   % pre-allocate
   pt = zeros (size (t, 1), 2);
   
   % process each point individually; the polyval function does not handle a
   % matrix.
   for i = 1:size (t, 1),
      
      % evaluate the polynomial for each dimension, putting together each
      % coordinate for the point
      for j = 1:2,
         pt(i,j) = polyval (param(j, :), t(i));
      end;
   end;