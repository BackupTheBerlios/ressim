/****************************************************************************/
/*                                                                          */
/* File:      subplane3D.c                                                  */
/*                                                                          */
/* Purpose:                                                                 */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/* Functions: int gen_static_subplane3D_list()                              */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <math.h>
#include "functions.h" 
#include "subplane3D.h"         
#include "intersection.h"


void subplane_3D_intersection_FRAC(struct fracture **FRAC)
{
   int i, j, k, l;
   int trace_nr=0;
   int found_nsubplane3D;                                     /*for controll*/

   int file_number = 0;    /*number of file for the output of the subplanes */
   int free_EDGE2D   = 0;           /*help variable: >0 --> get memory back */
   int free_VERTEX3D = 0;           /*help variable: >0 --> get memory back */

   double trace_length;
   double trace_aperture;
   double radius1, radius2, distance_points; 

   struct point a, b; 
   struct point sphere_midpt1, sphere_midpt2;

   struct fracture elem_frac[2];    /* required for the function arguments  */
                                    /* of 'intersection_nodes_plane_plane()'*/
   struct edge line[2];             /* required for the function arguments  */

   FILE *filedat = NULL; 
   char *Fname = get_var_char(uvar, "name_Subplane_file");

   /*************************************************************************/
   /* allocate memory for the list struct fracture 'subplane3D[]'           */
   /*************************************************************************/
   Subplane3D = (struct fracture *) malloc(sum_subplane3D * sizeof(struct fracture)); 
   if (Subplane3D == NULL)
   {
      fprintf(stderr,"Memory allocation failed: Subplane3D[] \n");
      exit (-1);
   }


   /*************************************************************************/
   /* open file with the data information of the corner points              */
   /*                                                                       */
   /* read in the 4 corner point data of the 'sum_subplane3D'               */
   /*    --> the four corner nodes has to lay in a plane                    */
   /*************************************************************************/
   while(1)
   {
      fprintf(stdout,"\nOpening Subplanefile [ %s ]", Fname);
      if ((filedat = fopen(Fname,"r")) == NULL)
      {
         printf("\nERROR Inputfile does not exists!\nEXIT PROGRAM\n");
         exit(-1);
      }
      else
      {
         printf("\nReading File: %s\n", Fname);
         found_nsubplane3D = gen_static_subplane3D_list(filedat, Subplane3D); 
         break;
      }
   }
   fclose(filedat);

   /*Controll*/
   if (found_nsubplane3D != sum_subplane3D)
   {
      fprintf(stdout,"\nEXIT because of ... ");
      fprintf(stdout,"\n   Number of Subplane3D declared in the Inputfile sum_subplane3D = %d ", sum_subplane3D);
      fprintf(stdout,"\n   Number of Subplane3D found in %s:  found_nsubplane3D = %d ", Fname, found_nsubplane3D);
      fprintf(stdout,"\n   sum_subplane3D != found_nsubplane3D  -->  program stops \n\n");
      exit(-1);
   }

   /**************************************************************************/
   /* 1.) Assign the 'useful' informations to the variables of the           */
   /*     struct fracture subplane3D[]                                       */
   /*     Some of them are dummy values. Be careful when you use them!!!     */
   /*                                                                        */
   /* 2.) Check, if the 4 points lay in a plane                              */
   /*     -->  TODO                                                          */
   /*                                                                        */
   /**************************************************************************/
   for (i=0; i<sum_subplane3D; i++)
   {
      Subplane3D[i].frac_nr = i;

      a.x = Subplane3D[i].pt[1].x - Subplane3D[i].pt[0].x;
      a.y = Subplane3D[i].pt[1].y - Subplane3D[i].pt[0].y;
      a.z = Subplane3D[i].pt[1].z - Subplane3D[i].pt[0].z;

      b.x = Subplane3D[i].pt[3].x - Subplane3D[i].pt[0].x;
      b.y = Subplane3D[i].pt[3].y - Subplane3D[i].pt[0].y;
      b.z = Subplane3D[i].pt[3].z - Subplane3D[i].pt[0].z;

      Subplane3D[i].norm.x =  a.y*b.z - a.z*b.y;
      Subplane3D[i].norm.y = -a.x*b.z + a.z*b.x;
      Subplane3D[i].norm.z =  a.x*b.y - a.y*b.x;

      Subplane3D[i].length[0] = sqrt(pow(a.x,2) + pow(a.y,2) + pow(a.z,2));
      Subplane3D[i].length[1] = sqrt(pow(b.x,2) + pow(b.y,2) + pow(b.z,2));
   
      Subplane3D[i].inside_subd3D = 0;    /* dummy values ! */
      Subplane3D[i].index_ch      = 0;    /* dummy values ! */
      Subplane3D[i].sum_ptch      = 0;    /* dummy values ! */

   }

   

   /*************************************************************************/
   /* Calulate the intersection lines of the subplane3D[] with the FRAC[]   */
   /*                                                                       */
   /* 1.) Calculate the spherical radius around subplane3D[] and the        */
   /*     radius around FRAC[]                                              */
   /* 2.) Calculate distance between the two midpoints:                     */
   /*                          midpoint subplane[] <-> midpoint FRAC[]      */
   /* 3.) if   : distance <= (radius1 + radius2)                            */
   /*            --> start calulation of the intersection lines             */
   /*     else : --> the two planes do not touch each other                 */
   /*                                                                       */
   /* TODO AH 11.08.                                                        */
   /* fuer 'sum_subplane3D > 1' gibt es noch einen Speicherzugriffsfehler   */
   /* NOCH AENDERN !!!!                                                     */
   /*                                                                       */
   /*************************************************************************/
   for(i=0; i<sum_subplane3D; i++)              /*loop over all subplane3D[]*/
   {
      trace_nr   = 0;    /*start value*/
      nvertex_nr = 0;    /*start value*/


      /**********************************************************************/
      /* Variable 'struct fracture Subplane3D[]' is rewritten as            */
      /* a variable of the type 'struct trace subpl_trace3D[]'              */
      /* --> so we can use the function 'PreNetgenOutput_TRACE()', which    */
      /*     deals with the type 'struct trace ...'                         */
      /*                                                                    */
      /* --> for some operations, variable 'struct fracture Subplane3D[]'   */
      /*     has to be used, and for some 'struct trace subpl_trace3D[]'.   */
      /*     Sorry for this confusion! ;-)                                  */
      /*                                                                    */
      /**********************************************************************/
      AssignSubplane3D_to_subpl_trace3D(i, Subplane3D, subpl_trace3D);


      /**********************************************************************/
      /* start calculation of intersection lines                            */
      /**********************************************************************/
      radius1 = approx_radius_sphere(i, Subplane3D, &sphere_midpt1);
      for(j=0; j < nfrac; j++)
      {
         radius2 = approx_radius_sphere(j, *FRAC, &sphere_midpt2);
         distance_points =  abs_vec_pt_pt(&sphere_midpt1, &sphere_midpt2);

         if ((radius1 + radius2) >= distance_points)
         {
            /* start calulation of the intersection lines */
            elem_frac[0] = Subplane3D[i];
            elem_frac[1] = (*FRAC)[j];


            if (intersection_nodes_plane_plane(elem_frac,pt_intersect)==1)
            {
               /*************************************************************/
               /* --> there exists two intersection points                  */
               /*                                                           */
               /* Are the two intersection points equal?                    */
               /* (that means: the two planes touch each other just an the  */
               /*  edge lines)                                              */
               /* Check the distance (=trace_length) between the two        */
               /* intersection points.                                      */ 
               /*                                                           */
               /* Version vor 06.02.2001 (alt!)                             */
               /*    if (trace_length  <= 10e-16 (epsilon_0))               */
               /*                                                           */
               /* Version 06.02.2001                                        */
               /* if (trace_length <= epsilon_length) (value in input file) */
               /*                                                           */
               /* YES: The two planes touch each other in one point or      */
               /*      they lay to close to each other. This is a problem   */
               /*      for the mesh generating program!                     */
               /*      Here, the single intersection point is not important */
               /*      --> No intersection line                             */
               /*                                                           */
               /* NO : There are two different intersection points and the  */
               /*      distance between the two points is large enough.     */
               /*      Assign the two points to the list 'EDGE2D'           */
               /*                                                           */
               /*************************************************************/
               epsilon_length = get_var_double(uvar, "epsilon_length");


               trace_length = abs_vec_pt_pt(&pt_intersect[0], &pt_intersect[1]);
               if (trace_length > epsilon_length)
               {
                  /**********************************************************/
                  /* Check,if the pt_intersect[] points lay on the          */
                  /* subpl_trace3D[](apply fct 'abs_distance_point_line()'  */
                  /*                                                        */
                  /* point on trace:                                        */
                  /*    Check, if the pt_intersect[] are not equal to       */
                  /*    the corner points of the subpl_trace3D[]            */
                  /*    (apply function 'Point1_equal_Point2()'             */
                  /*    Not equal: --> subpl_trace3D[i].nvertex_on++        */
                  /*      mesh generating process requires: split up the    */
                  /*      subpl_trace3D[] (done in 'PreNetgenOutput_TRACE()'*/
                  /*    Equal: no additional point on the subpl_trace3D[]   */
                  /*                                                        */
                  /* point not on trace: here, nothing more has to be done  */
                  /*                                                        */
                  /**********************************************************/
                  for (k=0; k<4; k++)  /* loop over the four subpl_trace3D[]*/
                  {
                     for (l=0; l<2; l++)  /*loop over the two pt_intersect[]*/
                     {
                        if(epsilon_0 >= abs_distance_point_line(
                                                 &subpl_trace3D[k].pt[0],
                                                 &subpl_trace3D[k].pt[1],
                                                 &pt_intersect[l]))
                        {
                           if (  (Point1_equal_Point2(subpl_trace3D[k].pt[0], 
                                                      pt_intersect[l]) != 1)
                               &&(Point1_equal_Point2(subpl_trace3D[k].pt[1], 
                                                      pt_intersect[l]) != 1))
                           {

                              VERTEX3D = add_VERTEX3D_to_list(nvertex_nr,
                                                              pt_intersect[l]);
                              VERTEX3D[nvertex_nr].inside_subd3D = 1; 
                              free_VERTEX3D++;
 
                              AssignVertexToSubplaneTrace3D(k, subpl_trace3D, 
                                                            &nvertex_nr);
                              /*AH,06.02.2001: Test*/
                              nvertex_nr++;
                           }
                        }
                     }
                  }
 

                  /**********************************************************/
                  /* Assign the calculated intersection lines to 'EDGE2D[]' */
                  /**********************************************************/
                  trace_aperture = (*FRAC)[j].frac_aperture;

                  EDGE2D = add_EDGE2D_to_StructTraceList(trace_nr, 
                                                         trace_length, 
                                                         pt_intersect,
                                                         trace_aperture, 
                                                         &trace_nr); /*incrementation*/
                  EDGE2D[trace_nr-1].inside_subd3D = 1; 
                  free_EDGE2D++;


                  /**********************************************************/
                  /* Check, if the intersection lines cut each other        */
                  /* YES: single intersection point 's_pt_intersect'        */
                  /**********************************************************/
                  for (k=0; trace_nr>1 && k<(trace_nr-1); k++)
                  {
                     /*TODO:AH if (1==1): IF--BEDINGUNG NOCH EINFUEGEN !!!*/
                     line[0].pt0 = EDGE2D[trace_nr-1].pt[0];
                     line[0].pt1 = EDGE2D[trace_nr-1].pt[1];
                     line[1].pt0 = EDGE2D[k].pt[0];
                     line[1].pt1 = EDGE2D[k].pt[1];
                     if (intersection_node_line_line(line,&s_pt_intersect)
                         == 1)
                     {
                        /****************************************************/
                        /* Assign the single intersection pt to 'VERTEX3D'  */ 
                        /****************************************************/
                        VERTEX3D = add_VERTEX3D_to_list(nvertex_nr,s_pt_intersect);
                        VERTEX3D[nvertex_nr].inside_subd3D = 1; 
                        free_VERTEX3D++;

                        AssignVertexToEDGE2D(trace_nr-1, EDGE2D);
                        AssignVertexToEDGE2D(k, EDGE2D);
                        nvertex_nr++;
                     }
                  }
               }
            }
         }
      } /*loop over the FRAC[] elements*/


      /*******************************************************************/
      /* Kluftspuren in Liniensegmente (1D Elemente) aufspalten          */
      /* (Schnittpkt zu Schnittpkt)                                      */
      /*******************************************************************/


      /*******************************************************************/
      /* Backbone bestimmen                                              */
      /*******************************************************************/


      /*******************************************************************/
      /* Durchflusswirksame Liniensegmente bestimmen                     */
      /*******************************************************************/


      /*******************************************************************/
      /* Data structure for mesh generation                              */
      /*                                                                 */
      /*    Provide data structure for the mesh generating program       */
      /*    Reduce the whole point, trace, fracture ... information to   */
      /*    --> vertex, edge, face, element                              */
      /*                                                                    */
      /* Write output files:  for tecplot (graphik)                         */
      /*                      for ART     (mesh generator)                  */
      /*                                                                    */
      /*                                                                    */
      /* coordinate transformation                                          */
      /*  ART can not handle a 2D plane with 1D elements in the 3D space!!! */
      /*  So the points has to be transformed in 2D.                        */
      /*                                                                    */
      /**********************************************************************/
/***
      DataStructure2D_for_MeshGenerator_ART(nvertex_nr, VERTEX3D,
                                            trace_nr, EDGE2D,
                                            Subplane3D, subpl_trace3D,
                                            file_number);
      file_number++;
**/

      PreNetgenOutput_1dTRACE_in2d(nvertex_nr, VERTEX3D,
                                   trace_nr, EDGE2D,
                                   i, Subplane3D, 
                                   subpl_trace3D,
                                   file_number);
      file_number++;


      /**********************************************************************/
      /* give memory back                                                   */
      /**********************************************************************/
      if (i== (sum_subplane3D-1)) free (Subplane3D);      /*give memory back*/
      if (free_EDGE2D   > 0)      free (EDGE2D);          /*give memory back*/
      if (free_VERTEX3D > 0)      free (VERTEX3D);        /*give memory back*/

      nvertex_net = 0;                                      /*set value back*/
      nedge_net = 0;                                        /*set value back*/
      free_EDGE2D   = 0;                                    /*set value back*/
      free_VERTEX3D = 0;                                    /*set value back*/

   } /*loop over the Subplane3D[] elements*/

}



