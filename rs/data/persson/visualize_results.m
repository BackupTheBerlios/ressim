% [fig] = visualize_results (vertices, edges, triangles, box)
%
% draw a plot of the a triangulation. vertices is an n-by-2 matrix
% containing an array of (x,y)-coordinates, edges in an m-by-2 matrix
% describing an array of (i,j)-indices of points which there should be
% lines between, and triangles in a k-by-3 matrix of (a,b,c)-indices that
% describes triangles. box is the minimal and maximal coordinates.
function [fig] = visualize_results (vertices, edges, triangles, box, ...
                                    varargin)
    % if we are passed options as the last parameter, then use it to
    % setup the figure. if this function is called interactively, then
    % use common defaults. acquire the options outside the loop for perf.
    if length (varargin) > 0
      opts = varargin{1};
      palette = getfield (opts, 'color');
      width = getfield (opts, 'line');      
    else
      palette = 'rgcmy'; % red, green, cyan, magenta, yellow
      width = 0.5; % default line width
    end;
    
    % the two first types of fractures (0 and 1) are internal partitioning and
    % border respectively; use blue and black for those fractures.
    palette = ['b', 'k', palette];
  
    % start a new graph and add lines to it
    newplot;
    hold on;
    
    % make sure that only the relevant parts of the graph is shown
    axis ([box(1, 1) box(2, 1) box(1, 2) box(2, 2)]);
    
    % white background color
    set (gca, 'Color', 'w');
    
    % first draw the background of triangles edges. the last parameter is
    % filled with the same color on every line
    if size(triangles,1) > 0
      triplot (triangles, vertices(:,1), vertices(:,2), 'black');
    end;
    
    % then draw the special contraint edges in a different color. indices in the
    % palette are one based whereas the boundary markers are zero-based, so we
    % need to add on to the third element of the edge "structure". all negative
    % boundary markers are external boundary conditions, so we visualize them as
    % if they were simple border (index == 2 in the palette).
    for k = 1:size(edges,1)
        plot ([vertices(edges(k, 1), 1) vertices(edges(k, 2), 1)], ...
              [vertices(edges(k, 1), 2) vertices(edges(k, 2), 2)], ...
              palette(mod (max (1, edges(k, 3)), length (palette)) + 1), ...
              'LineWidth', width);
    end;
    
    % we're done with the graph
    hold off;
    axis off;
    drawnow;

    % return the figure in case we want to manipulate it further
    fig = gcf;
