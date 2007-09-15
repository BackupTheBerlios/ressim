% [rounded] = num_of_dec (values, decimals)
%
% Round to a certain number of decimals, when written on scientific notation,
% e.g. num_of_dec (1.8673e-3, 2) == 1.86e-3.
function [rounded] = num_of_dec (values, decimals)
   % use absolutes for logarithm and avoid log(0) errors
   absolutes = abs (values);
   absolutes(~absolutes) = 1;

   % find the exponent of each of the numbers
   exponents = round (log10 (absolutes));
   
   % find the mantissas for each value, i.e. strip off the exponents
   mantissa = values ./ 10 .^ exponents;
   
   % chop off all the digits after the desired number of decimals
   chopped = floor (mantissa .* 10 .^ decimals);
   
   % return to the same form as it were originally
   rounded = chopped .* 10 .^ -(decimals - exponents);