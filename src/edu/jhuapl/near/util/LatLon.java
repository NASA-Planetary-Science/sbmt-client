package edu.jhuapl.near.util;

public class LatLon
{
	public double lat;
	public double lon;
	public double rad;

	public LatLon(double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
	}
	
	public LatLon()
	{
		
	}
	
	/**
	 * Convert cartesian coordinates to lat lon. Copied from spice's reclat function.
	 * @param xyz
	 * @return
	 */
	static public LatLon recToLatLon(double[] rectan)
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
}
