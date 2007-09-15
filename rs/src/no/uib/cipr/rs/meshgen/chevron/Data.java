package no.uib.cipr.rs.meshgen.chevron;

import java.io.Serializable;

/**
 * Raw data format as specified by 
 * m = load('data.mat');
 * Model = m.Model;
 * save('m.mat','Model','-V6');
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Data implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 4602121264455696008L;
    public int ni;
    public int nj;
    public int nk;
    public double[][][] x;
    public double[][][] y;
    public double[][][] z;
    public double[][][] kx;
    public double[][][] ky;
    public double[][][] kz;
    public double[][][] kxy;
    public double[][][] kxz;
    public double[][][] kyz;
}
