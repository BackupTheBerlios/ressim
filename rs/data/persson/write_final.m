% [] = write_final (vertices, edges, triangles, box, opts)
%
% Write the final mesh in the desired format.
function [] = write_final (vertices, edges, triangles, box, opts)
  % generate output for the MUFTE-UG simulator
  if isfield (opts, 'art') && opts.('art'),
     if isfield (opts, 'fractures'),
        % get the name of the input file and change the extension
        filename = change_ext (getfield (opts, 'fractures'), 'net');
     else
        filename = 'Subplane3D_000.net';
     end;
     
     % find triangles in terms of edges, not vertices
     [bars, faces] = find_bars (triangles);
     bars = annotate_bars (bars, edges);
     
     % write data on ART-compatible output
     write_art (filename, vertices, bars, faces);
     
     % write boundary conditions inferred from the network
     if isfield (opts, 'bnd') && opts.('bnd')
        % create the smallest set of fractures possible. since the coalesce
        % function is able to detect crossings, we get the same set of vertices
        % that was passed as input.
        edges = coalesce_edges (vertices, edges, true);
        
        % ditch unnecessary points
        [vertices, edges] = compact (vertices, edges, [], box, opts);
        
        % nicer output
        [vertices, edges] = sort_edges (vertices, edges);

        % write the entire domain as one element, in counter-clockwise order
        border = hull (vertices, edges, box);
        border = counter_clockwise (vertices, edges, border, box);        

        bndfile = change_ext (filename, 'bnd');
        write_art (bndfile, vertices, edges, border);     
     end;
  end;
