# sudo apt-get mesa-common-dev install libgl1-mesa-dev libglu1-mesa-dev

all: linuxogl2

diff:
	rm -f *.diff
	for x in *.c; do diff -dNu ../gmv.orig/$$x $$x > $$x.diff; done
	find -name \*.diff -empty -exec rm {} \;
	rm c99funcs.h.diff
	cat *.diff | gzip -9 > gmv-$$(date -%Y%m%d).diff.gz

# if you get problems with gl.h on Ubuntu Linux, check out this bugreport
# https://launchpad.net/distros/ubuntu/+source/mesa/+bug/29435

OBJSO =  main.o readgmv.o get_statistics.o initstuff.o gmvread.o \
	init_motif.o callbacks.o drawit.o drawaxes.o savescreen.o cbtun.o \
	drawpolygons.o drawpolylines.o drawnodepoints.o \
        drawcellfaces.o drawcellhiddenedges.o drawvectors.o centerform.o \
	subsetform.o matorderform.o snapshotform.o scaleaxisform.o \
	polysubsetform.o isosurfform.o makeisosurf.o \
	drawisosurface.o cutplaneform.o makecutplane.o \
	drawcutplane.o coloreditform.o attributes.o \
        animationform.o polygonform.o drawcelledges.o nodeform.o \
        cellform.o nodeselform.o cellselform.o cellexpform.o \
        drawanimvelflow.o gridanalform.o traceform.o drawtracers.o \
	icosadraw.o plotboxform.o imagestuff.o fieldlimform.o \
        tracelimform.o drawcolorbar.o boundingboxform.o drawboundingbox.o \
	drawsubsetbox.o animflyform.o vectbldform.o \
        cutlineform.o makecutline.o drawcutline.o fldcalcform.o \
        queryform.o nfieldform.o cfieldform.o cfieldlimform.o \
	cfldcalcform.o cvectbldform.o fldfiles_io.o isovolform.o \
	makeisovol.o pntsizeform.o autordform.o readgmvamr.o gen_amr.o \
	stereoform.o linesizeform.o fcoloreditform.o SciPlot.o \
	cutsphereform.o makecutsphere.o backgrndform.o gmvrc_io.o \
	vectorctlform.o surfform.o surfselform.o surfexpform.o \
	svectbldform.o sfieldform.o sfieldlimform.o sfldcalcform.o \
	drawsurfaces.o drawsurfedges.o rbzoomform.o nfldavgform.o \
	cfldavgform.o drawelem.o misc_ctlform.o windowsizeform.o \
	ffieldform.o ffieldlimform.o fvectbldform.o \
	readgmvray.o rayform.o drawrays.o raylimform.o concaveform.o\
	Ball.o BallAux.o BallMath.o \
	glw/compat.o glw/GLwDrawA.c glw/GLwMDrawA.c

#----------------------
# Set compiler options.

CCLNK = cc
GLWHOME=/usr/include/GL

#straight sunogl 		- use make sunogl
OGLHOME1 = /usr/openwin
GLWHOME1 = $(OGLHOME1)/include/X11/GLw
CCLINE1 = cc -c -g -xchip=ultra  -I$(OGLHOME1)/include -I$(GLWHOME1) -I/usr/dt/include -I$(GLWHOME1)
LIBS1 = -L$(OGLHOME1)/lib -L/usr/dt/lib -lGLw -lGL -lGLU -lXm -lXt -lX11 -ldga -lgen -lm -lmalloc
OBJS1 = $(OBJSO)
CCLNK1 = cc -g

#sgi opengl 			- use make sgiogl
CCLINE2 = cc -c -g -woff 1552 -I/usr/include -I$(GLWHOME)
LIBS2 = -lGLw -lGL -lGLU -lXm -lXt -lX11 -lXext -lm
OBJS2 = $(OBJSO) 
CCLNK2 = cc -g
 
#optimized sgi ogl for IRIX6.5 64bit	 - use make sgiogl6.5opt64
CCLINE3 = cc -c -64 -O3 -woff 1552 -I/usr/include -I$(GLWHOME)
LIBS3 = -lGLw -lGL -lGLU -lXm -lXt -lX11 -lXext -lm -lPW
OBJS3 = $(OBJSO)
CCLNK3 = cc -64 -O3
 
#optimized sgi ogl for IRIX6.5    - use make sgiogl6.5opt
CCLINE4 = cc -c -O3 -mips3 -woff 1552 -I/usr/include -I$(GLWHOME)
LIBS4 = -lGLw -lGL -lGLU -lXm -lXt -lX11 -lXext -lm -lPW
OBJS4 = $(OBJSO)
CCLNK4 = cc -O3 -mips3
 
#optimized sgi opengl for IRIX6.5 64bit for IRStereo - use make sgiogl6.5opt64ir
CCLINE5 = cc -c -64 -O3 -DIRSTEREO -I/usr/include -I$(GLWHOME)
LIBS5 = -lGLw -lGL -lGLU -lXm -lXt -lX11 -lXext -lm -lPW -lXsgivc
OBJS5 = $(OBJSO)
CCLNK5 = cc -64 -O3 
 
#sgi opengl with Mesa	- use make sgimesa
MESAHOME6 = ../../Mesa/Mesa6.2.1
GLWHOME6 = $(MESAHOME6)/include/GL
CCLINE6 = cc -c -O3 -woff 1552 -DMESA -I$(MESAHOME6)/include -I$(GLWHOME6)
LIBS6 = -L$(MESAHOME6)/lib32  \
-lGL -lGLU -lGLw -lXm -lXt -lXext -lX11 -lm 
OBJS6 = $(OBJSO)
CCLNK6 = cc -O3
 
#optimized Sun ultra Creator 3d	- use make solarisogl2
OGLHOME7 = /usr/openwin
GLWHOME7 = $(OGLHOME7)/include/X11/GLw
CCLINE7 = cc -c -xO4 -xchip=ultra  -I$(OGLHOME7)/include -I$(GLWHOME7) -I/usr/dt/include -I$(GLWHOME)
LIBS7 = -L$(OGLHOME7)/lib -L/usr/dt/lib -lGLw -lGL -lGLU -lXm -lXt -lX11 -ldga -lgen -lm -lmalloc
OBJS7 = $(OBJSO)
CCLNK7 = cc -xO4
 
