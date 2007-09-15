/****************************************************************************/
/*                                                                          */
/* File:      parameter.h                                                   */
/*                                                                          */
/* Purpose:   headerfile for the declaration                                */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/****************************************************************************/


/*****************************************************************************/
/*  Globale Variablen                                                        */
/*****************************************************************************/

/*AH16.03.00 int frac_dens_3d;*/ /* Kluftdichte [frac/m^3]                   */
double frac_dens_3d;           /* Kluftdichte [frac/m^3]                     */

int frac_gen_type;             /* determine kind of generating proces        */

int nfrac;                     /* sum of all fracture planes, domain         */
int nfrac_det;                 /* sum of all deterministic fracture planes   */
int ntrace;                    /* sum of all fracture tracer lines, domain   */
int nvertex_nr;                /* sum of all intersection-Points of VERTEX3D */
int edge_nr_2D, edge_nr_3D;

int nvertex_net, nedge_net, nface_net, nelement_net;

double frac_surface_det;       /*total surface of the deterministic fractures*/

double domain_volume;           /* Volumen des Untersuchungsgebietes (domain)*/

double ran3(double *);          /* aus Numerical recipes, p. 212-213         */

double rseed;                   /* rseed = Startzahl Zufallsgenerator        */
double *idum;       /* greift auf das Objekt zu, auf den der Zeiger verweist */

/*double trace_length;*/

static double epsilon_0 = 10e-10;
static double epsilon_checkPoints = 10e-6;


struct uservar    { char *typ;
                    char *name;
                    char *value;
                    int zeile;
} *uvar;


/* length: two length values which define the size of the fracture */
struct length     { double l0; 
                    double l1;
} frac_length;



struct point      { int pt_nr;
                    double x;
                    double y;
                    double z;
} dom_min, dom_max, pt[4], norm, mid_pt, pt_intersect[2], 
  qube[8];



struct poly_point { int pt_nr;
                    struct point glob;
                    struct point loc;
};


struct vertex     { int vertex_nr;
                    struct point pt;   /* single point */
                    int inside_subd3D; /* = -1: vertex outside subdomain */
                                       /* = +1: vertex inside  subdomain */
}*VERTEX3D;




struct edge       { int edge_nr;     
                    struct point pt0; 
                    struct point pt1;    
                    int plane_nr[2];
                    int inside_subd3D; /* = -1: edge total outside subdomain */
                                       /* = +1: edge total inside  subdomain */
                    int cw;            /* control word to assign boundary condition */
}*EDGE3D;



struct fracture   { int frac_nr;
                    double length[2];      /* just valid for rectangle fracture */
                                           /* length[0]  : pt[0] -- pt[1]  */
                                           /* length[1]  : pt[0] -- pt[3]  */
                    double diagonal[2];    /* diagonal length of the plane */
                                           /* diagonal[0]: pt[0] -- pt[2]  */
                                           /* diagonal[1]: pt[1] -- pt[3]  */
                    struct point norm;
                    struct point pt[4];
                    double frac_aperture;  /* aperture of the fracture planes   */
                    int inside_subd3D;     /* = -1: fracture total outside subd */
                                           /* = +1: fracture total inside  subd */
                    struct point *ch;      /* polygon points for the convex hull
                                              --> output description (netgen)   */
                    int index_ch;    /* = -1: fracture is still a rectangle     */
                                     /* = +1: fracture described as convex hull */
                    int sum_ptch;    /* # of polygon points of the convex hull  */
} *FRAC;


struct trace      { int nr;                           /*fracture trace number*/
                    double length;                    /*fracture trace length*/
                    struct point pt[2];  /*fracture trace start and end point*/
                    double frac_aperture;    /*aperture of the fracture trace*/
                    int nvertex_on;  /*sum of all VERTEX3D points laying on trace*/
                    int *vertex_on; /*number of VERTX3D point laying on trace*/
                    int inside_subd3D;  /*= -1: trace total outside subdomain*/
                                        /*= +1: trace total inside  subdomain*/
} *TRACE, *EDGE2D; 


/*****************************************************************************/
/*  variables: netgenerator ART                                              */
/*****************************************************************************/

struct vertex_net { int pt_nr_old;
                    int pt_nr;     
                    int appear_net; /* = -1: point not longer part of the net   
                                      >=  0: point part of the net (default)    
                                             # = how often the Vertex 
                                             appears in the net describtion  */ 
                    struct point pt; 
} *VERTEX_net;


struct edge_net   { int edge_nr;     
                    struct vertex_net pt0; 
                    struct vertex_net pt1;    
                    int plane_nr[2];
                    int appear_net; /* = -1: edge not longer part of the net   
                                      >= +1: edge part of the net (default)  */    
                    int cw;     /* control word to assign boundary condition */
}*EDGE_net;


struct face_net   { int face_nr;
                    int sum_edges;
                    int *EDGE_NR;
                    int cw;          /* control word to assign boundary condition */
} *FACE_net;


struct element_net{ int element_nr;
                    int sum_faces;
                    int *FACE_NR;
                    int cw;          /* control word to assign boundary condition */
} *ELEMENT_net;


/*****************************************************************************/
/*  variables: scanline method for sa-, mcmc- methode (optimization)         */
/*****************************************************************************/
struct ipoint     { int fracnr;
                    double x;
                    double y;
                    double z;
}; 

struct scanline {int index_plane;     /*plane || to xz or yz coordinate plane*/
                 int nipt_onscl;      /* # of intersect points on scanline   */
                 struct point pt0;    /* "left" point of scanline            */
                 struct point pt1;    /* "right" point of scanline           */
                 struct ipoint *ipt;  /* array of all intersection points    */
} *SCANLINE;


