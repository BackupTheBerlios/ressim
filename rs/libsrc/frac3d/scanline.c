/****************************************************************************/
/*                                                                          */
/* File:      scanline.c                                                    */
/*                                                                          */
/* Purpose:   Apply the Scanline methode in order to                        */
/*            o. calculate the intersection points fracture -- scanline     */
/*            o. calculate the separation distance between the intersection */
/*               points on the scanlines                                    */
/*            o. assign the separation distance to the array 'property'     */
/*                                                                          */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/* Functions: struct point *AddScanlinePtToList()                           */
/*            int PointCompareX()                                           */
/*            int PointCompareY()                                           */
/*            int PointCompareY()                                           */
/*                                                                          */
/*            extern struct point *AllocateStructPointList() (build_list.c) */
/*            extern double radius_sphere()                (intersection.c) */
/*            extern double abs_distance_point_line()      (intersection.c) */
/*            extern double abs_vec_pt_pt()                (intersection.c) */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "functions.h" 
#include "scanline.h"

/****************************************************************************/
/* *InitialScanlineMethod(int *nproperty, int ninvest_plane)                */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
double *InitialScanlineMethod(int *nproperty, int ninvest_plane)
{
   int i, j, k, l, ll;

   int nproperty_old = 0;    /* number of properties (old) */
   int nproperty_new = 0;    /* number of properties (new) */

   double help_plane;        /* help variable  */
   double dhelp_x, dhelp_y;  /* help variables */
   double delta_z;
   double radius;           /* radius of a sphere around the fracture plane */
   double distance_pt_line; 
          /* distance between a point and the perpendicular point on a line */

   struct point midpt;   /* mid-point of a sphere around the fracture plane */
   struct point s_pt_intersect;       /* intersection point (plane -- line) */

   double distance_pt_pt;    /* distance between two points */
   double *property_dist;

   struct fracture elem_frac;   


   /*************************************************************************/
   /* read parameter from input file                                        */
   /*************************************************************************/
   nscl_perplane = get_var_integer(uvar, "nscl_perplane");


   /*************************************************************************/
   /* Allocate memory:                                                      */
   /*************************************************************************/
   nscanline = nscl_perplane * ninvest_plane;
   SCANLINE  = AllocateStructScanlineList(nscanline);


   /*************************************************************************/
   /* loop over the n*investigation planes 'n_invest_plane'                 */
   /*************************************************************************/
   ll = 0;
   /*AH09.08.: printf("\n subplanes, on which the scanlines lay: ");*/
   for (l=0; l<ninvest_plane; l++)  
   {
      /**********************************************************************/
      /* Define the subplane, on which the scanline technique is applied.   */
      /*   Either x = variate (subplane || yz-plane): if (ran3(&rseed)>0.5) */
      /*       or y = variate (subplane || xz-plane)                        */
      /*                                                                    */
      /*   Assumption: subplanes has to be vertical planes                  */
      /*                                                                    */
      /**********************************************************************/
      delta_z = (dom_max.z - dom_min.z) / (nscl_perplane+1);

      help_plane = ran3(&rseed); 
      if (help_plane > 0.5)      /* index_plane = 1 */
      {
         dhelp_x = ran3(&rseed) * (dom_max.x - dom_min.x);

         /*AH09.08: printf("\n plane [%2d]: subplane || yz-plane: dhelp_x=%f",l,dhelp_x);*/

         for (i=0; i<nscl_perplane; i++) {
            SCANLINE[ll].pt0.y = dom_min.y;   
            SCANLINE[ll].pt1.y = dom_max.y;  

            SCANLINE[ll].pt0.x = SCANLINE[ll].pt1.x = dhelp_x;     
            SCANLINE[ll].pt0.z = SCANLINE[ll].pt1.z = delta_z * (i+1);
            SCANLINE[ll].index_plane = 1;

            ll++;
         }
      }
      else     /* index_plane = 2 */
      {
         dhelp_y = ran3(&rseed) * (dom_max.y - dom_min.y);

         /*printf("\n plane [%2d]: subplane || xz-plane: dhelp_y=%f",l,dhelp_y);*/

         for (i=0; i<nscl_perplane; i++) {
            SCANLINE[ll].pt0.x = dom_min.x; 
            SCANLINE[ll].pt1.x = dom_max.x; 

            SCANLINE[ll].pt0.y = SCANLINE[ll].pt1.y = dhelp_y;   
            SCANLINE[ll].pt0.z = SCANLINE[ll].pt1.z = delta_z * (i+1);
            SCANLINE[ll].index_plane = 2;

            ll++;
         }
      }
   }
      
 
   printf("\n             total # of scanlines: nscanline=%d ", nscanline);
   for (i=0; i<nscanline; i++)  
   {
      /**********************************************************************/
      /*                                                                    */
      /**********************************************************************/
      npt_on_scanline = 0;                             /* set default value */

      for (j=0; j<nfrac; j++)
      {
         /*******************************************************************/
         /* Check, if plane lays close to the line:                         */
         /*     Start investigation if the spherical radius around the      */
         /*     fracture plane is equal/larger than the distance between    */
         /*     the midpoint of the fracture plane and the perpendiclular   */
         /*     point on the scanline.                                      */
         /*                                                                 */
         /* --> YES: investigate if there is a common intersection point    */
         /* --> NO : skip it, go to the next fracture plane                 */
         /*                                                                 */
         /*******************************************************************/
         radius = radius_sphere(j, FRAC);  

         midpt.x = (FRAC[j].pt[0].x + FRAC[j].pt[2].x) / 2.0; 
         midpt.y = (FRAC[j].pt[0].y + FRAC[j].pt[2].y) / 2.0; 
         midpt.z = (FRAC[j].pt[0].z + FRAC[j].pt[2].z) / 2.0; 

         distance_pt_line = abs_distance_point_line(&SCANLINE[i].pt0, 
                                                    &SCANLINE[i].pt1,&midpt);
         if (radius >= distance_pt_line)
         {
            elem_frac = FRAC[j];
            if ((intersection_node_line_plane(SCANLINE[i].pt0, 
                         SCANLINE[i].pt1, elem_frac, &s_pt_intersect)) == 1)
            {
               /**********************************************************/
               /* Add intersection point to list 'SCANLINE[].ipt'        */
               /**********************************************************/
               if (npt_on_scanline < 1) {
                  if ((SCANLINE[i].ipt = 
                      (struct ipoint *)malloc(1*sizeof(struct ipoint)))==NULL)
                  {
                     fprintf(stderr,"Memory allocation failed: 'SCANLINE[i].ipt'\n");
                     exit (-1);
                  }
               }
               else {
                  if ((SCANLINE[i].ipt = realloc(SCANLINE[i].ipt, 
                             (npt_on_scanline+1)*sizeof(struct ipoint)))==NULL)
                  {
                     fprintf(stderr, "Memory allocation failed: 'SCANLINE[i].ipt'\n");
                     exit (-1);
                  }
               }

               SCANLINE[i].ipt[npt_on_scanline].fracnr       = j; 
               SCANLINE[i].ipt[npt_on_scanline].x            = s_pt_intersect.x;
               SCANLINE[i].ipt[npt_on_scanline].y            = s_pt_intersect.y;
               SCANLINE[i].ipt[npt_on_scanline].z            = s_pt_intersect.z;

             /*  SCANLINE[i].ipt[npt_on_scanline+1].fracnr     = -1; */

               npt_on_scanline++;
            }
         }
      }

      SCANLINE[i].nipt_onscl = npt_on_scanline; 


      /**********************************************************************/
      /* sort and calculate property                                        */
      /**********************************************************************/

/*** TODO: laeuft noch nicht, Probleme mit Feld 'property_dist)'
      SortIptOnScanline_and_CalculateDistProperty(i,
                                                  &nproperty_old,
                                                  &nproperty_new,
                                                  SCANLINE[i].nipt_onscl,
                                                  property_dist);
***/

      /***** TODO Abschnitt 001 durch Funktion 
             'SortIptOnScanline_and_CalculateDistProperty()' ersetzen */
      /*******************************************************************/
      /* if there is more than one intersection point on the scanline    */
      /*******************************************************************/
      if (npt_on_scanline > 1)
      {
         /****************************************************************/
         /* Sort the points of list 'SCANLINE[].ipt' in ascending order  */
         /****************************************************************/
         switch (SCANLINE[i].index_plane) 
         {
            case 1:  /* x-coordinate constant */
               qsort(SCANLINE[i].ipt, npt_on_scanline, sizeof(struct ipoint), 
                     PointCompareY);
               break;
            case 2:  /* y-coordinate constant */
               qsort(SCANLINE[i].ipt, npt_on_scanline, sizeof(struct ipoint), 
                  PointCompareX);
            break;
         }


         /****************************************************************/
         /* Allocate memory for the array 'property_dist'                */
         /*    The array contains the distances between the points on    */
         /*    all 'nscanline' scanlines                                 */
         /****************************************************************/
         if (nproperty_new < 1)  {
            nproperty_new = npt_on_scanline - 1;  
                            /* npt_on_scanline (laying on the same scanline)
                               form (npt_on_scanline-1) distance segments */
            if ((property_dist = 
                  (double *) malloc(nproperty_new * sizeof(double))) == NULL) 
            {
               fprintf(stderr, "Memory allocation failed: property_dist \n");
               exit (-1);
            }
         }
         else {
            nproperty_old  = nproperty_new ; 
            nproperty_new += npt_on_scanline - 1; 
            if ((property_dist = (double *) realloc(property_dist, 
                                   nproperty_new * sizeof(double))) == NULL) 
            {
               fprintf(stderr, "Memory allocation failed: property_dist \n");
               exit (-1);
            }
         }


         /****************************************************************/
         /* Include the distance values to the array                     */
         /****************************************************************/
         k = 0;
         for (j=nproperty_old; j<nproperty_new; j++) {
            distance_pt_pt   = abs_vec_ipt_ipt(&SCANLINE[i].ipt[k], 
                                               &SCANLINE[i].ipt[k+1]);
            property_dist[j] = distance_pt_pt;
            k++;
         }
      }
      /***** TODO Abschnitt 001 durch Funktion 
             'SortIptOnScanline_and_CalculateDistProperty()' ersetzen */
   }

   *nproperty = nproperty_new;      /* total number of property_dist 
                                       values in the array */
   return (property_dist); 
}



