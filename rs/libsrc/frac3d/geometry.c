/*****************************************************************************/
/*                                                                           */
/* File:      geometry.c                                                     */
/*                                                                           */
/* Purpose:                                                                  */
/*                                                                           */
/*                                                                           */
/* Author:    Annette Silberhorn-Hemminger  (AH)                             */
/*            Institut fuer Wasserbau                                        */
/*            Universitaet Stuttgart                                         */
/*            email: annette.hemminger@iws.de                                */
/*                                                                           */
/*                                                                           */
/* Functions: void gen_random_fracture_list()                                */
/*            void gen_static_fracture_list (FILE *PF)                       */
/*            void gen_static_trace_list (FILE *PF)                          */
/*            int FourPointsLayInPlane()                                     */
/*                                                                           */
/*****************************************************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <math.h>
#include "functions.h"
#include "geometry.h"

double pi = 3.141592654;


/*****************************************************************************/
/*  Geometrie-Eigenschaften der random Kluefte generieren                    */
/*  Funktion:  gen_random_fracture_list                                      */
/*****************************************************************************/
void gen_random_fracture_list() /* call by function gen_fracture_list in
				   prog_functions.c */
{
   int i,j;
   int help_j;
   int count50 = 50, n50 = 0;
   int count200 = 200, n200 = 0;
   int count500 = 500, n500 = 0;

   double length[2], constant_length, diagonal[2];
   double rand_percentage_orient, Fx_percentage_orient; 

   /**************************************************************************/
   /* Kluftgenerierung: Schleife Summe der Kluefte                           */
   /* nfrac_det :                                                            */
   /* nfrac     :                                                            */
   /**************************************************************************/
  
   /*set defualt value*/
   /*AH raus, 16.05.: for(i=nfrac_det; i < nfrac; i++)*/
   i                = nfrac_det;
   frac_surface     = frac_surface_det; 
             /*assign the deterministic fracture surface to the total surface*/ 
   frac_dens_3d_sim = 0; 

   while (frac_dens_3d_sim < frac_dens_3d) 
   {
      /* generate mid-point 'midpt' of the new fracture plane*/
      midpt.x = dom_min.x + ran3(&rseed) * (dom_max.x - dom_min.x) ;  
      midpt.y = dom_min.y + ran3(&rseed) * (dom_max.y - dom_min.y) ;
      midpt.z = dom_min.z + ran3(&rseed) * (dom_max.z - dom_min.z) ;

      /***********************************************************************/
      /* Im ersten Stochastik -- Durchgang (Bed: i==nfrac_det) werden die    */
      /*    Variablen eingelesen                                             */
      /*                                                                     */
      /*                                                                     */
      /***********************************************************************/
      if (i == nfrac_det)
      {
         orientation_type = get_var_integer(uvar, "orientation_type");
         norient          = get_var_integer(uvar, "norient");

         /********************************************************************/
         /* Speicher allokieren (dynamische Felder allokieren)               */
         /********************************************************************/
         percentage_orient     = (double *) malloc(sizeof (double) * norient);
         percentage_orient_sim = (double *) malloc(sizeof (double) * norient);
         alpha                 = (double *) malloc(sizeof (double) * norient);
         Phi                   = (double *) malloc(sizeof (double) * norient);
         kappa                 = (double *) malloc(sizeof (double) * norient);


         if ((percentage_orient == NULL) || (alpha == NULL) || 
          (Phi == NULL) || (kappa == NULL))
         {
            printf("\nMemory allocation failed  (fracture orientation) \n");
            exit (-1);
         }


         for (j=0; j<norient; j++)
         {
            sprintf (char_percentage_orient, "percentage_orient[%d]",j); 
            sprintf (char_alpha, "alpha[%d]",j); 
            sprintf (char_Phi, "Phi[%d]",j); 
            sprintf (char_kappa, "kappa[%d]",j); 

            percentage_orient[j] = get_var_double(uvar, char_percentage_orient);
            percentage_orient_sim[j] = 0.0;  /*set default value*/
            alpha[j]                 = get_var_double(uvar, char_alpha);
            Phi[j]                   = get_var_double(uvar, char_Phi);
            kappa[j]                 = get_var_double(uvar, char_kappa);


            /* Winkel alpha und phi vom Gradmass ins Bogenmass umrechnen */
            alpha[j] = alpha[j] * pi / 180;
            Phi[j]   = Phi[j]   * pi / 180;
         }
      }

      /***********************************************************************/
      /* Normalenvektor der Ebene generieren  norm                           */
      /*   1. Anwendung der spaehrischen Normalverteilung                    */
      /*   2. Winkel phi : Winkel Normalenvektor und seiner Projektion       */
      /*      in der xy-Ebene berechnen                                      */
      /*                                                                     */
      /* Modification: AH 12.05.00                                           */
      /***********************************************************************/
      rand_percentage_orient = ran3(&rseed);
      Fx_percentage_orient   = 0.0; 

      for (j=0; j<norient; j++)
      {
         Fx_percentage_orient += percentage_orient[j];

         if (rand_percentage_orient <= Fx_percentage_orient)
         {
            switch(orientation_type)
            {
              case 1:        /* Normalenvektor direkt aus Hauptorientierung */
                norm = kluftor_norm_nonvariat(alpha[j], Phi[j]);
                break;

              case 2:               /* Variation of the Fisher distribution */
                norm = kluftor_sphere_fisher_AH(alpha[j],Phi[j],kappa[j]);
                break;
            }
            percentage_orient_sim[j] += 1;
            help_j=j;
            break;
         }
      }
    
      /***********************************************************************/
      /* Projektion des Normalenvektors in die (x-y)-Ebene                   */
      /* Projektionsvektor mit: (x = norm.x , y = norm.y , z = 0)            */
      /* Berechnen des Winkels phi zwischen den beiden "Geraden"             */
      /* 0 <= phi <= 90° (pi/2)                                              */
      /***********************************************************************/
      cos_phi = sqrt((norm.x * norm.x) + (norm.y * norm.y)) /
         sqrt((norm.x * norm.x) + (norm.y * norm.y) + (norm.z * norm.z)) ;     
      phi = acos( cos_phi);
      phi_grad = acos( cos_phi) * 180 / pi ;  /* Kontrolle AH */
    
    
      /***********************************************************************/
      /* Hilfspunkt  help_pt  generieren (Element der Kluftebene)            */
      /* Winkel phi <= pi/4  -->  Kluftebene nahezu horizontal               */
      /*        phi >  pi/4  -->  Kluftebene nahezu vertikal                 */
      /***********************************************************************/
    
      if (phi >= 0.00 && phi <= pi/4) 
      {
         help_pt.x = ran3(&rseed) ;
         help_pt.y = ran3(&rseed) ;
      
         help_pt.z = (norm.x*(midpt.x-help_pt.x) + 
                      norm.y*(midpt.y-help_pt.y) + norm.z*midpt.z) / norm.z;
      }
    
      else if ( phi > pi/4  &&  phi <= pi/2) 
      {
         help_pt.x = ran3(&rseed) ;
         help_pt.z = ran3(&rseed) ;
      
         help_pt.y = (norm.x*(midpt.x-help_pt.x) + 
    	              norm.z*(midpt.z-help_pt.z) + norm.y*midpt.y) / norm.y; 
      }
    
      else
          printf("\nWinkel phi kleiner bzw. groesser als 45° --> ERROR \n");
    

      /***********************************************************************/
      /* Kluftlaengen generieren  length[2]                                  */
      /* Winkel phi <= pi/4  -->  Kluftebene nahezu horizontal               */
      /*                          horizontale Spurlaengenverteilung          */
      /*        phi >  pi/4  -->  Kluftebene nahezu vertikal                 */
      /*                            horizont. + vertikale Spurl.vert.        */
      /***********************************************************************/
      if (i == nfrac_det) length_dist_type = get_var_integer(uvar,"length_dist_type");

      switch(length_dist_type)
      {
         case 1:    /* Spurlaengenverteilung mit Erlang 2 Verteilung */
            if (i == nfrac_det)
            {
               fprintf(stdout,"\n\nSpurlaengenverteilung mit Erlang-2-Verteilung");
               lambda_h        = get_var_double(uvar, "lambda_h_1");
               X0_h            = get_var_double(uvar, "X0_h");
               lambda_v        = get_var_double(uvar, "lambda_v_1");
               X0_v            = get_var_double(uvar, "X0_v");
               epsilon_Erlang  = get_var_double(uvar, "epsilon_Erlang");
               fprintf(stdout,"\n   lambda_h=%.2f \t X0_h=%.2f ", lambda_h, X0_h);
               fprintf(stdout,"\n   lambda_v=%.2f \t X0_v=%.2f \t epsilon_Erlang=%.2f", 
                                    lambda_v, X0_v, epsilon_Erlang);
            }

            if (phi >= 0.00 && phi <= pi/4) 
            {
               xrand = ran3(&rseed);
               length[0] = erl2vert(xrand, lambda_h, X0_h, epsilon_Erlang);
               xrand = ran3(&rseed);
               length[1] = erl2vert(xrand, lambda_h, X0_h, epsilon_Erlang);
            }
            else if ( phi > pi/4  &&  phi <= pi/2) 
            {
               xrand = ran3(&rseed);
               length[0] = erl2vert(xrand, lambda_h, X0_h, epsilon_Erlang);
               xrand = ran3(&rseed);
               length[1] = erl2vert(xrand, lambda_v, X0_v, epsilon_Erlang);
            }
         break;
	
         case 2:    /* Spurlaengenverteilung mit Exponentialverteilung */
            if (i == nfrac_det) 
            {
               fprintf(stdout,"\n\nSpurlaengenverteilung mit Exponentialverteilung");
               lambda_h = get_var_double(uvar, "lambda_h_2");
               lambda_v = get_var_double(uvar, "lambda_v_2");
               fprintf(stdout,"\nlambda_h=%.2f\nlambda_v=%.2f",lambda_h,lambda_v);
            }
         
            if (phi >= 0.00 && phi <= pi/4) 
            {
               xrand = ran3(&rseed);
               length[0] = expvert(xrand, lambda_h);
               xrand = ran3(&rseed);
               length[1] = expvert(xrand, lambda_h);
            }
            else if ( phi > pi/4  &&  phi <= pi/2) 
            {
               xrand = ran3(&rseed);
               length[0] = expvert(xrand, lambda_h);
               xrand = ran3(&rseed);
               length[1] = expvert(xrand, lambda_v);
            }
         break;

         case 3:    /* constant value for all trace length */
            if (i == nfrac_det) 
            {
               fprintf(stdout,"\n\nTrace length with constant value ");
               constant_length = get_var_double(uvar, "const_length");
            }
               length[0]= length[1] = constant_length; 
         break;
      } 

      /***********************************************************************/
      /* 1a.) Richtungsvektor r der Geraden (midpt -> help_pt) bestimmen     */
      /*      Richtungsvektor r parallel zur Richtung (pt[0] -> pt[1])       */
      /*                                                                     */
      /* 1b.) Richtungsvektor b steht senkrecht auf dem Normalenvektor       */
      /*      und auf dem Richtungsvektor r  --> Vektorprodukt               */
      /*      Richtungsvektor b parallel zur Richtung (pt1 -> pt4)           */
      /*                                                                     */
      /***********************************************************************/
      vector_r.x = help_pt.x - midpt.x;
      vector_r.y = help_pt.y - midpt.y;
      vector_r.z = help_pt.z - midpt.z;
    
      vector_b.x = ( norm.y * vector_r.z) - (norm.z * vector_r.y);
      vector_b.y = (-norm.x * vector_r.z) + (norm.z * vector_r.x);
      vector_b.z = ( norm.x * vector_r.y) - (norm.y * vector_r.x);
    
    
      /***********************************************************************/
      /* 2.) Betrag rr des Vektors r                                         */
      /*     Betrag bb des Vektors b                                         */
      /***********************************************************************/
      rr =  sqrt( vector_r.x * vector_r.x + vector_r.y * vector_r.y 
                                          + vector_r.z * vector_r.z );

      bb = sqrt( vector_b.x * vector_b.x + vector_b.y * vector_b.y 
                                         + vector_b.z * vector_b.z );
    
      /***********************************************************************/
      /* 2.) Faktor lambda1 bestimmen                                        */
      /*     Faktor lambda2 bestimmen                                        */
      /***********************************************************************/
      lambda1 = length[0] / rr;
      lambda2 = length[1] / bb;

      /***********************************************************************/
      /* generate fracture edge points:  pt[0] / pt[1] / pt[2] / pt[3]       */
      /*                                                                     */
      /***********************************************************************/
      pt[0].x = midpt.x + 0.5 * (-lambda1*vector_r.x - lambda2*vector_b.x);
      pt[0].y = midpt.y + 0.5 * (-lambda1*vector_r.y - lambda2*vector_b.y);
      pt[0].z = midpt.z + 0.5 * (-lambda1*vector_r.z - lambda2*vector_b.z);
    

      pt[1].x = midpt.x + 0.5 * ( lambda1*vector_r.x - lambda2*vector_b.x);
      pt[1].y = midpt.y + 0.5 * ( lambda1*vector_r.y - lambda2*vector_b.y);
      pt[1].z = midpt.z + 0.5 * ( lambda1*vector_r.z - lambda2*vector_b.z);


      pt[2].x = midpt.x + 0.5 * ( lambda1*vector_r.x + lambda2*vector_b.x);
      pt[2].y = midpt.y + 0.5 * ( lambda1*vector_r.y + lambda2*vector_b.y);
      pt[2].z = midpt.z + 0.5 * ( lambda1*vector_r.z + lambda2*vector_b.z);
    

      pt[3].x = midpt.x + 0.5 * (-lambda1*vector_r.x + lambda2*vector_b.x);
      pt[3].y = midpt.y + 0.5 * (-lambda1*vector_r.y + lambda2*vector_b.y);
      pt[3].z = midpt.z + 0.5 * (-lambda1*vector_r.z + lambda2*vector_b.z);
    

      /***********************************************************************/
      /* calculate the length of the diagonal                                */
      /*   diagonal[0] = between point pt[0] and pt[2]                       */
      /*   diagonal[1] = between point pt[1] and pt[3]                       */
      /* apply function 'abs_vec_pt_pt()' (intersection_points.c)            */
      /***********************************************************************/
      diagonal[0] =  abs_vec_pt_pt(&pt[0], &pt[2]);
      diagonal[1] =  abs_vec_pt_pt(&pt[1], &pt[3]);

      /***********************************************************************/
      /* Kluftoeffnungsweite generieren: frac_aperture                       */
      /* konstanten Wert bzw. ueber log-Normalverteilung                     */
      /***********************************************************************/
      if (i==0) aperture_dist_type=get_var_integer(uvar,"aperture_dist_type");
    
      switch(aperture_dist_type)
      {
         case 1:    /* Aperture with constant value */
            if ( i == nfrac_det)
            {
               fprintf(stdout,"\n\nAperture with constant value: "); 
               aperture = get_var_double(uvar, "aperture");
               fprintf(stdout,"%.8f\n", aperture);
            }
            frac_aperture = aperture;
            break;
         case 2:    /* Aperture log normal distributed */
            if ( i == nfrac_det)
            {
               fprintf(stdout,"\n\nAperture log normal distributed: ");
               sigma = get_var_double (uvar, "sigma");
               mue   = get_var_double (uvar, "mue");
               xrand = ran3(&rseed);
               fprintf(stdout,"\n   sigma =%.2f \t mue =%.2f\n", sigma, mue);
            }
            frac_aperture = lognormvert(xrand, sigma, mue);
            break;
      } 

      if ( i < 1) {
         FRAC = add_FRAC_to_list(i, length, diagonal, norm, pt, 
		  		                 frac_aperture);
      }
      
      else
      {
         if (i == ( (n500+1) * count500))  {
            fprintf(stdout,"\n Checking overlapping  .............. %20d. fracture plane", i);
            n500++;
         }


         /*******************************************************************/
         /* Ueberpruefung auf Ueberlappung zwischen der zuletzt generierten */
         /* Ebene und den zuvor in die Liste eingetragenen.                 */
         /*******************************************************************/
         if ((parallel = test_parallel(i, norm, pt, frac_aperture)) == 1) 
         {
            i--;
            percentage_orient_sim[help_j] -= 1;    
                   /*muss wieder abgezogen werden, da Element verworfen wird*/
/***************
            fprintf(stdout,"\n                Paralleles Element wird verworfen\n");
***************/
         }
	  
         /*******************************************************************/
      	 /* Liste von FRAC erstellen                                        */
      	 /*******************************************************************/
         else
         {
            FRAC = add_FRAC_to_list(i, length, diagonal, norm, pt, 
            frac_aperture); /* call function from 
            build_list.c */
         }
      }

      ++i;  
      nfrac = i; 

      /**********************************************************************/
      /* Calculating fracture density 'frac_dens_3d_sim' of the generated   */
      /* field.                                                             */
      /*                                                                    */
      /* Case 1: frac_dens_type = 1  --> frac_dens_3d_sim [m^2 / m^3]       */
      /*         Calculating the fracture surface of the fracture 'i' and   */
      /*         add it to the total fracture surface 'frac_surface'        */
      /*                                                                    */
      /* Case 2: frac_dens_type = 2  --> frac_dens_3d_sim [fracture / m^3]  */
      /**********************************************************************/
      frac_dens_type = get_var_integer(uvar, "frac_dens_type");
      switch (frac_dens_type) {
      case 1:  frac_surface += length[0] * length[1];
               frac_dens_3d_sim = frac_surface / domain_volume; 
               break;
      case 2:  frac_dens_3d_sim = nfrac / domain_volume; 
               break;
      }

   }

   switch (frac_dens_type) {
   case 1:  fprintf(stdout,"\n\nFracture density [m^2/m^3] frac_dens_3d_sim=%f ", 
                              frac_dens_3d_sim);
            break;
   case 2:  fprintf(stdout,"\n\nFracture density [fracture/m^3] frac_dens_3d_sim=%f ", 
                              frac_dens_3d_sim);
            break;
   }
   fprintf(stdout,"\nNumber of fracture elements nfrac=%d \n", nfrac);

   fprintf(stdout,"\nHauptorientierungen  ");
   for (i = 0; i < norient; i++)
   {
      if (i==0) fprintf(stdout,"[%d] = %7.3f [\%]\n", 
                       i,percentage_orient_sim[i]/nfrac*100);
      else      fprintf(stdout,"                     [%d] = %7.3f [\%]\n", 
                       i,percentage_orient_sim[i]/nfrac*100);
   }

   /**************************************************************************/
   /* Speicher wieder frei geben                                             */
   /*  (frueher nicht moeglich, da innerhalb der (nfrac) -- for-Schleife     */
   /*   darauf zurueckgegriffen wird)                                        */
   /**************************************************************************/
   free(percentage_orient); 
   free(alpha); 
   free(Phi);  
   free(kappa);

}



