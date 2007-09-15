/****************************************************************************/
/*                                                                          */
/* File:      mcmc.c                                                        */
/*                                                                          */
/* Purpose:   Markov-Chain-Monte-Carlo methode                              */
/*                                                                          */
/*                                                                          */
/* Author:    Annette Silberhorn-Hemminger  (AH)                            */
/*            Institut fuer Wasserbau                                       */
/*            Universitaet Stuttgart                                        */
/*            email: annette.hemminger@iws.de                               */
/*                                                                          */
/* Remarks:                                                                 */
/*                                                                          */
/* Functions: void EvaluateInitialPicture()                                 */
/*            void SimulatedAnnealing()                                     */
/*            extern double *ScanlineMethod()        (scanline.c)           */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "functions.h" 
#include "mcmc.h" 
/*#include "scanline.h"*/
/* #include "geometry.h" */

void SA_MCMC_method()  
{
   int i, j;
 
   /*************************************************************************/
   /* 1.) determine number of classes                                       */ 
   /* 2.) set lower and upper class boundaries                              */ 
   /* 3.) set 'dream distribution' (Klasseneinteilung)                      */ 
   /* 4.) determine real, actual distribution of the of the property        */ 
   /*************************************************************************/
   fprintf(stdout,"\n EvaluateInitialPicture() starts  ......................\n");
   ninit_property = EvaluateInitialPicture(init_property);
   fprintf(stdout,"\n\n ............... EvaluateInitialPicture() done \n");


   /*************************************************************************/
   /* Simulated Annealing: pre-conditioning                                 */ 
   /*************************************************************************/
   printf("\n SimulatedAnnealing() starts  .......................\n");
   ninit_property = SimulatedAnnealing(ninit_property, real_class);
   printf("\n ............... SimulatedAnnealing() done \n");


   /*************************************************************************/
   /* Markov-Chain-Monte-Carlo algorithm                                    */ 
   /*************************************************************************/
   printf("\n MarkovChainMonteCarlo() starts  ....................\n");
   MarkovChainMonteCarlo(ninit_property, real_class);
   
   printf("\n");
   for (j=0; j<nclass_distance; j++) 
   {
      printf("\n    perc_frequ:  real_class[%2d]=%6.3f  dream_class[%2d]=%6.3f",
              j, real_class[j].perc_frequ, j,dream_class[j].perc_frequ);
   }

   printf("\n ............... MarkovChainMonteCarlo() done \n");

}


/****************************************************************************/
/*                                                                          */
/*                         F U N C T I O N S                                */
/*                                                                          */
/****************************************************************************/
/****************************************************************************/
/* EvaluateInitialPicture(): analyse the initial picture                    */
/*    1.) determine number of classes                                       */ 
/*    2.) set lower and upper class boundaries                              */ 
/*    3.) set 'dream distribution' (Klasseneinteilung)                      */ 
/*    4.) determine actual distribution of the property, declare the values */
/*        to their classes (count the number of members per class)          */
/****************************************************************************/
int EvaluateInitialPicture(double *init_property)
{
   int i, j;

   /*************************************************************************/
   /* read parameter from input file                                        */
   /*************************************************************************/
   nclass_distance   = get_var_integer(uvar, "nclass_distance");
   min_frac_distance = get_var_double(uvar, "min_frac_distance");
   max_frac_distance = get_var_double(uvar, "max_frac_distance");
   cdf_distance_type = get_var_integer(uvar, "cdf_distance_type");
   lambda_dist       = get_var_double(uvar, "lambda_dist");
   ninvest_plane     = get_var_integer(uvar, "ninvest_plane");



   /*************************************************************************/
   /* Allocate memory                                                       */
   /*************************************************************************/
   real_class  = AllocateMemory_StructClass(nclass_distance);
   dream_class = AllocateMemory_StructClass(nclass_distance);
   tmp_class   = AllocateMemory_StructClass(nclass_distance);



   /*************************************************************************/
   /* 1.) Declare the lower and upper boundary value of                     */
   /*     - real_class[]  : existing real distribution                      */
   /*     - dream_class[] : dream distribution (e.g. the given distribution)*/
   /*     - tmp_class[]   : help array                                      */
   /*                                                                       */
   /* 2.) Declare the percentage frequency:  dream_class.perc_frequ         */
   /*                                                                       */
   /*************************************************************************/
   SetClassValues_InitialStep(nclass_distance,
                              real_class, dream_class, tmp_class);
                              

   /*************************************************************************/
   /* ScanlineMethod(&nproperty)                                            */
   /*    Apply the Scanline methode                                         */
   /*    property : here the separation distances between two intersection  */
   /*               points                                                  */
   /*                                                                       */
   /*************************************************************************/
   init_property = InitialScanlineMethod(&nproperty, ninvest_plane);


   /*************************************************************************/
   /* o. determine actual distribution of the property                      */
   /* o. declare the values to their classes in terms of counting the       */
   /*    absolute frequency per class                                       */
   /* o. calculate the percentage frequency per class in [%]                */
   /*************************************************************************/
   SetClassValues_with_Properties(nclass_distance, real_class,
                                  nproperty, init_property); 

   return(nproperty);
}



