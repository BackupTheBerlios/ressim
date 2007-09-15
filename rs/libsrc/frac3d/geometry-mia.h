/*****************************************************************************/
/*                                                                           */
/* File:      geometry.h                                                     */
/*                                                                           */
/* Purpose:                                                                  */
/*                                                                           */
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


/*****************************************************************************/
/*  Geometrie-Eigenschaften der random Kluefte generieren                    */
/*  Funktion:  gen_random_fracture_list                                      */
/*****************************************************************************/
int parallel;
   
double cos_phi = 0.0;      /* Winkelmass: Normalenvektor-Projektionsvektor 
                              (xy-Ebene)*/
double phi = 0.0;          /* Winkel (siehe cos_phi), im Bogenmass  */
double phi_grad = 0.0;     /* Winkel im Gradmass (Kontrolle)  */

double lambda1 = 0.0;      /* Faktor in der Geradengleichung g */
double lambda2 = 0.0;      /* Faktor in der Geradengleichung g */
double bb = 0.0;           /* Betrag |r| des Vektors vector_r  */
double rr = 0.0;           /* Betrag |b| des Vektors vector_r  */
  
struct point help_pt = {0, 0.0, 0.0, 0.0}; 
struct point midpt;
  
struct point  vector_b;          /* Hilfsvektor  */
struct point  vector_r;          /* Hilfsvektor  */
   
int length_dist_type;             /*Index fuer die Kluftlaengenverteilung */ 
int aperture_dist_type;       /* Index fuer die Oeffnungsweitenverteilung */


int orientation_type;     /*Index fuer die Berechnung des Normalenvektors */
int norient;                        /* Anzahl der Hauptorientierungen     */
double *percentage_orient;          /* Prozentanteil der Hauporientierung */
double *percentage_orient_sim;      
                  /* Prozentanteil der Hauporientierung in der Simulation */
double *alpha;                      /* Fallrichtung , Fallazimut          */
double *Phi;                        /* Fallwinkel                         */
double *kappa;                      /* concentration parameter            */
char char_percentage_orient[20], char_alpha[20]; 
char char_Phi[20], char_kappa[20];
  

double sigma, mue;
double xrand, lambda_h, X0_h, lambda_v, X0_v, epsilon_Erlang;
double aperture, frac_aperture;
     
double const_length;                /*constant value for all trace lengths*/

int    frac_dens_type;              /*indicate the type of fracture density*/
double frac_surface;                /*total fracture surface in the domain*/
double frac_dens_3d_sim;            /*fracture density of the generated field*/
/* Mia 2D 22.07.2002 */
double trace_length;                /*total trace lenth in the domain*/
double trace_dens_2d_sim;           /*tracer density of the generated field*/

/*****************************************************************************/
/*  Geometrie-Eigenschaften der statischen Kluefte generieren                */
/*  Funktion:  gen_static_fracture_list                                      */
/*****************************************************************************/
char line[512];
char *rptr;

int listengroesse = 0;

double axb, betr_axb;


/*****************************************************************************/
/*  functions                                                                */
/*****************************************************************************/
int FourPointsLayInPlane(struct point *p0, struct point *p1,
                         struct point *p2, struct point *p3); 

/*****************************************************************************/
/*  extern functions                                                         */
/*****************************************************************************/
extern double abs_vec_pt_pt(struct point *node0, struct point *node1);

extern double abs_vec_point(struct point *node);  /*intersection.h*/

