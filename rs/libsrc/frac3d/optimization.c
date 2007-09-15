/*****************************************************************************/
/*                                                                           */
/* File:      optimization.c                                                 */
/*                                                                           */
/* Purpose:                                                                  */
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
#include "optimization.h"


void optimization_part1()   
{
   int i, j, k;
   int nfraclength = 2;
   int length_dist_type;

   double upper_border;
   double x_Expo, x_Erlang;
   double lambda_h, lambda_v;
   double sum_new_class_length=0;

   static char file_n20[20]="Statistic_frac3d.dat";
   FILE *f20;
   f20 = fopen(file_n20,"w");


   /**************************************************************************/
   /* get values from input file                                             */
   /**************************************************************************/
   nclass_length   = get_var_integer(uvar,"nclass_length");
   nclass_aperture = get_var_integer(uvar,"nclass_aperture");


   /**************************************************************************/
   /* set start values of the following parameters                           */
   /**************************************************************************/
   min_length    =  1000000.0; 
   max_length    = -1000000.0;
   mean_length   = 0.0;
   nlength       = 2*nfrac;  /*for each fracture o values --> total 2*nfrac */


   /**************************************************************************/
   /*                                                                        */
   /**************************************************************************/
   for (i=0; i<nfrac; i++)
   {
      /***********************************************************************/
      /* 1.) looking for the minimum values and maximum values               */
      /***********************************************************************/
      if (FRAC[i].length[0] < min_length) min_length = FRAC[i].length[0];
      if (FRAC[i].length[1] < min_length) min_length = FRAC[i].length[1];

      if (FRAC[i].length[0] > max_length) max_length = FRAC[i].length[0];
      if (FRAC[i].length[1] > max_length) max_length = FRAC[i].length[1];

      /***********************************************************************/
      /* 2.) calculating the mean value                                      */
      /***********************************************************************/
      mean_length += FRAC[i].length[0] + FRAC[i].length[1];
   }

   mean_length = mean_length / nlength;                        /* mean value */
   delta_length = (max_length - min_length)/nclass_length;   /* class length */

   /**************************************************************************/
   /* Allocate memory for  -> new_class_length[]                             */
   /*                      -> old_class_length[]                             */
   /*                      -> var_class_length[]                             */
   /**************************************************************************/
   if ((new_class_length = 
          (double *)malloc(nclass_length * sizeof(double))) == NULL) {
      fprintf(stderr,"Memory allocation failed in 'optimization.c'  \n");
      exit (-1);
   }
   if ((old_class_length = 
          (double *)malloc(nclass_length * sizeof(double))) == NULL) {
      fprintf(stderr,"Memory allocation failed in 'optimization.c'  \n");
      exit (-1);
   }
   if ((var_class_length = 
          (double *)malloc(nclass_length * sizeof(double))) == NULL) {
      fprintf(stderr,"Memory allocation failed in 'optimization.c'  \n");
      exit (-1);
   }


   /*set default values*/
   for (k=0; k<nclass_length; k++){
       new_class_length[k] = 0; 
       old_class_length[k] = 0; 
   }

   /**************************************************************************/
   /* Calculate the number of values in each class                           */
   /**************************************************************************/
   for (i=0; i<nfrac; i++) {
      for (j=0; j<nfraclength ; j++) {
         for (k=0; k<nclass_length; k++) {
            upper_border = min_length + (k+1)*delta_length;
            if (FRAC[i].length[j] <= upper_border) {
                new_class_length[k] = new_class_length[k]+1;
                break;
            }
         }
      }
   }

   /**************************************************************************/
   /* norm the number of the single class [i] by the total amount of values  */
   /**************************************************************************/
   for (i=0; i<nclass_length; i++) {
       new_class_length[i] = new_class_length[i] / nlength;
   }

   /*************************************************************************/
   /* Calculate the values of the given (the original) distribution         */ 
   /*************************************************************************/
   length_dist_type = get_var_integer(uvar,"length_dist_type");
   switch(length_dist_type)
   {
      case 1:    /* trace length distribution: Erlang-2 distribution */
      {
         lambda_h = get_var_double(uvar, "lambda_h_1");
         lambda_v = get_var_double(uvar, "lambda_v_1");

         for (i=0; i<nclass_length; i++) {
             x_Erlang = min_length + ((i+0.5)*delta_length);
             old_class_length[i] = 
                      1 - (1 + lambda_h*x_Erlang) * exp(-lambda_h*x_Erlang);
         }
      }
      case 2:    /* trace length distribution: Exponential distribution */
      {
         lambda_h = get_var_double(uvar, "lambda_h_2");
         lambda_v = get_var_double(uvar, "lambda_v_2");

         for (i=0; i<nclass_length; i++) {
             x_Expo = min_length + ((i+0.5)*delta_length);
             old_class_length[i] = 1 - exp(lambda_h*x_Expo);
         }
      }
   }


   /*************************************************************************/
   /* Calculate the differences between the given distribution              */ 
   /*                                --> old_class_length[]                 */ 
   /* and the new distribution       --> new_class_length[]                 */ 
   /* -->  var_class_length[]                                               */ 
   /*************************************************************************/
   for (i=0; i<nclass_length; i++) {
      sum_new_class_length += new_class_length[i]; 
      var_class_length[i] = old_class_length[i] - sum_new_class_length;
   }



   /*************************************************************************/
   /* output of the different ... density function TODO                     */
   /*                                                                       */
   /* 1.) Haeufigkeitsverteilung : new_class_length[]                       */
   /*************************************************************************/
   fprintf(f20,"TITLE=\"Kluftstatistik: Laengenverteilung \" ");
   fprintf(f20,"\nVARIABLES=\"Laenge\" \"Haeufigkeit\"  ");
   fprintf(f20,"\nZONE T=\"f(x = new length) \" ");
   fprintf(f20,"\nI=%d, J=1, K=1, F=POINT", nclass_length );
   fprintf(f20,"\nDT=(SINGLE SINGLE ) ");

   fprintf(f20,"\n%15.8f \t %f", 0.0, 0.0 ); 
   for(i=0; i<nclass_length; i++) {
      fprintf(f20,"\n%15.8f \t %f", 
              (min_length+((i+0.5)*delta_length)), new_class_length[i]);
   }
   fprintf(f20,"\n");

   /*************************************************************************/
   /* 2.) Summenhaeufigkeitsverteilung : new_class_length[]                 */
   /*************************************************************************/
   fprintf(f20,"\nZONE T=\"F(x = new length) \" ");
   fprintf(f20,"\nI=%d, J=1, K=1, F=POINT", nclass_length );
   fprintf(f20,"\nDT=(SINGLE SINGLE ) ");

   fprintf(f20,"\n%15.8f \t %f", 0.0, 0.0 ); 
   sum_new_class_length = 0;
   for(i=0; i<nclass_length; i++) 
   {
      sum_new_class_length += new_class_length[i]; 
      fprintf(f20,"\n%15.8f \t %f", 
              (min_length+((i+0.5)*delta_length)), sum_new_class_length);
   }
   fprintf(f20,"\n");


   /*************************************************************************/
   /* 3.) Summenhaeufigkeitsverteilung : old_class_length[]                 */
   /*************************************************************************/
   fprintf(f20,"\nZONE T=\"F(x = old length) \" ");
   fprintf(f20,"\nI=%d, J=1, K=1, F=POINT", nclass_length );
   fprintf(f20,"\nDT=(SINGLE SINGLE ) ");

   fprintf(f20,"\n%15.8f \t %f", 0.0, 0.0 ); 
   for(i=0; i<nclass_length; i++) {
      fprintf(f20,"\n%15.8f \t %f", 
              (min_length+((i+0.5)*delta_length)), old_class_length[i]);
   }
   fprintf(f20,"\n");


   /*************************************************************************/
   /* 4.) Variation zwischen 'old_class_length[]' und 'new_class_length[]'  */
   /*************************************************************************/
   fprintf(f20,"\nZONE T=\"Diff = F(old) - F(new) \" ");
   fprintf(f20,"\nI=%d, J=1, K=1, F=POINT", nclass_length );
   fprintf(f20,"\nDT=(SINGLE SINGLE ) ");

   fprintf(f20,"\n%15.8f \t %f", 0.0, 0.0 ); 
   for(i=0; i<nclass_length; i++) {
      fprintf(f20,"\n%15.8f \t %f", 
              (min_length+((i+0.5)*delta_length)), var_class_length[i]);
   }
   fprintf(f20,"\n");

   printf("\n\t Wrote %s.................. done", file_n20);
   fclose(f20);

   free(new_class_length);
   free(old_class_length);
   free(var_class_length);
}