/****************************************************************************/
/* SimulatedAnnealing()                                                     */
/*    Annealing for MCMC Method                                             */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
int SimulatedAnnealing(int nproperty_old, struct class *real_class)
{
   int i, ii, j, jj, k, l;
   double *old_property, *new_property; 
   struct scanline *tmp_SCANLINE;

   static char file_n30[20]="statistic_SA.dat";
   static char file_n40[20]="test_SA.dat";
   FILE *f30;
   FILE *f40;
   f30 = fopen(file_n30,"w");
   f40 = fopen(file_n40,"w");


   fprintf(f30,"TITLE=\"statistic Simulated Annealing\" "); 
   fprintf(f30,"\nVARIABLES=\"swap\"  \"object__old\" \"object__new\"  \"delta\"  \"Temp0\"  \"p_accept\" \"random\"  \"nswap\" ");
   fprintf(f30,"\nZONE I=???  F=POINT "); 


   /*************************************************************************/
   /* Allocate memory                                                       */
   /*************************************************************************/
   if ((old_property=(double *)malloc(nproperty_old*sizeof(double)))==NULL) {
      fprintf(stderr,"Memory allocation failed in 'old_property' \n");
      exit (-1);
   }

   /*************************************************************************/
   /* read in data and set default value                                    */
   /*************************************************************************/
   Temp0          = get_var_double(uvar, "Temp0");
   cool_down      = get_var_double(uvar, "cool_down");
   fix_temp_step  = get_var_integer(uvar, "fix_temp_step");
   temp_step      = get_var_integer(uvar, "temp_step");
   object_fct_old = object_fct_new = 0;
  
   l=1;


   /*************************************************************************/
   /* Temperatur loop                                                       */
   /*************************************************************************/
   for (i=0; i<temp_step; i++)  
   {
      nswap = 0;
     
      if (i==0) {
         object_fct_old = ObjectiveFunction(nclass_distance, 
                                            real_class, dream_class); 
      }

      /*AH 1108 raus:
        j=0;
        while (j<fix_temp_step)  */
      for (j=0; j<fix_temp_step; j++)
      {
         /*******************************************************************/
         /* 1.) draw randomly number 'draw_nr' of fracture element          */
         /*     and change the location of the fracture plane (coordinate   */
         /*     values), but not the orientation and size of the plane      */
         /*******************************************************************/
         draw_nr = (int) (ran3(&rseed) * nfrac); 
         DrawFracture_and_ChangeCoordinate(&draw_nr, &drawn_FRAC);


         /*******************************************************************/
         /* 1.a)"copy" the existing SCANLINE[] array to the temporary       */
         /*     array tmp_SCANLINE[]                                        */
         /*     if swap is taken    : 'SCANLINE[]' is take as new array     */
         /*        swap is not taken: copy back SCANLINE[] = tmp_SCANLINE[] */
         /*******************************************************************/
         tmp_SCANLINE = SCANLINE;


         /*******************************************************************/
         /* 2.) calculate the new property:                                 */
         /*     2.1) check if drawn 'old' fracture element intersects the   */
         /*          fixed scanline grid                                    */
         /*     2.2) check if modified 'new' fracture element intersects    */
         /*          the fixed scanline grid                                */
         /*     2.3) calculate the existing, real distribution of the       */
         /*          property  --> 'real_class'                             */
         /*                                                                 */
         /* --> apply function 'OneFracture_FixedSamples_ScanlineMethod'    */
         /*                                                 (scanline.c)    */
         /*                                                                 */
         /*******************************************************************/
         nproperty=0;
         new_property = OneFracture_FixedSampleGrid_ScanlineMethod(&nproperty, 
                                                                   draw_nr);

         /*******************************************************************/
         /* SetClassValues_with_Properties()                                */
         /*    o. determine actual distribution 'tmp_class' of the property */
         /*    o. declare the values to their classes in terms of counting  */
         /*       the absolute frequency per class                          */
         /*    o. calculate the percentage frequency per class in [%]       */
         /*******************************************************************/
         SetClassValues_with_Properties(nclass_distance, tmp_class,
                                        nproperty, new_property); 

 
         /*******************************************************************/
         /* ObjectiveFunction()                                             */
         /*    o. determine new object function  'object_fct_new'           */
         /*******************************************************************/
         object_fct_new = ObjectiveFunction(nclass_distance, 
                                            tmp_class, dream_class);

         /*******************************************************************/
         /* Try, AH 08.08.                                                  */
         /* Soll swap weiter untersucht werden:                             */
         /* o. Untersuchung wird nur dann durchgefuehrt, wenn               */
         /*    'object_fct_new != object_fct_old'                           */
         /*    Dadurch soll vermieden werden, dass bei der 'fix_temp_step'  */
         /*    -- loop staendig swaps ohne Aenderungen in der               */
         /*    'object_fct_new' untersucht werden.                          */
         /* o. wird swap nicht weiter untersucht, so muss das 'gezogene'    */
         /*    Kluftelement wieder auf seine alten Werte gesetzt werden.    */
         /*******************************************************************/
/**AH 1108 raus
         if (object_fct_new != object_fct_old) 
         {
***/
            /****************************************************************/
            /* Decide taking swap or not taking swap                        */
            /*    o. yes if   :  new_object_fct >= old_object_fct           */
            /*    o. eventuel :  with a certain probability 'probab'        */
            /*                                                              */
            /*    if swap is taken, the 'tmp_class' distribution is the new */
            /*    'real_class' distribution                                 */
            /*                                                              */
            /****************************************************************/
            if (1 == TakeSwap_SimulaAnneal(Temp0,&object_fct_old,&object_fct_new,
                                           draw_nr, drawn_FRAC, &l, f30)) 
            {
               nswap++; 

               for (jj=0; jj<nclass_distance; jj++) {
                  real_class[jj].lower      = tmp_class[jj].lower; 
                  real_class[jj].upper      = tmp_class[jj].upper; 
                  real_class[jj].abs_frequ  = tmp_class[jj].abs_frequ; 
                  real_class[jj].perc_frequ = tmp_class[jj].perc_frequ; 
               }

               /**AH 1108 raus: j++; */
            }
            else { 
               SCANLINE = tmp_SCANLINE; 
               FRAC[draw_nr] = drawn_FRAC; 
            }
/**AH 1108 raus:  
         }
         else { 
            SCANLINE = tmp_SCANLINE; 
            FRAC[draw_nr] = drawn_FRAC; 
         }
*******/

         free(new_property);
      }
      Temp0 = Temp0 * cool_down;
 
      fprintf(stdout,"\n temp_step=%5d: Temp0=%12.3e  object_fct_old=%10.4f ", 
                      i, Temp0, object_fct_old);
   }

   free(old_property);
   fclose(f30);
   fclose(f40);
 
   return (nproperty); 
}


