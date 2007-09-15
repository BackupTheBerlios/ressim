% [] = cleanup_files (stem, iterations)
%
% remove intermediate files after running the triangularization algorithm
% by launching an external program. the filenames must be given by their
% original stem, i.e. the filename with neither the extension nor the
% iteration counter.
function [] = cleanup_files (stem, iterations)
   % command to delete files in the file system
   delete = 'rm -f ';

   % remove all the output files, for all iterations
   for i = 0:iterations
      % first iteration have only the constraints
      if i == 0
         ext = ['poly'];
      % further iterations have all the files
      else
         ext = ['ele '; 'poly'; 'node'];
      end;

      % remove all files associated with this iteration
      for j = 1:size (ext, 1)
         name = make_name (stem, i, ext(j, :));
         system ([delete name]);
      end;
   end;
   
