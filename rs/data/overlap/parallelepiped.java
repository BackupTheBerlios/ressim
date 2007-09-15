public class parallelepiped {
    public static void main(String[] args) {
	double a = 20; //Double.parseDouble(args[0]);
	double s = Math.sin(Math.PI/180.*a);
	double c = Math.cos(Math.PI/180.*a);

	// alias for each of the dimensions
	final int X = 0;
	final int Y = 1;
	final int Z = 2;

	// unit vectors
	double[][] e = new double[][] {
	    { c, s, s }, // x
	    { s, c, s }, // y
	    { s, s, c }  // z
	};

	// points for the parallelepiped, specified as coordinate in the transformed
	// coordinate system (i.e. how many of the unit vectors above)
	double[][][] u = new double[/*layer*/][/*coordline*/][/*xyz-component*/] {
	    {
		{ 0, 0, 0 },
		{ 1.5, 0, 0 },
                { 0, 1, 0 },
                { 1.5, 1, 0 }
	    },
	    {
		{ 0, 0, 1 },
		{ 1.5, 0, 1 },
		{ 0, 1, 1 },
		{ 1.5, 1, 1 }
	    }
	};

	// convert each of the transformed coordinates to real-life coordinates
	double[][][] p = new double[u.length][][];
	for(int i = 0; i < u.length; i++) {
	    p[i] = new double[u[i].length][];
	    for(int j = 0; j < u[i].length; j++) {
		p[i][j] = new double[u[i][j].length];

		for(int m = 0; m < u[i][j].length; m++) {
		    for(int n = 0; n < e[m].length; n++) {
			p[i][j][n] += u[i][j][m] * e[m][n];
		    }
			    
		}
	    }
	}

	System.out.printf("GRID%n");
	System.out.printf("SPECGRID%n");
	System.out.printf("    1  1  1    1  F%n");
	System.out.printf("/%n");
	System.out.printf("COORD%n");
	for(int j = 0; j < p[0].length; j++) {
	    for(int i = 0; i < p.length; i++) {
		System.out.printf("  ");
		for(int k = 0; k < p[i][j].length; k++) {
		    System.out.printf("  %1.4f", p[i][j][k]);
		}
	    }
	    System.out.printf("%n");
	}
	System.out.printf("/%n");
	System.out.printf("ZCORN%n");
	for(int i = 0; i < p.length; i++) {
	    for(int j = 0; j < p[i].length; j++) {
		System.out.printf("  %1.4f", p[i][j][Z]);
	    }
	    System.out.printf("%n");
	}
	System.out.printf("/%n");
	System.out.printf("ACTNUM%n");
	System.out.printf("    %d*1%n", 1);
	System.out.printf("/%n");
	System.out.printf("PORO%n");
	System.out.printf("    %d*0.6%n", 1);
	System.out.printf("/%n");
	System.out.printf("PERMX%n");
	System.out.printf("    %d*100%n", 1);
	System.out.printf("/%n");
	System.out.printf("PERMY%n");
	System.out.printf("    %d*100%n", 1);
	System.out.printf("/%n");
	System.out.printf("PERMZ%n");
	System.out.printf("    %d*100%n", 1);
	System.out.printf("/%n");
	System.out.printf("/%n");
	System.out.printf("END%n");
    }
}