/****************************************************************************/
/* void subplane_3D_intersection_TRACE()                                    */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void subplane_3D_intersection_TRACE()
{
   int i, j, k, l, m, m1;
   int trace_nr=0;
   int found_nsubplane3D;                                     /*for controll*/

   int file_number = 0;    /*number of file for the output of the subplanes */
   int free_EDGE2D   = 0;           /*help variable: >0 --> get memory back */
   int free_VERTEX3D = 0;           /*help variable: >0 --> get memory back */

   int help_inside[2];

   double distance[2];
   double length_norm;
   double trace_length;   
   double trace_aperture;

   struct point norm_0; 
   struct point help_node[4];
   struct point pt_intersect[2];
   struct point s_pt_intersect;

   struct edge line[2];      /* for 'intersection_node_line_line()' */

   FILE *filedat = NULL; 
   char *Fname = get_var_char(uvar, "name_Subplane_file");


   /*************************************************************************/
   /* allocate memory for the list struct fracture 'subplane3D[]'           */
   /*************************************************************************/
   Subplane3D = (struct fracture *) malloc(sum_subplane3D * sizeof(struct fracture)); 
   if (Subplane3D == NULL)
   {
      fprintf(stderr,"Memory allocation failed: Subplane3D[] \n");
      exit (-1);
   }


   /*************************************************************************/
   /* open file with the data information of the corner points              */
   /*                                                                       */
   /* read in the 4 corner point data of the 'sum_subplane3D'               */
   /*    --> the four corner nodes has to lay in a plane                    */
   /*************************************************************************/
   while(1)
   {
      fprintf(stdout,"\nOpening Subplanefile [ %s ]", Fname);
      if ((filedat = fopen(Fname,"r")) == NULL)
      {
         printf("\nERROR Inputfile does not exists!\nEXIT PROGRAM\n");
         exit(-1);
      }
      else
      {
         printf("\nReading File: %s\n", Fname);
         found_nsubplane3D = gen_static_subplane3D_list(filedat, Subplane3D); 
         break;
      }
   }
   fclose(filedat);

   /*Controll*/
   if (found_nsubplane3D != sum_subplane3D)
   {
      fprintf(stdout,"\nEXIT because of ... ");
      fprintf(stdout,"\n   Number of Subplane3D declared in the Inputfile sum_subplane3D = %d ", sum_subplane3D);
      fprintf(stdout,"\n   Number of Subplane3D found in %s:  found_nsubplane3D = %d ", Fname, found_nsubplane3D);
      fprintf(stdout,"\n   sum_subplane3D != found_nsubplane3D  -->  program stops \n\n");
      exit(-1);
   }

   /**************************************************************************/
   /* 1.) Assign the 'useful' informations to the variables of the           */
   /*     struct fracture subplane3D[]                                       */
   /*     Some of them are dummy values. Be careful when you use them!!!     */
   /*                                                                        */
   /* 2.) TODO: Check, if the 4 points lay in a plane                        */
   /*                                                                        */
   /**************************************************************************/
   for (i=0; i<sum_subplane3D; i++)
   {
      AssignSubplane3DValues(i, Subplane3D);
   }

   

   /*************************************************************************/
   /* Determine if the TRACE[] lays within the Subplane3D[]                 */
   /*                                                                       */
   /* 1.) Calculate the standardized normal vector 'norm_0' of Subplane3D[] */
   /*     --> the plane Subplane3D[] is now defined by the 'Hessesche       */
   /*         Normalform'                                                   */
   /*                                                                       */
   /* 2.) The orientated distance 'distance' between the point TRACE[].pt[] */
   /*     and the plane Subplane3D[] which is written in the                */
   /*     'Hessesche Normalform', is defined by the equation                */
   /*                                                                       */
   /*   distance[] = vec(norm_0) * (vec(TRACE[].pt) - vec(Subplane3D[].pt)) */
   /*                                                                       */
   /*     with                                                              */
   /*     distance[0],   distance[1]  : distance                            */
   /*     TRACE[].pt[0], TRACE[].pt[1]: two trace points                    */
   /*     Subplane3D[].pt : one point of the plane Subplane3D[]             */
   /*     norm_0          : standardized normal vector of the Subplane3D[]  */
   /*                                                                       */
   /*                                                                       */
   /* 3.) if (distance[0] <= epsilon_dist) && (distance[1] <= epsilon_dist) */
   /*          --> TRACE lays within the plane Subplane3D[]                 */
   /*                calulation of the intersection point                   */
   /*     3.1) Does the two trace points lay inside or outside the plane?   */
   /*          if inside  --> nothing more to do                            */
   /*          if outside --> calculate the single intersection points      */
   /*                                                                       */
   /*     else --> TRACE does not lay within the plane Subplane3D[]         */
   /*                                                                       */
   /*                                                                       */
   /*                                                                       */
   /* 4.) calculate the intersetion points of the TRACE[] lines             */
   /*     fct 'intersection_node_line_line' (in intersection_points.c)      */
   /*                                                                       */
   /*                                                                       */
   /*                                                                       */
   /*************************************************************************/
   for(i=0; i<sum_subplane3D; i++)              /*loop over all subplane3D[]*/
   {
      trace_nr   = 0;    /*start value*/
      nvertex_nr = 0;    /*start value*/


      /**********************************************************************/
      /* Variable 'struct fracture Subplane3D[]' is rewritten as            */
      /* a variable of the type 'struct trace subpl_trace3D[]'              */
      /* --> so we can use the function 'PreNetgenOutput_TRACE()', which    */
      /*     deals with the type 'struct trace ...'                         */
      /*                                                                    */
      /* --> for some operations, variable 'struct fracture Subplane3D[]'   */
      /*     has to be used, and for some 'struct trace subpl_trace3D[]'.   */
      /*     Sorry for this confusion! ;-)                                  */
      /*                                                                    */
      /**********************************************************************/
      AssignSubplane3D_to_subpl_trace3D(i, Subplane3D, subpl_trace3D);



      /**********************************************************************/
      /* start calculation of the traces laying in the subplane and the     */
      /* intersection points                                                */
      /*                                                                    */
      /* 1.) Calculate the standardized normal vector 'norm_0' of the plane */
      /*     --> the plane Subplane3D[] is now defined by the 'Hessesche    */
      /*         Normalform'                                                */
      /*                                                                    */
      /**********************************************************************/
      length_norm = abs_vec_point(&Subplane3D[i].norm); 

      norm_0.x = (1 / length_norm) * Subplane3D[i].norm.x;
      norm_0.y = (1 / length_norm) * Subplane3D[i].norm.y;
      norm_0.z = (1 / length_norm) * Subplane3D[i].norm.z;


      for(j=0; j < ntrace; j++)
      {
         /* set default value: trace total outside subplane */
         help_inside[0] = help_inside[1] = -1; 


         /*******************************************************************/
         /* 2.) The orientated distance between the point TRACE[].pt[]      */
         /*     and the plane Subplane3D[] which is written in the          */
         /*     'Hessesche Normalform', is calculated.                      */
         /*     distance[0]                                                 */
         /*     distance[1]                                                 */
         /*******************************************************************/
         distance[0] =  norm_0.x * (TRACE[j].pt[0].x - Subplane3D[i].pt[0].x)
                      + norm_0.y * (TRACE[j].pt[0].y - Subplane3D[i].pt[0].y)
                      + norm_0.z * (TRACE[j].pt[0].z - Subplane3D[i].pt[0].z);

         distance[1] =  norm_0.x * (TRACE[j].pt[1].x - Subplane3D[i].pt[0].x)
                      + norm_0.y * (TRACE[j].pt[1].y - Subplane3D[i].pt[0].y)
                      + norm_0.z * (TRACE[j].pt[1].z - Subplane3D[i].pt[0].z);
       

         /*******************************************************************/
         /* 3.) if (distance[0] <= epsilon_0) && (distance[1] <= epsilon_0) */
         /*          --> TRACE lays within the plane Subplane3D[]           */
         /*         Check for each trace points if it lays inside or outside*/
         /*         the plane. Use fct 'PointIsInQuadrilateral_3D()':       */
         /*         Return argument = 4: for strictly interior points and   */
         /*                              points laying on the line segments */ 
         /*                         < 4: points are strictly outside        */
         /*                                                                 */
         /*         case =4: TRACE[] inside subplane                        */
         /*                                                                 */
         /*                                                                 */
         /*    TODO: macht es Sinn, in einem 2D Gebiet einzelne Schnittpkt  */
         /*          (single intersection point) zu haben???                */
         /*          -> Eigentlich sind nur die Klüfte, die in der Ebene    */
         /*             liegen von Interesse!?!                             */
         /*                                                                 */
         /*                                                                 */
         /*         case <4: calculate the intersection point               */
         /*                  Fct 'intersection_node_line_plane'             */
         /*                  Return argument = 0: no single intersection pt */
         /*                                  = 1: single intersection point */
         /*                                                                 */
         /*                                                                 */
         /*     else --> TRACE does not lay within the plane Subplane3D[]   */
         /*                                                                 */
         /*******************************************************************/
         if ((distance[0] <= epsilon_0) && (distance[1] <= epsilon_0))
         {
            help_node[0] = Subplane3D[i].pt[0];
            help_node[1] = Subplane3D[i].pt[1];
            help_node[2] = Subplane3D[i].pt[2];
            help_node[3] = Subplane3D[i].pt[3];

            /****************************************************************/
            /* Check if point TRACE[j].pt[xx] lays inside the Quadrilateral.*/
            /* If point lays on the outer boundary of the Quadrilateral,    */
            /* the function 'PointIsInQuadrilateral_3D' != 4                */
            /****************************************************************/
            if (PointIsInQuadrilateral_3D(TRACE[j].pt[0], help_node, 
                                          Subplane3D[i].norm) == 4) {
               pt_intersect[0] = TRACE[j].pt[0];
               help_inside[0]  = 1; 

               /*************************************************************/
               /* Assign the 's_pt_intersect' point to 'VERTEX3D[]'         */
               /*********************++**************************************/
               VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, pt_intersect[0]);
               VERTEX3D[nvertex_nr].inside_subd3D = 1; 
               nvertex_nr++;
               free_VERTEX3D++;

            }
            if (PointIsInQuadrilateral_3D(TRACE[j].pt[1], help_node, 
                                          Subplane3D[i].norm) == 4) {
               pt_intersect[1] = TRACE[j].pt[1];
               help_inside[1]  = 1; 

               /*************************************************************/
               /* Assign the 's_pt_intersect' point to 'VERTEX3D[]'         */
               /*********************++**************************************/
               VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, pt_intersect[0]);
               VERTEX3D[nvertex_nr].inside_subd3D = 1; 
               nvertex_nr++;
               free_VERTEX3D++;

            }

            else if ((help_inside[0] < 1) && (help_inside[1] < 1)) 
            { /* the two new intersection points have to be calculated */
                
               line[0].pt0 = TRACE[j].pt[0];
               line[0].pt1 = TRACE[j].pt[1];

               l=0; /*set default value, l=[0,1] counter for 'pt_intersect'*/

               for (k=0; (k<4) && (l<2); k++)   /* 4 = subplane has 4 edges */
               { 
                  line[1].pt0 = subpl_trace3D[k].pt[0];
                  line[1].pt1 = subpl_trace3D[k].pt[1];

                  if (1 == intersection_node_line_line(line, &s_pt_intersect))
                  {
                     /*******************************************************/
                     /* Assign the VERTEX3D number to the 'subpl_trace3D'   */
                     /***************++**************************************/
                     AssignVertexToSubplaneTrace3D(k, subpl_trace3D, &nvertex_nr);

                     /*******************************************************/
                     /* Assign the 's_pt_intersect' point to 'VERTEX3D[]'   */
                     /***************++**************************************/
                     VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, s_pt_intersect);
                     VERTEX3D[nvertex_nr].inside_subd3D = 1; 
                     nvertex_nr++;
                     free_VERTEX3D++;

                     pt_intersect[l] = s_pt_intersect;
                     l++;
 
                  }
               }
            }


/*AH 16.01.*/
            /*one new intersection pt has to be calculated --> pt_intersect[m]*/
            for (m=0; m<2; m++)   /* m=[0,1]= two edge points */
            {
               if (m==0) m1 = 1;
               if (m==1) m1 = 0;
   
               if ((help_inside[m] < 1)  && (help_inside[m1] == 1)) 
               { 
                  line[0].pt0 = TRACE[j].pt[0];
                  line[0].pt1 = TRACE[j].pt[1];
   
                  for (k=0; k<4; k++)   /* 4 = subplane has 4 edges */
                  { 
                     line[1].pt0 = subpl_trace3D[k].pt[0];
                     line[1].pt1 = subpl_trace3D[k].pt[1];
   
                     if (1 == intersection_node_line_line(line, &s_pt_intersect))
                     {
                        /****************************************************/
                        /* Assign the VERTEX3D number to the 'subpl_trace3D'*/
                        /*************+**************************************/
                        AssignVertexToSubplaneTrace3D(k, subpl_trace3D, &nvertex_nr);

                        /****************************************************/
                        /* Assign the 's_pt_intersect' point to 'VERTEX3D[]'*/
                        /***************++***********************************/
                        VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, s_pt_intersect);
                        VERTEX3D[nvertex_nr].inside_subd3D = 1; 
                        nvertex_nr++;
                        free_VERTEX3D++;

                        pt_intersect[m] = s_pt_intersect;
                     }
                  }
               }
            }



            /****************************************************************/
            /* Assign the pt_intersect[] points to 'VERTEX3D[]'             */
            /***************++***********************************************/
