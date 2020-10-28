package edu.jhuapl.sbmt.stateHistory.controllers.lidars;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.stateHistory.ui.lidars.PlannedLidarTrackView;

public class PlannedLidarTableController
{
	PlannedLidarTrackView view;

	public PlannedLidarTableController(final ModelManager modelManager, Renderer renderer, PlannedLidarTrackCollection collection, SmallBodyViewConfig config)
	{
		view = new PlannedLidarTrackView(collection, config);
		view.getTable().getLoadPlannedLidarTrackButton().addActionListener(e -> {

			File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
        	try
			{
				collection.loadPlannedLidarTracksFromFileWithName(file.getAbsolutePath());
			}
        	catch (IOException e1)
			{
        		JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
			}
		});

		view.getTable().getSavePlannedLidarTrackButton().addActionListener(e -> {

			File file = CustomFileChooser.showSaveDialog(view, "Select File", "plannedLidarTracks.csv");
        	if (file == null) return;

            try
			{
				collection.savePlannedLidarTracksToFileWithName(file.getAbsolutePath());
			}
            catch (IOException e1)
			{
            	JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
			}

		});
	}

	/**
	 * @return the view
	 */
	public PlannedLidarTrackView getView()
	{
		return view;
	}
}