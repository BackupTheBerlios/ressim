/****************************************************************************/
/*                                                                          */
/* File:      intersection_points.c                                         */
/*                                                                          */
/* Purpose:   Schnittpunkte (Durchstosspunkt) der                           */
/*            Begrenzungsgeraden g1-g4 (Ebene 0)  mit  Ebene 1  berechnen   */
/*              Gerade g1:  x = pt1[i] + lambda * (pt2[i]-pt1[i])           */
/*              Ebene   :                                                   */
/*              x = pt1[j] + mu * (pt2[j]-pt1[j]) + eta * ((pt4[j]-pt1[j])  */
/*            -->  3 lineare Gleichungen mit drei Unbekannten               */
/*                 Gauss'sches Eliminationsverfahren                        */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/*                                                                          */
/* Functions:                                                               */
/* 1.) intersection_nodes_plane_plane                                       */
/* 2.) intersection_node_line_plane                                         */
/* 3.) intersection_node_line_line                                          */
/*                                                                          */
/*    intern  double abs_vec_point()                                        */
/*            double abs_vec_pt_pt()                                        */
/*            double abs_distance_point_line()                              */
/*            int check_point_in_rectangle()                                */
/*                                                                          */
/*    extern  matrix_AA_fill(struct point, struct point, struct point,      */
/*                           struct point, struct point) (Routine gauss.c)  */
/*            gauss_elimination(double a[][4])   (Routine gauss.c)          */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <math.h>
#include "functions.h" 
#include "intersection.h"
#include "gls_solution.h"
#include "subvolume3D.h"


int intersection_nodes_plane_plane(struct fracture elem_frac[],
                                   struct point pt_intersect[])
{ 
   int i, k, n, m;          
   int sum_pt = 0;               /* interner Summenzahler der Schnittpunkte */
                                 /* je Betrachtung Schnitt Element--Element */
   int value_return = 0;         /* return Wert default auf 0 setzen        */

   struct point b0,b1;   
   struct point hp_intersect;  /* Schnittpunkt (im Wertebereich? -> testen) */
   
   struct fracture element0, element1;                     /* Hilfsvariable */

   int index_cramer;              /* return value of the function cramer()  */

