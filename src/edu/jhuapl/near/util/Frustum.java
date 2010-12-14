package edu.jhuapl.near.util;

public class Frustum
{
    // location of the origin of frustum
    public final double[] origin;// = new double[3];

    // vector pointing in upper left of frustum
    public final double[] ul;// = new double[3];

    // vector pointing in upper right of frustum
    public final double[] ur;// = new double[3];

    // vector pointing in lower left of frustum
    public final double[] ll;// = new double[3];

    // vector pointing in lower right of frustum
    public final double[] lr;// = new double[3];

    private final double a;
    private final double b;

    public Frustum(
            double[] origin,
            double[] ul,
            double[] ur,
            double[] ll,
            double[] lr)
    {
        this.origin = origin.clone();
        this.ul = ul.clone();
        this.ur = ur.clone();
        this.ll = ll.clone();
        this.lr = lr.clone();

        a = MathUtil.vsep(ul, ur);
        b = MathUtil.vsep(ul, lr);
    }

    /**
     * Given any point in 3D space compute the texture coordinates of the
     * point assuming the frustum represents the field of the view of
     * a camera.
     * @param pt desired point to compute texture coordinates for
     * @param uv returned texture coordinates as a 2 element vector
     */
    public void computeTextureCoordinates(double[] pt, double[] uv)
    {
        double[] vec = {
             pt[0] - origin[0],
             pt[1] - origin[1],
             pt[2] - origin[2]};

        MathUtil.vhat(vec, vec);

        double d1 = MathUtil.vsep(vec, ul);
        double d2 = MathUtil.vsep(vec, lr);

        double v = (d1*d1 + b*b - d2*d2) / (2.0*b);
        double u = d1*d1 - v*v;
        if (u <= 0.0)
            u = 0.0;
        else
            u = Math.sqrt(u);

        //System.out.println(v/b + " " + u/a + " " + d1 + " " + d2);

        v = v/b;
        u = u/a;

        if (v < 0.0) v = 0.0;
        else if (v > 1.0) v = 1.0;

        if (u < 0.0) u = 0.0;
        else if (u > 1.0) u = 1.0;

        uv[0] = u;
        uv[1] = v;
    }
}
