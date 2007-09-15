% [area_per_elem] = ideal (num, box)
%
% Figures out the ideal size of an element, given that it is desirable to
% fit num elements into box.
function [area_per_elem] = ideal (num_of_elements, box)
  % get the size of the box
  x_size = box(2, 1) - box(1, 1);
  y_size = box(2, 2) - box(1, 2);
  total_area = x_size * y_size;

  % we tend to generate too many elements due to the border constraint and
  % the fact that the edges will remove some and then add some. this 
  % function adjusts the initial number somewhat. however, there is no
  % theory whatsoever backing it -- it has been empirically determined and
  % is probably not valid outside our range of cases!
  num_of_elements = num_of_elements / (1.53 - 0.0577 * log(num_of_elements));

  % we want there to be dx distance between elements in the x direction;
  % to figure out the distance we calculate the area desired and uses the
  % relationship between the sides to find the actual length
  area_per_elem = total_area / num_of_elements;
   