/* AH, 06.02.2001: die Zuweisung der 'pt_intersect[]' Punkte erfolgt nun direkt
   an der Stelle, an der sie auch berechnet werden. Die VERTEX3D-Nummer der 
   neu berechneten 's_pt_intersect' Punkte werden dabei gleich an die 
   'subpl_trace' Linien weitergegeben.
            for (k=0; k<2; k++) {
               VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, pt_intersect[k]);
               VERTEX3D[nvertex_nr].inside_subd3D = 1; 
               nvertex_nr++;
               free_VERTEX3D++;
            }
*/


            /****************************************************************/
            /* Assign the calculated intersection lines to 'EDGE2D[]'       */
            /***************++***********************************************/
            trace_length   = abs_vec_pt_pt(&pt_intersect[0], &pt_intersect[1]);
            trace_aperture = TRACE[j].frac_aperture;

            EDGE2D = add_EDGE2D_to_StructTraceList(trace_nr, 
                                                   trace_length, 
                                                   pt_intersect,
                                                   trace_aperture, 
                                                   &trace_nr); /*incrementation*/
            EDGE2D[trace_nr-1].inside_subd3D = 1; 
            free_EDGE2D++;


            /***************++**************************************************/
            /* Check, if the intersection lines cut each other                 */
            /* YES: single intersection point 's_pt_intersect'                 */
            /***************++**************************************************/
            for (k=0; trace_nr>1 && k<(trace_nr-1); k++)
            {
               /*TODO:AH if (1==1): IF--BEDINGUNG NOCH EINFUEGEN !!!*/
               line[0].pt0 = EDGE2D[trace_nr-1].pt[0];
               line[0].pt1 = EDGE2D[trace_nr-1].pt[1];
               line[1].pt0 = EDGE2D[k].pt[0];
               line[1].pt1 = EDGE2D[k].pt[1];
               if (intersection_node_line_line(line,&s_pt_intersect) == 1)
               {
                  /****************************************************/
                  /* Assign the single intersection pt to 'VERTEX3D'  */ 
                  /****************************************************/
                  VERTEX3D = add_VERTEX3D_to_list(nvertex_nr, s_pt_intersect);
                  VERTEX3D[nvertex_nr].inside_subd3D = 1; 
                  free_VERTEX3D++;

                  AssignVertexToEDGE2D(trace_nr-1, EDGE2D);
                  AssignVertexToEDGE2D(k, EDGE2D);
                  nvertex_nr++;
               }
            }
         }
      } /*loop over the TRACE[] elements*/


      /*******************************************************************/
      /* Kluftspuren in Liniensegmente (1D Elemente) aufspalten          */
      /* (Schnittpkt zu Schnittpkt)                                      */
      /*******************************************************************/


      /*******************************************************************/
      /* Backbone bestimmen                                              */
      /*******************************************************************/


      /*******************************************************************/
      /* Durchflusswirksame Liniensegmente bestimmen                     */
      /*******************************************************************/


      /**********************************************************************/
      /* Data structure for mesh generation                                 */
      /*                                                                    */
      /*    Provide data structure for the mesh generating program          */
      /*    Reduce the whole point, trace, fracture ... information to      */
      /*    --> vertex, edge, face, element                                 */
      /*                                                                    */
      /* Write output files:  for tecplot (graphik)                         */
      /*                      for ART     (mesh generator)                  */
      /*                                                                    */
      /*                                                                    */
      /* coordinate transformation                                          */
      /*  ART can not handle a 2D plane with 1D elements in the 3D space!!! */
      /*  So the points has to be transformed in 2D.                        */
      /*                                                                    */
      /**********************************************************************/

      PreNetgenOutput_1dTRACE_in2d(nvertex_nr, VERTEX3D,
                                   trace_nr, EDGE2D,
                                   i, Subplane3D, 
                                   subpl_trace3D,
                                   file_number);
      file_number++;


      /**********************************************************************/
      /* give memory back                                                   */
      /**********************************************************************/
      free (Subplane3D); 
      if (free_EDGE2D   > 0) free (EDGE2D);           /*give the memory back*/
      if (free_VERTEX3D > 0) free (VERTEX3D);         /*give the memory back*/
      nvertex_net = 0;    /*set value back*/
      nedge_net = 0;      /*set value back*/

   } /*loop over the Subplane3D[] elements*/

}



