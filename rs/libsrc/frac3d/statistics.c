/****************************************************************************/
/*                                                                          */
/* File:      statistics.c                                                  */
/*                                                                          */
/* Purpose:                                                                 */
/*                                                                          */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Uni Stuttgart                                                 */
/*            email: annette.hemminger@iws.uni-stuttgart.de                 */
/*                                                                          */
/*                                                                          */
/* History:   18.05.2001 begin   (AH)                                       */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/* Functions:                                                               */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "functions.h"



/*****************************************************************************/
/* statistics_FracMesh()                                                     */
/*                                                                           */
/*    1.] some interesting values for the mesh generating process            */
/*     a] check the minimum distance between two points VERTEX_net           */
/*     b] check the minimum / maximum length of EDGE_net                     */
/*     c] check the minimum surface of an fracture plane FACE_net            */
/*                                                                           */
/*                                                                           */
/*****************************************************************************/
void statistics_FracMesh()
{
   int i, i1, j, k0, k1;
   int min_pt0, min_pt1;    
            /*point number of the two VERTEX_net points with minimum distance*/

   double ax, ay, az;

   double min_distance_help;

   double min_distance_VERTEX_net;
   double min_distance_EDGE_net;
   double max_distance_EDGE_net;

   FILE *f50;
   char *file_n50;
   f50 = fopen(file_n50=get_var_char(uvar, "log_file"), "a");


   /**************************************************************************/
   /*  set dummy start values                                                */
   /**************************************************************************/
   min_distance_VERTEX_net  = 10e12;  
   min_distance_EDGE_net    = 10e12;  
   max_distance_EDGE_net    = -1.0;  


   /**************************************************************************/
   /*  a] check the minimum distance between two points VERTEX_net           */
   /**************************************************************************/
   for (i=0; i<(nvertex_net-1); i++)
   {
      i1=i+1;

      for (j=i1; j<nvertex_net; j++)
      {
         ax = VERTEX_net[j].pt.x - VERTEX_net[i].pt.x;
         ay = VERTEX_net[j].pt.y - VERTEX_net[i].pt.y;
         az = VERTEX_net[j].pt.z - VERTEX_net[i].pt.z;
      
         min_distance_help = sqrt(ax*ax + ay*ay + az*az);
   
         if (min_distance_help < min_distance_VERTEX_net) {
            min_distance_VERTEX_net = min_distance_help;
            min_pt0 = i;
            min_pt1 = j;
         }
      }
   }

   fprintf(stdout,"\n\n min_distance_VERTEX_net = %e \n", 
           min_distance_VERTEX_net);


   /**************************************************************************/
   /*  b] check the minimum / maximum length of EDGE_net                     */
   /**************************************************************************/
   for (i=0; i<nedge_net; i++)
   {
      k0 = EDGE_net[i].pt0.pt_nr;
      k1 = EDGE_net[i].pt1.pt_nr;

      ax = VERTEX_net[k1].pt.x - VERTEX_net[k0].pt.x;
      ay = VERTEX_net[k1].pt.y - VERTEX_net[k0].pt.y;
      az = VERTEX_net[k1].pt.z - VERTEX_net[k0].pt.z;
   

      min_distance_help = sqrt(ax*ax + ay*ay + az*az);
   
      if (min_distance_help < min_distance_EDGE_net) {
         min_distance_EDGE_net = min_distance_help;
      }
   }

   fprintf(stdout,"\n\n min_distance_EDGE_net = %f \n", 
           min_distance_EDGE_net);


   /**************************************************************************/
   /*  c] check the minimum surface of an fracture plane FACE_net            */
   /*                                                                        */
   /* ---------------------------------------------------------------------- */
   /* see: http://www.exaflop.org/docs/cgafaq/                               */
   /*                                                                        */
   /* Subject 2.01: How do I find the area of a polygon?                     */
   /*   The signed area can be computed in linear time by a simple sum.      */
   /*   The key formula is this:                                             */
   /*                                                                        */
   /*   If the coordinates of vertex v_i are x_i and y_i, twice the signed   */
   /*   area of a polygon is given by                                        */  
   /*                                                                        */
   /*        2 A( P ) = sum_{i=0}^{n-1} (x_i y_{i+1} - y_i x_{i+1}).         */
   /*                                                                        */
   /*   Here n is the number of vertices of the polygon.                     */
   /*                                                                        */
   /*   References: [O' Rourke] pp. 18-27; [Gems II] pp. 5-6: "The Area of   */
   /*   a Simple Polygon", Jon Rokne.                                        */
   /*                                                                        */
   /*   To find the area of a planar polygon not in the x-y plane, use:      */
   /*                                                                        */
   /*        2 A(P) = abs(N . (sum_{i=0}^{n-1} (v_i x v_{i+1})))             */
   /*                                                                        */
   /*   where N is a unit vector normal to the plane. The `.' represents the */
   /*   dot product operator, the `x' represents the cross product operator, */
   /*    and abs() is the absolute value function.                           */
   /*                                                                        */
   /*   [Gems II] pp. 170-171:                                               */
   /*   "Area of Planar Polygons and Volume of Polyhedra", Ronald N. Goldman */
   /*                                                                        */
   /**************************************************************************/
/*
   for (i=0; i<nface_net; i++)
   {
   } 
*/



   /**************************************************************************/
   /* write output to log-file                                               */
   /**************************************************************************/
   fprintf(f50,"\n\n");
   fprintf(f50,"\n******************************************************************************");
   fprintf(f50,"\n*                some interesting statistical values                         *");
   fprintf(f50,"\n******************************************************************************\n");
   fprintf(f50,"\n min_distance_VERTEX_net = %e",min_distance_VERTEX_net);
   fprintf(f50,"\n VERTEX_net[%d](x,y,z): %e  %e  %e ", min_pt0, 
     VERTEX_net[min_pt0].pt.x,VERTEX_net[min_pt0].pt.y,VERTEX_net[min_pt0].pt.z);
   fprintf(f50,"\n VERTEX_net[%d](x,y,z): %e  %e  %e ", min_pt1, 
     VERTEX_net[min_pt1].pt.x,VERTEX_net[min_pt1].pt.y,VERTEX_net[min_pt1].pt.z);

   fprintf(f50,"\n\n min_distance_EDGE_net   = %e \n\n", min_distance_EDGE_net);
   fprintf(f50,"\n");

   fclose(f50);


}


