% [stats] = main (options)
%
% Main driver for the program. Specify any options as pairs of names and
% values as arguments to the function. If you want to run it in batch mode,
% give the names of the files as shown below. Otherwise, it returns the
% statistics of a single run-through.
%
% E.g.
%   display_statistics (main);
%   main ('input', 'inp.csv', 'output', 'outp.csv');
function [varargout] = main (varargin)
  % start afresh with the performance counters
  clear_perf;

  % convert the options sent to us into a structure of tunable parameters
  opts = struct (varargin{:});
  
  % if an input and output file is specified, then use 
  if isfield (opts, 'input') && isfield (opts, 'output')
    input_filename = getfield (opts, 'input');
    output_filename = getfield (opts, 'output');
    
    % remove those fields from the options so that they won't be part of
    % the output
    opts = rmfield (rmfield (opts, 'input'), 'output');
    
    % run through all the options specified in that file, using any extra
    % options specified on the "command-line" before those in the file
    main_batch (input_filename, output_filename, opts);
  else
    % if batch mode was not specified, then simply run the program using
    % the options that was our arguments. when we run the program once, the
    % default is to keep the output
    stats = main_once (merge_structs (opts, ...
            default_options ('keep', 1, 'visual', 1)), opts_to_str (opts));
    varargout(1) = {stats};
  end;