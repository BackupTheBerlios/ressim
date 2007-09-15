help:
	@echo Targets:
	@echo \ \ \ clean\ \ \ \ \ \ Delete all intermediate files
	@echo \ \ \ ex\ \ \ \ \ \ \ \ \ Use artificial fractures
	@echo \ \ \ mesh-gen\ \ \ Create structure
	@echo \ \ \ mesh-vis\ \ \ Show mesh only
	@echo \ \ \ sim-run\ \ \ \ Run simulation
	@echo \ \ \ sim-vis\ \ \ \ Perform visualization

.PHONY: clean ex mesh-gen mesh-vis sim-run sim-vis

clean:
	rm -f mesh.poly
	rm -f 3Dtest_*
	rm -f Quader_*
	rm -f Statistic_*
	rm -f Subplane3D_*
	rm -f *.[1-2].*
	rm -f mesh.inp*
	rm -rf gridding
	rm -rf simulation
	rm -rf visualization

# helper programs are located under this path
ROOT=../..
LIBDIR=$(realpath $(ROOT)/lib)

# get the path to the Java libraries through the project file
CLASSPATH:=$(shell xsltproc --stringparam root $(ROOT)/ mkcp.xsl $(ROOT)/.classpath)

# get the total amount of memory in the machine and subtract 128MB for the OS
MEMORY:=$(shell cat /proc/meminfo | grep "^MemTotal:" | sed "s/[^:]*:\ *\([0-9]\+\)\ kB/\1-131072/g" | bc)

# default memory requirement to run this example
JAVAOPT=-server -enableassertions -Xmx$(MEMORY)k -XX:+UseParallelGC -XX:+UseAdaptiveSizePolicy -XX:ReservedCodeCacheSize=32m -XX:+UseTLE #-XX:+UseLargePages

# allow connection to the VM through a local socket
DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=localhost,suspend=n

# command for launching Java classes
JAVA=java -cp $(CLASSPATH) $(JAVAOPT)

# shortcut for launching simulator
RS=/usr/bin/time -f "Seconds: %U" nice $(JAVA) no.uib.cipr.rs.Main

# if this macro is defined, then partitioning should be used
PARTITION=#coarse.inp

# triangularization parameters
MIN_ANGLE=q#28.6
MAX_AREA=a1.0
MIN_AREA=m0.025

# utility programs

$(LIBDIR)/triangle: $(LIBDIR)/Makefile
	make -C $(LIBDIR) triangle
	
$(LIBDIR)/frac3d: $(LIBDIR)/Makefile
	make -C $(LIBDIR) frac3d
	
$(LIBDIR)/gmv: $(LIBDIR)/Makefile
	make -C $(LIBDIR) gmv
	
# fracture generation	
	
Subplane3D_000.dat: Eingabefile.dat 2D_slices.dat $(LIBDIR)/frac3d
	$(LIBDIR)/frac3d
	
ex: ex.Subplane3D_000.dat
	cp -f ex.Subplane3D_000.dat Subplane3D_000.dat
	
# triangulation	

mesh.poly: Subplane3D_000.dat mesh
	nice $(JAVA) no.uib.cipr.rs.meshgen.triangle.Main Subplane3D_000.dat mesh mesh.poly

mesh.1.node mesh.1.ele mesh.1.poly: mesh.poly $(LIBDIR)/triangle
	$(LIBDIR)/triangle -p$(MIN_ANGLE)$(MIN_AREA) mesh.poly
	
mesh.2.node mesh.2.ele mesh.2.poly: mesh.1.node mesh.1.ele mesh.1.poly $(LIBDIR)/triangle
	$(LIBDIR)/triangle -pr$(MIN_ANGLE)$(MAX_AREA)$(MIN_AREA) mesh.1

# generate input mesh
	
mesh-gen gridding/mesh gridding/mesh.1: mesh mesh.2.node mesh.2.ele mesh.2.poly
	$(RS) mesh
	
# simulation	
	
.PRECIOUS: simulation/time

sim-run simulation/time: gridding/mesh.1 ../pvt/two run
	$(RS) run 2>&1
	
# visualization should also be dependent on time, so that the export will
# be rebuilt when we have more timesteps available
visualization/0000: gridding/mesh.1 simulation/time
	$(RS) run_gmv 2>&1

visualization/mesh: gridding/mesh
	$(RS) mesh_gmv 2>&1
	rm visualization/gmvrc
	
# copy master GMV settings to visualization directory
# TODO: When the GMV export writes these files itself, kill these lines
visualization/gmvrc: gmvrc
	cp -f gmvrc visualization/gmvrc

mesh-vis: visualization/mesh mesh.attr visualization/gmvrc $(LIBDIR)/gmv
	ps -ao cmd | grep ^gmv.*mesh 1>/dev/null || (cd visualization; $(LIBDIR)/gmv -a ../mesh.attr -i mesh 1>/dev/null) &

sim-vis: visualization/0000 sim.attr visualization/gmvrc $(LIBDIR)/gmv
	ps -ao cmd | grep ^gmv.*0000 1>/dev/null || (cd visualization; $(LIBDIR)/gmv -a ../sim.attr -i 0000 1>/dev/null) &