/****************************************************************************/
/*                                                                          */
/*               F U N C T I O N S                                          */
/*                                                                          */
/****************************************************************************/
/* gen_static_subplane3D_list()                                             */
/*                                                                          */
/*  return value: number of subplanes, which are read from input file       */
/*                                                                          */
/****************************************************************************/
int gen_static_subplane3D_list(FILE *PF, struct fracture Subplane3D[])
{
   int i=0, pt_nr=0, linenr=1;
   char line[512];
   char *rptr;
   struct point p0, p1, p2, p3;    /*arguments fct. 'FourPointsLayInPlane()'*/

   i=0;
   while (fgets(line, 512, PF) != NULL)   
   {
      while(1)
      {
         if(pt_nr < 4)    /* 4 = four corner nodes */
         {   
            fgets(line, 512, PF);
            if ((rptr = strpbrk(line, "#\n")) != NULL)   
            {
               line[rptr - line] = 0;   
            }
            if ((rptr = strtok(line, " \t")) != NULL)   
            {
               Subplane3D[i].pt[pt_nr].x = strtod(rptr, NULL);
               if ((rptr = strtok(NULL, " \t")) == NULL)    
               {    
                  fprintf(stderr,   
                  "male formed line %d, 3 arguments required"    
                  " found only 1\n", linenr);    
                  continue;    
               }    
               Subplane3D[i].pt[pt_nr].y = strtod(rptr, NULL);
               if ((rptr = strtok(NULL, " \t")) == NULL)   
               {    
                  fprintf(stderr,    
                  "male formed line %d, 3 arguments required"    
                  " found only 2\n", linenr);    
                  continue;    
               }    
               Subplane3D[i].pt[pt_nr].z = strtod(rptr, NULL);
               pt_nr++;
            }
         }
         else
         {
            /*****************************************************************/
            /* TODO : check, if the four points lay in a plane               */
            /*****************************************************************/
            p0=Subplane3D[i].pt[0];
            p1=Subplane3D[i].pt[1];
            p2=Subplane3D[i].pt[2];
            p3=Subplane3D[i].pt[3];

            if (FourPointsLayInPlane(&p0, &p1, &p2, &p3) == 1)
            {
               Subplane3D[i].pt[2] = p2;
               fprintf(stdout,"\n!!!Attention: Subplane3D[%d] ",i);
               fprintf(stdout,"\n   Not all of the four corner nodes lay in a plane ");
               fprintf(stdout,"\n   --> New point Subplane3D[%d].pt[2]: ", i);
               fprintf(stdout," x=%f  y=%f  z=%f \n", 
                Subplane3D[i].pt[2].x, Subplane3D[i].pt[2].y, Subplane3D[i].pt[2].z);
            }
            pt_nr = 0;
            i++; 
            break;
         }
         linenr++;    
      }
   }
   return (i);
}


