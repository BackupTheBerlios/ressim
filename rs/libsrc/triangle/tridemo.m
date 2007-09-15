%
% tridemo.m
%
% Demonstrates the mesh triangulation interface.
%

    % Construct the L-shaped domain.
    nodes = [ 1 0 0; 2 0 2; 3 1 2; 4 1 1; 5 2 1; 6 2 0 ];
    boundary = [ 1 1 2; 2 2 3; 3 3 4; 4 4 5; 5 5 6; 6 6 1 ];
    holes = [];

    % Add random points to the domain interior.
    n = size(nodes,1);
    for i=1:10
	point = 2 * rand(1,2);
	if point(1) <= 1.0 | point(2) <= 1.0
	    n = n + 1;
	    nodes = [ nodes; n point ];
	end
    end

    % Triangulate the domain and plot the result.
    [ nodes, triangles ] = triangulate(nodes, boundary, holes);
    trimesh(triangles(:,2:4),nodes(:,2),nodes(:,3),zeros(size(nodes,1),1))
    disp(sprintf('%d points added to domain.', size(nodes,1) - n));
