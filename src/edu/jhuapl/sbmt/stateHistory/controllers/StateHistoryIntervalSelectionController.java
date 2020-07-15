package edu.jhuapl.sbmt.stateHistory.controllers;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.table.StateHistoryTableView;

import glum.item.ItemEventType;

/**
 * Controller that governs the "Available Files" panel for the StateHistory tab
 * @author steelrj1
 *
 */
public class StateHistoryIntervalSelectionController
{
    private StateHistoryTableView view;

	/**
	 * @param historyModel
	 * @param bodyModel
	 * @param renderer
	 */
	public StateHistoryIntervalSelectionController(StateHistoryModel historyModel, SmallBodyModel bodyModel, Renderer renderer)
	{
		initializeIntervalSelectionPanel(historyModel, bodyModel, renderer);
	}

	/**
	 * Sets up listeners for various UI components
	 */
	private void initializeIntervalSelectionPanel(StateHistoryModel historyModel, SmallBodyModel bodyModel, Renderer renderer)
    {
		StateHistoryCollection runs = historyModel.getRuns();
		view = new StateHistoryTableView(runs);

		//Queries for a file to load history from, passing to the model for loading
        view.getLoadStateHistoryButton().addActionListener(e -> {

        	File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
        	try
			{
				historyModel.loadIntervalFromFile(file, bodyModel);
			}
        	catch (StateHistoryIOException | IOException e1)
			{
        		JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
			}
        });

        //Gets a file to save the history to, and passes that onto the model for saving
        view.getSaveStateHistoryButton().addActionListener(e -> {

            if (view.getTable().getSelectedRowCount() == 1)
            {
            	File file = CustomFileChooser.showSaveDialog(view, "Select File", "stateHistory.csv");
            	if (file == null) return;
                if (!FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("csv")) {
                    // remove the extension (if any) and replace it with ".csv"
                    file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".csv");
                }
                StateHistory history = runs.getSelectedItems().asList().get(0);
                try
				{
					historyModel.saveHistoryToFile(history, file);
				}
                catch (StateHistoryIOException e1)
				{
                	JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
				}
            }
            else {
                JOptionPane.showMessageDialog(null, "You must have exactly one row of the table selected to save an interval", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

        });

        view.getDeleteStateHistoryButton().addActionListener(e -> {

        	if (view.getTable().getSelectedRowCount() == 0) return;
        	int n = JOptionPane.showOptionDialog(view, "Delete selected trajectories?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        	if (n == JOptionPane.NO_OPTION) return;
    		for (StateHistory history : runs.getSelectedItems())
    		{
    			try
				{
					historyModel.removeRun(history);
				}
				catch (IOException e1)
				{
					JOptionPane.showMessageDialog(null, "Problem saving the state history configuration file", "Error",
                            JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}


    		}
        });


        //For each of the selected items, set their visiblity to true
        view.getShowStateHistoryButton().addActionListener(e ->
		{
			runs.getSelectedItems().forEach(history -> { runs.setVisibility(history, true); } );
		});

        //For each of the selected items, set the visibility to false
        view.getRemoveStateHistoryButton().addActionListener(e ->
        {
        	runs.getSelectedItems().forEach(history -> { runs.setVisibility(history, false); } );
        });

        //If a new state history segment is created, repaint the table
        historyModel.addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener()
		{
			@Override
			public void historySegmentCreated(StateHistory historySegment)
			{
				view.getTable().repaint();
			}

			@Override
			public void historySegmentRemoved(StateHistory historySegment)
			{
				view.getTable().repaint();
			}
		});

        //If the vtk properties of the state histories change, repaint the table and reset the clipping
        //range to ensure things are updated on the renderer
        runs.addPropertyChangeListener(evt ->
		{
			view.getTable().repaint();
			renderer.getRenderWindowPanel().resetCameraClippingRange();
		});

        //Responds to changes in item selection.  Enables/disables buttons, and fades displayed trajectories
        //in the renderer appropriately
        runs.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsSelected) return;
			view.getRemoveStateHistoryButton().setEnabled(runs.getSelectedItems().size() > 0);
			view.getShowStateHistoryButton().setEnabled(runs.getSelectedItems().size() > 0);
			view.getSaveStateHistoryButton().setEnabled(runs.getSelectedItems().size() == 1);
			for (StateHistory history : runs.getAllItems())
			{
				history.getTrajectory().setFaded(!runs.getSelectedItems().contains(history));
				runs.refreshColoring(history);
			}
			runs.updateTimeBarValue();

		});
    }

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public StateHistoryTableView getView()
	{
		return view;
	}
}
