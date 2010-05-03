package edu.jhuapl.near.util;

/**
 * This class contains miscellaneous geometry functions. Many of these functions
 * are from the SPICE toolkit that have been ported to Java.
 * @author eli
 *
 */
public class GeometryUtil 
{
	/**
	 * Convert lat lon to cartesian coordinates.
	 * @param latLon
	 * @return xyz
	 */
	static public double[] latrec(LatLon latLon)
	{
		double xyz[] = new double[3];
        xyz[0] = latLon.rad * Math.cos( latLon.lon ) * Math.cos( latLon.lat );
        xyz[1] = latLon.rad * Math.sin( latLon.lon ) * Math.cos( latLon.lat );
        xyz[2] = latLon.rad * Math.sin( latLon.lat );

        return xyz;
	}
	
	/**
	 * Convert cartesian coordinates to lat lon. Copied from spice's reclat function.
	 * @param xyz
	 * @return latLon
	 */
	static public LatLon reclat(double[] rectan)
	{
		LatLon llr = new LatLon();

		//vmax = MaxAbs(  rectan[0], MaxAbs( rectan[1], rectan[2] )   );
		double vmax = Math.max(  Math.abs(rectan[0]), Math.max( Math.abs(rectan[1]), Math.abs(rectan[2]) ) );

		if ( vmax > 0.)
		{
			double x1 = rectan[0] / vmax;
			double y1 = rectan[1] / vmax;
			double z1 = rectan[2] / vmax;
			llr.rad   = vmax * Math.sqrt( x1*x1 + y1*y1 + z1*z1 );
			llr.lat   = Math.atan2(z1, Math.sqrt( x1*x1 + y1*y1 ) );

			if ( x1 == 0. && y1 == 0.)
			{
				llr.lon = 0.;
			}
			else
			{
				llr.lon = Math.atan2(y1, x1);
			}
		}
		else
		{
			// The vector is the zero vector.

			llr.rad = 0.;
			llr.lon = 0.;
			llr.lat = 0.;
		}
		
		return llr;
	}

	/**
	 * Copied from SPICE
	 * @param v1
	 * @return
	 */
	static public double vnorm(double[] v1)
	{
		   /*                                                                                                                         
		   Local variables                                                                                                            
		   */                                                                                                                         
		   double                        normSqr;
		   double                        tmp0;
		   double                        tmp1;
		   double                        tmp2;
		   double                        v1max;

		   /*
		   Determine the maximum component of the vector.
		   */
		   //v1max = MaxAbs(  v1[0],   MaxAbs( v1[1], v1[2] )   );
		   v1max = Math.max(  Math.abs(v1[0]), Math.max( Math.abs(v1[1]), Math.abs(v1[2]) ) );

		   /*
		   If the vector is zero, return zero; otherwise normalize first.
		   Normalizing helps in the cases where squaring would cause overflow
		   or underflow.  In the cases where such is not a problem it not worth
		   it to optimize further.
		   */

		   if ( v1max == 0.0 )
		   {
		      return ( 0.0 );
		   }
		   else
		   {
		      tmp0     =  v1[0]/v1max;
		      tmp1     =  v1[1]/v1max;
		      tmp2     =  v1[2]/v1max;

		      normSqr  =  tmp0*tmp0 + tmp1*tmp1 + tmp2*tmp2;

		      return (  v1max * Math.sqrt( normSqr )  );
		   }
	}
	
	/**
	 * Copied from spice.
	 * Note unlike the original spice which takes a 3rd argument as output, here we return it from the function.
	 * @param v1
	 * @param vout
	 * @return
	 */
	static public double unorm(double[] v1, 
							   double[] vout)
	{
		   /*
		   Obtain the magnitude of v1.  Note:  since vmage is a pointer, the
		   value of what vmag is pointing at is *vmag.
		   */

		   double vmag = vnorm( v1 );

		   
		   
		   /*
		   If *vmag is nonzero, then normalize.  Note that this process is
		   numerically stable: overflow could only happen if vmag were small,
		   but this could only happen if each component of v1 were small.
		   In fact, the magnitude of any vector is never less than the
		   magnitude of any component.
		   */

		   if ( vmag > 0.0 )
		      {
		      vout[0] = v1[0] / vmag;
		      vout[1] = v1[1] / vmag;
		      vout[2] = v1[2] / vmag;
		      }
		   else
		      {
		      vout[0] = 0.;
		      vout[1] = 0.;
		      vout[2] = 0.;
		      }
		   
		   return vmag;
	}