/*****************************************************************************/
/* gen_static_fracture_list()                                                */
/*                                                                           */
/*  Read the four corner nodes of a fracture plane from an input file        */
/*  The fracture plane has not to be a rectangle !!!                         */
/*  The four corner nodes has to lay within a plane --> has to be checked    */
/*                                                                           */
/*****************************************************************************/
void gen_static_fracture_list (FILE *PF)
{
   int i=0, pt_nr=0, linenr=1; 
   double length[2], diagonal[2];
   struct point a, b, c, d, e, f;
   struct point p0, p1, p2, p2old, p3; /*arguments fct. 'FourPointsLayInPlane()'*/

   /*set default value*/
   frac_surface_det=0;

   while (fgets(line, 512, PF) != NULL)   
   {
      while(1)
      {
         if(pt_nr < 4)
         {   
            fgets(line, 512, PF);
            if ((rptr = strpbrk(line, "#\n")) != NULL)   
            {
               line[rptr - line] = 0;   
            }
            if ((rptr = strtok(line, " \t")) != NULL)   
            {
               pt[pt_nr].x = strtod(rptr, NULL);
               if ((rptr = strtok(NULL, " \t")) == NULL)    
               {    
                  fprintf(stderr,   
                  "male formed line %d, 3 arguments required"    
                  " found only 1\n", linenr);    
                  continue;    
               }    
               pt[pt_nr].y = strtod(rptr, NULL);
               if ((rptr = strtok(NULL, " \t")) == NULL)   
               {    
                  fprintf(stderr,    
                  "male formed line %d, 3 arguments required"    
                  " found only 2\n", linenr);    
                  continue;    
               }    
               pt[pt_nr].z = strtod(rptr, NULL);
               pt_nr++;
            }
         }
         else
         {
            pt_nr = 0;
            break;
         }
         linenr++;    
      }
 
      a.x = pt[1].x - pt[0].x;
      a.y = pt[1].y - pt[0].y;
      a.z = pt[1].z - pt[0].z;

      b.x = pt[3].x - pt[0].x;
      b.y = pt[3].y - pt[0].y;
      b.z = pt[3].z - pt[0].z;


      /***********************************************************************/
      /* TODO : check, if the four points lay in a plane                     */
      /***********************************************************************/
      p0=pt[0];  p1=pt[1];  p2=pt[2],  p3=pt[3];

      if (FourPointsLayInPlane(&p0, &p1, &p2, &p3) == 1)
      {
         p2old=pt[2];
         pt[2]=p2;
         fprintf(stdout,"\n!!!!Attention: FRAC[%d] ",i);
         fprintf(stdout,"\n    Not all of the four corner nodes lay in a plane ");
         fprintf(stdout,"\n    --> Old point   FRAC[%d].pt[2]: x=%f  y=%f  z=%f ",
                                   i, p2old.x, p2old.y, p2old.z);
         fprintf(stdout,"\n    --> New point   FRAC[%d].pt[2]: x=%f  y=%f  z=%f \n",
                                   i, pt[2].x, pt[2].y, pt[2].z);
      }
 
      /***********************************************************************/
      /* calculate the normal vector of the plane                            */
      /***********************************************************************/
      norm.x = a.y*b.z - a.z*b.y;
      norm.y = a.z*b.x - a.x*b.z;
      norm.z = a.x*b.y - a.y*b.x;

      /***********************************************************************/
      /* calculate the length of the diagonal                                */
      /*   diagonal[0] = between point pt[0] and pt[2]                       */
      /*   diagonal[1] = between point pt[1] and pt[3]                       */
      /***********************************************************************/
      c.x = pt[2].x - pt[0].x;
      c.y = pt[2].y - pt[0].y;
      c.z = pt[2].z - pt[0].z;

      d.x = pt[3].x - pt[1].x;
      d.y = pt[3].y - pt[1].y;
      d.z = pt[3].z - pt[1].z;

      diagonal[0] = sqrt(pow(c.x,2)+pow(c.y,2)+pow(c.z,2));
      diagonal[1] = sqrt(pow(d.x,2)+pow(d.y,2)+pow(d.z,2));
  
      /***********************************************************************/
      /* calculate the length of the fracture sides                          */
      /*   length[0]   = between point pt[0] and pt[1]                       */
      /*   length[1]   = between point pt[1] and pt[2]                       */
      /***********************************************************************/
      e.x = pt[1].x - pt[0].x;
      e.y = pt[1].y - pt[0].y;
      e.z = pt[1].z - pt[0].z;

      f.x = pt[2].x - pt[1].x;
      f.y = pt[2].y - pt[1].y;
      f.z = pt[2].z - pt[1].z;

      length[0] = sqrt(pow(e.x,2)+pow(e.y,2)+pow(e.z,2));
      length[1] = sqrt(pow(f.x,2)+pow(f.y,2)+pow(f.z,2));
  

      /***********************************************************************/
      /* assign the fracture aperture to the fracture plane : frac_aperture  */
      /*    constant value                                                   */
      /*    log normal distributed                                           */
      /***********************************************************************/
      if (i==0) aperture_dist_type=get_var_integer(uvar,"aperture_dist_type");
    
      switch(aperture_dist_type)
      {
         case 1:    /* Aperture with constant value */
            if ( i == 0)
            {
               fprintf(stdout,"\n\nAperture with constant value: "); 
               aperture = get_var_double(uvar, "aperture");
               fprintf(stdout,"%.8f\n", aperture);
            }
            frac_aperture = aperture;
            break;
         case 2:    /* Aperture log normal distributed */
            if ( i == 0)
            {
               fprintf(stdout,"\n\nAperture log normal distributed: ");
               sigma = get_var_double (uvar, "sigma");
               mue   = get_var_double (uvar, "mue");
               xrand = ran3(&rseed);
               fprintf(stdout,"\nsigma =%.2f \nmue =%.2f\n", sigma, mue);
            }
            frac_aperture = lognormvert(xrand, sigma, mue);
             break;
      }

      if ( i < 1)
      {
         /*printf("\nfrac_nr = %d", i);*/
         FRAC = insert_FRAC_into_list(i, length, diagonal, norm, pt, 
         frac_aperture, &listengroesse);
      }

      else
      {
         /********************************************************************/
         /* Check, if the new fracture plane overlapps with one of the       */
         /* generated ones (the one which are already in the list 'FRAC[]')  */
         /********************************************************************/
        if ((parallel = test_parallel(i, norm, pt, frac_aperture)) == 1) 
         {
            i--;
	    fprintf(stdout,"\n!!!ATTENTION!!!\nThe Check, if new fracture plane is overlapping an existing one was positiv,\nthe new parallel elmement will be overwritten!\n\n");
         }	  
         /*****************************************************************/
         /* add the new fracture plane to the list 'FRAC[]'               */
         /*****************************************************************/
         else
         {
            FRAC = insert_FRAC_into_list(i, length, diagonal, norm, pt, 
            frac_aperture, &listengroesse);
         }
      }

   frac_surface_det = FRAC[i].length[0] * FRAC[i].length[1];
   i++; 
   }
   
   fprintf(stdout,"\nDeterministic fracture surface [m^2] frac_surface_det=%f ", 
                     frac_surface_det);
   nfrac_det = listengroesse;
}


