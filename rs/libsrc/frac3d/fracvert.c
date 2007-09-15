/*****************************************************************************/
/* File:      fracvert.c                                                     */
/*                                                                           */
/* Purpose:   Verteilungsfunktionen                                          */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                           */
/*****************************************************************************/
#include <stdlib.h>
#include <stdio.h>
#include <time.h> 
#include <math.h> 
#include "functions.h" 
#include "fracvert.h" 

double fx;
double x1;


/*****************************************************************************/
/* Fakultaet                                                                 */
/*****************************************************************************/

double fak(double x)
{
  int i;
  int facx = 1;
  for (i = 1; i<=x; i++) facx *= (i+1);
  return (facx);
}


/*****************************************************************************/
/* Gleichverteilung                                                          */
/*****************************************************************************/

double glvert(double xrand)
{
  x1 = xrand;
  return (x1);
}


/*****************************************************************************/
/* konstante Werte fuer Normal und log-Normalverteilung                      */
/* Gundlage: "percent point Verfahren" nach R.E. ODEH & J.O. EVANS           */
/* aus "Applied Statistics" Vol.23, 1974                                     */
/* Journal of the Royal Statistical Society, London                          */
/*****************************************************************************/

#define p0 -0.322232421088
#define p1 -1.0
#define p2 -0.342242088547
#define p3 -0.204231210245 * pow(10,-1)
#define p4 -0.453642210148 * pow(10,-4)

#define q0 0.993484626060 * pow(10,-1)
#define q1 0.588581570495 
#define q2 0.531103462366
#define q3 0.103537752850
#define q4 0.38560700634 * pow(10,-2)

/*****************************************************************************/
/* Normalverteilung                                                          */
/*****************************************************************************/

double normvert(double xrand)
{
  double xx, T, ANUM, ADEN;
  if (xrand == 0.5) return (0.0);
  if (xrand > 0.5) xx = 1-xrand;
  else xx = xrand;
  T = sqrt(-2.0*log(xx));
  ANUM = (p0+p1*T+p2*pow(T,2)+p3*pow(T,3)+p4*pow(T,4));
  ADEN = (q0+q1*T+q2*pow(T,2)+q3*pow(T,3)+q4*pow(T,4));
  x1 = T+(ANUM/ADEN);
  if (xrand < 0.5) x1 = -x1;
  return (x1);
}


/*****************************************************************************/
/* logarithmische Normalverteilung                                           */
/*****************************************************************************/

double lognormvert(double xrand, double sigma, double mue)
{
   double xx,x0, T, ANUM, ADEN;

   if (xrand == 0.5) return (0.0);
   if (xrand > 0.5) xx = 1-xrand;
   else xx = xrand;

   T = sqrt(-2.0*log(xx));
   ANUM = (p0+p1*T+p2*pow(T,2)+p3*pow(T,3)+p4*pow(T,4));
   ADEN = (q0+q1*T+q2*pow(T,2)+q3*pow(T,3)+q4*pow(T,4));
   x0 = T+(ANUM/ADEN);

   if (xrand < 0.5) x0 = -x0;

   x1 = sigma * exp(x0) + mue;

   return (x1);
}


/*****************************************************************************/
/* Negative Exponentialverteilung                                            */
/* f(x) = 1 - exp(lambda*x)   mit   lambda < 0                               */
/*****************************************************************************/

double expvert(double xrand, double lambda)
{
   double x1;

/* AH, 21.02.2000:  x1 = 1 - pow(10,(xrand*lambda)); */

   /*x1 = -1/lambda * pow(10,(1-xrand));*/
   /*x1 = -1/lambda * pow(2.71,(1-xrand));*/
   x1 = log(1-xrand) / lambda; 

   return (x1);
}


/*****************************************************************************/
/* Poisson-Verteilung                                                        */
/*****************************************************************************/

double poisvert(double xrand, double lambda)
{
  x1 = (pow(lambda,xrand)/fak(xrand))*exp(-lambda);     /* fac Funktion s.o. */
  return (x1);
}


/*****************************************************************************/
/* Erlang-2-Verteilung                                                       */
/*****************************************************************************/

