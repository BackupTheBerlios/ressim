% [t] = param_match (param, pt, dim)
%
% Find parameter where line crosses an axis.
%
% Input:
%  param       Polynomials that describe each dimension of the line.
%  pt          Point coordinates. First column is the x-coordinates, second is
%              the y-coordinate.
%  dim         Dimension which should be tested. 1 for the x-dimension, 2 for
%              the y-dimension.
%
% Output:
%  t           Array containing as many parameter values as there were points,
%              containing either the parameter value necessary to create the
%              point, or +Inf if the line is parallel to the axis specified in
%              dim.
%
% Example:
%  param_match (params, [pt; pt], [1, 2])
function [t] = param_match (param, pt, dim)
   % preallocate result
   t = zeros (size (pt, 1), 1);
   
   % process each point by itself, since the latter expression is hard to
   % parallize due to indirect referencing.
   for i = 1:size (pt, 1),
      % only do if not parallel line; otherwise leave with default
      if abs (param(dim(i), 1)) > eps,
         % solve the equation to find the parameter value which gives this value
         % for the specified dimension
         t(i) = ( pt(i, dim(i)) - param(dim(i), 2) ) ./ param(dim(i), 1);
      else
         % if we're unable to find any value, then use inifinity as a default
         % value, with the sign saying whether the point is to the right of to
         % the left of the line.
         if pt(i, dim(i)) >= param(dim(i), 2),
            t(i) = +Inf;
         else
            t(i) = -Inf;
         end;
      end;
   end;