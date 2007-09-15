/****************************************************************************/
/*                                                                          */
/* File:      graham.c                                                      */
/*                                                                          */
/* Purpose:                                                                 */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                          */
/*                                                                          */
/* Modified for the code FRAC3D                                             */
/* Modifications:                                                           */
/*   1.) Coordinate values of the points are not of the type int (as in     */
/*       the origin code). Here, they are double values.                    */
/*                                                                          */
/* Functions:                                                               */
/*                                                                          */
/****************************************************************************/
/*
This code is described in "Computational Geometry in C" (Second Edition),
Chapter 3.  It is not written to be comprehensible without the
explanation in that book.

Input: 2n integer coordinates of points in the plane. 
Output: the convex hull, cw, in PostScript; other output precedes the PS.

NB: The original array storing the points is overwritten.

Compile: gcc -o graham graham.c macros.h

Written by Joseph O'Rourke.
Last modified: October 1997
Questions to orourke@cs.smith.edu.
--------------------------------------------------------------------
This code is Copyright 1998 by Joseph O'Rourke.  It may be freely
redistributed in its entirety provided that this copyright notice is
not removed.
--------------------------------------------------------------------
*/

#include   <stdio.h>
#include   <math.h>
#include   <stdlib.h>
#include   "functions.h"
#include   "subplane3D.h"
#include   "graham.h"

#define EXIT_FAILURE   1
#define X   0
#define Y   1
typedef enum { FALSE, TRUE }   bool;

#define DIM 2                  /* Dimension of points */
typedef double tPointd[DIM];   /* Type double point, AH 09.08.99 */


/*----------Point(s) Structure-------------*/
typedef struct tPointStructure tsPoint;
typedef tsPoint *tPoint;


struct tPointStructure { int vnum;
                         tPointd v;
                         bool delete;
};


/* Global variables */
#define PMAX    1000               /* Max # of points */
typedef tsPoint tPointArray[PMAX];
static tPointArray P;
int n = 0;                         /* Actual # of points */
int ndelete = 0;                   /* Number deleted */

/*----------Stack Structure-------------*/
typedef struct tStackCell tsStack; /* Used on in NEW() */
typedef tsStack *tStack;
struct tStackCell {
   tPoint   p;
   tStack   next;
};


/*----------Function Prototypes-------------*/
tStack  Pop( tStack s );
void    PrintStack( tStack t );
tStack  Push( tPoint p, tStack top );
tStack  Graham( int n, tPointArray P);   /* tStack  Graham( void); */
void    Squash(int *n, tPointArray P);
void    Copy( int i, int j );
void    PrintPostscript( tStack t );
int     Compare( const void *tp1, const void *tp2 );
void    FindLowest(int n, tPointArray P);
void    Swap( int i, int j );

int     AreaSign( tPointd a, tPointd b, tPointd c );
double  Area2( tPointd a, tPointd b, tPointd c );
bool    Left( tPointd a, tPointd b, tPointd c );

void    PrintPoints( int n, tPointArray P);


/****************************************************************************/
/* GrahamConvexHull_2D()                                                    */
/*                                                                          */
/****************************************************************************/
int GrahamConvexHull_2D(struct poly_point *polygon_frac, int n)
{
   int i;
   tStack   top;

   /*************************************************************************/
   /* Assign the coordinate values of the 'struct poly_point *polygon_frac' */
   /* to the variables, which are used within this fct 'GrahamConvexHull_2D'*/
   /* At the end, the values has to be re-assigned!                         */
   /*************************************************************************/
   FindLowest( n, P);
   for (i=0; i<n; i++)
   {   
      P[i].vnum = i;
      P[i].v[X] = polygon_frac[i].loc.x;
      P[i].v[Y] = polygon_frac[i].loc.y;
      P[i].delete = FALSE;
   }

   /* PrintPoints(n, P); */

   /*************************************************************************/
   /* FindLowest finds the rightmost lowest point and swaps with 0-th.      */
   /* The lowest point has the min y-coord, and amongst those, the          */
   /* max x-coord: so it is rightmost among the lowest.                     */
   /*************************************************************************/
   FindLowest( n, P);

   /* PrintPoints(n, P); */ 


   qsort(
      &P[1],             /* pointer to 1st elem */
      n-1,               /* number of elems */
      sizeof( tsPoint ), /* size of each elem */
      Compare            /* -1,0,+1 compare function */
   );

   /* printf("After sorting, ndelete = %d:\n", ndelete); */
   /* PrintPoints(n, P); */

   if (ndelete > 0) {
      Squash(&n, P);    /* Squash removes all elements from P marked delete. */
      /* printf("After squashing:\n");
         PrintPoints(n, P); */
   }

   top = Graham( n, P); 


   /*************************************************************************/
   /* Reassign the values to the 'struct poly_point *polygon_frac'          */
   /*************************************************************************/
   for (i=0; i<n; i++)
   {   
      polygon_frac[i].loc.x = P[i].v[X];
      polygon_frac[i].loc.y = P[i].v[Y];
   }

   return (n);

}



