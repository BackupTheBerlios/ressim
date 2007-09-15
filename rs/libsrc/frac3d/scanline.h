/****************************************************************************/
/*                                                                          */
/* File:      scanline.h                                                    */
/*                                                                          */
/* Purpose:   Scanline technique                                            */
/*                                                                          */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/*                                                                          */
/****************************************************************************/


/****************************************************************************/
/* variable of the function ScanlineMethod()                                */
/*                                                                          */
/****************************************************************************/

int nscl_perplane;                           /* # of scanlines per subplane */
int nscanline;                               /* total # of scanlines        */

int index_plane; 
            /* = 1: subplane || (yz) coordinate plane: x=const  , y variate */
            /* = 2: subplane || (xz) coordinate plane: x variate, y=const   */

int npt_on_scanline;      /* number of intersection points on all scanlines */


/****************************************************************************/
/* extern variable                                                          */
/****************************************************************************/
extern int ninvest_plane;                                         /* mcmc.h */
extern int nproperty;                                             /* mcmc.h */
/*extern struct scanline *SCANLINE;*/                             /* mcmc.h */


/****************************************************************************/
/* Functions                                                                */
/****************************************************************************/
double *InitialScanlineMethod(int *nproperty, 
                              int ninvest_plane);

double *OneFracture_FixedSampleGrid_ScanlineMethod(int *nproperty,
                                                   int draw_nr);

struct point *AddScanlinePtToList(int slpt_nr,
                                  struct point node);

struct scanline *AllocateStructScanlineList(int n);

int PointCompareX(const void *p1, const void *p2);

int PointCompareY(const void *p1, const void *p2);

/*struct point *AllocateStructPointList(int n); */

double abs_vec_ipt_ipt(struct ipoint *n0, struct ipoint *n1);

/***
void SortIptOnScanline_and_CalculateDistProperty(int ii,
                                                 int *nproperty_old,
                                                 int *nproperty_new,
                                                 int npt_on_scanline,
                                                 double *property_dist);
***/

/****************************************************************************/
/* extern function declaration                                              */
/****************************************************************************/
extern struct point *AllocateStructPointList(int n);          /*build_list.h*/

extern double radius_sphere(int, struct fracture *FRAC);    /*intersection.h*/

extern double abs_distance_point_line(struct point *n0,     /*intersection.h*/
                                      struct point *n1,
                                      struct point *point);

extern double abs_vec_pt_pt(struct point *node0,            /*intersection.h*/
                            struct point *node1);


int intersection_node_line_plane( struct point b0,          /*intersection.h*/
                                  struct point b1, 
                                  struct fracture plane,
                                  struct point *s_pt_intersect);