#solaris opengl with Mesa	- use make solarismesa
MESAHOME8 = ../../Mesa/Mesa6.2.1
GLWHOME8 = $(MESAHOME8)/include/GL
MOTIFHOME8 = /usr/dt
CCLINE8 = cc -c -xO4 -DMESA -I$(MESAHOME8)/include \
-I$(MOTIFHOME8)/include -I$(GLWHOME8) -I/usr/openwin/include
LIBS8 = -L$(MESAHOME8)/lib -L$(MOTIFHOME8)/lib -L/usr/openwin/lib \
-Bstatic -lGL -lGLU -lGLw \
-Bdynamic -lXm -lXt -lXmu -lXext -lX11 -lm
OBJS8 = $(OBJSO)
CCLNK8 = cc -xO4
 
#linux opengl                  - use make linuxogl2
MESAHOME9 = /usr/X11R6
GLWHOME9 = /usr/include/GL
MOTIFHOME9 = /usr/X11R6
CCLINE9 = gcc -c -m32 -g -funroll-loops -mieee-fp -DLINUX -I$(GLWHOME9) -I$(MOTIFHOME9)/include
LIBS9 = -L$(MOTIFHOME9)/lib \
-lGL -lGLU \
-lXm -lXt -lXmu -lXext -lXp -lXpm -lX11 -lm
OBJS9 = $(OBJSO)
CCLNK9 = gcc -g

#linux opengl with Mesa 	- use make linuxmesa
MESAHOME10 = ../mesa
GLWHOME10 = $(MESAHOME10)/include/GL
MOTIFHOME10 = /usr/X11R6
CCLINE10 = gcc -c -m32 -O3 -funroll-loops -mieee-fp -DLINUX -DMESA \
-I$(MESAHOME10)/include -I$(MESAHOME10)/src/glw -I$(GLWHOME10) -I$(MOTIFHOME10)/include
LIBS10 = -L$(MESAHOME10)/lib -L$(MOTIFHOME10)/lib \
$(MESAHOME10)/lib/libGL.a $(MESAHOME10)/lib/libGLU.a $(MESAHOME10)/lib/libGLw.a \
/usr/lib/libXm.a -lXt -lXmu -lXext -lXp -lXpm -lX11 -lm -lpthread
OBJS10 = $(OBJSO)
CCLNK10 = gcc -O3

#Compaq Digital Tru64 opengl               - use make tru64ogl2
GLWHOME11 = /usr/include/X11/GLw
CCLINE11 = cc -c -fast -tune ev5 -ieee -DDEC -I$(GLWHOME11)
LIBS11 = -lGLw -lGL -lGLU -lXm -lXt -lXmu \
-lXext -lX11 -lm
OBJS11 = $(OBJSO)
CCLNK11 = cc -O

#Compaq Digital Tru64 opengl with Mesa     - use make tru64mesa
MESAHOME12 = ../../Mesa/Mesa6.2.1
GLWHOME12 = $(MESAHOME12)/include/GL
CCLINE12 = cc -c -fast -tune ev5 -ieee -non_shared -DDEC -DMESA -I$(MESAHOME12)/include -I$(GLWHOME12)
LIBS12 = $(MESAHOME12)/lib/libGLw.a $(MESAHOME12)/lib/libGL.a \
 $(MESAHOME12)/lib/libGLU.a \
 -lXm -lXt -lXmu -lXext -lX11 -lSM -lICE -ldnet_stub -lm 
OBJS12 = $(OBJSO)
CCLNK12 = cc -O -non_shared
 
#hpux opengl 		 	- use make hpuxogl2
OGLHOME13 = /opt/graphics/OpenGL
GLWHOME13 = $(OGLHOME13)/include/GL
X11R6HOME13 = /usr/lib/X11R6
MOTIFHOME13 = /usr/lib/Motif1.2
CCLINE13 = cc -c -O -Aa -Ae -D_HPUX_SOURCE -I$(OGLHOME13)/include \
-I$(MOTIFHOME13) -I$(X11R6HOME13) -I$(GLWHOME13)
LIBS13 = -L$(OGLHOME13)/lib -L$(X11R6HOME13) -lGL -lGLU -lGLw -lXm -lXt -lXext -lX11 -lm
OBJS13 = $(OBJSO)
CCLNK13 = cc
 
#hpux opengl with Mesa		- use make hpuxmesa
MESAHOME14 = ../../Mesa/Mesa6.2.1
GLWHOME14 = $(MESAHOME14)/include/GL
X11R6HOME14 = /usr/lib/X11R6
MOTIFHOME14 = /usr/lib/Motif1.2
CCLINE14 = cc -c -O -Aa -Ae -D_HPUX_SOURCE -DMESA -I$(MESAHOME14)/include \
-I/usr/include/Motif1.2 -I/usr/include/X11R6 -I$(GLWHOME14)
LIBS14 = -L$(MESAHOME14)/lib -L$(MOTIFHOME14) -L$(X11R6HOME14) \
-lGL -lGLU -lGLw -lXm -lXt -lXext -lX11 -lm
OBJS14 = $(OBJSO)
CCLNK14 = cc
 
#rs6000 opengl			- use make rs6000ogl2
CCLINE15 = cc -c -DAIXV3 -I$(GLWHOME)
LIBS15 = -lXGLW -lGL -lGLU -lXm -lXt -lX11 -lm
OBJS15 = $(OBJSO)
CCLNK15 = cc

#rs6000 opengl with Mesa     - use make rs6000mesa
MESAHOME16 = ../../Mesa/Mesa6.2.1
GLWHOME16 = $(MESAHOME16)/include/GL
CCLINE16 = cc -c -O -DAIXV3 -DMESA -I$(MESAHOME16)/include -I$(GLWHOME16)
LIBS16 = -L$(MESAHOME16)/lib \
 -lGLw -lGL -lGLU \
 -lXm -lXt -lX11 -lm 
