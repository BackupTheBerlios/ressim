/***************************************************************************/
/*                                                                          */
/* File:      intersection.c                                                */
/*                                                                          */
/* Purpose:                                                                 */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                          */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/* Functions: calculate_delta_cube()                                        */
/*            assign_frac_to_cube()                                         */
/*            approx_radius_sphere()                                        */
/*            distance_midpoint_midpoint()                                  */
/*                                                                          */
/*            intersection_FRAC()                                           */
/*            intersection_TRACE()                                          */ 
/*                                                                          */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "functions.h" 
#include "intersection.h"


/****************************************************************************/
/* intersection_FRAC()                                                      */
/*                                                                          */
/*   PURPOSE  :                                                             */
/*                                                                          */
/*   ARGUMENTS:                                                             */
/*                                                                          */
/*   RETURN   :                                                             */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void intersection_FRAC(struct fracture **FRAC)
{
   int i,j,k;
   int count100 = 100, n100 = 0; 
  
   double radius1, radius2;
   double distance_points; 
  
   struct point sphere_midpt[2];
   struct fracture elem_frac[2];
   struct edge line[2]; 

   /*************************************************************************/
   /*  check it out   :-)                                                   */
   /*************************************************************************/
/**** AH
   calculate_delta_cube(&delta_cube_x, &delta_cube_y, &delta_cube_z,
                        &sum_cube_x, &sum_cube_y, &sum_cube_z);  
***/
  
   /*************************************************************************/
   /* fuer mind. 9 cubes (3*3*3) )                                          */
   /*    1.) Kluftelement i in Abhaengigkeit der Raumlage von               */
   /*        Klufteckpunkte pt[0][i] einem cube zuweisen                    */
   /*************************************************************************/
/*** AH, 05.01.99
      if (sum_cube_x >= 3  &&  sum_cube_y >= 3  &&  sum_cube_z >= 3)
      {
         assign_frac_to_cube(delta_cube_x,delta_cube_y,delta_cube_z,*FRAC);
      }
AH ***/
  
  
   /*************************************************************************/
   /* 1.) Berechnung  radius1 (Kugel um Kluftelement i) und                 */
   /*                 radius2 (Kugel um Kluftelement j)                     */
   /* 2.) Berechnung  Abstand zw. Mittelpkt Kluft i und Mittelpkt Kluft j   */
   /* 3.) Abstand  <=  Radien (1 und 2) --> Schnittpunkte/linien berechnen  */
   /*              >                    --> nicht weiter betrachten         */
   /*************************************************************************/
   edge_nr_3D = 0;
   nvertex_nr = 0;
   n100 = 0;
   for(i=0; i < (nfrac-1); i++)
   {
      if (i == ( (n100+1) * count100))  {
         fprintf(stdout,"\n Intersection lines  ............... %5d. fracture plane", i);
         n100++; 
      }
      radius1 = approx_radius_sphere(i, *FRAC, &sphere_midpt[0]);  
      
      for(j=(i+1); j < nfrac; j++) 
      {
         radius2 = approx_radius_sphere(j, *FRAC, &sphere_midpt[1]);   
         distance_points = abs_vec_pt_pt(&sphere_midpt[0], &sphere_midpt[1]);  
	  
         if ((radius1 + radius2) >= distance_points)  
         /* Schnittpunkt -- Berechnung starten ...........................*/
         {
            elem_frac[0] = (*FRAC)[i];
            elem_frac[1] = (*FRAC)[j];
	      
	      
            if (intersection_nodes_plane_plane(elem_frac,pt_intersect)==1)
            {
               /*************************************************************/
               /* --> Es existieren 2 Schnittpunkte                         */
               /*                                                           */
               /* Sind die ermittelten Schnittpunkte identisch?             */
               /* (d.h. die zwei Ebenen beruehren sich nur an einer Kante)  */
               /* --> Untersuchung des Abstandes zwischen den Schnittpkt    */
               /*     IF--Bedingung: Abstand <= 10e-16 (epsilon_0)          */
               /*     TODO: AH, 18.02.2000 stimmt so nicht mehr!!!          */
               /*                                                           */
               /* # JA: Die zwei Ebenen beruehren sich nur an einer Kante   */
               /*       Es gibt keine Schnittgerade                         */
               /*                                                           */
               /*      Anmerkung AH 20.04.1999                              */
               /*      Wie muss dieser Kantenschnittpunkt fuer die Netzgen. */
               /*      und fuer MUFTE_UG definiert werden?                  */
               /*      1.) Als Punkt im Polygonzug "Kluftelement"?          */
               /*          pt0 - pt1 - Schnittpkt - pt2 -pt3                */
               /*      2.) als singulaerer Schnittpkt in der VERTEX3D Liste?*/
               /*   Loesung 2.) wird im Programm umgesetzt (AH 20.04.1999)  */
               /*                                                           */
               /*                                                           */
               /* # NEIN: Es existieren zwei Schnittpunkte                  */
               /*         Liste mit den Knotenpunkten der Schnittgeraden    */
               /*         anlegen (EDGE3D--Liste)                           */ 
               /*                                                           */
               /*************************************************************/
               if (abs_vec_pt_pt(&pt_intersect[0], &pt_intersect[1]) 
                   <= epsilon_0)
               {
                  s_pt_intersect = pt_intersect[0];

                  /**********************************************************/
                  /* Liste mit singulaeren Schnittpunkten anlegen           */
                  /*   Speicher vom Typ "struct vertex" allokieren          */
                  /**********************************************************/
                  VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, s_pt_intersect);
                  nvertex_nr++;
               }

               else 
               {
                  /**********************************************************/
                  /* Liste mit den Knotenpunkten der Schnittgeraden anlegen */
                  /* Speicher vom Typ "struct edge" allokieren              */
                  /**********************************************************/
                  EDGE3D = add_EDGE3D_to_list(edge_nr_3D, pt_intersect, i, j);

                  /**********************************************************/
                  /* Intersectionslinien miteinander schneiden,             */
                  /*   --> auf singulaere Schnittpunkte untersuchen         */
                  /*       (s_pt_intersect)                                 */
                  /**********************************************************/
                  for (k=0; edge_nr_3D>0 && k<edge_nr_3D; k++)
                  {
                     /*TODO:AH if (1==1): IF--BEDINGUNG NOCH EINFUEGEN !!!*/
                     /* { */
                        line[0] = EDGE3D[edge_nr_3D];
                        line[1] = EDGE3D[k];
                        if (intersection_node_line_line(line,&s_pt_intersect)
                            == 1) 
                        {
 
                           /*************************************************/
                           /* Liste mit singulaeren Schnittpunkten anlegen  */
                           /*************************************************/
                           VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, 
                                                           s_pt_intersect);
                           ++nvertex_nr;
                        }
                  /*}*/
                  }
                  ++edge_nr_3D;
               }
            }
         }
      }
   } 
}