/****************************************************************************/
/* MarkovChainMonteCarlo()                                                  */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void MarkovChainMonteCarlo(int nproperty_old, struct class *real_class)
{
   int i, j, l, help_swap;
   double *old_property, *new_property; 
   struct scanline *tmp_SCANLINE;

   static char file_n60[20]="statistic_MCMC.dat";
   FILE *f60;
   f60 = fopen(file_n60,"w");


   fprintf(f60,"TITLE=\"statistic Simulated Annealing\" "); 
   fprintf(f60,"\nVARIABLES=\"swap\"  \"object__old\" \"object__new\" \"index_swap\" ");
   fprintf(f60,"\nZONE I=???  F=POINT "); 


   /* Kontrolle */
   printf("\n in MarkovChainMonteCarlo() ");
   for (j=0; j<nclass_distance; j++) {
      printf("\n    perc_frequ:  real_class[%2d]=%6.3f  dream_class[%2d]=%6.3f",
              j, real_class[j].perc_frequ, j,dream_class[j].perc_frequ);
   }


   /*************************************************************************/
   /*************************************************************************/
   /* Allocate memory                                                       */
   /*************************************************************************/
   if ((old_property=(double *)malloc(nproperty_old*sizeof(double)))==NULL) {
      fprintf(stderr,"Memory allocation failed in 'old_property' \n");
      exit (-1);
   }

   /*************************************************************************/
   /* read in data and set default value                                    */
   /*************************************************************************/
   n_iteration_mcmc = get_var_integer(uvar, "n_iteration_mcmc");
   object_fct_old   = object_fct_new = 0; 
   l = 1;


   for (i=0; i<n_iteration_mcmc; i++)  
   {
      if (i==0) {
         object_fct_old = ObjectiveFunction(nclass_distance, 
                                            real_class, dream_class); 
      }
      /**********************************************************************/
      /* 1.) draw randomly number 'draw_nr' of fracture element             */
      /*     and change the location of the fracture plane (coordinate      */
      /*     values), but not the orientation and size of the plane         */
      /**********************************************************************/
      draw_nr = (int) (ran3(&rseed) * nfrac); 
      DrawFracture_and_ChangeCoordinate(&draw_nr, &drawn_FRAC);

      /**********************************************************************/
      /* 1.a)"copy" the existing SCANLINE[] array to the temporary          */
      /*     array tmp_SCANLINE[]                                           */
      /*     if swap is taken    : 'SCANLINE[]' is take as new array        */
      /*        swap is not taken: copy back SCANLINE[] = tmp_SCANLINE[]    */
      /**********************************************************************/
      tmp_SCANLINE = SCANLINE;


      /**********************************************************************/
      /* 2.) calculate the new property:                                    */
      /*     2.1) check if drawn 'old' fracture element intersects the      */
      /*          fixed scanline grid                                       */
      /*     2.2) check if modified 'new' fracture element intersects       */
      /*          the fixed scanline grid                                   */
      /*     2.3) calculate the existing, real distribution of the          */
      /*          property  --> 'real_class'                                */
      /*                                                                    */
      /* --> apply function 'OneFracture_FixedSamples_ScanlineMethod'       */
      /*                                                 (scanline.c)       */
      /*                                                                    */
      /**********************************************************************/
      nproperty = 0; 
      new_property = OneFracture_FixedSampleGrid_ScanlineMethod(&nproperty, 
                                                                draw_nr);

      /**********************************************************************/
      /* SetClassValues_with_Properties()                                   */
      /*    o. determine actual distribution of the property                */
      /*    o. declare the values to their classes in terms of counting     */
      /*       the absolute frequency per class                             */
      /*    o. calculate the percentage frequency per class in [%]          */
      /**********************************************************************/
      SetClassValues_with_Properties(nclass_distance, tmp_class,
                                     nproperty, new_property); 

      /**********************************************************************/
      /* ObjectiveFunction()                                                */
      /*    o. determine new object function  'new_object_fct'              */
      /**********************************************************************/
      object_fct_new = ObjectiveFunction(nclass_distance, 
                                         tmp_class, dream_class);

      /**********************************************************************/
      /* Decide taking swap or not taking swap                              */
      /*    o. yes if   :  new_object_fct >= old_object_fct                 */
      /*    o. eventuel :  with a certain probability 'probab'              */
      /*                                                                    */
      /*    if swap is taken, the 'tmp_class' distribution is the new       */
      /*    'real_class' distribution                                       */
      /*                                                                    */
      /**********************************************************************/
      if (1 == TakeSwap_MCMC(&object_fct_old,&object_fct_new, draw_nr,
                             drawn_FRAC, &l, f60)) 
      {
         nswap++; 
         printf("\n MCMC:  nswap=%d ", nswap);
      }
      else { 
         SCANLINE = tmp_SCANLINE; 
         FRAC[draw_nr] = drawn_FRAC; 
      }
   }

   fclose(f60);
}


