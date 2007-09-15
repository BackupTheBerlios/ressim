help:
	@echo Targets:
	@echo \ \ \ clean\ \ \ \ \ \ Delete all intermediate files
	@echo \ \ \ mesh-gen\ \ \ Create structure
	@echo \ \ \ mesh-vis\ \ \ Show mesh only
	@echo \ \ \ sim-run\ \ \ \ Run simulation
	@echo \ \ \ sim-vis\ \ \ \ Perform visualization

.PHONY: clean ex mesh-gen mesh-vis sim-run sim-vis

clean:
	-rm -f outcrop.poly
	-rm -f mesh*.poly
	-rm -f mesh*.ele
	-rm -f mesh*.node
	-rm -f get_tensors.m
	-rm -f perm*.input
	-rm -f poro.input
	-rm -rf gridding
	-rm -rf simulation
	-rm -rf visualization

# helper programs are located under this path
ROOT=../..
LIBDIR=$(realpath $(ROOT)/lib)

# program to smoothen grid
PERSSON:=$(ROOT)/data/persson/persson.sh

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

# utility programs
$(LIBDIR)/triangle: $(LIBDIR)/Makefile
	make -C $(LIBDIR) triangle

$(LIBDIR)/gmv: $(LIBDIR)/Makefile
	make -C $(LIBDIR) gmv

outcrop.poly: outcrop.odp mesh
	$(JAVA) no.uib.cipr.rs.meshgen.triangle.Main outcrop.odp mesh outcrop.poly
#	$(ROOT)/libsrc/triangle/showme mesh.poly &

# triangulation	
mesh.2.node mesh.2.poly mesh.2.ele: outcrop.poly
	$(PERSSON) --fractures=outcrop.poly --visual=0 --keep=1 --libdir=$(LIBDIR)
#	$(ROOT)/libsrc/triangle/showme mesh.2.ele &

# generate input mesh
mesh-gen gridding/mesh gridding/mesh.1: mesh mesh.2.node mesh.2.ele mesh.2.poly
	$(RS) mesh

# upscaling
get_tensors.m poro.input: gridding/mesh.1
	rs upscale | grep ^matlab: | sed s/^matlab:// > get_tensors.m

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

mesh-vis: visualization/mesh mesh.attr $(LIBDIR)/gmv
	ps -ao cmd | grep ^gmv.*mesh 1>/dev/null || (cd visualization; $(LIBDIR)/gmv -a ../mesh.attr -i mesh 1>/dev/null) &

sim-vis: visualization/0000 sim.attr $(LIBDIR)/gmv
	ps -ao cmd | grep ^gmv.*0000 1>/dev/null || (cd visualization; $(LIBDIR)/gmv -a ../sim.attr -i 0000 1>/dev/null) &