/****************************************************************************/
/* AssignSubplane3DValues()
/*                                                                          */
/****************************************************************************/
void AssignSubplane3DValues(int i, struct fracture *Subplane3D) 
{
   struct point a, b;
 
   Subplane3D[i].frac_nr = i;

   a.x = Subplane3D[i].pt[1].x - Subplane3D[i].pt[0].x;
   a.y = Subplane3D[i].pt[1].y - Subplane3D[i].pt[0].y;
   a.z = Subplane3D[i].pt[1].z - Subplane3D[i].pt[0].z;

   b.x = Subplane3D[i].pt[3].x - Subplane3D[i].pt[0].x;
   b.y = Subplane3D[i].pt[3].y - Subplane3D[i].pt[0].y;
   b.z = Subplane3D[i].pt[3].z - Subplane3D[i].pt[0].z;

   Subplane3D[i].norm.x =  a.y*b.z - a.z*b.y;
   Subplane3D[i].norm.y = -a.x*b.z + a.z*b.x;
   Subplane3D[i].norm.z =  a.x*b.y - a.y*b.x;

/*AH 20.12.2000, Seitenlaengenberechnung macht keinen Sinn, da
     die Subplane nicht immer rechtwinklig ist! */
/*
   Subplane3D[i].length[0] = sqrt(pow(a.x,2) + pow(a.y,2) + pow(a.z,2));
   Subplane3D[i].length[1] = sqrt(pow(b.x,2) + pow(b.y,2) + pow(b.z,2));
*/
   
   Subplane3D[i].inside_subd3D = 0;    /* dummy values ! */
   Subplane3D[i].index_ch      = 0;    /* dummy values ! */
   Subplane3D[i].sum_ptch      = 0;    /* dummy values ! */

}


