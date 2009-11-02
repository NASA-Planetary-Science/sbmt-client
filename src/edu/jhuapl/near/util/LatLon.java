package edu.jhuapl.near.util;

public class LatLon
{
	public double lat;
	public double lon;
	public LatLon(double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
	}
	
	public LatLon()
	{
		
	}
	
	/**
	 * Convert cartesian coordinates to lat lon. Copied from spice's reclat_c function.
	 * @param xyz
	 * @return
	 */
	static public LatLon recToLatLon(double[] rectan)
	{
		double   vmax;
		double   x1;
		double   y1;
		double   z1;

		LatLon ll = new LatLon();
		/* Function Body */

		//vmax = MaxAbs(  rectan[0], MaxAbs( rectan[1], rectan[2] )   );
		vmax = Math.max(  Math.abs(rectan[0]), Math.max( Math.abs(rectan[1]), Math.abs(rectan[2]) ) );

		if ( vmax > 0.)
		{
			x1        = rectan[0] / vmax;
			y1        = rectan[1] / vmax;
			z1        = rectan[2] / vmax;
			//*radius   = vmax * Math.sqrt( x1*x1 + y1*y1 + z1*z1 );
			ll.lat = Math.atan2(z1, Math.sqrt( x1*x1 + y1*y1 ) );


			if ( x1 == 0. && y1 == 0.)
			{
				ll.lon = 0.;
			}

			else
			{
				ll.lon = Math.atan2(y1, x1);
			}

		}

		else
		{

			/*
		      The vector is the zero vector.
			 */

			//*radius    = 0.;
			ll.lon = 0.;
			ll.lat  = 0.;
		}
		
		return ll;
	}
}