OBJS16 = $(OBJSO)
CCLNK16 = cc -O
 
#NT opengl 		 	- use make ntogl2
SYSHOME17 = $(INTERIX_ROOT)/usr
OGLHOME17 = $(INTERIX_ROOT)/usr/X11R6/include/GL
MOTIFHOME17 = $(INTERIX_ROOT)/usr/X11R6/include/Xm
X11R6HOME17 = $(INTERIX_ROOT)/usr/X11R6
CCLINE17 = gcc -c -O2 -funroll-loops -mieee-fp -DNT -I$(SYSHOME17)/include -I$(X11R6HOME17)/include -I$(OGLHOME17) -I$(MOTIFHOME17) 
LIBS17 = -L$(SYSHOME17)/lib -L$(X11R6HOME17)/lib \
-lGL -lGLU -lGLw -lXm -lXt -lXext -lX11 -lXp -lSM -lICE -lm
OBJS17 = $(OBJSO)
CCLNK17 = gcc
 
#cygwin opengl 		 	- use make cygwinogl2
SYSHOME18 = /usr
OGLHOME18 = ../../Mesa/Mesa6.0.1/include/GL
MOTIFHOME18 = /usr/X11R6/include/Xm
X11R6HOME18 = /usr/X11R6
CCLINE18 = /usr/bin/gcc-2 -c -O3 -funroll-loops -mieee-fp -DNT -I$(X11R6HOME18)/include -I$(OGLHOME18) -I$(MOTIFHOME18) 
LIBS18 = -L$(SYSHOME18)/lib -L$(X11R6HOME18)/lib \
-lglu32 -lopengl32 -lGLU -lGL -lGLw -lXm -lXt -lXext -lX11 -lSM -lICE -lm
OBJS18 = $(OBJSO)
CCLNK18 = gcc
 
#Mac OS X opengl 		 - use make macxogl2
MESAHOME19 = ../../Mesa/Mesa6.2.1
GLWHOME19 = $(MESAHOME19)/include/GL
GLHOME19 = /usr/X11R6
MOTIFHOME19 = ../../OpenMotif
CCLINE19 = cc -c -O3 -funroll-loops -ffppc -DMACX \
-I/usr/include -I$(MOTIFHOME19)/include -I$(MESAHOME19)/include -I$(GLWHOME19) -I$(GLHOME19)/include -I/usr/include/sys
LIBS19 = -L$(GLHOME19)/lib \
-lGL -lGLU $(MESAHOME19)/lib/libGLw.a $(MOTIFHOME19)/lib/libXm.a \
-lXt -lX11 -lXext -lXp -lm
OBJS19 = $(OBJSO)
CCLNK19 = cc -O3 -flat-namespace
 
#linux 64 bit opengl                  - use make linux64ogl2
MESAHOME20 = /usr/X11R6
GLWHOME20 = /usr/include/GL
MOTIFHOME20 = /usr/X11R6/
CCLINE20 = gcc-ssa -c -O3 -m64 -funroll-loops -mieee-fp -DLINUX -I$(GLWHOME20) -I$(MOTIFHOME20)/include
LIBS20 = -L/usr/lib64 -L$(MOTIFHOME20)/lib64 \
-lGL -lGLU $(MESAHOME20)/lib64/libGLw.a \
/usr/X11R6/lib64/libXm.a -lXt -lXmu -lXext -lXp -lXpm -lX11 -lm
OBJS20 = $(OBJSO)
CCLNK20 = gcc-ssa -O3 -m64
 
#linux 64 bit opengl with Mesa 	       - use make linux64mesa
MESAHOME21 = ../../Mesa/Mesa6.2.1
GLWHOME21 = $(MESAHOME21)/include/GL
MOTIFHOME21 = /usr/X11R6/
CCLINE21 = gcc-ssa -c -O3 -m64 -funroll-loops -mieee-fp -DLINUX -DMESA \
-I$(MESAHOME21)/include -I$(GLWHOME21) -I$(MOTIFHOME21)/include
LIBS21 = -L/usr/lib64 -L$(MESAHOME21)/lib64 -L$(MOTIFHOME21)/lib64 \
-lGL -lGLU -lGLw \
/usr/X11R6/lib64/libXm.a -lXt -lXmu -lXext -lXp -lXpm -lX11 -lm -lpthread
OBJS21 = $(OBJSO)
CCLNK21 = gcc-ssa -O3 -m64


# Batch versions.
 
#sgi 64 bit opengl with Mesa	- use make sgibatch64
MESAHOME30 = /d11/fao/sgi/Mesa/Mesa6.2.1
GLWHOME30 = $(MESAHOME30)/include/GL
CCLINE30 = cc -c -64 -O3 -woff 1552 -DMESA -DBATCH -I$(MESAHOME30)/include -I$(GLWHOME30)
LIBS30 = -L$(MESAHOME30)/lib64  \
-lGL -lGLU -lGLw -lOSMesa -lXm -lXt -lXext -lX11 -lm 
OBJS30 = $(OBJSO)
CCLNK30 = cc -64 -O3
 
#sgi opengl with Mesa	- use make sgibatch
MESAHOME31 = /d11/fao/sgi/Mesa/Mesa6.2.1
GLWHOME31 = $(MESAHOME31)/include/GL
CCLINE31 = cc -c -O3 -woff 1552 -DMESA -DBATCH -I$(MESAHOME31)/include -I$(GLWHOME31)
LIBS31 = -L$(MESAHOME31)/lib32  \
-lGL -lGLU -lGLw -lOSMesa -lXm -lXt -lXext -lX11 -lm 
OBJS31 = $(OBJSO)
CCLNK31 = cc -O3
 
#solaris opengl with Mesa	- use make solarisbatch
MESAHOME32 = /d11/fao/solaris/Mesa/Mesa6.2.1
GLWHOME32 = $(MESAHOME32)/include/GL
MOTIFHOME32 = /usr/dt
CCLINE32 = cc -c -xO4 -DMESA -DBATCH -I$(MESAHOME32)/include \
-I$(MOTIFHOME32)/include -I$(GLWHOME32) -I/usr/openwin/include
LIBS32 = -L$(MESAHOME32)/lib -L$(MOTIFHOME32)/lib -L/usr/openwin/lib \
-Bstatic -lGL -lGLU -lGLw -lOSMesa \
-Bdynamic -lXm -lXt -lXmu -lXext -lX11 -lm
OBJS32 = $(OBJSO)
CCLNK32 = cc -xO4
 