/****************************************************************************/
/* intersection_TRACE()                                                     */
/*                                                                          */
/*   PURPOSE  : calculate the intersection point of two TRACE segments      */
/*                                                                          */
/*   ARGUMENTS:                                                             */
/*                                                                          */
/*   RETURN   :                                                             */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void intersection_TRACE(struct trace **TRACE)
{
   int i, j; 
   int dummy1 = 0, dummy2 = 0;
   struct edge line[2]; 

   nvertex_nr = 0; /*set default value*/
  
   for(i=0; i<(ntrace-1); i++)
   {
      line[0].pt0 = (*TRACE)[i].pt[0];
      line[0].pt1 = (*TRACE)[i].pt[1];

      /*TODO:AH if (1==1): IF--BEDINGUNG NOCH EINFUEGEN !!!*/
      for(j=(i+1); j<ntrace; j++) 
      {
         line[1].pt0 = (*TRACE)[j].pt[0];
         line[1].pt1 = (*TRACE)[j].pt[1];

         if (intersection_node_line_line(line,&s_pt_intersect) == 1) 
         {
            /****************************************************************/
            /* start list 'VERTEX3D' with the intersection points           */
            /****************************************************************/
            VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, s_pt_intersect);

            /****************************************************************/
            /* assign the vertex point with its number to both TRACE lines  */
            /*                                                              */
            /* 1.) initially (in function 'add_TRACE_to_StructTraceList()'), */
            /*     an array of 'initial_nvertex_on_trace' 20 int places was  */
            /*     allocated for 'TRACE[i].vertex_nr[]'. This was done in   */
            /*     order to avoid a lot of memory allocation steps.         */
            /*                                                              */
            /*     default values:                                          */
            /*     TRACE[].vertex_nr[0..initial_nvertex_on_trace-2] == -999  */
            /*     TRACE[].vertex_nr[initial_nvertex_on_trace-1]    == -1    */
            /*                                                              */
            /*     If one reaches the last available place with             */
            /*     TRACE[i].vertex_nr[] == -1                               */
            /*     --> additional memory has to be reallocated!             */
            /*                                                              */
            /*                                                              */
            /* 2.) within the function 'PreNetgenOutput_TRACE()' the TRACEs */
            /*     will be split in single line segments. e.g.              */
            /*     trace start pt - vertex pt - vertex pt - trace end pt    */
            /*                                                              */
            /****************************************************************/
            /* assign VERTEX3D number 'nvertex_nr' to TRACE[i].vertex_on[]  */
            /****************************************************************/
            
            if (  (Point1_equal_Point2((*TRACE)[i].pt[0], VERTEX3D[nvertex_nr].pt) 
                   != 1)
                && (Point1_equal_Point2((*TRACE)[i].pt[1], VERTEX3D[nvertex_nr].pt) 
                   != 1))
            { 

               dummy1 = (*TRACE)[i].nvertex_on;
               if ((*TRACE)[i].vertex_on[dummy1] != -1)
               {
                  (*TRACE)[i].vertex_on[dummy1] = nvertex_nr;
               }
               else 
               {
                  (*TRACE)[i].vertex_on = 
                     realloc((*TRACE)[i].vertex_on,((*TRACE)[i].nvertex_on+2)*sizeof(int));
                  if ((*TRACE)[i].vertex_on == NULL)
                  {
                     fprintf(stdout,"\nMemory allocation failed: (*TRACE)[%d].vertex_on\n",i);
                     exit (-1);
                  }
   
                  (*TRACE)[i].vertex_on[dummy1] = nvertex_nr;
                  (*TRACE)[i].vertex_on[dummy1+1] = -1;
               }
               (*TRACE)[i].nvertex_on++;
            }


            /****************************************************************/
            /* assign VERTEX3D number 'nvertex_nr' to TRACE[j]              */
            /****************************************************************/
            if (  (Point1_equal_Point2((*TRACE)[j].pt[0], VERTEX3D[nvertex_nr].pt) 
                   != 1)
                && (Point1_equal_Point2((*TRACE)[j].pt[1], VERTEX3D[nvertex_nr].pt) 
                   != 1))
            { 
               dummy2 = (*TRACE)[j].nvertex_on;
               if ((*TRACE)[j].vertex_on[dummy2] != -1)
               {
                  (*TRACE)[j].vertex_on[dummy2] = nvertex_nr;
               }
               else 
               {
                  (*TRACE)[j].vertex_on = 
                     realloc((*TRACE)[j].vertex_on,((*TRACE)[j].nvertex_on+2)*sizeof(int));
                  if ((*TRACE)[j].vertex_on == NULL)
                  {
                     fprintf(stdout,"\nMemory allocation failed: (*TRACE)[%d].vertex_on\n",j);
                     exit (-1);
                  }

                  (*TRACE)[j].vertex_on[dummy2] = nvertex_nr;
                  (*TRACE)[j].vertex_on[dummy2+1] = -1;
               }
               (*TRACE)[j].nvertex_on++;
            }
            ++nvertex_nr;
         }
      }
   }
}


