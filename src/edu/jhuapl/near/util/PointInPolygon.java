// This file was stolen from scibox

package edu.jhuapl.near.util;

public class PointInPolygon {

	/**
	 * Given coordinates (plon, plat) and a polygon with vertices (lon[], lat[]), return true if
	 * the point falls within the polygon.  If the point is on an edge or on a vertex, it may be 
	 * either inside or outside depending on roundoff errors.  The vertices may be given either 
	 * clockwise or counterclockwise.  
	 * 
	 * @param plon - longitude of the point to test, in radians
	 * @param plat - latitude of the point to test, in radians
	 * @param lon - array of longitudes for the vertices, in radians
	 * @param lat - array of latitudes for the vertices, in radians
	 * @return
	 */
	public static boolean pointInPolygonGeo(double plon, double plat, double [] lon, double [] lat)
	{
		final int n = lon.length;
		
		// make sure all longitudes are in the range -PI to PI
		if (plon > Math.PI) plon -= 2*Math.PI;
		if (plon < -Math.PI) plon += 2*Math.PI;
		for (int i=0; i<n; i++) {
			if (lon[i] > Math.PI) lon[i] -= 2*Math.PI;
			if (lon[i] < -Math.PI) lon[i] += 2*Math.PI;
		}

		boolean containsMeridian = false;
		for (int i=0; i<n; i++) {
			// if sin(lon[i]) has an opposite sign from sin(lon[0]) then 
			// this footprint contains either the 0 or 180 meridian
			if (Math.sin(lon[i]) * Math.sin(lon[0]) <=0) {
				containsMeridian = true;
				break;
			}
		}

		boolean inout = false;
		if (containsMeridian) {
			// if the footprint contains the 0 or 180 meridian, use sin(lon) to describe the polygon so
			// we avoid any discontinuities from -180<lon<180 or 0<lon<360
			// NOTE: there can be a false positive if the box contains lon 0 or 180 and the point is 180 degrees away.  
			// Use a distance test to weed those cases out before calling this routine.
			double [] sin_lon = new double[n];
			for (int i=0; i<n; i++) sin_lon[i] = Math.sin(lon[i]);
			inout = pointInPolygon(Math.sin(plon), plat, sin_lon, lat);
		} else {
			inout = pointInPolygon(plon, plat, lon, lat);
		}

//		System.out.print("PointInPolygon: ("+plat+", "+plon+")");
//		if (inout) {
//			System.out.print(" is");
//		} else {
//			System.out.print(" isn't");
//		}
//		System.out.println(" in polygon with vertices:");
//		for (int i=0; i<n; i++) {
//			System.out.println(i+": ("+lat[i]+", "+lon[i]+")");
//		}
		
		return inout;
	}
	
	/**
	 * Given a point (px, py) and a polygon with vertices (xx[], yy[]), return true if
	 * the point falls within the polygon.  If the point is on an edge or on a vertex, 
	 * it may be either inside or outside depending on roundoff errors.  
	 * The vertices may be given either clockwise or counterclockwise.
	 * 
	 * @param px
	 * @param py
	 * @param xx - array of X coordinates for the vertices
	 * @param yy - array of Y coordinates for the vertices
	 * @return
	 */
	public static boolean pointInPolygon(double px, double py, double [] xx, double [] yy)
	{
		// See http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html

		if (xx.length != yy.length) {
			// error - need to have the same length
		}

		final int n = xx.length;
		
		double [] x = new double[n];
		double [] y = new double[n];
		for (int i = 0; i < n; i++)
		{			
			x[i] = xx[i] - px;
			y[i] = yy[i] - py;
		}
	
		boolean inout = false;	
		for (int i = 0; i < n; i++) {
			int j = (i+1) % n;

			boolean mx = (x[i] >= 0);
			boolean nx = (x[j] >= 0);
			boolean my = (y[i] >= 0);
			boolean ny = (y[j] >= 0);
						
			if (!((my || ny) && (mx || nx)) || (mx && nx)) continue;
			
			if (!(my && ny && (mx || nx) && !(mx && nx))) {
				
				double value = (y[i]*x[j]-x[i]*y[j]) / (x[j]-x[i]);				
				if (value < 0) continue;
				
				if (value == 0) {
					inout = true;
					break;
				}
			} else {
				inout = !inout;
			}
		}

		return inout;
	}
}
