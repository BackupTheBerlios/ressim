/*****************************************************************************/
/*                                                                           */
/* File:      einlesen_pointfile.c                                           */
/*                                                                           */
/* Purpose:   reads static fracture plane from a pointfile.dat               */
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
#include "functions.h" 


struct fracture *insert_frac_into_list(char pt[3][3][512],
				       int *current_size)
{
  int i, j;
  char *ptr;
  double ptr2;
  if (current_size == 0)
    {
      if ((FRAC = malloc(2*sizeof(struct fracture))) == NULL)
	{
	  perror("insert_frac_into_list: malloc()");
	  exit(-1);
	}
      *current_size = 2;
  }
  else
    {
      if ((FRAC = realloc(FRAC,
			 (*current_size+2) * sizeof(struct fracture))) == NULL)
	{
	  perror("insert_var_into_list: realloc()");
	  exit(-1);
	}
      (*current_size)++;
    }
 
  for (i = 0; i < 3; i++)
    { 
      for (j = 0; j < 3; j++)
	{
	  if ((ptr = strdup(pt[i][j])) == NULL)
	    {
	      perror("insert_var_into_list: strdup()");
	      exit(-1);
	    }
	  ptr2 = strtod(pt[i][j], NULL);
	  switch (j)
	    {
	    case 0:
	      FRAC[*current_size].pt[i].x = ptr2;
	      printf("\n%dX Ist %.4f ", i, FRAC[*current_size].pt[i].x);
	      break;
	    case 1:
	      FRAC[*current_size].pt[i].y = ptr2;
	      printf("\n%dY Ist %.4f ", i, FRAC[*current_size].pt[i].y);
	      break;
	    case 2:
	      FRAC[*current_size].pt[i].z = ptr2;
	      printf("\n%dZ Ist %.4f ", i, FRAC[*current_size].pt[i].z);
	      break;
	    }
	}
    }
  FRAC[*current_size].frac_nr = *current_size+1;
  FRAC[*current_size+1].frac_nr = -1;

  return FRAC;
  
}

void read_pointfile (FILE *PF)
{ 
  int i, j;
  int pt_nr = 0, linenr = 1; 
  int listengroesse = 0; 
  char line[512]; 
  char *rptr; 
  char pt[3][3][512];

  while (fgets(line, 512, PF) != NULL)   
    {
      while(1)
	{
	  if(pt_nr < 3)
	    {   
	      /*fgets(line, 512, PF);*/
	      if ((rptr = strpbrk(line, "#\n")) != NULL)
		{
		  line[rptr - line] = 0;   
		}
	      if ((rptr = strtok(line, " \t")) != NULL)   
		{
		  strcpy(pt[pt_nr][0], rptr);
		  printf("\npt_nr ist %d Wert ist %s", pt_nr, pt[pt_nr][0]);
		  if ((rptr = strtok(NULL, " \t")) == NULL)    
		    {    
		      fprintf(stderr,   
			      "male formed line %d, 3 arguments required"    
			      " found only 1\n", linenr);    
		      continue;    
		    }    
		  strcpy(pt[pt_nr][1], rptr);    
		  printf("\npt_nr ist %d Wert ist %s", pt_nr, pt[pt_nr][1]);
		  if ((rptr = strtok(NULL, " \t")) == NULL)   
		    {    
		      fprintf(stderr,    
			      "male formed line %d, 3 arguments required"    
			      " found only 2\n", linenr);    
		      continue;    
		    }    
		  strcpy(pt[pt_nr][2], rptr);    
		  printf("\npt_nr ist %d Wert ist %s", pt_nr, pt[pt_nr][2]);
		  pt_nr++;
		}
	    }
	  else
	    {
	      pt_nr = 0;
	      break;
	    }
	  printf("\n####%d", linenr);
	  linenr++;    
	} 
      FRAC = insert_frac_into_list(pt, &listengroesse);   
      printf("\nListengroesse %d", listengroesse);
    }
}

main()
{
  FILE *filedat = NULL;
  char Fname[50]="Point_file.dat";
  while(1)
    {
      fprintf(stdout,"\nOpening Pointfile [ %s ]", Fname); 
      if ((filedat = fopen(Fname,"r")) == NULL)
	{
	  printf("\nERROR Inputfile does not exists!\nEXIT PROGRAM\n");
	  return(-1);
	}
      else
	{
	  printf("\nReading File: %s\n", Fname); 
	  read_pointfile(filedat);
	  break;
	}
    }
  fclose(filedat);
}
