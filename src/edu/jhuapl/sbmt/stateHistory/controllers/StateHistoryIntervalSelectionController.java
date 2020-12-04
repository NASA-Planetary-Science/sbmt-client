package edu.jhuapl.sbmt.stateHistory.controllers;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryModelIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
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
	public StateHistoryIntervalSelectionController(StateHistoryModel historyModel, SmallBodyModel bodyModel, StateHistoryRendererManager rendererManager)
	{
		initializeIntervalSelectionPanel(historyModel, bodyModel, rendererManager);
	}

	/**
	 * Sets up listeners for various UI components
	 */
	private void initializeIntervalSelectionPanel(StateHistoryModel historyModel, SmallBodyModel bodyModel, StateHistoryRendererManager rendererManager)
    {
		view = new StateHistoryTableView(rendererManager);

		//Queries for a file to load history from, passing to the model for loading
        view.getLoadStateHistoryButton().addActionListener(e -> {

        	File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
        	try
			{
        		StateHistoryModelIOHelper.loadStateHistoryFromFile(file, bodyModel.getModelName(), new StateHistoryKey(historyModel.getRuns()));
//				historyModel.loadIntervalFromFile(file, bodyModel);
			}
        	catch (StateHistoryIOException e1)
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
                StateHistory history = rendererManager.getSelectedItems().asList().get(0);
                try
				{
                	StateHistoryModelIOHelper.saveIntervalToFile(bodyModel.getModelName(), history, file.getAbsolutePath());
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
    		for (StateHistory history : rendererManager.getSelectedItems())
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
			rendererManager.getSelectedItems().forEach(history -> { rendererManager.setVisibility(history, true); } );
		});

        //For each of the selected items, set the visibility to false
        view.getHideStateHistoryButton().addActionListener(e ->
        {
        	rendererManager.getSelectedItems().forEach(history -> { rendererManager.setVisibility(history, false); } );
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
        rendererManager.addPropertyChangeListener(evt ->
		{
			view.getTable().repaint();
			rendererManager.getRenderer().getRenderWindowPanel().resetCameraClippingRange();
		});

        //Responds to changes in item selection.  Enables/disables buttons, and fades displayed trajectories
        //in the renderer appropriately
        rendererManager.addListener((aSource, aEventType) ->
		{
			try {
				if (aEventType != ItemEventType.ItemsSelected) return;
				ImmutableSet<StateHistory> selectedItems = rendererManager.getSelectedItems();

				view.getSaveStateHistoryButton().setEnabled(selectedItems.size() == 1);
				boolean allMapped = true;
				boolean allShown = true;
				for (StateHistory history : selectedItems)
				{
					if (history.isMapped() == false) allMapped = false;
					if (history.isVisible() == false) allShown = false;
				}
				view.getHideStateHistoryButton().setEnabled((selectedItems.size() > 0) && allShown);
				view.getShowStateHistoryButton().setEnabled((selectedItems.size() > 0) && allMapped);
				view.getDeleteStateHistoryButton().setEnabled(selectedItems.size() > 0);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
    }

	/**
	 * @return the view
	 */
	public StateHistoryTableView getView()
	{
		return view;
	}
}