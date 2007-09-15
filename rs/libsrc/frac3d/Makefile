
SHELL = /bin/sh

CC = /usr/bin/cc

CFLAGS =  -g  #-Wall 

LDFLAGS =  -lm

PROG     = frac3d

OBJECTS = main.o\
          area_computation.o\
          prog_functions.o\
          read_inputfile.o\
          read_startvalues.o\
          geometry.o\
          random.o\
          fracvert.o\
          build_list.o\
          parallel.o\
          gls_solution.o\
          intersection.o\
          intersection_points.o\
          graham.o\
          mcmc.o\
          optimization.o\
          pre_netgen_outp.o\
          prisma.o\
          quader.o\
          scanline.o\
          statistics.o\
          subplane3D.o\
          subvolume3D.o\
          transformation3D_2D.o\
          file_output.o

all: $(PROG)

$(PROG): $(OBJECTS)
	$(CC) $(CFLAGS) -o $(PROG) $(OBJECTS) $(LDFLAGS)

OBJECTS 	: functions.h
area_computation.o       : area_computation.h
file_output.o            : file_output.h
fracvert.o               : fracvert.h
geometry.o               : geometry.h
graham.o                 : subplane3D.h  graham.h
intersection_lines.o     : intersection.h
intersection_points.o    : intersection.h gls_solution.h subvolume3D.h
mcmc.o                   : mcmc.h
optimization.o           : optimization.h
parallel.o               : parallel.h intersection.h gls_solution.h
pre_netgen_outp.o        : pre_netgen_outp.h 
prog_functions.o         : prog_functions.h
prisma.o                 : subplane3D.h
quader.o                 : subplane3D.h
scanline.o               : scanline.h
subplane3D.o             : subplane3D.h  intersection.h 
subvolume3D.o            : subvolume3D.h intersection.h graham.h 
transformation3D_2D.o    : transformation3D_2D.h

clean:
	rm -f  *.o *.log *.tec *.net core a.out  *.*~ *~ tecplot.phy 

clr:
	rm -f *.log *.tec *.net

cc:
	rm -f *.o *.log *.tec core  a.out  *.*~ *~ tecplot.phy *.log *.tec *.net
