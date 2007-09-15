/****************************************************************************/
/*                                                                          */
/* File:      subplane3D.h                                                  */
/*                                                                          */
/* Purpose:   header file for subplane3D.h                                  */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/****************************************************************************/


/****************************************************************************/
/* data structures exported by the corresponding source file                */
/****************************************************************************/

int sum_subplane3D;

struct fracture *Subplane3D;   /* contains the node  data of the subplane 3D*/
struct trace subpl_trace3D[4]; /* contains the trace data of the subplane 3D*/


/****************************************************************************/
/* extern variables                                                         */
/* --> from pre_netgen_outp.h                                               */
/****************************************************************************/
extern int nvertex_total, nedge_total, nface_total, nelement_total; 
extern int nedge_on_face_total;              
extern int nedge_on_face;      /*FACE is divided on # of EDGE_net segments  */
extern int ncw_edge;           /*start number for the controll words        */
extern int ncw_face;           /*start number for the controll words        */
extern int ncw_fracture;       /*fixed number for the fracture controll word*/
 
extern double epsilon_length;                            /*pre_netgen_outp.h*/

struct edge_distance {double length;  
                            int old_nr;
};


/****************************************************************************/
/* function declaration                                                     */
/****************************************************************************/

void subplane_3D_intersection_FRAC();

void subplane_3D_intersection_TRACE();

int gen_static_subplane3D_list(FILE *PF, struct fracture subplane3D[]);

struct fracture assign_coordinates_to_element(int *index_plane, 
                                              struct point *node_plane); 

int frac_element_beside_subplane(int *index_plane,
                                 struct fracture *plane,
                                 int,  
                                 struct fracture *FRAC,
                                 struct fracture elem_frac[2]); 

void AssignSubplane3DValues(int i, 
                            struct fracture *Subplane3D);

void AssignSubplane3D_to_subpl_trace3D(int i,
                                       struct fracture *Subplane3D,
                                       struct trace *subpl_trace3D);

void AssignVertexToSubplaneTrace3D(int i, struct trace *subpl_trace3D,
                                  int *nvertex_nr);

void AssignVertexToEDGE2D(int i, 
                          struct trace *EDGE2D);


/****************************************************************************/
/* extern function declaration                                              */
/****************************************************************************/
extern int Point1_equal_Point2( struct point a,             /*subdomain_3D.h*/
                                struct point b);          

/*from intersection.h*/
extern double abs_distance_point_line(struct point *n0, 
                                      struct point *n1,
                                      struct point *point);

extern double abs_vec_point(struct point *node);

extern double abs_vec_pt_pt(struct point *node0,
                            struct point *node1);


extern void write_netgen_file(FILE *f3);                     /*file_output.h*/

/*from pre_netgen_outp.h*/
extern int DistanceCompare(const void *p1, const void *p2);

extern struct vertex_net *AllocateStructVertex_NetList(int n); /*pre_netgen_outp.h*/

extern struct edge_net *AllocateStructEdge_NetList(int n);

extern struct face_net *AllocateStructFace_NetList(int n);

extern struct edge_distance *AllocateStructEdgeDistanceList(int n);

extern int FourPointsLayInPlane(struct point *p0, struct point *p1, /*geometry.h*/
                                struct point *p2, struct point *p3);

extern void transform_coord_3D_2D(int nvertex_net,     /*transformation3D_2D.h*/
                                  struct vertex_net VERTEX_net[]);