/****************************************************************************/
/* AssignSubplane3D_to_subpl_trace3D()                                      */
/*                                                                          */
/****************************************************************************/
void AssignSubplane3D_to_subpl_trace3D(int i, 
                                       struct fracture *Subplane3D, 
                                       struct trace *subpl_trace3D)
{
   int j, j1, k;

   int initial_nvertex_on_trace=100; 
    /* AH: 16.03.2000 TODO --> Zahl raus nehmen!!! */
    /* in order to avoid a lot of allocation steps, each trace get an array
       of 100 possible 'places' for vertex points (=intersection points) */
 

   for (j=0; j<4; j++)
   {
      j1 = j+1;
      if (j==3) j1 = 0;
      subpl_trace3D[j].pt[0] = Subplane3D[i].pt[j];
      subpl_trace3D[j].pt[1] = Subplane3D[i].pt[j1];

      subpl_trace3D[j].length
         = sqrt(  pow((Subplane3D[i].pt[j1].x-Subplane3D[i].pt[j].x),2)
                + pow((Subplane3D[i].pt[j1].y-Subplane3D[i].pt[j].y),2)
                + pow((Subplane3D[i].pt[j1].z-Subplane3D[i].pt[j].z),2));

      subpl_trace3D[j].nr = i;

      subpl_trace3D[j].nvertex_on = 0;
      if ((subpl_trace3D[j].vertex_on 
              = (int *)malloc(initial_nvertex_on_trace * sizeof(int))) == NULL)
      {
         fprintf(stderr,"Memory allocation failed: 'subpl_trace3D[j].vertex_on'\n");
         exit (-1);
      }


      for (k=0; k<initial_nvertex_on_trace; k++)
      {
         subpl_trace3D[j].vertex_on[k] = -999;
         if (j==(initial_nvertex_on_trace-1)) subpl_trace3D[j].vertex_on[k] = -1;
      }
      subpl_trace3D[j].inside_subd3D = 1;
   }

}


