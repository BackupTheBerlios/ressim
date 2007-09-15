###############################################
# Makefile for the native numerical interface #
###############################################

#
# Set this to the root hierarchy where you want the libraries to install in
#
INSTALLROOT = /usr/local

#
# Set this to the root of your Java SDK installation
#
JAVAROOT =  $(INSTALLROOT)/java

#
# Modify these include paths to correspond to your installation
#
JNI_INCLUDE = -I$(JAVAROOT)/include -I$(JAVAROOT)/include/linux
C_INCLUDE   = -I$(INSTALLROOT)/include
LD_LIB      = -L$(INSTALLROOT)/lib \
	-lblas -lcblas -lclapack -lf2c -lm

#
# Check the compiler, linker options and names
#
CC      = gcc -c -fPIC -O2 -Wall $(JNI_INCLUDE) $(C_INCLUDE)
LD      = ld -shared $(LD_LIB)
JAVAC   = $(JAVAROOT)/bin/javac
INSTALL = install
RM      = rm -f

#
# No need to modify below
#----------------------------------------------------------------------

NAMESPACE = no.uib.cipr.matrix.nni
NNIPATH   = no/uib/cipr/matrix/nni
NNIPREFIX = no_uib_cipr_matrix_nni

all: blas lapack

install: all
	$(INSTALL) libnni_blas.so $(INSTALLROOT)/lib
	$(INSTALL) libnni_lapack.so $(INSTALLROOT)/lib

blas: libnni_blas.so 

lapack: libnni_lapack.so

$(NNIPATH)/BLAS.class: $(NNIPATH)/BLAS.java
	$(JAVAC) $(NNIPATH)/BLAS.java

$(NNIPREFIX)_BLAS.o: $(NNIPATH)/BLAS.class $(NNIPREFIX)_BLAS.c
	$(CC) $(NNIPREFIX)_BLAS.c

libnni_blas.so: $(NNIPREFIX)_BLAS.o
	$(LD) $(NNIPREFIX)_BLAS.o -o libnni_blas.so

$(NNIPATH)/LAPACK.class: $(NNIPATH)/LAPACK.java
	$(JAVAC) $(NNIPATH)/LAPACK.java

$(NNIPREFIX)_LAPACK.o: $(NNIPATH)/LAPACK.class $(NNIPREFIX)_LAPACK.c
	$(CC) $(NNIPREFIX)_LAPACK.c

libnni_lapack.so: $(NNIPREFIX)_LAPACK.o
	$(LD) $(NNIPREFIX)_LAPACK.o -o libnni_lapack.so

clean:
	$(RM) *.o
	$(RM) *.so
	$(RM) $(NNIPATH)/*.class