#linux opengl with Mesa 	- use make linuxbatch
MESAHOME33 = ../../Mesa/Mesa6.2.1
GLWHOME33 = $(MESAHOME33)/include/GL
MOTIFHOME33 = /usr/X11R6/
CCLINE33 = gcc -c -O3 -funroll-loops -mieee-fp -DLINUX -DMESA -DBATCH \
-I$(MESAHOME33)/include -I$(GLWHOME33) -I$(MOTIFHOME33)/include
LIBS33 = -L$(MESAHOME33)/lib -L$(MOTIFHOME33)/lib \
-lGL -lGLU -lGLw -lOSMesa \
-lXm -lXt -lXmu -lXext -lXp -lXpm -lX11 -lm -lpthread
OBJS33 = $(OBJSO)
CCLNK33 = gcc -O3
 
#Compaq Digital Tru64 opengl with Mesa     - use make tru64batch
MESAHOME34 = ../../Mesa/Mesa6.2.1
GLWHOME34 = $(MESAHOME34)/include/GL
CCLINE34 = cc -c -fast -tune ev5 -ieee -non_shared -DDEC -DMESA -DBATCH -I$(MESAHOME34)/include -I$(GLWHOME34)
LIBS34 = $(MESAHOME34)/lib/libGLw.a $(MESAHOME34)/lib/libGL.a \
 $(MESAHOME34)/lib/libGLU.a  $(MESAHOME34)/lib/libOSMesa.a\
 -lXm -lXt -lXmu -lXext -lX11 -lSM -lICE -ldnet_stub -lm 
OBJS34 = $(OBJSO)
CCLNK34 = cc -O -non_shared

#hpux opengl with Mesa		- use make hpuxbatch
MESAHOME35 = ../../Mesa/Mesa6.2.1
GLWHOME35 = $(MESAHOME34)/include/GL
X11R6HOME35 = /usr/lib/X11R6
MOTIFHOME35 = /usr/lib/Motif1.2
CCLINE35 = cc -c -O -Aa -Ae -D_HPUX_SOURCE -DMESA -DBATCH -I$(MESAHOME35)/include -I/usr/include/Motif1.2 -I/usr/include/X11R6 -I$(GLWHOME35)
LIBS35 = -L$(MESAHOME35)/lib -L$(MOTIFHOME35) -L$(X11R6HOME35) \
-lGL -lGLU -lGLw -lOSMesa -lXm -lXt -lXext -lX11 -lm
OBJS35 = $(OBJSO)
CCLNK35 = cc

#rs6000 opengl with Mesa     - use make rs6000batch
MESAHOME36 = ../../Mesa/Mesa6.2.1
GLWHOME36 = $(MESAHOME36)/include/GL
CCLINE36 = cc -c -O -DAIXV3 -DMESA -DBATCH -I$(MESAHOME36)/include -I$(GLWHOME36)
LIBS36 = -L$(MESAHOME36)/lib \
 -lGLw -lGL -lGLU -lOSMesa \
 -lXm -lXt -lX11 -lm 
OBJS36 = $(OBJSO)
CCLNK36 = cc -O
 
#macx opengl with Mesa 	- use make macxbatch
MESAHOME37 = ../../Mesa/Mesa6.2.1
GLWHOME37 = $(MESAHOME37)/include/GL
MOTIFHOME37 = ../../OpenMotif
X11R6HOME37 = /usr/X11R6
CCLINE37 = cc -c -O3 -funroll-loops -ffppc -DMACX -DBATCH \
-I/usr/include -I$(MOTIFHOME37)/include -I$(MESAHOME37)/include -I$(GLWHOME37)  -I/usr/include/sys -I$(X11R6HOME37)/include
LIBS37 = -L$(MESAHOME37)/lib -L$(MOTIFHOME37)/lib -L$(X11R6HOME37)/lib \
$(MESAHOME19)/lib/libGL.a $(MESAHOME19)/lib/libGLU.a $(MESAHOME19)/lib/libOSMesa.a $(MESAHOME19)/lib/libGLw.a \
$(MOTIFHOME37)/lib/libXm.a -lXt -lX11 -lXext -lXp -lm
OBJS37 = $(OBJSO)
CCLNK37 = cc -O3 -flat-namespace
 
#linux 64 bit opengl with Mesa 	       - use make linux64batch
MESAHOME38 = ../../Mesa/Mesa6.2.1
GLWHOME38 = $(MESAHOME21)/include/GL
MOTIFHOME38 = /usr/X11R6/
CCLINE38 = gcc-ssa -c -O3 -m64 -funroll-loops -mieee-fp -DLINUX -DMESA \
-I$(MESAHOME38)/include -I$(GLWHOME38) -I$(MOTIFHOME38)/include
LIBS38 = -L/usr/lib64 -L$(MESAHOME38)/lib64 -L$(MOTIFHOME38)/lib64 \
-lGL -lGLU -lGLw \
/usr/X11R6/lib64/libXm.a -lXt -lXmu -lXext -lXp -lXpm -lX11 -lm -lpthread
OBJS38 = $(OBJSO)
CCLNK38 = gcc-ssa -O3 -m64


#--------------------------------------
#Set variables and recall the makefile.

nooptions : default

sunogl :
	make -e CCLINE="$(CCLINE1)" LIBS="$(LIBS1)" OBJS="$(OBJS1)" CCLNK="$(CCLNK1)" gmv

sgiogl :
	make -e CCLINE="$(CCLINE2)" LIBS="$(LIBS2)" OBJS="$(OBJS2)" CCLNK="$(CCLNK2)" gmv

sgiogl6.5opt64 :
	make -e CCLINE="$(CCLINE3)" LIBS="$(LIBS3)" OBJS="$(OBJS3)" CCLNK="$(CCLNK3)" sgiogl6.5_64