/****************************************************************************/
/*                                                                          */
/*                             FUNCTIONS                                    */
/*                                                                          */
/****************************************************************************/
/****************************************************************************/
/* FindLowest finds the rightmost lowest point and swaps with 0-th.         */
/* The lowest point has the min y-coord, and amongst those, the             */
/* max x-coord: so it is rightmost among the lowest.                        */
/****************************************************************************/
void FindLowest(int n, tPointArray P)
{
   int i;
   int m = 0;   /* Index of lowest so far. */

   for ( i = 0; i < n; i++ )
      if ( (P[i].v[Y] <  P[m].v[Y]) ||
          ((P[i].v[Y] == P[m].v[Y]) && (P[i].v[X] > P[m].v[X])) ) 
         m = i;
/*AH 24-08-99   printf("Swapping %d with 0\n", m); */
   Swap(0,m); /* Swap P[0] and P[m] */
}


/****************************************************************************/
/*                                                                          */
/****************************************************************************/
void Swap( int i, int j )
{
   int    temp1;
   double temp2;
   /* Uses swap macro. */

   SWAP( temp1, P[i].vnum,   P[j].vnum );
   SWAP( temp2, P[i].v[X],   P[j].v[X] );
   SWAP( temp2, P[i].v[Y],   P[j].v[Y] );
   SWAP( temp1, P[i].delete, P[j].delete );
}


/****************************************************************************/
/* Compare: returns -1,0,+1 if p1 < p2, =, or > respectively;               */
/* here "<" means smaller angle.  Follows the conventions of qsort.         */
/****************************************************************************/
int Compare( const void *tpi, const void *tpj )
{
   int a;             /* area */
   int x, y;          /* projections of ri & rj in 1st quadrant */
   tPoint pi, pj;
   pi = (tPoint)tpi;
   pj = (tPoint)tpj;

   a = AreaSign( P[0].v, pi->v, pj->v );
   if (a > 0)
      return -1;
   else if (a < 0)
      return 1;
   else 
   { /* Collinear with P[0] */
      x =  fabs( pi->v[X] -  P[0].v[X] ) - fabs( pj->v[X] -  P[0].v[X] );
      y =  fabs( pi->v[Y] -  P[0].v[Y] ) - fabs( pj->v[Y] -  P[0].v[Y] );

      ndelete++;
      if ( (x < 0) || (y < 0) ) 
      {
         pi->delete = TRUE;
         return -1;
      }
      else if ( (x > 0) || (y > 0) ) 
      {
         pj->delete = TRUE;
         return 1;
      }
      else 
      { /* points are coincident */
         if (pi->vnum > pj->vnum)
             pj->delete = TRUE;
         else 
             pi->delete = TRUE;
         return 0;
      }
   }
}


/****************************************************************************/
/* Pops off top elment of stack s, frees up the cell, and returns new top.  */
/****************************************************************************/
tStack Pop( tStack s )
{
   tStack top;

   top = s->next;
   FREE( s );
   return top;
}


