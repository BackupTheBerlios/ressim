/****************************************************************************/
/*                                                                          */
/* File:      subvolume3D.c                                                 */
/*                                                                          */
/* Purpose:   3D-subvolume is cutted out of the 3D-volume                   */
/*            3D-subvolume: regular prisma                                  */
/*                                                                          */
/*            * intersectionlines : fractures -- subvolume-plane            */
/*            * intersectionpoints: intersection points -- subvolume-plane  */
/*                                                                          */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/* Functions: int subvolume_3D_intersection_FRAC()                          */
/*            int subvolume_3D_intersection_TRACE()                         */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "functions.h" 
#include "graham.h"
#include "intersection.h"
#include "subvolume3D.h"         


/****************************************************************************/
/* subvolume_3D_intersection_FRAC()                                         */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
int subvolume_3D_intersection_FRAC()
{
   int i, i1, j, k, k1, l;
   int help_PointInSubvolume;
   int help_free_polygon = 0;
   int nedge_frac = 4;
   int n_edgept;
   int count100 = 100, n100 = 0;
  
   struct point help_edgept[2]; 
   struct point sphere_midpt; 

   subvolume_type = get_var_integer(uvar, "subvolume_type");
  
   if (subvolume_type == 1)  
   {
      /**********************************************************************/
      /* Description of the subvolume as a regulare prisma.                 */
      /**********************************************************************/
      sum_subvol_edges   = get_var_integer(uvar, "sum_subvol_edges");

      /**********************************************************************/
      /* Allocation of memory for the array:    subvol_bot_pt[]             */
      /*                                        subvol_top_pt[]             */
      /*                                        subvol_side[]               */
      /* AH30.06.99 still TODO: give the allocated memory back !            */
      /**********************************************************************/
      if ((subvol_radius_side = 
             (double *)malloc(sum_subvol_edges * sizeof(double)))== NULL)  
      {
         fprintf(stderr,"Memory allocation failed: 'FACE_net[nface_net].EDGE_NR' \n");
         exit (-1);
      }

      subvol_side_midpt  = AllocateStructPointList(sum_subvol_edges);
      subvol_bot_pt      = AllocateStructPointList(sum_subvol_edges);
      subvol_top_pt      = AllocateStructPointList(sum_subvol_edges);
      subvol_side        = AllocateStructFractureList(sum_subvol_edges);


      /**********************************************************************/
      /*  Calculate the edges, length, ... of the subvol subvolume volume   */
      /*  --> function CalculateSubvolPrismaVariables()  in prisma.c        */
      /**********************************************************************/
      CalculateSubvolPrismaVariables(sum_subvol_edges,
                                     &subvol_radius_bot,
                                     subvol_radius_side,
                                     &subvol_bot_midpt,
                                     &subvol_top_midpt,
                                     subvol_side_midpt,
                                     subvol_bot_pt, 
                                     subvol_top_pt, 
                                     subvol_side); 
   }
   else if (subvolume_type == 2)  
   {
      /**********************************************************************/
      /* Description of the subvolume as a regulare/irregulare quader       */
      /**********************************************************************/
      sum_subvol_edges   = get_var_integer(uvar, "sum_subvol_edges");

      /**********************************************************************/
      /* Allocation of memory for the array:    subvol_bot_pt[]             */
      /*                                        subvol_top_pt[]             */
      /*                                        subvol_side[]               */
      /* AH30.06.99 still TODO: give the allocated memory back !            */
      /**********************************************************************/
      if ((subvol_radius_side = 
             (double *)malloc(sum_subvol_edges * sizeof(double)))== NULL)  
      {
         fprintf(stderr,"Memory allocation failed: 'FACE_net[nface_net].EDGE_NR' \n");
         exit (-1);
      }
      subvol_side_midpt  = AllocateStructPointList(sum_subvol_edges);
      subvol_bot_pt      = AllocateStructPointList(sum_subvol_edges);
      subvol_top_pt      = AllocateStructPointList(sum_subvol_edges);
      subvol_side        = AllocateStructFractureList(sum_subvol_edges);


      /**********************************************************************/
      /*  Calculate the edges, length, ... of the subvolume                 */
      /*  --> function CalculateSubvolQuaderVariables()  in quader.c        */
      /**********************************************************************/
      CalculateSubvolQuaderVariables(sum_subvol_edges,
                                     &subvol_radius_bot,
                                     subvol_radius_side,
                                     &subvol_bot_midpt,
                                     &subvol_top_midpt,
                                     subvol_side_midpt,
                                     subvol_bot_pt, 
                                     subvol_top_pt, 
                                     subvol_side); 
   }
   else 
   {
      fprintf(stdout,"\n The Value 'subvolume_type' = %d is not defined. Check the input file! \n", subvolume_type);
      exit (-1);
   }


   /*************************************************************************/
   /* Investigate fracture planes list 'FRAC' -- subvolume 3D               */
   /*                                                                       */
   /*************************************************************************/
   /* First loop: over the FRAC - elements                                  */
   /*                                                                       */
   /*   Second loop: over the subvol side planes                            */
   /*                                                                       */
   /*     Check, if the two planes cut each other theoretically             */
   /*                                                                       */
   /*     1.) calculate for each plane the radius of the sphere             */
   /*         which surrounds the plane : radius1, subvol_radius_side       */
   /*                                                                       */
   /*     2.) calculate the distance between the midpoints of the           */
   /*         two planes : distance_points                                  */
   /*                                                                       */
   /*     3.) if: distance <= (radius1 + subvol_radius_side)                */
   /*         --> yes: go to calculate the intersection points              */
   /*                  check, if they lay in the volume of the planes       */
   /*                  (see further on)                                     */
   /*         --> no : forget it and go to the next subvol plane            */
   /*                                                                       */
   /*************************************************************************/
   for (i=0; i<nfrac; i++)
   {

      if (i == ( (n100+1) * count100))  {
         fprintf(stdout,"\n Extracting subvolume  ............. %5d. fracture plane", i);
         n100++;
      }


      help_free_polygon  = 0;   /*set default value*/
      sum_polypt = 0;           /* set counter of # of polygon points = 0 */

      /**********************************************************************/
      /* Set default values                                                 */
      /*   FRAC[i].sum_ptch =  0    : sum of points on convex hull          */
      /*   FRAC[i].index_ch = -1    : fracture described by an rectangle    */
      /*                              and not by a convex hull              */
      /*   FRAC[i].inside_subd3D = 0: fracture plane outside subvolume      */
      /**********************************************************************/
      FRAC[i].sum_ptch =  0;   
      FRAC[i].index_ch = -1;   
      FRAC[i].inside_subd3D = 0;


      /**********************************************************************/
      /* Check, if all nodes of the fracture plane lay ...on/inside...      */
      /* the subvolume.                                                     */
      /* --> Apply function 'PointInSubvolume()'                            */
      /*                                                                    */
      /* Sum up the return values of function 'PointInSubvolume()'          */
      /* If 'help_PointInSubvolume' == 4 : all points lay inside subvolume  */
      /*                                   --> FRAC[i].inside_subd3D = 1    */
      /*                                                                    */
      /**********************************************************************/
      help_PointInSubvolume = 0;
      for (j=0; j<4; j++)      /* 4 = nodes of the fracture rectangle plane */
      {
         help_PointInSubvolume += PointInSubvolume(FRAC[i].pt[j]);
      }
      if (help_PointInSubvolume == 4) FRAC[i].inside_subd3D = 1;

      if (help_PointInSubvolume < 4)
      {
         /*******************************************************************/
         /* Investigate the side planes of the subvolume with respect to    */
         /* intersection points by cutting the fracture planes              */
         /*******************************************************************/
   
         radius1 = approx_radius_sphere(i, FRAC, &sphere_midpt);    
                                 /* sphere radius surrounding plane FRAC[i] */

         for (j=0; j<sum_subvol_edges; j++)
         {
            sum_radius1_2 = radius1 + subvol_radius_side[j];

            /* midpoint of the subvol side plane */
            center2.x = 0.5*(subvol_side[j].pt[2].x+subvol_side[j].pt[0].x);
            center2.y = 0.5*(subvol_side[j].pt[2].y+subvol_side[j].pt[0].y);
            center2.z = 0.5*(subvol_side[j].pt[2].z+subvol_side[j].pt[0].z);

            distance_points = 
                 sqrt(  pow((subvol_side_midpt[j].x - sphere_midpt.x),2)
                      + pow((subvol_side_midpt[j].y - sphere_midpt.y),2)
                      + pow((subvol_side_midpt[j].z - sphere_midpt.z),2));
              
            if ((sum_radius1_2) >= distance_points)
            {                       /* start intersection point calulation */
               elem_frac[0] = FRAC[i];
               elem_frac[1] = subvol_side[j];  

               /*************************************************************/
               /* check, if fracture plane cuts the side planes of the      */
               /* subvolume                                                 */
               /* YES: 1.) the fracture plane is 'inside' the subvolume     */
               /*           --> FRAC[i].inside_subd3D = 1                   */
               /*      2.) the fracture plane has not to be any longer      */
               /*          a rectangle; it could be a n*polygon             */
               /*           --> description as a polyline                   */
               /*************************************************************/
               if (intersection_nodes_plane_plane(elem_frac,pt_intersect)==1)
               {
                  FRAC[i].inside_subd3D = 1;

                  for (k=0; k<2; k++)           /*2= two pt_intersect points*/
                  {      
                     /*******************************************************/  
                     /* Check, if within the list 'polygon_frac[k].glob'    */
                     /* of the fracture plane 'FRAC[i]' exist already a     */
                     /* point which is equal to 'pt_intersect[k]'           */
                     /*                                                     */
                     /* If 'Point1_equal_Point2() is true:                  */
                     /*     -->  return value = 1                           */
                     /*                                                     */
                     /*    help_add < 1: no equal point                     */
                     /*                  --> add pt_intersect[k] to list    */
                     /*    help_add >=1: equal point                        */
                     /*                  --> do not add pt_intersect[k]     */
                     /*                                                     */
                     /*******************************************************/  
                     help_add = 0;
                     for (l=0; l<sum_polypt; l++) {
                        help_add += Point1_equal_Point2(polygon_frac[l].glob, 
                                                        pt_intersect[k]);
                     }

                     if (help_add < 1) 
                     {
                        polygon_frac = AddToPolygonList(&sum_polypt,
                                                        pt_intersect[k]);
                        help_free_polygon++;
                     }
                  } 
               }
            }
         }
         /*******************************************************************/
         /* Investigate the 'top' and the 'bottom' plane of the subvol with */
         /* respect to intersection points by cutting the fracture planes.  */
         /* --> just focus on the fracture edges and not on the subvol      */
         /*     edges. They are already investigated in the steps above.    */
         /*******************************************************************/
         /* top plane */
         distance_points = 
          sqrt(  pow((subvol_top_midpt.x - sphere_midpt.x),2)
               + pow((subvol_top_midpt.y - sphere_midpt.y),2)
               + pow((subvol_top_midpt.z - sphere_midpt.z),2));

         if ((radius1+subvol_radius_bot) >= distance_points)         
         {
            for (k=0; k<nedge_frac; k++)
            {
               k1 = k+1;
               if (k1 == nedge_frac) k1=0;

               if (intersection_node_line_polygon(FRAC[i].pt[k], 
                                                  FRAC[i].pt[k1],
                                                  sum_subvol_edges,
                                                  subvol_top_pt,    
                                                  &s_pt_intersect) == 1)
               {
                  help_add = 0;
                  for (l=0; l<sum_polypt; l++) {
                     help_add += Point1_equal_Point2(polygon_frac[l].glob,
                                                     s_pt_intersect);
                  }
                  if (help_add < 1)
                  {
                     polygon_frac = AddToPolygonList(&sum_polypt,
                                                     s_pt_intersect);
                     FRAC[i].inside_subd3D = 1;
                     help_free_polygon++;
                  }
               }
            }
         }

         /* bottom plane */
         distance_points = 
           sqrt(  pow((subvol_bot_midpt.x - sphere_midpt.x),2) 
                + pow((subvol_bot_midpt.y - sphere_midpt.y),2) 
                + pow((subvol_bot_midpt.z - sphere_midpt.z),2)); 

         if ((radius1+subvol_radius_bot) >= distance_points)         
         {
            for (k=0; k<nedge_frac; k++)
            {
               k1 = k+1;
               if (k1 == nedge_frac) k1=0;
   
               if (intersection_node_line_polygon(FRAC[i].pt[k], 
                                                  FRAC[i].pt[k1],
                                                  sum_subvol_edges,
                                                  subvol_bot_pt,    
                                                  &s_pt_intersect) == 1)
               {
                  help_add = 0;
                  for (l=0; l<sum_polypt; l++) {
                     help_add += Point1_equal_Point2(polygon_frac[l].glob,
                                                     s_pt_intersect);
                  }
                  if (help_add < 1)
                  {
                     polygon_frac = AddToPolygonList(&sum_polypt,
                                                     s_pt_intersect);
                     FRAC[i].inside_subd3D = 1;
                     help_free_polygon++;
                  }
               }
            }
         }
  
         if (sum_polypt > 0)  
         {
            /****************************************************************/  
            /* sum_polypt > 0:  there exists a list 'polygon_frac'          */
            /*                                                              */
            /* Investigate, if the single fracture edge point FRAC[i].pt[j] */
            /* lay on/inside/outside the subvol volume volume.              */
            /*                                                              */
            /* --> Apply function PointInSubvolume()                        */
            /*                                                              */
            /****************************************************************/  
            for (k=0; k<4; k++)
            {
               if (PointInSubvolume(FRAC[i].pt[k]) == 1)  
               {
                  /**********************************************************/
                  /* FRAC[i].pt[k] lays on/inside subvolume                 */
                  /*                                                        */
                  /* Check, if there is already an existing point           */
                  /* polygon_frac[k].glob which is equal to FRAC[i].pt[k]   */
                  /*                                                        */
                  /* If 'Point1_equal_Point2() is true: return value = 1    */
                  /*                                                        */
                  /*  help_add <  1: no point polygon_frac[l].glob is equal */
                  /*                 to FRAC[i].pt[k]  --> add to list      */
                  /*  help_add >= 1: point polygon_frac[l].glob equal to    */
                  /*                 FRAC[i].pt[k]  --> do not add to list  */
                  /*                                                        */
                  /**********************************************************/  
                  help_add = 0;
                  for (l=0; l<sum_polypt; l++)
                  {
                     help_add += Point1_equal_Point2(polygon_frac[l].glob, 
                                                     FRAC[i].pt[k]);
                  }
                  if (help_add < 1) 
                  {
                     polygon_frac = AddToPolygonList(&sum_polypt,
                                                  FRAC[i].pt[k]);
                     help_free_polygon++;
                  }
               } 
            }

            /****************************************************************/  
            /*                                                              */
            /* --> List 'polygon_frac' includes only the points which lay on*/
            /*     the subvol volume or within the subvol volume.           */
            /*                                                              */
            /****************************************************************/  
            /* Calculate the spherical rotation matrix ROT[][].             */
            /*                                                              */
            /* Declare the inverse matrix ROT_i[][], which has to be        */
            /* calculated by applying the function GaussJordan().           */
            /*                                                              */
            /* Further on:                                                  */
            /* --> applying the Gauss-Jordan elimination with full pivoting */
            /*                                                              */
            /* --> On output, matrix ROT_i is replaced by its matrix inverse*/
            /*     --> after applying the function GaussJordan(), the       */
            /*         rotation matrix ROT_i[][] includes its inverse matrix*/
            /*                                                              */
            /****************************************************************/  
            RotationMatrix3D(FRAC[i].pt[0], FRAC[i].pt[1], FRAC[i].pt[3], 
   		                     FRAC[i].norm, ROT);

            ROT_i[0][0] =  ROT[0][0];
            ROT_i[0][1] =  ROT[0][1];
            ROT_i[0][2] =  ROT[0][2];
   
            ROT_i[1][0] =  ROT[1][0];
            ROT_i[1][1] =  ROT[1][1];
            ROT_i[1][2] =  ROT[1][2];

            ROT_i[2][0] =  ROT[2][0];
            ROT_i[2][1] =  ROT[2][1];
            ROT_i[2][2] =  ROT[2][2];
   

            /* applying the Gauss-Jordan elimination with full pivoting */
            n_gj = m_gj = 3;
            GaussJordan(ROT_i, B_GJ, n_gj, m_gj);


            for (j=0; j<sum_polypt; j++)
            {      
               CoordinateTransformation(j, polygon_frac, FRAC[i].pt[0], ROT_i);
            }

            /*************************************************************/  
            /* Determing the convex hull of all points which are included*/
            /*    in the list 'polygon_frac[j]' and are indicated by the */
            /*    polygon_frac[j].index = 1.                             */
            /*                                                           */
            /*    The calculation is done in the transformed 2D plane of */
            /*    the fracture plane FRAC[i]                             */
            /*                                                           */
            /* AH TODO (10.08.99): Algorithmus auf 'Herz und Nieren      */
            /*                     testen'                               */
            /*                                                           */
            /*************************************************************/  
            sum_polypt = GrahamConvexHull_2D(polygon_frac, sum_polypt); 

            /*************************************************************/  
            /* Back coordinaten transformation into the global coordinate*/
            /* system                                                    */
            /*************************************************************/  
            for (j=0; j<sum_polypt; j++) {      
               CoordinateBackTransformation(j,polygon_frac,FRAC[i].pt[0],ROT);
            }


            /*************************************************************/  
            /* The local list polygon_frac becomes global.               */
            /*    --> needed for the output                              */
            /*                                                           */
            /* Declare 'FRAC[i].sum_ptch': # of points of the convex hull*/
            /*                                                           */
            /* Allocate memory for the array FRAC[i].ch[] and declare it.*/
            /*                                                           */
            /*************************************************************/  
            FRAC[i].sum_ptch = sum_polypt;
            FRAC[i].ch = 
                  (struct point *) malloc(sum_polypt * sizeof(struct point));
            if (FRAC[i].ch == NULL)
            {
               fprintf(stderr,"Nicht genuegend Speicher fuer FRAC[%d].ch \n",i);
               exit (-1);
            }

            for (j=0; j<sum_polypt; j++) {      
               FRAC[i].ch[j] = polygon_frac[j].glob; 
            }
            FRAC[i].index_ch = 1;
 
         }

      }
      /**********************************************************************/ 
      /* give the memory of 'polygon_frac' back                             */
      /**********************************************************************/ 
      if (help_free_polygon > 0)  free(polygon_frac);
   }

   /*************************************************************************/
   /* TODO !!!!!!!!!!!!!!!!!!!    AAAAAAAAAA                                */
   /* Schnittpunkteberechnung der neuen Kluftelemente!!!!!!!!!!!!!!!!       */
   /*                                                                       */
   /* Check the new fracture borders: are there intersection points?        */
   /*                                                                       */
   /* Apply function ....                                                   */
   /*                                                                       */
   /*************************************************************************/
   NewFractureBorders_inSubvolume_TouchEachOther(&nvertex_nr);
  

   /*************************************************************************/
   /*                                                                       */
   /* Investigate intersection lines (list 'EDGE3D') -- subvolume           */
   /* The list 'EDGE3D' has been determined in function 'intersection_FRAC' */
   /*                                                                       */
   /*************************************************************************/
   if (edge_nr_3D > 0)
   {   

      for (i=0; i<edge_nr_3D; i++)
      {
         EDGE3D[i].inside_subd3D = 0;       /* set default value: edge line 
                                               segment outside of subvolume */

         /*******************************************************************/
         /* Check, if all nodes of the edge line lay inside the subvolume.  */
         /* --> Apply function 'PointInSubvolume()'                         */
         /*                                                                 */
         /* Sum up the return value.                                        */
         /* If help_PointInSubvolume = 2: all points lay inside subvolume   */
         /*                               --> EDGE3D[i].inside_subd3D = 1   */
         /*                                                                 */
         /*******************************************************************/
         help_PointInSubvolume = 0;
         help_PointInSubvolume += PointInSubvolume(EDGE3D[i].pt0);
         help_PointInSubvolume += PointInSubvolume(EDGE3D[i].pt1);

         if (help_PointInSubvolume == 2) EDGE3D[i].inside_subd3D = 1;


         /* Calculate  midpoint of the edge line */ 
         center1.x = 0.5*(EDGE3D[i].pt0.x + EDGE3D[i].pt1.x);
         center1.y = 0.5*(EDGE3D[i].pt0.y + EDGE3D[i].pt1.y);
         center1.z = 0.5*(EDGE3D[i].pt0.z + EDGE3D[i].pt1.z);

         n_edgept = 0;    /* set counter for each edge line segment to zero */

         for (j=0; j<sum_subvol_edges; j++)
         {
            
            /****************************************************************/
            /* Check, if the subvol side planes and the edge line are close */
            /* to each other.                                               */
            /*                                                              */
            /* --> If the distance between the two middle points is smaller */
            /*     than the radius surrounding the subvolume side plane.    */
            /*                                                              */
            /****************************************************************/
            distance_points = 
                         sqrt(  pow((subvol_side_midpt[j].x - center1.x),2)
                              + pow((subvol_side_midpt[j].y - center1.y),2)
                              + pow((subvol_side_midpt[j].z - center1.z),2));


            if ((subvol_radius_side[j]) >= distance_points)
            {                       /* start intersection point calulation */
               /*************************************************************/
               /* check, if edge line cuts the side planes of the subvolume */
               /* subvol.                                                   */
               /* YES: 1.) edge line lays partlly in the subvolume          */
               /*          --> EDGE3D[i].inside_subd3D = 1                  */
               /*      2.) the edge line gets new coordinates for the point */
               /*          laying outside the volume                        */
               /*************************************************************/
               if (intersection_node_line_plane(EDGE3D[i].pt0,
                                                EDGE3D[i].pt1,
                                                subvol_side[j], 
                                                &s_pt_intersect) == 1)
               {
                  /**********************************************************/
                  /* assign the intersection point 's_pt_intersect' to the  */
                  /* help point 'help_edgept[]'.                            */
                  /* max # of intersection point: n_edgept <= 2             */
                  /**********************************************************/
                  EDGE3D[i].inside_subd3D = 1;
                  help_edgept[n_edgept] = s_pt_intersect; 
                  n_edgept++; 
               }
            }
         }

  
         if (n_edgept < 2)
         {
            /****************************************************************/
            /* Investigate the 'top' and the 'bottom' plane of the subvol   */
            /* with respect to intersection points by cutting the edge line.*/
            /****************************************************************/

            /* top plane */
            distance_points = sqrt(  pow((subvol_top_midpt.x - center1.x),2)  
                                   + pow((subvol_top_midpt.y - center1.y),2)  
                                   + pow((subvol_top_midpt.z - center1.z),2));  

            if ((subvol_radius_bot) >= distance_points)
            {
               if (1 == intersection_node_line_polygon(EDGE3D[i].pt0, 
                                                       EDGE3D[i].pt1,
                                                       sum_subvol_edges,
                                                       subvol_top_pt,    
                                                       &s_pt_intersect))
               {
                  EDGE3D[i].inside_subd3D = 1;
                  help_edgept[n_edgept] = s_pt_intersect; 
                  n_edgept++; 
               }
            }
         }


         if (n_edgept < 2)
         {
            /* bottom plane */
            distance_points = sqrt(  pow((subvol_bot_midpt.x - center1.x),2)  
                                   + pow((subvol_bot_midpt.y - center1.y),2)  
                                   + pow((subvol_bot_midpt.z - center1.z),2));  
 
            if ((subvol_radius_bot) >= distance_points)
            {
               if (1 == intersection_node_line_polygon(EDGE3D[i].pt0, 
                                                       EDGE3D[i].pt1,
                                                       sum_subvol_edges,
                                                       subvol_bot_pt,    
                                                       &s_pt_intersect))
               {
                     EDGE3D[i].inside_subd3D = 1;
                     help_edgept[n_edgept] = s_pt_intersect; 
                     n_edgept++; 
               }
            }
         }

         /*******************************************************************/
         /* Assign the new edge points 'help_edgept[]' to the line segment  */
         /* as new the new points.                                          */
         /*******************************************************************/
         if (n_edgept == 2)
         {
            EDGE3D[i].pt0 = help_edgept[0]; 
            EDGE3D[i].pt1 = help_edgept[1]; 
            /* EDGE3D[i].inside_subd3D = 1;      */
         }
         if (n_edgept == 1)
         {
            /****************************************************************/
            /* Only one new ege point --> help_edgept[0]                    */
            /* Which one of the two edge points lays outside the subvolume? */
            /* --> only one old egde point lays outside the subvolume       */
            /* --> Apply function 'PointInSubvolume()'                      */
            /****************************************************************/
            if (PointInSubvolume(EDGE3D[i].pt0) < 1) 
            {
               EDGE3D[i].pt0 = help_edgept[0];
               EDGE3D[i].inside_subd3D = 1;      
            }
            else if (PointInSubvolume(EDGE3D[i].pt1) < 1) 
            {
               EDGE3D[i].pt1 = help_edgept[0];
               EDGE3D[i].inside_subd3D = 1;      
            }
         }
      }
   }


   /*************************************************************************/
   /* Investigate if the points 'VERTEX3D[]' lay inside or outside of the   */
   /* subvolume.                                                            */
   /*                                                                       */
   /*  --> Apply function 'PointInSubvolume()'                              */
   /*************************************************************************/
   if (nvertex_nr > 0)
   { 
      for (i=0; i<nvertex_nr; i++)
      {
         VERTEX3D[i].inside_subd3D = 0;   /* set default value */
         if (PointInSubvolume(VERTEX3D[i].pt) > 0)
         {
            VERTEX3D[i].inside_subd3D = 1;
         } 
      }
   }
   return sum_subvol_edges; 
}



