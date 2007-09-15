% [vertices, edges, box] = read_hardcoded (filename)
%
% read geometry from a file. vertices is an n-by-2 matrix containing the
% x- and y-coordinates of each point. edges is an m-by-2 matrix containing
% the indices into the vertices array of the points. a point may be
% duplicated in the vertices matrix and each of the clones referred to. box
% is the bounding box of the problem, given as a 2-by-2 matrix of the
% minimum and maximum of x- and y-coordinates respectively.
function [vertices, edges, box] = read_hardcoded (filename)
    vertices = [ 0.2 0.2;
                 0.8 0.8;
                 0.5 0.2];
    edges = [ 1 2; 1 3 ];
    box = [ 0.0 0.0;
            1.0 1.0 ];