double x1;/*Funktionsvariable*/
double erl2vert(double xrand, double lambda, double x0, double epsilon_Erlang)
{
   double Fx;
   double F_x;

   double x;
   double epsilon2;
   x = x0;
   do
   { 
      /*Newton Iteration*/
      Fx = 1 - (1 + lambda * x)* exp(-lambda * x);
      F_x = (pow(lambda,2))* x * exp(-lambda * x);

      x1 = x - ((Fx-xrand)/F_x);
      epsilon2 = fabs(x-x1);                   /*absoluter Wert |x|*/
      x = x1;
   }while (epsilon_Erlang < epsilon2);

   /**************************************************************************/
   /*  AH 23.09.99                                                           */
   /*  Problem: Iteration kommt aus einem "lokalen Minimum" nicht mehr raus  */
   /*  --> neuen Startwert vorgeben, der im Bereich des halben Wertebereiches*/
   /*      liegt: [a,b]/2.0 --> x = [0,1.5m] --> x0 = 0.75 (z.B.)            */
   /*                                                                        */
   /*  !!! NOCH PROGRAMMIEREN: neuer Startwert ueber Eingabefile eingeben !!!*/
   /*                                                                        */
   /**************************************************************************/
   if ( (x1 < 0) || (isnan(x1) != 0) )   
   {
      /*AH printf("\n\n Neuer Startwert fuer Iteration vorgeben, Versuch1 "); */
      x = x0 = 0.75; 
      do
      {
         /*Newton Iteration*/
         Fx = 1 - (1 + lambda * x)* exp(-lambda * x);
         F_x = (pow(lambda,2))* x * exp(-lambda * x);

         x1 = x - ((Fx-xrand)/F_x);
         epsilon2 = fabs(x-x1);                   /*absoluter Wert |x|*/
         x = x1;
      }while (epsilon_Erlang < epsilon2);
   }
   
   return (x1);
}

/*****************************************************************************/
/* KLUFTORIENTIERUNG                                                         */
/*   Berechnung des Normalenverktors einer Ebene im Raum direkt ueber den    */
/*   Azimut und den Fallwinkel Phi (keine Variation !!!)                     */
/*                                                                           */
/*   Nach Fisher et al. (1993), p.19--21                                     */
/*   Koordinatensystem x-y-z: Rechtsystem                                    */
/*                 +x: entspricht der Sued-Richtung (geologisches KoordSyst) */
/*                 +y: entspricht der Ost -Richtung (geologishces KoordSyst) */
/*                 +z: positiv nach oben gerichtet                           */
/*                                                                           */
/*****************************************************************************/
struct point kluftor_norm_nonvariat(double Azimut, double Dip)
{
  double Theta, phi;
  struct point norm_vec;
  const double pi= 4*(atan(1));

  Theta = Dip +  pi/2.0 ;
  phi   = 2*pi - Azimut; 
  
  norm_vec.x = cos(Theta)*cos(phi);
  norm_vec.y = cos(Theta)*sin(phi);
  norm_vec.z = sin(Theta);
  /*AH, steht so falsch in Fisher (1993), p.19: 
    norm_vec.z = sin(Theta);
  */

  return (norm_vec);
}



/*****************************************************************************/
/* KLUFTORIENTIERUNG                                                         */
/*   Spaerische Normalverteilung / Fisher Verteilung                         */
/*   nach Fisher et al. (1993), page 59                                      */
/*                                                                           */
/* Anmerkung AH21.04.99: scheint zu klappen, noch weiter testen!             */
/* TODO: Bedeutung der Funktionswerte der Fisher-Verteilung nochmal          */
/*       ueberpruefen                                                        */
/*****************************************************************************/
struct point kluftor_sphere_fisher_AH(double Azimut, double Dip, double kappa)
{
   const double pi= 4*(atan(1));
   double Theta_geo_n, phi_geo_n; 
   double Theta_n, phi_n;         /*polar coodinates of 'point' n*/
   double Theta_var1, phi_var1; 
   double Theta_var2, phi_var2;         /*polar coodinates of 'point' n*/
 
   double lambda;
   double R[3][3];                                        /* Rotationsmatrix */

   struct point n;            /*upward directed normal (or pole) to the plane*/
   struct point var1;
   struct point var2;

 
   /*azimut A and dip D --> in terms of geological coordinates: (Fisher, p.19)*/
   Theta_geo_n = Dip + pi/2;
   phi_geo_n   = 2*pi - Azimut; 
 
