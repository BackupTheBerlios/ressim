/*****************************************************************************/
/* File:      build_list.h                                                   */
/*                                                                           */
/* Purpose:   Erstellung einer Liste von FRAC-Elementen                      */
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
#include "functions.h"


/*****************************************************************************/
/* Allocate memory for diferent typs of variables, structs ....              */
/*****************************************************************************/
struct point   *AllocateStructPointList(int n);

struct edge    *AllocateStructEdgeList(int n);

