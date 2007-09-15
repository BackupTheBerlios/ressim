/*****************************************************************************/
/*                                                                           */
/* File:      prog_functions.c                                               */
/*                                                                           */
/* Purpose:   contains functions for program sequence                        */
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
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include "functions.h"  


void open_pointfile_2Delements_in_3D() 
{
   FILE *filedat = NULL;
   char *Fname = get_var_char(uvar, "name_Point_file");
   while(1)
   {
      fprintf(stdout,"\nOpening Pointfile [ %s ]", Fname); 
      if ((filedat = fopen(Fname,"r")) == NULL)
      {
         printf("\nERROR Inputfile does not exists!\nEXIT PROGRAM\n");
         exit(-1);
      }
      else
      {
         printf("\nReading File: %s\n", Fname); 
         gen_static_fracture_list(filedat);
         break;
      }
   }
   fclose(filedat);
}

void open_pointfile_1Delements_in_3D() 
{
   FILE *filedat = NULL;
   char *Fname = get_var_char(uvar, "name_Point_file");
   /*printf("\nSo far so good");*/
   while(1)
   {
      fprintf(stdout,"\nOpening Pointfile [ %s ]", Fname); 
      if ((filedat = fopen(Fname,"r")) == NULL)
      {
         printf("\nERROR Inputfile does not exists!\nEXIT PROGRAM\n");
         exit(-1);
      }
      else
      {
         printf("\nReading File: %s\n", Fname); 
         gen_static_trace_list(filedat);
         break;
      }
   }
   fclose(filedat);
}


/*****************************************************************************/
/* choice of generation method                                               */
/*                                                                           */
/*****************************************************************************/
void gen_fracture_list()
{
  /*AH, 06.02.2001, siehe File 'AH_protokoll.txt'
     int index_frac_gen_type;
  */

  extern void gen_random_fracture_list();
  
  
  /***************************************************************************/
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
  /*AH, 06.02.2001, siehe File 'AH_protokoll.txt'
     index_frac_gen_type = read_frac_gen_type();
     switch(index_frac_gen_type)
  */

  
  switch(frac_gen_type)
    {
    case 1:
      nfrac_det = 0;
      gen_random_fracture_list();                              /* geometry.c */
      fprintf(stdout,"\nGenerated fracture list with random values!");
      break;
    case 2:
      open_pointfile_2Delements_in_3D();
      nfrac = nfrac_det;
      fprintf(stdout,"\nGenerated fracture list with static values!");
      break;
    case 3:
      open_pointfile_2Delements_in_3D();            /*deterministic approach*/
      gen_random_fracture_list();                   /*   stochastic approach*/
      fprintf(stdout,"\nGenerated fracture list with deterministic and static values!");
      break;
    case 4:
      open_pointfile_1Delements_in_3D();
      fprintf(stdout,"\nGenerated trace list with static values!");
      break;
    }
}


void open_inputfile()
{
   FILE *filedat = NULL;
   char Fname[50]="Eingabefile.dat";
   while(1)
   {
      fprintf(stdout,"\n\nFracture generating program --> FRAC3D <-- \n");
      fprintf(stdout,"\nOpening Inputfile"); 
      if ((filedat = fopen(Fname,"r")) == NULL)
      {
         printf("\nERROR Inputfile does not exists!\nEXIT PROGRAM ");
         exit(-1);
      }
      else
      {
	     printf("\nReading %s ...........", Fname); 
	     break;
      }
   }
   uvar = (struct uservar *) NULL;
   read_inputfile(filedat, &uvar);                  /*einlesen_file.c*/
   fclose(filedat);
   printf("successfull\n");
}
