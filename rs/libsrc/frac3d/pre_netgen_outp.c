/****************************************************************************/
/*                                                                          */
/* File:      pre_netgen_outp.c                                             */
/*                                                                          */
/* Purpose:                                                                 */
/*                                                                          */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/* Functions: void PreNetgenOutput_2dFRAC_in3d()                            */
/*            void PreNetgenOutput_1dTRACE_in3d()                           */
/*            void PreNetgenOutput_1dTRACE_in2d()                           */
/*                                                                          */
/*                                                                          */
/*            int DistanceCompare()                                         */
/*            struct vertex_net *AllocateStructVertex_NetList()             */
/*            struct edge_net *AllocateStructEdge_NetList()                 */
/*            struct face_net *AllocateStructFace_NetList()                 */
/*            struct element_net *AllocateStructElement_NetList()           */
/*            struct edge_distance *AllocateStructEdgeDistanceList()        */
/*            struct trace *AllocateStructTraceList()                       */
/*            void write_output_Subplane_tecplot()                          */
/*            void write_output_Subplane_netgenerator()                     */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "functions.h" 
#include "pre_netgen_outp.h"


/****************************************************************************/
/* PreNetgenOutput_FRAC()                                                   */
/*                                                                          */
/****************************************************************************/
void PreNetgenOutput_2dFRAC_in3d(int sum_subvol_edges)                    
{

   int i, i1, i2, i3, j, j1, k, k1, l, l1;
   int add_help, add_help_edge, add_help0, add_help1;
   int nrectangle   =  4;
   int nedgepoints  =  2;
 

   /* set default values for the start numbers for the controll words */
   ncw_edge = 1;                                   /* subvolume edges */
   ncw_face = 1;                                   /* subvolume face  */
   ncw_fracture = -1;                              /* fracture face   */
   ncw_fracture_edge = -1;                         /* fracture edges  */


   /*************************************************************************/
   /* I.)   Sum up the total  # of vertexs                                  */
   /*                  total  # of edges                                    */
   /*                  total  # of faces                                    */
   /*************************************************************************/
   nvertex_total = nedge_total = nface_total = nelement_total = 0;

   /*************************************************************************/
   /* I.1.) Subvolume                                                       */
   /*************************************************************************/
   nvertex_total  = 2 * sum_subvol_edges;
   nedge_total    = 3 * sum_subvol_edges;
   nface_total    = sum_subvol_edges + 2;
   nelement_total = 1;

   /*************************************************************************/
   /* I.2.) fracture elements FRAC                                          */
   /*       --> just the one with FRAC[].inside_subd3D = +1                 */
   /*************************************************************************/
   for (i=0; i<nfrac; i++)
   {
      if (FRAC[i].inside_subd3D > 0)
      {
         if (FRAC[i].index_ch == -1)    /* fracture are still rectangles */
         {
            nvertex_total += nrectangle;
            nedge_total   += nrectangle;
            nface_total++;
         
         }
         /* fractures are described as polygon (convex hull) */
         else if (FRAC[i].index_ch == 1)  
         {
            nvertex_total += FRAC[i].sum_ptch;
            nedge_total   += FRAC[i].sum_ptch;
            nface_total++;
         }
         else  fprintf(stderr,"PreNetgenOutput: FRAC[i].index_ch !=(+1) and !=(-1)");
      }   
   }


   /*************************************************************************/
   /* I.3.) edge elements EDGE3D                                            */
   /*       --> just the one with EDGE3D.inside_subd3D = +1                 */
   /*************************************************************************/
   for (i=0; i<edge_nr_3D; i++)
   {
      if (EDGE3D[i].inside_subd3D > 0)
      {
         nvertex_total += nedgepoints;
         nedge_total++;
      }   
   }

   /*************************************************************************/
   /* I.4.) vertex points VERTEX3D                                          */
   /*       --> just the one with VERTEX3D.inside_subd3D = +1               */
   /*************************************************************************/
   for (i=0; i<nvertex_nr; i++)
   {
      if (VERTEX3D[i].inside_subd3D > 0)
      {
         nvertex_total++; 
      }   
   }



   /*************************************************************************/
   /* II.) Allocate memory for the new listings:  VERTEX_net                */
   /*                                             EDGE_net                  */
   /*                                             FACE_net                  */
   /*                                             ELEMENT_net               */
   /*************************************************************************/
   VERTEX_net  = AllocateStructVertex_NetList(nvertex_total);
   EDGE_net    = AllocateStructEdge_NetList(nedge_total);
   FACE_net    = AllocateStructFace_NetList(nface_total);
   ELEMENT_net = AllocateStructElement_NetList(nelement_total);



   /*************************************************************************/
   /*                                                                       */
   /* III.) Assign the subvolume (subdomain)     (III.1)                    */
   /*              the fracture elements FRAC    (III.2)                    */
   /*              the edge elements EDGE3D      (III.3)                    */
   /*              the vertex points VERTEX3D    (III.4)                    */
   /*                                                                       */
   /*                                                                       */
   /*       to the listings of the vertex points -> VERTEX_net              */
   /*                              edges         -> EDGE_net                */
   /*                              faces         -> FACE_net                */
   /*                                                                       */
   /*************************************************************************/
   nvertex_net = nedge_net = nface_net = 0;

   /*************************************************************************/
   /* III.1.) subvolume (subdomain)                                         */
   /*************************************************************************/
   Subvolume_To_NetStructure(sum_subvol_edges, subvol_bot_pt, subvol_top_pt,
                             VERTEX_net, &nvertex_net,
                             EDGE_net, &nedge_net, &ncw_edge,
                             FACE_net, &nface_net, &ncw_face,
                             ELEMENT_net, &nelement_net, &ncw_element);



   /*************************************************************************/
   /* III.2.) fracture elements FRAC                                        */
   /*************************************************************************/
   FRAC_To_NetStructure(FRAC, nfrac, 
                        VERTEX_net, &nvertex_net,
                        EDGE_net, &nedge_net, &ncw_fracture_edge,
                        FACE_net, &nface_net, &ncw_fracture);
                      /*AH06.02.01  
                        EDGE_net, &nedge_net, &ncw_edge,
                        FACE_net, &nface_net, &ncw_face);
                      */



   /*************************************************************************/
   /* III.3.) edge elements EDGE3D                                          */
   /*                                                                       */
   /* if (edge_nr_3D > 0):                                                  */
   /*                                                                       */
   /*    In order to use the fct 'Lines3D_To_NetStructure()', the list      */
   /*    'struct edge EDGE3D' has to be rewritten in the format             */
   /*    'struct trace'                                                     */
   /*                                                                       */
   /*    a: allocate memory for list 'LINES'                                */
   /*                                                                       */
   /*    b: re-write the list 'EDGE3D' of type 'struct edge' into list of   */
   /*       type 'struct trace'                                             */
   /*                                                                       */
   /*    c: call fkt 'Lines3D_To_NetStructure()'                            */
   /*                                                                       */
   /*************************************************************************/
   if (edge_nr_3D > 0) {
      LINES = AllocateStructTraceList(edge_nr_3D);
   
      for (i=0; i<edge_nr_3D; i++)
      {
         LINES[i].inside_subd3D = EDGE3D[i].inside_subd3D;

         LINES[i].pt[0].x = EDGE3D[i].pt0.x;
         LINES[i].pt[0].y = EDGE3D[i].pt0.y;
         LINES[i].pt[0].z = EDGE3D[i].pt0.z;

         LINES[i].pt[1].x = EDGE3D[i].pt1.x;
         LINES[i].pt[1].y = EDGE3D[i].pt1.y;
         LINES[i].pt[1].z = EDGE3D[i].pt1.z;

         LINES[i].pt[0].pt_nr = EDGE3D[i].pt0.pt_nr;
         LINES[i].pt[1].pt_nr = EDGE3D[i].pt1.pt_nr;
      }

      Lines3D_To_NetStructure(edge_nr_3D, 
                              LINES,
                              VERTEX_net, 
                              &nvertex_net,
                              EDGE_net, 
                              &nedge_net);
   }



   /*************************************************************************/
   /* III.4.) vertex points VERTEX3D                                        */
   /*************************************************************************/
   VERTEX3D_To_NetStructure(nvertex_nr, &nvertex_net, VERTEX_net, VERTEX3D); 

}