/****************************************************************************/
/* subvolume_3D_intersection_TRACE()                                        */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
int subvolume_3D_intersection_TRACE()
{
   int i, j, k, k1, l;
   int help_PointInSubvolume;
   int help_free_polygon = 0;
   int nedge_frac = 4;
   int n_tracept = 0;
  
   struct point help_tracept[2]; 
   struct point sphere_midpt; 
   struct point s_pt_intersect;

   subvolume_type = get_var_integer(uvar, "subvolume_type");
  
   if (subvolume_type == 1)  
   {
      /**********************************************************************/
      /* Description of the subvolume as a regulare prisma.                 */
      /**********************************************************************/
      sum_subvol_edges   = get_var_integer(uvar, "sum_subvol_edges");

      /**********************************************************************/
      /* Allocation of memory for the array:    subvol_bot_pt[]             */
      /*                                        subvol_top_pt[]             */
      /*                                        subvol_side[]               */
      /* AH30.06.99 still TODO: give the allocated memory back !            */
      /**********************************************************************/
      if ((subvol_radius_side = 
             (double *)malloc(sum_subvol_edges * sizeof(double)))== NULL)  
      {
         fprintf(stderr,"Memory allocation failed: 'FACE_net[nface_net].EDGE_NR' \n");
         exit (-1);
      }

      subvol_side_midpt  = AllocateStructPointList(sum_subvol_edges);
      subvol_bot_pt      = AllocateStructPointList(sum_subvol_edges);
      subvol_top_pt      = AllocateStructPointList(sum_subvol_edges);
      subvol_side        = AllocateStructFractureList(sum_subvol_edges);


      /**********************************************************************/
      /*  Calculate the edges, length, ... of the subvol subvolume volume   */
      /*  --> function CalculateSubvolPrismaVariables()  in prisma.c        */
      /**********************************************************************/
      CalculateSubvolPrismaVariables(sum_subvol_edges,
                                     &subvol_radius_bot,
                                     subvol_radius_side,
                                     &subvol_bot_midpt,
                                     &subvol_top_midpt,
                                     subvol_side_midpt,
                                     subvol_bot_pt, 
                                     subvol_top_pt, 
                                     subvol_side); 
   }
   else if (subvolume_type == 2)  
   {
      /**********************************************************************/
      /* Description of the subvolume as a regulare/irregulare quader       */
      /**********************************************************************/
      sum_subvol_edges   = get_var_integer(uvar, "sum_subvol_edges");

      /**********************************************************************/
      /* Allocation of memory for the array:    subvol_bot_pt[]             */
      /*                                        subvol_top_pt[]             */
      /*                                        subvol_side[]               */
      /* AH30.06.99 still TODO: give the allocated memory back !            */
      /**********************************************************************/
      if ((subvol_radius_side = 
             (double *)malloc(sum_subvol_edges * sizeof(double)))== NULL)  
      {
         fprintf(stderr,"Memory allocation failed: 'FACE_net[nface_net].EDGE_NR' \n");
         exit (-1);
      }
      subvol_side_midpt  = AllocateStructPointList(sum_subvol_edges);
      subvol_bot_pt      = AllocateStructPointList(sum_subvol_edges);
      subvol_top_pt      = AllocateStructPointList(sum_subvol_edges);
      subvol_side        = AllocateStructFractureList(sum_subvol_edges);


      /**********************************************************************/
      /*  Calculate the edges, length, ... of the subvolume                 */
      /*  --> function CalculateSubvolQuaderVariables()  in quader.c        */
      /**********************************************************************/
      CalculateSubvolQuaderVariables(sum_subvol_edges,
                                     &subvol_radius_bot,
                                     subvol_radius_side,
                                     &subvol_bot_midpt,
                                     &subvol_top_midpt,
                                     subvol_side_midpt,
                                     subvol_bot_pt, 
                                     subvol_top_pt, 
                                     subvol_side); 
   }
   else 
   {
      fprintf(stdout,"\n The Value 'subvolume_type' = %d is not defined. Check the input file! \n", subvolume_type);
      exit (-1);
   }


   /*************************************************************************/
   /* Investigate trace list 'TRACE' <--> subvolume 3D                      */
   /*                                                                       */
   /*************************************************************************/
   /* First loop: over the TRACE - elements                                 */
   /*                                                                       */
   /*   Second loop: over the subvolume side planes                         */
   /*                                                                       */
   /*     Check, if the trace line and the subvolume side plane cut each    */
   /*     other theoretically                                               */
   /*                                                                       */
   /*     1.) Check, if the two nodes of the trace line lay ...on/inside... */
   /*         the subvolume                                                 */
   /*         YES: TRACE[i].inside_subd3D = 1                               */
   /*         NO : go further on (step 2)                                   */
   /*                                                                       */
   /*                                                                       */
   /*     2a.) calculate for the trace and the plane the radius of the      */
   /*          sphere which surrounds the plane:                            */
   /*          trace -> radius1                                             */
   /*          subvolume side plane -> subvol_radius_side                   */
   /*                                                                       */
   /*     2b.) calculate the distance between the midpoints of the          */
   /*          two spheres                                                  */
   /*                                                                       */
   /*     2c.) if: distance <= (radius1 + subvol_radius_side)               */
   /*          --> yes: calculate the intersection points                   */
   /*                   check, if they lay within the subvolume side planes */
   /*                   (see further on)                                    */
   /*          --> no : forget it and go to the next subvolume side plane   */
   /*                                                                       */
   /*************************************************************************/
   for (i=0; i<ntrace; i++)
   {
      help_free_polygon  = 0;   /*set default value*/
      sum_polypt = 0;           /* set counter of # of polygon points = 0 */

      /**********************************************************************/
      /* Set default values                                                 */
      /*   TRACE[i].inside_subd3D = 0: trace outside subvolume              */
      /**********************************************************************/
      TRACE[i].inside_subd3D = 0;


      /**********************************************************************/
      /* Check, if the two nodes of the trace line lay ...on/inside...      */
      /* the subvolume.                                                     */
      /* --> Apply function 'PointInSubvolume()'                            */
      /*                                                                    */
      /* Sum up the return values of function 'PointInSubvolume()'          */
      /* If 'help_PointInSubvolume' == 2 : all points lay inside subvolume  */
      /*                                   --> TRACE[i].inside_subd3D = 1   */
      /*                                                                    */
      /**********************************************************************/
      help_PointInSubvolume = 0;
      help_PointInSubvolume += PointInSubvolume(TRACE[i].pt[0]);
      help_PointInSubvolume += PointInSubvolume(TRACE[i].pt[1]);

      if (help_PointInSubvolume == 2) TRACE[i].inside_subd3D = 1;

      if (help_PointInSubvolume < 2)
      {
         /*******************************************************************/
         /* Investigate the side planes of the subvolume with respect to    */
         /* intersection points by cutting the trace lines                  */
         /*******************************************************************/
   
         radius1 = 0.5 * TRACE[i].length; 

         /* Calculate  midpoint of the trace line */ 
         center1.x = 0.5 * (TRACE[i].pt[0].x + TRACE[i].pt[1].x);
         center1.y = 0.5 * (TRACE[i].pt[0].y + TRACE[i].pt[1].y);
         center1.z = 0.5 * (TRACE[i].pt[0].z + TRACE[i].pt[1].z);

         n_tracept = 0;    /* set counter for each trace line segment to zero */

         for (j=0; j<sum_subvol_edges; j++)
         {

            /* midpoint of the subvol side plane */
            center2.x = 0.5*(subvol_side[j].pt[2].x+subvol_side[j].pt[0].x);
            center2.y = 0.5*(subvol_side[j].pt[2].y+subvol_side[j].pt[0].y);
            center2.z = 0.5*(subvol_side[j].pt[2].z+subvol_side[j].pt[0].z);

            
            /****************************************************************/
            /* Check, if the subvolume side planes and the trace line are   */
            /* close to each other.                                         */
            /*                                                              */
            /* --> If the distance between the two middle points is smaller */
            /*     than the radius surrounding the subvolume side plane.    */
            /*                                                              */
            /****************************************************************/
            distance_points = 
                         sqrt(  pow((subvol_side_midpt[j].x - center1.x),2)
                              + pow((subvol_side_midpt[j].y - center1.y),2)
                              + pow((subvol_side_midpt[j].z - center1.z),2));


            if ((subvol_radius_side[j]) >= distance_points)
            {                       /* start intersection point calulation */
               /*************************************************************/
               /* check, if trace line cuts the side planes of the          */
               /* subvolume.                                                */
               /* YES: 1.) trace line lays partlly in the subvolume         */
               /*          --> TRACE[i].inside_subd3D = 1                   */
               /*      2.) the tarce line gets new coordinates for the      */
               /*          point laying outside the volume                  */
               /*************************************************************/
               if (intersection_node_line_plane(TRACE[i].pt[0],
                                                TRACE[i].pt[1],
                                                subvol_side[j], 
                                                &s_pt_intersect) == 1)
               {
                  /**********************************************************/
                  /* assign the intersection point 's_pt_intersect' to the  */
                  /* help point 'help_tracept[]'.                           */
                  /* max # of intersection point: n_tracept <= 2             */
                  /**********************************************************/
                  TRACE[i].inside_subd3D = 1;
                  help_tracept[n_tracept] = s_pt_intersect; 
                  n_tracept++; 
               }
            }
         }

  
         if (n_tracept < 2)
         {
            /****************************************************************/
            /* Investigate the 'top' and the 'bottom' plane of the subvolume*/
            /* with respect to intersection points by cutting the trace line*/
            /****************************************************************/

            /* top plane */
            distance_points = sqrt(  pow((subvol_top_midpt.x - center1.x),2)  
                                   + pow((subvol_top_midpt.y - center1.y),2)  
                                   + pow((subvol_top_midpt.z - center1.z),2));  

            if ((subvol_radius_bot) >= distance_points)
            {
               if (1 == intersection_node_line_polygon(TRACE[i].pt[0], 
                                                       TRACE[i].pt[1],
                                                       sum_subvol_edges,
                                                       subvol_top_pt,    
                                                       &s_pt_intersect))
               {
                  TRACE[i].inside_subd3D = 1;
                  help_tracept[n_tracept] = s_pt_intersect; 
                  n_tracept++; 
               }
            }
         }


         if (n_tracept < 2)
         {
            /* bottom plane */
            distance_points = sqrt(  pow((subvol_bot_midpt.x - center1.x),2)  
                                   + pow((subvol_bot_midpt.y - center1.y),2)  
                                   + pow((subvol_bot_midpt.z - center1.z),2));  
 
            if ((subvol_radius_bot) >= distance_points)
            {
               if (1 == intersection_node_line_polygon(TRACE[i].pt[0], 
                                                       TRACE[i].pt[1],
                                                       sum_subvol_edges,
                                                       subvol_bot_pt,    
                                                       &s_pt_intersect))
               {
                     TRACE[i].inside_subd3D = 1;
                     help_tracept[n_tracept] = s_pt_intersect; 
                     n_tracept++; 
               }
            }
         }

         /*******************************************************************/
         /* Assign the new trcae points 'help_tracept[]' to the line segment*/
         /* as new the new points.                                          */
         /*******************************************************************/
         if (n_tracept == 2)
         {
            TRACE[i].pt[0] = help_tracept[0]; 
            TRACE[i].pt[1] = help_tracept[1]; 
            /* TRACE[i].inside_subd3D = 1;      */
         }
         if (n_tracept == 1)
         {
            /****************************************************************/
            /* Only one new trace point --> help_tracept[0]                 */
            /* Which one of the two trcae points lays outside the subvolume?*/
            /* --> only one old trace point lays outside the subvolume      */
            /* --> Apply function 'PointInSubvolume()'                      */
            /****************************************************************/
            if (PointInSubvolume(TRACE[i].pt[0]) < 1) 
            {
               TRACE[i].pt[0] = help_tracept[0];
               TRACE[i].inside_subd3D = 1;      
            }
            else if (PointInSubvolume(TRACE[i].pt[1]) < 1) 
            {
               TRACE[i].pt[1] = help_tracept[0];
               TRACE[i].inside_subd3D = 1;      
            }
         }
      }
   }


   /*************************************************************************/
   /* Investigate if the points 'VERTEX3D[]' lay inside or outside of the   */
   /* subvolume.                                                            */
   /*                                                                       */
   /*  --> Apply function 'PointInSubvolume()'                              */
   /*************************************************************************/
   if (nvertex_nr > 0)
   { 
      for (i=0; i<nvertex_nr; i++)
      {
         VERTEX3D[i].inside_subd3D = 0;   /* set default value */
         if (PointInSubvolume(VERTEX3D[i].pt) > 0)
         {
            VERTEX3D[i].inside_subd3D = 1;
         } 
      }
   }
   return sum_subvol_edges; 
}



