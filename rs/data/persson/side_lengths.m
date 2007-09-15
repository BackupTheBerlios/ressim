% [sides] = side_lengths (vertices, triangles)
%
% Find the lengths of each side in the triangles. These sizes are commonly
% used in measures.
% 
% Vertices is an m-by-2 vector containing the coordinates of all the 
% mentioned points, and triangles is an n-by-3 vector containing indices to 
% the points in the vertices array. The returning vector will by n-by-3 
% containing the lengths for each side in the triangle.
function [sides] = side_lengths (vertices, triangles)
  % pre-allocate output buffers
  sides = zeros (size (vertices, 1), 3);

	% loop through each of the triangles, calculating the
	% measure individually for each of them. the code can
	% probably be vectorized to perform faster, if needed
	for i = 1:size (triangles, 1)
		% find each of the corners of the triangle. the
		% index in the triangles array denotes the point,
		% the index in the vertices array gives us x and y
		p1 = [vertices(triangles(i, 1), 1) vertices(triangles(i, 1), 2)];
		p2 = [vertices(triangles(i, 2), 1) vertices(triangles(i, 2), 2)];
		p3 = [vertices(triangles(i, 3), 1) vertices(triangles(i, 3), 2)];
		
		% find the vector for each of these edges
		v1 = [p2(1)-p1(1) p2(2)-p1(2)];		% p1 -> p2
		v2 = [p3(1)-p2(1) p3(2)-p2(2)];		% p2 -> p3
		v3 = [p1(1)-p3(1) p1(2)-p3(2)];		% p3 -> p1
		
		% find the length of each of these vectors
		a = sqrt (v1(1).^2 + v1(2).^2);
		b = sqrt (v2(1).^2 + v2(2).^2);
		c = sqrt (v3(1).^2 + v3(2).^2);
    
    % distribute to output buffer
    sides(i, 1) = a;
    sides(i, 2) = b;
    sides(i, 3) = c;
	end;
