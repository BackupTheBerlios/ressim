#include <math.h>

#if !defined(__STDC_VERSION__) || !(__STDC_VERSION__ >= 199901L)
double round(x)
 double x;
{
   double t, u;
   t = fabs(x);
   u = ceil(t);
   if (u-t > 0.5) u -= 1.0;
   return (x < 0.0 ? -u : u);
}
#endif

