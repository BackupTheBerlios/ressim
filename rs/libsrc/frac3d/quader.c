/****************************************************************************/
/*                                                                          */
/* File:      subvol.c                                                      */
/*                                                                          */
/* Purpose:   3D-subvolume: regular prisma                                  */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/*                                                                          */
/* PURPOSE: Calculate the edges, length, ... of the prisma subvolume volume */
/*                                                                          */
/*         1.) calculation of the zentri_angle (= Winkel der Kuchenstuecke) */
/*             and the circle_chord (= Kreis -- Sehne)                      */
/*                                                                          */
/*         2.) calculation the coordinates of the 'subvol_top_midpt' point  */
/*             (just needed as help variable)                               */
/*                                                                          */
/*         3.) calculating the coordinats of the edges of the ground- and   */
/*             top-plane of the subvolume                                   */
/*                                                                          */
/*         4.) define the variable "struct fracture subvol_side" :          */
/*              - subvol_side[i].pt[0]                                      */
/*              - subvol_side[i].pt[1]                                      */
/*              - subvol_side[i].pt[2]                                      */
/*              - subvol_side[i].pt[3]                                      */
/*              - subvol_side[i].norm                                       */
/*              - subvol_side[i].length[0] (= horiz. length = circle_chord) */
/*              - subvol_side[i].length[1]                                  */
/*                      (= vert. length  = subvol_side_length)              */
/*                                                                          */
/*         5.) Calculate the radius_subvol_side:                            */
/*             Sphere radius surrounding the subvol side plane.             */
/*             Constant for all side planes.                                */
/*             (Just for the case of a regulare prisma or quader)           */
/*                                                                          */
/*                                                                          */
/* Functions:                                                               */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "functions.h" 
#include "subvolume3D.h"    


void CalculateSubvolQuaderVariables(int sum_subvol_edges,
                                    double *subvol_radius_bot,
                                    double *subvol_radius_side,
                                    struct point *subvol_bot_midpt,
                                    struct point *subvol_top_midpt,
                                    struct point *subvol_side_midpt,
                                    struct point *subvol_bot_pt,
                                    struct point *subvol_top_pt,
                                    struct fracture *subvol_side)
{
   int i, j, k;

   char char_subvol_bot_pt_x[20],char_subvol_bot_pt_y[20];
   char char_subvol_bot_pt_z[20];

   double pi = 3.141592654;
   double help_subvol_radius_side=0;
   double help_x, help_y, help_radius;

   struct point a, b, help_midpt;
 
   struct fracture help_bottom[1], help_top[1];

   FILE *f10;
   char *file_n10;
   f10 = fopen(file_n10= get_var_char(uvar, "quader3d_file"), "w");


   /*************************************************************************/
   /* Read in the known variables (from the input file)                     */
   /*************************************************************************/
   subvol_side_length = get_var_double(uvar, "subvol_side_length");
   
   for (i=0; i<sum_subvol_edges; i++)
   {
      sprintf (char_subvol_bot_pt_x, "subvol_bot_pt[%d].x",i);
      sprintf (char_subvol_bot_pt_y, "subvol_bot_pt[%d].y",i);
      sprintf (char_subvol_bot_pt_z, "subvol_bot_pt[%d].z",i);

      subvol_bot_pt[i].x = get_var_double(uvar, char_subvol_bot_pt_x);
      subvol_bot_pt[i].y = get_var_double(uvar, char_subvol_bot_pt_y);
      subvol_bot_pt[i].z = get_var_double(uvar, char_subvol_bot_pt_z);

      subvol_top_pt[i].x = subvol_bot_pt[i].x;
      subvol_top_pt[i].y = subvol_bot_pt[i].y;
      subvol_top_pt[i].z = subvol_bot_pt[i].z + subvol_side_length;
   }


