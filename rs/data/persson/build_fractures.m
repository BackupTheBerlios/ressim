% [vertices, edges, box] = build_fractures (opts)
%
% Build fracture network that serves as constraints in the triangulation.
function [vertices, edges, box] = build_fractures (opts)
  % if we have specified our own fractures, then use these instead of
  % generating a stochastic network
  if isfield (opts, 'fractures')
    % filename is contained in the parameter; use the extension to
    % determine the type of the file
    filename = getfield (opts, 'fractures');
    [path, base, ext] = fileparts (filename);
    
    % OpenOffice 2.0 presentations
    ext = lower (ext(2:length (ext)));
    if strcmp (ext, 'odp') | strcmp (ext, 'sxi')
      [vertices, edges, box] = read_openoffice (filename, ext, opts);
    % Adobe Illustrator
    elseif strcmp (ext, 'svg')
      [vertices, edges, box] = read_svg (filename, opts);
    % Triangle 1.x input files
    elseif strcmp (ext, 'poly')
      [vertices, edges, box] = read_poly (filename, opts);
    % space/tab-delimited plain text, four columns having (x1,y1)-(x2,y2)
    elseif strcmp (ext, 'txt')
      [vertices, edges, box] = read_txt (filename, opts);
    % ART (Almost Regular Triangulation) input files
    elseif strcmp (ext, 'bnd') || strcmp (ext, 'art')
      [vertices, edges, box] = read_art (filename, opts);
    elseif strcmp (ext, 'net')
      [vertices, edges, box] = read_art (filename, opts);
      edges = coalesce_edges (vertices, edges, true);
      [vertices, edges] = compact (vertices, edges, [], box, opts);
    else
      error ('Unknown file format: %s', filename);
    end;
    
  else
    % Frac3D
    [vertices, edges, box] = build_stochastic (opts);
  end;

  % snap the fractures to the nearest grid; precision is relative to dimensions
  vertices = snap_to_grid (vertices, box, opts);
