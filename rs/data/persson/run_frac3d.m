function [] = run_frac3d (opts)
  % get the directory of this function, which is where we assume that the
  % input files are also stored
  dir = fileparts (which (mfilename ()));
  
  % copy the template files to the read input files
  [s, w] = system (sprintf ('cp -f "%s" 2D_slices.dat', ...
                                  fullfile (dir, '2D_slices.dat.orig')));  
  write_frac3d_input (fullfile (dir, 'Eingabefile.dat.orig'), ...
                                                'Eingabefile.dat', opts);
  
  % change the file name in a way such that we either have backup of the
  % old file or the file is successfully changed
  %system ('mv -f Eingabefile.dat Eingabefile.old');
  %system ('mv Eingabefile.new Eingabefile.dat');
  
  % run the program itself
  libdir = getfield (opts, 'libdir');
  [s, w] = system (fullfile (libdir, 'frac3d'));
  
  % cleanup unnecessary intermediate files
  [s, w] = system ('rm Statistic_frac3d.dat');
  [s, w] = system ('rm Subplane3D_00[1-5].art');
  [s, w] = system ('rm Subplane3D_00[1-5].dat');
  [s, w] = system ('rm Quader_3D.tec');
  [s, w] = system ('rm 3Dtest_*');
