package edu.jhuapl.sbmt.stateHistory.rendering.text;

import java.awt.Font;

import vtk.vtkCaptionActor2D;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;

/**
 * vtkCaptionActor2D object that float near the spacecraft model, showing
 * user defined information such as "Distance to Center"
 * @author steelrj1
 *
 */
public class SpacecraftLabel extends vtkCaptionActor2D
{
	/**
	 * Scaling factor
	 */
	private static final double JupiterScale = 75000;

	/**
	 * Distance string, set to a default of "Distance to Center"
	 */
	private String distanceString = "Distance to Center";

	/**
	 * The passed in <pre>State</pre> object for this label
	 */
	private State state;

	/**
	 * The spacecraft position array in body fixed coordinates
	 */
	private double[] spacecraftPosition;

	/**
	 * The small body model used to calculate intercepts
	 */
	private SmallBodyModel smallBodyModel;

	/**
	 * Constructor.  Initializes attributes for the parent vtkCaptionActor2D object
	 */
	public SpacecraftLabel()
	{
		 SetCaption("");
		 GetTextActor().SetTextScaleModeToNone();
         GetCaptionTextProperty().SetColor(1.0, 1.0, 1.0);
         GetCaptionTextProperty().SetJustificationToLeft();
         GetCaptionTextProperty().BoldOff();
         GetCaptionTextProperty().ShadowOff();
         SetPosition(0.0, 0.0);
         SetWidth(0.2);
         SetHeight(.6);
         SetBorder(0);
         SetLeader(0);
         VisibilityOff();
	}

	/**
	 * vtk based Constructor
	 * @param id
	 */
	public SpacecraftLabel(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sets the font for the displayed string
	 * @param font
	 */
	public void setDistanceStringFont(Font font)
	{
		GetCaptionTextProperty().SetFontSize(font.getSize());
		GetCaptionTextProperty().SetFontFamilyAsString(font.getFamily());
		if (font.isBold()) GetCaptionTextProperty().BoldOn(); else GetCaptionTextProperty().BoldOff();
		if (font.isItalic()) GetCaptionTextProperty().ItalicOn(); else GetCaptionTextProperty().ItalicOff();
	}

	/**
	 * Sets the distance string property, then updates the rendered object
	 * @param distanceString
	 */
	public void setDistanceString(String distanceString)
	{
		this.distanceString = distanceString;
		setDistanceText(state, spacecraftPosition, smallBodyModel);
	}

	// set the distance text from center or surface to the spacecraft - Alex W
    /**
     * Sets the current distance text based on the passed in state, spacecraft position and small body model.
     * @param state					The <pre>State</pre> object that gives the spacecraft velocity
     * @param spacecraftPosition	The spacecraft position, as a double array, relative to the body fixed frame
     * @param smallBodyModel		The spacecraft model used to compute the distance from spacecraft to surface
     */
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

        double velocity[] = state.getSpacecraftVelocity();
        if (velocity == null) return;
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
        if (result == -1) return;
        double smallBodyRadius = Math.sqrt(spacecraftMarkerPosition[0]*spacecraftMarkerPosition[0] + spacecraftMarkerPosition[1]*spacecraftMarkerPosition[1] + spacecraftMarkerPosition[2]*spacecraftMarkerPosition[2]);
        if (distanceOption == 1)
        {
            radius = radius - smallBodyRadius;
        }

        String speedText = String.format("%7.1f km %7.3f km/sec   .", radius, speed);
        SetCaption(speedText);
        Modified();
    }
}
