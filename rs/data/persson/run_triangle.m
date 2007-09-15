% [] = run_triangle (stem, iterations, options)
%
% launch the external process to perform triangularization. the stem is
% the original filename, i.e. without either the iteration counter nor the
% extension. iterations is the number of times that the program should keep
% on refining the triangles and options is a string that is passed verbatim
% to the program. (if you don't want any options, then pass any empty
% array).
function [] = run_triangle (stem, iteration, args, opts)
   % path to the external program to run
   libdir = getfield (opts, 'libdir');
   program = fullfile (libdir, 'triangle');
   
   % input file that should have been generated for this iteration
   name = make_name (stem, iteration-1, 'poly');
   
   % input file is the output from the previous iteration, or the original
   % file written by our program
   cmd = [program ' -pQ' args ' ' name];
   system (cmd);