   for (i=0; i<2 && sum_pt<2 ; i++)
   {
      if (i==0)
      {
         element0 = elem_frac[0];         /* Element 0: Kanten als Geraden */ 
         element1 = elem_frac[1];         /* Element 1: als Element        */
      }
      else if (i==1)
      {
         element0 = elem_frac[1];         /* Element 1: Kanten als Geraden */
         element1 = elem_frac[0];         /* Element 0: als Element        */
      }

      /**********************************************************************/
      /*                                                                    */
      /*  Die 4 Elementkanten von Ebene 0 werden als 4 Geraden dargestellt  */
      /*  Jede Gerade (Ebene 0) mit Ebene 1 schneiden und deren             */
      /*  Schnittpunkte bestimmen                                           */
      /*                                                                    */
      /*  Sind schon zwei Schnittpunkte gefunden, so darf die for-Schleife  */
      /*  nicht mehr initialisiert werden!                                  */
      /**********************************************************************/
      for (k = 0; k<4 && sum_pt<2; k++)
      {
         m = k;
         n = m+1;
         if (k == 3)  n = 0;
         b0 = element0.pt[m];     /* Geradengl.: x = b0 + lambda *(b1 - b0) */
         b1 = element0.pt[n]; 


         /*******************************************************************/
         /* maximal sind bei Schnitt (Ebene -- Ebene) 2 Schnittpkte moeglich*/
         /*                                                                 */
         /* ACHTUNG:  noch einbauen, untersuchen !!!                        */
         /*      1.]   Fall Kante auf Kante (beruehren sich in einem Punkt  */
         /*            bzw. entlang einer Kante) --> nur ein Schnittpunkt ! */
         /*      2.]   Fall zwei Kluftebenen liegen parallel zu einander    */
         /*            bzw. aufeinander (ueberlappen sich)                  */
         /*      3.]   Fall Schnittpunkt liegt nicht innerhalb des          */
         /*            Untersuchungsgebietes                                */
         /* ACHTUNG:                                                        */
         /*                                                                 */
         /*   -->  die Summe der ermittelten Schnittpkt ist noch kleiner 2  */
         /*                                                                 */
         /*******************************************************************/
         if (sum_pt < 2)
         {
            /* Anwendung der Cramerschen Regel auf das lineare GLS */
            index_cramer = 
			      cramer(b0,b1,element1.pt[0],element1.pt[1],element1.pt[3]);

            if (index_cramer == 1)
			{
               /*************************************************************/
               /* 1.) pruefen ob GLS (berechnet in cramer)                  */
               /*     eindeutige Loesung besitzt (isnan( x_gauss[] ) == 0)  */
               /* 2.) wenn ja: pruefen ob Schnittpkt hp_intersect im        */
               /*     Wertebereich der Geraden k und im Wertebereich der    */
               /*     Kluft liegt                                           */
               /*************************************************************/
               if (isnan(x_cramer[0]) == 0 && isnan(x_cramer[1]) == 0  
                                        && isnan(x_cramer[2]) == 0)
               {
                  hp_intersect.x = b0.x + x_cramer[0] * (b1.x - b0.x);
                  hp_intersect.y = b0.y + x_cramer[0] * (b1.y - b0.y);
                  hp_intersect.z = b0.z + x_cramer[0] * (b1.z - b0.z);

                  /**********************************************************/
                  /* liegt hp_intersect im Wertebereich der Geraden k ?     */
                  /**********************************************************/
                  if (  (abs_vec_pt_pt(&b0, &hp_intersect) <= 
                                                  abs_vec_pt_pt(&b0, &b1)) 
                      &&(abs_vec_pt_pt(&b1, &hp_intersect) <= 
                                                  abs_vec_pt_pt(&b0, &b1)))
                  {

                     /********************************************************/
                     /* Zwei Bedingungen muessen erfuellt sein               */
                     /* (AH,17.05.01: Bedingung 2. neu hinzugefuegt!)        */
                     /*                                                      */
                     /* 1. liegt hp_intersect im Wertebereich der Kluftebene?*/
                     /* 2. wenn sum_pt > 0:                                  */
                     /*    hp_intersect != pt_intersect[sum_pt-1]            */
                     /*                                                      */
                     /* JA: beide if -- Bedingung erfuellt                   */
                     /*     hp_intersect  --> pt_intersect[sum_pt] zuweisen  */
                     /*                                                      */
                     /********************************************************/
                     if (PointIsInQuadrilateral_3D(hp_intersect,
                                                    element1.pt,
                                                    element1.norm) > 3)
                     {
                        if (sum_pt == 0) {
                           pt_intersect[sum_pt] = hp_intersect;
                           ++sum_pt;
                        }
                        else {
                           if(Point1_equal_Point2(hp_intersect, 
                                                  pt_intersect[sum_pt-1]) !=1)
                           {
                              pt_intersect[sum_pt] = hp_intersect;
                              ++sum_pt;
                           }
                        }
                     }
                  }
               }
            } 
         }

         /*******************************************************************/
         /* existieren                                                      */
         /*       zwei  Schnittpunkte  -->  return-Wert = 1                 */
         /*       keine Schnittpunkte  -->  return-Wert = 0  (default Wert) */
         /*******************************************************************/
         if (sum_pt == 2)  value_return = 1;
      }
   }
   return value_return;
}


/****************************************************************************/
/* intersection_node_plane_line                                             */
/*                                                                          */
/* Schneiden sich Gerade und Ebene 'plane' in einem Punkt 's_pt_intersect'  */
/*                                                                          */
/* Annahme: Die Gerade liegt nicht in der Ebenen --> sonst koennten auch    */
/*          zwei Schnittpunkte moeglich sein!!!                             */
/*                                                                          */
/*             /                                                            */
/*         ---/---------                                                    */
/*         | /         |                                                    */
/*         |/          |                                                    */
/*         /           |                                                    */
/*        /|           |                                                    */
/*       / |___________|                                                    */
/*                                                                          */
/****************************************************************************/
int intersection_node_line_plane(struct point b0, struct point b1,
                                 struct fracture plane,     
                                 struct point *s_pt_intersect)
{ 
   int index_return = 0;         /* return Wert default auf 0 setzen        */

   struct point hp_intersect;  /* Schnittpunkt (im Wertebereich? -> testen) */

   int index_cramer;              /* return value of the function cramer()  */
		 
   /* Anwendung der Cramerschen Regel auf das lineare GLS */
   index_cramer = cramer(b0,b1,plane.pt[0],plane.pt[1],plane.pt[3]);