/****************************************************************************/
/*                                                                          */
/*                             FUNCTIONS                                    */
/*                                                                          */
/****************************************************************************/
/* calculate_delta_cube()                                                   */
/*                                                                          */
/*    die Seitenlaengen (in xyz-Richtung) des cubes werden berechnet        */
/*       1.) 1. Naehrung: delta_cube_* = 3* mittlere Spurlaenge             */
/*           (fuer x, y, z gleich)                                          */
/*       2.) Anzahl (int-Groesse)  der cubes in xyz-Richtung berechnen      */
/*       3.) delta_cube aufgrund der Anzahl der cubes neu berechnen         */
/*                                                                          */
/****************************************************************************/
void calculate_delta_cube(double *delta_cube_x, double *delta_cube_y, 
                          double *delta_cube_z, int *sum_cube_x, 
                          int *sum_cube_y, int *sum_cube_z)
{
   double mean_trace_length = 0.28 ; 	       /*AH  mittlere Spurlaenge  AH*/
   
   *delta_cube_x = *delta_cube_y = *delta_cube_z = 3.0 * mean_trace_length;

   *sum_cube_x   = (int) ((dom_max.x - dom_min.x) / *delta_cube_x);
   *sum_cube_y   = (int) ((dom_max.y - dom_min.y) / *delta_cube_y);
   *sum_cube_z   = (int) ((dom_max.z - dom_min.z) / *delta_cube_z);

   *delta_cube_x = (dom_max.x - dom_min.x) / *sum_cube_x;   
   *delta_cube_y = (dom_max.y - dom_min.y) / *sum_cube_y;   
   *delta_cube_z = (dom_max.z - dom_min.z) / *sum_cube_z;   

}



