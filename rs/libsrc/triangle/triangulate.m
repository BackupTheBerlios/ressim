%
% Function: triangulate
% 
%   Triangulates a set of points with boundary.
%
% Pre:
%   nodes contains the node coordinates, in the format [# x y; ...].
%   boundary contains the boundary edges, in the format [# i j; ...].
%   holes contains points inside holes, in the format [# x y; ...].
%
% Post:
%   nodes contains the new node coordinates, in the format [# x y; ...].
%   triangles contains the triangulation, in the format [# i j k; ...].
%

function [nodes, triangles] = triangulate(nodes, boundary, holes)

    n = size(nodes, 1);
    m = size(boundary, 1);
    h = size(holes, 1);

    % Write out domain in PSLG format.
    fid = fopen('mesh.poly', 'w');
    fprintf(fid, '%d 2 0 0\n', n);
    
    % Points
    for i = 1:n
	fprintf(fid, '%d %f %f\n', nodes(i,1), nodes(i,2), nodes(i,3));
    end
    
    % Segments
    fprintf(fid, '%d 0\n', m);
    for j = 1:m
	fprintf(fid, '%d %d %d\n', boundary(j,1), ...
				   boundary(j,2), ...
				   boundary(j,3));
    end
    
    % Holes
    fprintf(fid, '%d\n', h);
    for k = 1:h
	fprintf(fid, '%f %f %f\n', holes(k,1), holes(k,2), holes(k,3));
    end
    
    % Regional attributes
    
    fclose(fid);

    % Triangulate domain. (add -s, -q or -a to avoid constrained Delaunay)
    !./triangle -pq mesh.poly > /dev/null

    % Read in new nodes.
    fid = fopen('mesh.1.node', 'r');
    n = fscanf(fid, '%d', 1);
    dummy = fscanf(fid, '%d', 3);
    nodes = [];
    for i = 1:n
	point = [ fscanf(fid, '%d', 1)' fscanf(fid, '%f', 2)' ];
	nodes = [ nodes; point ];
	fscanf(fid, '%d', 1);
    end
    fclose(fid);

    % Read in triangle mesh.
    fid = fopen('mesh.1.ele', 'r');
    m = fscanf(fid, '%d', 1);
    dummy = fscanf(fid, '%d', 2);
    triangles = [];
    for j = 1:m
	triangles = [ triangles; fscanf(fid, '%d', 4)' ];
    end
    fclose(fid);
    
    % Delete temporary files.
    !rm -f mesh.poly mesh.1.poly mesh.1.node mesh.1.ele