/****************************************************************************/
/*                                                                          */
/*                             FUNCTIONS                                    */
/*                                                                          */
/****************************************************************************/
/****************************************************************************/
/* center_point()                                                           */
/*   calculate the middle point of the fracture plane                       */
/*   (applying the FRAC - list !)                                           */
/****************************************************************************/
struct point center_point(int i, struct fracture *FRAC)
{
   struct point help_mid_pt;

   help_mid_pt.x = 0.5 * (FRAC[i].pt[0].x + FRAC[i].pt[2].x);
   help_mid_pt.y = 0.5 * (FRAC[i].pt[0].y + FRAC[i].pt[2].y);
   help_mid_pt.z = 0.5 * (FRAC[i].pt[0].z + FRAC[i].pt[2].z);

   return(help_mid_pt);
}


/****************************************************************************/
/* PointIsOnLineSegment_2D()                                                */
/*   check, if point C lays on a line segment. The line segment is defined  */
/*   by the two points A and B                                              */
/*                                                                          */
/* Comment: AH, 05.07.99                                                    */
/*          function got from Volker Reichenberger                          */
/*          function from 'http://www.exaflop.org/docs/cgafaq/...'          */
/*                                                                          */
/*      Modification, AH 17.05.2001                                         */
/*                                                                          */
/*      Wenn der Punkt C auf der Geraden, die durch AB geht liegt, und die  */
/*      Gerade parallel zu einer der Koordinatenachsen verlaeuft, wird der  */
/*      's' zu Null: s=0                                                    */ 
/*      Dies bedeutet jedoch per Definition, dass C innerhalb AB liegt, was */
/*      aber nur dann stimmt, wenn auch gilt: 0<r<1 = P is interior to AB   */
/*      --> folgendes wurde eingefuegt:                                     */
/*          wenn      [(s=0) und (0<=r<=1)]          -> return (s*L)        */
/*          wenn aber [(s=0) und ((r<0) oder (r>1))] -> return (r*L)        */
/*                                                                          */
/*                                                                          */
/* Return: index_return =0: C does not lay interior to line segment AB      */
/*                      =1: C does lays interior to line segment AB         */
/*                                                                          */
/****************************************************************************/
int PointIsOnLineSegment_2D(double Cx, double Cy, 
                            double Ax, double Ay, 
                            double Bx, double By)
{
  int index_return=0;

  double L,r,Px,Py,s;

  /*
  Let the point be C (Cx,Cy) and the line be AB (Ax,Ay) to (Bx,By).
  The length of the line segment AB is L:
  */

  L = sqrt( (Bx-Ax)*(Bx-Ax) + (By-Ay)*(By-Ay) );

  /*
  Let P be the point of perpendicular projection of C onto AB.
  Let r be a parameter to indicate P's location along the
  line containing AB, with the following meaning:

           r=0      P = A
           r=1      P = B
           r<0      P is on the backward extension of AB
           r>1      P is on the forward extension of AB
           0<r<1    P is interior to AB

  Compute r with this:
  */
  r = ((Ay-Cy)*(Ay-By)-(Ax-Cx)*(Bx-Ax))/(L*L);

  /* The point P can then be found: */
  Px = Ax + r*(Bx-Ax);
  Py = Ay + r*(By-Ay);

  /*
  And the distance from A to P = r*L.

  Use another parameter s to indicate the location along PC, 
  with the following meaning:

          s<0      C is left of AB
          s>0      C is right of AB
          s=0      C is on AB

  Compute s as follows:
  */
  s = ((Ay-Cy)*(Bx-Ax)-(Ax-Cx)*(By-Ay))/(L*L);
     

  /* Then the distance from C to P = s*L. */
  /* old return value:
     return s*L;
  */

  if ((fabs(s)<=epsilon_0) && ((0<=r) || (r<=1)))  {
     index_return = 1;
  }
  else index_return = 0;
}