sgiogl6.5opt :
	make -e CCLINE="$(CCLINE4)" LIBS="$(LIBS4)" OBJS="$(OBJS4)" CCLNK="$(CCLNK4)" sgiogl6.5

sgiogl6.5opt64ir :
	make -e CCLINE="$(CCLINE5)" LIBS="$(LIBS5)" OBJS="$(OBJS5)" CCLNK="$(CCLNK5)" sgiogl6.5_64ir

sgimesa :
	make -e CCLINE="$(CCLINE6)" LIBS="$(LIBS6)" OBJS="$(OBJS6)" CCLNK="$(CCLNK6)" sgiMesa

solarisogl2 :
	make -e CCLINE="$(CCLINE7)" LIBS="$(LIBS7)" OBJS="$(OBJS7)" CCLNK="$(CCLNK7)" solarisogl

solarismesa :
	make -e CCLINE="$(CCLINE8)" LIBS="$(LIBS8)" OBJS="$(OBJS8)" CCLNK="$(CCLNK8)" solarisMesa

linuxogl2 :
	make -e CCLINE="$(CCLINE9)" LIBS="$(LIBS9)" OBJS="$(OBJS9)" CCLNK="$(CCLNK9)" linuxogl

linuxmesa :
	make -e CCLINE="$(CCLINE10)" LIBS="$(LIBS10)" OBJS="$(OBJS10)" CCLNK="$(CCLNK10)" linuxMesa

tru64ogl2 :
	make -e CCLINE="$(CCLINE11)" LIBS="$(LIBS11)" OBJS="$(OBJS11)" CCLNK="$(CCLNK11)" tru64ogl

tru64mesa :
	make -e CCLINE="$(CCLINE12)" LIBS="$(LIBS12)" OBJS="$(OBJS12)" CCLNK="$(CCLNK12)" tru64Mesa

hpuxogl2 :
	make -e CCLINE="$(CCLINE13)" LIBS="$(LIBS13)" OBJS="$(OBJS13)" CCLNK="$(CCLNK13)" hpuxogl

hpuxmesa :
	make -e CCLINE="$(CCLINE14)" LIBS="$(LIBS14)" OBJS="$(OBJS14)" CCLNK="$(CCLNK14)" hpuxMesa

rs6000ogl2 :
	make -e CCLINE="$(CCLINE15)" LIBS="$(LIBS15)" OBJS="$(OBJS15)" CCLNK="$(CCLNK9)" rs6000ogl

rs6000mesa :
	make -e CCLINE="$(CCLINE16)" LIBS="$(LIBS16)" OBJS="$(OBJS16)" CCLNK="$(CCLNK16)" rs6000Mesa

ntogl2 :
	make -e CCLINE="$(CCLINE17)" LIBS="$(LIBS17)" OBJS="$(OBJS17)" CCLNK="$(CCLNK17)" ntogl

cygwinogl2 :
	make -e CCLINE="$(CCLINE18)" LIBS="$(LIBS18)" OBJS="$(OBJS18)" CCLNK="$(CCLNK18)" cygwinogl

macxogl2 :
	make -e CCLINE="$(CCLINE19)" LIBS="$(LIBS19)" OBJS="$(OBJS19)" CCLNK="$(CCLNK19)" macxogl

linux64ogl2 :
	make -e CCLINE="$(CCLINE20)" LIBS="$(LIBS20)" OBJS="$(OBJS20)" CCLNK="$(CCLNK20)" linux64ogl

linux64mesa :
	make -e CCLINE="$(CCLINE21)" LIBS="$(LIBS21)" OBJS="$(OBJS21)" CCLNK="$(CCLNK21)" linux64Mesa


# Batch versions.

sgibatch64 :
	make -e CCLINE="$(CCLINE30)" LIBS="$(LIBS30)" OBJS="$(OBJS30)" CCLNK="$(CCLNK30)" sgiBatch64

sgibatch :
	make -e CCLINE="$(CCLINE31)" LIBS="$(LIBS31)" OBJS="$(OBJS31)" CCLNK="$(CCLNK31)" sgiBatch

solarisbatch :
	make -e CCLINE="$(CCLINE32)" LIBS="$(LIBS32)" OBJS="$(OBJS32)" CCLNK="$(CCLNK32)" solarisBatch

linuxbatch :
	make -e CCLINE="$(CCLINE33)" LIBS="$(LIBS33)" OBJS="$(OBJS33)" CCLNK="$(CCLNK33)" linuxBatch

tru64batch :
	make -e CCLINE="$(CCLINE34)" LIBS="$(LIBS34)" OBJS="$(OBJS34)" CCLNK="$(CCLNK34)" tru64Batch

hpuxbatch :
	make -e CCLINE="$(CCLINE35)" LIBS="$(LIBS35)" OBJS="$(OBJS35)" CCLNK="$(CCLNK35)" hpuxBatch

rs6000batch :
	make -e CCLINE="$(CCLINE36)" LIBS="$(LIBS36)" OBJS="$(OBJS36)" CCLNK="$(CCLNK36)" rs6000Batch

macxbatch :
	make -e CCLINE="$(CCLINE37)" LIBS="$(LIBS37)" OBJS="$(OBJS37)" CCLNK="$(CCLNK37)" macxBatch

linux64batch :
	make -e CCLINE="$(CCLINE38)" LIBS="$(LIBS38)" OBJS="$(OBJS38)" CCLNK="$(CCLNK38)" linux64Batch


#-----------------------------------------
# Recalled targets.

gmv : snapshot_old $(OBJS)
	$(CCLNK) -o gmv $(OBJS) $(LIBS)
 
sgiogl6.5_64 : snapshot_old $(OBJS)
	$(CCLNK) -o sgiogl6.5_64 $(OBJS) $(LIBS)
	strip sgiogl6.5_64
 
sgiogl6.5_64ir : snapshot_old $(OBJS)
	$(CCLNK) -o sgiogl6.5_64ir $(OBJS) $(LIBS)
	strip sgiogl6.5_64ir

