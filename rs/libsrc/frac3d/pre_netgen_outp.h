/****************************************************************************/
/*                                                                          */
/* File:      pre_netgen_outp.h                                             */
/*                                                                          */
/* Purpose:   header file for pre_netgen_outp.h routine                     */
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
/* data structures: pre_netgen_outp.c                                       */
/****************************************************************************/
int nvertex_total, nedge_total, nface_total, nelement_total; 
int nedge_on_face_total;
int nedge_on_face = 0;       /* FACE is divided on # of EDGE_net segments   */

int ncw_edge     =  1;       /* start number for the controll words         */
int ncw_face     =  1;       /* start number for the controll words         */
int ncw_element  =  1;       /* start number for the controll words         */
int ncw_fracture = -1;       /* start number for the fracture controll word */
int ncw_fracture_edge=-1; /*start number of the fracture egdes controll word*/

double epsilon_length;       /*minimum length for the 1D elements which are 
                                  written to the meshgenerating output file */
                             

struct edge_distance {double length;
                      int old_nr;
};

struct trace *LINES;

/*************************************************************************/
/* Extern variables from header file 'subvolume_3D.h'                    */
/*************************************************************************/
extern int sum_subvol_edges;        /* Anzahl der Ecken der Grundflaeche */

extern struct point *subvol_bot_pt;
               /* array with the edge points of the prisma (bottom plane)*/
extern struct point *subvol_top_pt;
                  /* array with the edge points of the prisma (top plane)*/

extern struct fracture *subvol_side;
                               /* array of the faces of the prisma sides */

extern struct point subplane3D[4];    
extern struct trace subpl_trace3D[4]; 



/****************************************************************************/
/****************************************************************************/
/* function declaration                                                     */
/****************************************************************************/
/****************************************************************************/

void Subvolume_To_NetStructure(
          int sum_subvol_edges,
          struct point *subvol_bot_pt, struct point *subvol_top_pt,
          struct vertex_net *VERTEX_net, int *nvertex_net,
          struct edge_net *EDGE_net, int *nedge_net, int *ncw_edge,
          struct face_net *FACE_net, int *nface_net, int *ncw_face,
          struct element_net *ELEMENT_net, int *nelement_net, int *ncw_element
          ); 


void FRAC_To_NetStructure(struct fracture *FRAC,
                          int nfrac,
                          struct vertex_net *VERTEX_net,
                          int *nvertex_net,
                          struct edge_net *EDGE_net,
                          int *nedge_net,
                          int *ncw_edge,
                          struct face_net *FACE_net,
                          int *nface_net,
                          int *ncw_fracture);

void Lines3D_To_NetStructure(int nloop,
                             struct trace *LINES,
                             struct vertex_net *VERTEX_net,
                             int *nvertex_net,
                             struct edge_net *EDGE_net,
                             int *nedge_net);



void VERTEX3D_To_NetStructure(int nvertex_nr,
                              int *nvertex_net,
                              struct vertex_net *VERTEX_net,
                              struct vertex *VERTEX3D);


void Subplane_To_NetStructure(struct trace *subpl_trace3D,
                              int *nsubplane,
                              struct fracture *Plane,
                              struct vertex *VERTEX3D,
                              struct vertex_net *VERTEX_net,
                              int *nvertex_net,
                              struct edge_net *EDGE_net,
                              int *nedge_net,
                              int *nedge_on_face,
                              int *ncw_edge,
                              struct face_net *FACE_net,
                              int *nface_net,
                              int *ncw_face);


void EDGE2D_To_NetStructure(int trace_nr,
                            struct trace *EDGE2D,
                            struct vertex *VERTEX3D,
                            struct vertex_net *VERTEX_net,
                            int *nvertex_net,
                            struct edge_net *EDGE_net,
                            int *nedge_net,
                            int *ncw_fracture);


struct edge_distance *AllocateStructEdgeDistanceList(int n);

struct vertex_net *AllocateStructVertex_NetList(int n);

struct edge_net *AllocateStructEdge_NetList(int n);

struct face_net *AllocateStructFace_NetList(int n);

struct element_net *AllocateStructElement_NetList(int n);

struct trace *AllocateStructTraceList(int n);

void write_output_Subplane_tecplot(int file_number,
                                   int nvertex_net, 
                                   struct vertex_net VERTEX_net[],
                                   int nedge_net,   
                                   struct edge_net EDGE_net[]);

void write_output_Subplane_netgenerator(int file_number);


/****************************************************************************/
/* extern function declaration: build_list.h                                */
/****************************************************************************/
extern struct point *AllocateStructPointList(int n);


/****************************************************************************/
/* extern function declaration: subdomain_3D.h                              */
/****************************************************************************/

extern int Point1_equal_Point2(struct point a, struct point b);

extern int Point1_equal_Point2_epsilonlarge(struct point a, struct point b);

int DistanceCompare(const void *p1, const void *p2);


/****************************************************************************/
/* extern function declaration: intersection.h                              */
/****************************************************************************/
extern double abs_vec_pt_pt(struct point *node0, struct point *node1);


/****************************************************************************/
/* extern function declaration: transformation3D_2D.h                       */
/****************************************************************************/
extern void transform_coord_3D_2D(int nvertex_net,
                                  struct vertex_net VERTEX_net[]);


/****************************************************************************/
/* extern function declaration: file_output.h                               */
/****************************************************************************/
extern void write_ART_file_Subplane(FILE *f3);                 /*file_output.h*/

