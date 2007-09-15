/****************************************************************************/
/*                                                                          */
/* File:      gls_solution.c                                                */
/*                                                                          */
/* Purpose:                                                                 */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                          */
/* Remarks:   cramer rule                                                   */
/*            gauss elemination                                             */
/*                                                                          */
/* Functions:                                                               */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "functions.h" 
#include "gls_solution.h"
#define SWAP(a,b) {temp=(a); (a)=(b); (b)=temp;}



/****************************************************************************/
/* Cramersche Regel fuer lineares Gleichungssystem mit drei Gleichungen     */
/*    und drei Unbekannten                                                  */
/*    A * x = b                                                             */
/*    Voraussetzung: Determinante von A ist ungleich Null   det(A) != 0     */
/*                                                                          */
/*    return value: 0 = no solution of the linear equation system           */
/*                      --> no intersection point                           */
/*                                                                          */
/*                  1 = solution of the linear equation system              */
/*                      --> there is intersection point                     */
/*                                                                          */
/****************************************************************************/
int cramer(struct point node_b0, struct point node_b1,
           struct point node_a0, struct point node_a1, 
           struct point node_a3)
{
   int index_return = 0;
  
   double b1, b2, b3;
   double A11, A12, A13;
   double A21, A22, A23;
   double A31, A32, A33;
   double detA, detA1, detA2, detA3;

   double epsilon_cramer = 10e-10;

   
   /*************************************************************************/
   /* Matrix mit Werten belegen                                             */
   /*************************************************************************/
   A11 = node_b1.x - node_b0.x;
   A12 = node_a0.x - node_a1.x;
   A13 = node_a0.x - node_a3.x;

   A21 = node_b1.y - node_b0.y;
   A22 = node_a0.y - node_a1.y;
   A23 = node_a0.y - node_a3.y;

   A31 = node_b1.z - node_b0.z;
   A32 = node_a0.z - node_a1.z;
   A33 = node_a0.z - node_a3.z;

   /*************************************************************************/
   /* Rechteseite Vektor mit Werten belegen                                 */
   /*************************************************************************/
   b1 = node_a0.x - node_b0.x;
   b2 = node_a0.y - node_b0.y;
   b3 = node_a0.z - node_b0.z;


   /*************************************************************************/
   /* Determinaten berechnen, um die Cramersche Regel anwenden zu koennen   */
   /* muss gelten: detA != 0  bzw. fabs(detA) >= epsilon_0                  */
   /*************************************************************************/
   detA =   A11*A22*A33 - A11*A23*A32
          - A12*A21*A33 + A12*A23*A31
          + A13*A21*A32 - A13*A22*A31;
   
   if (fabs(detA) >= epsilon_cramer)		
   {
      detA1 =   b1*A22*A33 + A12*A23*b3 + A13*b2*A32
              - A13*A22*b3 - b1*A23*A32 - A12*b2*A33;

      detA2 =   A11*b2*A33 + b1*A23*A31 + A13*A21*b3
              - A13*b2*A31 - A11*A23*b3 - b1*A21*A33;

      detA3 =   A11*A22*b3 + A12*b2*A31 + b1*A21*A32
              - b1*A22*A31 - A11*b2*A32 - A12*A21*b3;


      /**********************************************************************/
      /* Loesungsvektor berechnen                                           */
      /**********************************************************************/
      x_cramer[0] = detA1 / detA;
      x_cramer[1] = detA2 / detA;
      x_cramer[2] = detA3 / detA;

	  index_return = 1;
   }
    
   return index_return;
}


