/****************************************************************************/
/*                                                                          */
/* File:      area_computation.h                                            */
/*                                                                          */
/* Purpose:   header file for area_computation.c                            */
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
/* variables                                                                */
/****************************************************************************/

int do_SA_MCMC;      /*index variable =0: don't start SA-, MCMC optimization*/
                     /*               =1: start SA-, MCMC optimization      */

/*int index_frac_gen_type; AH,06.02.2001 */

/****************************************************************************/
/* extern variables                                                         */
/****************************************************************************/

extern int sum_subvol_edges;   /* subvolume3D.h*/
extern int sum_subplane3D;     /* subplane3D.h*/
  
extern void subplane_3D_intersection_FRAC();     /*subplane3D.h*/
extern void subplane_3D_intersection_TRACE();    /*subplane3D.h*/

  

/****************************************************************************/
/* extern function declaration:                                             */
/****************************************************************************/
extern int subvolume_3D_intersection_FRAC();               /* subvolume3D.h */

extern int subvolume_3D_intersection_TRACE();              /* subvolume3D.h */


extern void output_FRAC (struct fracture **FRAC,           /* file_output.h */
                         struct edge     **EDGE3D,
                         struct vertex   **VERTEX3D);

extern void output_TRACE (struct trace  **TRACE,           /* file_output.h */
                          struct vertex **VERTEX3D);