sgiogl6.5 : snapshot_old $(OBJS)
	$(CCLNK) -o sgiogl6.5 $(OBJS) $(LIBS)
	strip sgiogl6.5

sgiMesa : snapshot_old $(OBJS)
	$(CCLNK) -o sgiMesa $(OBJS) $(LIBS)
	strip sgiMesa

solarisogl : snapshot_old $(OBJS)
	$(CCLNK) -o solarisogl $(OBJS) $(LIBS)
	strip solarisogl
 
solarisMesa : snapshot_old $(OBJS)
	$(CCLNK) -o solarisMesa $(OBJS) $(LIBS)
	strip solarisMesa

linuxogl : snapshot_old $(OBJS)
	$(CCLNK) -o linuxogl $(OBJS) $(LIBS)
	#strip linuxogl
 
linuxMesa : $(OBJS)
	$(CCLNK) -o linuxMesa $(OBJS) $(LIBS)
	#strip linuxMesa

tru64ogl : snapshot_old $(OBJS)
	$(CCLNK) -o tru64ogl $(OBJS) $(LIBS)
	strip tru64ogl

tru64Mesa : snapshot_old $(OBJS)
	$(CCLNK) -o tru64Mesa $(OBJS) $(LIBS)
	strip tru64Mesa

hpuxogl: snapshot_old $(OBJS)
	$(CCLNK) -o hpuxogl $(OBJS) $(LIBS)
	strip hpuxogl
 
hpuxMesa : snapshot_old $(OBJS)
	$(CCLNK) -o hpuxMesa $(OBJS) $(LIBS)
	strip hpuxMesa
 
rs6000ogl : snapshot_old $(OBJS)
	$(CCLNK) -o rs6000ogl $(OBJS) $(LIBS)
	strip rs6000ogl

rs6000Mesa : snapshot_old $(OBJS)
	$(CCLNK) -o rs6000Mesa $(OBJS) $(LIBS)
	strip rs6000Mesa

ntogl: snapshot_old $(OBJS)
	$(CCLNK) -o ntogl $(OBJS) $(LIBS)
	strip ntogl

cygwinogl: snapshot_mac $(OBJS)
	$(CCLNK) -o cygwinogl $(OBJS) $(LIBS)
	strip cygwinogl.exe -o cygwinogl

macxogl: snapshot_mac $(OBJS)
	$(CCLNK) -o macxogl $(OBJS) $(LIBS)
	strip macxogl

linux64ogl : snapshot_old $(OBJS)
	$(CCLNK) -o linux64ogl $(OBJS) $(LIBS)
	strip linux64ogl
 
linux64Mesa : snapshot_old $(OBJS)
	$(CCLNK) -o linux64Mesa $(OBJS) $(LIBS)
	strip linux64Mesa

# Batch versions.

sgiBatch64 : snapshot_old $(OBJS)
	$(CCLNK) -o sgiBatch64 $(OBJS) $(LIBS)
	strip sgiBatch64

sgiBatch : snapshot_old $(OBJS)
	$(CCLNK) -o sgiBatch $(OBJS) $(LIBS)
	strip sgiBatch
 
solarisBatch : snapshot_old $(OBJS)
	$(CCLNK) -o solarisBatch $(OBJS) $(LIBS)
	strip solarisBatch
 
linuxBatch : snapshot_old $(OBJS)
	$(CCLNK) -o linuxBatch $(OBJS) $(LIBS)
	strip linuxBatch

tru64Batch : snapshot_old $(OBJS)
	$(CCLNK) -o tru64Batch $(OBJS) $(LIBS)
	strip tru64Batch
 
hpuxBatch : snapshot_old $(OBJS)
	$(CCLNK) -o hpuxBatch $(OBJS) $(LIBS)
	strip hpuxBatch

rs6000Batch : snapshot_old $(OBJS)
	$(CCLNK) -o rs6000Batch $(OBJS) $(LIBS)
	strip rs6000Batch
 
macxBatch : snapshot_mac $(OBJS)
	$(CCLNK) -o macxBatch $(OBJS) $(LIBS)
	strip macxBatch
 
linux64Batch : snapshot_old $(OBJS)
	$(CCLNK) -o linux64Batch $(OBJS) $(LIBS)
	strip linux64Batch


#----------------
# Source targets.
 