/****************************************************************************/
/* ObjectiveFunction()                                                      */
/*    o. determine object function 'object_fct'                             */
/*                                                                          */
/****************************************************************************/
double ObjectiveFunction(int nclass, 
                         struct class *Rclass, struct class *Dclass)
{
   int i, j;
   double object_fct=0;

   /*************************************************************************/
   /* determine object functions:  chi-square distance                      */
   /*************************************************************************/
   for (i=0; i<nclass; i++) {
      object_fct += pow((  Rclass[i].perc_frequ * (double)nclass 
                              - Dclass[i].perc_frequ * (double)nclass),2) 
                          / (Dclass[i].perc_frequ * (double)nclass);
   }
   return (object_fct);
}


/****************************************************************************/
/* TakeSwap_SimulaAnneal()                                                  */
/*                                                                          */
/*    decide taking swap or not taking swap                                 */
/*       o. yes if   :  new_object_fct >= old_object_fct                    */
/*       o. eventuel :  with a certain probability 'probab'                 */
/*                                                                          */
/*    if swap is taken, the 'tmp_class' distribution is the new             */
/*    'real_class' distribution                                             */
/*                                                                          */
/****************************************************************************/
int TakeSwap_SimulaAnneal(double Temp0,
                          double *object_fct_old, double *object_fct_new,
                          int draw_nr, struct fracture drawn_FRAC, 
                          int *l, FILE *f30)
{
   int index_swap=0;       /* =0:do not swap, =1:do swap */
   double p_accept, help2;
   double delta_object_fct;

   delta_object_fct = *object_fct_old - *object_fct_new;

   if (*object_fct_old > *object_fct_new) { index_swap = 1; }

   else  {
      p_accept = exp(delta_object_fct / Temp0);
      help2    = ran3(&rseed);
   
      if (p_accept >= help2) index_swap = 1;
      else                   index_swap = 0;
   }

 
   if (index_swap == 1) { 

      if (*object_fct_old > *object_fct_new) {
         fprintf(f30,"\n %5d %10.4f  %10.4f  %10.3f  %12.2e  %10.3f  %10.3f  1", 
                      *l, *object_fct_old, *object_fct_new, delta_object_fct,
                      Temp0, 1.0     , 0.0);
      }
      else {
         fprintf(f30,"\n %5d %10.4f  %10.4f  %10.3f  %12.2e  %10.3f  %10.3f  1", 
                       *l, *object_fct_old, *object_fct_new, delta_object_fct,
                       Temp0, p_accept, help2);
      }

      *object_fct_old = *object_fct_new;
      *object_fct_new = 0;
   }
   else { 
      fprintf(f30,"\n %5d %10.4f  %10.4f  %10.3f  %12.2e  %10.3f  %10.3f  0", 
           *l, *object_fct_old, *object_fct_new, delta_object_fct, 
           Temp0, p_accept, help2);
   }
   *l = *l + 1;

   return(index_swap);
}