/****************************************************************************/
/* PointIsInPolygon_2D()                                                    */
/*                                                                          */
/*                                                                          */
/*    The algorithm for PointIsInPolygon_2D is from the                     */
/*    comp.graphics.algorithms Frequently Asked Questions.                  */
/*    (November 1999: http://www.exaflop.org/docs/cgafaq/                   */
/*                                                                          */
/* Subject 2.03: How do I find if a point lies within a polygon?            */
/*                                                                          */
/* The definitive reference is "Point in Polyon Strategies" by Eric Haines  */
/* [Gems IV] pp. 24-46.                                                     */
/*                                                                          */
/* The essence of the ray-crossing method is as follows. Think of standing  */
/* inside a field with a fence representing the polygon. Then walk north.   */
/* If you have to jump the fence you know you are now outside the poly.     */
/* If you have to cross again you know you are now inside again; i.e., if   */
/* you were inside the field to start with, the total number of fence jumps */
/* you would make will be odd, whereas if you were ouside the jumps will be */
/* even.                                                                    */
/*                                                                          */
/* The code below is from Wm. Randolph Franklin <wrf@ecse.rpi.edu> with     */
/* some minor modifications for speed. It returns 1 for strictly interior   */
/* points, 0 for strictly exterior, and 0 or 1 for points on the boundary.  */
/* The boundary behavior is complex but determined; | in particular, for a  */
/* partition of a region into polygons, each point | is "in" exactly one    */
/* polygon. See the references below for more = detail.                     */
/*                                                                          */
/* The code was modified only inasfar as it now uses other data             */
/* strucutures.                                                             */
/*                                                                          */
/*                                                                          */
/* RETURN: = 1  for strictly interior points                                */
/*         = 0  for strictly exterior                                       */
/*         = 0 or 1 for points on the boundary                              */
/*                                                                          */
/****************************************************************************/
int PointIsInPolygon_2D(int npol, 
                        struct point *pt_polyline, 
                        double x, double y)
{
   int i, j, index_return = 0;

   for (i = 0, j = npol-1; i < npol; j = i++) 
   {
      if (   (   ((pt_polyline[i].y <= y) && (y < pt_polyline[j].y )) 
              || ((pt_polyline[j].y <= y) && (y < pt_polyline[i].y ))) 
          &&  (x <   (pt_polyline[j].x - pt_polyline[i].x) 
                   * (y - pt_polyline[i].y) 
                   / (pt_polyline[j].y - pt_polyline[i].y) 
                   + pt_polyline[i].x )
         )

       index_return = !index_return;
   }
   return index_return;
}


