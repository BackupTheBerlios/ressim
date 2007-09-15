/*****************************************************************************/
/* File:      file_output.c                                                  */
/*                                                                           */
/* Purpose:   write output files                                             */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                *
/*                                                                           */
/*****************************************************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <math.h>
#include "functions.h" 
#include "file_output.h"


/*****************************************************************************/
/* output_FRAC()                                                             */
/*  Aufruf fuer die Ausgabe Files                                            */
/*****************************************************************************/
void output_FRAC (struct fracture **FRAC, 
                  struct edge     **EDGE3D,
                  struct vertex   **VERTEX3D)
{
  FILE *f1, *f2, *f3, *f5, *f7, *f8;

  char *file_n1, *file_n2, *file_n3, *file_n5, *file_n7, *file_n8;

  f1 = fopen(file_n1=get_var_char(uvar, "log_file"), "w");
  f2 = fopen(file_n2=get_var_char(uvar, "tec_file"), "w");
  f3 = fopen(file_n3=get_var_char(uvar, "art_file"), "w");
  f5 = fopen(file_n5=get_var_char(uvar, "intersect_file"), "w");
  f7 = fopen(file_n7=get_var_char(uvar, "arttec_file"), "w");
  f8 = fopen(file_n8=get_var_char(uvar, "artdx_file"), "w");

  write_log_file_FRAC(f1, *FRAC); 
  fprintf(stdout,"\n\tWrote %s", file_n1);

  write_tec_file_FRAC(f2, *FRAC); 
  fprintf(stdout,"\n\tWrote %s", file_n2);

  write_ART_tec_file_FRAC(f3, f7, f8);
  fprintf(stdout,"\n\tWrote %s", file_n3); 
  fprintf(stdout,"\n\tWrote %s", file_n7); 
  fprintf(stdout,"\n\tWrote %s", file_n8); 

  write_EDGE3D_file(f5, *EDGE3D);
  fprintf(stdout,"\n\tWrote %s", file_n5);

  fclose(f1);     
  fclose(f2);
  fclose(f3);
  fclose(f5);
  fclose(f7);
  fclose(f8);

  /*JZ160899 Der Speicher der durch die Listen belegt wird muss noch 
    freigegeben werden*/
  /*if (1 <= nfrac) free(FRAC);
  if (1 <= edge_nr_2D) free(EDGE2D);
  if (1 <= edge_nr_3D) free(EDGE3D);
  if (1 <= nvertex_nr) free(VERTEX3D);*/

}




/*****************************************************************************/
/* output_TRACE()                                                            */
/*  Aufruf fuer die Ausgabe Files                                            */
/*****************************************************************************/
void output_TRACE (struct trace  **TRACE,
                   struct vertex **VERTEX3D)
{
  FILE *f1, *f2, *f3, *f6, *f7, *f8; 
  
  char *file_n1, *file_n2, *file_n3, *file_n6, *file_n7, *file_n8;

  f1 = fopen(file_n1= get_var_char(uvar, "log_file"), "w");
  f2 = fopen(file_n2= get_var_char(uvar, "tec_file"), "w");
  f3 = fopen(file_n3= get_var_char(uvar, "art_file"), "w");
/*f6 = fopen(file_n6= get_var_char(uvar, "vertex3D_file"), "w"); */
  f7 = fopen(file_n7= get_var_char(uvar, "arttec_file"), "w");
  f8 = fopen(file_n8= get_var_char(uvar, "artdx_file"), "w");

/* noch TODO
  write_log_file_TRACE(f1, *TRACE); 
  fprintf(stdout,"\n\tWrote %s", file_n1);
*/

  write_tec_file_TRACE(f2, *TRACE);
  fprintf(stdout,"\n\tWrote %s", file_n2);

  write_ART_tec_file_TRACE(f3, f7, f8);
  fprintf(stdout,"\n\tWrote %s", file_n3);
  fprintf(stdout,"\n\tWrote %s", file_n7);
  fprintf(stdout,"\n\tWrote %s", file_n8);

/*write_VERTEX3D_file(f6, *VERTEX3D);
  fprintf(stdout,"\n\tWrote %s\n", file_n6); 
*/

  fclose(f1);     
  fclose(f2);
  fclose(f3);
/*fclose(f6); */
  fclose(f7);
  fclose(f8);



  /*JZ160899 Der Speicher der durch die Listen belegt wird muss noch 
    freigegeben werden*/
  /* if (1 <= nvertex_nr) free(VERTEX3D);*/

}