/****************************************************************************/
/* Get a new cell, fill it with p, and push it onto the stack.              */
/* Return pointer to new stack top.                                         */
/****************************************************************************/
tStack Push( tPoint p, tStack top )
{
   tStack   s;

   /* Get new cell and fill it with point. */
   NEW( s, tsStack );
   s->p = p;
   s->next = top;
   return s;
}


/****************************************************************************/
/*                                                                          */
/****************************************************************************/
void PrintStack( tStack t )
{
   if (!t) printf("Empty stack\n");
   while (t) { 
      printf("vnum=%d\tx=%f\ty=%f\n", 
             t->p->vnum,t->p->v[X],t->p->v[Y]); 
      t = t->next;
   }
}

/****************************************************************************/
/* Performs the Graham scan on an array of angularly sorted points P.       */
/****************************************************************************/
/* tStack Graham( void) */
tStack Graham( int n, tPointArray P) 
{
   tStack   top;
   int i;
   tPoint p1, p2;  /* Top two points on stack. */

   /* Initialize stack. */
   top = NULL;
   top = Push ( &P[0], top );
   top = Push ( &P[1], top );

   /* Bottom two elements will never be removed. */
   i = 2;

   while ( i < n ) 
   {
      if( !top->next) printf("Error\n"),exit(EXIT_FAILURE);
      p1 = top->next->p;
      p2 = top->p;
      if ( Left( p1->v , p2->v, P[i].v ) ) 
      {
         top = Push ( &P[i], top );
         i++;
      } 
      else  top = Pop( top );
   }

   return top;

}


/****************************************************************************/
/* Squash removes all elements from P marked delete.                        */
/****************************************************************************/
void Squash( int *n, tPointArray P)
{
   int i, j;
   i = 0; j = 0;
   while ( i < *n ) {
      if ( !P[i].delete )   /* if not marked for deletion */
      { 
         Copy( i, j ); /* Copy P[i] to P[j]. */
         j++;
      }
      /* else do nothing: delete by skipping. */
      i++;
   }
   *n = j;
}


/****************************************************************************/
/*                                                                          */
/****************************************************************************/
void Copy( int i, int j )
{
   P[j].v[X] = P[i].v[X];
   P[j].v[Y] = P[i].v[Y];
   P[j].vnum = P[i].vnum;
   P[j].delete = P[i].delete;
}


/****************************************************************************/
/* Returns twice the signed area of the triangle determined by a,b,c.       */
/* The area is positive if a,b,c are oriented ccw, negative if cw,          */
/* and zero if the points are collinear.                                    */
/****************************************************************************/
/* int Area2( tPointd a, tPointd b, tPointd c ) */
double Area2( tPointd a, tPointd b, tPointd c )
{
   double index_return;

   index_return =   (b[X] - a[X]) * (c[Y] - a[Y]) 
                  - (c[X] - a[X]) * (b[Y] - a[Y]);

   return index_return;
}


/****************************************************************************/
/* Returns true iff c is strictly to the left of the directed               */
/* line through a to b.                                                     */
/****************************************************************************/
bool Left( tPointd a, tPointd b, tPointd c )
{ 
   double epsilon_temp1 = 10e-12;

   return  (Area2( a, b, c ) > epsilon_temp1);

   /* return  Area2( a, b, c ) > 0; */
}


/****************************************************************************/
/*                                                                          */
/****************************************************************************/
void PrintPoints( int n, tPointArray P)
{
   int   i;

   printf("\nPoints:\n");
   for( i = 0; i < n; i++ )
      printf("vnum=%3d, x=%f, y=%f, delete=%d\n",
             P[i].vnum, P[i].v[X], P[i].v[Y], P[i].delete);
}


/****************************************************************************/
/*                                                                          */
/****************************************************************************/
int AreaSign( tPointd a, tPointd b, tPointd c )
{
    double area2;
    double epsilon_CH = 10.e-12;

    area2 = ( b[0] - a[0] ) * (double)( c[1] - a[1] ) -
            ( c[0] - a[0] ) * (double)( b[1] - a[1] );

    /* The area should be an integer. */
    if      ( area2 >  epsilon_CH) return  1;
    else if ( area2 < -epsilon_CH) return -1;
    else                     return  0;

}

