package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.ColormapUtil;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.gui.panel.JComboBoxWithItemState;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.saavtk.util.DownloadableFileManager.StateListener;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileStateListenerTracker;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;

public class StateHistoryColoringOptionsPanel extends JPanel
{

	/**
	 *
	 */
	private JLabel colorFunctionLabel;

	/**
	 *
	 */
	private JLabel colorRampLabel;

	/**
	 *
	 */
	private JComboBox<StateHistoryColoringFunctions> colorFunctionComboBox;

	/**
	 *
	 */
	private JComboBox<Colormap> colormapComboBox;

	private ItemListener plateColoringsItemListener;

	JComboBoxWithItemState<String> plateColorings = new JComboBoxWithItemState<String>();

	public StateHistoryColoringOptionsPanel(ColoringDataManager coloringDataManager)
	{
		initUI(coloringDataManager);
	}

	private void initUI(ColoringDataManager coloringDataManager)
	{
		configureColoringPanel(coloringDataManager);
	}

	protected final Map<JComboBoxWithItemState<?>, FileStateListenerTracker> listenerTrackers = new HashMap<>();

	/**
	 *
	 */
	private void configureColoringPanel(ColoringDataManager coloringDataManager)
	{
		setBorder(new TitledBorder(null, "Coloring Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		colorFunctionLabel = new JLabel("Trajectory Coloring Function:");
		colorFunctionComboBox = new JComboBox<StateHistoryColoringFunctions>(StateHistoryColoringFunctions.values());

		JPanel horizPanel1 = new JPanel();
		horizPanel1.setLayout(new BoxLayout(horizPanel1, BoxLayout.X_AXIS));

		horizPanel1.add(colorFunctionLabel);
		horizPanel1.add(colorFunctionComboBox);

		colorRampLabel = new JLabel("Color:");

		colormapComboBox = new JComboBox<Colormap>();
		ListCellRenderer<Colormap> tmpRenderer = ColormapUtil.getFancyColormapRender();
		((Component) tmpRenderer).setEnabled(true);
		colormapComboBox.setRenderer(tmpRenderer);
		for (String aStr : Colormaps.getAllBuiltInColormapNames())
		{
			Colormap cmap = Colormaps.getNewInstanceOfBuiltInColormap(aStr);
			colormapComboBox.addItem(cmap);
			if (cmap.getName().equals(Colormaps.getCurrentColormapName()))
				colormapComboBox.setSelectedItem(cmap);
		}
		colormapComboBox.setEnabled(true);

		colormapComboBox.setPreferredSize(new Dimension(300, 30));
		colormapComboBox.setMaximumSize(new Dimension(300, 30));

		colormapComboBox.setEnabled(!(((StateHistoryColoringFunctions) colorFunctionComboBox
				.getSelectedItem()) == StateHistoryColoringFunctions.PER_TABLE));

		JPanel horizPanel2 = new JPanel();
		horizPanel2.setLayout(new BoxLayout(horizPanel2, BoxLayout.X_AXIS));

		horizPanel2.add(colorRampLabel);
		horizPanel2.add(colormapComboBox);

		horizPanel1.add(Box.createHorizontalGlue());
		horizPanel2.add(Box.createHorizontalGlue());

		add(horizPanel1);
		add(horizPanel2);

		//footprint coloring options
		JLabel footprintColoringLabel = new JLabel("Footprint Coloring: ");

		JPanel footprintColoringPanel = new JPanel();
		footprintColoringPanel.setLayout(new BoxLayout(footprintColoringPanel, BoxLayout.X_AXIS));
		footprintColoringPanel.add(footprintColoringLabel);

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
                    String urlString = coloringDataManager.get(name, numberElements).getFileName();
                    if (urlString == null) continue;
                    plateColorings.setEnabled(name, FileCache.instance().isAccessible(urlString));
                    StateListener listener = e -> {
                    	plateColorings.setEnabled(name, e.isAccessible());
                    };
                    boxListeners.addStateChangeListener(urlString, listener);
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



        footprintColoringPanel.add(plateColorings);
		footprintColoringPanel.add(Box.createHorizontalGlue());

		add(footprintColoringPanel);

	}

	/**
	 * @return
	 */
	public JComboBox<StateHistoryColoringFunctions> getColorFunctionComboBox()
	{
		return colorFunctionComboBox;
	}

	/**
	 * @return
	 */
	public JComboBox<Colormap> getColormapComboBox()
	{
		return colormapComboBox;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		colorFunctionComboBox.setEnabled(enabled);
		colorFunctionLabel.setEnabled(enabled);
		colormapComboBox.setEnabled(!(((StateHistoryColoringFunctions) colorFunctionComboBox
				.getSelectedItem()) == StateHistoryColoringFunctions.PER_TABLE));
		colorRampLabel.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/**
	 * @param plateColoringsItemListener the plateColoringsItemListener to set
	 */
	public void setPlateColoringsItemListener(ItemListener plateColoringsItemListener)
	{
		this.plateColoringsItemListener = plateColoringsItemListener;
		plateColorings.addItemListener(plateColoringsItemListener);

	}
}
