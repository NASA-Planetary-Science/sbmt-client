package edu.jhuapl.sbmt.spectrum.ui.color;

import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.panel.JComboBoxWithItemState;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.saavtk.util.DownloadableFileManager.StateListener;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileStateListenerTracker;

public class EmissionAngleColoringPanel extends JPanel
{
	JComboBoxWithItemState<String> coloringComboBox;

	public EmissionAngleColoringPanel()
	{
		setVisible(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JLabel lblNewLabel_15 = new JLabel("Coloring by Avg Emission Angle (OREX Scalar Ramp, 0 to 90)");
		add(lblNewLabel_15);
		coloringComboBox = new JComboBoxWithItemState<>();
        ItemListener listener = (e) -> {
            if (e.getStateChange() != ItemEvent.DESELECTED)
            {
                setColoring(e.getSource());
            }
        };
		coloringComboBox.addItemListener(listener);
		add(coloringComboBox);
	}

	protected void setColoring(Object source)
    {
//        try
//        {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            int selectedIndex = coloringComboBox.getSelectedIndex() - 1;
            if (selectedIndex < 0)
            {
//                showColoringProperties.setEnabled(false);
//                smallBodyModel.setColoringIndex(-1);
//                return;
            }

//        }
    }

	protected final Map<JComboBoxWithItemState<?>, FileStateListenerTracker> listenerTrackers = new HashMap<>();

	protected void updateColoringComboBox(JComboBoxWithItemState<String> box, ColoringDataManager coloringDataManager,
			int numberElements)
	{
		// Store the current selection and number of items in the combo box.
		int previousSelection = box.getSelectedIndex();

		// Clear the current content.
		box.setSelectedIndex(-1);
		box.removeAllItems();

		synchronized (this.listenerTrackers)
		{
			// Get rid of current file access state listeners.
			FileStateListenerTracker boxListeners = listenerTrackers.get(box);
			if (boxListeners == null)
			{
				boxListeners = FileStateListenerTracker.of(FileCache.instance());
				listenerTrackers.put(box, boxListeners);
			} else
			{
				boxListeners.removeAllStateChangeListeners();
			}

			// Add one item for blank (no coloring).
			box.addItem("");
			for (String name : coloringDataManager.getNames())
			{
				// Re-add the current colorings.
				box.addItem(name);
				if (!coloringDataManager.has(name, numberElements))
				{
					// This coloring is not available at this resolution. List
					// it but grey it out.
					box.setEnabled(name, false);
				} else
				{
					String urlString = coloringDataManager.get(name, numberElements).getFileName();
					if (urlString == null)
						continue;
					box.setEnabled(name, FileCache.instance().isAccessible(urlString));
					StateListener listener = e ->
					{
						box.setEnabled(name, e.isAccessible());
					};
					boxListeners.addStateChangeListener(urlString, listener);
				}
			}

			int numberColorings = box.getItemCount();
			int selection = 0;
			if (previousSelection < numberColorings)
			{
				// A coloring was replaced/edited. Re-select the current
				// selection.
				selection = previousSelection;
			}

			box.setSelectedIndex(selection);
		}
	}
}