/*****************************************************************************/
/*                                                                           */
/*                          F U N C T I O N                                  */
/*                                                                           */
/*****************************************************************************/
/*  Schreiben des Log Files: FRAC                                            */
/*                                                                           */
/*****************************************************************************/
void write_log_file_FRAC(FILE *f1, struct fracture *FRAC) 
{
   int i, o_t, norient, l_d_t, a_d_t;
   double rseed, percent, alpha, Phi, kappa;
   double lambda_h, lambda_v, X0_h, X0_v, epsilon_Erlang; 
   double aperture, sigma, mue;
   char char_percentage_orient[21], char_alpha[20], char_Phi[20], char_kappa[20];
 
   fprintf(f1,"******************************************************************************");  
   fprintf(f1,"\n*                log file of a FRAC3D run                                    *");  
   fprintf(f1,"\n******************************************************************************\n");  


   fprintf(f1,"\n\n\n******************************************************************************");  
   fprintf(f1,"\n*                Fracture generation type                                    *");  
   fprintf(f1,"\n******************************************************************************\n");  
   frac_gen_type = get_var_integer(uvar, "frac_gen_type");
   fprintf(f1,"fracture generation type = %d", frac_gen_type);
   switch (frac_gen_type) {
      case 1:  fprintf(f1,"\n    stochastic generating approach (2D fracture planes in 3D space) \n\n");
               break;
      case 2:  fprintf(f1,"\n    deterministic approach (known 2D fracture planes in 3D space \n\n");
               break;
      case 3:  fprintf(f1,"\n    deterministic + stochastic ( known 2D + stochastic fracture planes in 3D space \n\n");
               break;
      case 4:  fprintf(f1,"\n    deterministic approach (known 1D fracture traces in 3D space \n\n");
               break;
  }
 
   rseed = get_var_double(uvar, "rseed");
   fprintf(f1,"\nInput value for random generator = %20.0f \n", rseed);

   switch (frac_dens_type) {
      case 1:  fprintf(f1,"\nFracture density [m^2/m^3]");
               fprintf(f1,"\n    input= %.3f \tsimulated= %.3f \n",
                                 frac_dens_3d, frac_dens_3d_sim);
               break;
      case 2:  fprintf(f1,"\nFracture density [fracture/m^3]");
               fprintf(f1,"\n    input= %.3f \tsimulated= %.3f \n",
                                 frac_dens_3d, frac_dens_3d_sim);
               break;
      }
      fprintf(stdout,"\nNumber of fracture elements nfrac=%d \n", nfrac);
   

   fprintf(f1,"\nSize of the generation domain (shape of a brick)"); 
   fprintf(f1,"\n    dom_min(x,y,z) \t %10.4f  %10.4f  %10.4f ",
 	             dom_min.x, dom_min.y, dom_min.z); 
   fprintf(f1,"\n    dom_max(x,y,z) \t %10.4f  %10.4f  %10.4f ",
                     dom_max.x,dom_max.y, dom_max.z); 

   fprintf(f1,"\n\nTotal number of simulated fractures \t = %d \n",nfrac );
  
  
   fprintf(f1,"\n\n\n******************************************************************************");  
   fprintf(f1,"\n*                Simulation of the normal vector of the fracture plane       *");  
   fprintf(f1,"\n******************************************************************************\n");  
   o_t = get_var_integer(uvar, "orientation_type");
   switch (o_t) {
      case 1:  fprintf(f1,"orientation_type = %d  -> no variation of azimuth and Fallwinkel\n", o_t); 
               break;
      case 2:  fprintf(f1,"orientation_type = %d  -> spherical Fisher distribution \n",o_t);
               break;
  }
  
   norient = get_var_integer(uvar, "norient");
   fprintf(f1,"\nNumber of main orientations = %d ", norient);

   if(get_var_integer(uvar, "frac_gen_type")==2)
   fprintf(f1,"\nNr. \t[\%]in  \tFallazimut \tFallwinkel \tKonzentration");
   else fprintf(f1,"\nNr. \t[\%]in  \t[\%]Simul \tFallazimut \tFallwinkel \tKonzentration");
   for (i = 0; i < norient; i++)
   {
      sprintf (char_percentage_orient, "percentage_orient[%d]",i); 
      sprintf (char_alpha, "alpha[%d]",i); 
      sprintf (char_Phi, "Phi[%d]",i); 
      sprintf (char_kappa, "kappa[%d]",i);      
      percent = get_var_double(uvar, char_percentage_orient) * 100;
      alpha = get_var_double(uvar, char_alpha);
      Phi = get_var_double(uvar, char_Phi);
      kappa = get_var_double(uvar, char_kappa);
      if (get_var_integer(uvar, "frac_gen_type")==2)
	{
	  fprintf(f1,"\n%d  \t%.2f \t%.2f \t\t%.2f \t\t%.2f", 
		  i, percent, alpha, Phi, kappa);
	}
      else
	{
	  fprintf(f1,"\n%d \t%.2f \t%.2f \t\t%.2f \t\t%.2f \t\t%.2f", 
	    i, percent, percentage_orient_sim[i]/nfrac*100, alpha, Phi, kappa);
	}
   }
  
  
   fprintf(f1,"\n\n\n******************************************************************************");  
   fprintf(f1,"\n*                Simulation of the fracture length                           *");  
   fprintf(f1,"\n******************************************************************************");  
   l_d_t = get_var_integer(uvar, "length_dist_type");
   fprintf(f1,"\nfracture length distribution = %d  --> ", l_d_t);
   switch (l_d_t) {
      case 1:  fprintf(f1,"Erlang2 distribution");
               lambda_h = get_var_double(uvar, "lambda_h_1");
               X0_h = get_var_double(uvar, "X0_h");
               lambda_v = get_var_double(uvar, "lambda_v_1");
               X0_v = get_var_double(uvar, "X0_v");
               epsilon_Erlang = get_var_double(uvar, "epsilon_Erlang");

               fprintf(f1,"\n    lambda_h\t\t %.3f", lambda_h);
               fprintf(f1,"\n    X0_h    \t\t %.3f", X0_h);
               fprintf(f1,"\n    lambda_v\t\t %.3f", lambda_v);
               fprintf(f1,"\n    X0_v    \t\t %.3f", X0_v);
               fprintf(f1,"\n    epsilon_Erlang\t %.3f", epsilon_Erlang);
               break;

      case 2:  fprintf(f1,"Exponential distribution");
               lambda_h = get_var_double(uvar, "lambda_h_2");
               lambda_v = get_var_double(uvar, "lambda_v_2");
               fprintf(f1,"\n    lambda_h\t\t %.3f", lambda_h);
               fprintf(f1,"\n    lambda_v\t\t %.3f", lambda_v);
               break;

      case 3:  fprintf(f1,"Constant length");
               const_length = get_var_double(uvar, "const_length");
               fprintf(f1,"\n    const_length \t %.3f", const_length);
               break;
   }
  
   fprintf(f1,"\n\n\n******************************************************************************");  
   fprintf(f1,"\n*                Simulation of the fracture aperture                         *");  
   fprintf(f1,"\n******************************************************************************");  
   a_d_t = get_var_integer(uvar, "length_dist_type");
   if (a_d_t == 1){
      aperture = get_var_double(uvar, "aperture");
      fprintf(f1,"\nKonstante Kluftoeffnungsweite von %.5f mm", aperture);
   }
   else{
      sigma = get_var_double(uvar, "sigma");
      mue = get_var_double(uvar, "mue");
      fprintf(f1,"\nKluftoeffnungsweite  -->  logarithmische Normalverteilung ");
      fprintf(f1,"\n\tsigma \tmue ");
      fprintf(f1,"\n\t%.3f \t%.3f", sigma, mue); 
   }

  
   fprintf(f1,"\n\n\n******************************************************************************");  
   fprintf(f1,"\n*                Geometrical values of the fracture elements                 *");  
   fprintf(f1,"\n******************************************************************************\n");  
   for (i = 0; i < nfrac; i++)
   {

      fprintf(f1,"\nKluftebene %d ",FRAC[i].frac_nr); 
      fprintf(f1,"\n    pt0(x,y,z) \t\t %10.4f  %10.4f  %10.4f ",
                   FRAC[i].pt[0].x,FRAC[i].pt[0].y,FRAC[i].pt[0].z); 
      fprintf(f1,"\n    pt1(x,y,z) \t\t %10.4f  %10.4f  %10.4f ",
                   FRAC[i].pt[1].x,FRAC[i].pt[1].y,FRAC[i].pt[1].z); 
      fprintf(f1,"\n    pt2(x,y,z) \t\t %10.4f  %10.4f  %10.4f ",
                   FRAC[i].pt[2].x,FRAC[i].pt[2].y,FRAC[i].pt[2].z); 
      fprintf(f1,"\n    pt3(x,y,z) \t\t %10.4f  %10.4f  %10.4f ",
                   FRAC[i].pt[3].x,FRAC[i].pt[3].y,FRAC[i].pt[3].z); 
      fprintf(f1,"\n    norm(x,y,z) \t %10.4f  %10.4f  %10.4f ",
                   FRAC[i].norm.x, FRAC[i].norm.y, FRAC[i].norm.z);
      fprintf(f1,"\n    side length \t %10.4f  %10.4f ",
                   FRAC[i].diagonal[0], FRAC[i].diagonal[1]);
   }
}


  
/*****************************************************************************/
/*  Schreiben des Tecplot Files                                              */
/*                                                                           */
/*****************************************************************************/
void write_tec_file_FRAC(FILE *f2, struct fracture *FRAC)
{
  int i, j, nodes;

  nodes = 4 * nfrac;
  fprintf(f2,"TITLE=\"2D fracture in 3D domain\" ");
  fprintf(f2,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
  fprintf(f2,"\nZONE N=%d, E=%d, F=FEPOINT, ET=QUADRILATERAL",nodes,nfrac);
  
  /* x-, y-, z- Koordinatenwerte   */
  for(i=0; i < nfrac; i++){
    fprintf(f2,"\n%8.5f  %8.5f  %8.5f", 
	    FRAC[i].pt[0].x, FRAC[i].pt[0].y, FRAC[i].pt[0].z); 
    fprintf(f2,"\n%8.5f  %8.5f  %8.5f", 
 	    FRAC[i].pt[1].x, FRAC[i].pt[1].y, FRAC[i].pt[1].z); 
    fprintf(f2,"\n%8.5f  %8.5f  %8.5f",
 	    FRAC[i].pt[2].x, FRAC[i].pt[2].y, FRAC[i].pt[2].z); 
    fprintf(f2,"\n%8.5f  %8.5f  %8.5f", 
	    FRAC[i].pt[3].x, FRAC[i].pt[3].y, FRAC[i].pt[3].z); 
  }
  
  /* Elementnummern  */
  j = 1;
  for(i=0; i < nfrac; i++)
    {
      fprintf(f2,"\n%d %d %d %d", j, j+1, j+2, j+3); 
      j = j+4;
    } 
  fprintf(f2,"\n"); 
}


void write_tec_file_TRACE(FILE *f2, struct trace *TRACE)
{
  int i, j, nodes;

  nodes = 2 * ntrace;
  fprintf(f2,"TITLE=\"1D fracture traces in 3D domain\" ");
  fprintf(f2,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
  fprintf(f2,"\nZONE N=%d, E=%d, F=FEPOINT, ET=TRIANGLE",nodes,ntrace);
  
  /* x-, y-, z- Koordinatenwerte   */
  for(i=0; i < ntrace; i++){
    fprintf(f2,"\n%8.5f  %8.5f  %8.5f", 
	    TRACE[i].pt[0].x, TRACE[i].pt[0].y, TRACE[i].pt[0].z); 
    fprintf(f2,"\n%8.5f  %8.5f  %8.5f", 
 	    TRACE[i].pt[1].x, TRACE[i].pt[1].y, TRACE[i].pt[1].z); 
  }
  
  /* Elementnummern  */
  j = 1;
  for(i=0; i < ntrace; i++)
    {
      j = 1+(i*2);
      fprintf(f2,"\n%d %d %d ", j, j+1, j+1); 
    } 
  fprintf(f2,"\n"); 
}



/****************************************************************************/
/* write_ART_tec_FRAC_file()                                                */
/*                                                                          */
/*  Schreiben des Files fuer den Netzgenerator                              */
/*  modified by Annette Hemminger, 24-08-1999                               */
/*                             AH  31-01-2000                               */
/*                                                                          */
/****************************************************************************/
void write_ART_tec_file_FRAC(FILE *f3, FILE *f7, FILE *f8) 
{
   int i, j;

   /*header*/
   fprintf(f3,"%%%% Version 3.0");
   fprintf(f3,"\n%%%% VertexNumber:   %d", nvertex_net);
   fprintf(f3,"\n%%%% EdgeNumber:     %d", nedge_net);
   fprintf(f3,"\n%%%% FaceNumber:     %d", nface_net);
   fprintf(f3,"\n%%%% ElementNumber:  %d", nelement_net);
   fprintf(f3,"\n%%%% DO NOT CHANGE LINES ABOVE !!!! ");
   fprintf(f3,"\n%%  NET: Vertices <-> Edges <-> Faces <-> Elements ");
   fprintf(f3,"\n%%  ");
   fprintf(f3,"\n%%  Eingabefile ART3D: erstellt von Kluftgenerator FRAC3D");
   fprintf(f3,"\n%%                     Annette Hemminger,CAB,TU Braunschweig");
   fprintf(f3,"\n%%  ");


   /*************************************************************************/
   /* all nodes: with their coordinate values x, y, z                       */
   /* - nodes of the prisma subdomain                                       */
   /* - nodes of the fracture elements (rectangle or polyline)              */
   /* - nodes of the intersection lines                                     */
   /* - nodes of the single intersection points                             */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Vertices: x y z ");
   for (i=0; i<nvertex_net; i++)
   {
      fprintf(f3,"\n%20.12f  %20.12f  %20.12f", 
              VERTEX_net[i].pt.x, VERTEX_net[i].pt.y, VERTEX_net[i].pt.z);
   }
   fprintf(f3,"\n$");


   /*************************************************************************/
   /* all edges: listed by the number of their coordinate node              */
   /* - edges of the prisma subdomain                                       */
   /* - edges of the fracture elements (rectangle or polyline)              */
   /* - edges of the intersection lines                                     */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Edges (Indices to List of Points): ");

   for (i=0; i<nedge_net; i++)
   {
      fprintf(f3,"\n%d: %d %d", 
                 EDGE_net[i].cw, EDGE_net[i].pt0.pt_nr, EDGE_net[i].pt1.pt_nr);
   }
   fprintf(f3,"\n$");


   /*************************************************************************/
   /* all faces: listed by the number of their edges                        */
   /* - faces of the prisma subdomain                                       */
   /* - faces of the fracture elements (rectangle or polyline)              */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Faces (Indices to List of Edges): ");

   for (i=0; i<nface_net; i++)
   {
      fprintf(f3,"\n%d:", FACE_net[i].cw);
      for (j=0; j<FACE_net[i].sum_edges; j++)
      {
         fprintf(f3," %d ", FACE_net[i].EDGE_NR[j] );
      }
   }
   fprintf(f3,"\n$");
  
   if (nelement_net > 0) 
   {
      /**********************************************************************/
      /* all elements: listed by the number of their outer faces (subdomain)*/
      /* --> faces of the prisma subdomain                                  */
      /*                                                                    */
      /**********************************************************************/
      fprintf(f3,"\n%% Elements (Indices to List of Faces): ");
      for (i=0; i<nelement_net; i++)
      {
         fprintf(f3,"\n%d:", ELEMENT_net[i].cw);
         for (j=0; j<ELEMENT_net[i].sum_faces; j++)
         {
            fprintf(f3," %d ", ELEMENT_net[i].FACE_NR[j] );
         }
      }
      fprintf(f3,"\n$");
   }
   fprintf(f3,"\n");


   /*************************************************************************/
   /* Tecplot                                                               */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f7,"TITLE=\"2D fracture in 3D subvolume\" ");
   fprintf(f7,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
   fprintf(f7,"\nZONE N=%d, E=%d, F=FEPOINT, ET=TRIANGLE",
                nvertex_net,nedge_net);

   /* x-, y-, z- coordinates */
   for (i=0; i<nvertex_net; i++)
   {
      fprintf(f7,"\n%12.8f  %12.8f  %12.8f", 
              VERTEX_net[i].pt.x, VERTEX_net[i].pt.y, VERTEX_net[i].pt.z);
   }

   /* edges of the polygon */
   for (i=0; i<nedge_net; i++)
   {
      fprintf(f7,"\n%d %d %d", 
      EDGE_net[i].pt0.pt_nr+1, EDGE_net[i].pt1.pt_nr+1, EDGE_net[i].pt1.pt_nr+1);
   }
   fprintf(f7,"\n");
 

   /*************************************************************************/
   /* Data Explorer  Hussam Sheta 26/05/02                                                               */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f8,"object 1 class array type float rank 1 shape 3 items %d data follows", nvertex_net);

   /* x-, y-, z- coordinates */
   for (i=0; i<nvertex_net; i++)
   {
      fprintf(f8,"\n%12.8f  %12.8f  %12.8f", 
              VERTEX_net[i].pt.x, VERTEX_net[i].pt.y, VERTEX_net[i].pt.z);
   }

   fprintf(f8,"\nobject 2 class array type int rank 1 shape 3 items %d data follows", nedge_net);

   /* edges of the polygon */
   for (i=0; i<nedge_net; i++)
   {
      fprintf(f8,"\n%d %d %d", 
      EDGE_net[i].pt0.pt_nr, EDGE_net[i].pt1.pt_nr, EDGE_net[i].pt1.pt_nr);
   }

   fprintf(f8,"\nattribute \"element type\" string \"triangles\"");
   fprintf(f8,"\nattribute \"ref\" string \"positions\"");
   fprintf(f8,"\n");
   fprintf(f8,"\nobject \"netz\" class field");
   fprintf(f8,"\ncomponent \"positions\" value 1");
   fprintf(f8,"\ncomponent \"connections\" value 2");
   fprintf(f8,"\ncomponent \"data\" value 1");
   fprintf(f8,"\nend");
   fprintf(f8,"\n");
 
}

/****************************************************************************/
/* write_ART_tec_file_TRACE()                                               */
/*                                                                          */
/*  Schreiben des Files fuer den Netzgenerator                              */
/*  modified by Annette Hemminger, 24-08-99                                 */
/*                                                                          */
/****************************************************************************/
void write_ART_tec_file_TRACE(FILE *f3, FILE *f7, FILE *f8)
{
   int i, j;

   /*header*/
   fprintf(f3,"%%%% Version 3.0");
   fprintf(f3,"\n%%%% VertexNumber:   %d", nvertex_net);
   fprintf(f3,"\n%%%% EdgeNumber:     %d", nedge_net);
   fprintf(f3,"\n%%%% FaceNumber:     %d", nface_net);
   fprintf(f3,"\n%%%% ElementNumber:  %d", nelement_net);
   fprintf(f3,"\n%%%% DO NOT CHANGE LINES ABOVE !!!! ");
   fprintf(f3,"\n%%  NET: Vertices <-> Edges <-> Faces <-> Elements ");
   fprintf(f3,"\n%%  ");
   fprintf(f3,"\n%%  Eingabefile ART3D: erstellt von Kluftgenerator FRAC3D");
   fprintf(f3,"\n%%                     Annette Hemminger,CAB,TU Braunschweig");
   fprintf(f3,"\n%%  ");


   /*************************************************************************/
   /* all nodes: with their coordinate values x, y, z                       */
   /* - nodes of the prisma subdomain                                       */
   /* - nodes of the fracture elements (rectangle or polyline)              */
   /* - nodes of the intersection lines                                     */
   /* - nodes of the single intersection points                             */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Vertices: x y z ");
   for (i=0; i<nvertex_net; i++)
   {
      fprintf(f3,"\n%8.5f  %8.5f  %8.5f", 
              VERTEX_net[i].pt.x, VERTEX_net[i].pt.y, VERTEX_net[i].pt.z);
   }
   fprintf(f3,"\n$");


   /*************************************************************************/
   /* all edges: listed by the number of their coordinate node              */
   /* - edges of the prisma subdomain                                       */
   /* - edges of the fracture elements (rectangle or polyline)              */
   /* - edges of the intersection lines                                     */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Edges (Indices to List of Points): ");

   for (i=0; i<nedge_net; i++)
   {
      fprintf(f3,"\n%d: %d %d", 
                 EDGE_net[i].cw, EDGE_net[i].pt0.pt_nr, EDGE_net[i].pt1.pt_nr);
   }
   fprintf(f3,"\n$");

   /*************************************************************************/
   /* all faces: listed by the number of their edges                        */
   /* - faces of the prisma subdomain                                       */
   /* - faces of the fracture elements (rectangle or polyline)              */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Faces (Indices to List of Edges): ");

   for (i=0; i<nface_net; i++)
   {
      fprintf(f3,"\n%d:", FACE_net[i].cw);
      for (j=0; j<FACE_net[i].sum_edges; j++)
      {
         fprintf(f3," %d ", FACE_net[i].EDGE_NR[j] );
      }
   }
   fprintf(f3,"\n$");
  
   if (nelement_net > 0) 
   {
      /**********************************************************************/
      /* all elements: listed by the number of their outer faces (subdomain)*/
      /* --> faces of the prisma subdomain                                  */
      /*                                                                    */
      /**********************************************************************/
      fprintf(f3,"\n%% Elements (Indices to List of Faces): ");
      for (i=0; i<nelement_net; i++)
      {
         fprintf(f3,"\n%d:", ELEMENT_net[i].cw);
         for (j=0; j<ELEMENT_net[i].sum_faces; j++)
         {
            fprintf(f3," %d ", ELEMENT_net[i].FACE_NR[j] );
         }
      }
      fprintf(f3,"\n$");
   }
   fprintf(f3,"\n");



   /*************************************************************************/
   /* Tecplot                                                               */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f7,"TITLE=\"1D fracture in 2D subplane\" ");
   fprintf(f7,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
   fprintf(f7,"\nZONE N=%d, E=%d, F=FEPOINT, ET=TRIANGLE",
                nvertex_net,nedge_net);

   /* x-, y-, z- coordinates */
   for (i=0; i<nvertex_net; i++)
   {
      fprintf(f7,"\n%12.8f  %12.8f  %12.8f", 
              VERTEX_net[i].pt.x, VERTEX_net[i].pt.y, VERTEX_net[i].pt.z);
   }

   /* edges */
   for (i=0; i<nedge_net; i++)
   {
      fprintf(f7,"\n%d %d %d", 
      EDGE_net[i].pt0.pt_nr+1, EDGE_net[i].pt1.pt_nr+1, EDGE_net[i].pt1.pt_nr+1);
   }
   fprintf(f7,"\n");

   /*************************************************************************/
   /* Data Explorer  Hussam Sheta 26/05/02                                                               */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f8,"object 1 class array type float rank 1 shape 3 items %d data follows", nvertex_net);

   /* x-, y-, z- coordinates */
   for (i=0; i<nvertex_net; i++)
   {
      fprintf(f8,"\n%12.8f  %12.8f  %12.8f", 
              VERTEX_net[i].pt.x, VERTEX_net[i].pt.y, VERTEX_net[i].pt.z);
   }

   fprintf(f8,"\nobject 2 class array type int rank 1 shape 3 items %d data follows", nedge_net);

   /* edges of the polygon */
   for (i=0; i<nedge_net; i++)
   {
      fprintf(f8,"\n%d %d %d", 
      EDGE_net[i].pt0.pt_nr, EDGE_net[i].pt1.pt_nr, EDGE_net[i].pt1.pt_nr);
   }

   fprintf(f8,"\nattribute \"element type\" string \"triangles\"");
   fprintf(f8,"\nattribute \"ref\" string \"positions\"");
   fprintf(f8,"\n");
   fprintf(f8,"\nobject \"netz\" class field");
   fprintf(f8,"\ncomponent \"positions\" value 1");
   fprintf(f8,"\ncomponent \"connections\" value 2");
   fprintf(f8,"\ncomponent \"data\" value 1");
   fprintf(f8,"\nend");
   fprintf(f8,"\n");
 
}