/****************************************************************************/
/* TakeSwap_MCMC()                                                          */
/*                                                                          */
/*    decide taking swap or not taking swap                                 */
/*                                                                          */
/*    if swap is taken, the 'tmp_class' distribution is the new             */
/*    'real_class' distribution                                             */
/*                                                                          */
/****************************************************************************/
int TakeSwap_MCMC(double *object_fct_old, double *object_fct_new,
                  int draw_nr, struct fracture drawn_FRAC,
                  int *l, FILE *f60)
{
   int index_swap=0;       /* =0:do not swap, =1:do swap */
   double help;
   double probability1, probability2;

   
   probability1 = exp(- *object_fct_old);
   probability2 = exp(- *object_fct_new);
   help         = ran3(&rseed);


   if (help < (probability2/(probability1 + probability2))) {
      index_swap = 1;
   }
   else  {
      index_swap = 0;
   }

   fprintf(f60,"\n %5d %10.4f %10.4f %4d",
                *l, *object_fct_old, *object_fct_new, index_swap);

   if (index_swap == 1) { 
      real_class = tmp_class;

      *object_fct_old = *object_fct_new;
      *object_fct_new = 0;
   }
   *l = *l + 1;

   return (index_swap);
}



/****************************************************************************/
/* DrawFracture_and_ChangeCoordinate()                                      */
/*                                                                          */
/****************************************************************************/
void DrawFracture_and_ChangeCoordinate(int *draw_nr, 
                                       struct fracture *drawn_FRAC)
{
   struct point move;
         