/****************************************************************************/
/* PreNetgenOutput_1dTRACE_in3d()                                           */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void PreNetgenOutput_1dTRACE_in3d(int sum_subvol_edges)                    
{
   int i, i1, i2, i3, j, j1, k, k1, l, l1;
   int add_help, add_help_edge, add_help0, add_help1;
   int nrectangle   =  4;
   int ntracepoints  =  2;
   int help_edge_nr_rectangle[4];
   int *help_edge_nr_polygon;


   /* set default values */
   ncw_edge = 1;         /* start number for the controll words         */
   ncw_face = 1;         /* start number for the controll words         */
   ncw_fracture = -1;    /* fixed number for the fracture controll word */


   /*************************************************************************/
   /* I.)   Sum up the total  # of vertexs                                  */
   /*                  total  # of edges                                    */
   /*                  total  # of faces                                    */
   /*************************************************************************/
   nvertex_total = nedge_total = nface_total = nelement_total = 0;

   /*************************************************************************/
   /* I.1.) Subvolume                                                       */
   /*************************************************************************/
   nvertex_total  = 2 * sum_subvol_edges;
   nedge_total    = 3 * sum_subvol_edges;
   nface_total    = sum_subvol_edges + 2;
   nelement_total = 1;


   /*************************************************************************/
   /* I.2.) trace elements TRACE                                            */
   /*       --> just the one with TRACE.inside_subd3D = +1                  */
   /*************************************************************************/
   for (i=0; i<ntrace; i++)
   {
      if (TRACE[i].inside_subd3D > 0)
      {
         nvertex_total += 2; 
         nedge_total++;
      }   
   }

   /*************************************************************************/
   /* I.3.) vertex points VERTEX3D                                          */
   /*       --> just the one with VERTEX3D.inside_subd3D = +1               */
   /*************************************************************************/
   for (i=0; i<nvertex_nr; i++)
   {
      if (VERTEX3D[i].inside_subd3D > 0)
      {
         nvertex_total++; 
      }   
   }



   /*************************************************************************/
   /* II.) Allocate memory for the new listings:  VERTEX_net                */
   /*                                             EDGE_net                  */
   /*                                             FACE_net                  */
   /*                                             ELEMENT_net               */
   /*************************************************************************/
   VERTEX_net  = AllocateStructVertex_NetList(nvertex_total);
   EDGE_net    = AllocateStructEdge_NetList(nedge_total);
   FACE_net    = AllocateStructFace_NetList(nface_total);
   ELEMENT_net = AllocateStructElement_NetList(nelement_total);



   /*************************************************************************/
   /*                                                                       */
   /* III.) Assign the subvolume (subdomain)     (III.1)                    */
   /*              the edge elements TRACE       (III.2)                    */
   /*              the vertex points VERTEX3D    (III.3)                    */
   /*                                                                       */
   /*       to the listings of the vertex points -> VERTEX_net              */
   /*                              edges         -> EDGE_net                */
   /*                              faces         -> FACE_net                */
   /*                                                                       */
   /*************************************************************************/
   nvertex_net = nedge_net = nface_net = 0;

   /*************************************************************************/
   /* III.1.) subvol (subdomain)                                            */
   /*************************************************************************/
   Subvolume_To_NetStructure(sum_subvol_edges, subvol_bot_pt, subvol_top_pt,
                             VERTEX_net, &nvertex_net,
                             EDGE_net, &nedge_net, &ncw_edge,
                             FACE_net, &nface_net, &ncw_face,
                             ELEMENT_net, &nelement_net, &ncw_element);


   printf("\n nvertex_net=%d \t nedge_net=%d \t nface_net=%d \n",
              nvertex_net, nedge_net, nface_net);


   /*************************************************************************/
   /* III.2.) trace elements TRACE                                          */
   /*         The trace elements get the controll word '0'                  */
   /*         <-- is this right?! (TODO, AH, 06.02.2001)                    */
   /*************************************************************************/
   Lines3D_To_NetStructure(ntrace, TRACE,
                           VERTEX_net, &nvertex_net,
                           EDGE_net, &nedge_net);


   /*************************************************************************/
   /* III.3.) vertex points VERTEX3D                                        */
   /*************************************************************************/
   VERTEX3D_To_NetStructure(nvertex_nr, &nvertex_net, VERTEX_net, VERTEX3D);

}



/****************************************************************************/
/* PreNetgenOutput_1dTRACE_in2d()                                           */
/*                                                                          */
/*                                                                          */
/*    Data structure for mesh generation                                    */  
/*                                                                          */
/*    Provide data structure for the mesh generating program                */
/*    Reduce the whole point, trace, fracture ... information to            */
/*    --> vertex, edge, face, element                                       */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/* Functions: void write_output_Subplane_tecplot()                          */
/*            void write_output_Subplane_netgenerator()                     */
/*                                                                          */
/****************************************************************************/
void PreNetgenOutput_1dTRACE_in2d(int nvertex_nr, struct vertex *VERTEX3D,
                                  int trace_nr,   struct trace *EDGE2D,
                                  int nsubplane,  struct fracture *Plane,  
                                  struct trace *subpl_trace3D,
                                  int file_number)
{
   int i, j, j1, k, k1, m, m1, m2;
   int nvertex_net_help, nedge_net_help, add_help;

   double help_length; 

   struct edge_distance *distance = NULL; /*abs. distance between TRACE[].pt[0]
                                            and the VERTEX3D points laying on i
                                            the TRACE */

   nedge_on_face=  0;          /*FACE is divided on # of EDGE_net segments  */
   ncw_edge     =  1;          /*start number for the controll words        */
   ncw_face     =  1;          /*start number for the controll words        */
   ncw_fracture = -1;          /*start number for the fracture controll word*/


   /*************************************************************************/
   /* I.)   Sum up the total  # of vertexs                                  */
   /*                  total  # of edges                                    */
   /*                  total  # of faces                                    */
   /*************************************************************************/
   nvertex_total = nedge_total = nface_total = nelement_total = 0;
   nedge_on_face_total = 0;


   /**********************************************************************/
   /* I.1.) subplane3D                                                   */
   /*                                                                    */
   /* Remark to 'nedge_total' and 'nface_total':                         */
   /* Count the subplane3D corner points and the number of EDGE2D points */
   /* laying on the boundary lines of the subpl_trace3D to 'nedge_total' */
   /* --> the subplane3D has to be divided in single EDGE_net segments   */
   /*     'nedge_total'   ... required for the ... # of EDGE_net         */
   /*     'nedge_on_face' ... required for the ... # of FACE[].EDGE_NR   */
   /*                                                                    */
   /**********************************************************************/
   nvertex_total  = 4;           /*the 4 corner nodes of the subplane3D  */
   nedge_total    = 4;           /*the 4 boundary lines of the subplane3D*/
   nedge_on_face_total = 4;      /*the 4 edges forming the face          */
   nface_total    = 1;              
   nelement_total = 0;

   for (j=0; j<4; j++)          /*4 = boundary lines of the subpl_trace3D*/
   {
      nedge_total          += subpl_trace3D[j].nvertex_on;   
      nedge_on_face_total  += subpl_trace3D[j].nvertex_on;   
   }


   /**********************************************************************/
   /* I.2.) trace elements EDGE2D                                        */
   /*       (here, all EDGE2D[].inside_subd3D = +1)                      */
   /* 'nedge_total' depending on the number of VERTEX3D points laying on */
   /* the EDG2D  --> the EDG2D is subdivided in line segments            */
   /**********************************************************************/
   for (j=0; j<trace_nr; j++) {
      if (EDGE2D[j].inside_subd3D > 0)  {       /*just for controll*/
         nvertex_total += 2;                    /*2 = two points per line*/
         nedge_total   += 1 + EDGE2D[j].nvertex_on;   
      }   
   }

   /**********************************************************************/
   /* I.3.) vertex points VERTEX3D                                       */
   /*       --> just the one with VERTEX3D.inside_subd3D = +1            */
   /**********************************************************************/
   for (j=0; j<nvertex_nr; j++) {
      if (VERTEX3D[j].inside_subd3D > 0) {
         nvertex_total++; 
      }   
   }


   /**********************************************************************/
   /* II.) Allocate memory for the new listings:  VERTEX_net             */
   /*                                             EDGE_net               */
   /*                                             FACE_net               */
   /*                                             FACE_net[].EDGE_NR     */
   /**********************************************************************/
   VERTEX_net    = AllocateStructVertex_NetList(nvertex_total);
   EDGE_net      = AllocateStructEdge_NetList(nedge_total);
   FACE_net      = AllocateStructFace_NetList(nface_total);

   nface_net     = 0;
   FACE_net[nface_net].face_nr = nface_net;
   if ((FACE_net[nface_net].EDGE_NR = 
               (int *)malloc(nedge_on_face_total * sizeof(int))) == NULL)
   {
      fprintf(stderr,"Memory allocation failed: 'Allocate1dIntArray()' \n");
      exit (-1);
   }


   /*nedge_on_face = 0; */ /*set back, start counting the real # of 'nedge_on_face'*/

   /**********************************************************************/
   /*                                                                    */
   /* III.) Assign the subplane  (subdomain)     (III.1)                 */
   /*              the edge elements EDGE2D      (III.2)                 */
   /*              the vertex points VERTEX3D    (III.3)                 */
   /*                                                                    */
   /*       to the listings of the vertex points -> VERTEX_net           */
   /*                              edges         -> EDGE_net             */
   /*                              faces         -> FACE_net             */
   /*                                                                    */
   /**********************************************************************/
   nvertex_net = nedge_net = nface_net = 0;

   /**********************************************************************/
   /* III.1.) subplane3D                                                 */
   /**********************************************************************/
   Subplane_To_NetStructure(subpl_trace3D, &nsubplane, Plane,
                            VERTEX3D,
                            VERTEX_net, &nvertex_net,
                            EDGE_net, &nedge_net, &nedge_on_face, &ncw_edge,
                            FACE_net, &nface_net, &ncw_face);


   /**********************************************************************/
   /* III.2.) EDGE2D                                                     */
   /**********************************************************************/
   EDGE2D_To_NetStructure(trace_nr, EDGE2D, VERTEX3D, 
                          VERTEX_net, &nvertex_net,
                          EDGE_net, &nedge_net, &ncw_fracture);



   /**********************************************************************/
   /* Very small EDGE_net[i] length causes trouble for the netgenerator  */
   /*                                                                    */
   /* What to do:                                                        */
   /* 1.) if the length ('help_length') of a EDGE_net[i] element is      */
   /*   smaller than 'epsilon_length'                                    */
   /*   and one point of EDGE_net[i] (EDGE_net[i].pt0 or EDGE_net[i].pt1)*/
   /*   does not occur more than 1 time in the toatal list EDGE_net[]    */
   /*                                                                    */
   /* --> Cancel the EDGE_net[i] element and the according point out of  */
   /*     the list VERTEX_net[], EDGE_net[]                              */
   /*                                                                    */
   /* 3.) The VERTEX_net[] numbers and the EDGE_net[] numbers have to    */
   /*     modified because some elements are not longer valid            */
   /*                                                                    */
   /**********************************************************************/
   for (m=0; m<nedge_net; m++)
   {
      help_length = abs_vec_pt_pt(&VERTEX_net[EDGE_net[m].pt0.pt_nr_old].pt,
                                  &VERTEX_net[EDGE_net[m].pt1.pt_nr_old].pt);
      if (help_length <= epsilon_length)
      {
         if (VERTEX_net[EDGE_net[m].pt0.pt_nr_old].appear_net < 1)
         {
            VERTEX_net[EDGE_net[m].pt0.pt_nr_old].appear_net = -1; /*kick out*/
            EDGE_net[m].appear_net = -1; 
         }

         if (VERTEX_net[EDGE_net[m].pt1.pt_nr_old].appear_net < 1)
         {
            VERTEX_net[EDGE_net[m].pt1.pt_nr_old].appear_net = -1; /*kick out*/
            EDGE_net[m].appear_net = -1; 
         }
      } 
   }


   /*************************************************************************/
   /* 3.) The VERTEX_net[] numbers and the EDGE_net[] numbers have to       */
   /*     modified because some elements are not longer valid               */
   /* 3.1) Assing the new numbers to the VERTEX_net[] and to the EDGE_net[] */
   /*************************************************************************/
   m1 = nvertex_net; 
   nvertex_net = 0; 
   for (m=0; m<m1; m++)
   {
      if (VERTEX_net[m].appear_net >= 0) 
      {
         VERTEX_net[m].pt_nr = nvertex_net; 
         nvertex_net++;
      }
   }

   m2 = nedge_net; 
   nedge_net = 0; 
   for (m=0; m<m2; m++)
   {
      if (EDGE_net[m].appear_net >= 0) 
      {
	   /* 	  
         EDGE_net[nedge_net].appear_net = 0; 
         if (VERTEX_net[EDGE_net[m].pt0.pt_nr_old].pt_nr !=
                VERTEX_net[EDGE_net[m].pt1.pt_nr_old].pt_nr)
         {
            EDGE_net[nedge_net].pt0.pt_nr  = 
                            VERTEX_net[EDGE_net[m].pt0.pt_nr_old].pt_nr;
            EDGE_net[nedge_net].pt1.pt_nr  = 
                            VERTEX_net[EDGE_net[m].pt1.pt_nr_old].pt_nr;
            nedge_net++;
         } Nui 19.04.05 BUG */
	   
         EDGE_net[m].appear_net = 0; 
         if (VERTEX_net[EDGE_net[m].pt0.pt_nr_old].pt_nr !=
                VERTEX_net[EDGE_net[m].pt1.pt_nr_old].pt_nr)
         {
            EDGE_net[m].pt0.pt_nr  = 
                            VERTEX_net[EDGE_net[m].pt0.pt_nr_old].pt_nr;
            EDGE_net[m].pt1.pt_nr  = 
                            VERTEX_net[EDGE_net[m].pt1.pt_nr_old].pt_nr;
            nedge_net++;
         }
      }
   }

   /*************************************************************************/
   /* 3.2) Kick out the list elements with '...appear_net < 0'              */
   /*      --> List VERTEX_net[] and  EDGE_net[] includes just the nodes    */
   /*          which forms the net                                          */
   /*************************************************************************/
   nvertex_net_help = 0;
   for (m=0; m<m1; m++) {
      if (VERTEX_net[m].appear_net >= 0) {
         VERTEX_net[nvertex_net_help] = VERTEX_net[m]; 
         nvertex_net_help++;
      }
   }


   nedge_net_help = 0;
   for (m=0; m<m2; m++) {
      if (EDGE_net[m].appear_net >= 0) {
         EDGE_net[nedge_net_help] = EDGE_net[m]; 
         nedge_net_help++;
      }
   }


   /* transform_coord_3D_2D(nvertex_net, VERTEX_net); */

   /**********************************************************************/
   /* output files:  for tecplot (graphik)                               */
   /*                for ART     (mesh generator)                        */
   /**********************************************************************/
   write_output_Subplane_tecplot(file_number, nvertex_net, VERTEX_net, 
                                 nedge_net,   EDGE_net); 

   /**********************************************************************/
   /* coordinate transformation                                          */
   /*  ART can not handle a 2D plane with 1D elements in the 3D space!!! */
   /*  So the points has to be transformed in 2D.                        */
   /**********************************************************************/
   transform_coord_3D_2D(nvertex_net, VERTEX_net); 

   write_output_Subplane_netgenerator(file_number);
   file_number++;


   /**********************************************************************/
   /* give memory back                                                   */
   /**********************************************************************/
   free (VERTEX_net); 
   free (EDGE_net);
   free (FACE_net); 

}