/****************************************************************************/
/* write_ART_file_Subplane()                                                */
/*                                                                          */
/*  Schreiben des Files fuer den Netzgenerator                              */
/*  modified by Annette Hemminger, 24-08-99                                 */
/*                                                                          */
/****************************************************************************/
void write_ART_file_Subplane(FILE *f3)
{
   int i, j;

   /*header*/
   fprintf(f3,"%%%% Version 3.0");
   fprintf(f3,"\n%%%% VertexNumber:   %d", nvertex_net);
   fprintf(f3,"\n%%%% EdgeNumber:     %d", nedge_net);
   fprintf(f3,"\n%%%% FaceNumber:     %d", nface_net);
   fprintf(f3,"\n%%%% ElementNumber:  %d", nelement_net);
   fprintf(f3,"\n%%%% DO NOT CHANGE LINES ABOVE !!!! ");
   fprintf(f3,"\n%%  NET: Vertices <-> Edges <-> Faces <-> Elements ");
   fprintf(f3,"\n%%  ");
   fprintf(f3,"\n%%  Eingabefile ART3D: erstellt von Kluftgenerator FRAC3D");
   fprintf(f3,"\n%%                     Annette Hemminger,CAB,TU Braunschweig");
   fprintf(f3,"\n%%  ");


   /*************************************************************************/
   /* all nodes: with their coordinate values x, y, z                       */
   /* - nodes of the prisma subdomain                                       */
   /* - nodes of the fracture elements (rectangle or polyline)              */
   /* - nodes of the intersection lines                                     */
   /* - nodes of the single intersection points                             */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Vertices: x y z ");
   for (i=0; i<nvertex_net; i++)
   {
      fprintf(f3,"\n%8.5f  %8.5f  %8.5f", 
              VERTEX_net[i].pt.x, VERTEX_net[i].pt.y, VERTEX_net[i].pt.z);
   }
   fprintf(f3,"\n$");


   /*************************************************************************/
   /* all edges: listed by the number of their coordinate node              */
   /* - edges of the prisma subdomain                                       */
   /* - edges of the fracture elements (rectangle or polyline)              */
   /* - edges of the intersection lines                                     */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Edges (Indices to List of Points): ");

   for (i=0; i<nedge_net; i++)
   {
      fprintf(f3,"\n%d: %d %d", 
                 EDGE_net[i].cw, EDGE_net[i].pt0.pt_nr, EDGE_net[i].pt1.pt_nr);
   }
   fprintf(f3,"\n$");

   /*************************************************************************/
   /* all faces: listed by the number of their edges                        */
   /* - faces of the prisma subdomain                                       */
   /* - faces of the fracture elements (rectangle or polyline)              */
   /*                                                                       */
   /*************************************************************************/
   fprintf(f3,"\n%% Faces (Indices to List of Edges): ");

   for (i=0; i<nface_net; i++)
   {
      fprintf(f3,"\n%d:", FACE_net[i].cw);
      for (j=0; j<FACE_net[i].sum_edges; j++)
      {
         fprintf(f3," %d ", FACE_net[i].EDGE_NR[j] );
      }
   }
   fprintf(f3,"\n$");
  
   if (nelement_net > 0) 
   {
      /**********************************************************************/
      /* all elements: listed by the number of their outer faces (subdomain)*/
      /* --> faces of the prisma subdomain                                  */
      /*                                                                    */
      /**********************************************************************/
      fprintf(f3,"\n%% Elements (Indices to List of Faces): ");
      for (i=0; i<nelement_net; i++)
      {
         fprintf(f3,"\n%d:", ELEMENT_net[i].cw);
         for (j=0; j<ELEMENT_net[i].sum_faces; j++)
         {
            fprintf(f3," %d ", ELEMENT_net[i].FACE_NR[j] );
         }
      }
      fprintf(f3,"\n$");
   }
   fprintf(f3,"\n");
 
}



