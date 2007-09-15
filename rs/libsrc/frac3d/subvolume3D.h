/****************************************************************************/
/*                                                                          */
/* File:      subvolume3D.h                                                 */
/*                                                                          */
/* Purpose:   header file for subvolume3D routine                           */
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
/* data structures: subvolume3D.c                                           */
/* --> subvolume_3D_intersection_FRAC()                                     */
/****************************************************************************/
int subvolume_type;      /*=1: irregulare quader, =2: regulare quader/prisma*/ 

double radius1;            /* radius of sphere surrounding fracture plane */
double sum_radius1_2;      /* = radius1 + radius_prisma_side */

struct point center1, center2;         /* center point of sphere (plane) */
double distance_points;      /* distance between two midpoints of planes */

struct fracture elem_frac[2];     /* Zur Elementuebergabe fuer die Fkt   */
                                  /* intersection_nodes_plane_plane()    */

int help_add;             /* help variable */

struct poly_point *polygon_frac;   /* array of points, which lay on the 
                                      fracture plane */

int sum_polypt;         /* sum of the number of poly points which are    */
                        /* which are included in the list 'polygon_frac' */


double ROT[3][3];              /* rotation matrix: spherical rotation of
                                              a cartesian coordinate system */
double ROT_i[3][3];            /* inverse rotation matrix */

/* variables for the functions arguments of the function GaussJordan() */
double B_GJ[3][3];
int n_gj, m_gj;

/*double epsilon_Point1_equal_Point2=10e-4;*/   /*fct 'Point1_equal_Point2()'*/
/*AH24.11.00: double epsilon_Point1_equal_Point2 = 10e-6;*/  /*fct 'Point1_equal_Point2()'*/


/****************************************************************************/
/* --> subvolume_3D_intersection_TRACE()                                    */
/****************************************************************************/
struct point subplane3D[4];    /* contains the node data of the subplane 3D */
struct trace subpl_trace3D[4]; /* contains the trace data of the subplane 3D*/



/****************************************************************************/
/* variables which are needed for the file 'prisma.c' and 'quader.c'        */
/****************************************************************************/
int sum_subvol_edges;                 /* number of edges of the ground plane*/

double subvol_radius_bot;     /*radius of the circle around bottom/top plane*/

double *subvol_radius_side;   /* radius of sphere arround subvol side plane */

struct point subvol_bot_midpt;             /* middle point subvolume bottom */
struct point subvol_top_midpt;             /* middle point subvolume top    */
struct point *subvol_side_midpt;          /* middle point of the side planes*/

struct point *subvol_bot_pt;/*array with the edge points of the bottom plane*/
struct point *subvol_top_pt;  /* array with the edge points of the top plane*/

struct fracture *subvol_side;     /* Array of the faces of the subvol sides */

double subvol_side_length;                 /* vertical length of the subvol */




/****************************************************************************/
/* function declaration: subvolume_3D.c                                     */
/****************************************************************************/
int subvolume_3D_intersection_FRAC();

int subvolume_3D_intersection_TRACE();


struct point center_point(int, struct fracture *FRAC);


int PointIsOnLineSegment_2D(double Cx, double Cy, 
                            double Ax, double Ay, 
                            double Bx, double By);

int PointIsInPolygon_2D(int npol, struct point *pt_polyline, 
                        double x, double y); 

int PointIsInQuadrilateral_3D(struct point A,
                              struct point node[4],
                              struct point n);

void RotationMatrix3D(struct point node0, struct point node1,
                      struct point node3, struct point normal, 
                      double A[3][3]);


double CosOfTwoVectors(struct point a, struct point b);

int Point1_equal_Point2(struct point a, struct point b);

int Point1_equal_Point2_epsilonlarge(struct point a, struct point b);

int PointInSubvolume(struct point node);  


struct poly_point *AddToPolygonList(int *sum_polygon_pt,
                                    struct point node);  

struct point *AllocateStructPointList(int n);  

struct fracture *AllocateStructFractureList(int n);  

void CoordinateTransformation(int i, 
                              struct poly_point *polygon_frac,
                              struct point O, double ROT_i[3][3]); 

void CoordinateBackTransformation(int i, 
                                  struct poly_point *polygon_frac,
                                  struct point O,
                                  double ROT[3][3]); 

void CalculateSubvolPrismaVariables(int sum_subvol_edges,
                                    double *subvol_radius_bot,
                                    double *subvol_radius_side,
                                    struct point *subvol_bot_midpt,
                                    struct point *subvol_top_midpt,
                                    struct point *subvol_side_midpt,
                                    struct point *subvol_bot_pt,
                                    struct point *subvol_top_pt,
                                    struct fracture *subvol_side);

void CalculateSubvolQuaderVariables(int sum_subvol_edges,
                                    double *subvol_radius_bot,
                                    double *subvol_radius_side,
                                    struct point *subvol_bot_midpt,
                                    struct point *subvol_top_midpt,
                                    struct point *subvol_side_midpt,
                                    struct point *subvol_bot_pt,
                                    struct point *subvol_top_pt,
                                    struct fracture *subvol_side);


void NewFractureBorders_inSubvolume_TouchEachOther(int *nvertex_nr);

/****************************************************************************/
/* extern function declaration:                                             */
/****************************************************************************/

extern int *Allocate1dIntArray(int n);      /* build_list.h */

extern double radius_sphere(int, struct fracture *FRAC);    /*intersection.h*/

extern double approx_radius_sphere(int k, struct fracture *FRAC, /*intersection.h*/
                                   struct point *sphere_midpt);

extern void GaussJordan(double a[][3], double b[][3], int n, int m);


