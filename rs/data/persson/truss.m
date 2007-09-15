% [force] = truss (ideal, lengths)
%
% Smoothen forces according to a truss scheme, where only repelling forces act;
% if the points get too far, then the springs don't pull them back together.
function [force] = truss (ideal, lengths)
   % find the difference between the ideal length (which is a scalar that
   % applies to all points) and each of the lengths, setting the difference
   % to zero if the bars are already longer than the ideal length
   force = max (ideal - lengths, 0);