/****************************************************************************/
/* gen_static_trace_list()                                                  */
/*                                                                          */
/*   PURPOSE  : generates based on given nodes (coordinate values) of the   */
/*              "1D elements in 3D space" the TRACE - list                  */
/*                                                                          */
/*   CALLED BY: function 'gen_fracture_list' in file 'prog_functions.c'     */
/*                                                                          */
/*   ARGUMENTS:                                                             */
/*                                                                          */
/*   RETURN   :                                                             */
/*                                                                          */
/****************************************************************************/
void gen_static_trace_list (FILE *PF)
{ 
   int i=0, pt_nr=0, linenr=1; 
   double trace_length;

   while (fgets(line, 512, PF) != NULL)   
   {
      while(1)
      {
         if(pt_nr < 2)
         {   
            fgets(line, 512, PF);
            if ((rptr = strpbrk(line, "#\n")) != NULL)   
            {
               line[rptr - line] = 0;   
            }
            if ((rptr = strtok(line, " \t")) != NULL)   
            {
               pt[pt_nr].x = strtod(rptr, NULL);
               if ((rptr = strtok(NULL, " \t")) == NULL)    
               {    
                  fprintf(stderr,   
                  "male formed line %d, 3 arguments required"    
                  " found only 1\n", linenr);    
                  continue;    
               }    
               pt[pt_nr].y = strtod(rptr, NULL);
               if ((rptr = strtok(NULL, " \t")) == NULL)   
               {    
                  fprintf(stderr,    
                  "male formed line %d, 3 arguments required"    
                  " found only 2\n", linenr);    
                  continue;    
               }    
               pt[pt_nr].z = strtod(rptr, NULL);
               pt_nr++;
            }
         }
         else
         {
            pt_nr = 0;
            break;
         }
         linenr++;    
      }
      /*printf("\n");*/
  

      /***********************************************************************/
      /* calculate the trace length                                          */
      /***********************************************************************/
      trace_length = sqrt( (pt[1].x - pt[0].x) * (pt[1].x - pt[0].x) 
                          +(pt[1].y - pt[0].y) * (pt[1].y - pt[0].y)); 
  
      /***********************************************************************/
      /* generate fracture trace aperture                                    */
      /*   case 1: constant value                                            */
      /*   case 2: log -normal distributed                                   */
      /***********************************************************************/
      if (i==0) aperture_dist_type=get_var_integer(uvar,"aperture_dist_type");
    
      switch(aperture_dist_type)
      {
         case 1:    /* Aperture with constant value */
            if ( i == 0)
            {
               fprintf(stdout,"\n\nAperture with constant value: "); 
               aperture = get_var_double(uvar, "aperture");
               fprintf(stdout,"%.8f\n", aperture);
            }
            frac_aperture = aperture;
            break;
         case 2:    /* Aperture log normal distributed */
            if ( i == 0)
            {
               fprintf(stdout,"\n\nAperture log normal distributed: ");
               sigma = get_var_double (uvar, "sigma");
               mue   = get_var_double (uvar, "mue");
               xrand = ran3(&rseed);
               fprintf(stdout,"\nsigma =%.2f \nmue =%.2f\n", sigma, mue);
            }
            frac_aperture = lognormvert(xrand, sigma, mue);
            break;
      }
  
      if ( i < 1)
      {
         TRACE = add_TRACE_to_StructTraceList(i, trace_length, pt, 
                                              frac_aperture, 
                                              &listengroesse);
      }
      else  
      {
         /********************************************************************/
         /* Ueberpruefung auf Ueberlappung zwischen der zuletzt              */
         /* generierten Ebene und den zuvor in die Liste eingetragenen.      */
         /********************************************************************/
         /*printf("\ntrace_nr = %d", i);*/
         /*AH 01.11.99 TODO: testen, ob zwei Trace aufeinander liegen! */
/*
         if (parallel = test_parallel(i, norm, pt, frac_aperture) == 1) 
         {
            i--;
            fprintf(stdout,"\n                Paralleles Element wird verworfen\n");
         }
         else
         {
            TRACE = add_TRACE_to_StructTraceList(i, trace_length, pt, 
                                      frac_aperture, &listengroesse);
         }
*/	  
         /********************************************************************/
         /* Liste von FRAC erstellen                                         */
         /********************************************************************/
         TRACE = add_TRACE_to_StructTraceList(i, trace_length, pt, 
                                              frac_aperture, &listengroesse);
      }
      /*printf("\nListengroesse %d", listengroesse);*/
      i++; 
   }
   ntrace = listengroesse;
}


