/****************************************************************************/
/*                                                                          */
/* File:      transformation3D_2D.h                                         */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/****************************************************************************/


/****************************************************************************/
/* data structures exported by the corresponding source file                */
/****************************************************************************/



/****************************************************************************/
/* extern variables                                                         */
/****************************************************************************/




/****************************************************************************/
/* function declaration                                                     */
/****************************************************************************/
void transform_coord_3D_2D(int nvertex_net, 
                           struct vertex_net VERTEX_net[]);


/****************************************************************************/
/* extern function declaration                                              */
/****************************************************************************/
extern void GaussJordan(double a[][3], double b[][3], int n, int m);
                                                       /*gls_loeser.h*/

extern void RotationMatrix3D(struct point node0, struct point node1,
                             struct point node3, struct point normal,
                             double A[3][3]);                /*subvolume3D.h*/



