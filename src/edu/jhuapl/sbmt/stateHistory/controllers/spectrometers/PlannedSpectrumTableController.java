package edu.jhuapl.sbmt.stateHistory.controllers.spectrometers;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.ui.spectrometers.PlannedSpectrumView;

import lombok.Getter;

public class PlannedSpectrumTableController
{
	@Getter
	PlannedSpectrumView view;

	public PlannedSpectrumTableController(final ModelManager modelManager, Renderer renderer, PlannedSpectrumCollection collection, SmallBodyViewConfig config)
	{
		view = new PlannedSpectrumView(collection, config);
		view.getTable().getLoadPlannedSpectrumButton().addActionListener(e -> {

			File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
        	try
			{
				collection.loadPlannedSpectraFromFileWithName(file.getAbsolutePath());
			}
        	catch (IOException e1)
			{
        		JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
			}
		});

		view.getTable().getSavePlannedSpectrumButton().addActionListener(e -> {

			File file = CustomFileChooser.showSaveDialog(view, "Select File", "plannedSpectra.csv");
        	if (file == null) return;

            try
			{
				collection.savePlannedSpectraToFileWithName(file.getAbsolutePath());
			}
            catch (IOException e1)
			{
            	JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
			}

		});
	}
}