/****************************************************************************/
/* Cramersche Regel:  VERSUCH !!!                                           */
/*                                                                          */
/****************************************************************************/
int cramer_AH(double A[][3], double b[])
{
   int index_return = 0;
  
   double detA, detA0, detA1, detA2;
   double epsilon_cramer = 10e-10;

   
   /*************************************************************************/
   /* Determinaten berechnen, um die Cramersche Regel anwenden zu koennen   */
   /* muss gelten: detA != 0  bzw. fabs(detA) >= epsilon_0                  */
   /*************************************************************************/
   detA =   A[0][0]*A[1][1]*A[2][2] - A[0][0]*A[1][2]*A[2][1]
          - A[0][1]*A[1][0]*A[2][2] + A[0][1]*A[1][2]*A[2][0]
          + A[0][2]*A[1][0]*A[2][1] - A[0][2]*A[1][1]*A[2][0];

   if (fabs(detA) >= epsilon_cramer)		
   {
      detA0 =   b[0]*A[1][1]*A[2][2] + A[0][1]*A[1][2]*b[2] 
              + A[0][2]*b[1]*A[2][1]
              - A[0][2]*A[1][2]*b[2] - b[0]*A[1][2]*A[2][1] 
              - A[0][1]*b[1]*A[2][2];

      detA1 =   A[0][0]*b[1]*A[2][2] + b[0]*A[1][2]*A[2][0]  
              + A[0][2]*A[1][1]*b[2]
              - A[0][2]*b[1]*A[2][0] - A[0][0]*A[1][2]*b[2] 
              - b[0]*A[1][0]*A[2][2];

      detA2 =   A[0][0]*A[1][1]*b[2] + A[0][1]*b[1]*A[2][0] 
              + b[0]*A[1][0]*A[2][1]
              - b[0]*A[1][1]*A[2][0] - A[0][0]*b[1]*A[2][1] 
              - A[0][1]*A[1][0]*b[2];


      /**********************************************************************/
      /* Loesungsvektor berechnen                                           */
      /**********************************************************************/
      x_cramer[0] = detA0 / detA;
      x_cramer[1] = detA1 / detA;
      x_cramer[2] = detA2 / detA;

	  index_return = 1;
   }
    
   return index_return;
}


/****************************************************************************/
/* Gauss Eliminationsverfahren                                              */
/****************************************************************************/
void gauss_elimination(double AA[][4])
{
  int i;
  int N = 2;
  int u=0, v=0, w=0, max=0 ;
  
  double t;
  double b[3][4];
  
  for (i=0; i <= N; i++)
    {
      b[i][0] = AA[i][0];
      b[i][1] = AA[i][1];
      b[i][2] = AA[i][2];
      b[i][3] = AA[i][3];
    }
  
  for (u = 0; u <= N; u++)      /* Vorwaerts-Elimination ...............*/
    {
      max = u;
      
      for (v = u+1; v <= N; v++)
	if ( abs(b[v][u]) > abs(b[max][u]) ) max = v;
      
      for (w = u; w <= N+1; w++)
	{ 
	  t = b[u][w];
	  b[u][w] = b[max][w];
	  b[max][w] = t; 
	}
      
      for (v = u+1; v <= N; v++)
	for (w = N+1; w >= u; w--)  b[v][w] -= b[u][w] * b[v][u] / (b[u][u]+10e-16);
    }
  
  
  for (v = N; v >= 0; v--)      /* Backsubstitution .....................*/
    {
      t = 0.0;
      
      for (w = v+1; w <= N; w++)   t = t + (b[v][w] * x_gauss[w]) ;
      
      x_gauss[v] = (b[v][N+1] - t) / (b[v][v]+10e-16);
    }
  
  
  /*JZ200799  printf("\ngauss_elimination "); 
    printf("\nx_gauss[0]= %6.3f \t x_gauss[1]= %6.3f \t x_gauss[2]= %6.3f\n",
	 x_gauss[0], x_gauss[1] ,x_gauss[2]);*/
  
}


/****************************************************************************/
/* matrix_AA_fill                                                           */
/****************************************************************************/
void matrix_AA_fill(struct point node_b0,
                    struct point node_b1,
                    struct point node_a0,
                    struct point node_a1,
                    struct point node_a3)
{
   AA_gauss[0][0] = node_b1.x - node_b0.x ;
   AA_gauss[0][1] = node_a0.x - node_a1.x ;
   AA_gauss[0][2] = node_a0.x - node_a3.x ;
   AA_gauss[0][3] = node_a0.x - node_b0.x ;

   AA_gauss[1][0] = node_b1.y - node_b0.y ;
   AA_gauss[1][1] = node_a0.y - node_a1.y ;
   AA_gauss[1][2] = node_a0.y - node_a3.y ;
   AA_gauss[1][3] = node_a0.y - node_b0.y ;

