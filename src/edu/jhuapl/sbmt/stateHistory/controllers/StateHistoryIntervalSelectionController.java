package edu.jhuapl.sbmt.stateHistory.controllers;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.version2.table.StateHistoryTableView;

import glum.item.ItemEventType;

/**
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

        view.getLoadStateHistoryButton().addActionListener(e -> {

        	File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
        	try
			{
				historyModel.loadIntervalFromFile(file, bodyModel);
			}
        	catch (StateHistoryIOException e1)
			{
        		JOptionPane.showMessageDialog(null, e1.getMessage(), "Loading Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
			}
        });

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
					historyModel.saveRowToFile(history, file);
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

        view.getShowStateHistoryButton().addActionListener(e ->
		{
			runs.getSelectedItems().forEach(history -> { runs.setVisibility(history, true); } );
		});

        view.getRemoveStateHistoryButton().addActionListener(e ->
        {
        	runs.getSelectedItems().forEach(history -> { runs.setVisibility(history, false); } );
        });

        historyModel.addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener()
		{
			@Override
			public void historySegmentCreated(StateHistory historySegment)
			{
				view.getTable().repaint();
			}
		});

        runs.addPropertyChangeListener(evt ->
		{
			view.getTable().repaint();
			renderer.getRenderWindowPanel().resetCameraClippingRange();
		});

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