   /*************************************************************************/
   /* define the variable "struct fracture subvol_side" :                   */
   /*   - subvol_side[i].pt[0]                                              */
   /*   - subvol_side[i].pt[1]                                              */
   /*   - subvol_side[i].pt[2]                                              */
   /*   - subvol_side[i].pt[3]                                              */
   /*   - subvol_side[i].norm                                               */
   /*   - subvol_side[i].length[0]  (= horiz. length = circle_chord)        */
   /*   - subvol_side[i].length[1]  (= vert. length = subvol_side_length    */
   /*                                                                       */
   /*************************************************************************/
   for (i=0; i<sum_subvol_edges; i++)
   {
      j = i+1;
      if (i == (sum_subvol_edges-1)) j=0;
      
      subvol_side[i].pt[0] = subvol_bot_pt[i];
      subvol_side[i].pt[1] = subvol_bot_pt[j];
      subvol_side[i].pt[2] = subvol_top_pt[j];
      subvol_side[i].pt[3] = subvol_top_pt[i];

      /*normal vector of the plane*/
      a.x = subvol_side[i].pt[1].x - subvol_side[i].pt[0].x;
      a.y = subvol_side[i].pt[1].y - subvol_side[i].pt[0].y;
      a.z = subvol_side[i].pt[1].z - subvol_side[i].pt[0].z;
       
      b.x = subvol_side[i].pt[3].x - subvol_side[i].pt[0].x;
      b.y = subvol_side[i].pt[3].y - subvol_side[i].pt[0].y;
      b.z = subvol_side[i].pt[3].z - subvol_side[i].pt[0].z;
       
      /*vector product  --> normal vector*/
      subvol_side[i].norm.x = (a.y*b.z - a.z*b.y);
      subvol_side[i].norm.y = (a.z*b.x - a.x*b.z);
      subvol_side[i].norm.z = (a.x*b.y - a.y*b.x);

      subvol_side[i].length[0] =
          sqrt(  pow((subvol_side[i].pt[1].x-subvol_side[i].pt[0].x),2)
               + pow((subvol_side[i].pt[1].y-subvol_side[i].pt[0].y),2));
      subvol_side[i].length[1] = subvol_side_length;
   }


   /*************************************************************************/
   /* Calculate the approximative middle point of the poylgon               */
   /*************************************************************************/
   for (i=0; i<sum_subvol_edges; i++)
   {
      help_x += subvol_bot_pt[i].x;
      help_y += subvol_bot_pt[i].y;
   }
   subvol_bot_midpt->x = help_x/sum_subvol_edges;
   subvol_bot_midpt->y = help_y/sum_subvol_edges;
   subvol_bot_midpt->z = subvol_bot_pt[0].z;

   subvol_top_midpt->x = subvol_bot_midpt->x;
   subvol_top_midpt->y = subvol_bot_midpt->y;
   subvol_top_midpt->z = subvol_bot_midpt->z + subvol_side_length; 
                                                  

   /*************************************************************************/
   /* Search for the biggest distance between the subvol_bot_pt -- points   */
   /* and the middle point 'help_midpt'                                     */
   /*************************************************************************/
   for (i=0; i<sum_subvol_edges; i++)
   {
      help_radius = sqrt (  pow((subvol_bot_midpt->x - subvol_bot_pt[i].x),2)
                          + pow((subvol_bot_midpt->y - subvol_bot_pt[i].y),2));

      if (i==0)  {
         *subvol_radius_bot = help_radius;
      }
      else if (help_radius > *subvol_radius_bot) {
         *subvol_radius_bot = help_radius;
      }
   }


   /*************************************************************************/
   /* Calculate the radius of the side planes and the middle points         */
   /* --> applying finction 'approx_radius_sphere()' in file intersection.c */
   /*************************************************************************/
   for (i=0; i<sum_subvol_edges; i++) 
   {
      subvol_radius_side[i] = approx_radius_sphere(i, subvol_side, &help_midpt);
      subvol_side_midpt[i]  = help_midpt;
   }



   /*************************************************************************/
   /* output of the subvol (tecplot)                                        */
   /*************************************************************************/
   fprintf(f10,"TITLE=\"subvol in 3D volume\" ");
   fprintf(f10,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
   fprintf(f10,"\nZONE N=%d, E=%d, F=FEPOINT, ET=QUADRILATERAL",
               2*sum_subvol_edges, sum_subvol_edges);

   for(i=0; i < sum_subvol_edges; i++)
   {
      fprintf(f10,"\n%8.5f \t %8.5f \t  %8.5f", 
       subvol_bot_pt[i].x, subvol_bot_pt[i].y, subvol_bot_pt[i].z); 
   }
   for(i=0; i < sum_subvol_edges; i++)
   {
      fprintf(f10,"\n%8.5f \t %8.5f \t  %8.5f", 
          subvol_top_pt[i].x, subvol_top_pt[i].y, subvol_top_pt[i].z); 
   }
   for(i=0; i < sum_subvol_edges; i++)
   {
      j = i+1; k = i+2;
      if (j == sum_subvol_edges) k = 1;
      fprintf(f10,"\n%d %d %d %d", 
                  j, k, k+sum_subvol_edges, j+sum_subvol_edges);
   }
   fprintf(f10,"\n");
   printf("\n\t Wrote %s.................. done", file_n10);
   fclose(f10);
}

