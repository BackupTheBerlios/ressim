/****************************************************************************/
/*                                                                          */
/* File:      parallel.h                                                    */
/*                                                                          */
/* Purpose:   header file for intersection routine                          */
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
double epsilon_alpha; 



/****************************************************************************/
/* extern data structures                                                   */
/****************************************************************************/
extern double x_cramer[3];       /* Loesungsvektor A*x =b, aus gls_loeser.h */
                                 /* Calculation with the cramer rule........*/
                                 /* x_cramer[0] == lambda (Geradengl.) .....*/
                                 /* x_cramer[1] == mue    (Ebenengl.) ......*/
                                 /* x_cramer[2] == eta    (Ebenengl.) ......*/


/****************************************************************************/
/* function declarations                                                    */
/****************************************************************************/

/****************************************************************************/
/* extern function declarations                                             */
/****************************************************************************/
/*intersection.h*/
extern double approx_radius_sphere(int k, struct fracture *FRAC, 
                                   struct point *sphere_midpt);

extern double abs_vec_pt_pt(struct point *node0, 
                            struct point *node1);


