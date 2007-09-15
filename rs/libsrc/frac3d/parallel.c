/*****************************************************************************/
/*                                                                           */
/* File:      parallel.c                                                     */
/*                                                                           */
/* Purpose:   Function zur Ueberpruefung der parallelitaet von               */
/*            FRACTURE_Elementen                                             */
/*                                                                           */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                           */
/* Remarks:                                                                  */
/*                                                                           */
/*                                                                           */
/* Functions:                                                                */
/*                                                                           */
/*                                                                           */
/*****************************************************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <math.h>
#include "functions.h"
#include "parallel.h"
#include "intersection.h"
#include "gls_solution.h"


int test_parallel(int frac_nr, struct point norm, struct point pt[4],
                  double frac_aperture)
{
   int i, j;
   int test=0;                        /*Das Testergebniss, default 0 = false */
   int index_cramer;                  /*return value of the function cramer()*/
   int index_check_parallel_case1;                              /* help value*/
   int index_check_parallel_case2;                              /* help value*/

   double c1, c2, c3;  
   double alpha;                    /*angle between two vectors, here in grad*/
   double cos_alpha;                /*cosinus alpha between two vectors      */
   double pi = 3.141592654;
 
   double distance1, distance2;
   double frac_dist, test_dist;
   double radius1, radius2;

   struct point sphere_midpt1, sphere_midpt2;
   struct point a, b0, b1;   
   struct point node2_0, node2_1, node2_2; 


   for (j=0; j < frac_nr; j++)
   {
      index_check_parallel_case1=0;    /*set default value*/
      index_check_parallel_case2=0;    /*set default value*/

      /***********************************************************************/
      /* First case:                                                         */
      /* Vektorprodukt von zwei parallellen Vektoren ergibt den Nullvektor   */
      /*    --> (A x B) C mit (C_x= C_y = C_z = 0)                           */
      /*        mit A = FRAC[j].norm                                         */
      /*        mit B = norm                                                 */
      /*                                                                     */
      /*    Ueberpruefen, ob das Vektorprodukt der zwei Normalenvektoren der */
      /*    zwei Ebenen den Nullvektor ergibt.                               */
      /*                                                                     */
      /*    Bedingung: (c1==0 && c2==0 && c3==0 <= epsilon_0)                */
      /*    --> YES :  Ebenen sind parallel                                  */
      /*    --> NO  :  Ebenen sind nicht parallel, schneiden sich            */
      /*                                                                     */
      /***********************************************************************/
      c1 = ( FRAC[j].norm.y * norm.z) - (FRAC[j].norm.z * norm.y);
      c2 = (-FRAC[j].norm.x * norm.z) + (FRAC[j].norm.z * norm.x);
      c3 = ( FRAC[j].norm.x * norm.y) - (FRAC[j].norm.y * norm.x);

      if (fabs(c1)<=epsilon_0 && fabs(c2)<=epsilon_0 && fabs(c3)<= epsilon_0) 
      {   
         index_check_parallel_case1 = 1;
      }   


      /***********************************************************************/
      /* Second case:                                                        */
      /* the two planes are not parallel, but the angle between their two    */
      /* normal vectors are quite small (size of epsilon_alpha TBD)          */
      /*                                                                     */
      /* 1.) vector a: normal vector plane 1  =>  FRAC[j].norm               */
      /*     vector b: normal vector plane 2  =>  norm                       */
      /*                                                                     */
      /*     cos alpha = (a*b) / |a|*|b|                                     */
      /*               --> apply function 'CosOfTwoVectors()' (subvolume.c)  */
      /*                   return value of the fct 'cos alpha'               */
      /*                                                                     */
      /* 2.) if (alpha <= epsilon_alpha) {                                   */
      /*         --> check the distance between the two planes               */
      /*     }                                                               */
      /*                                                                     */
      /*                                                                     */
      /***********************************************************************/
      a.x = FRAC[j].norm.x;
      a.y = FRAC[j].norm.y;
      a.z = FRAC[j].norm.z;

      epsilon_alpha = 5.0;
      cos_alpha = CosOfTwoVectors(a, norm);
      alpha = acos(cos_alpha) * 180 / pi;

      if (alpha <= epsilon_alpha) { 
         index_check_parallel_case2 = 1; 
      }


      if (index_check_parallel_case1 == 1)
      {   
         /********************************************************************/
         /* Matrix AA aus Matrix A und Vektor b aufbauen (x*A = b)           */
         /*                                                                  */
         /* Uebergabedaten:                                                  */
         /*                                                                  */
         /* Ebene1: neu generierte Ebene                                     */
         /*          b0 = pt[0]                                              */
         /*          b1 = --> Hilfspunkt berechnen                           */
         /*             = pt[0] - lambda_help * norm , mit lambda_help = 1.0 */
         /*             = pt[0] - norm                                       */
         /*                                                                  */
         /* Ebene1: FRAC[j]                                                  */
         /*          node2_0 = FRAC[j].pt[0]                                 */
         /*          node2_1 = FRAC[j].pt[1]                                 */
         /*          node2_2 = FRAC[j].pt[2]                                 */
         /*                                                                  */
         /*                                                                  */
         /********************************************************************/
         b0      = pt[0]; 
         b1.x    = pt[0].x - norm.x;
         b1.y    = pt[0].y - norm.y;
         b1.z    = pt[0].z - norm.z;

         node2_0 = FRAC[j].pt[0];
         node2_1 = FRAC[j].pt[1];
         node2_2 = FRAC[j].pt[2];


         /* Anwendung der Cramerschen Regel auf das lineare GLS */
         index_cramer = cramer(b0,b1, node2_0, node2_1, node2_2);
         if (index_cramer == 1)
         {
            /****************************************************************/
            /* 1.) pruefen ob GLS (berechnet in gauss_elimination)          */
            /*     eindeutige Loesung besitzt (isnan( x_gauss[] ) == 0)     */
            /* 2.) wenn ja: pruefen ob Schnittpkt hp_intersect im           */
            /*     Wertebereich der Geraden k und im Wertebereich der Kluft */
            /*     liegt                                                    */
            /****************************************************************/

            if (isnan(x_cramer[0]) == 0 && isnan(x_cramer[1]) == 0
                                        && isnan(x_cramer[2]) == 0)
            {
               distance1 = fabs(x_cramer[0]
                            *sqrt(pow(norm.x,2)+pow(norm.y,2)+pow(norm.z,2))); 
            }
         }
         /*******************************************************************/
         /* distance1 = Geradenskalierungsfaktor lambda                     */
         /*             x = b0 + lamda * norm                               */
         /* Ist 'distance1' kleiner als 'epsilon_0', so befinden sich die   */
         /* beiden Ebenen in einer Ebene.                                   */
         /* Nun muss noch ueberprueft werden, ob sich die beiden Ebenen     */
         /* wirklich ueberlappen.                                           */
         /*******************************************************************/
         if ((frac_dist = get_var_double(uvar,"frac_dist")) == 0)
         {
            test_dist=(frac_aperture/2)+(FRAC[j].frac_aperture/2)+frac_aperture;
         }
         else 
         {
            test_dist=(frac_aperture/2)+(FRAC[j].frac_aperture/2)+frac_dist;
         }

/*************************
         printf("\nAH (parallel.c) j=%d, frac_nr=%d: distance1=%f  test_dist=%f  frac_dist=%f\n",
                       j, frac_nr, distance1, test_dist, frac_dist);
         printf("                FRAC[%d].pt[0].z=%f, pt[0].z=%f \n",
                       j, FRAC[j].pt[0].z, pt[0].z);
*************************/

/*************************
         printf("\nAH (parallel.c) frac_nr=%d, j=%d: distance1=%f  <-->  test_dist=%f  (frac_dist=%f)",
                       frac_nr, j, distance1, test_dist, frac_dist);
*************************/

         if (distance1 <= test_dist)
         { 
	        FRAC[j+1].norm = norm;
            for (i = 0; i < 4; i++)
            {
               FRAC[j+1].pt[i] = pt[i];
            }

            radius1 = approx_radius_sphere(j+1, FRAC, &sphere_midpt1);
            radius2 = approx_radius_sphere(j,   FRAC, &sphere_midpt2);
            distance2 = abs_vec_pt_pt(&sphere_midpt1, &sphere_midpt2);
     
         printf("\n\nAH (parallel.c) frac_nr=%d, j=%d: distance1=%f  <-->  test_dist=%f  (frac_dist=%f)",
                       frac_nr, j, distance1, test_dist, frac_dist);
            printf("\n                                  radius2=%f + radius1=%f  <-->  distance2=%f",
                       radius2, radius1, distance2);

            if (radius1+radius2 >= distance2) 
            {
               test=1;
               printf("\n                                  neue Kluft parallel + ueberlappen -> verwerfen");
               /*************************************************************/
               /* Das neu zu untersuchende Kluftelement liegt parallel zu   */
               /* einem schon existierenden, zudem ueberlappen sich die     */
               /* beiden Kluftelemente                                      */
               /*                                                           */
               /* --> das neue Kluftelement wird verworfen, nicht in die    */
               /*     globale FRAC--Liste aufgenommen                       */
               /*                                                           */
               /*************************************************************/
            }
         }
      }

      else if (index_check_parallel_case2 == 1)
      {   
         /********************************************************************/
         /* Function 'abs_distance_point_line()' from intersection_points.c  */
         /*                                                                  */
         /*                                                                  */
         /*                                                                  */
         /********************************************************************/
/******
         TODO
         distance1 = abs_distance_point_line(
******/
      }

      if (test == 1) {
         return test;
         break;
      }
   }
/*AH 01.11.99 TODO: return Anweisung noch angeben */
   return test;
}


