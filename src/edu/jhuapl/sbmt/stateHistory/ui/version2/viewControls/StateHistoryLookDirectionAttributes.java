package edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls;

public class StateHistoryLookDirectionAttributes
{
	double[] upVector;
	double[] lookFromDirection;
	boolean zoomOnly;

	public StateHistoryLookDirectionAttributes(double[] upVector, double[] lookFromDirection, boolean zoomOnly)
	{
		super();
		this.upVector = upVector;
		this.lookFromDirection = lookFromDirection;
		this.zoomOnly = zoomOnly;
	}

	public double[] getUpVector()
	{
		return upVector;
	}

	public double[] getLookFromDirection()
	{
		return lookFromDirection;
	}

	public boolean isZoomOnly()
	{
		return zoomOnly;
	}

}