   if (index_cramer == 1)
   {
      /**********************************************************************/
      /* 1.) pruefen ob GLS (berechnet in cramer)                  */
      /*     eindeutige Loesung besitzt (isnan( x_gauss[] ) == 0)  */
      /* 2.) wenn ja: pruefen ob Schnittpkt hp_intersect im        */
      /*     Wertebereich der Geraden k und im Wertebereich der    */
      /*     Kluft liegt                                           */
      /**********************************************************************/
      if (isnan(x_cramer[0]) == 0 && isnan(x_cramer[1]) == 0  
                                  && isnan(x_cramer[2]) == 0)
      {
         hp_intersect.x = b0.x + x_cramer[0] * (b1.x - b0.x);
         hp_intersect.y = b0.y + x_cramer[0] * (b1.y - b0.y);
         hp_intersect.z = b0.z + x_cramer[0] * (b1.z - b0.z);

         /**********************************************************/
         /* liegt hp_intersect im Wertebereich der Geraden k ?     */
         /**********************************************************/
         if (  (abs_vec_pt_pt(&b0, &hp_intersect) <= 
                                         abs_vec_pt_pt(&b0, &b1)) 
             &&(abs_vec_pt_pt(&b1, &hp_intersect) <= 
                                         abs_vec_pt_pt(&b0, &b1)))
         {

            /*******************************************************/
            /* liegt hp_intersect im Wertebereich der Kluftebene?  */
            /* JA: if -- Bedingung erfuellt                        */
            /*     hp_intersect  --> s_pt_intersect zuweisen       */
            /*                                                     */
            /*******************************************************/
            if (PointIsInQuadrilateral_3D(hp_intersect,
                                          plane.pt,
                                          plane.norm) > 3)
            {
               *s_pt_intersect = hp_intersect;
               index_return = 1;
            }
         }
      }
   } 
   return index_return;
}



/****************************************************************************/
/* intersection_node_line_polygon()                                         */
/*                                                                          */
/* PURPOSE: Do a line and a polygon intersect each other?                   */
/*          Does the calculated intersection points lay within the          */
/*          defined section of the line and of the polygon?                 */
/*                                                                          */
/*          The plane is defined by three points which lay in the plane.    */
/*          --> Three points of the polygon are taken: plane[0]             */
/*                                                     plane[1]             */
/*                                                     plane[npoint-1]      */
/*              (equirement: polygon has at least three points)             */
/*                                                                          */
/*                                                                          */
/* ASSUMPTION!!!  Polygon has to be parallel to the (xy)-coordinate plane,  */
/*          because the function 'PointIsInPolygon_2D' just compare the     */
/*          x- and y- coordinate values!!!                                  */
/*                                                                          */
/*                                                                          */
/* ARGUMENTS:                                                               */
/*     struct point b0, b1          : two points which define the line      */
/*     int npoint                   : # of polygon points                   */
/*     struct point plane           : list of the polygon points            */
/*     struct point *s_pt_intersect : pointer to the intersection point,    */
/*                                    which has to determine                */
/*                                                                          */
/*                                                                          */
/* RETURN : = +1  if there is an intersection point 's_pt_intersect'        */
/*          =  0  if there is no intersection point 's_pt_intersect'        */
/*                                                                          */
/****************************************************************************/
int intersection_node_line_polygon(struct point b0, struct point b1,
                                   int npoint,
                                   struct point *plane,     
                                   struct point *s_pt_intersect)
{ 
   int index_return = 0;         /* return Wert default auf 0 setzen        */

   struct point hp_intersect;                         /* intersection point */

   int index_cramer;              /* return value of the function cramer()  */

		 
   /* Apply the Cramer formular to the linear system of equations */
   index_cramer = cramer(b0,b1,plane[0],plane[1],plane[npoint-1]);

