package edu.jhuapl.sbmt.common.client;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;

@FunctionalInterface
public interface InfoWindowManagerBuilder<Model> 
{
	ModelInfoWindow buildModelInfoWindow(Model model);
}
