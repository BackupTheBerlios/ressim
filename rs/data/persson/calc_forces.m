% [forces] = calc_forces (vertices, bars, ideal, opts)
%
% Calculate the force that will apply to each vertex from the bar springs.
% If the length is less than the ideal size, then a force will be put on
% those endpoints in an attempt to push them away from eachother.
function [forces] = calc_forces (vertices, bars, ideal, opts)
   % start out with no force to apply in both dimensions
   forces = zeros (size (vertices));
   
   % find the length of the bars using the coordinates of the points;
   % there'll be one length for each bar. the vectors go from b (second
   % index) to a (first index)
   vectors = vertices(bars(:, 2), :) - vertices(bars(:, 1), :);
   lengths = sqrt (sum (vectors .^ 2, 2));
      
   % find the ideal length from the sample
   %ideal = mean (lengths);
      
   % fudge factor; if we go for a slightly longer length, then we increase
   % the tension in the springs and more chance that a bar exhibit a
   % positive force
   ideal = ideal * opts.('scale'); % 1.2;

   % use selected variant of smoothing funcion to adjust points according to
   % desired length of an ideal edge (which is here assumed to be the same for
   % the entire mesh since we currently only handle isotropic grids)
   smoothing = str2func (opts.('smoothing'));
   force_lengths = smoothing (ideal, lengths);
      
   % divide each vector by its length so that they have a normative length
   % of one; then multiply with the force, getting the force that applies
   % in each direction. vectors are still in the direction from b (second
   % index) towards a (first index).
   unit_vectors = vectors ./ (lengths * [1, 1]);
   dir_forces = unit_vectors .* (force_lengths * [1, 1]);
   
   % apply half of each of the forces to each of the endpoint vertices. 
   % this could probably be vectorized.
   for i = 1:size (bars, 1)
      % indices for each of the endpoints for the bar
      a = bars(i, 1);
      b = bars(i, 2);
      
      % forces that apply to each of the endpoints. since the vector goes
      % from b to a, then prolonging it will create a repulsive force on a.
      % to do the same to b, we need to change the direction of the vector
      forces(a, :) = forces(a, :) - 0.5 .* dir_forces(i, :);
      forces(b, :) = forces(b, :) + 0.5 .* dir_forces(i, :);
   end;
   
   % artificial time step; don't do the entire movement at once, just move
   % towards that goal and then reevaluate at the next step if we are
   % heading in the right direction
   dt = opts.('timestep'); % 0.2;
   forces = forces .* dt;