#main.o : main.c main.h gmvdata.h lights.h formwidgets.h contour.h \
#	cutplane.h xstuff.h
#	$(CCLINE) main.c
# 
#readgmv.o : readgmv.c gmvdata.h gmvread.h
#	$(CCLINE) readgmv.c
# 
#cbtun.o : cbtun.c gmvdata.h
#	$(CCLINE) cbtun.c
# 
#initstuff.o : initstuff.c gmvdata.h lights.h contour.h cutplane.h
#	$(CCLINE) initstuff.c
# 
#init_motif.o : init_motif.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) init_motif.c
# 
#callbacks.o : callbacks.c gmvdata.h formwidgets.h lights.h \
#	xstuff.h contour.h cutplane.h
#	$(CCLINE) callbacks.c
# 
#drawit.o : drawit.c gmvdata.h formwidgets.h lights.h \
#	contour.h xstuff.h cutplane.h
#	$(CCLINE) drawit.c
# 
#drawaxes.o : drawaxes.c gmvdata.h
#	$(CCLINE) drawaxes.c
# 
#drawpolygons.o : drawpolygons.c lights.h gmvdata.h stipples.h
#	$(CCLINE) drawpolygons.c
# 
#drawpolylines.o : drawpolylines.c lights.h gmvdata.h
#	$(CCLINE) drawpolylines.c
# 
#get_statistics.o : get_statistics.c gmvdata.h
#	$(CCLINE) get_statistics.c
# 
#drawnodepoints.o : drawnodepoints.c lights.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawnodepoints.c
# 
#drawvectors.o : drawvectors.c gmvdata.h setfldcolr.h
#	$(CCLINE) drawvectors.c
# 
#drawcellfaces.o : drawcellfaces.c lights.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawcellfaces.c
# 
#drawcellhiddenedges.o : drawcellhiddenedges.c lights.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawcellhiddenedges.c
# 
#drawisosurface.o : drawisosurface.c gmvdata.h contour.h setfldcolr.h
#	$(CCLINE) drawisosurface.c
# 
#centerform.o : centerform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) centerform.c
# 
#subsetform.o : subsetform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) subsetform.c
# 
#matorderform.o : matorderform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) matorderform.c
# 
#isosurfform.o : isosurfform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) isosurfform.c
# 
#makeisosurf.o : makeisosurf.c gmvdata.h contour.h
#	$(CCLINE) makeisosurf.c
# 
#coloreditform.o : coloreditform.c formwidgets.h gmvdata.h xstuff.h lights.h 
#	$(CCLINE) coloreditform.c
# 
#cutplaneform.o : cutplaneform.c cutplane.h formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) cutplaneform.c
# 
#makecutplane.o : makecutplane.c cutplane.h gmvdata.h
#	$(CCLINE) makecutplane.c
# 
#drawcutplane.o : drawcutplane.c cutplane.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawcutplane.c
# 
#savescreen.o : savescreen.c gmvdata.h
#	$(CCLINE) savescreen.c
# 
#attributes.o : attributes.c gmvdata.h lights.h contour.h
#	$(CCLINE) attributes.c
# 
#animationform.o : animationform.c formwidgets.h gmvdata.h \
#	xstuff.h cutplane.h contour.h lights.h
#	$(CCLINE) animationform.c
# 
#polysubsetform.o : polysubsetform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) polysubsetform.c
# 
#snapshotform.o : snapshotform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) snapshotform.c
# 
#scaleaxisform.o : scaleaxisform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) scaleaxisform.c
# 
#polygonform.o : polygonform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) polygonform.c
# 
#drawcelledges.o : drawcelledges.c lights.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawcelledges.c
# 
#nodeform.o : nodeform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) nodeform.c
# 
#cellform.o : cellform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) cellform.c
# 
#nodeselform.o : nodeselform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) nodeselform.c
# 
#cellselform.o : cellselform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) cellselform.c
# 
#cellexpform.o : cellexpform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) cellexpform.c
# 
#drawanimvelflow.o : drawanimvelflow.c gmvdata.h setfldcolr.h
#	$(CCLINE) drawanimvelflow.c
# 
#gridanalform.o : gridanalform.c gmvdata.h xstuff.h contour.h
#	$(CCLINE) gridanalform.c
# 
#traceform.o : traceform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) traceform.c
# 
#drawtracers.o : drawtracers.c lights.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawtracers.c
# 
#icosadraw.o : icosadraw.c
#	$(CCLINE) icosadraw.c
# 
#plotboxform.o : plotboxform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) plotboxform.c
#
#imagestuff.o : imagestuff.c image.h 
#	$(CCLINE) imagestuff.c
# 
#fieldlimform.o : fieldlimform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) fieldlimform.c
# 
#tracelimform.o : tracelimform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) tracelimform.c
#
#drawcolorbar.o : drawcolorbar.c gmvdata.h cutplane.h
#	$(CCLINE) drawcolorbar.c
# 
#boundingboxform.o : boundingboxform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) boundingboxform.c
#
#drawboundingbox.o : drawboundingbox.c gmvdata.h
#	$(CCLINE) drawboundingbox.c
#
#drawsubsetbox.o : drawsubsetbox.c gmvdata.h
#	$(CCLINE) drawsubsetbox.c
# 
#animflyform.o : animflyform.c formwidgets.h gmvdata.h
#	$(CCLINE) animflyform.c
# 
#vectbldform.o : vectbldform.c gmvdata.h formwidgets.h xstuff.h
#	$(CCLINE) vectbldform.c
# 
#cutlineform.o : cutlineform.c cutplane.h formwidgets.h \
#	gmvdata.h xstuff.h SciPlot.h
#	$(CCLINE) cutlineform.c
# 
#makecutline.o : makecutline.c cutplane.h gmvdata.h
#	$(CCLINE) makecutline.c
# 
#drawcutline.o : drawcutline.c cutplane.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawcutline.c
# 
fldcalcform.o : fldcalcform.c formwidgets.h gmvdata.h xstuff.h
	$(CCLINE) fldcalcform.c
 
#queryform.o : queryform.c formwidgets.h gmvdata.h xstuff.h cutplane.h
#	$(CCLINE) queryform.c
# 
#nfieldform.o : nfieldform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) nfieldform.c
# 
#cfieldform.o : cfieldform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) cfieldform.c
# 
#cfieldlimform.o : cfieldlimform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) cfieldlimform.c
# 
cfldcalcform.o : cfldcalcform.c formwidgets.h gmvdata.h xstuff.h
	$(CCLINE) cfldcalcform.c
 
#cvectbldform.o : cvectbldform.c gmvdata.h formwidgets.h xstuff.h
#	$(CCLINE) cvectbldform.c
# 
#fldfiles_io.o : fldfiles_io.c
#	$(CCLINE) fldfiles_io.c
# 
#isovolform.o : isovolform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) isovolform.c
# 
#makeisovol.o : makeisovol.c gmvdata.h contour.h
#	$(CCLINE) makeisovol.c
# 
#pntsizeform.o : pntsizeform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) pntsizeform.c
# 
#autordform.o : autordform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) autordform.c
# 
#readgmvamr.o : readgmvamr.c gmvdata.h gmvread.h
#	$(CCLINE) readgmvamr.c
# 
#gen_amr.o : gen_amr.c gmvdata.h
#	$(CCLINE) gen_amr.c
# 
#stereoform.o : stereoform.c gmvdata.h formwidgets.h xstuff.h
#	$(CCLINE) stereoform.c
# 
#linesizeform.o : linesizeform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) linesizeform.c
# 
#fcoloreditform.o : fcoloreditform.c formwidgets.h gmvdata.h xstuff.h lights.h
#	$(CCLINE) fcoloreditform.c
# 
#SciPlot.o : SciPlot.h SciPlotP.h xstuff.h
#	$(CCLINE) SciPlot.c
# 
#cutsphereform.o : cutsphereform.c cutplane.h formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) cutsphereform.c
# 
#makecutsphere.o : makecutsphere.c cutplane.h gmvdata.h
#	$(CCLINE) makecutsphere.c
# 
#backgrndform.o : backgrndform.c formwidgets.h gmvdata.h xstuff.h lights.h
#	$(CCLINE) backgrndform.c
# 
#gmvrc_io.o : gmvrc_io.c formwidgets.h gmvdata.h 
#	$(CCLINE) gmvrc_io.c
# 
#vectorctlform.o : vectorctlform.c formwidgets.h gmvdata.h xstuff.h 
#	$(CCLINE) vectorctlform.c
# 
#surfform.o : surfform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) surfform.c
# 
#surfselform.o : surfselform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) surfselform.c
# 
#surfexpform.o : surfexpform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) surfexpform.c
# 
#svectbldform.o : svectbldform.c gmvdata.h formwidgets.h xstuff.h
#	$(CCLINE) svectbldform.c
# 
#sfieldform.o : sfieldform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) sfieldform.c
# 
#sfieldlimform.o : sfieldlimform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) sfieldlimform.c
# 
sfldcalcform.o : sfldcalcform.c formwidgets.h gmvdata.h xstuff.h
	$(CCLINE) sfldcalcform.c
 
