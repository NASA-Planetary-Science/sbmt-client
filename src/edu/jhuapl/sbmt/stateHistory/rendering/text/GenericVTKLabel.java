package edu.jhuapl.sbmt.stateHistory.rendering.text;

import java.awt.Font;

import vtk.vtkCaptionActor2D;

/**
 * vtkCaptionActor2D object that float near the spacecraft model, showing
 * user defined information such as "Distance to Center"
 * @author steelrj1
 *
 */
public class GenericVTKLabel extends vtkCaptionActor2D
{
	/**
	 * String to display
	 */
	private String stringToDisplay = "";

	/**
	 * Constructor.  Initializes attributes for the parent vtkCaptionActor2D object
	 */
	public GenericVTKLabel()
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
	public GenericVTKLabel(long id)
	{
		super(id);
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
		this.stringToDisplay = distanceString;
		setText(stringToDisplay);
	}

	// set the distance text from center or surface to the spacecraft - Alex W
    /**
     * Sets the current distance text based on the passed in state, spacecraft position and small body model.
     * @param state					The <pre>State</pre> object that gives the spacecraft velocity
     * @param spacecraftPosition	The spacecraft position, as a double array, relative to the body fixed frame
     * @param smallBodyModel		The spacecraft model used to compute the distance from spacecraft to surface
     */
    public void setText(String stringToDisplay)
    {
    	if (stringToDisplay != null)
    		SetCaption(stringToDisplay);
        Modified();
    }
}
