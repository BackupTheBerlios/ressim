% [x] = solve_2d (A, b)
%
% Solves a linear equation system Ax=b in two dimensions. A is a 2-by-2 
% matrix, while b is a 2-by-1 array. The result is a 2-by-1 vector. If
% the system is not consistent, an empty array is returned.
function [x] = solve_2d (A, b)
  tol = 1e-6;
  if abs (A(2,1)) < tol
    if abs (A(1,1)) < tol
      % linear system is underspecified: first colunm in null
      x = [];
    else
      x(2) = b(2) / A(2,2);
      x(1) = (b(1) - A(1,2) * x(2)) / A(1,1);
    end;
  elseif abs (A(1,2)) < tol
    if abs (A(2,2)) < tol
      % linear system is underspecified: second column is null
      x = [];
    else
      x(1) = b(1) / A(1,1);
      x(2) = (b(2) - A(2,1) * x(1)) / A(2,2);
    end;
  else
    det = A(2,1) - A(2,2) / A(1,2) * A(1,1);
    if abs(det) < tol
      % linear system is not consistent
      x = [];
    else
      x(1) = (b(2) - A(2,2) / A(1,2) * b(1)) / det;
      x(2) = (b(1) - A(1,1) * x(1)) / A(1,2);
    end;
  end;