   if (index_cramer == 1)
   {
      /**********************************************************************/
      /* 1.) check if Cramers formular has a solution                       */
      /*     --> (isnan( x_gauss[] ) == 0)                                  */
      /* 2.) YES: check if the intersection points lays within the defined  */
      /*     section of the line and of the polygon                         */
      /**********************************************************************/
      if (isnan(x_cramer[0]) == 0 && isnan(x_cramer[1]) == 0  
                                  && isnan(x_cramer[2]) == 0)
      {
         hp_intersect.x = b0.x + x_cramer[0] * (b1.x - b0.x);
         hp_intersect.y = b0.y + x_cramer[0] * (b1.y - b0.y);
         hp_intersect.z = b0.z + x_cramer[0] * (b1.z - b0.z);

         /*******************************************************************/
         /* Does hp_intersect lay in the defined section of the line?       */
         /*******************************************************************/
         if (  (abs_vec_pt_pt(&b0, &hp_intersect) <= 
                                         abs_vec_pt_pt(&b0, &b1)) 
             &&(abs_vec_pt_pt(&b1, &hp_intersect) <= 
                                         abs_vec_pt_pt(&b0, &b1)))
         {

            /****************************************************************/
            /* Does hp_intersect lay in the defined section of the polygon? */
            /* Check by applying the function 'PointIsInPolygon_2D()'       */
            /*                                                              */
            /* Remark: The function 'PointIsInPolygon_2D()' jsut focus on a */
            /*   2dim problem. In our case the top plane of the prisma is   */
            /*   parllel to the (xy)-coordinate plane. So the z_coordinates */
            /*   of the points of the polygon 'prisma_top_pt' are constant. */
            /*   --> Assumption of 2dim problem is o.k.                     */
            /*                                                              */
            /* YES: *s_pt_intersect = hp_intersect                          */
            /*      index_retunr = +1                                       */
            /*                                                              */
            /****************************************************************/
            if (1 == PointIsInPolygon_2D(npoint, 
                                         plane, 
                                         hp_intersect.x, hp_intersect.y))
            {
               *s_pt_intersect = hp_intersect;
               index_return = 1;
            }
         }
      }
   } 
   return index_return;
}




/****************************************************************************/
/* intersection_node_line_line                                              */
/*                                                                          */
/* Is there an intersection point within the defined sector of line line[0] */
/* and line[1]? In general: line[0] = g0, line[1] = g1                      */
/*     g0:  x = pt0 + lambda * (pt1 - pt0) = pt0 + lambda * a               */
/*     g1:  x = pt2 +    mue * (pt3 - pt2) = pt1 +    mue * b               */
/*                                                                          */
/* --> Linear equation system: 3 equations with 2 unknowns (lambda, mue)    */
/*       pt0.x + lambda * (pt1.x - pt0.x) = pt2.x + mue * (pt3.x - pt2.x)   */
/*       pt0.y + lambda * (pt1.y - pt0.y) = pt2.y + mue * (pt3.y - pt2.y)   */
/*       pt0.z + lambda * (pt1.z - pt0.z) = pt2.z + mue * (pt3.z - pt2.z)   */
/*                                                                          */
/*                   -->   lambda * (pt1-pt0) - mue * (pt3-pt2) = pt2-pt0   */
/*   with the variables a,b,c:i    lambda * a - mue *     b     =  c        */
/*                                                                          */
/* first: check, if line[0] || line[1]                                      */
/*                                                                          */
/*                                                                          */
/* RETURN :index_return = 0 --> s_pt_intersect lays not within the two edges*/
/*                      = 1 --> s_pt_intersect lays within the two edges    */
/*                                                                          */
/****************************************************************************/
int intersection_node_line_line(struct edge line[],
                                struct point *s_pt_intersect)
{
   int index_return = 0;
   int index_case = 0;

   double lambda, mue;
   double help1, help2, help3, help4, help5, help6;
   double help1_abs, help2_abs, help3_abs, help4_abs, help5_abs, help6_abs;
   double direction;  
   double dist_line0, dist_line0_pt0_hp, dist_line0_pt1_hp;  
   double dist_line1, dist_line1_pt0_hp, dist_line1_pt1_hp;  
 
   /*************************************************************************/
   /* changed by Annette, 06.05.2002                                        */
   /* old value: double epsilon_inll = 10e-14;                              */
   /* new value: double epsilon_inll = 10e-14;                              */
   /*************************************************************************/
   
   double epsilon_inll = 10e-10;

   struct point a, b, c;
   struct point hp_intersect;  /* Schnittpunkt (im Wertebereich? -> testen)*/

   /* Variablen a, b und c belegen */ 
   a.x = line[0].pt1.x - line[0].pt0.x;
   a.y = line[0].pt1.y - line[0].pt0.y;
   a.z = line[0].pt1.z - line[0].pt0.z;