   *drawn_FRAC = FRAC[*draw_nr];
   FRAC[*draw_nr].pt[0].x = ran3(&rseed) * (dom_max.x - dom_min.x) ;
   FRAC[*draw_nr].pt[0].y = ran3(&rseed) * (dom_max.y - dom_min.y) ;
   FRAC[*draw_nr].pt[0].z = ran3(&rseed) * (dom_max.z - dom_min.z) ;

   /*******************************************************************/
   /* Calculate the new coordinates of point pt[1], pt[2], pt[3]      */
   /* --> 'move' the old fracture points by the 'moving vector' move  */
   /*      move = FRAC[draw_nr].pt[0] - drawn_FRAC                    */
   /*******************************************************************/
   move.x = FRAC[*draw_nr].pt[0].x - drawn_FRAC->pt[0].x;
   move.y = FRAC[*draw_nr].pt[0].y - drawn_FRAC->pt[0].y;
   move.z = FRAC[*draw_nr].pt[0].z - drawn_FRAC->pt[0].z;
  
   FRAC[*draw_nr].pt[1].x += move.x;
   FRAC[*draw_nr].pt[1].y += move.y;
   FRAC[*draw_nr].pt[1].z += move.z;

   FRAC[*draw_nr].pt[2].x += move.x;
   FRAC[*draw_nr].pt[2].y += move.y;
   FRAC[*draw_nr].pt[2].z += move.z;

   FRAC[*draw_nr].pt[3].x += move.x;
   FRAC[*draw_nr].pt[3].y += move.y;
   FRAC[*draw_nr].pt[3].z += move.z;

}


/****************************************************************************/
/* SetClassValues_InitialStep()                                             */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
void SetClassValues_InitialStep(int nclass_distance,
                                struct class *real_class, 
                                struct class *dream_class,
                                struct class *tmp_class)
{
   int i, j;

   double delta_class;                     /* (max - min) / numb_of_classes */
   double F_lower, F_upper;                   /* function values of the cdf */

