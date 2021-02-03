package edu.jhuapl.sbmt.stateHistory.controllers;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.SpiceKernelIngestor;
import edu.jhuapl.sbmt.stateHistory.model.io.SpiceKernelLoadStatusListener;
import edu.jhuapl.sbmt.stateHistory.model.io.SpiceKernelNotFoundException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryModelIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.StateHistoryIntervalGenerationPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.table.StateHistoryTableView;

/**
 * Controller that governs the "Available Files" panel for the StateHistory tab
 * @author steelrj1
 *
 */
public class StateHistoryIntervalSelectionController
{
    private StateHistoryTableView view;
    private StateHistoryIntervalGenerationController intervalGenerationController;
//    private ObservationPlanningViewControlsController viewControlsController;

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

        	StateHistory historyFromFile = null;
        	File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
        	try
			{
        		historyFromFile = StateHistoryModelIOHelper.loadStateHistoryFromFile(file, bodyModel.getModelName(), new StateHistoryKey(historyModel.getRuns()));
        		historyFromFile.reloadPointingProvider();
        		historyModel.addInterval(historyFromFile);
			}
        	catch (StateHistoryIOException shioe)
        	{
        		if (shioe.getCause() instanceof SpiceKernelNotFoundException)
        		{
					String[] options = {"Yes", "No"};
					int result = JOptionPane.showOptionDialog(null, "Cannot find metakernel " + FilenameUtils.getBaseName(historyFromFile.getSourceFile())
																	+ " at specified location for state history " + historyFromFile.getStateHistoryName()
																	+ ". Would you like to choose a new file?  Otherwise the state history will be removed.", "Metakernel not found",
																	JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

					if (result == 0)
					{
						File newFile = CustomFileChooser.showOpenDialog(view, "Select Metakernel");
						if (newFile == null) return;
						File selectedMetakernel = newFile;
						SpiceKernelIngestor kernelIngestor = new SpiceKernelIngestor(historyModel.getCustomDataFolder());
						String newKernelLocationAfterIngestion;
						try {
							newKernelLocationAfterIngestion = kernelIngestor.ingestMetaKernelToCache(selectedMetakernel.getAbsolutePath(), new SpiceKernelLoadStatusListener() {

								@Override
								public void percentageLoaded(double percentage) {
//									progressBar.setValue((int)percentage);

								}
							});
							historyFromFile.setSourceFile(newKernelLocationAfterIngestion);
							historyFromFile.reloadPointingProvider();
			        		historyModel.addInterval(historyFromFile);
						} catch (StateHistoryIOException | IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
        		}
				else
				{
					JOptionPane.showMessageDialog(null, shioe.getMessage(), "Loading Error, see console for details.",
	                        JOptionPane.ERROR_MESSAGE);
					shioe.printStackTrace();
	                return;
				}
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

        view.getEditStateHistoryButton().addActionListener(e -> {

        	StateHistory history = rendererManager.getSelectedItems().asList().get(0);
        	StateHistoryIntervalGenerationPanel genPanel = new StateHistoryIntervalGenerationPanel(history);
        	final JFrame frame = new JFrame("Edit Existing Interval...");
           	genPanel.getGetIntervalButton().addActionListener(e2 -> {
           		if (genPanel.isEditMode())
               	{
	        		genPanel.updateStateHistory();
	        		rendererManager.getRuns().fireHistorySegmentUpdatedListeners(history);
	        		SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							frame.setVisible(false);
						}
					});

	        		return;
               	}

        	});

        	frame.add(genPanel);
        	frame.pack();
        	frame.setVisible(true);

        });

        view.getAddStateHistoryButton().addActionListener(e ->
        {
        	JFrame frame = new JFrame("Generate New Interval...");
        	frame.add(intervalGenerationController.getView());
        	frame.pack();
        	frame.setVisible(true);

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
			updateButtonState(rendererManager);
		});

        //Responds to changes in item selection.  Enables/disables buttons, and fades displayed trajectories
        //in the renderer appropriately
        rendererManager.addListener((aSource, aEventType) ->
		{
			updateButtonState(rendererManager);
		});
    }

	private void updateButtonState(StateHistoryRendererManager rendererManager)
	{
		ImmutableSet<StateHistory> selectedItems = rendererManager.getSelectedItems();
		view.getSaveStateHistoryButton().setEnabled(selectedItems.size() == 1);
		boolean allMapped = true;
		boolean allShown = true;
		for (StateHistory history : selectedItems)
		{
			if (history.isMapped() == false) allMapped = false;
			if (history.isVisible() == false) allShown = false;
		}
		view.getHideStateHistoryButton().setEnabled((selectedItems.size() > 0) && allShown && allMapped);
		view.getShowStateHistoryButton().setEnabled((selectedItems.size() > 0) && !allShown && allMapped);
		view.getEditStateHistoryButton().setEnabled(selectedItems.size() == 1);
		view.getDeleteStateHistoryButton().setEnabled(selectedItems.size() > 0);
	}

	/**
	 * @return the view
	 */
	public StateHistoryTableView getView()
	{
		return view;
	}

	/**
	 * @param intervalGenerationController the intervalGenerationController to set
	 */
	public void setIntervalGenerationController(StateHistoryIntervalGenerationController intervalGenerationController)
	{
		this.intervalGenerationController = intervalGenerationController;
	}
}