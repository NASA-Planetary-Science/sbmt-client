package edu.jhuapl.sbmt.gui.image.ui.images;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.popup.PopupManager;

public class ImagePickManager extends PickManager
{
	public ImagePickManager(Renderer renderer, StatusBar statusBar, ModelManager modelManager, PopupManager popupManager)
	{
		super(renderer, popupManager, PickUtil.formNonDefaultPickerMap(renderer, modelManager),
				new ImageDefaultPicker(renderer, statusBar, modelManager, popupManager));
	}
}