package edu.jhuapl.sbmt.dtm.ui.menu;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.sbmt.dtm.model.DEMKey;

public interface IDEMPopupMenuActionListener
{
	public void mapDEM(List<DEMKey> keys, boolean selected);
	public void showDEM(List<DEMKey> keys, boolean selected);
	public void showDEMBoundary(List<DEMKey> keys, boolean selected);
	public void centerDEM(List<DEMKey> demKeys, Renderer renderer);
	public void showDEMProperties(List<DEMKey> demKeys, PolyhedralModel smallBodyModel);
	public void saveDEMToFITS(List<DEMKey> keys, Component invoker);
	public void changeDEMOpacity(List<DEMKey> keys, Renderer renderer);
	public void setDEMBoundaryColor(List<DEMKey> keys, Color color);
	public void exportDEMToCustomModel(List<DEMKey> demKeys, PolyhedralModel smallBodyModel);
}
