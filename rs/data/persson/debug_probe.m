% [] = debug_probe (vertices, edges, triangles, box, rel_move, stage, ...
%                                                             opts, title)
%
% Provide visual feedback on the progress of the algorithm. Stage 1 is
% after the fractures are generated but before any triangles are added,
% stage 2 is after triangulation has occurred, stage 4 is after the
% vertices have been moved whereas stage 8 happens only once when the
% algorithm has converged.
function [] = ...
  debug_probe (vertices, edges, triangles, box, rel_move, stage, opts, title)
  % keep track of how many times this function has been called, which can
  % be used to provide a nice way to break after a given number of
  % iterations
  if stage == 0
    clear global debug_probe_counter
  end;
  global debug_probe_counter
  if isequal (debug_probe_counter, [])
    debug_probe_counter = 0;
  end;

  % boolean options (flags) that may be set:
  opt_save = isfield (opts, 'save') && getfield (opts, 'save');
  opt_visual = isfield (opts, 'visual') && getfield (opts, 'visual');
  
  % if we are going to save, then we have to be in visual mode
  % (strangely enough figures work even if -nodesktop is specified)
  opt_visual = opt_visual || opt_save;
  
  %disp (sprintf ('probe %d:', debug_probe_counter));
  if opt_visual
    if stage == 1
      figure;
      set (gcf, 'Name', title);
    end;
    
    fig = visualize_results (vertices, edges, triangles, box, opts);
    
    if opt_save
      % identifier for the case; if none is specified then use 0
      if isfield (opts, 'id')
        id = sprintf ('-%s', val2str (getfield (opts, 'id')));
      else
        id = '';
      end;
      
      filename = getfield (opts, 'filename'); % 'fig%s-%02d.png'
      % extension is used as the format of the file
      [path, base, ext] = fileparts (filename);
      ext = ext(2:length (ext));              % '.png' -> 'png'
      print (fig, ['-d' ext], sprintf (fullfile (path, base), ...
                                                id, debug_probe_counter));
    end;
  end;
  
  %stats = report_statistics (vertices, triangles);
  %display_statistics (stats);
  
  %disp (sprintf ('rel_move = %f', rel_move));
  %disp (sprintf ('vertices = %d, edges = %d, triangles =%d', ...
  %    size (vertices, 1), size (edges, 1), size (triangles, 1)));  
      
  % prepare for next invocation
  debug_probe_counter = debug_probe_counter + 1;