/****************************************************************************/
/* OneFracture_FixedSampleGrid_ScanlineMethod()                             */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
double *OneFracture_FixedSampleGrid_ScanlineMethod(int *nproperty, 
                                                   int draw_nr)
{
   int i, j, jj, k;

   int nproperty_old = 0;    /* number of properties (old) */
   int nproperty_new = 0;    /* number of properties (new) */

   double radius;           /* radius of a sphere around the fracture plane */
   double distance_pt_line; 
          /* distance between a point and the perpendicular point on a line */

   struct point midpt;   /* mid-point of a sphere around the fracture plane */
   struct point s_pt_intersect;       /* intersection point (plane -- line) */

   struct fracture elem_frac;   

   double distance_pt_pt;    /* distance between two points */
   double *property_dist;

   int old_nipt_onscl;        /*difference to old amount of ipts on scanline*/
   int new_nipt_onscl;        /*difference to new amount of ipts on scanline*/
   int tmp_nipt_onscl;     
   int tmp_which_ipt_nr; 


   for (i=0; i<nscanline; i++)  
   {
      old_nipt_onscl = new_nipt_onscl = 0;               /*set default value*/
      tmp_which_ipt_nr = -999;                           /*set default value*/

      /**********************************************************************/
      /* investigation per scanline                                         */
      /*   o. if the drawn 'old' fracture element intersects scanline [i]   */
      /*      --> old_nipt_onscl--                                          */
      /**********************************************************************/
      for (j=0; j<SCANLINE[i].nipt_onscl; j++) 
      {
         if (draw_nr == SCANLINE[i].ipt[j].fracnr) {
            old_nipt_onscl   = -1;
            tmp_which_ipt_nr =  j;
            break; 
         }
      }


      /**********************************************************************/
      /* Check, if drawn and changed plane lays close to the line:          */
      /*     Start investigation if the spherical radius around the         */
      /*     fracture plane is equal/larger than the distance between       */
      /*     the midpoint of the fracture plane and the perpendiclular      */
      /*     point on the scanline.                                         */
      /* --> YES: investigate if there is a common intersection point       */
      /* --> NO : skip it                                                   */
      /**********************************************************************/
      radius = radius_sphere(draw_nr, FRAC);  

      midpt.x = (FRAC[draw_nr].pt[0].x + FRAC[draw_nr].pt[2].x) / 2.0; 
      midpt.y = (FRAC[draw_nr].pt[0].y + FRAC[draw_nr].pt[2].y) / 2.0; 
      midpt.z = (FRAC[draw_nr].pt[0].z + FRAC[draw_nr].pt[2].z) / 2.0; 

      distance_pt_line = abs_distance_point_line(&SCANLINE[i].pt0, 
                                                 &SCANLINE[i].pt1,&midpt);
      if (radius >= distance_pt_line)
      {
         elem_frac = FRAC[draw_nr];
         if ((intersection_node_line_plane(SCANLINE[i].pt0, 
                      SCANLINE[i].pt1, elem_frac, &s_pt_intersect)) == 1)
         {
            /****************************************************************/
            /* intersection point is found                                  */
            /* the amount of the new intersection point is increased        */
            /****************************************************************/
            new_nipt_onscl = 1;
         }
      }


      if ((old_nipt_onscl == 0) && (new_nipt_onscl == 0))
      {
         /****************************************************************/
         /* No old ipt and no new ipt.                                   */
         /*    Array 'SCANLINE[i]' keeps the same. Here is nothing to do */
         /*    anymore. Go to the next scanline number.                  */
         /****************************************************************/

         /*AH 01,08. raus, wie sonst?! break; */
      }

      else if ((old_nipt_onscl < 0) && (new_nipt_onscl > 0))
      {
         /****************************************************************/
         /* Minus one old ipt, plus one new ipt.                         */
         /* -> Required memory for array 'SCANLINE[i]' keeps the         */
         /*    same. New intersection point is written on the            */
         /*    position of the old intersection point.                   */
         /* -> Amount of 'nproperty' keeps the same.                     */
         /****************************************************************/

         SCANLINE[i].ipt[tmp_which_ipt_nr].x = s_pt_intersect.x;      
         SCANLINE[i].ipt[tmp_which_ipt_nr].y = s_pt_intersect.y;      
         SCANLINE[i].ipt[tmp_which_ipt_nr].z = s_pt_intersect.z;      
      }

      else if ((old_nipt_onscl == 0) && (new_nipt_onscl > 0)) 
      {
         /****************************************************************/
         /* No old ipt, but plus one new ipt.                            */
         /* -> if SCANLINE[i].nipt_onscl == 0: Required memory for array */
         /*    'SCANLINE[i]' has to be allocated with malloc()           */
         /* -> if SCANLINE[i].nipt_onscl >  0: Required memory for array */
         /*    'SCANLINE[i]' has to be reallocated                       */
         /* -> Amount of 'nproperty' plus 'one'                          */
         /****************************************************************/
         tmp_nipt_onscl = SCANLINE[i].nipt_onscl + new_nipt_onscl;

         if (SCANLINE[i].nipt_onscl == 0) 
         {
            if ((SCANLINE[i].ipt=(struct ipoint *)
                    malloc((tmp_nipt_onscl)*sizeof(struct ipoint)))==NULL) {
               fprintf(stderr,"Memory allocation failed:'SCANLINE[i].ipt'\n");
               exit (-1);
            }
         }
         else if (SCANLINE[i].nipt_onscl > 0) {
            if ((SCANLINE[i].ipt = realloc(SCANLINE[i].ipt, 
                       (tmp_nipt_onscl+1)*sizeof(struct ipoint)))==NULL)
/*AAAAAAAAAAAA*/
            {
               fprintf(stderr,"Memory allocation failed:'SCANLINE[i].ipt'\n");
               exit (-1);
            }
         }

         /****************************************************************/
         /* New amount of 'nipt_onscl' and coordinates of new            */
         /* intersection point is written to the new memory.             */
         /****************************************************************/
         SCANLINE[i].nipt_onscl            = tmp_nipt_onscl;       

         SCANLINE[i].ipt[tmp_nipt_onscl-1].x = s_pt_intersect.x;      
         SCANLINE[i].ipt[tmp_nipt_onscl-1].y = s_pt_intersect.y;      
         SCANLINE[i].ipt[tmp_nipt_onscl-1].z = s_pt_intersect.z;      

         SCANLINE[i].ipt[tmp_nipt_onscl-1].fracnr = draw_nr;
      }

      else if ((old_nipt_onscl < 0) && (new_nipt_onscl == 0)) 
      {
         /****************************************************************/
         /* Minus one old ipt, but no new ipt.                           */
         /* -> Required memory for array 'SCANLINE[i]' is less,          */
         /*    memory has to give back                                   */
         /*                                                              */
         /*    Last array element is written to the empty position       */
         /*    of old ipt (from the drawn fracture plane).               */
         /*                                                              */
         /* TODO: Speicher <SCANLINE[i].ipt[jj]> zurueckgeben !!!        */
         /*                                                              */
         /* -> Amount of 'nproperty' minus 'one'                         */
         /****************************************************************/
         tmp_nipt_onscl = SCANLINE[i].nipt_onscl + old_nipt_onscl;
         jj = SCANLINE[i].nipt_onscl - 1; 

         if (tmp_nipt_onscl > 0) {
            SCANLINE[i].ipt[tmp_which_ipt_nr] = SCANLINE[i].ipt[jj];
         }

         SCANLINE[i].nipt_onscl  = tmp_nipt_onscl;

         /*free(SCANLINE[i].ipt[jj]);*/

      }

      /**********************************************************************/
      /* sort and calculate property                                        */
      /**********************************************************************/

/*** TODO: laeuft noch nicht, Probleme mit Feld 'property_dist)'
      SortIptOnScanline_and_CalculateDistProperty(i,
                                                  &nproperty_old,
                                                  &nproperty_new,
                                                  SCANLINE[i].nipt_onscl,
                                                  property_dist);
***/

      /***** TODO Abschnitt 002 durch Funktion 
             'SortIptOnScanline_and_CalculateDistProperty()' ersetzen */
      /*******************************************************************/
      /* if there is more than one intersection point on the scanline    */
      /*******************************************************************/
      /*if (npt_on_scanline > 1)*/
      if (SCANLINE[i].nipt_onscl > 1)
      {
         /****************************************************************/
         /* Sort the points of list 'SCANLINE[].ipt' in ascending order  */
         /****************************************************************/
         switch (SCANLINE[i].index_plane) 
         {
            case 1:  /* x-coordinate constant */
               qsort(SCANLINE[i].ipt, SCANLINE[i].nipt_onscl, 
                     sizeof(struct ipoint), PointCompareY);
               break;
            case 2:  /* y-coordinate constant */
               qsort(SCANLINE[i].ipt, SCANLINE[i].nipt_onscl, 
                     sizeof(struct ipoint), PointCompareX);
            break;
         }

         /****************************************************************/
         /* Allocate memory for the array 'property_dist'                */
         /*    The array contains the distances between the points on    */
         /*    all 'nscanline' scanlines                                 */
         /****************************************************************/
         if (nproperty_new < 1)  {
            nproperty_new = SCANLINE[i].nipt_onscl - 1;  
                    /* SCANLINE[i].nipt_onscl (laying on the same scanline)
                       form (SCANLINE[i].nipt_onscl-1) distance segments */
            if ((property_dist = 
                  (double *) malloc(nproperty_new * sizeof(double))) == NULL) 
            {
               fprintf(stderr, "Memory allocation failed: property_dist \n");
               exit (-1);
            }
         }
         else {
            nproperty_old  = nproperty_new ; 
            nproperty_new += SCANLINE[i].nipt_onscl - 1; 
            if ((property_dist = (double *) realloc(property_dist, 
                                   nproperty_new * sizeof(double))) == NULL) 
            {
               fprintf(stderr, "Memory allocation failed: property_dist \n");
               exit (-1);
            }
         }


         /****************************************************************/
         /* Include the distance values to the array                     */
         /****************************************************************/
         k = 0;
         for (j=nproperty_old; j<nproperty_new; j++) 
         {
            distance_pt_pt   = abs_vec_ipt_ipt(&SCANLINE[i].ipt[k], 
                                               &SCANLINE[i].ipt[k+1]);

            property_dist[j] = distance_pt_pt;
            /*printf("\n                property_dist[%d]=%f",j,property_dist[j]);*/
            k++;
         }
      }
      /***** TODO Abschnitt 002 durch Funktion 
             'SortIptOnScanline_and_CalculateDistProperty()' ersetzen */

/****AH raus, nur zur Kontrolle
      printf("\n   SCANLINE[%d]:  nipt_onscl=%d    nproperty_new=%d ", 
                    i, SCANLINE[i].nipt_onscl, nproperty_new);
*****/

   }

   *nproperty = nproperty_new; 
                    /* total number of property_dist values in the array */

   return (property_dist); 
}



