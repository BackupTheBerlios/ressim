/*****************************************************************************/
/*                                                                           */
/* File:      read_startvalues.c                                             */
/*                                                                           */
/* Purpose:   initialisation                                                 */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                  */
/*                                                                           */
/*                                                                           */
/*****************************************************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/time.h>
#include <math.h>
#include "functions.h" 



/*******************************************************************/
/* Startwert Zufallsgenerator einlesen : rseed                     */
/*******************************************************************/
#define MAX 2147483647
#define MIN 0


/*****************************************************************************/
/* read_start_values_from_inputfile()                                        */
/*                                                                           */
/*                                                                           */
/*                                                                           */
/*****************************************************************************/
#define MAX 2147483647
void read_start_values_from_inputfile()
{

  
   /**************************************************************************/
   /* read in the variable 'frac_gen_type' which controlls the type of       */
   /* generating process                                                     */
   /**************************************************************************/
   frac_gen_type = read_frac_gen_type();                 /*read_startvalues.c*/
 

   if ((frac_gen_type == 1) || (frac_gen_type == 3)) 
   {
      /***********************************************************************/
      /* read in the start number to activate the random generator           */
      /* just do it for                                                      */
      /* o.) frac_gen_type = 1: stochastic generating process                */
      /* o.) frac_gen_type = 3: deterministic + stochastic generating process*/
      /***********************************************************************/
      read_rseed();                                      /*read_startvalues.c*/

      /***********************************************************************/
      /* read fracture density 'frac_dens_3d' [fracture area m^2 / m^3]      */ 
      /***********************************************************************/
      /*AH 16.03.00 
         frac_dens_3d = get_var_integer(uvar, "frac_dens_3d");
      */
      frac_dens_3d = get_var_double(uvar, "frac_dens_3d");

   }


   /**************************************************************************/
   /* read in the domain size                                                */
   /**************************************************************************/
   read_domain();                                        /*read_startvalues.c*/

}


/***************************************************************************/
/*                                                                         */
/*                          F U N C T I O N                                */
/*                                                                         */
/***************************************************************************/
/* void read_rseed()                                                       */
/*                                                                         */
/***************************************************************************/
void read_rseed()
{
  rseed = get_var_double(uvar, "rseed");
  idum = &rseed;
}


/***************************************************************************/
/* int read_frac_gen_type()                                                */
/*                                                                         */
/* read in the type of fracture generating                                 */
/* return it to the main program                                           */
/*                                                                         */
/*                                                                         */
/* what kind of generating process should be done ...........              */
/*                                                                         */
/* frac_gen_type                                                           */
/*       = 1: stochastic generating process                                */
/*            function: gen_random_fracture_list() in geometry.c           */
/*              * fracture edge points                                     */
/*              * orientation (normal vector)                              */
/*              * length of the fracture plane                             */
/*              * fracture apperture                                       */
/*                                                                         */
/*       = 2: deterministic process                                        */
/*            file 'Point_file.dat' contains the node coordinate values    */
/*            (known 2D elements in 3D space)                              */
/*            function: 'read_frac_gen_type()' in file 'prog_functions.c'  */
/*                      'gen_static_fracture_list()' in geometry_stat.c    */
/*                                                                         */
/*       = 3: deterministic + stochastic generating process                */
/*            file 'Point_file.dat' contains the node coordinate values    */
/*            function: 'read_frac_gen_type()'                             */
/*                      'gen_static_fracture_list()'                       */
/*                      'gen_random_fracture_list()'                       */
/*                                                                         */
/*       = 4: deterministic generating process                             */
/*            existing trace lines (1D elements in 3D space) are read in   */
/*            --> 1D elements in 3D space                                  */
/*                                                                         */
/*                                                                         */
/***************************************************************************/
int read_frac_gen_type()
{
   /*AH, 06.02.2001: die Variable ist nun in 'parameter.h' definiert
     int frac_gen_type;
   */

   frac_gen_type = get_var_integer(uvar, "frac_gen_type");

   switch (frac_gen_type) 
   {
      case 1: 
         fprintf(stdout,"\nType fracture generating: frac_gen_type=%d ", frac_gen_type);
         fprintf(stdout,"\n\tstochastic generating approach ");
         break;

      case 2:
         fprintf(stdout,"\nType fracture generating: frac_gen_type=%d ", frac_gen_type);
         fprintf(stdout,"\n\tdeterministic approach: known 2D elements in 3D space");
         break;

      case 3:
         fprintf(stdout,"\nType fracture generating: frac_gen_type=%d ", frac_gen_type);
         fprintf(stdout,"\n\tdeterministic + stochastic: known 2D elements + stochastic elements");
         break;

      case 4:
         fprintf(stdout,"\nType fracture generating: frac_gen_type=%d ", frac_gen_type);
         fprintf(stdout,"\n\tdeterministic approach: known 1D elements in 3D space");
         break;
   }

   return frac_gen_type; 
}


/*******************************************************************/
/* Gebietsgroesse (Punkte max/min) einlesen                        */
/* hier erst einmal die einfache Annahme: Domain = Wuerfel         */
/*                                                                 */
/* Anzahl der Kluefte nfrac                                        */
/*******************************************************************/
void read_domain()
{
   /****************************************************************/
   /* Minimaler Domain--Punkt                                      */ 
   /****************************************************************/
   dom_min.x = get_var_double(uvar, "min.x");
   dom_min.y = get_var_double(uvar, "min.y");
   dom_min.z = get_var_double(uvar, "min.z");

    
   /****************************************************************/
   /* Maximaler Domain--Punkt                                      */ 
   /****************************************************************/
   dom_max.x = get_var_double(uvar, "max.x");
   dom_max.y = get_var_double(uvar, "max.y");
   dom_max.z = get_var_double(uvar, "max.z");

 
   /****************************************************************/
   /* Kluftanzahl  nfrac  berechnen                                */ 
   /* AH 16.05., raus genommen, da Kluftdichte als Kluftflaeche pro*/ 
   /*            Volumeneinheit definiert ist [m^2 / m^3]          */ 
   /****************************************************************/
   domain_volume = (dom_max.x - dom_min.x) * (dom_max.y - dom_min.y)
                                           * (dom_max.z - dom_min.z);
   fprintf(stdout,"\ndomain_volume [m^3] = %.2f \n",domain_volume );  

/*******************
   AH 16.05.
   if((frac_gen_type == 1) || (frac_gen_type == 3))
   {
      nfrac =  frac_dens_3d * domain_volume ;
      printf("\nKluftanzahl nfrac = %d", nfrac);
   }
*******************/

}


