%
% tridemoconstrained.m
%
% Demonstrates the mesh triangulation interface using constrained Delaunay.
%

    % Construct a square domain
    nodes = [ 1 0 0; 2 0 2; 3 2 2; 4 2 0 ];
    boundary = [ 1 1 2; 2 2 3; 3 3 4; 4 4 1 ];
    holes = [];
    
    % fracture 1 and 2
    f11 = [0.5 0.7];
    f12 = [1.5 1.3];
    f21 = [0.5 1.2];
    f22 = [1.5 0.8];
    
    nodes=[nodes; 5 f11; 6 f12; 7 f21; 8 f22];
    boundary=[boundary; 5 5 6; 6 7 8];

    % Uniformly distributed points.
    n = size(nodes,1);
    
    [x y]=meshgrid(0:.2:2,0:.2:2);
    
    numI=size(x,2);
    numJ=size(x,1);
    
    for i=1:numI
      for j=1:numJ
	pert = 1+0.1*rand(1);
	point = [x(i,j)*pert y(i,j)*pert];
	n = n + 1;
	nodes = [ nodes; n point ];
      end
    end

    % Triangulate the domain and plot the result.
    [ nodes, triangles ] = triangulate(nodes, boundary, holes);
    
    clf
    hold on
    h=trimesh(triangles(:,2:4),nodes(:,2),nodes(:,3), ...
	    zeros(size(nodes,1),1));
    set(h,'Marker','.','MarkerSize',8);
    
    get(h)
    
    % plot the fractures
    plot([f11(1) f12(1)], [f11(2) f12(2)],'b','LineWidth',2);
    plot([f21(1) f22(1)], [f21(2) f22(2)],'b','LineWidth',2);

    view(2)
    disp(sprintf('%d points added to domain.', size(nodes,1) - n));