/****************************************************************************/
/*                                                                          */
/*                          F U N C T I O N S                               */
/*                                                                          */
/****************************************************************************/
/****************************************************************************/
/* TODO: Funktion loeschen!!! (ah, 19.06.2001)                              */
/* struct point *AddScanlinePtToList()                                      */
/*                                                                          */
/*   Purpose      : Build list 'SCANLINEpt' with points on the scanline     */
/*   Arguments    : int slpt_nr = scanline point number, # of list elements */
/*                  struct point node = point which has to add to the list  */
/*                                                                          */
/*   Return value : list 'SCANLINEpt'                                       */
/*                                                                          */
/****************************************************************************/
/****
struct point *AddScanlinePtToList(int slpt_nr,  
                                  struct point node)
{
   if (slpt_nr < 1)
   {
      if ((SCANLINEpt = (struct point *)
                        malloc(2*sizeof(struct point))) == NULL)
      {
         fprintf(stderr, "Memory allocation failed:SCANLINEpt\n");
         exit (-1);
      }
   }
   else
   {
      if ((SCANLINEpt = (struct point *)
        realloc(SCANLINEpt, (slpt_nr+1)*sizeof(struct point))) == NULL)
      {
         fprintf(stderr, "Memory allocation failed:SCANLINEpt\n");
         exit (-1);
      }
   }

   SCANLINEpt[slpt_nr].pt_nr   = slpt_nr; 
   SCANLINEpt[slpt_nr].x       = node.x;
   SCANLINEpt[slpt_nr].y       = node.y;
   SCANLINEpt[slpt_nr].z       = node.z;
   SCANLINEpt[slpt_nr+1].pt_nr = -1;

   return SCANLINEpt; 
}
****/