/*****************************************************************************/
/*                                                                           */
/*                           F U N C T I O N S                               */
/*                                                                           */
/*****************************************************************************/
/* FourPointsLayInPlane()                                                    */
/*                                                                           */
/* Purpose: Do the four points lay in a plane?                               */
/*                                                                           */
/*              3 ------------- 2                                            */
/*                |           |                                              */
/*                |          |                                               */
/*              0 |__________| 1                                             */
/*                                                                           */
/* Parameters:                                                               */
/*                                                                           */
/*                                                                           */
/* Description:                                                              */
/*                                                                           */
/*  Four points are given p0, p1, p2, and p3. The plane E ist defined by the */
/*  three points p0, p1, and p3. We check, if point p2 lays within in the    */
/*  plane E. If point p2 does not lay in the plane, we calculate the         */
/*  projection point p2' of point p2, which lays in the plane.               */
/*                                                                           */
/*  The following steps have to be done:                                     */
/*  1.) plane E is defined by the three points points p0, p1, and p3.        */
/*      E: x = x_0 + lambda*(x_1-x_0) + mue*(x_3-x_0)                        */
/*         x = x_0 + lambda*(   a   ) + mue*(   b   )                        */
/*                                                                           */
/*  2.) determine the normal vector of the plane E                           */
/*         n = (a) x (b)                                                     */
/*                                                                           */
/*  3.) determine the normalised normal vector n_0                           */
/*       n_0 = 1/|n| * n                                                     */
/*                                                                           */
/*  --> now we have the 'Hessesche Normalform' of the plane E                */
/*      E: n_0 * (x - x_0) = 0                                               */
/*                                                                           */
/*  4.) calculate the orientated distance between point P and the plane E    */
/*         d = n_0 * (x_P - x_0)                                             */
/*                                                                           */
/*      three cases are possible:                                            */
/*         d < 0 : point P and the origin of the cartesian coordinate system */
/*                 lay in the same half-space ('Halbraum') with respect to   */
/*                 plane E                                                   */
/*         d > 0 : point P and the origin of the cartesian coordinate system */
/*                 do not lay in the same half-space ('Halbraum') w.r.t.     */
/*                 plane E                                                   */
/*         d = 0 : point P lays in the plane E                               */
/*                                                                           */
/*  5.) calculate the projection point p2_new of point p2                    */
/*      x_p2_new= x_p2 - (d * n_0)                                           */
/*                                                                           */
/*                                                                           */
/*  6.) point p2 is replaced by its projection point p2'                     */
/*                                                                           */
/*                                                                           */
/* Return :  point_changed = 0: point p2 lays in plane E                     */
/*           point_changed = 1: new point p2                                 */
/*                                                                           */
/*****************************************************************************/
int FourPointsLayInPlane(struct point *p0, struct point *p1, 
                         struct point *p2, struct point *p3)
{
   int point_changed = 0;
   double distance, length_n; 

