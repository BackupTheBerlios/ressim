% [qualities] = size_quality (areas)
%
% Calculate the relative size quality of a set of triangles.
% 
% Areas is an m-by-1 vector containing the size of each triangle. The 
% returning vector will by n-by-1 containing the quality measure for each 
% triangle.
function [qualities] = size_quality (areas)
  % preallocate output buffers
  qualities = zeros (size (areas, 1), 1);

	% all sizes are measured relatively to the mean area size
	m = mean (areas);
	
	% when we know the average area size, then calculate the 
	% individual qualities relative to this
	for i = 1:size (areas, 1)
		qualities(i) = m.^2 / (m.^2 + (areas(i)-m).^2);
	end;
