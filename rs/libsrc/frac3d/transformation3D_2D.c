/*****************************************************************************/
/*                                                                           */
/* File:      transformation3D_2D.c                                          */
/*                                                                           */
/* Purpose:   Read in a set of points (x,y,z). All the points have to lay    */
/*            in a plane (plane in 3d space).                                */
/*            Do a transformation and a rotation of the coordinate system    */
/*            --> finally, the points (with their transformed coordinates)   */
/*                lay within a 2d plane (local coordinate system).           */
/*                                                                           */
/*            Why doing this: the netgenerator ART can not handle a plane,   */
/*            which should be discretized with 2d elements in 3d space.      */
/*            Here, we are not interessted in a 3d discretization.           */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                  */
/*                                                                           */
/*****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "functions.h"
#include "transformation3D_2D.h"

#define SWAP(a,b) {double temp=(a); (a)=(b); (b)=temp;}

void transform_coord_3D_2D(int nvertex_net, struct vertex_net VERTEX_net[])
{

   int i;
   int n_gj, m_gj;                    /*arguments of the fct. 'GaussJordan()'*/
   double ROT[3][3];                  /* rotation matrix: spherical rotation 
                                            of a cartesian coordinate system */
   double B_GJ[3][3];                 /*arguments of the fct. 'GaussJordan()'*/

   struct point a, b, c, n, origin_K1, hpt;  


   /**************************************************************************/
   /* Serach for the corner nodes of the quadrilateral subplane3D            */
   /* --> VERTEX_net[0] ... VERTEX_net[3] are the corner nodes               */
   /*                                                                        */
   /* Calculate the norm vector (vector product)                             */
   /*     n = (VERTEX_net[1]-VERTEX_net[0]) x (VERTEX_net[3]-VERTEX_net[0])  */
   /*     n = (             a             ) x (             b             )  */
   /*                                                                        */
   /* The axis (x1, y1, z1) of the new cartesian coordinate system K_1       */
   /*    x1 = a                                                              */
   /*    y1 = ... vector product  y1 = (n) x (a)                             */
   /*    z1 = n                                                              */
   /*                                                                        */
   /* What about vector b? Vector a and b are not directily othogonal!       */
   /*                                                                        */
   /**************************************************************************/
   a.x = VERTEX_net[1].pt.x - VERTEX_net[0].pt.x;
   a.y = VERTEX_net[1].pt.y - VERTEX_net[0].pt.y;
   a.z = VERTEX_net[1].pt.z - VERTEX_net[0].pt.z;

   b.x = VERTEX_net[3].pt.x - VERTEX_net[0].pt.x;
   b.y = VERTEX_net[3].pt.y - VERTEX_net[0].pt.y;
   b.z = VERTEX_net[3].pt.z - VERTEX_net[0].pt.z;

   n.x =  a.y*b.z - a.z*b.y;
   n.y = -a.x*b.z + a.z*b.x;
   n.z =  a.x*b.y - a.y*b.x;

   c.x =  n.y*a.z - n.z*a.y;
   c.y = -n.x*a.z + n.z*a.x;
   c.z =  n.x*a.y - n.y*a.x;


/********
   c.x =  a.y*n.z - a.z*n.y;
   c.y = -a.x*n.z + a.z*n.x;
   c.z =  a.x*n.y - a.y*n.x;
*********/


   /**************************************************************************/
   /* Transformation: the origin of the old coordinates system K^0 is        */
   /*     transformed to the point  'origin_K1'.                             */
   /*     --> point 'origin_K1' = origin of the coordinate system K^1        */
   /*                                                                        */
   /*                             v.x     VERTEX_net[0].pt.x    origin_K1.x  */
   /* transformation vector  v =  v.y  =  VERTEX_net[0].pt.y =  origin_K1.y  */
   /*                             v.z     VERTEX_net[0].pt.z    origin_K1.z  */
   /*                                                                        */
   /* transformation      node[].x_neu = node[].x_alt - v.x                  */
   /*   (in general)      node[].y_neu = node[].y_alt - v.y                  */
   /*                     node[].z_neu = node[].z_alt - v.z                  */
   /*                                                                        */
   /**************************************************************************/
   origin_K1 = VERTEX_net[0].pt;
   for (i=0; i<nvertex_net; i++)
   {
      VERTEX_net[i].pt.x = VERTEX_net[i].pt.x - origin_K1.x;
      VERTEX_net[i].pt.y = VERTEX_net[i].pt.y - origin_K1.y;
      VERTEX_net[i].pt.z = VERTEX_net[i].pt.z - origin_K1.z;
   }

   origin_K1.x = 0.0;     /*new coordinate values for the origin point of K^1*/
   origin_K1.y = 0.0; 
   origin_K1.z = 0.0;


   /**************************************************************************/
   /* Rotation: cartesian coordinate system K^1 is rotated into the new      */
   /*           cartesian coordinate system K^2.                             */
   /*                                                                        */
   /*           Rotation of the point node[] around the origin 'origin_K1'   */
   /*           with the inverse matrix of the rotation matrix ROT[][].      */
   /*                                                                        */
   /* 1.)   Calculation of the rotation matrix ROT[][]                       */
   /*                                                                        */
   /* Comment to the calculation of the rotation matrix:                     */
   /* Calculate the spherical rotation matrix of a cartesian coordinate      */
   /* system around its origin. Therefor, nine angles has to be calculated.  */
   /* (see: Bronstein, p. 217)                                               */
   /*                                                                        */
   /* 2.)   Calculation of the inverse rotation matrix ROT[][]               */
   /*       Applying the  Gauss-Jordan elimination method.                   */
   /*       --> Hereby, the matrix ROT[][] is replaced by its inverse matrix */
   /*                                                                        */
   /* 3.)   Rotation of all points node[]                                     */
   /*                                                                        */
   /**************************************************************************/
   RotationMatrix3D(origin_K1, a, c, n, ROT); 

   n_gj = m_gj = 3;
   GaussJordan(ROT, B_GJ, n_gj, m_gj);      /*Gauss-Jordan elimination method*/

   for (i=0; i<nvertex_net; i++)
   {
      hpt = VERTEX_net[i].pt;

      VERTEX_net[i].pt.x = hpt.x*ROT[0][0] + hpt.y*ROT[0][1] + hpt.z*ROT[0][2];
      VERTEX_net[i].pt.y = hpt.x*ROT[1][0] + hpt.y*ROT[1][1] + hpt.z*ROT[1][2];
      VERTEX_net[i].pt.z = hpt.x*ROT[2][0] + hpt.y*ROT[2][1] + hpt.z*ROT[2][2];
   }
}