/****************************************************************************/
/*                                                                          */
/*       FUNCTIONS                                                          */
/*                                                                          */
/****************************************************************************/
/****************************************************************************/
/* DistanceCompare()                                                        */
/*   Purpose : Compare the double value of two array elements of the type   */
/*             'double': which one is large?                                */
/*             distance1 > distance2  :  index_return = -1                  */
/*             distance1 < distance2  :  index_return = +1                  */
/*                                                                          */
/*  Needed for the function 'qsort()': see also manpage qsort               */
/*                                                                          */
/****************************************************************************/
int DistanceCompare(const void *p1, const void *p2)
{
   int index_return = 0;
 
   struct edge_distance  *a_distance = (struct edge_distance *)p1;
   struct edge_distance  *b_distance = (struct edge_distance *)p2;

   if (a_distance->length < b_distance->length)  index_return = -1;
   if (a_distance->length > b_distance->length)  index_return =  1;

   return index_return;
}


/****************************************************************************/
/*  AllocateStructVertex_NetList()                                          */
/*                                                                          */
/*   PURPOSE     : allocate memory for a list of type 'struct vertex_net'   */
/*   ARGUMENTS   : int n  = # size of the list ( ... # of elements)         */
/*   RETURN value: list 큞ist'                                              */
/*                                                                          */
/****************************************************************************/
struct vertex_net *AllocateStructVertex_NetList(int n)
{
   struct vertex_net *list;

   list = (struct vertex_net *)malloc(n * sizeof(struct vertex_net));

   if (list == NULL)
   {
      fprintf(stderr,"Memory allocation failed: Vertex_Net[] \n");
      exit (-1);
   }
   return (list);
}


/****************************************************************************/
/*  AllocateStructEdge_NetList()                                            */
/*                                                                          */
/*   PURPOSE     : allocate memory for a list of type 'struct edge_net'     */
/*   ARGUMENTS   : int n  = # size of the list ( ... # of elements)         */
/*   RETURN value: 큞ist'                                                   */
/*                                                                          */
/****************************************************************************/
struct edge_net *AllocateStructEdge_NetList(int n)
{
   struct edge_net *list;

   list = (struct edge_net *)malloc(n * sizeof(struct edge_net));

   if (list == NULL)
   {
      fprintf(stderr,"Memory allocation failed: Edge_Net[] \n");
      exit (-1);
   }
   return (list);
}



/****************************************************************************/
/* AllocateStructFace_NetList()                                             */
/*                                                                          */
/*   PURPOSE     : allocate memory for a list of type 'struct face_net'     */
/*   ARGUMENTS   : int n  = # size of the list ( ... # of elements)         */
/*   RETURN value: 'list'                                                   */
/*                                                                          */
/****************************************************************************/
struct face_net *AllocateStructFace_NetList(int n)
{
   struct face_net *list;

   list = (struct face_net *)malloc(n * sizeof(struct face_net));

   if (list == NULL)
   {
      fprintf(stderr,"Memory allocation failed: Face_Net[] \n"); 
      exit (-1);
   }
   return (list);
}


/****************************************************************************/
/* AllocateStructElement_NetList()                                          */
/*                                                                          */
/*   PURPOSE     : allocate memory for list of type 'struct element_net'    */
/*   ARGUMENTS   : int n  = # size of the list ( ... # of elements)         */
/*   RETURN value: 'list'                                                   */
/*                                                                          */
/****************************************************************************/
struct element_net *AllocateStructElement_NetList(int n)
{
   struct element_net *list;

   list = (struct element_net *)malloc(n * sizeof(struct element_net));

   if (list == NULL)
   {
      fprintf(stderr,"Memory allocation failed: Element_Net[] \n");
      exit (-1);
   }
   return (list);
}



/****************************************************************************/
/*  AllocateStructEdgeDistanceList()                                        */
/*                                                                          */
/*   PURPOSE     : allocate memory for a list of type 'struct vertex_net'   */
/*   ARGUMENTS   : int n  = # size of the list ( ... # of elements)         */
/*   RETURN value: list 큞ist'                                              */
/*                                                                          */
/****************************************************************************/
struct edge_distance *AllocateStructEdgeDistanceList(int n)
{
   struct edge_distance *list;

   list = (struct edge_distance *)malloc(n * sizeof(struct edge_distance));

   if (list == NULL)
   {
      fprintf(stderr,"Memory allocation failed: Edge-Distance[] \n");
      exit (-1);
   }
   return (list);
}



/****************************************************************************/
/*  AllocateStructTraceList()                                               */
/*                                                                          */
/*   PURPOSE     : allocate memory for a list of type 'struct trace'        */
/*   ARGUMENTS   : int n  = # size of the list ( ... # of elements)         */
/*   RETURN value: list 큞ist'                                              */
/*                                                                          */
/****************************************************************************/
struct trace *AllocateStructTraceList(int n)
{
   struct trace *list;

   list = (struct trace *)malloc(n * sizeof(struct trace));

   if (list == NULL)
   {
      fprintf(stderr,"Memory allocation failed: Trace[] \n");
      exit (-1);
   }
   return (list);
}



