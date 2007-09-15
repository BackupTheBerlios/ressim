/*****************************************************************************/
/*                                                                           */
/* File:      area_computation.c                                             */
/*                                                                           */
/* Purpose:   different methods of calculation an optimization in            */
/*            dependence to generation                                       */
/*                                                                           */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                           */
/*****************************************************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <math.h>
#include "functions.h"
#include "area_computation.h"


void area_computation()
{
   int i;


   /**************************************************************************/
   /* 2D fracture planes (known and stochastic ones) in the 3D space         */
   /*                                                                        */
   /*    1.) Optimization                                                    */
   /*    2.) Multilayer -- Technik                                           */
   /*    3.) Subplane3D                                                      */
   /*    4.) Intersektionen (Schnittlinien bzw. Schnittpunkte bestimmen      */
   /*    5.) Subvolume (3D Volumengebiet)                                    */
   /*    6.) Backbone                                                        */
   /*    7.) Output (Netzgener., Stroemungs-- und Transport Programm)        */
   /*                                                                        */
   /**************************************************************************/
   if ((frac_gen_type == 1) || (frac_gen_type == 2) || (frac_gen_type == 3))
   {
      /***********************************************************************/
      /* 1.) Optimization                                                    */
      /*                                                                     */
      /*     Assumption: just for stochastic generated fracture fields.      */
      /*     o.) frac_gen_type = 1: stochastic generating process            */
      /*     o.) frac_gen_type = 3: deterministic                            */
      /*                            + stochastic generating process          */
      /*                                                                     */
      /***********************************************************************/
      if ((frac_gen_type == 1) || (frac_gen_type == 3))
      {

         /********************************************************************/
         /* read in data and set default value                               */
         /********************************************************************/
         do_SA_MCMC = get_var_integer(uvar, "do_SA_MCMC");
         if ((do_SA_MCMC < 0) || (do_SA_MCMC > 1)) {            /*Kontrolle*/
            fprintf(stderr,"ERROR: check value of 'do_SA_MCMC' in input file \n");
            exit (-1);
         }


         /********************************************************************/
         /* Optimization Part 1:  fracture length                            */
         /*                                                                  */
         /********************************************************************/
         /***/
      	  printf("\n\nOptimization_part1()  starts  ");
      	  if ((frac_gen_type == 1) || (frac_gen_type == 3)) {
      	  optimization_part1(); 
      	  }
      	  printf(".................. done\n"); 
         /***/     
      
         /********************************************************************/
         /* Optimierung  Teil 2 : Trennflaechenabstaende                     */
         /* Assumption: just for stochastic generated fracture fields.       */
         /*                                                                  */
         /********************************************************************/
	     if ((do_SA_MCMC == 1) && (frac_gen_type == 1)) 
         {
      	     fprintf(stdout,"\n\nSA_MCMC_method() startet  ");
   	        /* SA_MCMC_method(&FRAC); */
   	        SA_MCMC_method();
	        fprintf(stdout,".................. done\n"); 
      	  }
      }
      
      
      /***********************************************************************/
      /* 2.) Multilayer -- Technik                                           */
      /***********************************************************************/
      
      
      /***********************************************************************/
      /* 3.) Subplane3D                                                      */
      /***********************************************************************/
      sum_subplane3D = get_var_integer(uvar, "sum_subplane3D");
      if (sum_subplane3D > 0)
      {
         printf("\n\nExtracting subplane (2D in 3D) "); 
         subplane_3D_intersection_FRAC(&FRAC);
         printf(".................. done\n"); 
      }
      
      
      /***********************************************************************/
      /* 4.) Intersektionen (Schnittlinien bzw. Schnittpunkte bestimmen      */
      /***********************************************************************/
      fprintf(stdout,"\nIntersection lines: fracture plane <--> fracture plane  .........."); 
      intersection_FRAC(&FRAC);                              /*intersection.c*/
      
      /***********************************************************************/
      /* 5.) Subvolume (3D Volumengebiet)                                    */
      /***********************************************************************/
      printf("\n\nExtracting subvolume (3D)  "); 
      sum_subvol_edges = subvolume_3D_intersection_FRAC();  
      printf(".................. done\n"); 
      
      /***********************************************************************/
      /* 6.) Backbone                                                        */
      /***********************************************************************/
      
      
      /***********************************************************************/
      /* 7.) Output (Netzgener., Stroemungs-- und Transport Programm)        */
      /***********************************************************************/
      printf("\n PreNetgenOutput_2dFRAC_in3d() starts  ");
      PreNetgenOutput_2dFRAC_in3d(sum_subvol_edges);
      printf(".................. done\n"); 
      
      printf("\n output_FRAC() starts           ");
      output_FRAC(&FRAC, &EDGE3D, &VERTEX3D); 
      printf(".................. done\n"); 


      /***********************************************************************/
      /* AH, 17.05.2001: new!!! and TODO                                     */
      /* statstics                                                           */
      /* 1.] some interesting values for the mesh generating process         */
      /*  a] check the minimum distance between two points VERTEX_net        */
      /*  b] check the minimum / maximum length of EDGE_net                  */
      /*  c] check the minimum surface of an fracture plane FACE_net         */
      /***********************************************************************/
      printf("\n statistics_FracMesh starts           ");
      statistics_FracMesh(&FRAC, &EDGE3D, &VERTEX3D);
      printf(".................. done\n"); 


   }
  
  
   /**************************************************************************/
   /* 1D trace lines (known ones) in the 3D space                            */
   /*                                                                        */
   /*    1.) Subplane3D                                                      */
   /*    2.) Intersektionen (Schnittlinien bzw. Schnittpunkte bestimmen      */
   /*    3.) Subvolume (3D Volumengebiet)                                    */
   /*    4.) Output (Netzgener., Stroemungs-- und Transport Programm)        */
   /*                                                                        */
   /**************************************************************************/
   if (frac_gen_type == 4) 
   {

      /***********************************************************************/
      /* Subplane3D                                                          */
      /* TODO: geht nur fuer eine subplane, bei mehreren steigt das Programm */
      /*       aus.                                                          */
      /***********************************************************************/
      sum_subplane3D = get_var_integer(uvar, "sum_subplane3D");
      if (sum_subplane3D > 0)
      {
         printf("\n\nExtracting subplane (2D in 3D) "); 
         subplane_3D_intersection_TRACE();
         printf(".................. done\n"); 
      }
      
      /***********************************************************************/
      /* calculate the intersection points                                   */
      /***********************************************************************/
      printf("\nINTERSECTION_TRACE (1D elements in 3D space) "); 
      intersection_TRACE(&TRACE);                            /*intersection.c*/
      printf(".................. done\n"); 
      
      
      /***********************************************************************/
      /* Subvolume (3D Volumengebiet)                                        */
      /***********************************************************************/
/** TODO: stimmt noch was nicht: wird hier nun ein Volumengebiet oder eine
          Fläche ausgeschnitten?!?
**/
      printf("\nSUBVOLUME is cutted out  "); 
      sum_subvol_edges = subvolume_3D_intersection_TRACE();  
      printf(".................. done\n"); 
      
      /***********************************************************************/
      /* Output (Netzgener., Stroemungs-- und Transport Programm)            */
      /***********************************************************************/
      printf("\n PreNetgenOutput_1dTRACE_in3d() starts  ");
      PreNetgenOutput_1dTRACE_in3d(sum_subvol_edges);
      printf(".................. done\n"); 
      
      printf("\n output_TRACE() starts           ");
      output_TRACE(&TRACE, &VERTEX3D); 

      printf(".................. done\n"); 
    }
  
}
