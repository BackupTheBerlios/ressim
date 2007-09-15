% [vertices] = snap_to_grid (vertices, box, opts)
%
% Move vertices to the closest (structured) grid point. Use this function to
% avoid having more than one point within each (fine) grid cell.
function [vertices] = snap_to_grid (vertices, box, opts)
  x = 1; y = 2; minimum = 1; maximum = 2;
  precision = getfield (opts, 'aperture');
  if precision > eps && ~isempty (vertices),     
     horz_prec = (box(maximum, x) - box(minimum, x)) .* precision;
     vert_prec = (box(maximum, y) - box(minimum, y)) .* precision;
     vertices = horzcat (round (vertices (:, x) ./ horz_prec) .* horz_prec, ...
                         round (vertices (:, y) ./ vert_prec) .* vert_prec);
  end;