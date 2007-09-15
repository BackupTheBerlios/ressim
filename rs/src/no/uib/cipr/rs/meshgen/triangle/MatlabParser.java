package no.uib.cipr.rs.meshgen.triangle;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import no.uib.cipr.rs.geometry.Point3D;

/**
 * Read lines from a file that has been exported as an array from Matlab
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class MatlabParser implements Source {
    // source from which we read the coordinates
    private Scanner scanner;
    
    public MatlabParser(String inputFile) throws FileNotFoundException {
        // if no file name is given, then use piped data
        if(inputFile == null) {
            scanner = new Scanner(System.in);
        }
        else {
            scanner = new Scanner(new FileReader(inputFile));
        }
    }
    
    // read a line from the file. this method will mutate the scanner
    // member of the object (advance the input).
    private Line readNextLine() throws TriExc {
        // each line consists of four coordinates, the x- and y-value
        // of the start and end of the line respectively
        double x1 = scanner.nextDouble();
        double y1 = scanner.nextDouble();
        double x2 = scanner.nextDouble();
        double y2 = scanner.nextDouble();
        
        // boundary flag
        int marker = (int) scanner.nextDouble();
        Kind kind = Kind.fromMarker(marker);
        
        // turn each pair of values into a point, and then the pair of
        // points into a vector, which represents the line
        Point3D p1 = new Point3D(x1, y1, 0d);
        Point3D p2 = new Point3D(x2, y2, 0d);
                
        // return the line as a pair of points and a boolean flag
        return new Line(p1, p2, kind);
    }
    
    private ArrayList<Line> readEntireFile() throws TriExc {
        // list of lines that is read from the input
        ArrayList<Line> lines = new ArrayList<Line>();
        
        // continue reading more points while there are more left
        while(scanner.hasNext()) {
            Line l = readNextLine();
            lines.add(l);
        }
        return lines;
    }
    
    private Map<Point3D, Integer> sendPoints(PointHandler pointHandler,
            ArrayList<Line> lines) throws TriExc {
        // mapping from the points to the indices given by the sink
        Map<Point3D, Integer> map = new HashMap<Point3D, Integer>();

        pointHandler.prepareForPoints(2*lines.size());
        for(Line l : lines) {
            for(Point3D p : l.points) {
                int i = pointHandler.onPoint(p.x(), p.y(), p.z());
                map.put(p, i);
            }
        }
        pointHandler.closePoints();
        return map;
    }
    
    private void sendLines(FractureHandler fractureHandler,
            ArrayList<Line> lines, Map<Point3D, Integer> map)
            throws TriExc {
        fractureHandler.prepareForFractures(lines.size());
        for(Line l : lines) {
            Point3D p1 = l.points[0];
            Point3D p2 = l.points[1];
            int i1 = map.get(p1);
            int i2 = map.get(p2);
            fractureHandler.onFracture(i1, i2, l.kind);
        }
        fractureHandler.closeFractures();
    }
    
    public void readAll(PointHandler pointHandler, 
                FractureHandler fractureHandler) throws TriExc {
        // read the entire input file into memory        
        ArrayList<Line> lines = readEntireFile();
        
        Map<Point3D, Integer> map = sendPoints(pointHandler, lines);
        sendLines(fractureHandler, lines, map);
    }
    
    // make sure that the files are closed when we are done with them
    public void close() throws IOException {
        scanner.close();
    }
}
