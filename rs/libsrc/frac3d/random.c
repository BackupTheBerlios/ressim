/****************************************************************************/
/*                                                                          */
/* File:      random.c                                                      */
/*                                                                          */
/* Purpose:                                                                 */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/*                                                                          */
/* Functions:                                                               */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <math.h>
#include "functions.h" 


/*******************************************************************/
/* Random--Funktion aus Numerical recipes ... ran3 (p. 212)        */
/*******************************************************************/

#define MBIG 1000000000
#define MSEED 161803398
#define MZ 0
#define FAC (1.0/MBIG)

double ran3(double *idum)
{
  static int inext, inextp;
  static double ma[56];
  static int iff=0;
  double mj,mk;
  int i,ii,k;
  
  if (*idum < 0 || iff == 0) {
    iff=1 ;
    mj = MSEED-(*idum < 0 ? -*idum : *idum);
    mj /= MBIG ;
    ma[55] = mj ;
    mk = 1 ;
    for (i=1; i<=54; i++) {
        ii=(21*i) % 55 ;
        ma[ii] = mk ;
        mk = mj - mk ;
        if (mk < MZ) mk += MBIG ;
        mj = ma[ii] ;
    } 

    for (k=1; k<=4; k++)
      for (i=1; i<=55; i++) {
          ma[i] -= ma[1+(i+30) % 55] ;
          if (ma[i] < MZ) ma[i] += MBIG ;
      }
    inext = 0 ;
    inextp = 31 ;
    *idum = 1 ;
}

if (++inext  == 56) inext = 1 ;
if (++inextp == 56) inextp = 1 ;
mj = ma[inext] - ma[inextp] ;
if (mj < MZ) mj += MBIG ;
ma[inext] = mj ;

return(mj*FAC); 
}