   /*upward directed normal (or pole) to the plane n_plane  (Fisher, p.19)*/
   n.x = cos(Theta_geo_n) * cos(phi_geo_n);
   n.y = cos(Theta_geo_n) * sin(phi_geo_n);
   n.z = sin(Theta_geo_n);
   /*n.z = - sin(Theta_geo_n);*/
 
   /**************************************************************************/
   /* polar coordinates of the upward directed normal n_plane (Fisher, p.30) */
   /* in generell: Bronstein p. 217                                          */
   /*                                                                        */
   /* Theta_polar_n = acos( n.z / sqrt(n.x*n.x + n.y*n.y + n.z*n.z));        */
   /* phi_polar_n   = atan(n.y / n.x);                                       */
   /*                                                                        */
   /* Care is required for the cases: n.x=0, n.y=0 !!!                       */
   /**************************************************************************/
   Theta_n = acos( n.z / sqrt(n.x*n.x + n.y*n.y + n.z*n.z)); 
   
   if ((fabs(n.x) <= epsilon_0) && (fabs(n.y) <= epsilon_0)) { 
      phi_n = 0.0; 
   }
   else if (fabs(n.x) <= epsilon_0)  {
      if (n.y > 0.0)      phi_n = pi/2;
      else if (n.y < 0.0) phi_n = 3/2*pi;
   }
   else if (fabs(n.y) <= epsilon_0)  {
      if (n.x > 0.0)      phi_n = 0.0;
      else if (n.x < 0.0) phi_n = pi;
   }
   else {phi_n  = atan(n.y / n.x); }


   /**************************************************************************/
   /* Simulate data from the Fisher distribution                             */
   /* Fisher et al. (1993), p.59                                             */
   /**************************************************************************/
   lambda         = exp(-2.0 * kappa);
   Theta_var1 = 2*asin(sqrt(-log10(ran3(&rseed)*(1-lambda)+lambda)/(2 * kappa)));
   phi_var1   = 2*pi * ran3(&rseed);  

  
   var1.x = sin(Theta_var1) * cos(phi_var1);
   var1.y = sin(Theta_var1) * sin(phi_var1);
   var1.z = cos(Theta_var1);
 
   /**************************************************************************/
   /* Rotate (Theta_var, phi_var) to (Theta_1, phi_1) using rotation matrix R*/
   /* Rotation matrix R (Theta_n, phi_n, 0)                                  */
   /*                                                                        */
   /* (see: Fisher (1993), p.32:  R (general form of the rotation matrix)    */
   /**************************************************************************/
   R[0][0] = cos(Theta_n) * cos(phi_n);
   R[0][1] = cos(Theta_n) * sin(phi_n);
   R[0][2] = - sin(Theta_n);
   R[1][0] = - sin(phi_n);
   R[1][1] = cos(phi_n);
   R[1][2] = 0;
   R[2][0] = sin(Theta_n) * cos(phi_n);
   R[2][1] = sin(Theta_n) * sin(phi_n);
   R[2][2] = cos(Theta_n);
   
  
   /*gesucht: inverse Matrix --> apply Gauss-Jordan elimination */
   n_gj = m_gj = 3;
   GaussJordan(R, B_GJ, n_gj, m_gj);

 
   /**************************************************************************/
   /* v = matrix 'R' * vector 'var'                                          */
   /**************************************************************************/
   var2.x = R[0][0]*var1.x + R[0][1]*var1.y + R[0][2]*var1.z; 
   var2.y = R[1][0]*var1.x + R[1][1]*var1.y + R[1][2]*var1.z;
   var2.z = R[2][0]*var1.x + R[2][1]*var1.y + R[2][2]*var1.z;
  
   Theta_var2 = acos( var2.z / sqrt(var2.x*var2.x + var2.y*var2.y + var2.z*var2.z)); 
   
   if ((fabs(var2.x) <= epsilon_0) && (fabs(var2.y) <= epsilon_0)) { 
      phi_var2 = 0.0; 
   }
   else if (fabs(var2.x) <= epsilon_0)  {
      if (var2.y > 0.0)      phi_var2 = pi/2;
      else if (var2.y < 0.0) phi_var2 = 3/2*pi;
   }
   else if (fabs(var2.y) <= epsilon_0)  {
      if (var2.x > 0.0)      phi_var2 = 0.0;
      else if (var2.x < 0.0) phi_var2 = pi;
   }
   else {phi_var2  = atan(var2.y / var2.x); }

   return (var2);
}