/****************************************************************************/
/* write_output_Subplane_tecplot()                                          */
/*                                                                          */
/****************************************************************************/
void write_output_Subplane_tecplot(int file_number,
                                   int nvertex_net, 
                                   struct vertex_net VERTEX_net[],
                                   int nedge_net,   
                                   struct edge_net EDGE_ne[])
{
   int i, i1, i2;
   int strlength;
   char buf1[30], buf2[20];
   FILE *fileout = NULL;

   strcpy(buf1, "Subplane3D_000.dat");
   sprintf(buf2, "%d" , file_number);
   strlength = strlen(buf2);
   for (i=0;  i<strlength; i++) {
      buf1[14-strlength+i] = buf2[i]; /*12 = string 'Subplane3D__0000'*/
   }                                         /* end of initialization */

   fileout = fopen(buf1, "w");
   fprintf(fileout,"TITLE=\"1D fracture traces in 3D domain\" ");
   fprintf(fileout,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
   fprintf(fileout,"\nZONE N=%d, E=%d, F=FEPOINT, ET=TRIANGLE",nvertex_net,nedge_net);

   /* x-, y-, z- coordinate */
   for(i=0; i<nvertex_net; i++)
   {
      if (VERTEX_net[i].appear_net >= 0)
      {
         fprintf(fileout,"\n%8.5f  %8.5f  %8.5f",
           VERTEX_net[i].pt.x, VERTEX_net[i].pt.y, VERTEX_net[i].pt.z);
      }
   }

   /* Inzidenz matrix */
   for(i=0; i<nedge_net; i++)
   {
      if (EDGE_net[i].appear_net >= 0)
      {
         i1 = EDGE_net[i].pt0.pt_nr + 1;
         i2 = EDGE_net[i].pt1.pt_nr + 1;
         fprintf(fileout,"\n%d %d %d ", i1, i2, i2);
      }
   }
   fprintf(fileout,"\n");

   fprintf(stdout,"\n\tWrote %s", buf1);

   fclose(fileout);
}


/****************************************************************************/
/* write_output_Subplane_netgenerator(()                                    */
/*                                                                          */
/****************************************************************************/
void write_output_Subplane_netgenerator(int file_number)
{
   int i;
   int strlength;
   char buf1[30], buf2[20];
   FILE *fileout = NULL;


   /*************************************************************************/
   /* initialize new file                                                   */
   /*************************************************************************/
   strcpy(buf1, "Subplane3D_000.art");
   sprintf(buf2, "%d" , file_number);
   strlength = strlen(buf2);
   for (i=0;  i<strlength; i++) {
      buf1[14-strlength+i] = buf2[i]; /*12 = string 'Subplane3D__0000'*/
   }                                         /* end of initialization */


   fileout = fopen(buf1, "w");

   write_ART_file_Subplane(fileout);           /* in file_output.c */
   fprintf(stdout,"\n\tWrote %s", buf1);

   fclose(fileout);


}


/****************************************************************************/
/* Subvolume_To_NetStructure()                                              */
/*                                                                          */
/****************************************************************************/
void Subvolume_To_NetStructure(int sum_subvol_edges,
                               struct point *subvol_bot_pt,
                               struct point *subvol_top_pt,
                               struct vertex_net *VERTEX_net,
                               int *nvertex_net, 
                               struct edge_net *EDGE_net,
                               int *nedge_net, 
                               int *ncw_edge, 
                               struct face_net *FACE_net,
                               int *nface_net, 
                               int *ncw_face, 
                               struct element_net *ELEMENT_net,
                               int *nelement_net, 
                               int *ncw_element)
{
   int i, i1, i2, i3;
   int nrectangle=4;


   /*************************************************************************/
   /* III.1.) subvol (subdomain)                                            */
   /*************************************************************************/
   /* points of the subvol bottom plane */
   for (i=0; i<sum_subvol_edges; i++)
   {
      VERTEX_net[*nvertex_net].pt_nr = *nvertex_net;
      VERTEX_net[*nvertex_net].pt.x  = subvol_bot_pt[i].x;
      VERTEX_net[*nvertex_net].pt.y  = subvol_bot_pt[i].y;
      VERTEX_net[*nvertex_net].pt.z  = subvol_bot_pt[i].z;
      (*nvertex_net)++;
   }

   /* points of the subvol top plane */
   for (i=0; i<sum_subvol_edges; i++)
   {
      VERTEX_net[*nvertex_net].pt_nr = *nvertex_net;
      VERTEX_net[*nvertex_net].pt.x  = subvol_top_pt[i].x;
      VERTEX_net[*nvertex_net].pt.y  = subvol_top_pt[i].y;
      VERTEX_net[*nvertex_net].pt.z  = subvol_top_pt[i].z;
      (*nvertex_net)++;
   }


   /* egdes of the subvol bottom plane */
   for (i=0; i<sum_subvol_edges; i++)
   {
      i1 = i+1;
      if (i1 == sum_subvol_edges) i1 = 0;

      EDGE_net[*nedge_net].edge_nr   = *nedge_net; 
      EDGE_net[*nedge_net].pt0.pt_nr = VERTEX_net[i].pt_nr;
      EDGE_net[*nedge_net].pt1.pt_nr = VERTEX_net[i1].pt_nr;
      EDGE_net[*nedge_net].cw        = *ncw_edge; 
      (*ncw_edge)++;
      (*nedge_net)++;
   }

   /* egdes between nodes of the bottom and the top plane */
   for (i=0; i<sum_subvol_edges; i++)
   {
      i1 = i+sum_subvol_edges; 

      EDGE_net[*nedge_net].edge_nr   = *nedge_net; 
      EDGE_net[*nedge_net].pt0.pt_nr = VERTEX_net[i].pt_nr;
      EDGE_net[*nedge_net].pt1.pt_nr = VERTEX_net[i1].pt_nr;
      EDGE_net[*nedge_net].cw        = *ncw_edge; 
      (*ncw_edge)++;
      (*nedge_net)++;
   }

   /* egdes of the subvol top plane */
   for (i=0; i<sum_subvol_edges; i++)
   {
      i1 = i+sum_subvol_edges; 
      i2 = i1+1;
      if (i2 == (2*sum_subvol_edges)) i2 = sum_subvol_edges;

      EDGE_net[*nedge_net].edge_nr   = *nedge_net; 
      EDGE_net[*nedge_net].pt0.pt_nr = VERTEX_net[i1].pt_nr;
      EDGE_net[*nedge_net].pt1.pt_nr = VERTEX_net[i2].pt_nr;
      EDGE_net[*nedge_net].cw        = *ncw_edge; 
      (*ncw_edge)++;
      (*nedge_net)++;
   }
   
   /* face of the subvol bottom plane */
   FACE_net[*nface_net].face_nr   = *nface_net;
   FACE_net[*nface_net].sum_edges = sum_subvol_edges;
   if ((FACE_net[*nface_net].EDGE_NR 
           = (int *)malloc(sum_subvol_edges * sizeof(int))) == NULL)
   {
      fprintf(stderr,"Memory allocation failed: 'FACE_net[nface_net].EDGE_NR' \n");
      exit (-1);
   }

   for (i=0; i<sum_subvol_edges; i++)
   {
      FACE_net[*nface_net].EDGE_NR[i] = i;
   }
   FACE_net[*nface_net].cw = *ncw_face;  
   (*ncw_face)++;  
   (*nface_net)++;
   

   /* faces of the subvol side planes; side planes are rectangles */
   for (i=0; i<sum_subvol_edges; i++)
   {
      FACE_net[*nface_net].face_nr   = *nface_net;
      FACE_net[*nface_net].sum_edges = nrectangle; /* 4 nodes per side plane */
      if ((FACE_net[*nface_net].EDGE_NR 
           = (int *)malloc(sum_subvol_edges * sizeof(int))) == NULL)
      {
         fprintf(stderr,"Memory allocation failed: 'FACE_net[nface_net].EDGE_NR' \n");
         exit (-1);
      }

      i1 = i  + 1 + sum_subvol_edges;
      i2 = i  + 2*sum_subvol_edges;
      i3 = i1 - 1;
      if (i == (sum_subvol_edges-1)) i1 = sum_subvol_edges;

      FACE_net[*nface_net].EDGE_NR[0] = i;
      FACE_net[*nface_net].EDGE_NR[1] = i1;
      FACE_net[*nface_net].EDGE_NR[2] = i2;
      FACE_net[*nface_net].EDGE_NR[3] = i3;

      FACE_net[*nface_net].cw = *ncw_face;  
      (*ncw_face)++;  
      (*nface_net)++;
   }

   /* face of the subvol top plane */
   FACE_net[*nface_net].face_nr   = *nface_net;
   FACE_net[*nface_net].sum_edges = sum_subvol_edges;
   if ((FACE_net[*nface_net].EDGE_NR 
           = (int *)malloc(sum_subvol_edges * sizeof(int))) == NULL)
   {
      fprintf(stderr,"Memory allocation failed: 'FACE_net[nface_net].EDGE_NR' \n");
      exit (-1);
   }

   for (i=0; i<sum_subvol_edges; i++)
   {
      FACE_net[*nface_net].EDGE_NR[i] = i + 2*sum_subvol_edges;
   }
   FACE_net[*nface_net].cw = *ncw_face;  
   (*ncw_face)++;  
   (*nface_net)++;
   

   /* element of the subvol bottom plane */
   ELEMENT_net[*nelement_net].element_nr = *nelement_net;
   ELEMENT_net[*nelement_net].sum_faces  = sum_subvol_edges+2; 
                           /*side planes (=sum_subvol_edges) + top + bottom */

   if ((ELEMENT_net[*nelement_net].FACE_NR 
           = (int *)malloc((sum_subvol_edges+2) * sizeof(int))) == NULL)
   {
      fprintf(stderr,"Memory allocation failed: 'ELEMENT_net[].FACE_NR' \n");
      exit (-1);
   }



   for (i=0; i<sum_subvol_edges+2; i++)
   {
      ELEMENT_net[*nelement_net].FACE_NR[i] = i;
   }
   ELEMENT_net[*nelement_net].cw = *ncw_element;  
   (*nelement_net)++;

}


/****************************************************************************/
/* FRAC_To_NetStructure()                                                   */
/*                                                                          */
/****************************************************************************/
void FRAC_To_NetStructure(struct fracture *FRAC, 
                          int nfrac,
                          struct vertex_net *VERTEX_net, 
                          int *nvertex_net,
                          struct edge_net *EDGE_net, 
                          int *nedge_net, 
                          int *ncw_fracture_edge,
                          struct face_net *FACE_net, 
                          int *nface_net, 
                          int *ncw_fracture)
{
   int i, j, j1, k ,k1;
   int add_help;
   int nrectangle   =  4;

   int help_edge_nr_rectangle[4];
   int *help_edge_nr_polygon;


   for (i=0; i<nfrac; i++)
   {
      if (FRAC[i].inside_subd3D > 0)
      {
         if (FRAC[i].index_ch == -1)    /* case 1:   */
         {
            /****************************************************************/
            /* fracture is still a rectangle                                */
            /****************************************************************/
            for (j=0; j<=nrectangle; j++)
            {
               if (j<nrectangle)
               {
                  /**********************************************************/
                  /* Check, if there already exists a point in 'VERTEX_net' */
                  /* --> apply function 'Point1_equal_Point2_epsilonlarge   */
                  /*     (in subdomain_3D.c)                                */
                  /*                                                        */
                  /* Point already exists: assign the point number of the   */
                  /*                existing point to the point FRAC[].pt[] */
                  /* Point is new : assign new number to  point FRAC[].pt[] */
                  /**********************************************************/
                  k1 = *nvertex_net; 
                  add_help = 0;
                  for (k=0; k<k1; k++) 
                  {
                     if ((Point1_equal_Point2_epsilonlarge(FRAC[i].pt[j], 
                                                           VERTEX_net[k].pt)) 
                         == 1)
                     {
                        FRAC[i].pt[j].pt_nr = VERTEX_net[k].pt_nr;
                        add_help += 1;
                     }
                  }
   
                  if (add_help < 1)
                  {
                     FRAC[i].pt[j].pt_nr = *nvertex_net;
                     VERTEX_net[*nvertex_net].pt_nr = *nvertex_net;
                     VERTEX_net[*nvertex_net].pt.x  = FRAC[i].pt[j].x;
                     VERTEX_net[*nvertex_net].pt.y  = FRAC[i].pt[j].y;
                     VERTEX_net[*nvertex_net].pt.z  = FRAC[i].pt[j].z;
                     (*nvertex_net)++;
                  }
               }

               if (j > 0)
               {
                  j1 = j;
                  if (j1 == nrectangle) j1 = 0;

                  /***********************************************************/
                  /* Check, if there already exists a edge element 'EDGE_net'*/
                  /* which is equal to the edge element 'FRAC[].pt[].pt_nr'  */
                  /* --> Compare the two end points of the edge element      */
                  /*                                                         */
                  /* --> apply function 'Point1_equal_Point2_epsilonlarge'   */
                  /*     (in subvolume3D.c)                                  */
                  /*                                                         */
                  /* --> Edge already exists: skip the EDGE3D[i] element     */
                  /* --> Edge is new : assign new egde to the list 'EDGE_net'*/
                  /*                                                         */
                  /***********************************************************/
                  k1 = *nedge_net; 
                  add_help = 0;
                  for (k=0; k<k1; k++) 
                  {
                     if (  (  (EDGE_net[k].pt0.pt_nr == FRAC[i].pt[j-1].pt_nr) 
                            &&(EDGE_net[k].pt1.pt_nr == FRAC[i].pt[j1].pt_nr )) 
                        
                         ||(  (EDGE_net[k].pt1.pt_nr == FRAC[i].pt[j-1].pt_nr) 
                            &&(EDGE_net[k].pt0.pt_nr == FRAC[i].pt[j1].pt_nr )))
                     {
                        add_help++;
                        help_edge_nr_rectangle[j-1] = k;
                        break;
                     }
                  }

                  if (add_help < 1) 
                  {
                     help_edge_nr_rectangle[j-1]    = *nedge_net;
                     EDGE_net[*nedge_net].edge_nr   = *nedge_net; 
                     EDGE_net[*nedge_net].pt0.pt_nr = FRAC[i].pt[j-1].pt_nr; 
                     EDGE_net[*nedge_net].pt1.pt_nr = FRAC[i].pt[j1].pt_nr;

                     /* for the 3D case: fracture edges get positiv controll number. 
                        So, a boundary condition can be assigned to the fracture edge.
                        old: EDGE_net[nedge_net].cw        = *ncw_fracture;
                     */
                     /* AH, 06.02.2001, Fracture edges bekommen eigene, negative
                        Kontrollwoerter
                        EDGE_net[*nedge_net].cw        = *ncw_edge; 
                        (*ncw_edge)++;
                     */
                     EDGE_net[*nedge_net].cw        = *ncw_fracture_edge; 
                     (*ncw_fracture_edge)--;  /*descenting negative number*/
                     (*nedge_net)++;
                  }
               }
            }

            /* face of the fracture plane */
            FACE_net[*nface_net].face_nr   = *nface_net;
            FACE_net[*nface_net].sum_edges = nrectangle; 
            if ((FACE_net[*nface_net].EDGE_NR 
                       = (int *)malloc(nrectangle * sizeof(int))) == NULL)
            {
               fprintf(stderr,"Memory allocation failed: 'FACE_net[].EDGE_NR' \n");
               exit (-1);
            }   


            for (j=0; j<nrectangle; j++)
            {
               j1 = *nedge_net - nrectangle + j;
               /*AH2902 FACE_net[*nface_net].EDGE_NR[j] = EDGE_net[j1].edge_nr;*/
               FACE_net[*nface_net].EDGE_NR[j] = help_edge_nr_rectangle[j];
            }
            FACE_net[*nface_net].cw = *ncw_fracture;
            (*ncw_fracture)--;  /*descenting negative number*/
            (*nface_net)++;
         }

         /*******************************************************************/
         /* fracture is a polygon (no more a rectangle)                     */
         /*******************************************************************/
         else if (FRAC[i].index_ch == 1)  
         {
            if ((help_edge_nr_polygon 
                = (int *)malloc(FRAC[i].sum_ptch * sizeof(int))) == NULL) {
               fprintf(stderr,"Memory allocation failed: 'help_edge_nr_polygon' \n");
               exit (-1);
            }   

            for (j=0; j<=FRAC[i].sum_ptch; j++)
            {
               if (j<FRAC[i].sum_ptch)
               {
                  /**********************************************************/
                  /* Check, if there already exists a point in 'VERTEX_net' */
                  /* --> apply function 'Point1_equal_Point2_epsilonlarge'  */
                  /*     (in subdomain_3D.c)                                */
                  /*                         :                              */
                  /* Point already exists: assign the point number of the   */
                  /*                existing point to the point FRAC[].pt[] */
                  /* Point is new : assign new number to  point FRAC[].pt[] */
                  /**********************************************************/
                  k1 = *nvertex_net; 
                  add_help = 0;
                  for (k=0; k<k1; k++) 
                  {
                     if (Point1_equal_Point2_epsilonlarge(FRAC[i].ch[j],
                                                          VERTEX_net[k].pt)==1)
                     {
                        FRAC[i].ch[j].pt_nr = VERTEX_net[k].pt_nr;
                        add_help += 1;
                     }
                  }
 
                  if (add_help < 1) 
                  {
                     FRAC[i].ch[j].pt_nr = *nvertex_net;
                     VERTEX_net[*nvertex_net].pt_nr = *nvertex_net;
                     VERTEX_net[*nvertex_net].pt.x  = FRAC[i].ch[j].x;
                     VERTEX_net[*nvertex_net].pt.y  = FRAC[i].ch[j].y;
                     VERTEX_net[*nvertex_net].pt.z  = FRAC[i].ch[j].z;
                     (*nvertex_net)++;
                  }
               }

               if (j > 0)
               {
                  j1 = j;
                  if (j == FRAC[i].sum_ptch) j1 = 0;
   

                  /***********************************************************/
                  /* Check, if there already exists a edge element 'EDGE_net'*/
                  /* which is equal to the edge element 'FRAC[].pt[].pt_nr'  */
                  /* --> Compare the two end ponts of the edge element       */
                  /*                                                         */
                  /* --> apply function 'Point1_equal_Point2_epsilonlarge'   */
                  /*     (in subdomain_3D.c)                                 */
                  /*                                                         */
                  /* --> Edge already exists: skip the EDGE3D[i] element     */
                  /* --> Edge is new : assign new egde to the list 'EDGE_net'*/
                  /*                                                         */
                  /***********************************************************/
                  k1 = *nedge_net; 
                  add_help = 0;
                  for (k=0; k<k1; k++) 
                  {
                     if (  (  (EDGE_net[k].pt0.pt_nr == FRAC[i].ch[j-1].pt_nr) 
                            &&(EDGE_net[k].pt1.pt_nr == FRAC[i].ch[j1].pt_nr )) 
                        
                         ||(  (EDGE_net[k].pt1.pt_nr == FRAC[i].ch[j-1].pt_nr) 
                            &&(EDGE_net[k].pt0.pt_nr == FRAC[i].ch[j1].pt_nr )))
                     {
                        add_help++;
                        help_edge_nr_polygon[j-1] = k;
                        break;
                     }
                  }

                  if (add_help < 1) 
                  {
                     help_edge_nr_polygon[j-1]      = *nedge_net;
                     EDGE_net[*nedge_net].edge_nr   = *nedge_net; 
                     EDGE_net[*nedge_net].pt0.pt_nr = FRAC[i].ch[j-1].pt_nr; 
                     EDGE_net[*nedge_net].pt1.pt_nr = FRAC[i].ch[j1].pt_nr;
   
                     /* for the 3D case: fracture edges get positiv controll number. 
                        So, a boundary condition can be assigned to the fracture edge.
                        old: EDGE_net[nedge_net].cw        = *ncw_fracture;
                     */
                     /* AH, 06.02.2001, Fracture edges bekommen eigene, negative
                        Kontrollwoerter
                        EDGE_net[*nedge_net].cw        = *ncw_edge; 
                        (*ncw_edge)++;
                     */
                     EDGE_net[*nedge_net].cw        = *ncw_fracture_edge; 
                     (*ncw_fracture_edge)--;   /*descenting negative number*/
                     (*nedge_net)++;
                  }
               }
            }


            /* face of the fracture plane */
            FACE_net[*nface_net].face_nr   = *nface_net;
            FACE_net[*nface_net].sum_edges = FRAC[i].sum_ptch; 
            if ((FACE_net[*nface_net].EDGE_NR 
                       = (int *)malloc(FRAC[i].sum_ptch * sizeof(int))) == NULL)
            {
               fprintf(stderr,"Memory allocation failed: 'FACE_net[].EDGE_NR' \n");
               exit (-1);
            }   


            for (j=0; j<FRAC[i].sum_ptch; j++)
            {
               j1 = *nedge_net - FRAC[i].sum_ptch + j;
               /*AH2902 FACE_net[*nface_net].EDGE_NR[j] = EDGE_net[j1].edge_nr;*/
               FACE_net[*nface_net].EDGE_NR[j] = help_edge_nr_polygon[j]; 
            }
            FACE_net[*nface_net].cw        = *ncw_fracture;
            (*ncw_fracture)--;  /*descenting negative number*/
            (*nface_net)++;
         }
         else { 
          fprintf(stderr,"PreNetgenOutput: FRAC[i].index_ch !=(+1) and !=(-1)");
         }
      }
   }
}


/****************************************************************************/
/* Lines3D_To_NetStructure()                                                */
/*                                                                          */
/****************************************************************************/
void Lines3D_To_NetStructure(int nloop,
                             struct trace *LINES, 
                             struct vertex_net *VERTEX_net,
                             int *nvertex_net,
                             struct edge_net *EDGE_net,
                             int *nedge_net)
{
   int i, k, k1, l, l1; 
   int add_help_edge, add_help0, add_help1;


   for (i=0; i<nloop; i++)
   {
      if (LINES[i].inside_subd3D > 0)
      {
         /*******************************************************************/
         /* Check, if there already exists a edge element 'EDGE_net' which  */
         /* is equal to the trace element 'LINES[i]'                        */
         /* --> apply function 'Point1_equal_Point2_epsilonlarge'           */
         /*                                                                 */
         /* --> Edge already exists: skip the LINES[i] element              */
         /* --> Edge is new : assign new egde to the list 'EDGE_net'        */
         /*******************************************************************/
         l1 = *nedge_net;
         add_help_edge = 0;
         for (l=0; l<l1; l++)
         {
            if (  (  (Point1_equal_Point2_epsilonlarge(LINES[i].pt[0], 
                                  VERTEX_net[EDGE_net[l].pt0.pt_nr].pt) == 1)
                   &&(Point1_equal_Point2_epsilonlarge(LINES[i].pt[1], 
                                  VERTEX_net[EDGE_net[l].pt1.pt_nr].pt) == 1))
                ||(  (Point1_equal_Point2_epsilonlarge(LINES[i].pt[1], 
                                  VERTEX_net[EDGE_net[l].pt0.pt_nr].pt) == 1)
                   &&(Point1_equal_Point2_epsilonlarge(LINES[i].pt[0], 
                                  VERTEX_net[EDGE_net[l].pt1.pt_nr].pt) == 1)))
            {
               add_help_edge++;
               break;
            }
         }


         if (add_help_edge == 0)
         {
            /****************************************************************/
            /* Check, if there already exists a point in 'VERTEX_net'       */
            /* --> apply function 'Point1_equal_Point2_epsilonlarge'        */
            /*                         :                                    */
            /* Point already exists: assign the point number of the         */
            /*                existing point to the point LINES.pt[]        */
            /* Point is new : assign new number to the point LINES.pt[]     */
            /****************************************************************/
            k1 = *nvertex_net;
            add_help0 = 0;
            add_help1 = 0;
            for (k=0; k<k1; k++) {
               if (Point1_equal_Point2_epsilonlarge(LINES[i].pt[0], 
                                                    VERTEX_net[k].pt) == 1)
               {
                  LINES[i].pt[0].pt_nr = VERTEX_net[k].pt_nr;
                  add_help0++; 
                  break;
               }
            }
            for (k=0; k<k1; k++) {
               if (Point1_equal_Point2_epsilonlarge(LINES[i].pt[1], 
                                                    VERTEX_net[k].pt) == 1)
               {
                  LINES[i].pt[1].pt_nr = VERTEX_net[k].pt_nr;
                  add_help1++; 
                  break;
               }
            }


            if (add_help0 == 0) {
               LINES[i].pt[0].pt_nr = *nvertex_net;
               VERTEX_net[*nvertex_net].pt_nr = *nvertex_net;
               VERTEX_net[*nvertex_net].pt.x  = LINES[i].pt[0].x;
               VERTEX_net[*nvertex_net].pt.y  = LINES[i].pt[0].y;
               VERTEX_net[*nvertex_net].pt.z  = LINES[i].pt[0].z;
               (*nvertex_net)++;
            }
            if (add_help1 == 0) {
               LINES[i].pt[1].pt_nr = *nvertex_net;
               VERTEX_net[*nvertex_net].pt_nr = *nvertex_net;
               VERTEX_net[*nvertex_net].pt.x  = LINES[i].pt[1].x;
               VERTEX_net[*nvertex_net].pt.y  = LINES[i].pt[1].y;
               VERTEX_net[*nvertex_net].pt.z  = LINES[i].pt[1].z;
               (*nvertex_net)++;
            }

            EDGE_net[*nedge_net].edge_nr   = *nedge_net; 
            EDGE_net[*nedge_net].pt0.pt_nr = LINES[i].pt[0].pt_nr; 
            EDGE_net[*nedge_net].pt1.pt_nr = LINES[i].pt[1].pt_nr;
            (*nedge_net)++;
         }
      }
   }
}


/****************************************************************************/
/* VERTEX3D_To_NetStructure()                                               */
/*                                                                          */
/****************************************************************************/
void VERTEX3D_To_NetStructure(int nvertex_nr,
                              int *nvertex_net, 
                              struct vertex_net *VERTEX_net,
                              struct vertex *VERTEX3D)
{
   int i, j, j1;
   int add_help; 


