/****************************************************************************/
/*                                                                          */
/* File:      subvol.c                                                      */
/*                                                                          */
/* Purpose:   3D-subvolume: regular prisma                                  */
/*                                                                          */
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


void CalculateSubvolPrismaVariables(int sum_subvol_edges,
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
   double pi = 3.141592654;
   double help_subvol_radius_side=0;

   double zentri_angle;                                       /*Zentriwinkel*/
   double circle_chord;                  /*Sehne (engl: chord) eines Kreises*/

   struct point a, b;

   FILE *f9;
   char *file_n9;
   f9 = fopen(file_n9=get_var_char(uvar, "prisma3d_file"), "w");


   /*************************************************************************/
   /* Description of the subvolume as a regulare prisma.                    */
   /* Declare the prisma variables.                                         */
   /*************************************************************************/
   *subvol_radius_bot  = get_var_double(uvar, "subvol_radius_bot");
   subvol_side_length = get_var_double(uvar, "subvol_side_length");

   subvol_bot_midpt->x = get_var_double(uvar, "subvol_bot_midpt.x");
   subvol_bot_midpt->y = get_var_double(uvar, "subvol_bot_midpt.y");
   subvol_bot_midpt->z = get_var_double(uvar, "subvol_bot_midpt.z");

   subvol_top_midpt->x = subvol_bot_midpt->x;
   subvol_top_midpt->y = subvol_bot_midpt->y;
   subvol_top_midpt->z = subvol_bot_midpt->z + subvol_side_length;


   /*************************************************************************/
   /* calculation of the zentri_angle (= Winkel der Kuchenstuecke)          */
   /* and the circle_chord (= Kreis -- Sehne)                               */
   /*************************************************************************/
   zentri_angle = 2*pi / sum_subvol_edges;
   circle_chord = 2* (*subvol_radius_bot) * sin(zentri_angle / 2); 

   
   /*************************************************************************/
   /* calculating the coordinats of the edges of the ground- and top-plane  */
   /* of the prisma                                                         */
   /*************************************************************************/
   for (i=0; i<sum_subvol_edges; i++)
   {
      subvol_bot_pt[i].x = (*subvol_radius_bot) * cos(zentri_angle * (i+0.5)) 
                              + subvol_bot_midpt->x;
      subvol_bot_pt[i].y = (*subvol_radius_bot) * sin(zentri_angle * (i+0.5))
                              + subvol_bot_midpt->y;
      subvol_bot_pt[i].z = subvol_bot_midpt->z;


      subvol_top_pt[i].x = subvol_bot_pt[i].x;
      subvol_top_pt[i].y = subvol_bot_pt[i].y;
      subvol_top_pt[i].z = subvol_top_midpt->z;
   } 


   /*************************************************************************/
   /* Calculate the radius_subvol_side:                                     */
   /*    Sphere radius surrounding the subvol side plane.                   */
   /*************************************************************************/
   help_subvol_radius_side = 0.5 * sqrt(circle_chord * circle_chord
                               + subvol_side_length * subvol_side_length);


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
      
      subvol_side[i].pt[0]     = subvol_bot_pt[i];
      subvol_side[i].pt[1]     = subvol_bot_pt[j];
      subvol_side[i].pt[2]     = subvol_top_pt[j];
      subvol_side[i].pt[3]     = subvol_top_pt[i];

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

      subvol_side[i].length[0] = circle_chord;
      subvol_side[i].length[1] = subvol_side_length;

      /*Not a variable of the "struct fracture subvol_side", 
        just use the loop*/ 
      subvol_radius_side[i] = help_subvol_radius_side;
   }

   /*************************************************************************/
   /* Calculate the middle point of the side plane                          */
   /*************************************************************************/
   for (i=0; i<sum_subvol_edges; i++)
   {
      subvol_side_midpt->x = subvol_side[i].pt[0].x  +
                            (subvol_side[i].pt[1].x-subvol_side[i].pt[0].x)/2; 
      subvol_side_midpt->y = subvol_side[i].pt[0].y  +
                            (subvol_side[i].pt[1].y-subvol_side[i].pt[0].y)/2; 
      subvol_side_midpt->z = subvol_side[i].pt[2].z/2;
   }


   /*************************************************************************/
   /* output of the subvol (tecplot)                                        */
   /*************************************************************************/
   fprintf(f9,"TITLE=\"subvol in 3D volume\" ");
   fprintf(f9,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
   fprintf(f9,"\nZONE N=%d, E=%d, F=FEPOINT, ET=QUADRILATERAL",
               2*sum_subvol_edges, sum_subvol_edges);

   for(i=0; i < sum_subvol_edges; i++)
   {
      fprintf(f9,"\n%8.5f \t %8.5f \t  %8.5f", 
       subvol_bot_pt[i].x, subvol_bot_pt[i].y, subvol_bot_pt[i].z); 
   }
   for(i=0; i < sum_subvol_edges; i++)
   {
      fprintf(f9,"\n%8.5f \t %8.5f \t  %8.5f", 
          subvol_top_pt[i].x, subvol_top_pt[i].y, subvol_top_pt[i].z); 
   }
   for(i=0; i < sum_subvol_edges; i++)
   {
      j = i+1; k = i+2;
      if (j == sum_subvol_edges) k = 1;
      fprintf(f9,"\n%d %d %d %d", 
                  j, k, k+sum_subvol_edges, j+sum_subvol_edges);
   }
   fprintf(f9,"\n");
   printf("\n\t Wrote %s.................. done", file_n9);
   fclose(f9);
   fprintf(stdout,"\n\tWrote %s", file_n9);
}


