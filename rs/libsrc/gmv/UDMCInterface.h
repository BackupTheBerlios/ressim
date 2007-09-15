#ifndef CCI_H
#define CCI_H

#ifdef __cplusplus
extern "C" {
#endif

/* LA-UR-04-6462 */

#define COMP_RANK  4

/**< max rank for a tensor, it may be
     changed to a larger number for the maximum
     value of rank in a tensor.     */

/**
\mainpage Unified Data Model (UDM) - C/C++ Interface
\author William W. Dai, CCN-8, Los Alamos National Laboratory 
\version V-2004-09-10
\section Copyright Copyright

  Copyright (c) 2004, The Regents of the University of California.
  All rights reserved.

  Copyright (2004). The Regents of the University of California.
  This software was produced under U.S. Government contract
  W-7405-ENG-36 for Los Alamos National Laboratory (LANL),
  which is operated by the University of California for the U.S.
  Department of Energy. The U.S. Government has rights to use,
  reproduce, and distribute this software. NEITHER THE GOVERNMENT
  NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
  ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software
  is modified to produce derivative works, such modified software
  should be clearly marked, so as not to confuse it with the
  version available from LANL.

  Additionally, redistribution and use in source and binary forms,
  with or without modification, are permitted provided that the
  following conditions are met:
\verbatim
    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the following
      disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the University of California, LANL, the
      U.S. Government, nor the names of its contributors may be
      used to endorse or promote products derived from this
      software without specific prior written permission.
\endverbatim
  THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS
  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
  NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  SHALL THE UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  OF SUCH DAMAGE.

\section acknowledgements Acknowledgements
 This research is funded by the Department of Energy's ASCI program.
 This report is published under LA-UR No. 04-1485.
 Los Alamos National Laboratory is operated by the University of California
 for the National Nuclear Security Administration of the United States
 Department of Energy under contract W-7405-ENG-36.

\section abstract Abstract
 This UDM C/C++ User's Guide provides the information needed to read
 and write data in parallel or serial, and to query metadata using the
 Unified Data Model (UDM) library.
 This Guide also contains the typical usages of C/C++
 interface functions and example codes for the usages.

\section overview Overview

 The main goal of the UDM library is to provide simulation codes
 a parallel IO capability for one- and multi-dimensional arrays,
 structured and unstructured meshes, and variables defined on
 the meshes, and to provide an infrastructure for parallel dada
 processing tools. Since a file generated
 through the UDM library is self-describing, without bookkeeping
 users may read any part of a one- or multi-dimensional array,
 any part of a structured or unstructured mesh, and variables
 associated with any part of mesh. 
 The goal of the UDM C/C++ Interface is to expose the main
 functionality of the UDM library through a number of C/C++
 callable functions. 

 The data written through the UDM library
 are contained in a Hierarchical Data Format (HDF) file.
 The structure of an HDF file can be thought of as similar to
 a UNIX file system. In a Unix file system, directories can
 contain other files. The top directory is referred to as the
 "root" directory and has the "/" designation. Each file has
 certain attributes, e.g., read/write permissions, etc.

 In a HDF file, the object corresponding to a directory is a Group.
 A HDF Group is a container for other HDF Groups and HDF Datasets.
 HDF Datasets roughly correspond to Unix files normally containing 
 large binary data. Arrays, structured and unstructured meshes, and
 variables defined on meshes are examples of Datasets.

 Attributes are associated with a Group or a Dataset. Unlike Groups
 and Datasets, Attributes can only written once the Group or Dataset
 they will be associated with has been created. Likewise, Attributes
 are read by referring to the location of the object to which they are
 associated. Attributes allow additional information
 to be associated with a Group or Dataset.
 With Groups, Datasets and Attributes,
 one can create structured, self-describing files.

 The current UDM library is build on the top of the capability of
 one-dimensional array in HDF5, and the MPI library.
 Although there are tools to view HDF files, such as h5vew
 and h5dump, a data file generated through the UDM library, or a
 UDM file, may not be correctly interpreted through these tools.
 For example, multi-dimensional arrays, structured and unstructured meshes,
 and variables may not been correctly recognized through HDF5 tools.
 Even multi-dimensional arrays written through the UDM library may not
 be correctly recognized through HDF5 tools, because the layout of
 the arrays is optimized in UDM files, which is different
 from the one in HDF5 files. Furthermore, structured and unstructured
 meshes, and variables are the unique capabilities in the UDM library,
 and their layout in UDM files is optimized for their best possible IO
 performance. Therefore, currently only the functions in this User's Guide
 may correctly interpret multi-dimensional arrays, structured and
 unstructured meshes, and variables in UDM files.

 For all the C/C++ interface functions, an id of an object is unique,
 and it is non-negative. A function will return 0
 if no errors were encountered; otherwise, -1 will be returned.
 All constants shown in the User's Guide are included in the file

 - UDMCInterface.h

*/  

/**
\defgroup file Functions for File Open/Close

 This section covers the functions to open files and groups, and to
 close files.

*/

/**
\ingroup file Functions for File Open/Close

 UDM_FILE_MODE contains the values of open modes for files.

*/

enum UDM_FILE_MODE{
/** - file create */
 udm_file_create,
/** - file read only */
 udm_file_read_only,
/** - file read and write */
 udm_file_read_write
};
typedef enum UDM_FILE_MODE UDM_FILE_MODE;

/**
\ingroup file Functions for File Open/Close

 Given a relative path or full path for a file,
 FIO_open will create a new file or open an existing file.
 This function
 also automatically creates or opens the root group. The output, file_id,
 is also the id of the root group.

 - file_name: an input, the relative path or full path for a file,
              such as restart/file1.

 - mode: an input, one of the three values of UDM_FILE_MODE.

 - file_id: an output, the id of a file just created or opened. 

*/

int FIO_open(const char        *file_name,
             const UDM_FILE_MODE mode,
             int                 *file_id);

/**
\ingroup file Functions for File Open/Close

 FIO_close will close the file associated with a given file_id.

 - file_id: an input, the id of a file which was created or opened before.

*/

int FIO_close(const int file_id);

/**
\ingroup file Functions for File Open/Close

 FIO_group_open will open a sub-group under a group or the root group. 

 - parent_id: an input, the id of the parent group. 
   Since the id of a file is used also as the id for the root, 
  to open a group under a root, file_id should be used as parent_id.

 - path_and_name: an input, the path and name of a group.
   If the leading char in path_and_name is not '/', 
   path_and_name is relative to parent_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3", and 
   "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, parent_id may be 
   the id of any object
   in this file. 

 - group_id: an output, the id of the group just created or opened.

 - If there already exists a group path_and_name under parent_id in the file,
   no new groups will be created, but a group_id will be returned.

 - If there is no path_and_name under parent_id in the file, the whole path
   and the group will be created, and an id will be returned for
   the group.

 Example 1 in Section 7.2 shows the usage of FIO_open, FIO_group_open
 and FIO_close.
*/
/* EExample 1: ctest_001.c */

int FIO_group_open(const int   parent_id,
                   const char  *path_and_name,
                   int         *group_id);

/**
\defgroup array Functions for Writing/Reading Arrays

 This section covers the functions to write and read arrays. 

*/

/**
\ingroup array Functions for Writing/Reading Arrays

 UDM_DATA_TYPE contains the values of datatype.

*/

enum UDM_DATA_TYPE { 
     /** char */ 
     udm_char             = 1,     
     /** double */ 
     udm_double           = 2,     
     /** float  */ 
     udm_float            = 3,     
     /** int    */
     udm_int              = 4,     
     /** long   */
     udm_long             = 5,     
     /** long double */
     udm_long_double      = 6,     
     /** long long   */
     udm_long_long        = 7,     
     /** char*       */
     udm_string           = 8,     
     /* invalid      */ 
     udm_datatype_invalid = 9
};
typedef enum UDM_DATA_TYPE UDM_DATA_TYPE;

/**
\ingroup array Functions for Writing/Reading Arrays

 UDM_DATA_STRUCT defines the structure of one- or multi-dimensional arrays.

*/

struct UDM_DATA_STRUCT {
           int dims;           /**< The dimension 0 is the one changing most
                                    slowly, and the dimension (dims-1) is
                                    the one changing most fast.           */  

           long long *sizes;   /**< the sizes of an array on the current 
                                    pe in dims dimensions, which exclude
                                    any possible ghost points.            */

           long long *fsizes;  /**< an array with (dims + dims) elements for 
                                    the sizes of ghost (fake) points. 
                                    The first dims values are the sizes at
                                    the lower ends of dims dimensions, and
                                    the remaining dims values are the sizes
                                    at the higher ends.                   */

           long long *offsets; /**< offsets of the part of the array on the
                                    current pe in dims dimensions.        */

           long long *gsizes;  /**< the total sizes of an array in dims
                                    dimensions, excluding ghost points.   */

           long long *psizes;  /**< an array with dims elements, which is 
                                    for the sizes of pe configuration in 
                                    dims dimensions.
                                                                          */  
           long long *plist;   /**< an array with its size 
                                    psizes[0] * psizes[1] * ... * psizes[dims-1]
                                    for the order of pes in the
                                    pe configuration. plist sweeps the 
                                    dimension dims-1 first, and sweeps the
                                    dimension 0 last. By default, plist[i] = i,
                                    for i = 0, 1, ..., npes-1. 
\verbatim
           ----------------------------------------
    dim 1  |            |            |            |
           | plist(1)   | plist(3)   | plist(5)   |
      ^    |            |            |            |
      |    ----------------------------------------
      |    |            |            |            |
      |    | plist(0)   | plist(2)   | plist(4)   |
           |            |            |            |
           ----------------------------------------
                        ----> dim 0
\endverbatim
                                                                    */
           int multiple;           /**< the number of values at each point.   
                                    By default, it is set to 1.     */   
};
typedef struct UDM_DATA_STRUCT UDM_DATA_STRUCT;
 
/**
\ingroup array Functions for Writing/Reading Arrays

 FIO_dstruct_init sets all the pointers in UDM_DATA_STRUCT to null, and set
 multiple to 1.

 - ds: an input and output, an object of UDM_DATA_STRUCT.

*/

int FIO_dstruct_init(UDM_DATA_STRUCT *ds);

/**
\ingroup array Functions for Writing/Reading Arrays

  FIO_array_write writes a one- or multi-dimensional array under a group.
  This function is to replace the function FIO_write.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of an array.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - ds: an input, the structure of an array. A null ds indicates that 
   the most recent ds will used.

 - datatype: an input, the datatype of an array.

 - array: the starting address of an array.

 - array_id: an output, the id of an array.

 - This function writes an array into an udm file. If ds is not null, 
   the array will be interpreted according to ds, and the ds will bookkept as 
   the most recent ds. If ds is null, the most recent ds will used to
   interpreted the array.  
 
 - The dimension 0 is the one which changes most slowly, and the dimension
   (dims-1) is the one changing most fast.

 - If fsizes of ds is not set to null, it should be an array with 2 * dims
   elements. The first dims elements are the sizes of ghost points in
   the lower ends of dims dimensions, and the next dims elements are
   the sizes of ghost points in the higher ends of dims dimensions. The sizes of
   ghost points may be zero. The starting address, array, includes the
   ghost points.

 - Either fsizes is set to null on all pes, or fsizes is not null on
   any one of pes.

 - Even if fsizes is not null, offsets and sizes do NOT include
   ghost points. If there are no ghost points for ALL pes,
   fsizes may be set to null.

 - If ds->offsets is provided, it will be used as offsets
   of the data in the global configuration of the array.

 - If offsets is not provided, the function will calculate
   offsets according to psizes and plist. If psizes is not
   provided, it will be assumed that
   psizes[0] = npes, and psizes[i] = 1 for i != 0.
   plist is a list of pe ranks in the order of dims dimension.
\verbatim
           ----------------------------------------
    dim 1  |            |            |            |
           | plist(1)   | plist(3)   | plist(5)   |
      ^    |            |            |            |
      |    ----------------------------------------
      |    |            |            |            |
      |    | plist(0)   | plist(2)   | plist(4)   |
           |            |            |            |
           ----------------------------------------
                        ----> dim 0
\endverbatim
   If plist is not provided, a default plist will be assumed which is
   the layout in the order of dimensions (dims-1), (dims-2),...,0.

 - If plist is provided, its size is
   psizes[0] * psizes[1] * ... * [dims-1].

 - If offsets, or psizes, or plist, or fsizes is
   not provided in any one of pes, it must be set to null at all pes.

 - Although HDF5 tools, h5view and h5dump, may display the arrays written
   through this function, in general, these tools may not correctly
   interpret multi-dimensional arrays. The layout of a multi-dimensional
   array in an udm file, which is written through this function,
   is optimized for the best possible IO performance.

 - gsizes of ds is ignored in this function.

*/

int FIO_array_write(const int group_id,
              const char      *path_and_name,
              UDM_DATA_STRUCT *ds,
              UDM_DATA_TYPE   datatype,
              const void      *array,
              int             *array_id);

/**
\ingroup array Functions for Writing/Reading Arrays

 FIO_write writes a one- or multi-dimensional array under a group.
 This function has been replaced by FIO_array_write. 

 Examples 2-7 in Section 7.3 show the usage of FIO_dstruct_init and FIO_write. 
*/ 
/* 
 EExample 2: ctest_002.c
 EExample 3: ctest_003.c
 EExample 4: ctest_004.c
 EExample 5: ctest_005.c
 EExample 6: ctest_006.c
 EExample 7: ctest_007.c
*/
int FIO_write(const int       group_id,
              const char      *path_and_name,
              UDM_DATA_STRUCT ds,
              UDM_DATA_TYPE   datatype,
              const void      *array,
              int             *array_id);

/*
\ingroup array Functions for Writing/Reading Arrays

 FIO_rwrite writes a one- or multi-dimensional array under a group.
 FIO_rwrite does exactly the same thing as FIO_write
   except for the layout of a multi-dimensional array in an udm file.
   The layout of a multi-dimensional array written through FIO_rwrite
   is NOT optimized for its IO performance, but the array may be viewed
   through h5view and h5dump. The usage of FIO_rwrite is the same as
   taht of function FIO_write, but FIO_rwrite ignores ds.fsizes and
   ds.multiple.

 Examples 8,9 in Section 7.3 show the usage of FIO_rwrite.
*/
/* 
 EExample 8: ctest_008_1Darray_3cw.c
 EExample 9: ctest_009_2Darray_0aw.c
*/

int FIO_rwrite(const int group_id,
                 const char *path_and_name,
                 UDM_DATA_STRUCT ds,
                 UDM_DATA_TYPE datatype,
                 const void *array,
                 int *array_id);

/**
\ingroup array Functions for Writing/Reading Arrays

 FIO_array_create creates an id for an array which may written
 through a set of subsequent calls of FIO_array_write. This function
 is a collective call, ie, all the pes should call this function if any
 pe does.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of an array.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be the id of any 
   object in this file.

 - datatype: an input, the data type 0f the array.
 
 - linear_size: an input, the total size neeeded for the array in term of the 
   data type, datatype. The total size includes all the ghost points of all the 
   pes in all the subsequent calls of FIO_array_append.

 - array_id: an output, the id of the array.
*/
int FIO_array_create(const int group_id,
                         const char *path_and_name,
                         UDM_DATA_TYPE datatype,
                         const long long linear_size, 
                         int *array_id);
 
/**
\ingroup array Functions for Writing/Reading Arrays

 FIO_array_append writes a part of an array after the array was created
 through FIO_array_create.  

 - arrau_id: an input, the id of arrays, which should be returned through
   a previous call of FIO_array_create.

 - ds: an input, the structure of the part of the array.
   The following fields of ds must be given before the call,
   dims, offsets, sizes, multiple (if it is not 1), fsizes (if it is
   not null. The values of ds.sizes and ds.offsets exclude any possible ghost 
   points.

 - datatype: an input, the datatype of the array.

 - array: an input, the starting address of the part of the array.

 - ds.dims and ds.multiple should be the same as those used in the previous call
   of FIO_array_create.
 
 - ds.plist, ds.psizes and ds.gsizes are ignored in this function.

 - ds.fsizes should be null on all the pes, or not null on any one of pes.
   If fsizes of ds is not set to null, it should be an array with 2 * dims
   elements. The first dims elements are the sizes of ghost points in
   the lower ends of all dims dimensions of this part, and the next dims
   elements are the sizes of ghost points in the higher ends of all dimensions.
   The sizes of ghost points may be zero. The starting address, array,
   includes the ghost points.

 - ds.fsizes should be all null in all the calls of this function for the array
   if it is null in one call. Otherwise, ds.fsizes should be valid in all the
   call of this function for the array.
*/ 
int FIO_array_append(const int array_id,
                        UDM_DATA_STRUCT ds,
                        UDM_DATA_TYPE datatype,
                        const void *array);
/**
\ingroup array Functions for Writing/Reading Arrays

  FIO_array_close closes an array which was previously created through 
  FIO_array_create.

 - array_id: an input, the id of arrays, which should be returned through
   a previous call of FIO_array_create.

   The following example shows the usage of FIO_array_create, FIO_array_write
   and FIO_array_close.

 \include ctest_array_create.c
*/
int FIO_array_close(const int array_id);

/**
\ingroup array Functions for Writing/Reading Arrays

 FIO_get_ds_on_pe gets the data structure of an array on one of the original 
   pes.

 - group_id: an input, the id of a group. 

 - path_and_name: an input, the path and name of an array.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be the id of any 
   object in this file.

 - rank: an input, the rank of one of the pes which wrote the array.

 - ds: an output, the structure of the part of the array on the original pe.  
 
 - This function returns ds in which
   ds->psizes = NULL, ds->plist = NULL.

 - The output ds->sizes, ds->gsizes, ds->offsets are measured without
   ghost points even if ds->fsizes != NULL in output.

 - ds returned from this function may be used in FIO_read
   and FIO_fread.

 - Clean up after use, for example, 
\verbatim 
       free(ds->gsizes);
       if (ds->offsets) free(ds->offsets);
       if (ds->sizes)   free(ds->sizes);
       if (ds->fsizes)  free(ds->fsizes).
\endverbatim

*/
int FIO_get_ds_on_pe(const int group_id,
               const char *path_and_name, 
               const int rank,
               UDM_DATA_STRUCT *ds);

int FIO_get_ds(const int group_id,
               const char *path_and_name,
               UDM_DATA_STRUCT *ds);

/**
\ingroup array Functions for Writing/Reading Arrays

 FIO_fread reads a part of one- or multi-dimensional array.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of an array.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be the id of any 
   object in this file.
 
 - ds: an input, the structure of an array. The following fields of ds must be 
   given before the call, dims, sizes, offsets, fsizes (if ghost points are
   requested), and multiple (if not 1). 

 - datatype: an input, the datatype requested.

 - buffer: an input and an output, the starting address of an array
   to be filled. The memory will be filled with the values of the part
   of the array.

 - (ds.offsets[i] + ds.sizes[i]) can not be larger than the total size of the 
   array in the dimension i, excluding any possible ghost points.

 - ds.fsizes may be set to null if no ghost points have to be
   read. If ds.fsizes is NOT null, ds.fsizes should be 2 * ds.dims
   long with its values defined. The first ds.dims elements are
   the sizes of fake (ghost) points at the lower ends of ds.dims
   dimensions, and the next ds.dims elements are the sizes of ghost
   points at the higher ends of the dimensions.
   If the original array doesn't have ghost points
   stored beyond a full domain, it will be an error to read
   the array beyond the domain.

 - ds.sizes and ds.offsets are measured without ghost points even if
   ds.fsizes is NOT null.

 - gsizes, psizes and plist of ds are ignored in this function.

 - The argument, buffer, should be allocated before the call,
   for example,
\verbatim 
       int *buffer = (int *) malloc( mysize * sizeof(int));
       FIO_fread(..., buffer).
\endverbatim
   Here, mysize should be large enough to include the values in ds->fsizes
   if ds->fsizes is not null.

 Example 10, 11 in Section 7.4 show the usage of FIO_get_ds and FIO_fread.
*/
/* 
 EExample 10: ctest_010_1Dwrite_getds_fread.c 
 EExample 11: ctest_010_3Dwrite_getds_fread.c
*/ 
 
int FIO_fread(const int     group_id,
              const char      *path_and_name,
              UDM_DATA_STRUCT *ds,
              UDM_DATA_TYPE   datatype,
              void            *buffer);

/**
\ingroup array Functions for Writing/Reading Arrays

 FIO_read reads a part of one- or multi-dimensional array.


 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of an array.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - datatype: an input, the datatype requested.

 - ds: an input or output, the structure of an array.
   The following fields of ds must be
   given before the call, dims, sizes, offsets, fsizes (if ghost points are
   requested), and multiple (if not 1).

 - buffer: an input or output,
   the starting address of the part of the array.

 - sizes and/or offsets of ds should either be set to null for all pes 
   or be valid on each pe. 

 - If ds->sizes and/or ds->offsets are set to null before the call,
   the function returns ds of the array. If the array was written
   through FIO_rwrite, this function returns ds in which sizes[i]
   = gsizes[i], offsets[i] = 0, (i = 0, 1, ..., dims-1), and
   and fsizes = NULL. If the array was written through FIO_write
   and the number of pes calling this function is different
   from the number of pes used when the array was written, this function 
   returns ds of the first pe in which sizes[i] != 0. If the
   array was written through FIO_write, and the
   number of pes calling this function is the same as the
   number of pes used when the array was written, sizes, offsets
   and fsizes of ds returned will be the same as those in the original
   pe when the array was written.

-  If ds->sizes and/or ds->offsets are set to null before the call, 
   a cleanup is expected after the call, for example, 
\verbatim 
       free(ds->gsizes);
       if (ds->offsets) free(ds->offsets);
       if (ds->sizes)   free(ds->sizes);
       if (ds->fsizes)  free(ds->fsizes).
\endverbatim

 - ds.fsizes may be set to null if no ghost points have to be
   read. If ds.fsizes is NOT null, ds.fsizes should be 2 * ds.dims
   long with its values defined. The first ds.dims elements are
   the sizes of fake (ghost) points at the lower ends of all ds.dims
   dimensions, and the next ds.dims elements are the sizes at the higher 
   ends of the dimensions. If the original array doesn't have ghost points
   stored beyond a full domain, it will be an error to read
   array beyond the domain.

 - ds.sizes and ds.offsets are measured without ghost points even if
   ds.fsizes is NOT null.

 - gsizes, psizes and plist of ds are ignored in this function.

 - The argument, buffer, may be dynamically allocated before the call,
   for example, 
\verbatim 
       buffer = (int *) malloc( mysize * sizeof(int));
       FIO_read(..., &buffer).
\endverbatim
   Here, mysize should be large enough to include the values in ds->fsizes
   if ds->fsizes is not null.

 - The argument, buffer, may be statically allocated before the call,
   for example, 
\verbatim 
       int a[mysize];  int *buffer = a;
       FIO_read(..., &buffer).
\endverbatim

 - If buffer is not allocated before the call, set buffer to null before
   the call, for example, 
\verbatim 
       int *buffer = NULL;
       FIO_read(..., &buffer).
\endverbatim
   Clean up after use, for example, free(buffer).

 */

int FIO_read(const int       group_id,
             const char      *path_and_name,
             UDM_DATA_STRUCT *ds,
             UDM_DATA_TYPE   datatype,
             void            *buffer);


/**
\defgroup pool Functions for Writing/Reading Many Small Arrays 

 This section covers the functions for writing/reading many small arrays.  

*/

/**
\ingroup pool Functions for Writing/Reading Many Small Arrays

 FIO_create_dspool create a container for many small arrays.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of a container.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - narrays: an input, the number of arrays this container will contain.

 - dims: an input, the common dimension of these arrays. 

 - datatype: an input, the common datatype of these arrays.

 - linearsize: an input, the number of values in all these arrays on
   the current pe.

 - pool_id: an output, the id of this container.
 

 - path_and_name specifies the path and name of the container. 

 - All narrays arrays in this container must have the same dimensionality,
   and the same datatype.

 - If psizes and plist are not null, they will be used to calculate
   the offsets of each array on each pe. psizes and plist in this function
   have exactly the same definition as dstruct.psizes, dstruct.plist in
   function FIO_write, and default psizes and plist will be used when
   needed as in Function FIO_write..

*/

int FIO_create_dspool(const int group_id,
                const char *path_and_name,
                int   narrays,
                int   dims,
                long  long *psizes,
                long  long *plist,
                UDM_DATA_TYPE datatype,
                long long linearsize,
                int *pool_id);

#define FIO_FCREATE_DSPOOL myfio_fcreate_dspool_

void FIO_FCREATE_DSPOOL(const int *group_id,
                        const char *path,
                        int        *narrays,
                        int        *dims,
                        long long  *psizes,
                        long long  *plist,
                        int        *datatype,
                        long long  *linearsize,
                        int        *pool_id,
                        int        *ierr);

/**
\ingroup pool Functions for Writing/Reading Many Small Arrays

 FIO_store writes a small array into a container.

 - pool_id: an input, the id of a container.

 - name: an input, the name of a small array.

 - dims: an input, the dimensionality of the array, which must be the same
         as that of the container.

 - sizes: an input, an array with dims elements for the sizes of 
          the small array in the current pe.

 - offsets: an input, an array with dims elements for the offsets of
            the part of the small array in the current pe.

 - datatype: an input, the datatype of the array.

 - array: an input, the starting address of the part of the array.
 
 - offsets may either be null for all pes or have valid values for all pes.
   If offsets is null, psizes and plist of the container will be used
   to calculate the offsets.

*/

int FIO_store(int pool_id,
                const char *name, int dims,
                long long *sizes, long long *offsets,
                UDM_DATA_TYPE datatype,
                const void *array);

#define FIO_FSTORE myfio_fstore_

void FIO_FSTORE(int        *pool_id,
                  const char *name,
                  int        *dims,
                  long long  *sizes,
                  long long  *offsets,
                  int        *datatype,
                  const void *array,
                  int        *ierr);

/**
\ingroup pool Functions for Writing/Reading Many Small Arrays

 FIO_close_dspool closes the container.

 - pool_id: an input, the id of a container.

*/

int FIO_close_dspool(int pool_id);

#define FIO_FCLOSE_DSPOOL myfio_fclose_dspool_

void FIO_FCLOSE_DSPOOL(int *pool_id, int *ierr);

/**
\ingroup pool Functions for Writing/Reading Many Small Arrays

 FIO_open_dspool opens a container which was created and closed before.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of a container.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - pool_id: an output, the id of a container.

*/

int FIO_open_dspool(const int group_id,
                      const char *path_and_name,
                      int *pool_id);

#define FIO_FOPEN_DSPOOL myfio_fopen_dspool_

void FIO_FOPEN_DSPOOL(const int *group_id, const char *path_and_name,
                        int *pool_id, int *ierr);

/**
\ingroup pool Functions for Writing/Reading Many Small Arrays

 FIO_get_pooldataset gets a part of array which is stored in a container.
 Currently, this function can read only the part of data on the original pe. 

 - pool_id: an input, the id of a container.

 - name: an input, the name of the array.

 - datatype: an input, the datatype requested.

 - dims: an input, the dimension of the array.

 - offsets: an input, an array with dims elements for the offsets of
            the part of the array.

 - sizes: an input, an array with dims elements for the sizes of
          the part of the array.

 - buffer: an input, the starting address of the part of the array.

 - If offsets and/or sizes are null, this function reads the part of
   the array in the original pe if the number of pes calling 
   this function is the same as the number of pes used when the array
   was written, and read the part of array on the first pe 
   if the numbers of pes are different. Currently, this function can
   deal with the case with null offsets and/or sizes.

 - datatype may be different from what is stored. 

 Example 12 in Section 7.5 shows the usage of the functions for writing/reading
 arrays in a container.
*/
/* 
 EExample 12: ctest_011_pool.c
*/

int FIO_get_pooldataset(const int pool_id,
                          const char *name,
                          UDM_DATA_TYPE datatype,
                          int  dims,
                          long long *offsets,
                          long long *sizes,
                          void *buffer);

#define FIO_FGET_POOLDATASET myfio_fget_pooldataset_

void FIO_FGET_POOLDATASET(const int *pool_id, const char *name,
                            int *datatype, int *dims,
                            long long *offsets, long long *sizes,
                            void *buffr, int *ierr);

/**
\defgroup array_list Functions for Listing Groups and Arrays

This section covers the functions of listing groups and arrays.

*/

/**
\ingroup array_list Functions for Listing Groups and Arrays

 UDM_OBJECT_TYPE contains the values of object_type.

*/

enum UDM_OBJECT_TYPE { 
     /** - attribute   */
      udm_attribute,
     /** - array       */
      udm_dataset,
     /** - udm_group   */
      udm_group
};
typedef enum UDM_OBJECT_TYPE UDM_OBJECT_TYPE;
 
/**
\ingroup array_list Functions for Listing Groups and Arrays

  UDM_LIST_STRUCT defines the structure the functions
  FIO_list, FIO_fget_all_obj and FIO_get_all_obj return.

*/

struct UDM_LIST_STRUCT {
       /** - the name of an array or a group, which does not include path  */
       char *name;              
       /** - udm_dataset or udm_group,  udm_attribute is not supported yet */
       UDM_OBJECT_TYPE objtype; 
       /** - valid only when objtype is udm_dataset */
       UDM_DATA_STRUCT dstruct; 
       /** - the id of an object  */
       int objID;              
       /** - valid only when objtype is udm_dataset */
       UDM_DATA_TYPE datatype; 
      };
typedef struct UDM_LIST_STRUCT UDM_LIST_STRUCT;

/**
\ingroup array_list Functions for Listing Groups and Arrays

 FIO_list_nitems gives the number of groups and arrays under a given group.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of a group.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - filter: an input, a name to be matched with.

 - nitems: an output, the number of objects found.
 
 - If filter is null, this function gives the number of arrays
   and sub-groups in a group. If filter is not null, 
   the number of arrays and sub-groups includes
   only those with their names matched with filter.

 - The arrays stored in a pool container are not included in the output.

 - The output doesn't include any grandchildren of the group.

 */

int FIO_list_nitems(const int  group_id,
                    const char *path_and_name,
                    const char *filter,
                    int        *nitems);

/**
\ingroup array_list Functions for Listing Groups and Arrays

 FIO_list gives the information of the groups and arrays under a given group.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of a group.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be 
   the id of any object
   in this file.

 - filter: an input, a name to be matched with.

 - nitems: an input or output, the number of arrays and sub-groups under a 
   group.

 - items:  an input or output, an array with nitems elements.   

 - If filter is null, this function gives the number of arrays 
   and sub-groups in a group, and the information of each child. If
   filter is not null, this function gives the information of only those
   children with their names matched with filter. 

 - If items[i].objtype is udm_group, the dstruct and datatype of items[i]
   are ignored.

 - If iterms[i].objtype is udm_dataset, the following fields of dstruct will
   be available after the call, dims, sizes, offsets, gsizes, fsizes and
   multiple.

 - For the case with items[i].objtype == udm_dataset,
   the fields of returned dstruct of
   items[i] have the following values.
   If the array was written through FIO_rwrite, 
   sizes[j] = gsizes[j], offsets[j] = 0,
   (j = 0, 1, ..., dims-1), and fsizes = NULL.
   If the array was written through FIO_write and the number of
   pes calling this function is different from the number
   of pes used when the array was written, dstruct has the values of the
   first pe in which sizes[j] is not zero for all j.
   If the array was written through FIO_write, and the number of
   pes calling this function is the same as the number
   of pes used when the array was written,
   sizes, offsets, fsizes will be the same
   as those in the original pe.

 - For the case with items[i].objtype == udm_dataset, sizes, offsets and
   gsizes of items[i].dstruct do not include any possible fake (ghost) points.

 - psizes and plist of items[i].dstruct are all set to null in the return.

 - If nitems is known, items may be allocated before the call.
   If items is not allocated before the call, it must be set to null, 
   for example, 
\verbatim
       UDM_LIST_STRUCT *items = NULL;
       FIO_list(group_id, path, &nitems, &items).
\endverbatim

 - After use, a clean-up is needed, for example, 
\verbatim
       for (i = 0; i < nitems; i++)
         { free(items[i].name);
           if (items[i].objtype == udm_dataset)
              { free(items[i].dstruct.gsizes);
                if (items[i].dstruct.offsets)free(items[i].dstruct.offsets);
                if (items[i].dstruct.sizes)  free(items[i].dstruct.sizes);
                if (items[i].dstruct.fsizes) free(items[i].dstruct.fsizes);
               }
          }
       if (nitems) free(items).
\endverbatim

 Example 13 in Section 7.6 shows the usage of FIO_list_nitems and FIO_list.
*/
/* 
 EExample 13: ctest_012_list.c  
*/

int FIO_list(const int       group_id,
             const char      *path_and_name,
             const char      *filter,
             int             *nitems,
             UDM_LIST_STRUCT **items);

/**
\defgroup smesh Functions for Structured Meshes and Variables

 This section covers the functions for structured meshes and variables.
 In this section, a grid point is a corner of a mesh element, 
 but a coordinate point of a mesh refers to a point defined by
 three coordinates in a three-dimensional (3D) mesh, 
 by two coordinates in a two-dimensional (2D) mesh, and by one
 coordinate in an one-dimensional (1D) mesh. An element of a mesh
 refers to a numerical cell in
 simulations, and an element is a three-dimensional entity in a 3D mesh, a
 two-dimensional entity in a 2D mesh, and one-dimensional entity in an 1D mesh.
 A coordinate point may be a corner of an element, and also may be the center 
 of an element.

*/

/**
\ingroup smesh Functions for Structured Meshes and Variables 
 
 UDM_MESH_OBJECT_TYPE defines the possible values for mesh object type.

*/

enum UDM_MESH_OBJECT_TYPE { 
     /** - unstructured mesh */ 
     udm_unstructured_mesh,
     /** - variable dedined on unstructured mesh */
     udm_unstructured_meshvar,
     /** - structured mesh */ 
     udm_structured_mesh,
     /** - variable dedined on structured mesh */
     udm_structured_meshvar,
     /** - invalid mesh object */ 
     udm_mesh_object_invalid = 100
};
typedef enum UDM_MESH_OBJECT_TYPE UDM_MESH_OBJECT_TYPE;
 
/**
\ingroup smesh Functions for Structured Meshes and Variables 

 UDM_STRUCTURED_MESH defines the possible definitions of a structured mesh.

*/

struct UDM_STRUCTURED_MESH {
       int  dims;           /**< the dimensionality of a structured mesh */ 
       long long sizes[3];  /**< sizes of mesh elements in dims dimension (NOT
                                 sizes of grid points). Dimension 0 is the
                                 dimension changing most slowly. The field does 
                                 not include any possible ghost mesh elements.*/   

       long long offsets[3];/**< offsets of mesh elements in dims dimension for
                                 the current pe, which don't include
                                 any possible ghost mesh elements.            */
 
       long long gsizes[3]; /**< total sizes of mesh elements in dims 
                                 dimensions, excluding 
                                 any possible ghost mesh elements.            */

       long long psizes[3]; /**< sizes of pe-configuration in dims dimensions.
                                 This field
                                 will be used when offsets are not provided
                                 in writing a mesh.  By default, 
                                 psizes[0] = npes, psizes[i] = 1 for i != 0.
                                                                         */
       long long *plist;    /**< This is an array to indicate the order of pes
                                 when offsets are not
                                 provided for writing a mesh.
\verbatim
           ----------------------------------------
    dim 1  |            |            |            |
           | plist(1)   | plist(3)   | plist(5)   |
      ^    |            |            |            |
      |    ----------------------------------------
      |    |            |            |            |
      |    | plist(0)   | plist(2)   | plist(4)   |
           |            |            |            |
           ----------------------------------------
                        ----> dim 0
\endverbatim
                                                                              */ 

       long long nbdyl[3];  /**< the number of ghost mesh elements at the lower
                                 ends of dims dimensions.                    */ 
       long long nbdyr[3];  /**< the number of ghost mesh elements at the
                                 higher ends of dims dimensions.        */

/*  for coordinate points being element centered or not  */

       int element_centered;/**< It should be 1 if a coordinate point,
                                 for example in a 2D mesh, 
                                 (coord[0][k],coord[1][j]) is
                                 a center of a mesh element, and it should be
                                 0 if (coord[0][k],coord[1][j])
                                 is a grid point.               */  

/* for a set of uniform grids at any dimension  */

       double dcoord[3];    /**< If dcoord[i] > 0 (i = 0, 1, 2), dimension
                                 i will be considered uniform in dimension i
                                 in the current pe.                      */

       double coordmin[3];  /**< coordmin[i] (i = 0, 1, 2) is the minimum
                                 value of coordinates of real
                                 mesh elements in dimension i.
                                 coordmin[i] will be used for the following
                                 two situations: (a) for a uniform mesh, ie,
                                 dcoord[i] > 0, (b) element_centered = 1.
                                                                          */
/* for a set of nonuniform grids at any dimension  */

       void *coord[3];      /**< coord[i] (i = 0, 1, 2) should be valid
                                 if dcoord[i] < 0. If element_centered = 0,
                                 the array coord[i] is
                                 (sizes[i] + nbdyl[i] + nbdyr[i] + 1)
                                 long, otherwise it is 
                                 (sizes[i] + nbdyl[i] + nbdyr[i]) long.  */

       UDM_DATA_TYPE datatype; /**<  datatype of coord[i], i = 0, 1, 2.
                                  Currently, only udm_float, udm_double,
                                  and udm_long_double are supported.     */
     };
typedef struct UDM_STRUCTURED_MESH UDM_STRUCTURED_MESH;

/**
\ingroup smesh Functions for Structured Meshes and Variables 
 
 UDM_MESH_VAR_TYPE defines the possible types of variables defined on
 either structured meshes or unstructured meshes.

*/

enum UDM_MESH_VAR_TYPE { 
     /** - variables defined on 3D zones */ 
     udm_zone,
     /** - variables defined on faces, including faces in a 2D or 3D space */
     udm_face,
     /** - variables defined on edges, including edges in an 1D, or 2D, or 3D space */
     udm_edge,
     /** - variables defined on nodes    */
     udm_node,
     /** - invalid mesh variable type    */
     udm_meshvartype_invalid  = 100
};
typedef enum UDM_MESH_VAR_TYPE UDM_MESH_VAR_TYPE;

/**
\ingroup smesh Functions for Structured Meshes and Variables 
 
 UDM_MESH_VAR defines the structure of a variable associated with either
 a structured mesh or an unstructured mesh. 

*/

struct UDM_MESH_VAR {
       UDM_MESH_VAR_TYPE type; /**< zone, face, edge or node variable */  

       int rank;           /**< rank = 0 for a scalar; rank = 1
                                for a vector; rank > 1 for a tensor. */ 
	
       int comp_sizes[COMP_RANK]; /**< For a scalar, this field is ignored.
                                For a vector, sizes[0] is the number of
                                components of the vector.
                                For a tensor, sizes[0], sizes[1], ...,
                                sizes[rank-1] are the number of
                                components for each index. For example,
                                for a tensor Tij (i = 0, 1, 2; 
                                j = 0, 1, 2), rank = 2, sizes[0] = 3;
                                sizes[1] = 3.                        */

       int components[COMP_RANK]; /**< This field is used to indicate a specific
                                     component of a vector or tensor.
                                     The values of the array is
                                     zero-based. 
                                     For a scalar this field is ignored.
                                     For a vector, components[0] is used
                                     to specify a component. For tensors,
                                     for example, 
                                     (components[0], components[1]) =
                                     (0,1) refers to the component 
                                     T_01 of a tensor T_ij.          */

       void *buffer;           /**< It is the starting address of the values 
                                    for a variable, or a component of a 
                                    variable.
                                    The size of buffer should be consistent
                                    with the associated mesh and the type of
                                    the variable. For example, 
                                    for an element variable on a structured 
                                    mesh, a valid size should be product of 
                                    (sizes[i] + nbdyl[i] + nbdyr[i])
                                    (i = 0, 1, 2) of the associated 3D mesh. 
                                    For a node variable, a valid size should 
                                    be the product of 
                                    (sizes[i] + nbdyl[i] + nbdyr[i] + 1) 
                                    (i = 0, 1, 2) of the associated 3D mesh if
                                    element_centered of the mesh is 0, and 
                                    should be the product of 
                                    (sizes[i] + nbdyl[i] + nbdyr[i])
                                    of the associated 3D mesh if 
                                    element_centered of he mesh is 1.  */

       UDM_DATA_TYPE datatype; /**< datatype of buffer            */

/*       int size_matched_with_elem;  This field is Ignored for 
                                        unstructured mesh variables,
                                        and it is ignored for structured
                                        mesh variables which are defined
                                        on (2D or 3D) elements. This 
                                        field will be used for structured
                                        node-variables, face-variables in
                                        3D meshes, and edge-variables in 
                                        2D meshes. It is 1 if the size
                                        of buffer match with sizes, nbdyl
                                        and nbdyr of the associated mesh. 
                                        It is 0 if the size of buffer in
                                        dimension i is (sizes[i] +
                                        nbdyl[i] + nbdyr[i] + 1) of the 
                                        associated mesh. The case with
                                        size_matched_with_elem = 1
                                        happens when the coordinate of the 
                                        associated mesh is the corner of
                                        a mesh element.              */

       int  compressed; /**< This field is not implemented yet, intent to 
                             cover the case with buffer[0] being all values
                             ont the domain of this pe.         */
      };
typedef struct UDM_MESH_VAR UDM_MESH_VAR;

/**
\ingroup smesh Functions for Structured Meshes and Variables

 FIO_smesh_init initializes all the members of a structured mesh to
 indicate that these members are undefined.

 - mesh: an input and output. Through the call, sizes, offsets, gsizes,
   nbdyl, nbdyr, dcoord, coordmin, coord, psizes, plist and datatype of mesh
   are set to the following, 
\verbatim
   sizes = (0, 0, 0)
   offsets = (-1, -1, -1)
   gsizes = (0, 0, 0)
   nbdyl = (0, 0, 0)
   nbdyr = (0, 0, 0)
   dcoord = (-1, -1, -1)
   coordmin = (0, 0, 0)
   coord = (NULL, NULL, NULL)
   psizes = (npes, 1, 1)
   plist = NULL
   datatype = udm_datatype_invalid
\endverbatim

*/

int FIO_smesh_init(UDM_STRUCTURED_MESH *mesh);

/**
\ingroup smesh Functions for Structured Meshes and Variables

 FIO_smesh_write writes a structured mesh.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of a structured mesh .
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be 
   the id of any object
   in this file.

 - mesh: an input, the mesh to be written. The following fields of mesh
   must be given before the call, dims, sizes. 

 - If some pe specifies one of these fields of mesh, offsets, nbdyl, nbdyr,
   psizes, plist, all the pes have to specify the field. If a mesh doesn't
   involve any one of these fields, the field doesn't have to to touched after
   FIO_smesh_init.

 - All the pes must have the same value for the fields, element_centered
   and datatype. 

 - The field of mesh, sizes, is an array for the sizes of mesh elements (NOT
   grid points) in dims dimensions in the current pe,
   offsets is an array for offsets of mesh elements in the
   dimensions. Both sizes and offsets exclude any possible
   ghost mesh elements. The field, nbdyl, is an array for the sizes of ghost
   mesh elements at the lower ends of dims dimensions, and 
   nbdyr is an array for the sizes of ghost mesh elements
   at the higher ends
   of the dimensions.

 - mesh.nbdyl and mesh.nbdyr don't have to be set if there are no
   ghost elements.

 - If mesh.offsets is given, mesh.psizes and mesh.plist are ignored.

 - If mesh.offsets is not given, psizes and plist will be used to
   calculate offsets. psizes is for the 
   sizes of pes in a pe-configuration.
   plist is a list of pe ranks in the
   order of dims dimensions.
\verbatim
           ----------------------------------------
    dim 1  |            |            |            |  
           | plist(1)   | plist(3)   | plist(5)   |
      ^    |            |            |            |   
      |    ----------------------------------------   
      |    |            |            |            |  
      |    | plist(0)   | plist(2)   | plist(4)   | 
           |            |            |            |
           ----------------------------------------
                        ----> dim 0 

\endverbatim
   If psizes and/or plist
   are not given, default psizes and/or plist will be used. The 
   default psizes is psizes[0] = the number of pes and
   psizes[i] = 1; i = 1, 2. The default plist is 
   plist[i] = i, i = 0, 1, ... 

 - On each pe, a mesh may be either uniform or nonuniform in any
   dimension. But, coord and/or (dcoord, coordmin) on all pes should
   constitutes a structured mesh. 

 - mesh.coordmin[i] is the minimum coordinate
   of mesh grid points (NOT coordinate points) in the current pe 
   excluding ghost elements. 
   There are two situations in whcih coordmin is expected to be provided,
   (a) a uniform part of mesh without coordinates provided, (b) a mesh with
   element_centered = 1.

 - In dimension i of a pe, a uniform part of a mesh is described by 
   mesh.dcoord[i] and mesh.coordmin[i]. A nonuniform coordinate in 
   dimension i is described by the array mesh.coord[i] which includes
   ghost elements. If valid dcoord[i] is provided, dcoord[i] and coordmin[i]
   will be used as coordinates in dimension i. Otherwise, coord[i] will be
   used.

 - If mesh.element_centered = 1, mesh.coord[i] is a center of a mesh
   element for a nonuniform mesh. In this case, the
   length of the array mesh.coord[i] is expected to be equal to
   mesh.sizes[i] + nbdyl[i] + nndyr[i].
   If mesh.element_centered = 0, mesh.coord[i] is a grid point of an
   element. In this case, mesh.coord[i]
   is assuned to be (sizes[i] + nbdyl[i] + nbdyr[i] + 1) long if
   mesh.coord[i] is defined.

  Examples 14-19 in Section 7.7 show the usage of FIO_smesh_write.
*/
/* 
 EExamples 14: ctest_016a_smesh_2Dnonunif_w.c
 EExamples 15: ctest_016c_smesh_2Dunif_w.c
 EExamples 16: ctest_017a_smesh_1w_nonuniform.c 
 EExamples 17: ctest_017a_smesh_2w_nonuniform.c 
 EExamples 18: ctest_018_smesh_3w_uniform.c 
 EExamples 19: ctest_018b_w_smesh2D_uniform.c
*/

int FIO_smesh_write(const int           group_id,
                    const char          *path_and_name,
                    UDM_STRUCTURED_MESH mesh,
                    int                 *mesh_id);

/**
\ingroup smesh Functions for Structured Meshes and Variables

 FIO_smeshvar_write writes a number of variables associated with a
 structured mesh.

 - group_id: an input, the id of a group.

 - path_and_names: an input, an array of paths and names
   for nvars variables.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of an
   element of path_and_names  
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in one of path_and_names, such as /grp1/grp2/mygrp, 
   the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - mesh_id: an input, the id of a structured mesh.

 - nvars: an input, the number of variables to be written.

 - vars: an input, an array with nvars elements for nvars variables.

 - var_ids: an output, an arrays with nvars elements for ids of the
   nvars variables.

 - Each component
   of a vector or tensor is considered one variable in the input.
   All components of
   a vector or tensor should have same path, name, type, rank, comp_sizes,

 - The size of an element variable is supposed to match with sizes, nbdyl,
   nbdyr, and element_centered of the associated mesh, and the type
   of variable.

 - The sizes of node variables are supposed to mach mesh "coordinate points" 
   of the associated mesh.  
   If coordinate points are centers of mesh elements, the sizes of
   a node variable
   are (sizes[i] + nbdyl[i] + nbdyr[i]) (i = 0,1,2). If the coordinate 
   points are corners of mesh elements, the sizes of node variables
   are (sizes[i] + nbdyl[i] + nbdyr[i] + 1) (i = 0,1,2) of the
   associated mesh.

 - Edge-variables in a 3D mesh are not supported.
   For face-variables in ia 3D mesh or edge-variables in a 2D mesh,
   only vectors are supported. 

   Examples 20,21 in Section 7.8 show the usage of FIO_smeshvar_write.
*/
/* 
  EExample 20: ctest_019a_smeshvar_1aw.c
  EExample 21: ctest_020_smeshvar_2w.c

*/

int FIO_smeshvar_write(const int    group_id,
                       char         *path_and_names[],
                       const int    mesh_id,
                       int          nvars,
                       UDM_MESH_VAR vars[],
                       int          var_ids[]);
 
/**
\ingroup smesh Functions for Structured Meshes and Variables

 FIO_get_smeshsize_on_pe gets the information about the sizes of a
 structured mesh on an original pe.

 - mesh_id: an input, the id of a structured mesh.

 - pe_rank:  an input, one of of the original pes which wrote the mesh.

 - mesh: an output, the structured mesh, the following fields of mesh
   will be available after the call,
   dims, sizes, offsets, nbdyl, nbdyr, coordmin, dcoord,

 Example 22 in Section 7.9 show the usage of FIO_get_smeshsize_on_pe.
*/
/*
 EExample 22: ctest_018b_smeshsize_on_pe.c
*/

int FIO_get_smeshsize_on_pe(const int mesh_id, const int per_ank,
                              UDM_STRUCTURED_MESH *mesh);

/**
\ingroup smesh Functions for Structured Meshes and Variables

 FIO_get_smesh reads any part of a structured mesh.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of a structured mesh.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be 
   the id of any object
   in this file.

 - mesh: input and output for a structured mesh.
         The following fields of mesh must be given before the call,
         dims, sizes, offsets, nbdyl, nbdyr, datatype.
         This function will fill the values for the fields, coord[i], 
         and/or dcoordi, coordmin. 

 - mesh_id: an output, the id of the mesh for future reference.

 - Given dims, datatype, sizes,
   offsets, nbdyl, nbdyr of mesh, this function gives
   (dcoord[i], coordmin[i]) (i = 0,1,2) if dimension i is
   uniform in the part, or coord[i] (i = 0,1,2) if the part is nonuniform. In
   the second case, dcoord[i] <= 0 in the output. If this function gives 
   dcoord[i] and coordmin[i], coordmin[i] is the starting grid point for real
   elements excluding ghost elements. If this function gives coord[i]
   coord[i][0] is the starting coordinate point including
   any possible ghost mesh elements.

 - If the original mesh doesn't have ghost elements, but nbdyl
   and/or nbdyr are not vanishing, this function will gives
   coordinates as long as (offsets[i] - nbdyl[i]) >= 0 and
   (offsets[i] + sizes[i] + nbdyr[i]) is not greater than the total
   sizes of the mesh.

 - If the requested ghost elements are beyond the domain of the original mesh
   including original ghost elements, there will be an error.

 - The requested range (offsets[i], offsets[i]+sizes[i]) of real
   elements is supposed to be in the domain of the original mesh excluding
   ghost elements.

 - If the part of mesh requested is uniform in
   dimension i, mesh->coord[i] will not be touched. In this case, 
   mesh->coordmin[i] and mesh->dcoord[i] have been assigned valid
   values. If the part of mesh
   requested is nonuniform in dimension i, mesh->coord[i] should be
   allocated before the call, and mesh->coordmin[i] and mesh->dcoord[i]
   will not be touched.

 Examples 23,24 in Section 7.9 show the usage of FIO_get_smesh.
*/
/*
 EExample 23: ctest_017b_smesh_2r_nonuniform.c
 EExample 24: ctest_018b_smesh_4r_uniform.c 

*/

int FIO_get_smesh(const int           group_id,
                  const char          *path_and_name,
                  UDM_STRUCTURED_MESH *mesh,
                  int *mesh_id);

/**
\ingroup smesh Functions for Structured Meshes and Variables

 FIO_fread_smeshvar reads a set of variables associated with a given
 structured mesh.

 - group_id: an input, the id of a group.

 - path_and_names: an input, an array of paths and names
   for nvars variables.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of an
   element of path_and_names  
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in one of path_and_names, such as /grp1/grp2/mygrp, 
   the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - nvars: an input, the number of variables requested.

 - mesh_id: an input, the id of the associated mesh.

 - sizes: an input, the sizes of mesh elements for the  part of mesh
          on which these variables are defined. 

 - offsets: an input, the offsets of the part of mesh elements on which
            these variables are defined.

 - nbdyl: an input, the sizes of ghost mesh elements at the lower ends 
          of the part of mesh on which these variables are defined.

 - nbdyr: an input, the sizes of ghost mesh elements at the higher ends 
          of the part of mesh on which these variables are defined.

 - vars: an input and output, an array for variables.
         The following fields of UDM_MESH_VAR must be given before the call,
         type, rank, comp_sizes (if rank > 0), components (if rank > 0).
         The field, buffer, of each element of vars will be filled with
         the values of a variable or a component of a variable.

 - This function will read nvars variables associated with one mesh.
   Each component of a vector or tensor is considered a variable in the
   input. All components of a same vector or tensor should have same
   path_and_name.

 - This function reads only those values of the variables, which
   are defined on the region specified by offsets, sizes, nbdyl and
   nbdyr of the associated mesh.
   Some of these nvars variables may be node variables, while others
   may be element, or face, or edge  variables.

 - The field, buffer, of each element of vars should be allocated before the 
   call, and these buffers are not necessarily have the same length.
   For example, element variables have the length
   (sizes[i] + nbdyl[i] + nbdyr[i]) in dimension i, while node
   variables have the length
   (sizes[i] + nbdyl[i] + nbdyr[i] + 1) if coordinate points of the
   associated mesh is not centers of mesh elements.

 - nbdyl and nbdyr may be set to null if no ghost elements are
   requested.

 - The range between offsets[i] and (offsets[i]+sizes[i]) of real
   elements is supposed to be in the domain of the original mesh
   excluding any original ghost elements.

 Examples 25-29 in Section 7.10 show the usage of FIO_fread_smeshvar 
*/
/*  
 EExample 25: ctest_016b_smesh_2Dnonunif_r.c
 EExample 26: ctest_016d_smesh_2Dunif_r.c
 EExample 27: ctest_018c_r_smesh2D_uniform.c
 EExample 28: ctest_023_smeshvar_1br.c
 EExample 29: ctest_024_smeshvar_1cr.c
*/
 
int FIO_fread_smeshvar(const int    group_id,
                       char         **path_and_names,
                       const int    nvars,
                       const int    mesh_id,
                       long long    *sizes, long long *offsets,
                       long long    *nbdyl, long long *nbdyr,
                       UDM_MESH_VAR vars[]);

/**
\ingroup smesh Functions for Structured Meshes and Variables

 Given the id of a variable, FIO_get_smesh_info gives the information of
 the associated structured mesh.

 - var_id: an input, the id of a structured mesh variable.

 - mesh_id: an output, the id of the associated structured mesh.

 - mesh: an output, the associated structured mesh.
         The following fields of mesh will be available after the call,
         dims, sizes, offsets, gsizes, nbdyl, nbdyr, dcoord, coordmin, 
         element_centered and datatype.

 - If the mesh is globally uniform in dimension i, this function also
   gives a valid dcoord[i] and coordmin[i]. Otherwise,
   dcoord[i] < 0 in the output.

 - If the number of pes calling this function is the same as
   the number of pes 
   used in writing the mesh, sizes, offsets, nbdyl and nbdyr of
   mesh will be those on the original pe. If the numbers of
   pes are different,
   sizes, offsets, nbdyl and nbdyr will be the first of original
   pes, on which there were mesh elements.

 Example 30 in Section 7.11 shows the usage of FIO_get_smesh_info.
*/
/* 
 EExample 30: ctest_025_smesh_get_meshinfo.c
*/

int FIO_get_smesh_info(const int var_id,
                       int       *mesh_id, 
                       UDM_STRUCTURED_MESH *mesh);

/**
\ingroup smesh Functions for Structured Meshes and Variables

 FIO_get_nvars gives the number of variables associated with a given
 structured mesh.

 - mesh_id: an input, the id of a structured mesh.

 - nvars: an output, the number of variables defined on the mesh.
          A vector or tensor is considered one variable in the output.
*/

int FIO_get_nvars(const int mesh_id, int *nvars);

/**
\ingroup smesh Functions for Structured Meshes and Variables

 Given the id of a structured mesh, FIO_get_var_info will give the
 information of all the variables associated with the mesh under a
 given group.

 - group_id: an input, the id of a group.

 - mesh_id: an input, the id of a structured mesh.

 - nvars: an input or an output, the number of variables associated
          with the mesh.

 - path_and_names: an input, an array of paths and names
   for nvars variables.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of an
   element of path_and_names  
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in one of path_and_names, such as /grp1/grp2/mygrp, 
   the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - var_ids: an output, an arrays with nvars element for the ids of variables.

 - vars: an output, an arrays with nvars elements for nvars variables.
 
 - If any one of path_and_names, var_ids and vars is null before the call, 
   nvars is an output, and this function
   allocates path_and_names, var_ids and vars.
   In this case, path_and_name, var_ids and vars should be cleaned up after use,
   for example,
\verbatim
       char **paths = NULL;
       int  *var_ids = NULL;
       UDM_MESH_VAR *vars = NULL;
       int nvars;
       FIO_get_var_info(grpid, mesh_id, &nvars, &paths, &var_ids, &vars).
       for (i = 0; i < nvars; i++)
         { free(paths[i]);
          }.
       if (nvars)
          { free(paths);
            free(var_ids);
            free(vars);
           }. 
\endverbatim

 - path_and_names, var_ids and vars may be allocated before the call, for example,
\verbatim
       char **paths = (char **)malloc(nvars * sizeof(char *));
       int  *var_ids =  (int *)malloc(nvars * sizeof(int));
       UDM_MESH_VAR *vars = (UDM_MESH_VAR *)
                             malloc(nvars * sizeof(UDM_MESH_VAR));
       FIO_get_var_info(grpid, mesh_id, &nvars, &paths, &var_ids, &vars). 
       And also
\endverbatim

 - This function will not get values of any variables.

 Example 31 in Section 7.11 shows the usage of FIO_get_nvars and FIO_get_var_info.
*/
/*
 EExample 31: ctest_026_smesh_get_varinfo.c
*/

int FIO_get_var_info(const int group_id,
                      const int mesh_id, int *nvars,
                      char ***path_and_names,
                      int  **var_ids,
                      UDM_MESH_VAR **vars);

/**
\defgroup unsmesh Functions for Unstructured Meshes and Variables
 
 This section covers the functions for unstructured meshes and variables.
 In this section, A "zone" always refers to a 3D entity which has a volume,
 A "face" refers to an entity which is on a plane. Therefore, a face has an 
 area but doesn't have a volume. An "edge" refers to an entity which is on 
 a liner. An edge has a length, but has neither a volume nor an area. A 
 "node" is an entity with a point. Although there are no concept of mesh 
 element in the definition of unstructured mesh, "mesh element" is frequently 
 referenced in the following document for unstructured meshes. If a mesh is 
 made from zones, each zone is called a mesh element even through a zone may 
 be made from faces, edges and nodes. If a mesh is made from faces, but not
 zones, a face is called a mesh element. If a mesh is made from edges, but not 
 zones and faces, an edge is called a mesh element. 
 A mesh made from faces may be in both a two- or three-dimensional space,
 a mesh made from edges may be in a one-, or two-, or three-dimensional
 space. The words "zone-mesh", "face-mesh" and "edge-mesh" "
 are sometimes used in this document. A " zone-mesh" refers to a mesh with its
 mesh elements being zones, a "face-mesh" refers to a mesh with its mesh
 elements being faces, and an "edge-mesh" refers to a mesh with its mesh
 elements being edges. Therefore, for an edge-mesh, zones and faces
 will not be involved, but may involve two or three coordinate arrays;
 for a face-mesh, zones will not be involved, but may involve three coordinates
 arrays; and for a zone-mesh, zones, faces, edges, and nodes may be all
 involved.     

*/

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 UDM_UNSTRUCTURED_MESH_TYPE defines the types of unstructured meshes. 

*/

enum UDM_UNSTRUCTURED_MESH_TYPE {
                    /** - points, not implemented yet  */
                    udm_point    = 0,
                    /** - bars with two nodes  */
                    udm_bar      = 1,
                    /** - triangles with three nodes */
                    udm_triangle = 2,
                    /** - quadrangles with four nodes */
                    udm_quad     = 3,
                    /** - pentagon with five nodes    */
                    udm_pentagon = 10, 
                    /** - tetrahedron with four nodes */
                    udm_tet      = 4,
                    /** - pyramids with five nodes       */
                    udm_pyramid  = 5,
                    /** - hexahedron with eight nodes */ 
                    udm_hex      = 6,
                    /** - wedges with six nodes       */ 
                    udm_wedge    = 7,
                    /** - pentagon prism with 10 nodes   */ 
                    udm_pentagon_prism = 11,           
                    /** - mixture of others, not implemented yet for writing*/ 
                    udm_mixed_elements = 8, 
                    /** - general polyhedrons  */
                    udm_general_mesh = 9,
                    /** - invalid unstructured mesh type */
                    udm_meshtype_invalid = 100 
                   };
typedef enum UDM_UNSTRUCTURED_MESH_TYPE UDM_UNSTRUCTURED_MESH_TYPE;

/** 
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 UDM_UNSTRUCTURED_MESH gives the possible definitions of unstructured
 meshes which are currently supported.

*/

struct UDM_UNSTRUCTURED_MESH { 
       UDM_UNSTRUCTURED_MESH_TYPE type; /**< type of unstructured mesh */ 

       int dims;  /**< It is the dimensionality of mesh. It should be 3
                       if coord[0], coord[1], and coord[2] (below) are used to
                       specify the coordinates of nodes, and it should
                       be 2 if only coord[0], coord[1] are used, and it
                       should be 1 if only coord[0] is used.        */
                   
       long long offsets[4]; /**< offsets[i] with i from 0 to 3 are
                                  offsets of zones, faces, edges and
                                  nodes in the current pe.
                                  Currently, only the offset for mesh
                                  elements has been implemented, i.e.,
                                  offsets[0] for a zone-mesh, 
                                  offsets[1] for a face-mesh, offsets[2]
                                  for an edge_mesh.               */
                                  
       long long gsizes[4]; /**< gsizes[i] with i from 0 to 3 are
                                 the total number of zones, faces,
                                 edges and nodes. Currently, only the
                                 total number of mesh elements are
                                 implemented, ie, gsizes[0] for a zone-mesh,
                                 offsets[1] for a face-mesh, and offsets[2]
                                 for an edge-mesh.
                                                                    */ 

       long long sizes[4]; /**< sizes[i] with i from 0 to 3 are
                                the numbers of zones, faces, edges
                                and nodes in the current pe.       */

       long long fsizes[4]; /**< fsizes[i] with i from 0 to 3 are
                                 the numbers of ghost zones, faces, 
                                 edges and nodes in the current pe.
                                 If fsizes[0] is set to non-zero, 
                                 the array fakezones must be fsizes[0]
                                 long. If fsizes[1] is non-zero, 
                                 the array fakefaces must be fsizes[1] long,
                                 and same logic for 
                                 (fsizes[2], fakeedges)
                                 and (fsizes[3], fakenodes).        */

       long long ssizes[4]; /**< ssizes[i] with i from 1 to 3 are
                                 the numbers of slip faces, edges, and
                                 nodes in the current pe. If ssizes[1]
                                 is set to non-zero, the array slipfaces
                                 must be ssizes[1] long. If ssizes[2]
                                 non-zero, slipedges must be ssizes[2]
                                 long, and if ssizes[3] is non-zero,
                                 slipnodes must be ssizes[3] long. 
                                 ssizes[0] is ignored.              */
   
       long long bsizes[4]; /**< bsizes[i] with i from 1 to 3 are
                                 the numbers of faces, edges, and nodes
                                 on a boundary.If bsizes[1] is set to
                                 non-zero, the array bdryfaces must be
                                 bsizes[1] long. If bsizes[2] is non-zero,
                                 the array bdryedges must be bsizes[2]
                                 long, and if bsizes[3] is non-zero,
                                 bdrynodes must be bsizes[3] long. 
                                 bsizes[0] is ignored.              */

       UDM_DATA_TYPE datatype_mesh; /**< datatype of all void integer
                                         arrays used in the structure.  
                                                                    */

       UDM_DATA_TYPE datatype_coord; /**< datatype of coordinate arrays.
                                                                    */
 
       long long *plist; /**< plist is used for the order of elements
                              when a mesh is written and when  
                              the offsets of elements on pes are not given.
                              For zone-meshes, the elements are zones; for
                              face-meshes, the elements are faces; and for
                              edge-meshes, the elements are edges.
                                                                    */
       
       int idmin; /**< 1 for one-based ids, and 0 for zero-based ids.
		               This should be the same for all pes.
		               For example, in a mesh with 0-based ids, a
                               node with id 1 has coordinates
                               (coord[0][1], coord[1][1]). But, a mesh with
                               1-based ids, a node with id 1 has coordinates
                               (coord[0][0], coord[1][0]).
                               If users don't explicitly specify idmin in 
                               a call of FIO_mesh_write, idmin in the
                               most recent call of FIO_mesh_write 
                               will be used.  */ 
                       
       int order_for_nodelist; /**< This field will be used
                                    to indicate the order of nodes for 
                                    each mesh element, if a mesh element
                                    is made from a list of node in the mesh
                                    definition. Only three values 
                                    are allowed: 1 for the order of Ensight
                                    (right hand rule), -1 for the
                                    order used in GMV (left hand rule),
                                    0 for others or unspecified. By default,
                                    -1 will be used. If users don't explicitly
                                    specify it in a call of FIO_mesh_write,
                                    the one in the most recent call of
                                    FIO_mesh_write will be used. If a mesh
                                    doesn't involve a nodelist for element, 
                                    this field is ignored.  */ 
                        
       void *num_faces_for_zone; /**< an array of sizes[0] long
                                      for the number of faces for each zones
                                      for general polyhedrons. This field may be 
                                      used only when the type of mesh is 
                                      udm_general_mesh or udm_mixed_elements. */ 
       void *num_edges_for_zone; /**< an array of sizes[0] long 
                                      for the number of edges for zones
                                      for general polyhedrons. This field may 
                                      be used only when the type of mesh is 
                                      udm_general_mesh or udm_mixed_elements. */ 
       void *num_nodes_for_zone; /**< an array of sizes[0] long 
                                      for the number nodes for zones
                                      for general polyhedrons. This field may 
                                      be used only when the type of mesh is 
                                      udm_general_mesh or udm_mixed_elements. */  
       void *num_edges_for_face; /**< an array of sizes[1] long 
                                      for the number of edges
                                      for faces.                          */ 
       void *num_nodes_for_face; /**< an array of sizes[1] long 
                                      for the number of nodes
                                      for faces.                          */
       void *facelist_for_zone;  /**< an array of the face list for
                                      zones. The length of the array is
                                      the sum of the number of faces of each 
                                      zone. This field is ignored if no faces 
                                      are involved in a mesh.             */
       void *edgelist_for_zone;  /**< an array of the edge list for 
                                      zones. The length of the array is the sum
                                      of the number of edges of each zone.
                                      This field is ignored if no edges are 
                                      involved in a mesh.                 */
       void *nodelist_for_zone;  /**< an array of the node list for 
                                      zones. The length of the array is the sum 
                                      of the number of nodes of each zone.   */
       void *edgelist_for_face;  /**< an array of the edge list for 
                                      faces. The length of the array is the sum 
                                      of the number of edges of each face.   */ 
       void *nodelist_for_face;  /**< an array of the node list for   
                                      faces. The length of the array is the sum 
                                      of the number of nodes of each face.   */
       void *nodelist_for_edge;  /**< an array of the node list for  
                                      edges. The length of the array is the 
                                      double of the number of edges.         */
                                   
       void *gid_zone; /**< an array of length sizes[0] for global ids for 
                            zones. This field has not been implemented yet */
       void *gid_face; /**< an array of length sizes[1] for global ids for 
                            faces. This field has not been implemented yet */
       void *gid_edge; /**< an array of length sizes[2] for global ids of edges,
                            not implemented yet */
       void *gid_node; /**< an array of length sizes[3] for global ids of 
                            nodes */ 
 
       void *fakezones; /**< an array of length fsizes[0] for the 
                             list of ghost zones.                     */  
       void *masterzones; /**< an array of length 2 * fsizes[0]. The values of 
                               the array are the master pe and zone id on the 
                               master pe of each ghost zone,
                               ie, (rank, id, rank, id, ....).        */ 
 
       void *fakefaces; /**< an array of length fsizes[1] for the 
                             list of ghost faces.                     */  
       void *masterfaces; /**< an array of length 2 * fsizes[1]. The values of 
                               the array are the master pe and face id on the 
                               master pe of each ghost face,
                               ie, (rank, id, rank, id, ....).        */ 
      
       void *fakeedges; /**< an array of length fsizes[2] for the 
                             list of ghost edges.                     */  
       void *masteredges; /**< an array of length 2 * fsizes[2]. The values of 
                               the array are the master pe and edge id on the 
                               master pe of each ghost ghost edge,
                               ie, (rank, id, rank, id, ....).        */ 
 
       void *fakenodes; /**< an array of length fsizes[3] for the
                             list of ghost nodes.                     */  
       void *masternodes; /**< an array of length 2 * fsizes[3]. The values of 
                               the array are the master pe and node id on the 
                               master pe of each ghost node,
                               ie, (rank, id, rank, id, ....).        */ 

       void *slipfaces; /**< an array of length ssizes[1] for the 
                             list of slip faces.             */
       void *slipedges; /**< an array of length ssizes[2] for the 
                             list of slip edges.             */
       void *slipnodes; /**< an array of length ssizes[3] for the 
                             list of slip nodes.             */  

       void *bdryfaces; /**< an array of length bsizes[1] for the list
                             faces on the boundary.                  */

       void *bdryedges; /**< an array of length bsizes[2] for the list
                             edges on the boundary.                  */

       void *bdrynodes; /**< an array of length bsizes[3] for the list
                             nodes on the boundary.                  */

       void *nelems_connected_to_elem; /**< an array whose length is number of 
                                            mesh elements for the number of 
                                            mesh elements each mesh element 
                                            connected to.  In zone-meshes, zone 
                                            is the element, in face-meshes, 
                                            face is the element, and in 
                                            edge-meshes, edge is the element,
                                            If this array is not provided,
                                            don't set this array and the
                                            following array.        */

       void *elems_connected_to_elem;  /**< an array whose length is the sum
                                            of nelems_connected_to_elem for the
                                            list of mesh elements connected by 
                                            each element.           */

       void *nelems_connected_to_node; /**< an array of length sizes[3]
                                            for the number of mesh elements
                                            each node connected to.
                                            If this array is not provided,
                                            don't set this array and the
                                            following array.        */

       void *elems_connected_to_node;  /**< an array whose length is the sum
                                            of nelems_connected_to_node for the
                                            list of elements connected by each
                                            node.                   */

       UDM_UNSTRUCTURED_MESH_TYPE *types; /**< an array whose length is the 
                                               number of mesh elements. 
                                               In zone-meshes, zone is the 
                                               element, in face-meshes, face 
                                               is the element, and in 
                                               edge-meshes, edge is the element.
                                               This array is used if 
                                               and only if mesh type is
                                               udm_mixed_elements.
                                               The values of the array are not
                                               allowed to be udm_mixed_elements
                                               and udm_general_mesh. The values
                                               are either all zone-types (such 
                                               as udm_tet, udm_hex, udm_wedge, 
                                               udm_pentagon_prism) or all
                                               face-types (such as udm_triangle,
                                               udm_quad, udm_pentagon).  
                                               This field has not been 
                                               implemented yet. */

       void *coord[3]; /**< coord[i] is the coordinate array in dimension i,
                            i = 0, 1, ..., dims-1.                        */ 
      };  
typedef struct UDM_UNSTRUCTURED_MESH UDM_UNSTRUCTURED_MESH;

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 UDM_UNSTRUCTURED_MESH_SIZE gives sizes of variety of connectivity arrays
 in a part (or a whole) of an unstructured mesh. This struct is designed only for the
 use in FIO_meshsize.

*/

struct UDM_UNSTRUCTURED_MESH_SIZE {
       
  long long size_nodelist_for_zone; /**< the size of the array nodelist_for_zone
                                         for a part of mesh or whole mesh.   */
  long long totalsize_nodelist_for_zone;   
  long long size_edgelist_for_zone; /**< the size of the array edgelist_for_zone
                                         for a part of mesh or whole mesh.   */ 
  long long totalsize_edgelist_for_zone;
  long long size_facelist_for_zone; /**< the size of the array facelist_for_zone
                                         for a part of mesh or whole mesh.   */ 
  long long totalsize_facelist_for_zone;
  long long size_nodelist_for_face; /**< the size of the array nodelist_for_face
                                         for a part of mesh or whole mesh.   */ 
  long long totalsize_nodelist_for_face; 
  long long size_edgelist_for_face; /**< the size of the array edgelist_for_face
                                         for a part of mesh or whole mesh.   */ 
  long long totalsize_edgelist_for_face;
  long long size_elems_connected_to_elem; 
                                    /**< The size of the array 
                                         elems_connected_to_elem for a part of 
                                         mesh or whole mesh. If it is zero, 
                                         there are no arrays, 
                                         nelems_connected_to_elem and 
                                         elems_connected_to_elem, defined in 
                                         the mesh or the part of the mesh.   */  
  long long totalsize_elems_connected_to_elem;
  long long size_elems_connected_to_node;
                                    /**< The size of the array 
                                         elems_connected_to_node for a part of 
                                         mesh or whole mesh. If it is zero, 
                                         there are no arrays,
                                         nelems_connected_to_node and 
                                         elems_connected_to_node, defined in 
                                         the mesh or the part of the mesh.   */  
  long long totalsize_elems_connected_to_node;
  long long size_masterzones;      /**<  the size of the array masterzones for 
                                         a part of mesh. If it is zero, there 
                                         are no info about  masterzones
                                         is available.                       */
  long long totalsize_masterzones;
  long long size_masterfaces;      /**< the size of the array masterfaces for a 
                                        part of mesh.                        */ 
  long long totalsize_masterfaces;
  long long size_masteredges;      /**< the size of the array masteredges for a 
                                        part of mesh.                        */ 
  long long totalsize_masteredges;
  long long size_masternodes;      /**< the size of the array masternodes for a 
                                        part of mesh.                        */
  long long totalsize_masternodes;
  int gids_found[4];                /**< if gids_found[i] != 0, gids[i] is found
                                         in the mesh, i = 0, 1, 2, 3. Currently,
                                         only gids[3], ie, gids of nodes, is
                                         supported.                          */
 };
typedef struct UDM_UNSTRUCTURED_MESH_SIZE UDM_UNSTRUCTURED_MESH_SIZE; 
    

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_mesh_init sets every field of an unstructured mesh to invalid values.
 As a result,
 a user doesn't have to deal with other mesh definitions except the definition
 he/she is using. Specifically, after this call,
 dims is set to -1, idmin to idmin_default which is
 used in the previous call of FIO_mesh_write. order_for_nodelist to 0, type to
 udm_meshtype_invalid, datatype_mesh and datatype_coord to udm_datatype_invalid,
 offsets to (-1, -1, -1, -1), gsizes, sizes, fsizes, ssizes, bsizes to 
 (0, 0, 0, 0).
 all the pointers are set to null 

 - mesh: an input and output.

*/

int FIO_mesh_init(UDM_UNSTRUCTURED_MESH *mesh);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_mesh_write writes an unstructured mesh. This function is a collective
 call, ie, all the pes in the communicator must call this function. But, the
 number of mesh elements on a particular pe may be zero.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of an unstructured mesh.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be 
   the id of any object
   in this file.

 - mesh: an input, the part of mesh to be written.

 - coord_id: an input or output, the id of coordinates associated with
             the mesh.
             If coordinates are defined in the mesh, coord[0], coord[1], ...
             coord[dims-1] are used as the coordinate arrays, and the coord_id
             will be the output of the set of coordinates. If there are
             no coordinates defined in the mesh, a positive coord_id will be
             used for the coordinates of the array, but a negative coord_id
             is ignored in this call. 

 - mesh_id: an output, the id of an unstructured mesh.

 - Before the call, mesh.type, mesh.datatype_mesh, mesh.sizes[3] must
   be given. If zones are involved, mesh.sizes[0] must be given;
   if faces are involved, mesh.sizes[1] must be given; if edges are
   involved, mesh.sizes[2] must be given.

 - mesh.dims is the number of dimensions of coordinates, which may be
   given through either mesh.coord or coord_id. Therefore, for a zone-mesh,
   mesh.dims = 3; for a face-mesh, mesh.dims may be either
   2 or 3; for an edge-mesh, mesh.dims may be 1, or 2, or 3.

 - If coordinates are given through mesh.coord, mesh.datatype_coord
   must be given, and coord_id will be returned.

- If the offset of mesh elements is not provided for some pe, or it
   is a negative number, mesh.plist will be used to calculate the
   element offset of each pe. In this case the element order is
   determined through plist[0], plist[1], ..., plist[npes-1]. If
   mesh.plist is not given, a default plist will be used, which is
   plist[i] = i, i = 0, 1,..., npes-1.
   For a zone- mesh, offset of mesh elements is mesh.offsets[0]; for a
   face-mesh, the offset is mesh.offsets[1]; and for a
   edge-mesh, the offset is mesh.offsets[2].

 - mesh.gsizes is ignored in this function.

 - When mesh.type != udm_general_mesh, and type != udm_mixed_elements,
   an unstructured mesh must be defined through one of the following:  
\verbatim 
   (1)  nodelist_for_zone. 
   (2)  edgelist_for_zone,  nodelist_for_edge.  
   (3)  facelist_for_zone,  num_node_for_face, nodelist_for_face. 
   (4)  num_nodes_for_face, nodelist_for_face, 
        and sizes[0] > 0 for some pe, and sizes[1] must be the
        same as sizes[0] * (num of faces per element).  
   (5)  facelist_for_zone,  num_edges_for_face, edgelist_for_face,
        nodelist_for_edge.  
   (6)  mesh.coord[0], mesh.coord[1], mesh.coord[2].   
   (7)  nodelist_for_face.  
   (8)  edgelist_for_face, nodelist_for_edge.  
   (9)  coord[0], coord[1]. 
   (10) nodelist_for_edge.  
   (11) coord[0].   
\endverbatim

 - When mesh.type == udm_general_mesh, an unstructurted mesh mush be
   defined through one of the following:
\verbatim
   (1)  num_nodes_for_zone, nodelist_for_zone. 
   (2)  num_edges_for_zone, edgelist_for_zone, nodelist_for_edge
   (3)  num_faces_for_zone, facelist_for_zone, num_nodes_for_face,
        nodelist_for_face.  
   (4)  num_faces_for_zone, num_nodes_for_face, nodelist_for_face,
        and sum of num_faces_for_zone must be equal to sizes[1]. 
   (5)  num_faces_for_zone, facelist_for_zone, num_edges_for_face,
        edgelist_for_face, nodelist_for_edge. 
   (6)  num_nodes_for_zone, coord[0], coord[1], coord[2].  
   (7)  num_nodes_for_face, nodelist_for_face. 
   (8)  num_edges_for_face, edgelist_for_face, nodelist_for_edge. 
   (9)  num_nodes_for_face, coord[0], coord[1].
\endverbatim

 - Some of the 20 definitions above don't define a true mesh, because of
   the lack of connectivity. But they define the locations of all
   the nodes, which are supported in the UDM library. 

 - gid_node may be used in any one of above mesh definitions. gid_edge,
   gid_face, and gid_zone are not supported in the current UDM library yet.

 - If a mesh is written in a given definition, all other pointers
   in UDM_UNSTRUCTURED_MESH 
   must be set to null explicitly, which may be done through
   calling FIO_mesh_init(&mesh) before the mesh is defined.

 Examples 32-52 in Section 7.12 show the usage of FIO_mesh_init and FIO_mesh_write.
*/
/* 
 EExample 32: ctest_027a_mesh_e_n_1aw.c
 EExample 33: ctest_028_mesh_e_n_mixed_1aw.c
 EExample 34: ctest_029a_mesh_e_n_1aw.c
 EExample 35: ctest_030_mesh_f_e_n_mixed_1aw.c
 EExample 36: ctest_031_mesh_f_e_n_x_1aw.c
 EExample 37: ctest_032_mesh_f_n_1aw.c
 EExample 38: ctest_033_mesh_f_n_mixed_1aw.c
 EExample 39: ctest_034_mesh_n_1aw.c
 EExample 40: ctest_035_mesh_n_gid_1aw.c     
 EExample 41: ctest_036_mesh_n_x_1aw.c
 EExample 42: ctest_037a_mesh_n_x_mixed_1aw.c
 EExample 43: ctest_038_mesh_zone_face_mixed_1aw.c
 EExample 44: ctest_039_mesh_3D_face_n.c
 EExample 45: ctest_040_mesh_3D_face_x.c
 EExample 46: ctest_041_mesh2D_e_n_1aw.c 
 EExample 47: ctest_042_mesh2D_e_n_mixed_1aw.c    
 EExample 48: ctest_043_mesh2D_n_1aw.c
 EExample 49: ctest_044_mesh2D_n_mixed_1aw.c
 EExample 50: ctest_043a_mesh2D_n_1aw.c
 EExample 51: ctest_044b_pentagon_w.c
 EExample 52: ctest_053_mesh_zfn_g_w.c
*/

int FIO_mesh_write(const int group_id,
                   const char *path,
                   UDM_UNSTRUCTURED_MESH mesh,
                   int *mesh_id, int *coord_id);
                                           
/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_mesh_create creates a mesh. The content of the mesh may be written
 through the subsequent calls of FIO_mesh_append until FIO_mesh_close
 is called. This function is a collective call, ie, all the pes in the
 communicator must call this function.
 Examples of the usage will be given after
 function FIO_mesh_close.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of an unstructured mesh.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - mesh: an input, the mesh to be written.
         The following fields of mesh must be set before the call: dims, 
         idmin, order_for_nodelist, type, datatype_mesh, and gsizes. 
         mesh.datatype_coord
         must also be given if mesh.coord will be given in subsequent calls 
         of FIO_mesh_append.  

 - mesh_id: an output, the id of an unstructured mesh.

 - total_linear_size: an input, the size of all the integer pointers used in the
                      mesh definition. For example, if the mesh is defined through
                      six arrays, num_faces_for_zone, facelist_for_zone, 
                      num_nodes_for_face, nodelist_for_face, fakezones,
                      and gid_node, total_linear_size
                      is the sum of the lengths of the six arrays on all the pes.
 
 - coord_id: an input or output, the id of coordinates associated with
             the mesh.
             If coordinates are defined in the mesh, coord[0], coord[1], ...
             coord[dims-1] are used as the coordinate arrays, and the coord_id
             will be the output for  the set of coordinates. If there are
             no coordinates defined in the mesh, an positive coord_id will be
             used for the coordinates of the array, but a negative coord_id
             is ignored in this call.
*/

int FIO_mesh_create(const int group_id,
              const char *path_and_name,
              UDM_UNSTRUCTURED_MESH mesh,
              const long long total_linear_size,
              int *mesh_id, int *coord_id);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

  FIO_mesh_append appends a part of mesh into an unstructured mesh
  FIO_mesh_create
  created before. This function is a collective call, ie, all the pes in the
  communicator must call this function. But, the number of mesh elements on an
  particular pe may be zero. Examples of the usage will be given after
  function FIO_mesh_close.

  - mesh_id: an input, the id of the mesh created before through FIO_mesh_create.

  - coord_id: an input, the id of coordinates of the mesh created before through
              FIO_mesh_create. If the mesh doesn't have coordinates defined, 
              a negative coord_id should be set before the call.

  - mesh: an input, the part of mesh to be appended.
          The following fields of mesh must be set and should be the same as 
          those in function FIO_mesh_create:  dims, idmin, order_for_nodelist, 
          type,  datatype_mesh and datatype_coord. mesh.gsizes is ignored in 
          this function.  
  
  - The input, mesh, is the same as that used in function FIO_mesh_write. 
    Particularly, although the member, mesh.offsets, doesn't have to be set 
    before the call, mesh.offsets may be set before the call. If a user set 
    mesh.offsets before the call, only one value of mesh.offsets is used. For 
    a zone-mesh, only offsets[0] will be used; for a face-mesh, only offsets[1] 
    will be used; and for a edge-mesh, only offsets[2] will be used. The one 
    value of mesh.offsets is the offset of mesh elements measured in the 
    current collective call of FIO_mesh_append.
    For example, through one pe, a face-mesh with 10 face elements is created
    through one call of FIO_mesh_create and two subsequent calls of 
    FIO_mesh_append.
    Each of the calls of FIO_mesh_append has 5 mesh elements.   
    In the both calls of FIO_mesh_append, mesh.offsets[1] should be 0.
    mesh.offsets[1] should not be 5 in any of the calls of FIO_mesh_append.
 
*/

int FIO_mesh_append(const int mesh_id, const int coord_id,
                      UDM_UNSTRUCTURED_MESH mesh);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

  FIO_mesh_close closes a mesh FIO_mesh_create created before.

  - A mesh cannot be closed without call of FIO_mesh_append before.
    When a mesh is closed through FIO_mesh_close,
    the total number of nodes, ie, mesh.sizes[3], written through
    FIO_mesh_append should be the same as mesh.gsizes[3] in 
    FIO_mesh_create. 

  Examples 55,56 in Section 7.13 show the usage of FIO_mesh_create, FIO_mesh_append,
  and FIO_mesh_close.
*/
/*
  EExample 55: ctest_056_mesh_append_zn_w.c 
  EExample 56: ctest_057_mesh_append_zfn_w.c 
*/

int FIO_mesh_close(const int mesh_id);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_meshcoord_write writes a set of coordinates to a number of unstructured meshes. 

 - mesh_ids: an input, an array for ids of a number of unstructured meshes.

 - n_mesh: an input, the number of unstructured meshes.

 - dims: an input, the common dimensionality of the meshes.

 - size: an input, the size of arrays, coord1, coord2, coord3.

 - datatype: an input, the datatype of arrays, coord1, coord2, coord3.

 - coord1, coord2, coord3: input, arrays for coordinates in dimension 0, 1, 
   and 2 respectively. 

 - coord_id: an output, the id of the set of coordinates.

 - The input, dims, must be the same with the dimensionality of these n_mesh
   meshes. If dims = 1, only coord1 will be used, and coord2 and coord3 are
   ignored. If dims = 2, coord1 and coord2 must be given, and coord3
   is ignored. If dims = 3, all three coordinates must be given.

 - mesh_ids points to a list of meshes which the set of coordinates is
   associated with. These meshes must have the same number of nodes,
   mesh.sizes[3], and mesh.sizes[3] of these meshes must be the same as
   the input, size. Each of these meshes may use only a part of nodes in
   its mesh definition. 

 - The output coord_id may be used for writing other meshes.

 Examples 53,54 in Section 7.12 show the usage of FIO_meshcoord_write.
*/
/* 
 EExample 53: ctest_045_mesh_n_and_x.c 
 EExample 54: ctest_046a_mesh_n_shared_x.c
*/

int FIO_meshcoord_write(int *mesh_ids, int n_mesh,
                          int dims, long long size,
                          UDM_DATA_TYPE datatype,
                          const void *coord1,
                          const void *coord2,
                          const void *coord3,
                          int *coord_id);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_meshvar_write writes a set of variables which are associated
 with one or more unstructured meshes.

 - group_id: an input, the id of a group.

 - path_and_names: an input, an array of paths and names
   for nvars variables.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of an
   element of path_and_names
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in one of path_and_names, such as /grp1/grp2/mygrp,
   the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - mesh_ids: an input, an array for ids of the associated meshes.

 - n_mesh: an input, the number of meshes in mesh_ids.

 - nvars: an input, the number of variables to be written.

 - vars: an input, an array of UDM_MESH_VAR.
         The following fields of UDM_MESH_VAR must be specified for each
         variable: type, rank, datatype, and buffer. If rank is one
         or more, comp_sizes and components must be specified.
 
 - var_ids: an output, an array for ids of the nvars variables.

 - Each component of a vector
   or tensor is considered one variable in the input of this function.

 - Each variable may have its own path_and_name, but the components of
   a vector or tensor should have the same path_and_name. Two variables
   with same path_and_name are considered as different components of a
   same vector or tensor.

 - n_mesh should be one except for the case in which all the variables
   in this call are defined on nodes. Only nodal variables may be associated
   with more than one mesh.   

 - Zone-, or face-, or edge- variable may be associated with only one mesh.

 Examples 57-66 in Section 7.14 show the usage of FIO_meshvar_write.
*/
/* 
 EExample 57: ctest_047_meshvar_0a_w.c
 EExample 58: ctest_048a_meshvar_mixed_0w.c
 EExample 59: ctest_049a_mesh_n_shared_nodevar_1aw.c
 EExample 60: ctest_050a_meshvar_z_f_e_n_2aw.c
 EExample 61: ctest_051_mixed_meshvar_z_f_e_n_2aw.c
 EExample 62: ctest_052_mixed_meshvar2D_e_n_1aw.c 
 EExample 63: ctest_053_meshvar_pentagon_w.c
 EExample 64: ctest_054_meshvar_zn_w.c 
 EExample 65: ctest_054b_meshvar_zfn_w.c
 EExample 66: ctest_055_meshvar_zfn_g_w.c
*/

int FIO_meshvar_write(const int    group_id,
                        char         *path_and_names[],
                        const int    mesh_ids[],
                        const int    n_mesh,
                        int          nvars,
                        UDM_MESH_VAR vars[],
                        int          var_ids[]);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_meshvar_create creates a set of variables which are associated
 with one or more unstructured meshes. The content of the variables
 will be written through subsequent calls of FIO_meshvar_append.
 This function is a collective call, ie., all the pes have to call
 this function if it is called by any pe. The examples of the usage
 will be given after FIO_meshvar_append.

 - group_id: an input, the id of a group.

 - path_and_names: an input, an array of paths and names
   for nvars variables.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of an
   element of path_and_names
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in one of path_and_names, such as /grp1/grp2/mygrp,
   the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - mesh_ids: an input, an array for ids of the associated meshes.

 - n_mesh: an input, the number of meshes in mesh_ids.

 - nvars: an input, the number of variables to be written.

 - vars: an input, an array of UDM_MESH_VAR.
         The following fields of UDM_MESH_VAR must be specified for each
         variable: type, rank, and datatype. If rank is one
         or more, comp_sizes and components must be specified.

 - var_ids: an output, an array for ids of the nvars variables.

*/

int FIO_meshvar_create(const int  group_id,
                        char       *path_and_name[],
                        const int  mesh_ids[],
                        const int  nmesh,
                        int        nvars,
                        UDM_MESH_VAR vars[],
                        int          var_ids[]);


/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_meshvar_append writes a part of variables defined on a part
 of unstructured mesh. This function is a collective call.

 - nvars: an input, the number of variables in var_ids and vars.
   Each component of a vector or tensor is considered one variable
   in this function.

 - var_ids: an input, an array of ids for variables. 

 - vars: an input, an array of UDM_MESH_VAR.
         The following fields of UDM_MESH_VAR must be specified for each
         variable: type, rank, datatype, and buffer. If rank is one
         or more, comp_sizes and components must be specified.
 
 - The buffer of a variable in a particular call on a particular pe
   should be consistent with the part of mesh in the particular call
   on the particular pe. For example, for an application with 2 pes,
   a mesh is written through two calls of FIO_mesh_append of each pe.
   Therefore, each pe writes two parts of mesh. Suppose pe0 wrote part1
   and part3 in the two calls of FIO_mesh_append, and pe1 wrote part2
   and part4 in the two calls. The first call of FIO_meshvar_append for
   pe0 and pe1 is associated to part1 and part2 of mesh, and the second
   call of pe0 and pe1 associated to part3 and part4. 

   Examples 67,68 in Section 7.15 show the usage of FIO_meshvar_create and 
   FIO_meshvar_append. 
*/
/*
 EExample 67: ctest_058_meshvar_append_zfn_w1.c
 EExample 68: ctest_059_meshvar_append_zfn_w2.c 
*/
int FIO_meshvar_append(const int nvars,
                         int var_ids[], 
                         UDM_MESH_VAR vars[]);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_get_meshdef gets basic information of an unstructured mesh.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of an unstructured mesh.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be 
   the id of any object
   in this file.

 - mesh: an output, the unstructured mesh. The following fields of mesh
   will be set through the call: dims, idmin, order_for_nodelist, type, 
   datatype_mesh, datatype_coord, gsizes, sizes, offsets, fsizes, ssizes,
   and bsizes.  

 - mesh_id: an output, the id of the unstructured mesh.

 - coord_id: an output, the id of the coordinates used in the unstructured mesh.

 - For a zone-mesh, only offsets[0] is meaningful in mesh.offsets;
   for a face-mesh, only offsets[1] is meaningful; and for a edge-mesh,
   only offsets[2] is meaningful.

 - The fields of mesh in output,
   offsets, sizes, fsizes, ssizes and bsizes,  
   have different meanings depending on the usage of calls. If the number of pes
   to call this function is different from
   the original number of pes which wrote the mesh, 
   this function sets the fields to those on the first original pes
   on which there are mesh elements. If the number of pes to call this function
   is the same as the original number of pes which wrote the mesh,
   this function sets the fields to those
   on the original pe.

 - All pointers of mesh members are set to null in return.

*/

int FIO_get_meshdef(const int group_id,
                    const char *path_and_name,
                    UDM_UNSTRUCTURED_MESH *mesh,
                    int *mesh_id, int *coord_id);


/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 For a given number of element of an unstructured mesh,
 FIO_meshsize gets the numbers of zones, faces, edges, and nodes
 and the sizes of the arrays used for mesh connectivity.

 - mesh_id: an input, the id of a mesh.

 - mesh: input and output. The following fields of mesh should be
         specified before the call: 
         dims, type, offsets[0] and sizes[0] for a zone-mesh, 
         offsets[1] and sizes[1] for a face-mesh, offsets[2] and
         sizes[2] for a edge-mesh. The following fields of mesh
         will be output through the call: sizes, fsizes, ssizes,
         bsizes.   

 - msize: an output, the information for the arrays used in the mesh.
          The following fields of msize will be output through the call:
          size_nodelist_for_zone for the size needed for the array
          nodelist_for_zone, size_edgelist_for_zone for the size needed
          for the array edgelist_for_zone, size_facelist_for_zone for
          the size needed for the array nodelist_for_face, 
          size_edgelist_for_face for the size needed for the array
          edgelist_for_face, size_elems_connected_to_elem for the
          size needed for th array elems_connected_to_elem,
          size_elems_connected_to_node for the size needed for the 
          array elems_connected_to_node, gids_found for global ids.

 - If msize->size_elems_connected_to_elem is zero, there are no
   elems_connected_to_elem and nelems_connected_to_elem defined
   in the part of mesh.
   If msize->size_elems_connected_to_node it is zero, there are no
   elems_connected_to_node and nelems_connected_to_node defined
   in the part of mesh.

 - Only gids_found[3] is implemented. If gids_found[3] is not zero,
   there are gnodes defined in original mesh definition.
 
 - This function sets all the pointers of mesh to null.

 - mesh->gsize is ignored in this function.

*/

int FIO_meshsize(const int mesh_id,
                   UDM_UNSTRUCTURED_MESH   *mesh,
                   UDM_UNSTRUCTURED_MESH_SIZE *msize);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 
 For a given number of element of an unstructured mesh,
 FIO_get_meshsize is the previous version of FIO_meshsize, and it is
 kept for previous users.

*/

int FIO_get_meshsize(const int mesh_id,
                     UDM_UNSTRUCTURED_MESH   *mesh,
                     long long *size_nodelist_for_zone,
                     long long *size_edgelist_for_zone,
                     long long *size_facelist_for_zone,
                     long long *size_nodelist_for_face,
                     long long *size_edgelist_for_face,
                     int gids_found[]);


/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 For a given rank of pe which was used in writing an unstructured mesh,
 FIO_meshsize_on_pe gets the numbers of zones, faces, edges, and nodes
 and the sizes of arrays used for mesh connectivity.

 - mesh_id: an input, the id of a mesh.

 - pe_rank: an input, one rank of original pes which wrote the mesh.

 - mesh: an output, the unstructured mesh. The following fields of mesh
         will be output through the call: dims, type, datatype_mesh,
         datatype_coord, offsets, sizes,
         fsizes, ssizes, bsizes. Only one value in offsets, the offset of
         mesh elements, will be set in the call, and the other three
         will not be touched.

 - msize: an output, the information for the arrays used in the mesh.
          The following fields of msize will be output through the call:
          size_nodelist_for_zone for the size needed for the array
          nodelist_for_zone, size_edgelist_for_zone for the size needed
          for the array edgelist_for_zone, size_facelist_for_zone for
          the size needed for the array nodelist_for_face,
          size_edgelist_for_face for the size needed for the array
          edgelist_for_face, size_elems_connected_to_elem for the
          size needed for th array elems_connected_to_elem,
          size_elems_connected_to_node for the size needed for the
          array elems_connected_to_node, gids_found for global ids.

 - If msize->size_elems_connected_to_elem is zero, there are no
   elems_connected_to_elem and nelems_connected_to_elem defined
   in the part of mesh.
   If msize->size_elems_connected_to_node it is zero, there are no
   elems_connected_to_node and nelems_connected_to_node defined
   in the part of mesh.

 - Only gids_found[3] is implemented. If gids_found[3] is not zero,
   there are gnodes defined in original mesh definition.

 - mesh->gsize is ignored in this function.

*/

int FIO_meshsize_on_pe(const int mesh_id, const int pe_rank,
                     UDM_UNSTRUCTURED_MESH   *mesh,
                     UDM_UNSTRUCTURED_MESH_SIZE *msize);


/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_get_meshsize_on_pe is the previous version of FIO_meshsize_on_pe.
 This function is kept for previous users.

*/

int FIO_get_meshsize_on_pe(const int mesh_id, const int pe_rank,
                     UDM_UNSTRUCTURED_MESH   *mesh,
                     long long *size_nodelist_for_zone,
                     long long *size_edgelist_for_zone,
                     long long *size_facelist_for_zone,
                     long long *size_nodelist_for_face,
                     long long *size_edgelist_for_face,
                     int gids_found[]);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_get_mesh gets a part of an unstructured mesh.

 - group_id: an input, the id of a group.

 - path_and_name: an input, the path and name of an unstructured mesh.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be 
   the id of any object
   in this file.
 
 - mesh: input and output, the unstructured mesh. 
         The following fields of mesh must be given before the call:
         dims, idmin, order_for_nodelist, type, datatype_mesh, 
         offsets and sizes.
         If coordinates were defined in the mesh and coordinates are
         requested, mesh.datatype_coord must be given.  
         If ghost zones, faces, edges and nodes were defined and are
         requested, fsizes must be given. If slip faces, edges and nodes
         are defined and are requested, fsizes must be given. If
         boundary zones, faces, edges and nodes were defined and are requested,
         bsizes must be given. Any of the pointers in mesh will be output
         if it was defined in the original mesh. mesh.coord is also
         an output if coordinates were defined and are requested.

 - mesh_id: an output, the id of the mesh for future reference.

 - All the arrays requested must be allocated before the call, and
   all other arrays must set to null before the call.

 - If the original mesh is defined through nodelist_for_zone,
   this function gives nodelist_for_zone. It also gives 
   facelist_for_zone, edgelist_for_face and nodelist_for_edge if their
   memories are allocated. 

 - Functions FIO_meshsize and FIO_meshsize_on_pe are recommended
   for the sizes of the arrays requested.

 - mesh->gsize is ignored in the call.

 Examples 69-77 in Section 7.16 show the usage of FIO_get_meshdef, FIO_get_meshsize,
 FIO_get_meshsize_on_pe, FIO_get_mesh.
*/
/* 
 EExample 69: ctest_027b_mesh_e_n_1br.c 
 EExample 70: ctest_029b_mesh_f_e_n_1br.c 
 EExample 71: ctest_037b_mesh_read_1.c 
 EExample 72: ctest_046b_mesh_n_shared_x_r.c
 EExample 73: ctest_048b_meshvar_mixed_1r.c 
 EExample 74: ctest_041b_mesh_read2D.c
 EExample 75: ctest_051a_mesh_read_whole.c
 EExample 76: ctest_051b_mesh_read_pe.c
 EExample 77: ctest_051c_mesh_read_part.c
*/

int FIO_get_mesh(const int group_id,
                 const char *path_and_name,
                 UDM_UNSTRUCTURED_MESH *mesh,
                 int *mesh_id);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_get_nodelist gets nodelist_for_zone (for a zone-mesh) or
 nodelist_for_face (for a face-mesh) for a part of an unstructured mesh
 if nodelist_for_zone (or nodelist_for_face) is not defined in the original
 mesh.

 - mesh: an input, the unstructured mesh. The following fields
         of mesh must be given
         before the call: dims, datatype and sizes.  

 - For udm_general_mesh, this function currently work only for the 
   following two cases.
   (1) zone elements are made from faces and faces are made from nodes,
   (2) face elements are made from edges and edges are made from nodes.
*/

int FIO_get_nodelist(UDM_UNSTRUCTURED_MESH *mesh);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_fread_meshvar reads a number of variables defined on a part of
 an unstructured mesh.

 - group_id: an input, the id of a group.

 - path_and_names: an input, an array of paths and names
   for nvars variables.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of an
   element of path_and_names  
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in one of path_and_names, such as /grp1/grp2/mygrp, 
   the path is an absolute
   path, relative to the root. In this case, group_id may be
   the id of any object
   in this file.

 - nvars: an input, the number of variables requested.

 - mesh_id: an input, the id of the unstructured mesh associated with the
            set of variables.

 - offset_elm: an input, the offset of mesh elements.

 - size_elm: an input, the number of mesh elements on which the variables 
             are queried.

 - vars: input and output, an array for mesh variables.
         The following fields of vars[i] (i = 0, 1, ..., nvars-1) must be
         given before the call: type, rank, and datatype. If rank is not 
         zero, comp_sizes ad components must be given before the call.
         After the call, buffer will be filled with the values of a variable or
         a component of a variable.

 - This function reads nvars variables associated
   with an unstructured mesh. Each component of a vector
   or tensor is considered as
   a variable in the input. 

 - This function reads only a part of each variable, and the part is
   specified by offset_elm and size_elm. If offset_elm is set to zero and
   size_elm is set to the total number of mesh elements, this function will
   read the variables defined on the whole mesh.

 Examples 78-82 in Section 7.17 show the usage of FIO_fread_meshvar.
*/
/* 
 EExample 78: ctest_049b_mesh_n_shared_nodevar_2ar.c 
 EExample 79: ctest_050b_meshvar_z_f_e_n_2br.c
 EExample 80: ctest_051d_meshvar_read_whole.c
 EExample 81: ctest_051e_meshvar_read_pe.c
 EExample 82: ctest_051f_meshvar_read_part.c
*/
int FIO_fread_meshvar(const int group_id,
                        char **path_and_names,
                        const int nvars,
                        const int mesh_id,
                        long long offset_elm,
                        long long size_elm,
                        UDM_MESH_VAR vars[]);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_nmesh gives the number of unstructured meshes for a given id of 
 mesh variable.

 - var_id: an input, the id of a variable.

 - nmesh: an output, the number of the associated meshes.

 - Since a structured mesh variable may be associated with
   only one mesh, this function has no meaning for structured mesh variables.

 - Since only a node variable may be associated with more than one
   unstructured mesh, this function is meaningful only for node variables of
   unstructured meshes.
*/

int FIO_nmesh(const int var_id, int *nmesh);

/**
\ingroup unsmesh Functions for Unstructured Meshes and Variables

 FIO_get_mesh_info gives the information of the associated
 meshes for a given variable.

 - var_id: an input, the id of a variable.

 - nmesh: an input and output, the number of the associated meshes.

 - mesh_ids: an output, an array for the ids of the associated meshes.

 - coord_ids: an output, an array for the ids of coordinates 
              of the associated meshes. 

 - meshes: an output, an array of the associated meshes. The following 
   fields of each mesh are output through the call: dims, type, idmin,
   order_for_nodelist, datatype_mesh, datatype_coord, gsizes, sizes,
   offsets, fsizes, ssizes, and bsizes.

 - For a zone-mesh, only offsets[0] is meaningful in offsets of meshes;
   for a face-mesh, only offsets[1] is meaningful; and for an edge-mesh,
   only offsets[2] is meaningful.

 - The fields of meshes, offsets, sizes, fsizes, ssizes and bsizes,  
   have different meanings depending on the usage of calls. If the number of pes
   calling this function is different from
   the original number of pes which wrote the mesh,
   this function sets these fields to those on the first original pes
   on which there are mesh elements. If the number of pes calling this function
   is the same as the original number of pes which wrote the mesh,
   this function sets the fields to those
   on the original pe.

 - Before the call, mesh_ids, coord_ids and meshes don't have to
   be allocated. For this case, they must be set to null,  
\verbatim 
       int nmesh;
       int *mesh_ids = NULL;
       int *coord_ids = NULL;
       UDM_UNSTRUCTURED_MESH *meshes = NULL;
       FIO_get_mesh_info(var_id, &nmesh, &mesh_ids, &coord_ids, &meshes).
\endverbatim
    A clean-up is needed after use
\verbatim 
       if (nmesh)
          { free(mesh_ids);
            free(coord_ids);
            free(meshes);
           }
\endverbatim

 - mesh_ids, coord_ids and meshes may be allocated before the call.

 Example 83 in Section 7.18 shows the usage of FIO_nmesh and FIO_get_mesh_info.
*/
/*
 EExample 83:  ctest_050c_meshvar_z_read.c 
*/
int FIO_get_mesh_info(const int var_id, int *nmesh,
                      int **mesh_ids, int **coord_ids,
                      UDM_UNSTRUCTURED_MESH **meshes);

/**
\defgroup meshlist Functions for Listing Meshes and Variables

 This section covers the functions to list meshes and variables.

*/

/**
\ingroup meshlist Functions for Listing Meshes and Variables

 UDM_MESH_LIST_STRUCT defines possible outputs from function
 FIO_meshlist.

*/

struct UDM_MESH_LIST_STRUCT {
       char *name;   /**< path and name                            */
       int  objID;   /**< id of an object                          */

       UDM_MESH_OBJECT_TYPE objtype; /**< udm_unstructured_mesh,    or
                                          udm_unstructured_meshvar, or 
                                          udm_structured_mesh,      or 
                                          udm_structured_meshvar */

       int  dims; /**< dimensionality of a mesh                  */

/* for unstructured mesh */

       int  idmin; /**< 1 for one-based ids, 0 for zero-based ids  */

       int order_for_nodelist; /**< to indicate the order of nodes for
                                    each mesh element. Only three values
                                    are allowed: 1 for the order of Ensight
                                    (right hand side rule), -1 for the
                                    order used in GMV (left hand rule),
                                    0 for others or unspecified.     */

       int coordID; /**< valid for an unstructured mesh when 
                         coordinates are defined in the mesh.        */

       UDM_UNSTRUCTURED_MESH_TYPE  meshtype; /**< only for unstructured
                                                  mesh.              */     
       long long gsizes[4]; /**< For unstructured meshes, 
                                 gsizes[i] with i from 0 to 3 are the total
                                 numbers of zones, faces, edges, nodes.
                                 Currently,  only the total number of
                                 elements are implemented, which is
                                 gsizes[0] for zone-meshes, gsizes[1]
                                 for face-meshes, and gsizes[2] for
                                 edge-meshes.  For structured meshes,
                                 gsizes[i] with i from 0 to 2 are the sizes
                                 of mesh elements in 3 dimensions, excluding
                                 any ghost elements. The dimension 0 is
                                 the dimension changing most slowly. 
                                                                     */
       long long offsets[4]; /**< For unstructured meshes,
                                  currently only the offset of mesh elements
                                  is implemented, which is offsets[0] for
                                  zone-meshes, offsets[1] for face-meshes,
                                  and offsets[2] for edge-meshes.    */
       
       long long sizes[4]; /**< For unstructured meshes, they are
                                the numbers of zones, faces, edges and
                                nodes. For structured meshes, sizes[i], 
                                i = 0, 1, 2, are the sizes of mesh elements
                                in three dimension, excluding any ghost
                                elements.                            */

       long long fsizes[4]; /**< This field is for unstructured meshes only.
                                 fsizes[i], i = 0,1,2,3, are the sizes
                                 of ghost zones, ghost faces, ghost
                                 edges, and ghost nodes.          
                                                                     */

       long long ssizes[4]; /**< This field is for unstructured meshes only.
                                 ssizes[i], i = 1,2,3 are the sizes 
                                 of slip faces, slip edges and slip
                                 nodes. ssizes[0] is ignored. 
                                                                     */ 

       long long bsizes[4]; /**< This field is for unstructured meshes only.
                                 bsizes[i], i = 1,2,3 are the sizes 
                                 of faces, edges and nodes which are
                                 on bounaries. bsizes[0] is ignored. */ 

       UDM_UNSTRUCTURED_MESH_SIZE msize; 

       UDM_DATA_TYPE  datatype_mesh; /**< datatype stored for ids in
                                          mesh definition.           */
       
       UDM_DATA_TYPE datatype_coord; /**< datatype stored for coordinates
                                          in mesh definition.        */  

/* for structured mesh */  
 
       long long nbdyl[3]; /**< This field is for structured meshes only.
                                the numbers of ghost elements at the 
                                lower ends of three dimensions       */
       long long nbdyr[3]; /**< for structured meshes only. 
                                the numbers of ghost elements at the 
                                higher ends of three dimensions      */
           
       int element_centered; /**< This field is for structure meshes only.
                                  It is 1 if 
                                  (coord[0][k],coord[1][j],coord[2][i]) 
                                  is a center of an element, and is 0
                                  if (coord[0][k],coord[1][j],coord[2][i])
                                  is a corner of an element.         */
       
       double dcoord[3]; /**< This field is for structured meshes only.
                              If dcoord[i] > 0, dimension i will be
                              considered uniform, and dcoord[i] is the 
                              width of an element in dimension i, 
                              i = 0, 1, 2.                          */
           
       double coordmin[3]; /**< This field is for structured meshes only.
                                coordmin[i] is 
                                the minimum value of coordinate of mesh
                                elements in dimension i,
                                excluding any possible ghost elements.
                                                                       */
/* for mesh variables  */  

       UDM_DATA_TYPE  datatype_var; /**< This field is for mesh variables only,
                                         and it is the datatype stored
                                         for the variable. */ 
                                                       
       UDM_MESH_VAR_TYPE vartype;   /**< This field is for mesh variables
                                         only to indicate 
                                         zone-variable, or face-variable,
                                         edge-variable, or node-variable. */  

       int meshvar_rank;            /**< This field is for mesh variables only.  
                                         It is 0 for scalars, 1 for vectors,
                                         and a number larger than one for 
                                         tensors.                    */

       int var_comp_sizes[COMP_RANK]; /**< For vectors, var_comp_sizes[0] is the
                                        number of components of the
                                        vector. For tensors, var_comp_sizes[i]
                                        is the number of components for
                                        index i, i = 0, 1, ...,
                                        meshvar_rank-1. For example,
                                        for a tensor Tij (i = 0, 1, 2;
                                        j = 0, 1, 2), meshvar_rank = 2,
                                        var_comp_sizes[0] = 3; 
                                        var_comp_sizes[1] = 3.        */ 
                          
/* for structured mesh variables */  

/*       int size_matched_with_elem;    to be removed,
                                        This field is for structured
                                        mesh variables only. It is 1 
                                        if sizes of a variable match
                                        with sizes, nbdyl, and nbdyr
                                        of in all the dimension i, 
                                        i = 0,..., dims-1. It is 0
                                        if the size of a variable 
                                        in the dimension i is 
                                        (sizes[i] + nbdyl[i] + nbdyr[i] + 1) 
                                        of a structured mesh. The case
                                        with size_matched_with_elem = 0
                                        happens when the coordinate of the
                                        mesh is the corner of a mesh
                                        element, and the variable is
                                        a node variable.             */
      };
typedef struct UDM_MESH_LIST_STRUCT UDM_MESH_LIST_STRUCT;

/**
\ingroup meshlist Functions for Listing Meshes and Variables

 FIO_meshlist_nitems gives the number of meshes or variables under a
 given group.

 - group_id: an input, the id of a group

 - path_and_name: an input, the path and name of a group.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be 
   the id of any object
   in this file.

 - filter: an input, the name to be matched with.
   A non-null filter limits the scope of search in which
   the name of mesh or variable should match with filter.

 - type: an input, the type of UDM_MESH_OBJECT_TYPE,
   which limits the scope of the query.

 - recursive: an input, 0 or 1. 1
   for a recursive query, and 0 for a query limited to the immediate
   children of the group.

 - nitems: an output, the number of objects under the group which meet
   the specifications.

*/

int FIO_meshlist_nitems(const int group_id,
                          const char *path_and_name,
                          const char *filter,
                          UDM_MESH_OBJECT_TYPE type,
                          int recursive,
                          int *nitems);

/**
\ingroup meshlist Functions for Listing Meshes and Variables

 FIO_meshlist gives the information of meshes or variables under a given
 group.

 - group_id: an input, the id of a group

 - path_and_name: an input, the path and name of a group.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to group_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, group_id may be 
   the id of any object
   in this file.

 - filter: an input, the name to be matched with.
   A non-null filter limits the scope of search in which
   the name of mesh or variable should match with filter.

 - type: an input, the type of UDM_MESH_OBJECT_TYPE,
   which limits the scope of the query.

 - recursive: an input, 1 or 0, 1
   for a recursive query, and 0 for a query limited to the immediate
   children of the group specified by group_id and path_and_name.

 - nitems: an input or output, the number of objects under the group which meet
   the specifications. 

 - items: an output, an array of UDM_MESH_LIST_STRUCT.
   If items is allocated before the call, nitems is an input,
   otherwise nitems is an output.

 - If items is not allocated before the call, users
   must set items to null. In this case, this function will calculate
   nitems and allocate the memory for items, and users are expected
   to clean up after use, for example,
\verbatim
        for (i = 0; i < *nitems; i++)
          { free(items[i].name);
           }
        if (*nitems) free(items);
\endverbatim

 - If type is udm_unstructured_mesh, the following fields of UDM_MESH_LIST_STRUCT
   will be available after the call, name, objID, dims, idmin, order_for_nodelist,
   coordID (> 0 if mesh coordinates exist), meshtype, gsizes, offsets, sizes, 
   fsizes, ssizes, bsizes, datatype_mesh, datatype_coord.  
   Among offsets, only offsets[0] in list is meaningful
   for a zone-mesh; only offsets[1] is meaningful for a face-mesh;
   and only offsets[2] is meaningful for an edge-mesh.

 - If type is udm_unstructured_mesh, the fields of list, 
   offsets, sizes, fsizes, ssizes and bsizes, 
   have different meanings depending on the usage of the call. If the number of pes
   calling this function is different from
   the original number of pes which wrote the mesh,
   this function sets these fields to those on the first original pes
   on which there are mesh elements. If the number of pes calling this function
   is the same as the original number of pes which wrote the mesh,
   this function sets the fields to those
   on the original pe.

 - If type is udm_structured_mesh, the following fields of UDM_MESH_LIST_STRUCT
   will be available after the call, name, objID, dims, gsizes, sizes, offsets,
   nbdyl, nbdyr, element_centered,
   dcoord, coordmin.

 - If type is udm_structured_mesh, the fields of UDM_MESH_LIST_STRUCT,
   nbdyl, nbdyr, sizes, offsets, have different meanings depending on the usage
   of the call. If the number of pes calling this function is different from the
   original number of pes which wrote the mesh, this function sets these
   fields to those on the first original pes on which there are mesh elements.
   If the number of pes calling this function is the same as the original
   number of pes, this function sets these fields to those on the original pe. 

 - If type is udm_unstructured_meshvar or udm_structured_meshvar, the following 
   fields of UDM_MESH_LIST_STRUCT will be available after the call,
   name, objID, dims, datatype_var, vartype, meshvar_rank, var_comp_sizes. 

 - The items[i].name returned is the path relative to the input
   (group_id, path_and_name). For example,
   items[i].name = "group1/subgroup1/mymesh".

 Examples 84,85 in Section 7.19 show the usage of FIO_meshlist_nitems and FIO_meshlist.
*/
/* 
 EExample 84: ctest_048c_meshvar_mixed_2r.c 
 EExample 85: ctest_019b_smesh_meshlist.c 
*/

int FIO_meshlist(const int group_id,
                   const char *path,
                   const char *filter,
                   UDM_MESH_OBJECT_TYPE type,
                   int recursive,
                   int *nitems,
                   UDM_MESH_LIST_STRUCT **items);


int FIO_smesh_create(const int group_id,
                       const char *path,
                       UDM_STRUCTURED_MESH mesh,
                       long long total_size,
                       int *mesh_id);

int FIO_smesh_append(int mesh_id, UDM_STRUCTURED_MESH mesh);

int FIO_smesh_close(const int mesh_id);

int FIO_smeshvar_create(const int   group_id,
                          char         *path[],
                          const int    mesh_id,
                          int          nvars,
                          UDM_MESH_VAR vars[],
                          int          varids[]);

int FIO_smeshvar_append(int nvars, int var_ids[],
                        UDM_MESH_VAR vars[]);

/**
\defgroup mpi Functions Related to MPI

 This section covers the functions which are directly related to MPI.

*/

/**
\ingroup mpi Functions Related to MPI

 FIO_comm_create creates a user-defined MPI communication world, which
 may be different from the default one. The default one is MPI_COMM_WORLD.   

 - n: an input, the number of pes in a user-defined MPI communication world.

 - ranks: an input, a list of ranks with the length of n.

 - If the user-defined communication world is the MPI default,
   i.e, MPI_COMM_WORLD,
   there is no need to call this function. If
   the user-defined communication world is a sub-set of MPI_COMM_WORLD,
   this function should be called before FIO_open. 

 - Multi files which are all open must have the same 
   user-defined communication world.

 - FIO_comm_free should be called before the end of use
   of the user-defined communication world if FIO_comm_create
   was called. 

*/

int FIO_comm_create(int n, int *ranks);

#define FIO_COMM_FCREATE myfio_comm_fcreate_

void FIO_COMM_FCREATE(int *n, int *ranks, int *ierr);

/**
\ingroup mpi Functions Related to MPI      

 FIO_comm_free frees the allocation used in function FIO_comm_create.
 If FIO_comm_create was called, this
 function should be called before the end of use 
 of the user-defined communication world.

 Example 86 in Section 7.20 shows the usage of FIO_comm_create and
 FIO_comm_free.
*/
/* 
 EExample 86:  ctest_062_create_comm.c  
*/

int FIO_comm_free(); 

#define FIO_COMM_FFREE myfio_comm_ffree_

void FIO_COMM_FFREE(int *ierr); 

/**
\ingroup mpi Functions Related to MPI

 FIO_set_mpi_collective sets the collective or independent modes for
 MPI_IO. This function was written by Mike Gaeta.

 - file_id: an input, the id of a file

 - transfer_mode: input, the mode of MPI_IO, 0 for independent, and 1
   for collective.   

*/ 

int FIO_set_mpi_collective(int *file_id,
                           int *transfer_mode);

/**
\ingroup mpi Functions Related to MPI

 FIO_set_mpi_info sets keyword-value pairs for MPI_INFO members.
 This function was written by Mike Gaeta.

 - kw: an input, keyword.  

 - val: an input, value.  

 Example 87 in Section 7.20 shows the usage of FIO_set_mpi_info.
*/
/*
 EExample 87: ctest_063_set_mpinfo.c 
*/ 

int FIO_set_mpi_info(char *kw,
                       char *val);

/**
\ingroup mpi Functions Related to MPI

 Given the locations of pes in a pe-configuration,
 FIO_get_plist gets plist.

 - dims: an input, the dimensionality of a pe-configuration.

 - psizes: an input, an array with dims elements for the sizes of the
   pe-configuration.

 - myloc: an input, an array with dims elements for the location of
   current pe in the pe-configuration.

 - list: an output, an array of pe ranks for pe list which
   may be used in FIO_write, FIO_rwrite and FIO_smesh_write.

*/

int FIO_get_plist(long long *psizes,
                  int  *myloc,
                  int  dims,
                  long long *list);

#define FIO_FGET_PLIST myfio_fget_plist_

void FIO_FGET_PLIST(long long *psizes,
                    int  *myloc,
                    int  *dims, long long *list, int *ierr);

/**
\ingroup mpi Functions Related to MPI

 FIO_npes_used gets the number of pes used in creating a given object.

 - obj_id: an input, the id of an object

 - ncpus: an output, the number of pes used in creating the object and its 
   generations.

*/

int FIO_npes_used(const int obj_id, int *ncpus);

/**
\defgroup attributes Functions for Writing/Reading Attributes

 This section covers the functions to write and read attributes.

*/

/**
\ingroup attributes Functions for Writing/Reading Attributes

 FIO_attr_write writes an attribute to an object which may be a group,
 array, structured/unstructured mesh and variable. To write an attribute,
 all the pes are supposed to have the same values for the attribute.

 - attr_name: an input, the name of an attribute.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be 
   the id of any object
   in this file.

 - datatype: an input, the datatype of attribute values.

 - count: an input, the number of elements in buffer for attribute values.

 - buffer: an input, the starting address for the values of the attribute.

 - When datatype is udm_string or udm_char, buffer should be a char array,
   and count is the
   number of characters in the string which excludes the terminating
   character '\\0'.

 Example 88 in Section 7.21 shows the usage of FIO_attr_write.
*/
/*
 EExample 88: ctest_013_attr_write.c
 */

int FIO_attr_write(const char  *attr_name,
                   const int     object_id,
                   const char    *path,
                   UDM_DATA_TYPE datatype,
                   const int     count,
                   const void    *buffer);

/**
\ingroup attributes Functions for Writing/Reading Attributes

 FIO_attr_fread reads the values of an attribute attached to a given object. 

 - object_id: an input, the id of an object.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be 
   the id of any object
   in this file.

 - attr_name: an input, the name of an attribute.

 - datatype: an input, the datatype of attribute values.

 - buffer: an input, the starting address for the values of the attribute.

 - buffer has to be allocated before the call, for example,  
\verbatim
       char *buffer = (char *) malloc(100 * sizeof(char)); or
       char buffer[100];
       FIO_attr_fread(..., buffer);
\endverbatim

 - If an attribute is udm_string, declare buffer as a char
   array, for example,
\verbatim
       char buffer[size];
       FIO_attr_fread( ..., udm_string, buffer).
\endverbatim

 */

int FIO_attr_fread(const int     object_id,
                   const char    *path,
                   const char    *attr_name,
                   UDM_DATA_TYPE datatype,
                   void          *buffer);

/*  
FIO_cycle_write and FIO_cycle_read are not supposed to be used 

\ingroup attributes Functions for Writing/Reading Attributes

 FIO_cycle_write writes the cycle number and/or the time of
 simulation to a given object. In the UDM library, these two values
 will be stored as an attribute of the given object. 

 - object_id: an input, the id of an object.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be
   the id of any object
   in this file.

 - time: any non-negative double to be considered as simulation time,
   a negative value will be ignored. 

 - cycle: any non negative long long to be considered as a cycle number.
   a negative value will be ignored. 

 */

int FIO_cycle_write(const int  object_id, const char *path,
                      const double time, const long long cycle);

 /*  
\ingroup attributes Functions for Writing/Reading Attributes

 FIO_get_cycle reads the cycle number and/or the time of
 simulation from a given object.

 - object_id: an input, the id of an object.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be
   the id of any object
   in this file.

 - time: the simulation time. A negative value of time indicates that
   time is not set to the object.

 - cycle: the simulation cycle. A negative value of time indicates that
   the cycle is not set to the object.

 Example 89 in Section 7.21 shows the usage of FIO_cycle_write and 
 FIO_get_cycle. 
*/
/* 
 EExample 89: ctest_068_time_cycle.c
*/

int FIO_get_cycle(const int  object_id, const char *path,
                    double *time, long long *cycle); 

/**
\ingroup attributes Functions for Writing/Reading Attributes

 FIO_attr_read reads a given attribute of a given object.

 - object_id: an input, the id of an object.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be 
   the id of any object
   in this file.

 - attr_name: an input, the name of an attribute.

 - datatype: an input, the datatype of attribute values. 

 - count: an input or output, the number of elements for attribute values.

 - buffer: an input or output, the starting address of attribute values.
 
 - If buffer is not allocated before the call, set it to null, 
   for example, 
\verbatim
       char *buffer = NULL;
       FIO_attr_read(..., &buffer);
\endverbatim
   and free it after use, for example, 
\verbatim
   if (buffer) free(buffer).
\endverbatim

 - buffer may be allocated before the call, for example,  
\verbatim
       char *buffer = (char *) malloc(100 * sizeof(char));.
       FIO_attr_read(..., &buffer).
\endverbatim

 - buffer may be statically allocated before the call, for example,
\verbatim
       char p[100]; 
       char *buffer = p;
       FIO_attr_read(..., &buffer).
\endverbatim

 -  An example for the case with an attribute which has only a single value is
\verbatim
       int *buffer = NULL; 
       FIO_attr_read(..., &buffer);
\endverbatim
    or 
\verbatim
       int  value;  
       int *buffer = &value;
       FIO_attr_read(..., &buffer).
\endverbatim

 - If datatype is udm_string or udm_char, for example, do the following
\verbatim
      char *buffer = NULL;  // or
      char *buffer = (char *) malloc(nchar * sizeof(char));
      FIO_attr_read(..., udm_string, &count, &buffer);   // or
      FIO_attr_read(..., udm_char,   &count, &buffer);
\endverbatim
   The output count is the number of characters in the char
   array, which  does not include the terminating char '\\0'.

 Example 90 in Section 7.21 shows the usage of FIO_attr_fread and FIO_attr_read.
*/
/*
 EExample 90: ctest_014_attr_read.c 
 */

int FIO_attr_read(const int     object_id,
                  const char    *path,
                  const char    *attrName,
                  UDM_DATA_TYPE datatype,
                  int           *count,
                  void          *buffer);

/**
\ingroup attributes Functions for Writing/Reading Attributes

 FIO_get_nattr gets the number of attributes which are attached to a given
 object.

 - object_id: an input, the id of an object.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be 
   the id of any object
   in this file.

 - filter: an input, a char array for the name to be matched with.

 - nattr: an output, the number of attributes.

 - If filter is null, this function gets the number of attributes attached to 
   the object. If filter is not
   null, the number of attributes includes only those with
   their names matched with filter.

 */

int FIO_get_nattr(const int object_id,
                  const char *path_and_name,
                  const char *filter,
                  int  *nattr);

/**
\ingroup attributes Functions for Writing/Reading Attributes

   FIO_get_attr_info gets the numbers of characters in attribute
   names, the datatypes of attribute values, and the sizes of
   arrays for attribute values.

 - object_id: an input, the id of an object.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be 
   the id of any object
   in this file.

 - nattr: an input, the number of attributes.
  
 - filter: an input, a char array for the name to be matched with.

 - sizes_in_name: an output, an array of nattr long for the sizes of
   char arrays of each attribute name, which include the terminating char '\\0'..

 - datatypes: an output, an array of nattr long for the datatypes of 
   attribute values.

 - sizes_in_value: an output, an array of nattr long for the sizes of
   attribute values, which excludes any terminating char '\\0' if
   datatype is udm_char or udm_string.

 - If filter is null, this function gets the number of attributes attached to
   the object. If filter is not null, this function returns the information of only those
   attributes with their names matched with filter. 

 - sizes_in_name, datatypes, and sizes_in_value should be allocated before
   the call, for example, 
\verbatim
   int *sizes_in_name = (int *)malloc(nattr * sizeof(int));
   FIO_get_attr_info(..., sizes_in_name, datatypes, sizes_in_value);
\endverbatim

 */

int FIO_get_attr_info(const int object_id,
                      const char *path_and_name,
                      const char *filter,
                      const int nattr,
                      int *sizes_in_name,
                      UDM_DATA_TYPE *datatypes,
                      int *sizes_in_value);

/**
\ingroup attributes Functions for Writing/Reading Attributes

 FIO_get_attr_names gets names of all the attributes attached to a given object.

 - object_id: an input, the id of an object.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be 
   the id of any object
   in this file.

 - nattr: an input, the number of attributes.

 - names: an output, an array with nattr elements for nattr attribute names.
 
 - names and names[i] (i = 0; ..., i < nattr) should be allocated
   before the call, for example,
\verbatim
       char **names = (char **) malloc(nattr * sizeof(char*));
       for (i = 0; i < nattr; i++)
         { char *names[i] = (char *) malloc(sizes_in_name[i] * sizeof(char));
          }
       FIO_get_attr_names(..., names).
\endverbatim

 Example 91 in Section 7.21 shows the usage of FIO_get_nattr, FIO_get_attr_info
 and FIO_get_attr_names.
*/
/*
 EExample 91: ctest_015_get_attr_info.c 
 */

int FIO_get_attr_names(const int object_id,
                       const char *path_and_name,
                       const int nattr,
                       char  **names);

/** 
\ingroup attributes Functions for Writing/Reading Attributes
 
 FIO_get_attrs gets names, datatypes, and values of all the attributes
 attached to a given object.

 - object_id: an input, the id of an object.

 - path_and_name: an input, the path and name of an object.
   If the leading char in path_and_name is not '/',
   path_and_name is relative to object_id. Examples of path_and_name
   are "grp1", "grp1/sgrp2/ssgrp3",
   and "grp1/sgrp2/ssgrp3/". If there is a leading '/'
   in path_and_name, such as /grp1/grp2/mygrp, the path is an absolute
   path, relative to the root. In this case, object_id may be 
   the id of any object
   in this file.

 - nattr: an input, the number of attributes.

 - filter: an input, a char array for the name to be matched with.

 - names: an output, an array with nattr elements for the names of
   nattr attributes.

 - counts: an output, an array with nattr elements for the sizes of values of
   attributes.

 - datatypes: an output, an array with nattr elements for the datatypes
   of attributes values.

 - values: an output, an array with nattr elements for the starting addresses
   of attribute values. 

 - If filter is null, this function gets the names, datatypes and values
   of all the attributes.
   If filter is not null, the function gets the information of only those
   attributes whose names match with filter.

 - nattrs should be provided before the call, but
   names, counts, datatypes and values may be set to null
\verbatim
       void **values = NULL;
       char **names = NULL;
       int  *counts = NULL;
       UDM_DATA_TYPE *types = NULL;
       FIO_get_attrs(..., nattr, &names, &counts, &types, &values);
\endverbatim

 - If any one of values, names, counts and types is NULL before the call,
   this function will allocate the memories for all these arrays.
   After use, a clean-up is needed, for example,
\verbatim
       for (i = 0; i < nattr; i++)
         { free(names[i]);
           free(values[i]);
          }
       if (nattr)
          { free(values);
            free(names);
            free(counts);
            free(types);
           }
\endverbatim

 - The output, values, should be interpreted according to datatypes,

 - If datatypes[i] is udm_char or udm_string, counts[i] is the number of
   characters in the char array excluding the termination char '\\0'.

 Example 92 in Section 7.21 shows the usage of FIO_get_attrs.
*/
/* 
 EExample 92: ctest_016_get_attr.c 
 */

int FIO_get_attrs(const int     object_id,
                  const char    *path_and_name,
                  const char    *filter,
                  const int     nattr,
                  char          ***names,
                  int           **counts,
                  UDM_DATA_TYPE **datatypes,
                  void          ***vals);

/**
\defgroup mis Functions for Miscellaneous Use 

 This section covers the functions for some miscellaneous uses.

*/

/**
\ingroup mis Functions for Miscellaneous Use 

 FIO_dstruct_free frees the allocations in an object of UDM_DATA_STRUCT.

 - ds: an input.

*/

int FIO_dstruct_free(UDM_DATA_STRUCT ds);

/**
\ingroup mis Functions for Miscellaneous Use 

FIO_get_meshcoord reads coordinates of a part of mesh. 
This function has not been implemented yet.  
*/ 

int FIO_get_meshcoord(const int mesh_id,
                      int dims, long long offset_node,
                      long long nnodes, UDM_DATA_TYPE datatype,
                      void *coord1, void *coord2, void *coord3);
/**
\ingroup mis Functions for Miscellaneous Use 

 FIO_extents gets the extents of an object which may
 be a group or mesh. If the object a 
 group or file, this function will return
 the extents of all meshes under this group or file.
 If the object is a mesh, this function returns the extents
 of the mesh.
 If the object is an array or a variable, this function does
 nothing.

 - object_id: an input, the id of an object.

 - extents: an output, the extent of coordinates.

 - dims: an output, the dimensionality of coordinates.  

 - extents[0] and extents[1] are the minimum and maximum of
   of the coordinate in dimension 0, extents[2] and extents[3] are
   the minimum and maximum of the coordinate in dimension 1, and extents[4]
   and extents[5] are the minimum and maximum of the coordinate
   in dimension 2.
   If the dimensions of the meshes involved are not more than 2 (or 1),
   the maximum
   and the minimum coordinates in dimension 2 (and dimension 1) are set to zero.

 Example 93 in Section 7.22 shows the usage of FIO_extents.
*/
/*
 EExample 93: ctest_062_mesh_n_shared_x_extent.c 
*/

int FIO_extents(const int object_id, double extents[6], int *dims);


/**
\ingroup mis Functions for Miscellaneous Use

  FIO_mesh_extents gets the extents of the part of a mesh in each
orginal pe. 

  - mesh_id:  an input, the id of a mesh, either structured or unstructured.

  - extents:  an output, an array of pointers. The length of the array
    is npes_used, and each pointer points to an array of 6 doubles.
    extents[0][0] and extents[0][1] are the minimum and maximum of 
    of the coordinate in dimension 0 for the part of mesh which was written
    through pe = 0, extents[0][2] and extents[0][3] are those in
    dimension 1, and extents[0][4] and extents[0][5] are those in
    dimension 2. The array extents[1] is for the part of mesh which was
    written through pe = 1, and so on. If the mesh is 2D (or 1D), the maximum
    and the minimum coordinates in dimension 2 (and dimension 1) are set to zero.  
    
  - npes_used: an input, the number of pes used in writing the mesh, which
    may be obtained through function FIO_npes_used.
 
  - dims: an output, the dimension of the mesh, which is dims in UDM_STRUCTURED_MESH
    or UDM_UNSTRUCTURED_MESH.
 */

int FIO_mesh_extents(const int meshid, double *extents[],
                       int npes_used, int *dims);

/**
\ingroup mis Functions for Miscellaneous Use 

 FIO_get_all_nitems gives the number of groups or arrays, which match with both type,
 and name, under group_id. 

 - group_id: an input, the id of a group
 
 - type: an input, udm_group or udm_dataset. udm_atrribute of UDM_OBJECT_TYPE is excluded. 

 - name: an input, the name any of children should match with in order to be selected.
   If name is set to null, this function gives the number of objects
   which match with only type. 

 - nitems: an output, the number of children which match with both type 
   and name under group_id.

 Example 94 in Section 7.22 shows the usage of FIO_get_all_nitems.
*/
/*
 EExample 94: ctest_064_get_all_nitems.c
*/ 

int FIO_get_all_nitems(int           group_id,
                       UDM_OBJECT_TYPE type,
                       const char      *name,
                       int             *nitems);


/**
\ingroup mis Functions for Miscellaneous Use

 FIO_fget_all_obj will list the information about the children under a
 given group which match with the given type and name. 

 - group_id: an input, the id of a group

 - type: an input, udm_group or udm_dataset. udm_atrribute of UDM_OBJECT_TYPE is excluded.

 - name: an input, the name any of children should match with in order to be selected.
   If name is set to null, this function gives the number of objects
   which match with only type.

 - nitems: an input, the length of the array items for
   the number of children matching with the given type and name.
   nitems may be obtained through the previous function.

 - items: an output, an array of UDM_LIST_STRUCT with nitems elements.

 - items must be allocated before the call.

 - The following fields of UDM_LIST_STRUCT for items will be available after
   the call, name, objtype and objID. 

 - If type is udm_dataset, datatype and the following fields of dstruct
   will also be given after the call, dims, sizes,
   offsets, gsizes, offsets and multiple. 
   For this case, the following fields of returned dstruct
   have the following values.
   If the array was written through FIO_rwrite,
   sizes[j] = gsizes[j], offsets[j] = 0,
   (j = 0, 1, ..., dims-1), and fsizes = NULL.
   If the array was written through FIO_write and the number of
   pes calling this function is different from the number
   of pes used when the array was written, dstruct has the values of the
   first pe in which sizes[j] is not zero for all j.
   If the array was written through FIO_write, and the number of
   pes calling this function is the same as the number
   of pes used when the array was written,
   sizes, offsets, fsizes will be the same
   as those in the original pe.
*/

int FIO_fget_all_obj(int group_id,
                    UDM_OBJECT_TYPE type,
                    const char *name,
                    const int  nitems,
                    UDM_LIST_STRUCT *items);

/**
\ingroup mis Functions for Miscellaneous Use 

 FIO_get_all_obj will list the information about the children under a
 given group which match the given type and name.

 - group_id: an input, the id of a group

 - type: an input, udm_group or udm_dataset.
 
 - name: an input, the name any of children should match with in order to be 
   selected. If name is set to null, this function gives the number of objects
   which match with only type.

 - nitems: an output, the number of children matching with the given type and 
   name.

 - items: an output, an array of UDM_LIST_STRUCT with nitems elements. 

 - items should not be allocated before the call, but it should be cleaned
   after use, for example, 
\verbatim
       UDM_LIST_STRUCT *items;
       int  nitems;
       FIO_get_all_obj(..., &nitems, &items);
       for (i = 0; i < nitems; i++)
         { free(items[i].name);
           if (type == udm_dataset)
              { if (items[i].dstruct.gsizes)   free(items[i].dstruct.gsizes);
                if (items[i].dstruct.offsets)  free(items[i].dstruct.offsets);
                if (items[i].dstruct.sizes)    free(items[i].dstruct.sizes);
                if (items[i].dstruct.fsizes)   free(items[i].dstruct.fsizes);
               }
          }
       if (nitems) free(items).
\endverbatim

 - The following fields of UDM_LIST_STRUCT for items will be available after
   the call, name, objtype and objID. 

 - If type is udm_dataset, datatype and
   the following fields of dstruct s
   will also be given after the call, dims, sizes,
   fsizes, offsets, gsizes and multiple.
   For this case, the following fields of returned dstruct
   have the following values.
   If the array was written through FIO_rwrite,
   sizes[j] = gsizes[j], offsets[j] = 0,
   (j = 0, 1, ..., dims-1), and fsizes = NULL.
   If the array was written through FIO_write and the number of
   pes calling this function is different from the number
   of pes used when the array was written, dstruct has the values of the
   first pe in which sizes[j] is not zero for all j.
   If the array was written through FIO_write, and the number of
   pes calling this function is the same as the number
   of pes used when the array was written,
   sizes, offsets, fsizes will be the same
   as those in the original pe.
 
 Example 95 in Section 7.22 shows the usage of FIO_fget_all_obj and
 FIO_get_all_obj.
*/
/*
 EExample 95: ctest_065_get_all_obj.c 
*/ 

int FIO_get_all_obj(int group_id,
                    UDM_OBJECT_TYPE type,
                    const char *name,
                    int  *nitems,
                    UDM_LIST_STRUCT **items);

/**
\ingroup mis Functions for Miscellaneous Use

 FIO_get_path gives a path of an object relative to a given group.

 - group_id: an input, the id of a group

 - object_id: an input, the id of an object under the given group.

 - path: an output, the path of object_id relative to group_id, a typical
   output is path = grpA/grpB/grpC/

 Example 96 in Section 7.22 shows the usage of FIO_get_path.
*/
/*
 EExample 96: ctest_066_get_path.c 
*/

int FIO_get_path(int   group_id,
                 int   object_id,
                 char  **path);

/**
\ingroup mis Functions for Miscellaneous Use

 FIO_get_ancestors gives all the ids of ancestors for a given id of object.

 - object_id: an input, the id of an object. Currently, it can be only the id of
   an udm_group or udm_dataset.

 - ancestors: an output, an array with num elements.

 - num: an output, the number of groups above object_id.

 - If ancestors is not allocated before the call, set it to  null, for example,
\verbatim
      int *ancestors = NULL;
      FIO_get_ancestors(..., &ancestors, &n);
\endverbatim

 - ancestors may be allocated before the call if the size is known before the
   call, for example,
\verbatim
      int *ancestors = (int *) malloc(size * sizeof(int));
      FIO_get_ancestors(..., &ancestors, &n);
\endverbatim

 - In the output, ancestors[0], ..., ancestors[n-1] are the
   root id, ..., and the parent id of the object.

 Example 97 in Section 7.22 shows the usage of FIO_get_ancestors.
*/
/*
 EExample 97: ctest_067_get_ancestor.c
*/

int FIO_get_ancestors(int object_id,
                      int **ancestors,
                      int *num);
/**
\page appdx Example Codes

 The following sections in this chapter will give example codes 
 for the usage of interface functions.

 \section filesec File Open/Close, Group Open 

 This section shows the usage of FIO_open, FIO_group_open
 and FIO_close.

 \include ctest_001.c

\section dswritesec Array Write 

 This section shows the usage of FIO_dstruct_init, FIO_write, and FIO_rwrite.

 \include ctest_002.c
 \include ctest_003.c
 \include ctest_004.c
 \include ctest_005.c
 \include ctest_006.c
 \include ctest_007.c
 \include ctest_008_1Darray_3cw.c
 \include ctest_009_2Darray_0aw.c

\section dsreadsec Array Read

 This section shows the usage of FIO_get_ds and FIO_fread.

 \include ctest_010_1Dwrite_getds_fread.c
 \include ctest_010_3Dwrite_getds_fread.c

\section poolsec Arrays in Container

 This section shows the usage of the functions for writing/reading
 arrays in a container.

 \include ctest_011_pool.c

\section listsec Array and Group List  

 This section shows the usage of FIO_list_nitems and FIO_list.

 \include ctest_012_list.c

\section smeshwritesec Structured Mesh Write

 This section shows the usage of FIO_smesh_write.

 \include ctest_016a_smesh_2Dnonunif_w.c
 \include ctest_016c_smesh_2Dunif_w.c
 \include ctest_017a_smesh_1w_nonuniform.c
 \include ctest_017a_smesh_2w_nonuniform.c
 \include ctest_018_smesh_3w_uniform.c
 \include ctest_018b_w_smesh2D_uniform.c

\section smeshvarwritesec Structured Mesh Variable Write

 This section shows the usage of FIO_smeshvar_write.

 \include ctest_019a_smeshvar_1aw.c
 \include ctest_020_smeshvar_2w.c

\section smeshreadsec Structured Mesh Read

 This section shows the usage of FIO_get_smeshsize_on_pe, FIO_get_smeshsize,
 and FIO_get_smesh.

 \include ctest_018b_smeshsize_on_pe.c
 \include ctest_017b_smesh_2r_nonuniform.c
 \include ctest_018b_smesh_4r_uniform.c

\section smeshvarreadsec Structured Mesh Variable Read

 This section shows  the usage of FIO_fread_smeshvar.

 \include ctest_016b_smesh_2Dnonunif_r.c
 \include ctest_016d_smesh_2Dunif_r.c
 \include ctest_018c_r_smesh2D_uniform.c
 \include ctest_023_smeshvar_1br.c
 \include ctest_024_smeshvar_1cr.c

\section smeshvarinfosec Structured Mesh and Variable Information

 This section shows the usage of FIO_get_smesh_info, 
FIO_get_nvars and FIO_get_var_info.

 \include ctest_025_smesh_get_meshinfo.c
 \include ctest_026_smesh_get_varinfo.c

\section meshwritesec Unstructured Mesh Write

 This section shows the usage of FIO_mesh_init and FIO_mesh_write.

 \include ctest_027a_mesh_e_n_1aw.c
 \include ctest_028_mesh_e_n_mixed_1aw.c
 \include ctest_029a_mesh_e_n_1aw.c
 \include ctest_030_mesh_f_e_n_mixed_1aw.c
 \include ctest_031_mesh_f_e_n_x_1aw.c
 \include ctest_032_mesh_f_n_1aw.c
 \include ctest_033_mesh_f_n_mixed_1aw.c
 \include ctest_034_mesh_n_1aw.c
 \include ctest_035_mesh_n_gid_1aw.c
 \include ctest_036_mesh_n_x_1aw.c
 \include ctest_037a_mesh_n_x_mixed_1aw.c
 \include ctest_038_mesh_zone_face_mixed_1aw.c
 \include ctest_039_mesh_3D_face_n.c
 \include ctest_040_mesh_3D_face_x.c
 \include ctest_041_mesh2D_e_n_1aw.c
 \include ctest_042_mesh2D_e_n_mixed_1aw.c
 \include ctest_043_mesh2D_n_1aw.c
 \include ctest_044_mesh2D_n_mixed_1aw.c
 \include ctest_043a_mesh2D_n_1aw.c
 \include ctest_044b_pentagon_w.c
 \include ctest_053_mesh_zfn_g_w.c

 \include ctest_045_mesh_n_and_x.c
 \include ctest_046a_mesh_n_shared_x.c

 \section meshappendsec Unstructured Mesh Append 

 This section shows the usage of FIO_mesh_create, FIO_mesh_append,
  and FIO_mesh_close.

 \include ctest_056_mesh_append_zn_w.c
 \include ctest_057_mesh_append_zfn_w.c

 \section meshvarwritesec Unstructured Mesh Variable Write

 This section shows the usage of FIO_meshvar_write.

 \include ctest_047_meshvar_0a_w.c
 \include ctest_048a_meshvar_mixed_0w.c
 \include ctest_049a_mesh_n_shared_nodevar_1aw.c
 \include ctest_050a_meshvar_z_f_e_n_2aw.c
 \include ctest_051_mixed_meshvar_z_f_e_n_2aw.c
 \include ctest_052_mixed_meshvar2D_e_n_1aw.c
 \include ctest_053_meshvar_pentagon_w.c
 \include ctest_054_meshvar_zn_w.c
 \include ctest_054b_meshvar_zfn_w.c
 \include ctest_055_meshvar_zfn_g_w.c

 \section meshvarcreatesec Unstructured Mesh Variable Append

 This section shows the usage of FIO_meshvar_create and
   FIO_meshvar_append.

 \include ctest_058_meshvar_append_zfn_w1.c
 \include ctest_059_meshvar_append_zfn_w2.c

 \section meshreadsec Unstructured Mesh Read

 This section shows the usage of FIO_get_meshdef, FIO_get_meshsize,
 FIO_get_meshsize_on_pe, and FIO_get_mesh.

 \include ctest_027b_mesh_e_n_1br.c
 \include ctest_029b_mesh_f_e_n_1br.c
 \include ctest_037b_mesh_read_1.c
 \include ctest_046b_mesh_n_shared_x_r.c
 \include ctest_048b_meshvar_mixed_1r.c
 \include ctest_041b_mesh_read2D.c
 \include ctest_051a_mesh_read_whole.c
 \include ctest_051b_mesh_read_pe.c
 \include ctest_051c_mesh_read_part.c

 \section meshvarreadsec Unstructured Mesh Variable Read

 This section shows the usage of FIO_fread_meshvar.

 \include ctest_049b_mesh_n_shared_nodevar_2ar.c
 \include ctest_050b_meshvar_z_f_e_n_2br.c
 \include ctest_051d_meshvar_read_whole.c
 \include ctest_051e_meshvar_read_pe.c
 \include ctest_051f_meshvar_read_part.c

 \section meshinfosec Unstructured Mesh and Variable Information

 This section shows the usage of FIO_nmesh and FIO_get_mesh_info.

 \include ctest_050c_meshvar_z_read.c

 \section meshlistsec Mesh List

 This section shows the usage of FIO_meshlist_nitems and FIO_meshlist.

 \include ctest_048c_meshvar_mixed_2r.c
 \include ctest_019b_smesh_meshlist.c

 \section mpisec Functions Related to MPI

 This section shows the usage of FIO_comm_create,
 FIO_comm_free, and FIO_set_mpi_info.

 \include ctest_062_create_comm.c
 \include ctest_063_set_mpinfo.c

\section attrsec Attribute Write and Read 

 This section shows the usage of FIO_attr_write, FIO_attr_fread, FIO_attr_read, 
 FIO_get_nattr, FIO_get_attr_info, FIO_get_attr_names, and FIO_get_attrs.

 \include ctest_013_attr_write.c
 \include ctest_014_attr_read.c
 \include ctest_015_get_attr_info.c
 \include ctest_016_get_attr.c

\section missec Miscellaneous Usages

 This section shows the usage of FIO_extents, FIO_get_all_nitems,
 FIO_fget_all_obj, FIO_get_all_obj, FIO_get_path, and FIO_get_ancestors.

 \include ctest_062_mesh_n_shared_x_extent.c
 \include ctest_064_get_all_nitems.c
 \include ctest_065_get_all_obj.c
 \include ctest_066_get_path.c
 \include ctest_067_get_ancestor.c
*/

/************************************************************************
 This part is generated for the connection to GMV 
************************************************************************/
/**
\ingroup mis Functions for Miscellaneous Use

 FIO_meshsize_per_coord give the number of meshes on a given pe, and
 each of mesh has its own set of coordinates. This function also gives
 the general information of the meshes, and the variables defined on the
 meshes. 

 - file_id: input, the id of a file.
 
 - perank:  input, a rank of an original pe which wrote the file. The
   possible value of perank may be obtained through FIO_npes_used.

 - nmesh: output, the number of mesh.

 - names: output, a list of mesh names with a length nmesh.
 
 - coord_ids: output, an array with a length of nmesh for each of meshes.

 - msizes: output, an array with a length of nmesh. 

 - meshs:  output, an array with a length of nmesh.

 - nvars_each_mesh: output, an array with a length of nmesh for the numbers
   of variables defined on each mesh.

 - varnames_each_mesh: output, varnames_each_mesh[k][v] will be the name
   of the v-th variable for the k-th mesh. 

 - vars_each_mesh: output, varnames_each_mesh[k][v] will be the variable of
   the v-th variable for the k-th mesh.
 
 - This function will allocate the memory for names, coord_ids, meshs, 
   nvars_each_mesh, varnames_each_mesh, and vars_each_mesh, and users are 
   expected to free the memory after use.

\verbatim
    int  pe, nmesh, k, v, nvars; 
    int  *coord_ids, *nvars_each_mesh;
    char **names;
    char ***varnames_each_mesh;     
    UDM_UNSTRUCTURED_MESH *meshs;
    UDM_UNSTRUCTURED_MESH_SIZE *msizes;
    UDM_MESH_VAR **vars_each_mesh; 
    
    pe = 0;
    FIO_meshsize_per_coord(file_id, pe, &nmesh, &names, &coord_ids, &msizes, 
                           &meshs, &nvars_each_mesh, &varnames_each_mesh, 
                           &vars_each_mesh);
    if (nmesh) { 
        free(coord_ids);
        free(msizes);
        free(meshs);

        for (k = 0; k < nmesh; k++) { 
            nvars = nvars_each_mesh[k];
            if (nvars)  { 
                for (v = 0; v < nvars; v++) { 
                    if (varnames_each_mesh[k][v]) 
                      free(varnames_each_mesh[k][v]);
                }
                if (varnames_each_mesh[k]) free(varnames_each_mesh[k]);
                if (vars_each_mesh[k])     free(vars_each_mesh[k]);
            }
            if (names[k]) free(names[k]);
        }
        if (names) free(names);
        if (nvars_each_mesh)    free(nvars_each_mesh);
        if (varnames_each_mesh) free(varnames_each_mesh);
        if (vars_each_mesh)     free(vars_each_mesh);
    }
\endverbatim
*/
/*
 Examples: gmv_test_1.c, gmv_test_2.c
*/

int FIO_meshsize_per_coord(const int file_id, const int perank, int *nmesh,
                           char ***names, int **coord_ids,
                           UDM_UNSTRUCTURED_MESH_SIZE **msizes,
                           UDM_UNSTRUCTURED_MESH **meshs,
                           int **nvars_each_mesh,
                           char ****varnames_each_mesh,
                           UDM_MESH_VAR ***vars_each_mesh);
/**
\ingroup mis Functions for Miscellaneous Use

  FIO_get_mesh_per_coord reads a part of mesh, and this part of mesh was 
  written through one pe. 

 - file_id: input, the id of a file
 
 - coord_id: input, the id of set of coordinates. The possible values for 
   coord_id may be obtained through FIO_meshsize_per_coord. 

 - perank:  input, a rank of an original pe which wrote the file. The
   possible value of perank may be obtained through FIO_npes_used.
 
 - name: input, the name of a mesh. The possible names may be obtained through
   FIO_meshsize_per_coord.

 - mesh: input and output. The members of mesh, datatype_mesh and 
   datatype_coord, must be set before the call. 

 - The arrays for connectivity and coordinates may either be or not be allocated
   before the call. If they are not allocated before the call, users have to set
   them to null which may be done through FIO_mesh_init. In this case, this
   function will allocate the neccessary memory, and users are expected to free
   them after use.   

\verbatim
   UDM_UNSTRUCTURED_MESH mesh;
   FIO_mesh_init(&mesh);
   FIO_get_mesh_per_coord(file_id,  coord_id,  pe_rank, mesh_name, &mesh);

   if (mesh.nodelist_for_zone)  free(mesh.nodelist_for_zone);
   if (mesh.edgelist_for_zone)  free(mesh.edgelist_for_zone);
   if (mesh.facelist_for_zone)  free(mesh.facelist_for_zone);
   if (mesh.nodelist_for_face)  free(mesh.nodelist_for_face);
   if (mesh.edgelist_for_face)  free(mesh.edgelist_for_face);
   if (mesh.num_faces_for_zone) free(mesh.num_faces_for_zone);
   if (mesh.num_edges_for_zone) free(mesh.num_edges_for_zone);
   if (mesh.num_nodes_for_zone) free(mesh.num_nodes_for_zone);
   if (mesh.num_edges_for_face) free(mesh.num_edges_for_face);
   if (mesh.num_nodes_for_face) free(mesh.num_nodes_for_face);
   if (mesh.coord[0])           free(mesh.coord[0]);
   if (mesh.coord[1])           free(mesh.coord[1]);
   if (mesh.coord[2])           free(mesh.coord[2]);
\endverbatim
*/
int FIO_get_mesh_per_coord(const int file_id, const int coord_id, 
                           const int perank, const char *name,  
                           UDM_UNSTRUCTURED_MESH *mesh);
/**
\ingroup mis Functions for Miscellaneous Use

FIO_get_meshvar_per_coord reads a set of variables defined on a part of mesh 
which was written on one pe.  

 - file_id: input, the id of a file

 - coord_id: input, the id of set of coordinates. The possible values for 
   coord_id may be obtained through FIO_meshsize_per_coord.

 - perank:  input, a rank of an original pe which wrote the file. The
   possible value of perank may be obtained through FIO_npes_used.

 - meshname: input, the name of a mesh in which the set of variables are 
   defined.  The possible names may be obtained through FIO_meshsize_per_coord.

 - varnames: input, the names of the set of variables.

 - vars: input and output, an array of variables. Each component of a vector or
   tensor is considered one variable in the array. The following members of each
   variable must be given before the call, type, rank, 
   comp_sizes and components (if rank > 0), and datatype. The member, buffer, of
   each variable may either be or not be allocated before the call. If buffer is
   not allocated, users must explicitly set buffer to NULL before the call. In
   this case, this function will allocate the neccessary memory, and users are
   expected to free the memory through free().  

 - nvars: input, the size of array, vars.  
*/ 
int FIO_get_meshvar_per_coord(const int file_id, const int coord_id, 
                              const int perank, const char *meshname,
                              char **varnames, UDM_MESH_VAR *vars,
                              const int nvars);

/************************************************************************/
/************************************************************************/ 
/*
* int FIO_get_path_old(int  group_id,
*                      char        *name,
*                      UDM_OBJECT_TYPE type,
*                      char        **path);
*/

/*------------------------------------------------------------------------
* Function FIO_get_nitems
*           input: group_id, type, nitems
*           output: nitems
* Notes:
*   This function gives the number of objects, which is either udm_group
*   or udm_dataset, under group_id.
*                                                                        */
/*
*int FIO_get_nitems(int         group_id,
                    UDM_OBJECT_TYPE type,
                    int         *nitems);
*/
/*------------------------------------------------------------------------
* Function FIO_list_all
*           input: group_id, type, nitems
*           output: items
* Notes:
*   This function lists all items of a given type, udm_group or
*   udm_dataset, under group_id. The name of each items, items[i].name,
*   is the path of this object relative to group_id. The memory of items
*   should be allocated before the call.
*   UDM_LIST_STRUCT *items = (UDM_LIST_STRUCT *)
*                            malloc( nitems * sizeof(UDM_LIST_STRUCT));
*   Currently, type can not be udm_attr.
*                                                                       */  
/* int FIO_list_all(int group_id,
                    UDM_OBJECT_TYPE type,
                    const int nitems,
                    UDM_LIST_STRUCT *items);
*/  
/*--------------------------------------------------------------------------
* Function FIO_get_all_names
*         input:  group_id, type
*         output: nitems, items
* Notes:
*   This function lists all items of a given type, udm_group or udm_dataset,
*   under group_id. The name of each items, items[i].name, is the path of this
*   object relative to group_id. The memory of items don't have to be allocated.
*       (a) usage:
*                  UDM_LIST_STRUCT *items;
*                  int  nitems;
*                  FIO_get_all_names(..., &nitems, &items);
*       (b) This function returns all items of this type under group_id,
*           which include all grand children.
*       (e) free after use
*              for (i = 0; i < nitems; i++)
*                { free(items[i].name);
*                  if (type == udm_dataset)
*                     { if (items[i].dstruct.gsizes)   
*                         free(items[i].dstruct.gsizes);
*                       if (items[i].dstruct.offsets)  
*                         free(items[i].dstruct.offsets);
*                       if (items[i].dstruct.sizes)    
*                         free(items[i].dstruct.sizes);
*                       if (items[i].dstruct.fsizes)   
*                         free(items[i].dstruct.fsizes);
*                      }
*                 }
*              free(items);
*       (f) Currently, type can not be udm_attr
                                                                         */
/* int FIO_get_all_names(int group_id,
                         UDM_OBJECT_TYPE type,
                         int  *nitems,
                         UDM_LIST_STRUCT **items);
*/ 
/*------------------------------------------------------------------------
* Function FIO_get_all_path
*         input:  group_id, name, type, n, path
*         output: path
* Notes:
*       (a) usage:
*                  char **allpath = NULL;
*                  int  n = 0;
*                  FIO_get_all_path(..., &n, &allpath);
*       (b) This function gives all the paths relative to group_id.
*       (c) The returned n is the number of paths returned.
*       (d) A returned path includes name in the path.
*       (e) free after use
*              for (i = 0; i < *n; i++) free(allpath[i]);
*              free(allpath);
*       (f) Currently, type can not be udm_attr
*                                                                         */
/*******************************************************************************/
/*******************************************************************************/
#ifdef __cplusplus
}
#endif 
#endif
