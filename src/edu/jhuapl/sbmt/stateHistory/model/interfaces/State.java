package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import crucible.core.math.vectorspace.UnwritableVectorIJK;

/**
 * @author steelrj1
 *
 */
public interface State
{
    /**
     * @return
     */
    public double getEphemerisTime();

    /**
     * @return
     */
    public String getUtc();

    /**
     * @param path
     */
    public void writeToCSV(String path);

    /**
     * @param instrumentFrameName
     * @param index
     * @return
     */
    public UnwritableVectorIJK getFrustum(String instrumentFrameName, int index);

    /**
     * @param instrumentFrameName
     * @return
     */
    public double[] getInstrumentLookDirection(String instrumentFrameName);

    /** Spacecraft Position (km) */
    /**
     * @return
     */
    public double[] getSpacecraftPosition();

    /** Spacecraft Velocity (km/s) */
    /**
     * @return
     */
    public double[] getSpacecraftVelocity();

    /** Earth Position (km) */
    /**
     * @return
     */
    public double[] getEarthPosition();

    /** Sun Position (km) */
    /**
     * @return
     */
    public double[] getSunPosition();


    /**
     * @return
     */
    public double[] getSpacecraftXAxis();

    /**
     * @return
     */
    public double[] getSpacecraftYAxis();

    /**
     * @return
     */
    public double[] getSpacecraftZAxis();

//  /**
//  * @return
//  */
// public double getViewingAngle();
//
// /** Roll Angle (deg) */
// /**
//  * @return
//  */
// public double getRollAngle();
//
// /** Spacecraft Altitude (km) */
// /**
//  * @return
//  */
// public double getSpacecraftAltitude();
//
// /** Intercept XYZ (km) - derived from SurfaceIntercept */
// /**
//  * @return
//  */
// public double[] getSurfaceIntercept();
//
// /** Intercept Lat-Lon (deg) */
// /**
//  * @return
//  */
// public double[] getSurfaceInterceptLatLon();
//
// /** Cross Track Spacing (km) */
// /**
//  * @return
//  */
// public double getCrossTrackPixelSpacingKm();
//
// /** Along Track Spacing (km) */
// /**
//  * @return
//  */
// public double getAlongTrackPixelSpacing();
//
// /** Solar Incidence Angle (deg) */
// /**
//  * @return
//  */
// public double getSolarIncidenceAngle();
//
// /** Emission Angle (deg) */
// /**
//  * @return
//  */
// public double getEmissionAngle();
//
// /** Local Solar Time (deg) */
// /**
//  * @return
//  */
// public double getLocalSolarTime();

//  /** Sub Solar Point Lat-Lon (deg) */
//  /**
//   * @return
//   */
//  public double[] getSubSolarPointLatLon();
//
//  /**
//   * @return
//   */
//  public float getFrameScore();

}