   b.x = line[1].pt1.x - line[1].pt0.x;
   b.y = line[1].pt1.y - line[1].pt0.y;
   b.z = line[1].pt1.z - line[1].pt0.z;

   c.x = line[1].pt0.x - line[0].pt0.x;
   c.y = line[1].pt0.y - line[0].pt0.y;
   c.z = line[1].pt0.z - line[0].pt0.z;


   /*************************************************************************/
   /* check, if 'line[0]' || 'line[1]'                                      */
   /*   YES: AreTwoLinesParallel() == 0  --> no intersection point          */
   /*   NO : AreTwoLinesParallel() == 1  --> go further on                  */
   /*************************************************************************/
   if (AreTwoLinesParallel(a, b, &direction) != 1)  
   {
      index_return = 0;   /* the two lines are parallel */
   }
   else
   {
      /**********************************************************************/
      /* Calculate the two unknowns 'lambda' and 'mue'                      */
      /* --> attention: do not divide with zero !!!                         */
      /**********************************************************************/
/**
      help1 = a.x*b.y - a.y*b.x;   help1_abs = fabs(help1); 
      help2 = a.x*b.z - a.z*b.x;   help2_abs = fabs(help2); 
      help3 = a.y*b.z - a.z*b.y;   help3_abs = fabs(help3); 
**/

      help1 = a.y*b.x - a.x*b.y;   help1_abs = fabs(help1); 
      help2 = a.z*b.x - a.x*b.z;   help2_abs = fabs(help2); 
      help3 = a.z*b.y - a.y*b.z;   help3_abs = fabs(help3); 

      if (help1_abs > epsilon_inll)
      {
         mue = (a.x*c.y - a.y*c.x) / help1; 
         index_case = 1;
      }
      else if (help2_abs > epsilon_inll)
      {
         mue = (a.x*c.z - a.z*c.x) / help2; 
         index_case = 1;
      }
      else if (help3_abs > epsilon_inll)
      {
         mue = (a.y*c.z - a.z*c.y) / help3; 
         index_case = 1;
      }

      if (index_case > 0)
      {
         if (fabs(a.x) > epsilon_inll)
         {
            lambda = (c.x + mue * b.x) / a.x;
         }
         else if (fabs(a.y) > epsilon_inll)
         {
            lambda = (c.y + mue * b.y) / a.y;
         }
         else if (fabs(a.z) > epsilon_inll)
         {
            lambda = (c.z + mue * b.z) / a.z;
         }
      }


      if (index_case == 0)
      {
/*********
         help4 = a.x*b.y - a.y*b.x;   help4_abs = fabs(help4); 
         help5 = a.x*b.z - a.z*b.x;   help5_abs = fabs(help5); 
         help6 = a.y*b.z - a.z*b.y;   help6_abs = fabs(help6); 
*********/

         help4 = a.y*b.x - a.x*b.y;   help4_abs = fabs(help4); 
         help5 = a.z*b.x - a.x*b.z;   help5_abs = fabs(help5); 
         help6 = a.z*b.y - a.y*b.z;   help6_abs = fabs(help6); 

         if (help4_abs > epsilon_inll)
         {
            lambda = (b.x*c.y - b.y*c.x) / help4; 
            index_case = 1;
         }
         else if (help5_abs > epsilon_inll)
         {
            lambda = (b.x*c.z - b.z*c.x) / help5; 
            index_case = 1;
         }
         else if (help6_abs > epsilon_inll)
         {
            lambda = (b.y*c.z - b.z*c.y) / help6; 
            index_case = 1;
         }

         if (index_case > 0)
         {
            if (fabs(b.x) > epsilon_inll)
            {
               mue = (lambda * a.x - c.x) / b.x;
            }
            else if (fabs(b.y) > epsilon_inll)
            {
               mue = (lambda * a.y - c.y) / b.y;
            }
            else if (fabs(b.z) > epsilon_inll)
            {
               mue = (lambda * a.z - c.z) / b.z;
            }
         }
      }


      /* put in 'lambda' in the equation and calculate the intersection point */
      hp_intersect.x = line[0].pt0.x + lambda * a.x;
      hp_intersect.y = line[0].pt0.y + lambda * a.y;
      hp_intersect.z = line[0].pt0.z + lambda * a.z;

      /* check if the intersection point lays within the defined sector 
         of the line */
      dist_line0        = abs_vec_pt_pt(&line[0].pt1, &line[0].pt0);
      dist_line0_pt0_hp = abs_vec_pt_pt(&line[0].pt0, &hp_intersect);
      dist_line0_pt1_hp = abs_vec_pt_pt(&line[0].pt1, &hp_intersect);

      if(fabs(dist_line0-(dist_line0_pt0_hp+dist_line0_pt1_hp)) <= epsilon_0)
      {
         dist_line1        = abs_vec_pt_pt(&line[1].pt1, &line[1].pt0);
         dist_line1_pt0_hp = abs_vec_pt_pt(&line[1].pt0, &hp_intersect);
         dist_line1_pt1_hp = abs_vec_pt_pt(&line[1].pt1, &hp_intersect);

         if(fabs(dist_line1-(dist_line1_pt0_hp+dist_line1_pt1_hp)) <= epsilon_0)
         {
             *s_pt_intersect = hp_intersect;   
             index_return = 1;
         }
      }
   }