#drawsurfaces.o : drawsurfaces.c lights.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawsurfaces.c
# 
#drawsurfedges.o : drawsurfedges.c lights.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawsurfedges.c
# 
#rbzoomform.o : rbzoomform.c formwidgets.h gmvdata.h xstuff.h 
#	$(CCLINE) rbzoomform.c
# 
#nfldavgform.o : nfldavgform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) nfldavgform.c
# 
#cfldavgform.o : cfldavgform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) cfldavgform.c
# 
#drawelem.o : drawelem.c gmvdata.h lights.h
#	$(CCLINE) drawelem.c
# 
#misc_ctlform.o : misc_ctlform.c gmvdata.h xstuff.h
#	$(CCLINE) misc_ctlform.c
# 
#windowsizeform.o : windowsizeform.c gmvdata.h xstuff.h
#	$(CCLINE) windowsizeform.c
# 
#ffieldform.o : ffieldform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) ffieldform.c
# 
#ffieldlimform.o : ffieldlimform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) ffieldlimform.c
# 
#fvectbldform.o : fvectbldform.c gmvdata.h formwidgets.h xstuff.h
#	$(CCLINE) fvectbldform.c
#
#gmvread.o : gmvread.c gmvread.h gmvrayread.h
#	$(CCLINE) gmvread.c
#
#readgmvray.o : readgmvray.c gmvdata.h gmvread.h gmvrayread.h
#	$(CCLINE) readgmvray.c
# 
#drawrays.o : drawrays.c lights.h gmvdata.h setfldcolr.h
#	$(CCLINE) drawrays.c
# 
#rayform.o : rayform.c formwidgets.h gmvdata.h lights.h xstuff.h
#	$(CCLINE) rayform.c
# 
#raylimform.o : raylimform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) raylimform.c
# 
#concaveform.o : concaveform.c formwidgets.h gmvdata.h xstuff.h
#	$(CCLINE) concaveform.c
#
#Ball.o: Ball.c Ball.h BallMath.h
#	$(CCLINE) Ball.c
# 
#BallAux.o: BallAux.c BallAux.h
#	$(CCLINE) BallAux.c
# 
#BallMath.o: BallMath.c BallMath.h BallAux.h
#	$(CCLINE) BallMath.c

glw/compat.o : glw/compat.c
	$(CCLINE) -Iglw -o glw/compat.o glw/compat.c
 
glw/GLwDrawA.o: glw/GLwDrawA.c
	$(CCLINE) -Iglw -o glw/GLwDrawA.o glw/GLwDrawA.c
	
glw/GLwMDrawA.o: glw/GLwMDrawA.c
	$(CCLINE) -Iglw -o glw/GLwMDrawA.o glw/GLwMDrawA.c 
 
clean:
#	rm -f *.o *.u
	rm -f `find -name \*.c | sed s/\.c$$/.o/ | xargs`
	rm -f glw/*.o
	rm -f linux*

default:
	@echo "Type one of the following:"
	@echo "  make sgiogl		sgi opengl"
	@echo "  make sgiogl6.5opt64	optimized sgi ogl for IRIX6.5 64bit"
	@echo "  make sgiogl6.5opt	optimized sgi opengl for IRIX6.5"
	@echo "  make sgimesa		sgi opengl with Mesa"
	@echo "  make sgibatch		sgi batch with Mesa"
	@echo "  make solarisogl2		Sun Ultra w/3D Creator"
	@echo "  make solarismesa	solaris opengl with Mesa"
	@echo "  make solarisbarch	solaris batch with Mesa"
	@echo "  make linuxogl2	 	linux opengl"
	@echo "  make linuxmesa	 	linux opengl with Mesa"
	@echo "  make linuxbatch 	linux batch with Mesa"
	@echo "  make tru64ogl2		Compaq Digital Tru64 Tru64 opengl"
	@echo "  make tru64mesa		Compaq Digital Tru64 opengl with Mesa"
	@echo "  make tru64batch	Compaq Digital Tru64 batch with Mesa"
	@echo "  make hpuxogl2		hpux opengl"
	@echo "  make hpuxmesa		hpux opengl with Mesa"
	@echo "  make hpuxbatch		hpux batch with Mesa"
	@echo "  make rs6000ogl2	rs6000 opengl"
	@echo "  make rs6000mesa	rs6000 opengl with Mesa"
	@echo "  make macxogl2		Mac OS X opengl"
	@echo "  make macxbatch 	Mac OS X batch with Mesa"
	@echo "  make linux64ogl2	linux 64-bit opengl"
	@echo "  make linux64mesa	linux 64-bit linux opengl with Mesa"
	@echo "  make linux64batch	linux 64-bit linux batch with Mesa"

snapshot_mac :
	cp savescreen.macx.c savescreen.c 
	cp imagestuff.macx.c imagestuff.c

snapshot_old :
#	cp savescreen.old.c savescreen.c 
#	cp imagestuff.old.c imagestuff.c

