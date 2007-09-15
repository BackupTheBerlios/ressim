/****************************************************************************/
/*                                                                          */
/* File:      fracvert.h                                                    */
/*                                                                          */
/* Author:    Annette Hemminger (AH)                                        */
/*            Institut fuer ComputerAnwendungen im Bauing.wesen             */
/*            TU Braunschweig                                               */
/*            Pockelsstrasse 3                                              */
/*            38106 Braunschweig                                            */
/*            email: a.hemminger@tu-bs.de                                   */
/*                                                                          */
/* History:   25.01.2000  (modified)                                        */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/****************************************************************************/


/****************************************************************************/
/* data structures: fracvert.c                                              */
/****************************************************************************/

/* variables for the functions arguments of the function GaussJordan() */
double B_GJ[3][3];
int n_gj, m_gj;


/****************************************************************************/
/* function declaration: fracvert.c                                         */
/****************************************************************************/


/****************************************************************************/
/* extern function declaration:                                             */
/****************************************************************************/

extern void GaussJordan(double a[][3], double b[][3], int n, int m);

