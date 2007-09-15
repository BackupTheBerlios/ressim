% [args] = translate_params (box, iteration, total, opts)
%
% Translate a structure of tunable parameters to a parameter string that is
% passed to the external Triangle program.
function [args] = translate_params (box, iteration, total, opts)
  % if there is no tunables, then no extra parameter is passed
  args = '';
  
  % allow the Triangle program to generate extra Steiner points in order to
  % meet quality constraints, which may be set in other tunables
  if ~isfield (opts, 'steiner') || opts.('steiner')
    area = ideal (getfield (opts, 'elements'), box);
    
    % if we have more than one iteration, it must be to refine the mesh into
    % finer triangles; ask the Triangle program to consider the input files
    % as a step in a range of refinements
    if iteration > 1
      args = ['r' args];
    end;

    % use quality refinement in every level
    args = sprintf ('%sq', args);
  
    % only include the maximum area refinement option for the last
    % iteration (using it on previous iterations causes premature
    % refinement and we get slivers)
    if iteration == total
      % minimum angle may be specified and passed without any prefix
      if isfield (opts, 'minangle')
        angle = opts.('minangle');
        args = sprintf ('%s%2.1f', args, angle);
      end;

      % maximum area may be specified after the 'a' prefix
      if isfield (opts, 'maxarea')
        % combine the relative maximum area with the ideal triangle size
        % given the dimensions of the problem
        relmax = opts.('maxarea');
        args = sprintf ('%sa%3.2f', args, relmax * area);
      end;
    end;
    
    % minimum area may be specified after the 'm' prefix; we always want
    % to enforce this limit in order for it to prevail through all
    % refinement levels
    if isfield (opts, 'minarea')
      relmin = opts.('minarea');
      args = sprintf ('%sm%7.6f', args, relmin * area);
    end;    
  else
    % prohibit the Triangle program from generating extra points
    args = sprintf ('%sS', args );
  end;
  