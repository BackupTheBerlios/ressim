/*****************************************************************************/
/*                                                                           */
/* File:      optimization.h                                                 */
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
/*****************************************************************************/

   
/****************************************************************************/
/* data structures: optimization.c                                          */
/****************************************************************************/
int nclass_length;                                     /* number of classes */
int nclass_aperture;          
int nlength;

double *new_class_length;        
double *old_class_length;        
double *var_class_length;        

int *new_class_aperture;      
int *old_class_aperture;      

double delta_length;                                   /* delta class value */
double delta_aperture;

double mean_length;                                           /* mean value */
double mean_aperture;

double standdev_length;                               /* standard deviation */
double standdev_aperture;

double min_length,   max_length;                           /* min/max value */
double min_aperture, max_aperture;



/****************************************************************************/
/* extern variables                                                         */
/****************************************************************************/


/****************************************************************************/
/* function declaration: subvolume_3D.c                                     */
/****************************************************************************/


/****************************************************************************/
/* extern function declaration:                                             */
/****************************************************************************/