/****************************************************************************/
/* PointIsInQuadrilateral_3D()                                              */
/*                                                                          */
/* Purpose:                                                                 */
/*   Check, if Point A lays always left of the line segments of the polygon.*/
/*   Doing this by walkin along the polyline counter clockwise.             */
/*                                                                          */
/*          pt3             pt2                                             */
/*            ---------------                                               */
/*            |             |                                               */
/*             |            |                                               */
/*              |     * A  |                                                */
/*              |__________|                                                */
/*             pt0        pt1                                               */
/*                                                                          */
/*   For example line segment pt0-pt1:                                      */
/*   1.) Does point A lays on the line pt0-pt1 and also within the          */
/*       segment area pt0-pt1?                                              */
/*       Yes: index_return = 1                                              */
/*       No : go futher on ...                                              */
/*                                                                          */
/*   2.) direction vector  a = pt0 --> pt1                                  */
/*       direction vector  b = pt0 --> A                                    */
/*   3.) vector product    c = (a)x(b)                                      */
/*   4.) Vector 'c' and the normal vector of the plane 'n' are collinear    */
/*       Check, if they point in the same direction:  c = lambda * n        */
/*                                                                          */
/*       if (lambda >= 0) --> 'c' and 'n' point in the same direction       */
/*                            A lays left to the line segment pt0-pt1       */
/*                                                                          */
/*       if (lambda <  0) --> 'c' and 'n' point in the opposite direction   */
/*                            A lays right to the line segment pt0-pt1.     */
/*                            This means that A lays outside the plane.     */
/*                            index_return = 0                              */
/*                                                                          */
/* Return: = 4  for strictly interior points and points laying on the       */
/*              line segments                                               */
/*         > 3  points are strictly outside                                 */
/*                                                                          */
/****************************************************************************/
int PointIsInQuadrilateral_3D(struct point A, 
                              struct point node[4],
                              struct point n)
{
   int i, i1, npoint = 4, index_return = 0;
   double direction;
   double d1, d2, d3;
   struct point a, b, c;