/****************************************************************************/
/* assign_frac_to_cube()                                                    */
/*                                                                          */
/*    Entsprechend der Raumlage der Kluftelemente (dem Eckpunkt pt[0][i])   */
/*    werden den cubes, entsprechend ihrer Raumlage, die Kluefte            */
/*    (Zeiger auf die Kluefte) zugeordnet                                   */
/*                                                                          */
/*    Anmerkung: alle Kluefte werden dadurch erfasst, denn pt[0][i] liegt   */
/*               immer innerhalb des Untersuchungsgebietes (gleicherveilt)  */
/*               weitere Eckpunkte koennen natuerlich ueber das UGeb        */
/*               hinausragen                                                */
/****************************************************************************/
/* AH 02.02.99
void assign_frac_to_cube(double delta_cube_x, 
                         double delta_cube_y, 
                         double delta_cube_z,
                         struct fracture *FRAC)
{
   int i; 
   int cx, cy, cz; 
   int l=0;                          T* Summenzaehler fuer Kluefte pro cube *T 

   printf("\nTEST cube_assign_frac : delta_cube_x = %f \n", delta_cube_x); 
   printf("\n                        delta_cube_y = %f \n", delta_cube_y); 
   printf("\n                        delta_cube_z = %f \n", delta_cube_z); 

   for(i=0; i<nfrac; i++)
   {
     cx = (int) (FRAC[i].pt[0].x / delta_cube_x); T* cube - Nummer bestimmen *T
     cy = (int) (FRAC[i].pt[0].y / delta_cube_y);
     cz = (int) (FRAC[i].pt[0].z / delta_cube_z);

     l = cube[cx][cy][cz].sum = cube[cx][cy][cz].sum++ ; 
     T* Kluftelement i cube zuweisen   *T
     T* !!!   HIER NOCH AENDERN  !!! AH 02.02.99 *T
     cube[cx][cy][cz].node[l] = &frac[i].pt[0];
     T* !!!   HIER NOCH AENDERN  !!! AH 02.02.99 *T

     printf("\n cube[%d][%d][%d].node[%d] = %f \t %f \t %f ", 
            cx, cy, cz, l, *cube[cx][cy][cz].node[l]);
     printf("\n cube[%d][%d][%d].node[%d] = %f \t %f \t %f \n", 
            cx, cy, cz, l, pt[0][i]);
   }
}
AH 02.02.99 */


