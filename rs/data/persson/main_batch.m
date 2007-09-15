% [] = main_batch (input_filename, output_filename, extra)
%
% Run through the program using an input file to specify the options for
% each test run. If any extra options are specified, they will be used
% instead of the default.
function [] = main_batch (input_filename, output_filename, extra)
  % read a list of options we are going to alter for each iteration
  specified = read_csv (input_filename);
  
  % array of output statistics; there are going to be as many rows here as
  % in the input array of specified tunables
  all_output = [];

  % each line of the input is a different case
  for i = 1:size(specified, 1)
    % retrieve a full set of options for this case, using default values
    % for those options that were not specified in the file
    user_opts = merge_structs (extra, specified(i));
    all_opts = merge_structs (user_opts, default_options);
    
    % generate title from the options that the user specified
    if isfield (user_opts, 'active')
      user_opts = rmfield (user_opts, 'active');
    end;
    title = opts_to_str (user_opts);

    % generate fractures using Frac3D and run triangulation on them
    if getfield (all_opts, 'active')
      stats = flatten (main_once (all_opts, title));
    else
      stats = empty_stats (all_opts);
    end;

    % combine the statistics of the output with the case data from the input
    % giving a complete table of the cases that can be analyzed. this file
    % could even be used as another input file since the latest output is
    % always kept! (it is specified first in the merge). note that we only
    % write the specified tunables and not the default ones, and also that
    % there should be exactly as many output rows as there are tunables,
    % making it possible to merge those two structure lists.
    output = merge_structs (specified(i), stats, true);
    all_output = [all_output; output];    
  end;
  
  % write the output 
  write_csv (output_filename, all_output);