   for (i=0; i<npoint; i++) 
   {
      i1 = i+1;
      if (i == (npoint-1)) i1 = 0;

      /*AH, 18.02.2000
        if (abs_distance_point_line(&node[i], &node[i1], &A) <= epsilon_0)
      */

      if (abs_distance_point_line(&node[i], &node[i1], &A) <= epsilon_checkPoints)
      /*point A lays on the line ...*/
      {
         d1 = abs_vec_pt_pt(&node[i],&node[i1]);
         d2 = abs_vec_pt_pt(&node[i],&A);
         d3 = abs_vec_pt_pt(&node[i1],&A);

         /*AH,17.05.2001, raus: dies fuehrt bei kleinen numerischen Ungenauigkeiten
           zu Fehlern!!
           if ((d2+d3) > d1) i
         */ 

         if ((d2<=d1) && (d3<=d1)) { /*point A lays between node[i] and node[i1]*/
            index_return = 4;
            break;
         }
         else  {                 /*point A lays not between node[i] and node[i1]*/
            index_return = 0;    
         }
      }

      /*point A lays not on the line (node[i]--node[i1])*/
      else
      {
         a.x = node[i1].x - node[i].x;
         a.y = node[i1].y - node[i].y;
         a.z = node[i1].z - node[i].z;

         b.x = A.x - node[i].x;  
         b.y = A.y - node[i].y;  
         b.z = A.z - node[i].z;   

         c.x = (a.y*b.z - a.z*b.y);      /*vector product  --> normal vector*/
         c.y = (a.z*b.x - a.x*b.z);
         c.z = (a.x*b.y - a.y*b.x);

         /*******************************************************************/
         /* Check, if the normal vector 'n' of the plane and the normal     */
         /* vector 'c' ...                                                  */
         /*                                                                 */
         /*  ... are parallel    : AreTwoLinesParallel() = 0                */
         /*                                                                 */
         /*  ... point in the same direction                                */
         /*        direction <  'c' points in the opposite direction as 'n' */
         /*        direction >= 'c' points in the same direction as 'n'     */
         /*                                                                 */
         /*                                                                 */
         /*******************************************************************/
         if (AreTwoLinesParallel(c, n, &direction) == 0)  
         {
            if (direction > 0)  index_return++;
         }
      }
   }
   return index_return;
}


/****************************************************************************/
/* RotationMatrix3D()                                                       */
/*                                                                          */
/*   PURPOSE:                                                               */
/*   Calculate the spherical rotation matrix of a cartesian coordinate      */
/*   system around its origin. Therefor, nine angles has to be calculated.  */
/*   (see: Bronstein, p. 217)                                               */
/*                                                                          */
/*                                                                          */
/*   ARGUMENTS of the function: three points of the fracture plane          */
/*                              normal vector of the fracture plane         */
/*                              rotation matrix ROT[3][3] (has to be filled)*/
/*                                                                          */
/*   --> They form the three axis of the new cartesian coordinate system    */
/*       new X-axis: direction vector between FRAC point pt[0] and pt[1]    */
/*       new Y-axis: direction vector between FRAC point pt[0] and pt[3]    */
/*       new Z-axis: norm vector of the FRAC plane                          */
/*                                                                          */
/*                                                                          */
/*   --> Rotation matrix A[3][3]                                            */
/*                                                                          */
/*              A[0][0]=cos(x',x)   A[0][1]=cos(y',x)   A[0][2]=cos(z',x)   */
/*   A[3][3] =  A[1][0]=cos(x',y)   A[1][1]=cos(y',y)   A[1][2]=cos(z',y)   */
/*              A[2][0]=cos(x',z)   A[2][1]=cos(y',z)   A[2][2]=cos(z',z)   */
/*                                                                          */
/*                                                                          */
/*   RETURN value: spherical rotation matrix                                */
/*                                                                          */
/****************************************************************************/
void RotationMatrix3D(struct point node0, struct point node1, 
                      struct point node3, struct point normal,
                      double A[3][3])                                  
{
   struct point e1, e2, e3;
   struct point n1, n2, n3;

   
   /*************************************************************************/
   /* 'old' cartesian coordinate system: declare the axis e1, e2, e3        */
   /*************************************************************************/
   e1.x = e2.y = e3.z = 1;
   e1.y = e1.z = e2.x = e2.z = e3.x = e3.y = 0;

   /*************************************************************************/
   /* 'new' cartesian coordinate system: declare the axis n1, n2, n3        */
   /*                                    based on the 'old' point vectors   */
   /*************************************************************************/
   n1.x = node1.x - node0.x; 
   n1.y = node1.y - node0.y;
   n1.z = node1.z - node0.z;

   n2.x = node3.x - node0.x;
   n2.y = node3.y - node0.y;
   n2.z = node3.z - node0.z;

   n3.x = normal.x; 
   n3.y = normal.y;
   n3.z = normal.z;


   /*************************************************************************/
   /* rotation matrix A[][]                                                 */
   /*************************************************************************/

   A[0][0] = CosOfTwoVectors(n1, e1);
   A[0][1] = CosOfTwoVectors(n2, e1);
   A[0][2] = CosOfTwoVectors(n3, e1);

   A[1][0] = CosOfTwoVectors(n1, e2);
   A[1][1] = CosOfTwoVectors(n2, e2);
   A[1][2] = CosOfTwoVectors(n3, e2);

   A[2][0] = CosOfTwoVectors(n1, e3);
   A[2][1] = CosOfTwoVectors(n2, e3);
   A[2][2] = CosOfTwoVectors(n3, e3);


}


/****************************************************************************/
/* CosOfTwoVectors()                                                        */
/*                                                                          */
/*   PURPOSE: calculate the angle between two vectors                       */
/*                                                                          */
/*   ARGUMENTS of the function: two vectors                                 */
/*                                                                          */
/*   RETURN value: angle alpha [rad]                                        */
/*                                                                          */
/****************************************************************************/
double CosOfTwoVectors(struct point a, struct point b)
{
   double cos;

   cos =   (a.x*b.x + a.y*b.y + a.z*b.z) 
         / (  sqrt(a.x*a.x + a.y*a.y + a.z*a.z)
            * sqrt(b.x*b.x + b.y*b.y + b.z*b.z));

   return (cos);
}