	/**
	 * Copied from spice
	 * @param v1
	 * @param v2
	 * @return
	 */
	static public double vdot( double[] v1, double[] v2 )
	{
		return ( v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2] );
	}
	
	/**
	 * Copied from spice
	 * @param v1
	 * @param v2
	 * @return
	 */
	static public double vsep( double[] v1, double[] v2 )
	{
		   /*
		   Local variables

		   The following declarations represent, respectively:

		   Magnitudes of v1, v2
		   Either of the difference vectors: v1-v2 or v1-(-v2)
		   Unit vectors parallel to v1 and v2
		   */

		   double     dmag1;
		   double     dmag2;
		   double[]   vtemp = new double[3];
		   double[]   u1 = new double[3];
		   double[]   u2 = new double[3];
		   double     vsep;


		   /*
		   Calculate the magnitudes of v1 and v2; if either is 0, vsep = 0
		   */

		   dmag1 = unorm ( v1, u1 );

		   if ( dmag1 == 0.0 )
		      {
		      vsep = 0.0;
		      return vsep;
		      }
		 
		      dmag2 = unorm ( v2, u2 );

		   if ( dmag2 == 0.0 )
		      {
		      vsep = 0.0;
		      return vsep;
		      }
		 
		   if ( vdot(u1,u2) > 0. )
		      {
		      vtemp[0] = u1[0] - u2[0];
		      vtemp[1] = u1[1] - u2[1];
		      vtemp[2] = u1[2] - u2[2];
		 
		      vsep = 2.00 * Math.asin (0.50 * vnorm(vtemp));
		      }
		 
		   else if ( vdot(u1,u2) < 0. )
		      {
		      vtemp[0] = u1[0] + u2[0];
		      vtemp[1] = u1[1] + u2[1];
		      vtemp[2] = u1[2] + u2[2];
		 
		      vsep = Math.PI - 2.00 * Math.asin (0.50 * vnorm(vtemp));
		      }

		   else
		      {
		      vsep = 0.5 * Math.PI;
		      }
		 

		   return vsep;

	}

	/**
	 * Copied from spice
	 * @param v1
	 * @param vout
	 */
	static public void vhat( double[] v1, double[] vout )
	{
		   /*                                                                                                                                                                                        
		   Local variables                                                                                                                                                                           
		   */
		   double                        vmag;


		   /*                                                                                                                                                                                        
		   Obtain the magnitude of v1.                                                                                                                                                               
		   */
		   vmag = vnorm(v1);

		   /*                                                                                                                                                                                        
		   If vmag is nonzero, then unitize.  Note that this process is                                                                                                                              
		   numerically stable: overflow could only happen if vmag were small,                                                                                                                        
		   but this could only happen if each component of v1 were small.                                                                                                                            
		   In fact, the magnitude of any vector is never less than the                                                                                                                               
		   magnitude of any component.                                                                                                                                                               
		   */

		   if ( vmag > 0.0 )
		      {
		      vout[0] = v1[0] / vmag;
		      vout[1] = v1[1] / vmag;
		      vout[2] = v1[2] / vmag;
		      }
		   else
		      {
		      vout[0] = 0.0;
		      vout[1] = 0.0;
		      vout[2] = 0.0;
		      }
	}

	/**
	 * Compute the distance between 2 3D points
	 * @param pt1
	 * @param pt2
	 * @return
	 */
	static public double distanceBetween(double[] pt1, double[] pt2)
	{
		double[] vec = {
				pt2[0]-pt1[0],
				pt2[1]-pt1[1],
				pt2[2]-pt1[2]
		};
		return GeometryUtil.vnorm(vec);
	}
}
