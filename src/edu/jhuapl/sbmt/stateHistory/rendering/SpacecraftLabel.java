package edu.jhuapl.sbmt.stateHistory.rendering;

import vtk.vtkCaptionActor2D;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;

public class SpacecraftLabel extends vtkCaptionActor2D
{
	private static final double JupiterScale = 75000;
	private String distanceString = "Distance to Center";
	private State state;
	private double[] spacecraftPosition;
	private SmallBodyModel smallBodyModel;

	public SpacecraftLabel()
	{
		 SetCaption("");
         GetCaptionTextProperty().SetColor(1.0, 1.0, 1.0);
         GetCaptionTextProperty().SetJustificationToLeft();
         GetCaptionTextProperty().BoldOff();
         GetCaptionTextProperty().ShadowOff();
         SetPosition(0.0, 0.0);
         SetWidth(0.2);
         SetHeight(.6);
         SetBorder(0);
         SetLeader(0);
         VisibilityOn();
	}

	public SpacecraftLabel(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	public void setDistanceString(String distanceString)
	{
		this.distanceString = distanceString;
		setDistanceText(state, spacecraftPosition, smallBodyModel);
	}

	// set the distance text from center or surface to the spacecraft - Alex W
    public void setDistanceText(State state, double[] spacecraftPosition, SmallBodyModel smallBodyModel)
    {
    	this.state = state;
    	this.spacecraftPosition = spacecraftPosition;
    	this.smallBodyModel = smallBodyModel;
    	int distanceOption = 0;
    	if(distanceString.equals("Distance to Surface"))
        {
            distanceOption = 1;
        }
//        State state = currentFlybyStateHistory.getCurrentValue();

//        double[] spacecraftPosition = currentFlybyStateHistory.getSpacecraftPosition();
        double velocity[] = state.getSpacecraftVelocity();
        double speed = Math.sqrt(velocity[0]*velocity[0] + velocity[1]*velocity[1] + velocity[2]*velocity[2]);

        double[] spacecraftMarkerPosition = new double[3];
        double[] spacecraftDirection = new double[3];
        double[] spacecraftViewpoint = new double[3];
        double[] spacecraftViewDirection = new double[3];
        MathUtil.unorm(spacecraftPosition, spacecraftDirection);
        MathUtil.vscl(JupiterScale, spacecraftDirection, spacecraftViewpoint);
        MathUtil.vscl(-1.0, spacecraftDirection, spacecraftViewDirection);

        double radius = Math.sqrt(spacecraftPosition[0]*spacecraftPosition[0] + spacecraftPosition[1]*spacecraftPosition[1] + spacecraftPosition[2]*spacecraftPosition[2]);
        double result = smallBodyModel.computeRayIntersection(spacecraftViewpoint, spacecraftViewDirection, spacecraftMarkerPosition);
        double smallBodyRadius = Math.sqrt(spacecraftMarkerPosition[0]*spacecraftMarkerPosition[0] + spacecraftMarkerPosition[1]*spacecraftMarkerPosition[1] + spacecraftMarkerPosition[2]*spacecraftMarkerPosition[2]);
        if (distanceOption == 1)
        {
            radius = radius - smallBodyRadius;
        }

        String speedText = String.format("%7.1f km %7.3f km/sec   .", radius, speed);
        SetCaption(speedText);
        Modified();
//        updateActorVisibility();
    }

}