/****************************************************************************/
/* AssignVertexToSubplaneTrace3D()                                          */
/*                                                                          */
/* if a point lay on the subpl_trace3D[] and the corner points of the       */ 
/* subpl_trace3D[] are not equal to the point:                              */
/*                                                                          */
/* Not equal: --> subpl_trace3D[i].nvertex_on++                             */
/*                                                                          */
/* Why: for mesh generating process, the subpl_trace3D[] has to be split    */
/*      in single trace segments.                                           */
/*                                                                          */
/****************************************************************************/
void AssignVertexToSubplaneTrace3D(int i, struct trace *subpl_trace3D, 
                                   int *nvertex_nr)
{
   int counter;
   int dummy; 

   counter = *nvertex_nr;
   dummy = subpl_trace3D[i].nvertex_on;

   if (subpl_trace3D[i].vertex_on[dummy] != -1) {
      subpl_trace3D[i].vertex_on[dummy] = counter;
   }

   else {
      subpl_trace3D[i].vertex_on = realloc( subpl_trace3D[i].vertex_on,
                                    (subpl_trace3D[i].nvertex_on+2)*sizeof(int));
      if (subpl_trace3D[i].vertex_on == NULL) {
         fprintf(stdout,"\nMemory allocation failed: subpl_trace3D[i]\n");
         exit (-1);
      }
      subpl_trace3D[i].vertex_on[dummy]   = counter;
      subpl_trace3D[i].vertex_on[dummy+1] = -1;
   }

   subpl_trace3D[i].nvertex_on++;

/* AH, 0602.2001: der 'counter' darf hier nicht um '1' erhoeht werden.
   Ansonsten stimmt die Anzahl der Vertex-Punkte nicht mehr, bzw, der erste
   Vertex-Punkt bekommt anstelle der Nummer '0' die Nummer '1'!!! 
   
   counter++;
AH, 06.02.2001: */


   *nvertex_nr = counter;

}


/****************************************************************************/
/* AssignVertexToEDGE2D()                                                   */
/*                                                                          */
/****************************************************************************/
void AssignVertexToEDGE2D(int i, struct trace *EDGE2D) 
{
   int dummy; 

   dummy = EDGE2D[i].nvertex_on;

   if (EDGE2D[i].vertex_on[dummy] != -1)
   {
      EDGE2D[i].vertex_on[dummy] = nvertex_nr;
   }

   else
   {
      EDGE2D[i].vertex_on =
         realloc( EDGE2D[i].vertex_on,
              (EDGE2D[i].nvertex_on+2)*sizeof(int));
      if (EDGE2D[i].vertex_on == NULL)
      {
         fprintf(stdout,"\nMemory allocation failed: EDGE2D[]\n");
         exit (-1);
      }
      EDGE2D[i].vertex_on[dummy]   = nvertex_nr;;
      EDGE2D[i].vertex_on[dummy+1] = -1;
   }

   EDGE2D[i].nvertex_on++;
}




