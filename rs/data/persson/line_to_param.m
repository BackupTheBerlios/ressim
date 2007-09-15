% [param] = line_to_param (line)
%
% Convert a line by coordinates to parameterized form.
%
% Input:
%  line     Matrix containing end-point coordinates; the first row is the
%           starting point, with columns being the x- and y-coordinate
%           respectively. The second row is the ending point.
%
% Output:
%  param    Matrix where the rows are the polynomials describing x- and y-
%           coordinates, respectively. The second column in each row is the
%           constant term (which is the first point), while the first column is 
%           the linear term (which when multiplied by one will bring us from the
%           first point to the second.
%
% Example:
%  p = line_to_param (vertices (edges(a, :), :));
function [param] = line_to_param (line)
   % psuedo-constants to address the "fields" in the matrix
   a = 1; b = 2; zth = 2; fst = 1;
   
   % we are going to have a two-by-two matrix in the end, since we have two
   % dimensional geometry. for 3D, this should be extended.
   param(:, zth) = line (a, :);
   param(:, fst) = line (b, :) - line (a, :);