   AA_gauss[2][0] = node_b1.z - node_b0.z ;
   AA_gauss[2][1] = node_a0.z - node_a1.z ;
   AA_gauss[2][2] = node_a0.z - node_a3.z ;
   AA_gauss[2][3] = node_a0.z - node_b0.z ;

}


int *ivector(int nl, int nh);


/****************************************************************************/
/* Gauss-Jordan elimination with full pivoting                              */
/* (after 'numerical recipes', p. 36)                                       */
/****************************************************************************/
void GaussJordan(double a[][3], double b[][3], int n, int m)
{
   int *indxc, *indxr, *ipiv;
   int i, icol, irow, j, k, l, ll; 

   double big, dum, pivinv, temp;


   /*************************************************************************/
   /* allocate memory                                                       */
   /*************************************************************************/
   indxc = ivector(0,n);
   indxr = ivector(0,n);
   ipiv  = ivector(0,n);
   
   for (j=0; j<n; j++) ipiv[j] = 0; 

   for (i=0; i<n; i++)            /*main loop over the columns to be reduced*/
   {
      big = 0.0;
      for (j=0; j<n; j++)      /*outer loop of the search for apivot element*/
      {
         if (ipiv[j] != 1)
         {
            for (k=0; k<n; k++) 
            {
               if (ipiv[k] == 0) 
               {
                  if (fabs(a[j][k]) >= big)
                  {
                     big = fabs(a[j][k]);
                     irow = j;
                     icol = k;
                  }
               }
               else if (ipiv[k] > 1) printf("\n GAUSSJ: Singular Matrix-1 \n");
			}      
         }
      }
      ++(ipiv[icol]);    
   

      /**********************************************************************/
      /* We now have the pivot element, so we interchanges rows, if needed  */
      /* to put the pivot element on the diagonal. The columns are not      */
      /* physically interchanged, only relabeled: indxc[i], the column of   */
      /* the i-th pivot element, is the i-th column that is reduced, while  */
      /* indxr[i] is the row in which that pivot element was originally     */
      /* located. if indxr[i] != indxc[i] there is ab implied column        */
      /* interchange. With this form of bookkeeping, the solution b's will  */
      /* end up in the correct order, and the inverse matrix will be        */
      /* scrambled by columns.                                              */
      /**********************************************************************/
      if (irow != icol)
      {
         for (l=0; l<n; l++) SWAP(a[irow][l], a[icol][l]);
         for (l=0; l<n; l++) SWAP(b[irow][l], b[icol][l]);
      }


      /*we are ready to divide the pivot row by the pivot element, 
        located at irow and icol*/
      indxr[i] = irow;
      indxc[i] = icol; 
      if (a[icol][icol] == 0.0)  printf("\nGAUSSJ: Singular Matrix-2 \n");
      pivinv = 1.0/a[icol][icol];
      a[icol][icol] = 1.0;
	  
      for (l=0; l<n; l++)  a[icol][l] *= pivinv;
      for (l=0; l<m; l++)  b[icol][l] *= pivinv;

      for (ll=0; ll<n; ll++)  /*Next, we reduce the rows ...*/
      {
         if (ll != icol)         /*... except for the pivot one, of course*/
         {
            dum = a[ll][icol];
            a[ll][icol] = 0.0;
            for (l=0; l<n; l++) a[ll][l] -= a[icol][l]*dum;
            for (l=0; l<m; l++) b[ll][l] -= b[icol][l]*dum;
         }
      }
   }
   
   /*************************************************************************/
   /* It only remains to unscramble the solution in view of the column      */
   /* interchanges. We do this by interchanging pairs of columns in the     */
   /* reverse order that the permutation was buil up.                       */
   /*************************************************************************/
   for (l=(n-1); l>=0; l--)
   {
      if (indxr[l] != indxc[l])
	  for (k=0; k<n; k++) SWAP(a[k][indxr[l]], a[k][indxc[l]]);
   }

   free(ipiv);
   free(indxc);
   free(indxr);
}


/****************************************************************************/
/* Allocates an int vector with range [nl...nh]                             */
/****************************************************************************/
int *ivector(int nl, int nh)
{
   int *v;
   
   v = (int *) malloc((unsigned) (nh-nl+1) * sizeof(int)); 
   if (!v)
   {
      fprintf(stderr,"Memory allocation failed for ... (gaussjordan)");
      exit (-1);
   }
   return v-nl;
}

