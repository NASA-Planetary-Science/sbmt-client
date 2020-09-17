package edu.jhuapl.sbmt.stateHistory.controllers.imagers;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.ui.imagers.PlannedImageView;

public class PlannedImageTableController
{
	PlannedImageView view;

	public PlannedImageTableController(final ModelManager modelManager, Renderer renderer, PlannedImageCollection collection, SmallBodyViewConfig config)
	{
		view = new PlannedImageView(collection, config);
		view.getTable().getLoadPlannedImageButton().addActionListener(e -> {

			File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
        	try
			{
				collection.loadPlannedImagesFromFileWithName(file.getAbsolutePath());
			}
        	catch (IOException e1)
			{
        		JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
			}
		});

		view.getTable().getSavePlannedImageButton().addActionListener(e -> {

			File file = CustomFileChooser.showSaveDialog(view, "Select File", "plannedImages.csv");
        	if (file == null) return;

            try
			{
				collection.savePlannedImagesToFileWithName(file.getAbsolutePath());
			}
            catch (IOException e1)
			{
            	JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
			}

		});
	}

	public PlannedImageView getView()
	{
		return view;
	}
}
