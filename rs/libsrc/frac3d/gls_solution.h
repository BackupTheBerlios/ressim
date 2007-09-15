/****************************************************************************/
/*                                                                          */
/* File:      gls_loeser.h                                                  */
/*                                                                          */
/* Purpose:   header file for function file gls_loeser.c                    */
/*                                                                          */
/* Author:    Annette Hemminger (AH)                                        */
/*            Institut fuer ComputerAnwendungen im Bauing.wesen             */
/*            TU Braunschweig                                               */
/*            Pockelsstrasse 3                                              */
/*            38106 Braunschweig                                            */
/*            email: a.hemminger@tu-bs.de                                   */
/*                                                                          */
/* History:   13.11.98 begin                                                */
/* modified   28.07.99 (AH)                                                 */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/****************************************************************************/




/****************************************************************************/
/* data structures exported by the corresponding source file                */
/****************************************************************************/


double AA_gauss[3][4];               /* Matrix 3*4 des lin. Gl.systems .....*/
                                     /* beinhaltet A und b aus A*x = b .....*/
double x_gauss[3];                   /* Loesungsvektor aus  A*x = b ........*/

double x_cramer[3];                  /* Loesungsvektor aus  A*x = b ........*/

/* #define SWAP(a, b) {double temp(a); (a) = (b); (b) = temp;} */

/****************************************************************************/
/* function declarations                                                    */
/****************************************************************************/

void gauss_elimination(double a[][4]);  

void matrix_fill(struct point, struct point, struct point,
                 struct point, struct point);

int cramer(struct point, struct point, struct point,
           struct point, struct point);

int cramer_AH(double A[][3], double b[]);

void GaussJordan(double a[][3], double b[][3], int n, int m); 