   /*************************************************************************/
   /* 1.) Declare the lower and upper border of                             */
   /*     - real_class[]  : existing real distribution                      */
   /*     - dream_class[] : dream distribution (e.g. the given distribution)*/
   /*     - tmp_class[]   : required for boundaries                         */
   /*                                                                       */
   /* 2.) Declare the percentage frequency:  dream_class.perc_frequ         */
   /*                                                                       */
   /*     2.1.) given: cdf (e.g. F(x) negative exponential distribution)    */
   /*           calculate delta percentage which is equal to the            */
   /*           percentage frequency 'dream_class[i].perc_frequ' [%]        */
   /*              F_lower = F(x = dream_class[i].lower)                    */
   /*              F_upper = F(x = dream_class[i].upper)                    */
   /*              dream_class[i].perc_frequ = (F_upper - F_lower) * 100    */
   /*                                                                       */
   /*     2.2.) empirical distribution : better as 1.), more accurate       */
   /*           TODO --> Kai anfragen, ob er die Werte noch hat             */
   /*                                                                       */
   /*************************************************************************/

   delta_class = (max_frac_distance - min_frac_distance) / (double) nclass_distance;

   for (i=0; i<nclass_distance; i++)
   {
      /* calculate boundary values and set default values for given classes */
      if (i==0) {
         real_class[i].lower = tmp_class[i].lower = dream_class[i].lower
               = min_frac_distance;
      }
      else {
         real_class[i].lower = tmp_class[i].lower = dream_class[i].lower 
               = real_class[i-1].upper;
      }

      real_class[i].upper = tmp_class[i].upper = dream_class[i].upper
               = real_class[i].lower + delta_class;

      real_class[i].abs_frequ  = tmp_class[i].abs_frequ  = 0; 
      real_class[i].perc_frequ = tmp_class[i].perc_frequ = 0; 


      if(cdf_distance_type == 1) {
         F_lower = 1 - exp(lambda_dist * dream_class[i].lower); 
         F_upper = 1 - exp(lambda_dist * dream_class[i].upper); 
         dream_class[i].perc_frequ = (F_upper - F_lower) * 100.0;
      }
      else {
         perror("ERROR:  cdf_distance_type != 1  (mcmc.c)");
         exit(-1);
      }
   }
}



/****************************************************************************/
/* SetClassValues_with_Properties()                                         */
/*                                                                          */
/* o. determine actual distribution of the property                         */
/* o. declare the values to their classes in terms of counting the          */
/*    absolute frequency per class                                          */
/* o. calculate the percentage frequency per class in [%]                   */
/*                                                                          */
/****************************************************************************/
void SetClassValues_with_Properties(int nclass_distance, 
                                    struct class *CLASS, 
                                    int nproperty, double *init_property)
{
   int i, j;
  
   /*set default value*/
   for (i=0; i<nclass_distance; i++) {
      CLASS[i].abs_frequ  = 0; 
      CLASS[i].perc_frequ = 0; 
   }


   /* declare the values to their classes in terms of counting the */
   /* absolute frequency per class                                 */
   for (i=0; i<nproperty; i++) {
      for (j=0; j<nclass_distance; j++) {
         if(  (init_property[i] >= CLASS[j].lower) 
            &&(init_property[i] <  CLASS[j].upper)) 
         {
            CLASS[j].abs_frequ++;
            break;
         }
      }
   }

   for (i=0; i<nclass_distance; i++) {
      if (nproperty == 0) {              /* security check */
         CLASS[i].abs_frequ  = 0; 
         CLASS[i].perc_frequ = 0; 
      }
      else {
         if (CLASS[i].abs_frequ == 0) {CLASS[i].perc_frequ = 0; }
         else {
            CLASS[i].perc_frequ = 
                  (double) CLASS[i].abs_frequ / (double) nproperty * 100;
         }
      }
   }
}


/****************************************************************************/
/* AllocateMemory_StructClass(nclass_distance)                              */
/*                                                                          */
/*                                                                          */
/****************************************************************************/
struct class *AllocateMemory_StructClass(int nclass_distance)
{
   struct class *tmp_class;

   if ((tmp_class=(struct class*)malloc(nclass_distance*sizeof(struct class)))
       ==NULL) {
      fprintf(stderr,"Memory allocation failed in 'tmp_class' \n");
      exit (-1);
   }
   return (tmp_class);
}

