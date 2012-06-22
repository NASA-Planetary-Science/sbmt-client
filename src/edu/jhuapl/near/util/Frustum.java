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
     * Given a point in the frustum compute the texture coordinates of the
     * point assuming the frustum represents the field of the view of
     * a camera.
     * @param pt desired point to compute texture coordinates for
     * @param uv returned texture coordinates as a 2 element vector
     */
    public void computeTextureCoordinates(double[] pt, int width, int height, double[] uv)
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

        adjustTextureCoordinates(width, height, uv);
    }

    /**
     * This function adjusts the texture coordinates slightly. The reason for this is
     * that in opengl, a texture coordinate value of, say, 0 (or anywhere along the boundary)
     * corresponds to the outer boundary of the pixels along the image border, not the center of
     * the pixels along the image border. However, when the field of view of the camera is
     * provided in spice instrument kernels, it is assumed that the ray pointing along the boundary
     * of the frustum points to the center of the pixels along the border, not the outer boundary
     * of the pixels.
     *
     * To give an oversimplified example, suppose the image is only 2 pixels by 2 pixels, then
     * a texture coordinate of 0, should really be set to 0.25 and a texture coordinate of 1
     * should really be 0.75. Thus, the texture coordinates need to be squeezed slightly.
     * This function does that and maps the range [0, 1] to [1/(2*width), 1-1/(2*width)]
     * or [1/(2*height), 1-1/(2*height)].
     *
     * @param width
     * @param height
     * @param uv
     */
    public static void adjustTextureCoordinates(int width, int height, double[] uv)
    {
        final double umin = 1.0 / (2.0*height);
        final double umax = 1.0 - umin;
        final double vmin = 1.0 / (2.0*width);
        final double vmax = 1.0 - vmin;

        // We need to map the [0, 1] interval into the [umin, umax] and [vmin, vmax] intervals
        uv[0] = (umax - umin) * uv[0] + umin;
        uv[1] = (vmax - vmin) * uv[1] + vmin;
    }
}
