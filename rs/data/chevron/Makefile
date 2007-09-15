RS_DIR:=~/workspace/rs
LIBS:=jmatio.jar:log4j-1.2.12.jar

all: mesh.dat.gz

chevron.class: chevron.java
	javac -cp $(RS_DIR)/bin:$(LIBS) chevron.java

mesh.dat.gz: mesh.mat chevron.class
	java -cp $(RS_DIR)/bin:$(LIBS):. chevron mesh.mat mesh.dat.gz

mesh.mat: data.mat
	matlab -nodisplay -nosplash -nojvm -r "m=load('data.mat');Model=m.Model;save('mesh.mat','Model','-V6');quit" | tail -n +15

clean:
	rm -f chevron.class
	rm -f mesh.mat
	rm -f mesh.dat.gz