   return index_return;
}





/****************************************************************************/
/*                                                                          */
/*                             FUNCTIONS                                    */
/*                                                                          */
/****************************************************************************/
/* abs_vec_point()                                                          */
/*                                                                          */
/*    Betrag eines Punktvektors berechnen                                   */
/*                                                                          */
/****************************************************************************/
double abs_vec_point(struct point *node)
{
   double abs_length_vec;
   double aa, bb, cc;
   
   aa = node->x * node->x; 
   bb = node->y * node->y;
   cc = node->z * node->z;
  
   abs_length_vec = sqrt(aa + bb + cc);
  
   return(abs_length_vec);
}



/****************************************************************************/
/* abs_vec_pt_pt()                                                          */
/*                                                                          */
/*   Betrag des Richtungsvektors zwischen zwei Punkten                      */
/*                                                                          */
/****************************************************************************/
double abs_vec_pt_pt(struct point *n0, struct point *n1)
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
/* abs_distance_point_line()                                                */
/*                                                                          */
/*                                                                          */
/* Purpose: Distance between a point and the perpendicular point on a line  */
/*                                                                          */
/* Arguments:                                                               */
/*   struct point *n0, *n1 :  Points which describe the line                */
/*   struct point *point   :  looking for the perpendicular point on the    */
/*                            line to this given point.                     */
/*                                                                          */
/* Return value: Absolut value of the distance between a point and the      */
/*               perpendicular point on a line.                             */
/*                                                                          */
/****************************************************************************/
double abs_distance_point_line(struct point *n0, 
                               struct point *n1,
                               struct point *point)
{
   double lambda_F;           /* Parameterwert des Lotfusspunktes F ........*/
   double distance_point_F;   /* Abstand Punkt point von der Geraden g .....*/
   double aa,bb,cc;

   struct point d, e;         /* Richtungsvektor zwischen n0 und n1 ........*/
   struct point point_F;      /* Richtungsvektor zwischen F und Punkt point */


   d.x = n1->x - n0->x; 
   d.y = n1->y - n0->y; 
   d.z = n1->z - n0->z; 

   e.x = (point->x - n0->x);
   e.y = (point->y - n0->y);
   e.z = (point->z - n0->z);


   lambda_F = ( (e.x * d.x + e.y * d.y + e.z * d.z)
               /(d.x * d.x + d.y * d.y + d.z * d.z));
               
   point_F.x = n0->x + (lambda_F * d.x) - point->x; 
   point_F.y = n0->y + (lambda_F * d.y) - point->y; 
   point_F.z = n0->z + (lambda_F * d.z) - point->z; 

   aa = (point_F.x * point_F.x); 
   bb = (point_F.y * point_F.y); 
   cc = (point_F.z * point_F.z); 
  
   distance_point_F = sqrt(aa + bb +cc); 
  
   return(distance_point_F);

}