/****************************************************************************/
/* PointCompareX()                                                          */
/*   Purpose : Compare the x-values of two array elements of the type       */
/*             'struct point': if (a_x > b_x)  or  (a_x < b_x)              */
/*             Needed for the function 'qsort()'                            */
/*                                                                          */
/****************************************************************************/
int PointCompareX(const void *p1, const void *p2)
{
   int index_return = 0;

   struct ipoint *a = (struct ipoint *)p1;
   struct ipoint *b = (struct ipoint *)p2;

   if (a->x < b->x) index_return = -1;
   if (a->x > b->x) index_return =  1;

   return index_return;
}


/****************************************************************************/
/* PointCompareY()                                                          */
/*   Purpose : Compare the y-values of two array elements of the type       */
/*             'struct point': if (a_y > b_y)  or  (a_y < b_y)              */
/*             Needed for the function 'qsort()'                            */
/*                                                                          */
/****************************************************************************/
int PointCompareY(const void *p1, const void *p2)
{
   int index_return = 0;

   struct ipoint *a = (struct ipoint *)p1;
   struct ipoint *b = (struct ipoint *)p2;

   if (a->y < b->y) index_return = -1;
   if (a->y > b->y) index_return =  1;

   return index_return;
}


/****************************************************************************/
/*  AllocateStructScanlineList()                                            */
/*                                                                          */
/*   PURPOSE     : allocate memory for dynamic list type 'struct scanline'  */
/*   ARGUMENTS   : int n  = # size of the list ( ... # of elements)         */
/*   RETURN value: list ´scanline_list´                                     */
/*                                                                          */
/****************************************************************************/
struct scanline *AllocateStructScanlineList(int n)
{
   struct scanline *scanline_list;

