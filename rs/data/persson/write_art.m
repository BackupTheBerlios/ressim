% [] = write_art (filename, points, bars, faces)
%
% Write the output of a triangulation on ART (Almost Regular Triangulation)
% format, suitable for input to the MUFTE simulator.
function [] = write_art (filename, points, bars, faces)
   % pseudo-constants, to refer to structure fields
   x = 1; y = 2; a = 1; b = 2; c = 3;

   % we use one-based indices, whereas ART uses zero-based. don't change the
   % color of the edges, though!
   bars  (:, a:b) = bars  (:, a:b) - 1;
   faces = faces - 1;

   % boundary marker 1 indicates border, but should not be used since all
   % boundary conditions already have positive numbers in ART input
   notused = eq (bars (:, c), 1); bars (notused, c) = 0;

   % remap type identifier for bars (see read_art() for more comments); subtract
   % one to get the domain 2:inf back to 1:inf-1
   positive = ge (bars (:, c), 2); bars (positive, c) = bars (positive, c) - 1;

   % swap the meaning of negative and positive markers for external and internal
   % boundary by changing the sign back
   bars (:, c) = -bars (:, c);
   
   % open the output file for writing
   fid = fopen (filename, 'W+');  
   
   % header
   fprintf (fid, '%%%% Version 3.0\n');
   fprintf (fid, '%%%% VertexNumber: %d\n', size (points, 1));
   fprintf (fid, '%%%% EdgeNumber: %d\n', size (bars, 1));
   fprintf (fid, '%%%% FaceNumber: %d\n', size (faces, 1));
   fprintf (fid, '%%%% ElementNumber: 0\n');
   
   % vertices
   fprintf (fid, '%% Vertices: x y z\n');
   for i = 1:size (points, 1),
      %fprintf (fid, '%0.20e %0.20e %0.20e\n', points (i, x), points (i, y), 0);
      fprintf (fid, '%6.3f %6.3f %6.3f\n', points (i, x), points (i, y), 0);
   end;
   fprintf (fid, '$\n');
   
   % edges
   fprintf (fid, '%% Edges (Indices to List of Points):\n');
   for i = 1:size (bars, 1),
      fprintf (fid, '%d: %d %d\n', bars (i, c), bars (i, a), bars (i, b));
   end;
   fprintf (fid, '$\n');
   
   % triangles
   fprintf (fid, '%% Faces (Indices to List of Edges):\n');
   for i = 1:size (faces, 1),
      fprintf (fid, '1:');
      for j = 1:length (faces(i, :)),
         fprintf (fid, ' %d', faces (i, j));
      end;
      fprintf (fid, '\n');
   end;
   fprintf (fid, '$\n');
   
   % return file resource handle to operating system
   fid = fclose (fid);