BINDIR=../lib
SRCDIR=../libsrc

all: triangle frac3d gmv

triangle: $(SRCDIR)/triangle
	make -C $(SRCDIR)/triangle
	install $(SRCDIR)/triangle/triangle $(BINDIR)

frac3d: $(SRCDIR)/frac3d
	make -C $(SRCDIR)/frac3d
	install $(SRCDIR)/frac3d/frac3d $(BINDIR)
	
gmv: $(SRCDIR)/gmv
	make -C $(SRCDIR)/gmv
	install $(SRCDIR)/gmv/linuxogl $(BINDIR)/gmv

clean:
	rm -f $(BINDIR)/triangle
	rm -f $(BINDIR)/frac3d
	rm -f $(BINDIR)/gmv
