% display_statistics (stats)
%
% Write statistical summary for a data-series.
function display_statistics (stats)
  % number of element generated from the algorithm  
  nv = stats.count.vertices;
  nt = stats.count.triangles;
  disp (sprintf ('count: vertices=%d, triangles=%d', nv, nt));

	% statistical measures
	m = stats.angle.mean;   % (1);
	s = stats.angle.stddev; % (2);
  p = stats.angle.pct;
	%l = stats.angle.min;    % (3);
	%h = stats.angle.max;    % (4);
	
	% write to console
	disp (sprintf ('angle: mean=%f, stdev=%f, 1pct=%f', m, s, p));

	% alternate measures
	m = stats.alt.mean;     % (1);
	s = stats.alt.stddev;   % (2);
  p = stats.alt.pct;
	%l = stats.alt.min;     % (3);
	%h = stats.alt.max;     % (4);
	
	% write to console
	disp (sprintf ('alt  : mean=%f, stdev=%f, 1pct=%f', m, s, p));

  % statistical measures
	m = stats.size.mean;    % (5);
	s = stats.size.stddev;  % (6);
  p = stats.size.pct;
	%l = stats.size.min;     % (7);
	%h = stats.size.max;     % (8);
	
	% write to console
	disp (sprintf ('size : mean=%f, stdev=%f, 1pct=%f', m, s, p));
  
  % time/benchmark measures
  p = stats.time.prep;
  t = stats.time.triangle;
  a = stats.time.adjust;
  
  disp (sprintf ('time : prep=%f, tri. =%f, adj.=%f', p, t, a));
        
