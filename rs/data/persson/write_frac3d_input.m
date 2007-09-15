% [] = write_frac3d_input (master, instance, opts)
%
% Write an input file for Frac3D. Master is the name of the original
% input file, whereas instance is the name of the file that will be
% written. Options is an array containing options for this one case that
% will be built (typically a row of the input matrix).
function [] = write_frac3d_input (master, instance, opts)
  % extract the options from the options array. we use a random seed
  % between 0.0 and 1.0.
  rseed = opts.('seed');  % 0.230975101
  density = opts.('density'); % 0.2
  
  % open both the files, starting with the instance file since there is a
  % greater chance that we'll experience problems doing that
  fout = fopen (instance, 'W');
  fin = fopen (master, 'r');

  % generate an output file based on the input
  while true
    % read one more line of input if present; otherwise, stop iterating
    line = fgetl (fin);
    if ~ischar (line)
      break;
    end;
    
    % run the line through the various filters. the first and third group
    % are everything before and after the value we are set to replace;
    % the second group will be replaced by the new parameter. note that
    % Matlab uses \s instead of \w for whitespace.
    if size (regexp (line, 'double\s+rseed\s+\d+\s*'), 1) == 1
      line = sprintf ('double rseed %10d', round (rseed * 2.^31));
    end;
    
    if size (regexp (line, 'double\s+frac_dens_3d\s+\d+(\.\d+)?\s*'), 1) == 1
      line = sprintf ('double frac_dens_3d %f', density);
    end;
    
    % write the line back to the instance file (file for this problem)
    fprintf (fout, '%s\n', line);
  end;
  
  % return the file handles to the operating system
  fclose (fin);
  fclose (fout);