   for (i=0; i<nvertex_nr; i++)
   {
      if (VERTEX3D[i].inside_subd3D > 0)
      {
         /*******************************************************************/
         /* Check, if there already exists a point in 'VERTEX_net'          */
         /* --> Apply function 'Point1_equal_Point2_epsilonlarge'           */
         /*     return value == 1: two points are equal                     */
         /*     return value != 1: two points are not equal                 */
         /*                                                                 */
         /* Sum up the help variable 'add_help'. If at the ed of the loop   */
         /* the variable 'add_help' is still zero, then there is no equal   */ 
         /* point in the list 'VERTEX_net'.                                 */
         /* --> Assign the point 'VERTEX3D[i].pt' to the list               */
         /*******************************************************************/
         add_help = 0;
         j1 = *nvertex_net;
         for (j=0; j<j1; j++)
         {
            if (Point1_equal_Point2_epsilonlarge(VERTEX3D[i].pt, 
                                                 VERTEX_net[j].pt) == 1)
            {
               add_help += 1;
            }
         }

         if (add_help < 1)
         {
            /* VERTEX3D[i].vertex_nr = nvertex_net; */
            VERTEX_net[*nvertex_net].pt_nr = *nvertex_net;
            VERTEX_net[*nvertex_net].pt.x  = VERTEX3D[i].pt.x;
            VERTEX_net[*nvertex_net].pt.y  = VERTEX3D[i].pt.y;
            VERTEX_net[*nvertex_net].pt.z  = VERTEX3D[i].pt.z;
            (*nvertex_net)++;
         }
      }
   }
}

/****************************************************************************/
/* Subplane_To_NetStructure()                                               */
/*                                                                          */
/****************************************************************************/
void Subplane_To_NetStructure(struct trace *subpl_trace3D,
                              int *nsubplane,
                              struct fracture *Plane,
                              struct vertex *VERTEX3D,
                              struct vertex_net *VERTEX_net,
                              int *nvertex_net,
                              struct edge_net *EDGE_net,
                              int *nedge_net,
                              int *nedge_on_face,
                              int *ncw_edge,
                              struct face_net *FACE_net, 
                              int *nface_net, 
                              int *ncw_face)
{

   int j, j1, k, k1, m, m1;
   int add_help;

   struct edge_distance *distance = NULL; /*abs. distance between TRACE[].pt[0]
                                            and the VERTEX3D points laying on i
                                            the TRACE */


   /**********************************************************************/
   /* III.1.) subplane3D                                                 */
   /*                                                                    */
   /* Points of the rectangle subplane3D, 4=corner points                */
   /*                                                                    */
   /* The points of the subplane3D get a '...appear_net' value which is  */
   /* higher than 0. This is done in order to give the points a kind of  */
   /* higher priority. Otherwise, if the points just appear once         */
   /* (...appear_net=0), they could kicked out at the line where the     */
   /* following check is done:                                           */
   /* 'Very small EDGE_net[i] length causes trouble for the netgenerator'*/
   /*                                                                    */
   /**********************************************************************/
   for (j=0; j<4; j++)
   {
      VERTEX_net[*nvertex_net].pt_nr_old  = *nvertex_net;
      VERTEX_net[*nvertex_net].appear_net = 1000;     /*=kind of priority*/
      VERTEX_net[*nvertex_net].pt.x       = Plane[*nsubplane].pt[j].x;
      VERTEX_net[*nvertex_net].pt.y       = Plane[*nsubplane].pt[j].y;
      VERTEX_net[*nvertex_net].pt.z       = Plane[*nsubplane].pt[j].z;
      (*nvertex_net)++;
   }

   for (m=0; m<4; m++)  /*4=boundary lines of the subplane3D*/
   {
      /*******************************************************************/
      /* there are no VERTEX3D points on the subpl_trace[i]              */
      /* --> subpl_trace[i] has not to be splitted                       */
      /*******************************************************************/
      if (subpl_trace3D[m].nvertex_on == 0)
      {
         m1 = m+1;
         if (m1 == 4) m1 = 0;

         EDGE_net[*nedge_net].edge_nr       = *nedge_net; 
         EDGE_net[*nedge_net].appear_net    = 1; 
         EDGE_net[*nedge_net].pt0.pt_nr_old = VERTEX_net[m].pt_nr_old;
         EDGE_net[*nedge_net].pt1.pt_nr_old = VERTEX_net[m1].pt_nr_old;
         EDGE_net[*nedge_net].cw            = *ncw_edge; 
         FACE_net[*nface_net].EDGE_NR[*nedge_on_face] = *nedge_net;
         (*nedge_on_face)++; 
         (*nedge_net)++;
      }

      /*******************************************************************/
      /* there are VERTEX3D points on the subpl_trace[i]                 */
      /* --> subpl_trace[i] has to be splitted in different EDGE_net[]   */
      /*******************************************************************/
      else if (subpl_trace3D[m].nvertex_on > 0)
      {
         distance = AllocateStructEdgeDistanceList(subpl_trace3D[m].nvertex_on); 
   
         for (j=0; j<subpl_trace3D[m].nvertex_on; j++)
         {
            distance[j].length = abs_vec_pt_pt(&subpl_trace3D[m].pt[0], 
                                               &VERTEX3D[subpl_trace3D[m].vertex_on[j]].pt);
            distance[j].old_nr = j; 
         }
     
         /****************************************************************/
         /* If there are more than one vertex point on the subpl_trace3D */
         /* -> sort the distance[subpl_trace3D[].vertex_on] in ascending */
         /*    order.                                                    */
         /* If there is just one vertex point on the subpl_trace3D[]     */
         /* -> skip sorting, split the subpl_trace3D directly in 2 edges */
         /*    1. line segment:  subpl_trace3D[].pt0 -- VERTEX3D[]       */
         /*    2. line segment:  VERTEX3D[]  -- subpl_trace3D[].pt1T     */
         /****************************************************************/
         if (subpl_trace3D[m].nvertex_on > 1)  
         {
            qsort(&distance[0], subpl_trace3D[m].nvertex_on, 
                  sizeof(struct edge_distance),
                  DistanceCompare);
         }

         for (j=0; j<=subpl_trace3D[m].nvertex_on; j++)
         {
            EDGE_net[*nedge_net].edge_nr    = *nedge_net; 
            EDGE_net[*nedge_net].appear_net = 1; 
            EDGE_net[*nedge_net].cw         = *ncw_edge; 
            FACE_net[*nface_net].EDGE_NR[*nedge_on_face] = *nedge_net;
            (*nedge_on_face)++; 

            if (j==0) 
            {
               EDGE_net[*nedge_net].pt0.pt_nr_old = VERTEX_net[m].pt_nr_old;
            }
            else 
            {
               j1  = subpl_trace3D[m].vertex_on[distance[j-1].old_nr];
               /**********************************************************/
               /* Check, if there exists already the point VERTEX3D in   */
               /* the list 'VERTEX_net' (-> loop over all VERTEX_net[])  */
               /* --> Apply function 'Point1_equal_Point2_epsilonlarge'  */
               /*     return value == 1: two points are equal            */
               /*     return value != 1: two points are not equal        */
               /*                                                        */
               /* If at the end of the loop, 'add_help' == 0             */
               /* --> Assign the point 'VERTEX3D[i].pt' to the list      */
               /**********************************************************/
               add_help = 0;
               k1 = *nvertex_net;
               for (k=0; k<k1; k++)
               {
                  if (1 == Point1_equal_Point2_epsilonlarge(VERTEX3D[j1].pt,
                                                            VERTEX_net[k].pt))
                  {
                     add_help++;
                  
                     /* node point already exists, EDGE_net[].pt0 gets number
                        of existing point. */
                     EDGE_net[*nedge_net].pt0.pt_nr_old = k;
                     VERTEX_net[k].appear_net++; 
                     VERTEX3D[j1].inside_subd3D = -1; /* set default value back */
                     break;
                  }
               }

               if (add_help < 1) /* No point VERTEX_net[] == VERTEX3D[] */
               {
                  VERTEX_net[*nvertex_net].pt_nr_old  = *nvertex_net;
                  VERTEX_net[*nvertex_net].appear_net = 0;
                  VERTEX_net[*nvertex_net].pt.x       = VERTEX3D[j1].pt.x;
                  VERTEX_net[*nvertex_net].pt.y       = VERTEX3D[j1].pt.y;
                  VERTEX_net[*nvertex_net].pt.z       = VERTEX3D[j1].pt.z;
                  (*nvertex_net)++;

                  EDGE_net[*nedge_net].pt0.pt_nr_old  = *nvertex_net;
   
                  VERTEX3D[j1].inside_subd3D = -1; /* set default value back */
               }
            }


            if (j==(subpl_trace3D[m].nvertex_on))
            {
               m1 = m+1;
               if (m1 == 4) m1 = 0;
               EDGE_net[*nedge_net].pt1.pt_nr_old = VERTEX_net[m1].pt_nr_old;
            }
            else      
            {
               j1 = subpl_trace3D[m].vertex_on[distance[j].old_nr];
               /**********************************************************/
               /* Check, if there already exists a point in 'VERTEX_net' */
               /* --> Apply function 'Point1_equal_Point2_epsilonlarge'  */
               /*     return value == 1: two points are equal            */
               /*     return value != 1: two points are not equal        */
               /*                                                        */
               /* If at the end of the loop, 'add_help' == 0             */
               /* --> Assign the point 'VERTEX3D[i].pt' to the list      */
               /**********************************************************/
               add_help = 0;
               k1 = *nvertex_net;
               for (k=0; k<k1; k++)
               {
                  if (1 == Point1_equal_Point2_epsilonlarge(VERTEX3D[j1].pt,
                                                            VERTEX_net[k].pt))
                  {
                     add_help++;
                     
                     /* node point already exists, EDGE_net[].pt0 gets number
                        of existing point. */
                     EDGE_net[*nedge_net].pt1.pt_nr_old = k;
                     VERTEX_net[k].appear_net++;
                     VERTEX3D[j1].inside_subd3D = -1; /* set default value back */
                     break;
                  }
               }
   
               if (add_help < 1) /* No point VERTEX_net[] == VERTEX3D[] */
               {
                  VERTEX_net[*nvertex_net].pt_nr_old  = *nvertex_net;
                  VERTEX_net[*nvertex_net].appear_net = 0;
                  VERTEX_net[*nvertex_net].pt.x       = VERTEX3D[j1].pt.x;
                  VERTEX_net[*nvertex_net].pt.y       = VERTEX3D[j1].pt.y;
                  VERTEX_net[*nvertex_net].pt.z       = VERTEX3D[j1].pt.z;
                  VERTEX3D[j1].inside_subd3D         = -1; /* set default value back */
   
                  EDGE_net[*nedge_net].pt1.pt_nr_old  = *nvertex_net;
                  (*nvertex_net)++;
               }
   
            }
            (*nedge_net)++;
         }
      }
      (*ncw_edge)++;
   }

   /*if (distance != NULL) free(distance); */  /* give memory back */
   
   /* face of the rectangle subplane3D */
   FACE_net[*nface_net].sum_edges = *nedge_on_face;
   FACE_net[*nface_net].cw        = *ncw_face;  
   (*ncw_face)++;  
   (*nface_net)++;

}




/****************************************************************************/
/* EDGE2D_To_NetStructure()                                                 */
/*                                                                          */
/****************************************************************************/
void EDGE2D_To_NetStructure(int trace_nr,
                            struct trace *EDGE2D,
                            struct vertex *VERTEX3D,
                            struct vertex_net *VERTEX_net,
                            int *nvertex_net,
                            struct edge_net *EDGE_net,
                            int *nedge_net,
                            int *ncw_fracture)
{
   int i, j, j1, k, k1, m;
   int add_help;

   struct edge_distance *distance = NULL; /*abs. distance between TRACE[].pt[0]
                                            and the VERTEX3D points laying on i
                                            the TRACE */


   for (m=0; m<trace_nr; m++)
   {
      if (EDGE2D[m].inside_subd3D > 0)
      {
         for (j=0; j<2; j++)   /*2=two points per EDGE2D*/
         {
            if (j<2)
            {
               /**********************************************************/
               /* Check, if there already exists a point in 'VERTEX_net' */
               /* --> apply function 'Point1_equal_Point2_epsilonlarge'  */
               /*     (in subdomain_3D.c)                                */
               /*                         :                              */
               /* Point already exists: assign the point number of the   */
               /*               existing point to the point EDGE2D[].pt[]*/
               /* Point is new :assign new number to  point EDGE2D[].pt[]*/
               /**********************************************************/
               k1 = *nvertex_net; 
               add_help = 0;
               for (k=0; k<k1; k++) 
               {
                  if (Point1_equal_Point2_epsilonlarge(EDGE2D[m].pt[j], 
                                                       VERTEX_net[k].pt)==1)
                  {
                     EDGE2D[m].pt[j].pt_nr = VERTEX_net[k].pt_nr_old;
                     VERTEX_net[k].appear_net++; 
                     add_help += 1;
                  }
               }
   
               if (add_help < 1)
               {
                  EDGE2D[m].pt[j].pt_nr = *nvertex_net;
                  VERTEX_net[*nvertex_net].pt_nr_old  = *nvertex_net;
                  VERTEX_net[*nvertex_net].appear_net = 0;
                  VERTEX_net[*nvertex_net].pt.x       = EDGE2D[m].pt[j].x;
                  VERTEX_net[*nvertex_net].pt.y       = EDGE2D[m].pt[j].y;
                  VERTEX_net[*nvertex_net].pt.z       = EDGE2D[m].pt[j].z;
                  (*nvertex_net)++;
               }
            }
         }


         /*******************************************************************/
         /* in general: assign the EDGE2D[].pt[] to the VERTEX_net[] list   */
         /*             assign the EDGE2D[].nr   to the EDGE_net[] list     */
         /*                                                                 */
         /* 1.) Check, if there are VERTEX3D points on the EDGE2D           */
         /*     (VERTEX3D: intersection points with other EDGE2D[] line)    */
         /*     EDGE2D[i].nvertex_on : # of VERTEX3D Points on EDGE2D[i]    */ 
         /*                                                                 */
         /*     If (EDGE2D[i].nvertex_on > 0)                               */
         /*     --> The EDGE2D[i] has to be splitted in single line segments*/
         /*         EDGE2D.pt[] - VERTEX3D[] - VERTEX3D[] -...- EDGE2D.pt[] */
         /*                                                                 */
         /*     --> Each line segment forms a new EDGE_net[] element        */
         /*                                                                 */
         /*     --> Check, if the VERTEX3D point already exists in the list */
         /*         VERTEX_net[]                                            */
         /*                                                                 */
         /*******************************************************************/
         if (EDGE2D[m].nvertex_on > 0)
         {
            distance = AllocateStructEdgeDistanceList(EDGE2D[m].nvertex_on); 

            for (j=0; j<EDGE2D[m].nvertex_on; j++)
            {
               distance[j].length = abs_vec_pt_pt(&EDGE2D[m].pt[0], 
                                         &VERTEX3D[EDGE2D[m].vertex_on[j]].pt);
               distance[j].old_nr = j; 
            }
        
            for (j=0; j<=EDGE2D[m].nvertex_on; j++)
            /****************************************************************/
            /* If there are more than one vertex point on the EDGE2D        */
            /* -> sort the distance[EDGE2D[i].nvertex_on] in ascending order*/
            /*                                                              */
            /* If there is just one vertex point on the EDGE2D              */
            /* ->skip sorting, split the EDGE2D directly in two line segment*/
            /*   1. line segment:  EDGE2D[].pt[0] -- VERTEX3D[]             */
            /*   2. line segment:  VERTEX3D[] -- EDGE2D[].pt[0]             */
            /****************************************************************/
            if (EDGE2D[m].nvertex_on > 1)  
            {
               qsort(&distance[0], EDGE2D[m].nvertex_on, sizeof(struct edge_distance),
                     DistanceCompare);
            }

            for (j=0; j<=EDGE2D[m].nvertex_on; j++)
            {
               EDGE_net[*nedge_net].edge_nr    = *nedge_net; 
               EDGE_net[*nedge_net].appear_net = 1; 

               if (j==0) 
               {
                  EDGE_net[*nedge_net].pt0.pt_nr_old = EDGE2D[m].pt[0].pt_nr;
               }
               else 
               {
                  j1  = EDGE2D[m].vertex_on[distance[j-1].old_nr];
                  /**********************************************************/
                  /* Check, if there exists already the point VERTEX3D in   */
                  /* the list 'VERTEX_net' (-> loop over all VERTEX_net[])  */
                  /* --> Apply function 'Point1_equal_Point2_epsilonlarge'  */
                  /*     return value == 1: two points are equal            */
                  /*     return value != 1: two points are not equal        */
                  /*                                                        */
                  /* If at the end of the loop, 'add_help' == 0             */
                  /* --> Assign the point 'VERTEX3D[i].pt' to the list      */
                  /**********************************************************/
                  add_help = 0;
                  k1 = *nvertex_net;
                  for (k=0; k<k1; k++)
                  {
                     if (1 == Point1_equal_Point2_epsilonlarge(VERTEX3D[j1].pt,
                                                               VERTEX_net[k].pt))
                     {
                        add_help++;
                        
                        /* node point already exists, EDGE_net[].pt0 gets number
                           of existing point. */
                        EDGE_net[*nedge_net].pt0.pt_nr_old = k;
                        VERTEX_net[k].appear_net++; 
                        VERTEX3D[j1].inside_subd3D = -1; /* set default value back */
                        break;
                     }
                  }
         
                  if (add_help < 1) /* No point VERTEX_net[] == VERTEX3D[] */
                  {
                     VERTEX_net[*nvertex_net].pt_nr_old  = *nvertex_net;
                     VERTEX_net[*nvertex_net].appear_net = 0;
                     VERTEX_net[*nvertex_net].pt.x       = VERTEX3D[j1].pt.x;
                     VERTEX_net[*nvertex_net].pt.y       = VERTEX3D[j1].pt.y;
                     VERTEX_net[*nvertex_net].pt.z       = VERTEX3D[j1].pt.z;
                     (*nvertex_net)++;
 
                     EDGE_net[*nedge_net].pt0.pt_nr_old  = *nvertex_net;

                     VERTEX3D[j1].inside_subd3D = -1; /* set default value back */
                  }
               }


               if (j==(EDGE2D[m].nvertex_on))
               {
                  EDGE_net[*nedge_net].pt1.pt_nr_old = EDGE2D[m].pt[1].pt_nr;
               }
               else      
               {
                  j1 = EDGE2D[m].vertex_on[distance[j].old_nr];
                  /**********************************************************/
                  /* Check, if there already exists a point in 'VERTEX_net' */
                  /* --> Apply function 'Point1_equal_Point2_epsilonlarge'  */
                  /*     return value == 1: two points are equal            */
                  /*     return value != 1: two points are not equal        */
                  /*                                                        */
                  /* If at the end of the loop, 'add_help' == 0             */
                  /* --> Assign the point 'VERTEX3D[i].pt' to the list      */
                  /**********************************************************/
                  add_help = 0;
                  k1 = *nvertex_net;
                  for (k=0; k<k1; k++)
                  {
                     if (1 == Point1_equal_Point2_epsilonlarge(VERTEX3D[j1].pt,
                                                               VERTEX_net[k].pt))
                     {
                        add_help++;
                        
                        /* node point already exists, EDGE_net[].pt0 gets number
                           of existing point. */
                        EDGE_net[*nedge_net].pt1.pt_nr_old = k;
                        VERTEX_net[k].appear_net++;
                        VERTEX3D[j1].inside_subd3D = -1; /* set default value back */
                        break;
                     }
                  }
      
                  if (add_help < 1) /* No point VERTEX_net[] == VERTEX3D[] */
                  {
                     VERTEX_net[*nvertex_net].pt_nr_old  = *nvertex_net;
                     VERTEX_net[*nvertex_net].appear_net = 0;
                     VERTEX_net[*nvertex_net].pt.x       = VERTEX3D[j1].pt.x;
                     VERTEX_net[*nvertex_net].pt.y       = VERTEX3D[j1].pt.y;
                     VERTEX_net[*nvertex_net].pt.z       = VERTEX3D[j1].pt.z;
                     VERTEX3D[j1].inside_subd3D = -1; /*set default value back*/

                     EDGE_net[*nedge_net].pt1.pt_nr_old  = *nvertex_net;
                     (*nvertex_net)++;
                  }

               }

               EDGE_net[*nedge_net].cw = *ncw_fracture;
               (*nedge_net)++;
            }
         }


         /*******************************************************************/
         /* Here, no VERTEX3D points lay on EDGE2D[m]:EDGE2D[m].nvertex_on=0*/ 
         /*                                                                 */
         /* --> EDGE2D[m] forms EDGE_net[] element                          */
         /*     EDGE2D[m].pt[].pt_nr is already the new point number of the */
         /*     list 'VERTEX_net[].pt_nr                                    */ 
         /*                                                                 */
         /*******************************************************************/
         else if (EDGE2D[m].nvertex_on == 0)
         {
            EDGE_net[*nedge_net].edge_nr       = *nedge_net; 
            EDGE_net[*nedge_net].appear_net    = 0;
            EDGE_net[*nedge_net].pt0.pt_nr_old = EDGE2D[m].pt[0].pt_nr; 
            EDGE_net[*nedge_net].pt1.pt_nr_old = EDGE2D[m].pt[1].pt_nr;
            EDGE_net[*nedge_net].cw            = *ncw_fracture;
            (*nedge_net)++;
         }
      }
      /*if (distance != NULL) free(distance); */

      /*each trace gets its own controll word (negative number)*/
      (*ncw_fracture)--;

   }  /* loop: 'for (m=0; m<trace_nr; m++)' */
}


