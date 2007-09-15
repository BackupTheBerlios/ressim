function [stats] = report_statistics (vertices, triangles)
  % since it can be hard to know how many points
  count = struct ('vertices', size (vertices, 1), ...
                  'triangles', size (triangles, 1));

  % commonly used sizes employed in the statistics
  sides = side_lengths (vertices, triangles);
  areas = triangle_area (sides);

  % calculate measures for each of the triangles
  a_q = angle_quality (sides);
  a_stats = gather_statistics (a_q);
  
  s_q = size_quality (areas);
  s_stats = gather_statistics (s_q);
        
  % alternate angle quality
  t_q = alt_quality (sides, areas);
  t_stats = gather_statistics (t_q);
        
  % combine into a common row
  stats = struct ('count',  count, ...
                  'angle',  a_stats, ...
                  'size',   s_stats, ...
                  'alt',    t_stats);
