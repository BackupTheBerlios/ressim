function [vertices, edges, box] = read_openoffice (filename, ext, opts)
  % directory of this function contains the stylesheets
  dir = fileparts (which (mfilename ()));
  
  % start out with empty sets; all information will be read from the file
  vertices = [];

  % extract the style file and use it to find out the maximum and minimum
  % coordinates of the figure
  system (sprintf (['unzip -p "%s" styles.xml | xsltproc --novalid ' ...
    '"%s" - > box.mat'], filename, fullfile (dir, ['box-' ext '.xsl'])));
                   
  % create a bounding box from the min./max. coordinates
  maxcoords = load ('box.mat', '-ASCII');
  box = [0, 0; maxcoords(1), maxcoords(2)];
  
  % extract the content file and run it through the stylesheet to get to
  % we use a pipe instead of the xslt() function so we don't have to store
  % the entire content file ourself.
  system (sprintf (['unzip -p "%s" content.xml | xsltproc --novalid ' ...
    '"%s" - > lines.mat'], filename, fullfile (dir, ['lines-' ext '.xsl'])));
               
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
