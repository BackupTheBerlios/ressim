% [stats] = gather_statistics (qualities)
%
% stats is a row containing the mean, standard deviation, min and max
% value for the angle quality and the size quality respectively.
function [stats] = gather_statistics (qualities)
	% statistical measures
	m = mean (qualities);
	s = std (qualities);
  q = sort (qualities);
  p = q (max (1, floor (length (qualities) / 100))); % one'th percentile
	%l = min (qualities);
	%h = max (qualities);

  stats = struct ('mean', m, 'stddev', s, 'pct', p); %, 'min', l, 'max', h);
        