   scanline_list = (struct scanline *)malloc(n * sizeof(struct scanline));

   if (scanline_list == NULL) {
      fprintf(stderr,"Memory allocation failed (fct AllocateStructScanlineList)\n");
      exit (-1);
   }
   return (scanline_list);
}


/****************************************************************************/
/* abs_vec_ipt_ipt()                                                        */
/*                                                                          */
/*   Betrag des Richtungsvektors zwischen zwei Punkten                      */
/*                                                                          */
/****************************************************************************/
double abs_vec_ipt_ipt(struct ipoint *n0, struct ipoint *n1)
{
   double abs_length_vec;
   double aa, bb, cc;

   aa =  (n1->x - n0->x) * (n1->x - n0->x);
   bb =  (n1->y - n0->y) * (n1->y - n0->y);
   cc =  (n1->z - n0->z) * (n1->z - n0->z);

   abs_length_vec = sqrt(aa + bb + cc);

   return(abs_length_vec);
}


/****************************************************************************/
/* SortIptOnScanline_and_CalculateDistProperty()                            */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void SortIptOnScanline_and_CalculateDistProperty(int ii,
                                                 int *nproperty_old,
                                                 int *nproperty_new,
                                                 int npt_on_scanline,
                                                 double *property_dist)
{
   int j, k;
 
   double distance_pt_pt;    /* distance between two points */

   /*******************************************************************/
   /* if there is more than one intersection point on the scanline    */
   /*******************************************************************/
   if (npt_on_scanline > 1)
   {
      /****************************************************************/
      /* Sort the points of list 'SCANLINE[].ipt' in ascending order  */
      /****************************************************************/
      switch (SCANLINE[ii].index_plane) 
      {
         case 1:  /* x-coordinate constant */
            qsort(SCANLINE[ii].ipt, npt_on_scanline, sizeof(struct ipoint), 
                  PointCompareY);
            break;
         case 2:  /* y-coordinate constant */
            qsort(SCANLINE[ii].ipt, npt_on_scanline, sizeof(struct ipoint), 
               PointCompareX);
         break;
      }


      /****************************************************************/
      /* Allocate memory for the array 'property_dist'                */
      /*    The array contains the distances between the points on    */
      /*    all 'nscanline' scanlines                                 */
      /****************************************************************/
      if (*nproperty_new < 1)  {
         *nproperty_new = npt_on_scanline - 1;  
                         /* npt_on_scanline (laying on the same scanline)
                            form (npt_on_scanline-1) distance segments */
         if ((property_dist = 
               (double *) malloc(*nproperty_new * sizeof(double))) == NULL) 
         {
            fprintf(stderr, "Memory allocation failed: property_dist \n");
            exit (-1);
         }
      }
      else {
         *nproperty_old  = *nproperty_new ; 
         *nproperty_new += npt_on_scanline - 1; 
         if ((property_dist = (double *) realloc(property_dist, 
                                *nproperty_new * sizeof(double))) == NULL) 
         {
            fprintf(stderr, "Memory allocation failed: property_dist \n");
            exit (-1);
         }
      }


      /****************************************************************/
      /* Include the distance values to the array                     */
      /****************************************************************/
      k = 0;
      for (j=*nproperty_old; j<*nproperty_new; j++) 
      {
         distance_pt_pt   = abs_vec_ipt_ipt(&SCANLINE[ii].ipt[k], 
                                            &SCANLINE[ii].ipt[k+1]);

         property_dist[j] = distance_pt_pt;
         /*printf("\n                property_dist[%d]=%f",j,property_dist[j]);*/
         k++;
      }
   }
}


