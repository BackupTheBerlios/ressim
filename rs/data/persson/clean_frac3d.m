% [] = clean_frac3d ()
%
% Remove intermediate files that are output from Frac3D fracture generation
% program. This function must not be called before the input has been read
% from the triangulation routine.
function [] = clean_frac3d ()
  system ('rm Eingabefile.dat');
  system ('rm 2D_slices.dat');
  system ('rm Subplane3D_*.dat');
  system ('rm Subplane3D_*.art');