/*****************************************************************************/
/*  Schreiben des Ausgabefiles der Intersectionslinien (EDGE3D) fuer tecplot */
/*                                                                           */
/*****************************************************************************/
void write_EDGE3D_file(FILE *f5, struct edge *EDGE3D)
{
  int i,j;
  fprintf(f5,"TITLE=\"Intersection lines in the 3D domain\" ");
  fprintf(f5,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
  fprintf(f5,"\nZONE N=%d, E=%d, F=FEPOINT, ET=TRIANGLE",
	  edge_nr_3D*2,edge_nr_3D);
  for (i=0; i<edge_nr_3D; i++)
    {
      fprintf(f5,"\n%8.5f  %8.5f  %8.5f", EDGE3D[i].pt0.x, 
	      EDGE3D[i].pt0.y, EDGE3D[i].pt0.z); 
      fprintf(f5,"\n%8.5f  %8.5f  %8.5f", EDGE3D[i].pt1.x, 
	      EDGE3D[i].pt1.y, EDGE3D[i].pt1.z); 
    }
  
  for (i=1; i<=edge_nr_3D; i++)
    {
      j = i*2 - 1;
      fprintf(f5,"\n %d \t %d \t %d ", j, j+1, j+1 ); 
    }
  fprintf(f5,"\n");
}

/*****************************************************************************/
/*  Schreiben des Ausgabefile der singulaeren Schnittpunkte (VERTEX3D)       */
/*  fuer tecplot                                                             */
/*                                                                           */
/*****************************************************************************/
void write_VERTEX3D_file(FILE *f6, struct vertex *VERTEX3D)
{
  int i;
  fprintf(f6,"TITLE=\"single intersection points in the 3D domain\" ");
  fprintf(f6,"\nVARIABLES=\"X\" \"Y\" \"Z\"     ");
  fprintf(f6,"\nZONE N=%d,E=%d,F=FEPOINT,ET=TRIANGLE",nvertex_nr,nvertex_nr);
  
  for (i=0; i<nvertex_nr; i++)
    {
      fprintf(f6,"\n%8.5f  %8.5f  %8.5f", 
	      VERTEX3D[i].pt.x, VERTEX3D[i].pt.y, VERTEX3D[i].pt.z);
    }
  
  for (i=1; i<=nvertex_nr; i++)
    {
      fprintf(f6,"\n %d \t %d \t %d ",i,i,i); 
    }
  fprintf(f6,"\n");
}


