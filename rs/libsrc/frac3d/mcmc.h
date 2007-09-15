/****************************************************************************/
/*                                                                          */
/* File:      mcmc.h                                                        */
/*                                                                          */
/* Purpose:   Markov-Chain-Monte-Carlo methode                              */
/*                                                                          */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/*                                                                          */
/****************************************************************************/

/*#define temp_step 4*/
/*#define fix_temp_step 4*/

int     nclass_distance; 
double  min_frac_distance;      /* min. value [m] of the fracture distance */
double  max_frac_distance;      /* max. value [m] of the fracture distance */

int     cdf_distance_type;   /* type of the cdf, 1 = exponential cdf       */
double  lambda_dist;         /* function parameter for the exponential cdf */

int     ninvest_plane;
                    /* # of planes on which the scanline method is applied */


/* simulated annealing and markov-chain-monte-carlo */
int    draw_nr;
int    n_iteration_mcmc;
int    temp_step; 
int    fix_temp_step;      
int    nswap; 
double Temp0;
double cool_down;     /* cool down factor: 0.9 <= anneal <= 1.0 */ 
                      /* anneal = 0.8 is to fast!               */
double object_fct_old, object_fct_new;
struct fracture drawn_FRAC;


int     nproperty; 
int     ninit_property; 
double  *init_property;    /*initial property, from 'EvaluateInitialPicture'*/

struct  class{ double lower;                   /* lower border of the class */
               double upper;                   /* upper border of the class */
               int    abs_frequ;        /* absolut frequency of elements    */
               double perc_frequ;       /* percentage frequency of elements */
} *real_class, *dream_class, *tmp_class; 


/*
struct scanline {int index_plane;    A*plane || to xz or yz coordinate plane*A
                 int nipt_onscl;       A* # of intersect points on scanline *A
                 struct point pt0;             A* "left" point of scanline  *A
                 struct point pt1;             A* "right" point of scanline *A
                 struct ipoint *ipt;    A* array of all intersection points *A
} *SCANLINE;
*/

/****************************************************************************/
/* extern variable declaration                                              */
/****************************************************************************/
extern int nscanline;                 
/*extern struct scanline *SCANLINE;*/


/****************************************************************************/
/* function declaration                                                     */
/****************************************************************************/
int EvaluateInitialPicture(double *init_property);

int SimulatedAnnealing(int ninit_property, struct class *real_class);
                         
void MarkovChainMonteCarlo(int ninit_property, struct class *real_class); 
                         
struct class *AllocateMemory_StructClass(int nclass_distance);

void SetClassValues_InitialStep(int nclass_distance,
                                struct class *real_class,
                                struct class *dream_class,
                                struct class *tmp_class);


void SetClassValues_with_Properties(int nclass_distance, 
                                    struct class *tmp_class,
                                    int nproperty, 
                                    double *init_property);  

void DrawFracture_and_ChangeCoordinate(int *draw_nr,
                                       struct fracture *drawn_FRAC);


double ObjectiveFunction(int nclass, 
                         struct class *Rclass, struct class *Dclass);

int TakeSwap_SimulaAnneal(double temp0,
                          double *object_fct_old, double *object_fct_new,
                          int draw_nr, struct fracture drawn_FRAC, 
                          int *l, FILE *f30);

int TakeSwap_MCMC(double *object_fct_old, double *object_fct_new,
                  int draw_nr, struct fracture drawn_FRAC,
                  int *l, FILE *f60);




/****************************************************************************/
/* extern function declaration                                              */
/****************************************************************************/
extern double *InitialScanlineMethod(int *nproperty,            /*scanline.h*/
                                     int ninvest_plane);

extern double *OneFracture_FixedSampleGrid_ScanlineMethod();    /*scanline.h*/


