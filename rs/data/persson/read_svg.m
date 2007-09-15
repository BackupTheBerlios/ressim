function [vertices, edges, box] = read_svg (filename, opts)
  % directory of this function contains the stylesheets
  dir = fileparts (which (mfilename ()));
  
  % start out with empty sets; all information will be read from the file
  vertices = [];
  edges = [];

  % do the same operation to find the bounding box
  %xslt (filename, 'box-svg.xsl', 'box.mat');
  system (sprintf ('cat "%s" | xsltproc --novalid "%s" - > box.mat', ...
                               filename, fullfile (dir, 'box-svg.xsl')));

  % create a bounding box from the min./max. coordinates
  maxcoords = load ('box.mat', '-ASCII');
  box = [0, 0; maxcoords(1), maxcoords(2)];
  
  % process the file with a stylesheet to find the line data in a format
  % that we can import directly in Matlab. xslt() unfortunately does not
  % handle a relative path.
  %xslt (filename, 'lines-svg.xsl', 'lines.mat');
  system (sprintf ('cat "%s" | xsltproc --novalid "%s" - > lines.mat', ...
                               filename, fullfile (dir, 'lines-svg.xsl')));

  % read the coordinates that was extracted; from these coordinates we can
  % create a set of vertices and lines
  linecoords = load ('lines.mat', '-ASCII');
  
  % currently, all fractures are of type 0. we could map each color to its own
  % type and use that information to decide which multiplier it should be
  % assigned.
  t = 0;  
  
  % create vertices and edges from the start and end coordinates
  % respectively. each row in the file is one line with four values on it
  edges = zeros (size (linecoords, 1), 3);
  for i = 1:size (linecoords, 1)
    [vertices, a] = ...
       add_point (vertices, [linecoords(i, 1), linecoords(i, 2)], box, opts);
    [vertices, b] = ...
       add_point (vertices, [linecoords(i, 3), linecoords(i, 4)], box, opts);
    edges(i, :) = [a, b, t];
  end;
  
  % remove temporary file after it is loaded
  system ('rm lines.mat');
  system ('rm box.mat');

  % second coordinate is swapped when reading from OpenOffice; we need to
  % turn it upside-down.
  y = 2; min = 1; max = 2;
  vertices(:, y) = box(max, y) - (vertices(:, y) - box(min, y)) + box(min, y);
