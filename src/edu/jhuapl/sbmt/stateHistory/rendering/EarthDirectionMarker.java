package edu.jhuapl.sbmt.stateHistory.rendering;

import vtk.vtkConeSource;

public class EarthDirectionMarker extends vtkConeSource
{

	public EarthDirectionMarker()
	{
		// TODO Auto-generated constructor stub
	}

	public EarthDirectionMarker(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}




	// set the earth pointer size - Alex W
    public void setEarthPointerSize(int radius)
    {
        double rad = markerRadius * (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);//markerRadius * ((4.5/100.0)*(double)radius + 0.5);
        double height = markerHeight * (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
        SetRadius(rad);
        SetHeight(height);
        Update();
        Modified();
        updateActorVisibility();
    }
}
