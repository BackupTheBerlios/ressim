/*****************************************************************************/
/* File:      file_output.h                                                  */
/*                                                                           */
/* Purpose:   headerfile for file_output.c                                   */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                           */
/*                                                                           */
/* Remarks:                                                                  */
/*                                                                           */
/*****************************************************************************/


/****************************************************************************/
/* data structures:                                                         */
/****************************************************************************/




/****************************************************************************/
/* extern data structures:                                                  */
/****************************************************************************/
extern double *percentage_orient_sim;                         /* geometry.h */
                    /* Prozentanteil der Hauporientierung in der Simulation */
extern double frac_dens_3d_sim; /*generated fracture density*//* geometry.h */

extern int frac_dens_type;                                    /* geometry.h */

extern double const_length; /*constant value for all trace lengths*/
                                                              /* geometry.h */


/****************************************************************************/
/* function declaration: subdomain_3D.c                                     */
/****************************************************************************/
void output_FRAC (struct fracture **FRAC, 
                  struct edge     **EDGE3D,
                  struct vertex   **VERTEX3D);


void output_TRACE (struct trace  **TRACE,
                   struct vertex **VERTEX3D);


void write_log_file_FRAC(FILE *f1, struct fracture *FRAC); 


void write_log_file_TRACE(FILE *f1, struct trace *TRACE); 


void write_tec_file_FRAC(FILE *f2, struct fracture *FRAC);


void write_tec_file_TRACE(FILE *f2, struct trace *TRACE);


void write_ART_tec_file_FRAC(FILE *f3, FILE *f7, FILE *f8);


void write_ART_tec_file_TRACE(FILE *f3, FILE *f7, FILE *f8);


void write_ART_file_Subplane(FILE *f3);


void write_EDGE3D_file(FILE *f5, struct edge *EDGE3D);


void write_VERTEX3D_file(FILE *f6, struct vertex *VERTEX3D);

