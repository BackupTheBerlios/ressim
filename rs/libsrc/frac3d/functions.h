/*****************************************************************************/
/* File:      functions.h                                                    */
/*                                                                           */
/* Purpose:   headerfile for the declaration                                 */
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
#include "parameter.h"



/*****************************************************************************/
/* functions                                                                 */
/*****************************************************************************/
void read_rseed();     /* read start number to activate the random generator */

int  read_frac_gen_type();               /* read type of fracture generating */

void read_domain();           /* read coordinate nodes of the 3D domain size */

void area_computation();     /*differentiated computation 
			       in dependence to geneation*/

int test_parallel(int frac_nr,
		  struct point norm,
		  struct point pt[4],
		  double frac_aperture); 

FILE *open_file(char **filename, int type);


/*****************************************************************************/
/* distribution-functions                                                    */
/*****************************************************************************/

double normvert(double xrand);

double lognormvert(double xrand, double sigma, double mue);

double expvert(double xrand, double lambda);

double erl2vert(double xrand, double lambda, double x0, double epsilon);

double glvert(double xrand);

double poisvert(double xrand, double lamda);

struct point kluftor_sphere_fisher_AH(double Azimut, 
				      double Dip, 
				      double kappa);

struct point kluftor_norm_nonvariat(double alpha, 
				    double Phi);


/*****************************************************************************/
/* list-functions                                                            */
/*****************************************************************************/

void read_pointfile(FILE *PF);

void read_inputfile(FILE *IF, struct uservar **uvar);

int get_var_integer(struct uservar *uvar, char *str);

double get_var_double(struct uservar *uvar, char *str);

char *get_var_char(struct uservar *uvar, char *str);

void print_varlist(struct uservar *uvar);

void gen_fracture_list();

void gen_random_fracture_list();

void gen_static_fracture_list(FILE *PF);

void gen_static_trace_list(FILE *PF);


/*****************************************************************************/
/* functions for elementlist generation                                      */
/*****************************************************************************/

struct fracture *add_FRAC_to_list (int frac_nr,
				  double length[2],
				  double diagonal[2],
				  struct point norm,
				  struct point pt[4],
				  double frac_aperture);

struct fracture *insert_FRAC_into_list(int frac_nr,
                                       double length[2],
                                       double diagonal[2],
                                       struct point norm,
                                       struct point pt[4],
                                       double frac_aperture,
                                       int *current_size);

struct trace *add_TRACE_to_StructTraceList(int trace_nr,
                                           double length,
                                           struct point pt[2],
                                           double frac_aperture,
                                           int *current_size);

struct trace *add_EDGE2D_to_StructTraceList(int trace_nr,
                                            double length,
                                            struct point pt[2],
                                            double frac_aperture,
                                            int *current_size);


struct vertex *add_VERTEX3D_to_list(int vertex_nr,
                                    struct point s_pt_intersect);

struct edge *add_EDGE3D_to_list(int edge_nr,
                                struct point pt_intersect[2],
                                int i,
                                int j);

/*****************************************************************************/
/* computation of Intersections                                              */
/*****************************************************************************/

void intersection_FRAC(struct fracture **FRAC);

void intersection_TRACE(struct trace **TRACE);


/*****************************************************************************/
/* subdomains 2D, 3D                                                         */
/*****************************************************************************/

int subdomain_3D_intersection_FRAC();

void subdomain_3D_intersection_TRACE();


/*****************************************************************************/
/* preprocessing for outputfile net generator ART                            */
/*****************************************************************************/
void PreNetgenOutput_2dFRAC_in3d(int );

void PreNetgenOutput_1dTRACE_in3d(int );

void PreNetgenOutput_1dTRACE_in2d(int nvertex_nr,
                                  struct vertex *VERTEX3D,
                                  int trace_nr,
                                  struct trace *EDGE2D,
                                  int nsubplane,
                                  struct fracture *Plane,  /*Subplane3D[i],*/
                                  struct trace *subpl_trace3D,
                                  int file_number);



/*****************************************************************************/
/* optimization                                                              */
/*****************************************************************************/
void SA_MCMC_method( ); 

