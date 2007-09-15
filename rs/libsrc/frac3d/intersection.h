/****************************************************************************/
/*                                                                          */
/* File:      intersection.h                                                */
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

int sum_cube_x ;         /* Anzahl der cubes in x-Richtung */
int sum_cube_y ;
int sum_cube_z ;

double delta_cube_x;     /* length of the x-side of the cube */
double delta_cube_y;
double delta_cube_z;

#define max1 10000
struct point inside_node[max1]; 
struct point s_pt_intersect;

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
double abs_vec_point(struct point *node);


double abs_vec_pt_pt(struct point *node0, struct point *node1);


void assign_frac_to_cube(double, double, double, struct fracture *FRAC); 


void calculate_delta_cube(double *delta_cube_x, double *delta_cube_y,   
                          double *delta_cube_z, int *sum_cube_x,   
                          int *sum_cube_y, int *sum_cube_z);
 

int check_point_in_rectangle(struct point *node_inter,
                             struct point *node0,
                             struct point *node1,
                             struct point *node2,
                             struct point *node3,
                             double *length0,
                             double *length1);


double abs_distance_point_line(struct point *n0,
                               struct point *n1,
                               struct point *point);


double distance_midpoint_midpoint(struct point pt_i0, struct point pt_i2,
                                  struct point pt_j0, struct point pt_j2);


int intersection_nodes_plane_plane(struct fracture elem_frac[2],
                                   struct point pt_intersect[2]);


int intersection_node_line_plane( struct point b0, struct point b1,
                                  struct fracture plane,
                                  struct point *s_pt_intersect); 

int intersection_node_line_polygon(struct point b0, struct point b1,
                                   int npoint,
                                   struct point *plane,
                                   struct point *s_pt_intersect);


int intersection_node_line_line(struct edge line[2],
                                struct point *s_pt_intersect);


double approx_radius_sphere(int, struct fracture *FRAC, struct point *sphere_midpt);


double radius_sphere(int, struct fracture *FRAC);


int AreTwoLinesParallel(struct point a, struct point b, double *direction);



/****************************************************************************/
/* extern function declaration: subdomain_3D.h                              */
/****************************************************************************/

extern int Point1_equal_Point2(struct point a, struct point b);
 
extern int PointIsInQuadrilateral_3D(struct point A,   
                                     struct point node[4],
                                     struct point n);

