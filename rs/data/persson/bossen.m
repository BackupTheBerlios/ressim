% [force] = bossen (ideal, lengths)
%
% Smoothen forces according to a Lennard-Jones potential scheme using both
% attractive and repelling forces, although avoiding numerical instabilities.
function [force] = bossen (ideal, lengths)
   % normalized length is the relation between the actual length and the
   % ideal length; our goal is to create edges with a normalized length of
   % unity. smoothing function has both repelling and attracting forces. see
   % http://www.cs.cmu.edu/afs/cs/user/ph/www/bossen.letter.ps.gz
   % http://www.cs.cmu.edu/~ph/pliant.imr.ps.gz
   norm_length = lengths ./ ideal;
   force = (1 - norm_length.^4) .* exp (-norm_length.^4);