/****************************************************************************/
/* Point1_equal_Point2()                                                    */
/*                                                                          */
/*   PURPOSE: compare to points if they are equal                           */
/*                                                                          */
/*   ARGUMENTS of the function: two points of the type 'struct point'       */
/*                                                                          */
/*   RETURN value: int index_equal = 0 : not equal                          */
/*                                 = 1 : equal                              */
/*                                                                          */
/****************************************************************************/
int Point1_equal_Point2(struct point a, struct point b)
{
  int index_equal = 0;
  /*AH; declared in subvolume3D.h:*/
  /*double epsilon_Point1_equal_Point2 = 10e-4;*/
  /*TODO: epsilon_Wert in Headerfile oder Eingabefile*/
  double epsilon_Point1_equal_Point2 = 10e-10;

  if (   (fabs(a.x - b.x) < epsilon_Point1_equal_Point2) 
      && (fabs(a.y - b.y) < epsilon_Point1_equal_Point2)
      && (fabs(a.z - b.z) < epsilon_Point1_equal_Point2))  
  {
     index_equal = 1; 
  }
 
  return index_equal; 
}


/****************************************************************************/
/* Point1_equal_Point2_epsilonlarge()                                       */
/*                                                                          */
/*   PURPOSE: compare to points if they are equal                           */
/*                                                                          */
/*   ARGUMENTS of the function: two points of the type 'struct point'       */
/*                                                                          */
/*   RETURN value: int index_equal = 0 : not equal                          */
/*                                 = 1 : equal                              */
/*                                                                          */
/****************************************************************************/
int Point1_equal_Point2_epsilonlarge(struct point a, struct point b)
{
  int index_equal = 0;
  /*AH; declared in subvolume3D.h:*/
  /*double epsilon_Point1_equal_Point2 = 10e-4;*/
  /*TODO: epsilon_Wert in Headerfile oder Eingabefile*/
  double epsilon_Point1_equal_Point2 = 10e-5;

  if (   (fabs(a.x - b.x) < epsilon_Point1_equal_Point2) 
      && (fabs(a.y - b.y) < epsilon_Point1_equal_Point2)
      && (fabs(a.z - b.z) < epsilon_Point1_equal_Point2))  
  {
     index_equal = 1; 
  }
 
  return index_equal; 
}




/****************************************************************************/
/* PointInSubvolume()                                                       */
/*                                                                          */
/*   PURPOSE: Investigate if the point pt_intersect[] lay on the            */
/*            surface of the subvol or inside the subvol volume             */
/*                                                                          */
/*                                                                          */
/*            1.) Filter out the point, which lays outside the              */
/*                subvol volume volume:                                     */
/*                                                                          */
/*            1.a) If point lays within (bot.z <= point.z <= top.z)         */
/*                 the range of the z-coordinates of the subvol             */
/*                 --> investigate step 1.b)                                */
/*                                                                          */
/*                 If point lays not within the range                       */
/*                 --> point will not be added to the list                  */
/*                     'polygon_frac'                                       */
/*                                                                          */
/*            1.b)                                                          */
/*            1.b1) Investigate if point lays on the surface of             */
/*                  the subvol (on the 'polygon_subvol' line)               */
/*                                                                          */
/*              --> Apply the function 'PointIsOnLineSegment_2D'            */
/*                  Calculate the distance of a point to a line.            */
/*                                                                          */
/*                  If (fabs(distance) <= epsilon_0):                       */
/*                  --> yes: index_add = 1                                  */
/*                  --> no : investigate step 1.b2)                         */
/*                                                                          */
/*              Remark:                                                     */
/*              The point, which has to be checked, lays within             */
/*              the z-spread of the subvol. So, the problem is              */
/*              reduced to a 2D problem (x and y coordinates)               */
/*                                                                          */
/*                                                                          */
/*            1.b2) Investigate if point lays inside or outside             */
/*                  the polyline 'polygon_subvol'                           */
/*              -->  Apply the algorithm "PointIsInPolygon_2D":             */
/*                                                                          */
/*                   If 'PointIsInPolygon_2D' == 0: outside the line        */
/*                   If 'PointIsInPolygon_2D' != 0: inside the line         */
/*                       --> index_add = 1                                  */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/*   ARGUMENTS:                                                             */
/*                                                                          */
/*   RETURN value:   index_add  = 0 : node lays NOT on/inside subvolume     */
/*                                    (--> do not add NODE to list)         */
/*                                                                          */
/*                   index_add  = 1 : node lays on/inside subvolume         */
/*                                    (--> add NODE to list)                */
/*                                                                          */
/****************************************************************************/
int PointInSubvolume(struct point node) 
{
   int i, i1;  
   int index_add = 0;
   int index_help;

   double distance_xx1;                /* return value of the function 
                                          'PointIsOnLineSegmentes_2D()' */

   /*************************************************************************/
   /* 1.a) If point lays within (bot.z <= point.z <= top.z)                 */
   /*      the range of the z-coordinates of the subvol                     */
   /*      --> investigate step 1.b)                                        */
   /*                                                                       */
   /*************************************************************************/
   if ((subvol_bot_midpt.z <= node.z) && (node.z  <= subvol_top_midpt.z))  
   {
      /**********************************************************************/  
      /* 1.b1)                                                              */
      /*                                                                    */
      /*   Remark:                                                          */
      /*   The point, which has to be checked, lays within the z-spread of  */
      /*   the subvol. So, the problem is reduced to a 2D problem           */
      /*   (x and y coordinates)                                            */
      /*                                                                    */
      /**********************************************************************/  
      for (i=0; i<sum_subvol_edges; i++)
      {
         i1 = i+1;
         if (i == (sum_subvol_edges-1)) i1=0;

         if (PointIsOnLineSegment_2D(node.x, node.y,
                                     subvol_bot_pt[i].x, 
                                     subvol_bot_pt[i].y, 
                                     subvol_bot_pt[i1].x, 
                                     subvol_bot_pt[i1].y) == 1) 
         {
            index_add = 1; 
         }
      }


      /**********************************************************************/  
      /* 1.b2) Investigate if point lays inside or outside the polyline     */
      /*       'polygon_subvol'                                             */
      /**********************************************************************/  
      if (index_add == 0)
      {
         index_help = PointIsInPolygon_2D(sum_subvol_edges, 
                                          subvol_bot_pt, 
                                          node.x, node.y);

         if (index_help != 0)  index_add = 1; 

      }        
   }        
   return (index_add);
}        


/****************************************************************************/
/* AddToPolygonList()                                                       */
/*                                                                          */
/*   PURPOSE     : allocate memory for the dynamic list 'polygon_frac'      */
/*   ARGUMENTS   : int *sum_polyp    = # of elements of the list            */
/*                 struct point node = node element which has to be added   */
/*                                     to the list                          */
/*   RETURN value: list ´polygon_frac´                                      */
/*                                                                          */
/****************************************************************************/
struct poly_point *AddToPolygonList(int *sum_polypt,
                                    struct point node)
{
   int i, n;
   n = *sum_polypt; 


   if (n == 0)
   {
      if ((polygon_frac = 
          (struct poly_point *)malloc(2*sizeof(struct poly_point))) == NULL)
      {
         fprintf(stderr,"Memory allocation failed for ");
         fprintf(stderr,"struct poly_point *polygon_frac \n");
         exit (-1);
      }
      i = n;
      polygon_frac[i].pt_nr  = n;
      polygon_frac[i].glob.x = node.x; 
      polygon_frac[i].glob.y = node.y; 
      polygon_frac[i].glob.z = node.z; 
      polygon_frac[i+1].pt_nr = -1;
      *sum_polypt = n+1;
   }        

   else
   {
      /****************************************************************/  
      /* AH 16.10.1999: where ist the check????                       */
      /* Check, if the node already exists in the list 'polygon_frac' */
      /*                                                              */
      /* YES: do not add node to the list 'polygon_frac'              */
      /* NO : allocate memory and add the node to the list            */ 
      /*                                                              */
      /****************************************************************/  
      polygon_frac = (struct poly_point *)realloc(polygon_frac,
                             (n+2)*sizeof(struct poly_point));
      if(polygon_frac == NULL)
      {
         fprintf(stderr,"Memory allocation failed for ");
         fprintf(stderr,
              "struct poly_point *polygon_frac (subvolume_3D.c)\n");
         exit (-1);
      }
      i = *sum_polypt;
      polygon_frac[i].pt_nr  = n;
      polygon_frac[i].glob.x = node.x; 
      polygon_frac[i].glob.y = node.y; 
      polygon_frac[i].glob.z = node.z; 
      polygon_frac[i+1].pt_nr = -1;
      *sum_polypt = n+1;
   }
   return (polygon_frac);
}