   struct point a, b, n, n_0, p2_new;

   /**************************************************************************/
   /*  1.) plane E is defined by the three points points p0, p1, and p3.     */
   /*      x = x_0 + lambda*(x_1 - x_0) + mue*(x_3 - x_0)                    */
   /*      x = x_0 + lambda*(    a    ) + mue*(    b    )                    */
   /**************************************************************************/
   a.x = p1->x - p0->x;
   a.y = p1->y - p0->y;
   a.z = p1->z - p0->z;

   b.x = p3->x - p0->x;
   b.y = p3->y - p0->y;
   b.z = p3->z - p0->z;


   /**************************************************************************/
   /*  2.) determine the normal vector of the plane E:   n = (a) x (b)       */
   /**************************************************************************/
   n.x = a.y*b.z - a.z*b.y;
   n.y = a.z*b.x - a.x*b.z;
   n.z = a.x*b.y - a.y*b.x;

   /**************************************************************************/
   /*  3.) determine the normalised normal vector n_0:  n_0 = 1/|n| * n      */
   /*      |n| = length_n                                                    */
   /*  --> Hessesche Normalform                                              */
   /**************************************************************************/
   length_n = abs_vec_point(&n);
   n_0.x = n.x / length_n;
   n_0.y = n.y / length_n;
   n_0.z = n.z / length_n;
   
   /**************************************************************************/
   /*  4.) calculate the orientated distance between point P and the plane E */
   /*      out of the Hessesche Normalform:  d = n_0 * (x_P - x_0)           */
   /**************************************************************************/
   distance =   n_0.x * (p2->x - p0->x)  
              + n_0.y * (p2->y - p0->y)  
              + n_0.z * (p2->z - p0->z);  

   /**************************************************************************/
   /* 4.1)                                                                   */
   /*   case1: fabs(distance) is bigger than epsilon_0 (~zero),              */
   /*          point P2 lays outside of the plane E.                         */
   /*          -> the projection point p2' of point p2  has to be calculated */
   /*   case2: fabs(distance) is smaller than epsilon_0 (~zero),             */
   /*          point P2 lays inside of the plane E.                          */
   /******************************yy******************************************/
   if (fabs(distance) >= epsilon_0)
   {
      /*5.) calculate the projection point p2_new: x_p2_new= x_p2 -(d * n_0) */
      p2_new.x = p2->x  - (distance * n_0.x);
      p2_new.y = p2->y  - (distance * n_0.y);
      p2_new.z = p2->z  - (distance * n_0.z);

      *p2 = p2_new;

      point_changed = 1;
   }

   return (point_changed);
}


