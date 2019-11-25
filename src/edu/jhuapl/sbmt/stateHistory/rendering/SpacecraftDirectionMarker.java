package edu.jhuapl.sbmt.stateHistory.rendering;

import vtk.vtkConeSource;

public class SpacecraftDirectionMarker extends vtkConeSource
{

	public SpacecraftDirectionMarker()
	{
        SetRadius(markerRadius);
        SetHeight(markerHeight);
        SetCenter(0, 0, 0);
        SetResolution(50);
        Update();
	}

	public SpacecraftDirectionMarker(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}





    // set the spacecraft pointer size - Alex W
    public void setSpacecraftPointerSize(int radius)
    {
        double rad = markerRadius *(2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
        double height = markerHeight * (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
        spacecraftMarkerHead.SetRadius(rad);
        spacecraftMarkerHead.SetHeight(height);
        spacecraftMarkerHead.Update();
        spacecraftMarkerHead.Modified();
        updateActorVisibility();
    }
}
