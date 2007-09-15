% [vertices, edges, box] = build_stochastic (opts)
%
% Build a stochastic fracture network using Frac3D fracture generator.
function [vertices, edges, box] = build_stochastic (opts)
  run_frac3d (opts);

  % use the X-Z plane as input
  plane = 0;

  % compose the proper filename for this plane
  filename = sprintf ('Subplane3D_%03d', plane);

  % read the problem (box) and its constraints from file
  if isfield (opts, 'art') && opts.('art'),
     [vertices, edges, box] = read_art ([filename, '.art']);
  else
     [vertices, edges, box] = read_tecplot ([filename, '.dat']);
  end;
  
  % remove intermediate files created by the fracture generator
  clean_frac3d;