/****************************************************************************/
/* check_point_in_rectangle()                                               */
/*                                                                          */
/*   Ueber die Entfernung des Punktes (Schnittpunkt) zu den einzelnen       */
/*   Elementkanten (Lotfusspunkt) wird ueberprueft, ob der Punkt innerhalb  */
/*   des Wertebereiches liegt                                               */
/*                                                                          */
/*   Achtung:  Anwendung nur fuer Rechtecke !!!                             */
/*                                                                          */
/****************************************************************************/
int check_point_in_rectangle(struct point *node_inter,
                             struct point *node0, 
                             struct point *node1,
                             struct point *node2, 
                             struct point *node3,
                             double *length0,
                             double *length1)
{
   int return_index = 0;        /* Index: =0 kein Schnittpkt, =1 Schnittpkt */
   int help1, help2; 
   double l_a, l_b, l_c, l_d;   /* Abstand: Intersektionspkt 
                                            -- Lotfusspkt der Elementkanten */
   l_a = abs_distance_point_line(node0, node1, node_inter);
   l_b = abs_distance_point_line(node1, node2, node_inter);
   l_c = abs_distance_point_line(node2, node3, node_inter);
   l_d = abs_distance_point_line(node3, node0, node_inter); 

/*AH, 18.02.2000. raus
   help1 = (int) (((l_a + l_c) - *length1) <= epsilon_0); 
   help2 = (int) (((l_b + l_d) - *length0) <= epsilon_0); 
*/

   help1 = (int) (((l_a + l_c) - *length1) <= epsilon_checkPoints); 
   help2 = (int) (((l_b + l_d) - *length0) <= epsilon_checkPoints); 


   
   if (help1 && help2) 
   {     
      return_index = 1;
   }
            
   return(return_index);
} 


/****************************************************************************/
/* AreTwoLinesParallel(a, b)()                                              */
/*                                                                          */
/*    Check, if two lines g0 and g1 are paralell:                           */
/*                                                                          */
/*    g0:  x = pt0 + lambda * a                                             */
/*    g1:  x = pt1 +    mue * b                                             */
/*                                                                          */
/*    -->  *direction = b/a                                                 */
/*                                                                          */
/* RETURN :index_return = 0 --> g0 || g1                                    */
/*                      = 1 --> g0 not || g1                                */
/*                                                                          */
/****************************************************************************/
int AreTwoLinesParallel(struct point a, struct point b, double *direction)
{
   int index_return = 0;
   struct point b_test;

   /*AH, 18.02.2000
      if ((fabs(a.x) > epsilon_0) && (fabs(b.x) > epsilon_0))
   */
   if ((fabs(a.x) > epsilon_checkPoints) && (fabs(b.x) > epsilon_checkPoints))
   {
      *direction = b.x / a.x;
      b_test.y   = *direction * a.y;
      b_test.z   = *direction * a.z;

      /*AH, 18.02.2000
        if ((fabs(b_test.y-b.y)<epsilon_0) && (fabs(b_test.z-b.z)<epsilon_0))
      */
      if ((fabs(b_test.y-b.y)<epsilon_checkPoints) && (fabs(b_test.z-b.z)<epsilon_checkPoints))
           index_return = 0;
      else index_return = 1;
   }

   /*AH, 18.02.2000
   else if ((fabs(a.y) > epsilon_0) && (fabs(b.y) > epsilon_0))
   */
   else if ((fabs(a.y) > epsilon_checkPoints) && (fabs(b.y) > epsilon_checkPoints))
   {
      *direction = b.y / a.y;
      b_test.x   = *direction * a.x;
      b_test.z   = *direction * a.z;
     
   /*AH, 18.02.2000
      if ((fabs(b_test.x-b.x)<epsilon_0) && (fabs(b_test.z-b.z)<epsilon_0))
   */
      if ((fabs(b_test.x-b.x)<epsilon_checkPoints) && (fabs(b_test.z-b.z)<epsilon_checkPoints))
           index_return = 0;
      else index_return = 1;
   }
  
   /*AH, 18.02.2000
   else if ((fabs(a.z) > epsilon_0) && (fabs(b.z) > epsilon_0))
   */
   else if ((fabs(a.z) > epsilon_checkPoints) && (fabs(b.z) > epsilon_checkPoints))
   {
      *direction = b.z / a.z;
      b_test.x   = *direction * a.x;
      b_test.y   = *direction * a.y;
     
   /*AH, 18.02.2000
      if ((fabs(b_test.x-b.x)<epsilon_0) && (fabs(b_test.y-b.y)<epsilon_0))
   */
      if ((fabs(b_test.x-b.x)<epsilon_checkPoints) && (fabs(b_test.y-b.y)<epsilon_checkPoints))
           index_return = 0;
      else index_return = 1;
   }
   else index_return = 1;

   return (index_return);
}

