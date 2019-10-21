package edu.jhuapl.sbmt.spectrum.ui.color;

import java.util.Hashtable;

import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;

public class SpectrumColoringFactory
{
	private static Hashtable<SpectrumColoringStyle, ISpectrumColoringPanel> coloringPanels = new Hashtable<SpectrumColoringStyle, ISpectrumColoringPanel>();

	public static void registerColoringPanel(SpectrumColoringStyle style, ISpectrumColoringPanel panel)
	{
		coloringPanels.put(style, panel);
	}

	public static ISpectrumColoringPanel getColoringPanelForStyle(SpectrumColoringStyle style)
	{
		return coloringPanels.get(style);
	}

}
