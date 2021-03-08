package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.panel.JComboBoxWithItemState;
import edu.jhuapl.saavtk.model.plateColoring.ColoringData;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.saavtk.model.plateColoring.LoadableColoringData;
import edu.jhuapl.saavtk.util.DownloadableFileManager.StateListener;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileStateListenerTracker;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.StateHistoryFOVPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.viewOptions.table.ViewOptionsTableView;

import glum.item.ItemEventType;

/**
 * @author steelrj1
 *
 */
public class StateHistoryFOVController
{
	/**
	 * The view governed by this controller
	 */
	private StateHistoryFOVPanel view;

	/**
	 * The collection of state history items
	 */
	private StateHistoryCollection runs;

	private ColoringDataManager coloringDataManager;

	private ItemListener plateColoringsItemListener;

	private JComboBoxWithItemState<String> plateColorings = new JComboBoxWithItemState<String>();

	private StateHistoryRendererManager rendererManager;

	protected final Map<JComboBoxWithItemState<?>, FileStateListenerTracker> listenerTrackers = new HashMap<>();

	/**
	 * Constructor.  Sets properties and initializes the view control panel
	 * @param runs
	 * @param renderer
	 */
	public StateHistoryFOVController(StateHistoryRendererManager rendererManager, ColoringDataManager coloringDataManager)
	{
		this.rendererManager = rendererManager;
		this.runs = rendererManager.getRuns();
		this.coloringDataManager = coloringDataManager;
		initializeViewControlPanel(rendererManager);
	}

	/**
	 * Initializes the view control panel, sets action listeners, etc
	 */
	private void initializeViewControlPanel(StateHistoryRendererManager rendererManager)
	{
		makePlateColorings();
		view = new StateHistoryFOVPanel();
		view.setAvailableFOVs(runs.getAvailableFOVs());

        rendererManager.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsChanged) return;
			view.setAvailableFOVs(runs.getAvailableFOVs());

		});

        rendererManager.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
					if (rendererManager.getSelectedItems().size() > 0) {
						view.setTableHidden(false);
						runs.setCurrentRun(rendererManager.getSelectedItems().asList().get(0));
					}

					view.setAvailableFOVs(runs.getAvailableFOVs());
					view.repaint();
					view.validate();
				}
			});
		});

		ViewOptionsTableView tableView = new ViewOptionsTableView(rendererManager, plateColorings);
		tableView.setup();
		view.setTableView(tableView);
	}

	private void makePlateColorings()
	{
		ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
		int newResolutionLevel = 0;
		int numberElements = resolutions.size() > newResolutionLevel ? resolutions.get(newResolutionLevel) : -1;

		// Store the current selection and number of items in the combo box.
        int previousSelection = plateColorings.getSelectedIndex();

        // Clear the current content.
        plateColorings.setSelectedIndex(-1);
        plateColorings.removeAllItems();

        synchronized (this.listenerTrackers)
        {
            // Get rid of current file access state listeners.
            FileStateListenerTracker boxListeners = listenerTrackers.get(plateColorings);
            if (boxListeners == null)
            {
                boxListeners = FileStateListenerTracker.of(FileCache.instance());
                listenerTrackers.put(plateColorings, boxListeners);
            }
            else
            {
                boxListeners.removeAllStateChangeListeners();
            }
            // Add one item for blank (no coloring).
            plateColorings.addItem("");
            for (String name : coloringDataManager.getNames())
            {
                // Re-add the current colorings.
            	plateColorings.addItem(name);
                if (!coloringDataManager.has(name, numberElements))
                {
                    // This coloring is not available at this resolution. List it but grey it out.
                	plateColorings.setEnabled(name, false);
                }
                else
                {
                	ColoringData coloringData = coloringDataManager.get(name, numberElements);
                    if (coloringData instanceof LoadableColoringData)
                    {
                        String urlString = ((LoadableColoringData) coloringData).getFileId();

                        plateColorings.setEnabled(name, FileCache.instance().isAccessible(urlString));
                        StateListener listener = e -> {
                            plateColorings.setEnabled(name, e.isAccessible());
                        };
                        boxListeners.addStateChangeListener(urlString, listener);
                    }
                }
            }

            int numberColorings = plateColorings.getItemCount();
            int selection = 0;
            if (previousSelection < numberColorings)
            {
                // A coloring was replaced/edited. Re-select the current selection.
                selection = previousSelection;
            }

            plateColorings.setSelectedIndex(selection);
        }

        plateColoringsItemListener = e ->
		{
			JComboBoxWithItemState<String> combo = (JComboBoxWithItemState<String>)e.getSource();
			rendererManager.setFootprintPlateColoring(combo.getModel().getSelectedItem().toString());
		};
	}

	/**
	 * @return the view
	 */
	public StateHistoryFOVPanel getView()
	{
		return view;
	}
}