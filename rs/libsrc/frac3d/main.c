/*****************************************************************************/
/*                                                                           */
/* File:      main.c                                                         */
/*                                                                           */
/* Purpose:   main program                                                   */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                           */
/* Remarks:                                                                  */
/*                                                                           */
/*                                                                           */
/*****************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "functions.h" 



int main()
{
  
   /**************************************************************************/
   /* read in the input File 'Eingabefile.dat'                               */
   /**************************************************************************/
   open_inputfile();                                       /*prog_functions.c*/


   /**************************************************************************/
   /* 'read_start_values_from_inputfile()'                                   */
   /*                                                                        */
   /* o.) 'frac_gen_type': variable controlls the type of generating process */
   /* o.) 'rseed'        : random start number                               */
   /* o.) 'frac_dens_3d' : fracture density [fracture area m^2 / m^3]        */
   /* o.) 'dom_min/dom_max': two domain points of the generating domain      */
   /*                                                                        */
   /**************************************************************************/
   read_start_values_from_inputfile();


   /**************************************************************************/
   /* generating the list of FRAC[]                                          */
   /**************************************************************************/
   gen_fracture_list();                                    /*prog_functions.c*/
   printf("\n Global Fracture list .................. done\n"); 


   /**************************************************************************/
   /* area_computation()                                                     */
   /* post-processing after generating steps                                 */
   /**************************************************************************/
   /*                                                                        */
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
   /*                                                                        */
   /* 1D trace lines (known ones) in the 3D space                            */
   /*                                                                        */
   /*    1.) Subplane3D                                                      */ 
   /*    2.) Intersektionen (Schnittlinien bzw. Schnittpunkte bestimmen      */ 
   /*    3.) Subvolume (3D Volumengebiet)                                    */ 
   /*    4.) Output (Netzgener., Stroemungs-- und Transport Programm)        */ 
   /*                                                                        */
   /**************************************************************************/
   area_computation();


   printf("\n program finally done :-) \n\n");

   return(0);
}
