/*****************************************************************************/
/*                                                                           */
/* File:      read_inputfile.c                                               */
/*                                                                           */
/* Purpose:   implementation of program-parameters                           */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                           */
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



struct uservar *insert_var_into_list(struct uservar *uvar, char str[3][512],
		                     int *current_size, int zeile)
{
   int i;
   char *ptr;
   if (*current_size == 0)
   {
      if ((uvar = malloc(2*sizeof(struct uservar))) == NULL)
      {
         perror("insert_var_into_list: malloc()");
         exit(-1);
      }
      *current_size = 2;
   }
   else
   {
      if ((uvar = realloc(uvar,
          (*current_size+1) * sizeof(struct uservar))) == NULL)
      {
         perror("insert_var_into_list: realloc()");
         exit(-1);
      }
      (*current_size)++;
   }
  
   for (i = 0; i < 3; i++)
   {
      if ((ptr = strdup(str[i])) == NULL)
      {
         perror("insert_var_into_list: strdup()");
         exit(-1);
      }
      switch (i)
      {
       case 0:
         uvar[*current_size-2].typ = ptr;
         break;
       case 1:
         uvar[*current_size-2].name = ptr;
         break;
       case 2:
         uvar[*current_size-2].value = ptr;
         break;
      }
   }
   uvar[*current_size-2].zeile = zeile;
   uvar[*current_size-1].typ = NULL;
   uvar[*current_size-1].value = NULL;
   uvar[*current_size-1].name = NULL;
   uvar[*current_size-1].zeile = -1;

   return uvar;;
  
}

int get_var_integer(struct uservar *uvar, char *str)
{
  int i;
  for(i = 0; uvar[i].zeile > 0; i++)
    {
      if (strcmp(str, uvar[i].name) == 0)
	{
	  if (strcmp("integer", uvar[i].typ) == 0)
	    {
	      return((int)strtol(uvar[i].value, NULL, 10));
	    }
	  else
	    {
	      fprintf(stderr,
		      "Typenfehler in Eingabefile: "
		      "zeile %d\n", uvar[i].zeile);
	      exit(-1);
	    }
	}
    }
  fprintf(stderr, "Integer Variable %s nicht im Eingabefile definiert\n",str);
  fprintf(stderr, "Program STOP \n\n");
  exit(-1);
}


double get_var_double(struct uservar *uvar, char *str)
{
   int i;
   for(i = 0; uvar[i].zeile > 0; i++)
   {
      if (strcmp(str, uvar[i].name) == 0)
      {
         if (strcmp("double", uvar[i].typ) == 0)
         {
            return(strtod(uvar[i].value, NULL));
         }
         else
         {
            fprintf(stderr,"Typenfehler in Eingabefile: " "zeile %d\n", 
                    uvar[i].zeile);
             exit(-1);
         }
      }
   }
   fprintf(stderr, "Double Variable %s nicht im Eingabefile definiert\n", str);
   fprintf(stderr, "Program STOP \n\n");
   exit(-1);
}

char *get_var_char(struct uservar *uvar, char *str)
{
   int i;
   char *ptr;
   for(i = 0; uvar[i].zeile > 0; i++)
   {
      if (strcmp(str, uvar[i].name) == 0)
      {
         if (strcmp("char", uvar[i].typ) == 0) {
            ptr = uvar[i].value;
            return(ptr);
         }
         else {
            fprintf(stderr, "Typenfehler in Eingabefile: " "zeile %d\n", 
                    uvar[i].zeile);
            exit(-1);
         }
      } 
   }
}


void read_inputfile(FILE *IF, struct uservar **uvar)
{
   int linenr = 1;
   int listengroesse = 0;
  
   char line[512];
   char *rptr;
   char str[3][512];
  
   while (fgets(line, 512, IF) != NULL)
   {
      if ((rptr = strpbrk(line, "#\n")) != NULL)
      {
         line[rptr - line] = 0;
      }

      if ((rptr = strtok(line, " \t")) != NULL)
      {
         strcpy(str[0], rptr);
         if ((rptr = strtok(NULL, " \t")) == NULL)
         {
            fprintf(stderr,
            "male formed line %d, 3 arguments required"
            "found only 1\n", linenr);
            continue;
         }
         strcpy(str[1], rptr);
         if ((rptr = strtok(NULL, " \t")) == NULL)
         {
            fprintf(stderr,
            "male formed line %d, 3 arguments required"
            "found only 2\n", linenr);
            continue;
         }
         strcpy(str[2], rptr);
  
         *uvar = insert_var_into_list(*uvar, str,
                                      &listengroesse, linenr);
      }
      linenr++;
   }
}

