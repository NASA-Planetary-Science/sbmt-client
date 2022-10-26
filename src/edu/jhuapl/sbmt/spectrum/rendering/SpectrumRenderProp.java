package edu.jhuapl.sbmt.spectrum.rendering;

import java.awt.Color;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;

class SpectrumRenderProp
{
	// Associated properties
	/** Defines if the spectrum data should be visible. */
	boolean isVisible;
	/** Defines whether the installed ColorProvider is a custom ColorProvider */
	boolean isCustomCP;
	/** Defines the ColorProvider associated with a spectrum border. */
	ColorProvider borderColorProvider;


	/**
	 * Standard Constructor
	 */
	SpectrumRenderProp()
	{
		isVisible = true;
		isCustomCP = false;
		borderColorProvider = new ConstColorProvider(Color.GREEN);
	}
}