/****************************************************************************/
/* AllocateStructFractureList()                                             */
/*                                                                          */
/*   PURPOSE     : allocate memory for a dynamic list of type               */
/*                 'struct fracture'                                        */
/*   ARGUMENTS   : int n  = # size of the list ( ... # of elements)         */
/*   RETURN value: list ´fracture_list´                                     */
/*                                                                          */
/****************************************************************************/
struct fracture *AllocateStructFractureList(int n)
{
   struct fracture *fracture_list;

   fracture_list = (struct fracture *)malloc(n * sizeof(struct fracture));

   if (fracture_list == NULL)
   {
      fprintf(stderr,"Nicht genuegend Speicher fuer fracture_list (Function AddToFractureList)\n");
      exit (-1);
   }
   return (fracture_list);
}


      

/****************************************************************************/
/* CoordinateTransformation()                                               */
/*                                                                          */
/*   PURPOSE: Transformate the coordinate values into the new cartesian     */
/*            coordinate system (= in the locale coordinate system)         */
/*                                                                          */
/*            First step : translation of the point p_old with respect      */
/*                to the new origin (located at point pt[0])                */
/*                --> new coordinates of point p_new1:                      */
/*                                                                          */
/*                    p_new1.x = p_old.x - pt[0].x                          */
/*                    p_new1.x = p_old.x - pt[0].y                          */
/*                    p_new1.x = p_old.x - pt[0].z                          */
/*                                                                          */
/*                here: b[] = polygon_frac[].glob - pt[0]                   */
/*                                                                          */
/*            Second step: rotation of the point p_new1 around the new      */
/*                origin with the rotation matrix ROT[][]                   */
/*                (rotation matrix ROT[][] is already its inverse)          */
/*                                                                          */
/*                New coordinates of point 'polygon_frac[].loc'             */
/*                polygon_frac[].loc   = b[] * ROT[][]                      */
/*                                                                          */
/*   ARGUMENTS                                                              */
/*                                                                          */
/*   RETURN value:                                                          */
/*                                                                          */
/****************************************************************************/
void CoordinateTransformation(int i, struct poly_point *polygon_frac, 
                              struct point O, 
                                 double ROT_i[3][3]) 
{
   struct point b;   /*help variable */

   /* first step */
   b.x = polygon_frac[i].glob.x - O.x;
   b.y = polygon_frac[i].glob.y - O.y; 
   b.z = polygon_frac[i].glob.z - O.z; 

   /* second step */
   polygon_frac[i].loc.x =   b.x*ROT_i[0][0] + b.y*ROT_i[0][1] 
                           + b.z*ROT_i[0][2];

   polygon_frac[i].loc.y =   b.x*ROT_i[1][0] + b.y*ROT_i[1][1] 
                           + b.z*ROT_i[1][2];

   polygon_frac[i].loc.z =   b.x*ROT_i[2][0] + b.y*ROT_i[2][1] 
                           + b.z*ROT_i[2][2];
}


/****************************************************************************/
/* CoordinateBackTransformation()                                           */
/*                                                                          */
/*   PURPOSE: Transformate the coordinate values from the local cartesian   */
/*            coordinate system back to the global coordinate system.       */
/*                                                                          */
/*            First step : back rotation, rotation matrix ROT[][]           */
/*                                                                          */
/*            Second step: move origin of the coordinate system back to the */
/*                         origin of the global coordinate system.          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void CoordinateBackTransformation(int i, struct poly_point *polygon_frac, 
                                  struct point O, 
                                  double ROT[3][3]) 
{
   struct point b;  /* help variable */

   /* first step */
   b.x =   polygon_frac[i].loc.x*ROT[0][0] + polygon_frac[i].loc.y*ROT[0][1] 
         + polygon_frac[i].loc.z*ROT[0][2];

   b.y =   polygon_frac[i].loc.x*ROT[1][0] + polygon_frac[i].loc.y*ROT[1][1] 
         + polygon_frac[i].loc.z*ROT[1][2];

   b.z =   polygon_frac[i].loc.x*ROT[2][0] + polygon_frac[i].loc.y*ROT[2][1] 
         + polygon_frac[i].loc.z*ROT[2][2];

   /* second step */
   polygon_frac[i].glob.x = O.x + b.x;
   polygon_frac[i].glob.y = O.y + b.y; 
   polygon_frac[i].glob.z = O.z + b.z; 
}



/****************************************************************************/
/* CoordinateBackTransformation()                                           */
/*                                                                          */
/*   PURPOSE: Transformate the coordinate values from the local cartesian   */
/*            coordinate system back to the global coordinate system.       */
/*                                                                          */
/*            First step : back rotation, rotation matrix ROT[][]           */
/*                                                                          */
/*            Second step: move origin of the coordinate system back to the */
/*                         origin of the global coordinate system.          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void NewFractureBorders_inSubvolume_TouchEachOther(int *nvertex_nr)
{
   int i, i1, j, j1, k, k1, l, l1;

   struct edge line[2];

   for (i=0; i<(nfrac-1); i++)
   {
      i1 = i+1;

      if (FRAC[i].index_ch == -1)       /*fracture [i] is still a rectangle*/
      {
         for (j=0; j<4; j++) 
         {
            j1=j+1; 
            if (j1==4) j1=0;

            line[0].pt0 = FRAC[i].pt[j];
            line[0].pt1 = FRAC[i].pt[j1];

           
            for(k=(i+1); k<nfrac; k++)
            {
               if (FRAC[k].index_ch == -1)     /*fracture [k] is a rectangle*/
               {
                  for (l=0; l<4; l++) 
                  {
                     l1=l+1; 
                     if (l1==4) l1=0;

                     line[1].pt0 = FRAC[k].pt[l];
                     line[1].pt1 = FRAC[k].pt[l1];


                     if (intersection_node_line_line(line,&s_pt_intersect)==1)
                     {
                        /****************************************************/
                        /*assign single intersetion point to list 'VERTEX3D'*/
                        /****************************************************/
                        VERTEX3D = 
                             add_VERTEX3D_to_list(*nvertex_nr, s_pt_intersect);
                        (*nvertex_nr)++;
                     }
                  }
               }

               else if (FRAC[k].index_ch == 1)    /*fracture [k] is a polygon*/
               {
                  for (l=0; l<FRAC[k].sum_ptch; l++) 
                  {
                     l1=l+1; 
                     if (l1==FRAC[k].sum_ptch) l1=0;

                     line[1].pt0 = FRAC[k].ch[l];
                     line[1].pt1 = FRAC[k].ch[l1];


                     if (intersection_node_line_line(line,&s_pt_intersect)==1)
                     {
                        /****************************************************/
                        /*assign single intersetion point to list 'VERTEX3D'*/
                        /****************************************************/
                        VERTEX3D = 
                             add_VERTEX3D_to_list(*nvertex_nr, s_pt_intersect);
                        (*nvertex_nr)++;
                     }
                  }
               }
            }
         }
      }

         
      else if (FRAC[i].index_ch == 1)           /*fracture [i] is a polygon*/
      {
         for (j=0; j<FRAC[i].sum_ptch; j++) 
         {
            j1=j+1; 
            if (j1==FRAC[i].sum_ptch) j1=0;

            line[0].pt0 = FRAC[i].ch[j];
            line[0].pt1 = FRAC[i].ch[j1];

           
            for(k=(i+1); k<nfrac; k++)
            {
               if (FRAC[k].index_ch == -1)     /*fracture [k] is a rectangle*/
               {
                  for (l=0; l<4; l++) 
                  {
                     l1=l+1; 
                     if (l1==4) l1=0;

                     line[1].pt0 = FRAC[k].pt[l];
                     line[1].pt1 = FRAC[k].pt[l1];


                     if (intersection_node_line_line(line,&s_pt_intersect)==1)
                     {
                        /****************************************************/
                        /*assign single intersetion point to list 'VERTEX3D'*/
                        /****************************************************/
                        VERTEX3D = 
                             add_VERTEX3D_to_list(*nvertex_nr, s_pt_intersect);
                        (*nvertex_nr)++;
                     }
                  }
               }

               else if (FRAC[k].index_ch == 1)    /*fracture [k] is a polygon*/
               {
                  for (l=0; l<FRAC[k].sum_ptch; l++) 
                  {
                     l1=l+1; 
                     if (l1==FRAC[k].sum_ptch) l1=0;

                     line[1].pt0 = FRAC[k].ch[l];
                     line[1].pt1 = FRAC[k].ch[l1];


                     if (intersection_node_line_line(line,&s_pt_intersect)==1)
                     {
                        /****************************************************/
                        /*assign single intersetion point to list 'VERTEX3D'*/
                        /****************************************************/
                        VERTEX3D = 
                             add_VERTEX3D_to_list(*nvertex_nr, s_pt_intersect);
                        (*nvertex_nr)++;
                     }
                  }
               }
            }
         }
      }

      else {
         fprintf(stderr,"subvolume3D.c: FRAC[i].index_ch !=(+1) and !=(-1)");
      }
   }
}