/****************************************************************************/
/* approx_radius_sphere()                                                   */
/*    Purpose : calculate the spherical radius around the FRAC[k]           */
/*    Rerturn : the spherical radius 'radius'                               */
/****************************************************************************/
double approx_radius_sphere(int k, struct fracture *FRAC, 
                            struct point *sphere_midpt)
{
   double radius, check_radius;
   struct point sphere;

   sphere = *sphere_midpt;

   if (FRAC[k].diagonal[0] >= FRAC[k].diagonal[1])
   {
      sphere.x = FRAC[k].pt[0].x + 0.5*(FRAC[k].pt[2].x-FRAC[k].pt[0].x); 
      sphere.y = FRAC[k].pt[0].y + 0.5*(FRAC[k].pt[2].y-FRAC[k].pt[0].y); 
      sphere.z = FRAC[k].pt[0].z + 0.5*(FRAC[k].pt[2].z-FRAC[k].pt[0].z); 
 
      radius = 0.5 * FRAC[k].diagonal[0];

      check_radius = abs_vec_pt_pt(&sphere, &FRAC[k].pt[1]); 
      if (check_radius > radius)  radius = check_radius;

      check_radius = abs_vec_pt_pt(&sphere, &FRAC[k].pt[3]); 
      if (check_radius > radius)  radius = check_radius;
   }
   else 
   {
      sphere.x = FRAC[k].pt[1].x + 0.5*(FRAC[k].pt[3].x-FRAC[k].pt[1].x); 
      sphere.y = FRAC[k].pt[1].y + 0.5*(FRAC[k].pt[3].y-FRAC[k].pt[1].y); 
      sphere.z = FRAC[k].pt[1].z + 0.5*(FRAC[k].pt[3].z-FRAC[k].pt[1].z); 
 
      radius = 0.5 * FRAC[k].diagonal[1];

      check_radius = abs_vec_pt_pt(&sphere, &FRAC[k].pt[0]); 
      if (check_radius > radius)  radius = check_radius;

      check_radius = abs_vec_pt_pt(&sphere, &FRAC[k].pt[2]); 
      if (check_radius > radius)  radius = check_radius;
   }
   *sphere_midpt = sphere;

   return(radius);
}


/****************************************************************************/
/* radius_sphere()                                                          */
/*   calculate the radius of a sphere                                       */
/****************************************************************************/
double radius_sphere(int k, struct fracture *FRAC)
{
   double radius;
   radius = 0.5 * sqrt(  FRAC[k].length[0] * FRAC[k].length[0]
                       + FRAC[k].length[1] * FRAC[k].length[1]);
   return(radius);
}



/****************************************************************************/
/* distance_midpoint_midpoint()                                             */
/*   calculate the distance between the two midpoints on the 'line segments'*/
/*   which are defined by two corner points                                 */
/*   line segment i:   pt_i0 <--> pt_i2                                     */
/*   line segment j:   pt_j0 <--> pt_j2                                     */
/*                                                                          */
/* return value: the distance                                               */
/****************************************************************************/
double distance_midpoint_midpoint(struct point pt_i0, struct point pt_i2,
                            struct point pt_j0, struct point pt_j2)
{
   struct point mid_pt_i, mid_pt_j;
   double help_x, help_y, help_z;
   double help_distance;

   mid_pt_i.x = 0.5 * pt_i0.x + pt_i2.x;
   mid_pt_i.y = 0.5 * pt_i0.y + pt_i2.y;
   mid_pt_i.z = 0.5 * pt_i0.z + pt_i2.z;
          
   mid_pt_j.x = 0.5 * pt_j0.x + pt_j2.x;
   mid_pt_j.y = 0.5 * pt_j0.y + pt_j2.y;
   mid_pt_j.z = 0.5 * pt_j0.z + pt_j2.z;

   help_x = mid_pt_j.x - mid_pt_i.x;
   help_y = mid_pt_j.y - mid_pt_i.y;
   help_z = mid_pt_j.z - mid_pt_i.z;
          
   help_distance = sqrt( (help_x * help_x) 
                        +(help_y * help_y) 
                        +(help_z * help_z)); 

   return(help_distance);
}


