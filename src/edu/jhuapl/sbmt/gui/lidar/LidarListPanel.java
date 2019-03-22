package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import edu.jhuapl.saavtk.colormap.SigFigNumberFormat;
import edu.jhuapl.saavtk.gui.ColorCellRenderer;
import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.IconUtil;
import edu.jhuapl.saavtk.gui.RadialOffsetChanger;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.Track;

import net.miginfocom.swing.MigLayout;

/**
 * Panel used to display a list of lidar tracks.
 * <P>
 * The following functionality is supported:
 * <UL>
 * <LI>Display list of tracks in a table
 * <LI>Allow user to show, hide, or remove tracks
 * <LI>Allow user to drag or manually translate tracks
 * </UL>
 */
public class LidarListPanel extends JPanel implements ActionListener, ChangeListener, ListSelectionListener,
		PickManagerListener, TableModelListener, TrackEventListener
{
	// Ref vars
	private LidarSearchDataCollection refModel;
	private PickManager refPickManager;

	// State vars
	private LidarShiftPicker lidarPicker;

	// GUI vars
	private LidarTrackTranslateDialog translateDialog;
	private LidarPopupMenu lidarPopupMenu;
	private JTable trackTable;
	private JLabel titleL;
	private JButton selectAllB, selectInvertB, selectNoneB;
	private JButton hideB, showB, removeB;
	private JButton translateB;
	private JToggleButton dragB;

	private JCheckBox showErrorCB;
	private JComboBox<ItemGroup> errorModeBox;
	private JLabel errorModeL, errorValueL;

	private RadialOffsetChanger radialOffsetChanger;
	private JSpinner pointSizeSpinner;

	public LidarListPanel(ModelManager aModelManager, LidarSearchDataCollection aModel, PickManager aPickManager,
			Renderer aRenderer)
	{
		refModel = aModel;
		refPickManager = aPickManager;

		lidarPicker = new LidarShiftPicker(aRenderer, aModelManager, refModel);

		lidarPopupMenu = new LidarPopupMenu(refModel, aRenderer);

		setLayout(new MigLayout());

		// Table area
		selectInvertB = new JButton(IconUtil.loadIcon("resources/icons/itemSelectInvert.png"));
		selectInvertB.addActionListener(this);
		selectInvertB.setToolTipText("Invert Selection");

		selectNoneB = new JButton(IconUtil.loadIcon("resources/icons/itemSelectNone.png"));
		selectNoneB.addActionListener(this);
		selectNoneB.setToolTipText("Clear Selection");

		selectAllB = new JButton(IconUtil.loadIcon("resources/icons/itemSelectAll.png"));
		selectAllB.addActionListener(this);
		selectAllB.setToolTipText("Select All");

		titleL = new JLabel("Tracks: ---");
		TableModel tableModel = new LidarTableModel(aModel);
		trackTable = new JTable();
		trackTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		trackTable.setDefaultRenderer(Color.class, new ColorCellRenderer(false));
		trackTable.getSelectionModel().addListSelectionListener(this);
		trackTable.setModel(tableModel);
		installTrackTableMouseListener();
		add(titleL, "growx,span,split");
		add(selectInvertB, "w 24!,h 24!");
		add(selectNoneB, "w 24!,h 24!");
		add(selectAllB, "w 24!,h 24!,wrap 2");
		JScrollPane tableScrollPane = new JScrollPane(trackTable);
		add(tableScrollPane, "growx,growy,pushx,pushy,span,wrap");

		// Action buttons
		hideB = new JButton("Hide");
		hideB.addActionListener(this);
		hideB.setToolTipText("Hide Tracks");
		showB = new JButton("Show");
		showB.addActionListener(this);
		showB.setToolTipText("Show Tracks");
		removeB = new JButton("Remove All");
		removeB.addActionListener(this);
		removeB.setToolTipText("Remove Tracks");
		add(hideB, "span,split,sg g1");
		add(showB, "sg g1");
		add(removeB, "sg g1,wrap");

		// Translation section
		translateB = new JButton("Translate Tracks");
		translateB.addActionListener(this);
		dragB = new JToggleButton("Drag Tracks");
		dragB.addActionListener(this);
		add(dragB, "span,split,sg g2");
		add(translateB, "sg g2,wrap");

		// Radial offset section
		radialOffsetChanger = new RadialOffsetChanger();
		radialOffsetChanger.setModel(refModel);
		radialOffsetChanger.setOffsetScale(refModel.getOffsetScale());

		add(radialOffsetChanger, "growx,wrap");

		// Point size section
		JLabel tmpL = new JLabel("Point Size:");
		pointSizeSpinner = new JSpinner();
		pointSizeSpinner.addChangeListener(this);
		pointSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 1, 100, 1));
		add(tmpL, "span,split");
		add(pointSizeSpinner, "growx,wrap");

		// Error section
		showErrorCB = new JCheckBox("Show Error:");
		showErrorCB.addActionListener(this);
		showErrorCB.setToolTipText(
				"<html>\nIf checked, the track error will be calculated and shown to the right of this checkbox.<br>\nWhenever a change is made to the tracks, the track error will be updated. This can be<br>\na slow operation which is why this checkbox is provided to disable it.<br>\n<br>\nThe track error is computed as the mean distance between each lidar point and its<br>\nclosest point on the asteroid.");
		errorValueL = new JLabel("");
		add(showErrorCB, "span,split");
		add(errorValueL, "growx,w 0::");

		ItemGroup[] errorModeArr = { ItemGroup.All, ItemGroup.Visible, ItemGroup.Selected };
		errorModeL = new JLabel("Mode:");
		errorModeBox = new JComboBox<>(errorModeArr);
		errorModeBox.setSelectedItem(ItemGroup.Visible);
		errorModeBox.addActionListener(this);
		add(errorModeL, "");
		add(errorModeBox, "");

		updateGui();
		configureColumnWidths();

		// Register for events of interest
		PickUtil.autoDeactivatePickerWhenComponentHidden(refPickManager, lidarPicker, this);
		tableModel.addTableModelListener(this);
		refModel.addListener(this);
		refPickManager.addListener(this);

		// TODO: This registration should be done by the refModel
		refModel.handleDefaultPickerManagement(refPickManager.getDefaultPicker(), aModelManager);
	}

	/**
	 * Sets in the new LidarModel.
	 * <P>
	 * Not sure of the functionality in this method. This keeps the same
	 * functional design as the original defective design in case there are
	 * undocumented side effects.
	 * <P>
	 * TODO: In the future expand on the reasoning or get rid of this very bad
	 * design.
	 */
	@Deprecated
	public void injectNewLidarModel(LidarSearchDataCollection aLidarModel, Renderer aRenderer)
	{
		radialOffsetChanger.setModel(aLidarModel);
		radialOffsetChanger.setOffsetScale(aLidarModel.getOffsetScale());

		// Reset radialOffsetChanger silently (because all the points are "new")
		radialOffsetChanger.reset();

		lidarPopupMenu = new LidarPopupMenu(aLidarModel, aRenderer);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		List<Track> tmpL = refModel.getSelectedTracks();
		if (source == selectAllB)
			trackTable.selectAll();
		else if (source == selectNoneB)
			trackTable.clearSelection();
		else if (source == selectInvertB)
			GuiUtil.invertSelection(trackTable, this);
		else if (source == hideB)
			refModel.setTrackVisible(tmpL, false);
		else if (source == showB)
			refModel.setTrackVisible(tmpL, true);
		else if (source == removeB)
			refModel.removeAllLidarData();
		else if (source == translateB)
			doActionTranslate(tmpL);
		else if (source == dragB)
			doActionDrag();
		else if (source == errorModeBox)
			updateErrorUI();
		else if (source == showErrorCB)
			updateErrorUI();

		updateGui();
		updateErrorUI();
	}

	@Override
	public void handleTrackEvent(Object aSource, ItemEventType aEventType)
	{
		if (aEventType == ItemEventType.ItemsSelected)
			updateTableSelection();

		updateGui();
		updateErrorUI();
	}

	@Override
	public void pickerChanged()
	{
		boolean tmpBool = lidarPicker == refPickManager.getActivePicker();
		dragB.setSelected(tmpBool);
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		Number val = (Number) pointSizeSpinner.getValue();
		refModel.setPointSize(val.intValue());
	}

	@Override
	public void tableChanged(TableModelEvent aEvent)
	{
		configureColumnWidths();
	}

	@Override
	public void valueChanged(ListSelectionEvent aEvent)
	{
		// Transform from rows to Tracks
		int[] idxArr = trackTable.getSelectedRows();
		List<Track> pickL = new ArrayList<>();
		for (int aIdx : idxArr)
			pickL.add(refModel.getTrack(aIdx));

		// Update the model's selection
		refModel.setSelectedTracks(pickL);
	}

	/**
	 * Helper method to configure the column widths on the track table
	 */
	private void configureColumnWidths()
	{
		int maxPts = 99;
		String sourceStr = "Data Source";
		for (int aRow = 0; aRow < trackTable.getRowCount(); aRow++)
		{
			Track tmpTrack = refModel.getTrack(aRow);
			maxPts = Math.max(maxPts, tmpTrack.getNumberOfPoints());
			String tmpStr = LidarTableModel.getSourceFileString(tmpTrack);
			if (tmpStr.length() > sourceStr.length())
				sourceStr = tmpStr;
		}

		String trackStr = "Trk " + trackTable.getRowCount();
		String pointStr = "" + maxPts;
		String begTimeStr = "9999-88-88T00:00:00.000000";
		String endTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		Object[] nomArr = { true, Color.BLACK, trackStr, pointStr, begTimeStr, endTimeStr, sourceStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = trackTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(trackTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			trackTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}

	/**
	 * Helper method that handles the drag action
	 */
	private void doActionDrag()
	{
		Picker targPicker = null;
		if (dragB.isSelected() == true)
			targPicker = lidarPicker;

		refPickManager.setActivePicker(targPicker);
		if (targPicker == null)
			refModel.setSelectedPoint(null, null);
	}

	/**
	 * Helper method that handles the drag action.
	 */
	private void doActionTranslate(List<Track> aTrackL)
	{
		if (translateDialog == null)
			translateDialog = new LidarTrackTranslateDialog(this, refModel);

		translateDialog.setVisible(true);
	}

	/**
	 * Helper method to install the custom listener on the table. This listener
	 * will allow the user to change the Color associated with the lidar track
	 * and provide a popup menu.
	 */
	private void installTrackTableMouseListener()
	{
		trackTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent aEvent)
			{
				// Handle the Color customization
				int row = trackTable.rowAtPoint(aEvent.getPoint());
				int col = trackTable.columnAtPoint(aEvent.getPoint());
				if (aEvent.getClickCount() == 2 && row >= 0 && col == 1)
				{
					Track tmpTrack = refModel.getTrack(row);
					Color oldColor = tmpTrack.getColor();
					Color tmpColor = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(trackTable), oldColor);
					if (tmpColor != null)
						refModel.setTrackColor(tmpTrack, tmpColor);

					return;
				}

				maybeShowPopup(aEvent);
			}

			@Override
			public void mousePressed(MouseEvent aEvent)
			{
				maybeShowPopup(aEvent);
			}

			@Override
			public void mouseReleased(MouseEvent aEvent)
			{
				maybeShowPopup(aEvent);
			}
		});
	}

	/**
	 * Helper method to handle the showing of the table popup menu.
	 */
	private void maybeShowPopup(MouseEvent aEvent)
	{
		// Bail if this is not a valid popup action
		if (aEvent.isPopupTrigger() == false)
			return;

		// TODO: Is this necessary?
		// Force the menu to be hidden by default
		lidarPopupMenu.setVisible(false);

		lidarPopupMenu.showPopup(aEvent, null, 0, null);
	}

	/**
	 * Helper method to update the error UI
	 */
	private void updateErrorUI()
	{
		boolean tmpBool = showErrorCB.isSelected();
		errorModeL.setEnabled(tmpBool);
		errorModeBox.setEnabled(tmpBool);

		// Bail if error computations are disabled
		String errorStr = "";
		if (tmpBool == false)
		{
			errorValueL.setText(errorStr);
			return;
		}

		// Calculate the error computations and update the relevant display
		ItemGroup errorMode = (ItemGroup) errorModeBox.getSelectedItem();
		Set<Track> selectedS = new HashSet<>(refModel.getSelectedTracks());

		// Calculate the cumulative track error and number of lidar points
		double errorSum = 0.0;
		int cntPoints = 0;
		int cntTracks = 0;
		for (int aRow = 0; aRow < trackTable.getRowCount(); aRow++)
		{
			// Skip over Tracks that we are not interested in
			Track tmpTrack = refModel.getTrack(aRow);
			if (errorMode == ItemGroup.Visible && tmpTrack.getIsVisible() == false)
				continue;
			if (errorMode == ItemGroup.Selected && selectedS.contains(tmpTrack) == false)
				continue;

			errorSum += refModel.getTrackError(tmpTrack);
			cntPoints += tmpTrack.getNumberOfPoints();
			cntTracks++;
		}

		// Calculate RMS error
		double errorRMS = Math.sqrt(errorSum / cntPoints);
		if (cntTracks == 0 || cntPoints == 0)
			errorRMS = 0.0;

		// Update the errorValueL
		SigFigNumberFormat errFormat = new SigFigNumberFormat(7, "---");
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		errorStr = errFormat.format(errorRMS) + " RMS: ";
		errorStr += cntFormat.format(cntTracks) + " tracks ";
		errorStr += "(" + cntFormat.format(cntPoints) + " points)";
		errorValueL.setText(errorStr);
	}

	/**
	 * Updates the various UI elements to keep them synchronized
	 */
	private void updateGui()
	{
		// Update various buttons
		int cntFullTracks = refModel.getNumberOfTracks();
		boolean isEnabled = cntFullTracks > 0;
		dragB.setEnabled(isEnabled);
		selectInvertB.setEnabled(isEnabled);

		int cntFullPoints = 0;
		for (Track aTrack : refModel.getTracks())
			cntFullPoints += aTrack.getNumberOfPoints();

		List<Track> pickL = refModel.getSelectedTracks();
		int cntPickTracks = pickL.size();
		isEnabled = cntPickTracks > 0;
		selectNoneB.setEnabled(isEnabled);

		isEnabled = cntFullTracks > 0 && cntPickTracks < cntFullTracks;
		selectAllB.setEnabled(isEnabled);

		isEnabled = cntPickTracks > 0;
//		removeB.setEnabled(isEnabled);
		translateB.setEnabled(isEnabled);
		// TODO: Enable state of removeB should be based on cntPickTracks
		isEnabled = cntFullTracks > 0;
		removeB.setEnabled(isEnabled);

		int cntPickPoints = 0;
		int cntShowTracks = 0;
		for (Track aTrack : pickL)
		{
			cntPickPoints += aTrack.getNumberOfPoints();
			if (aTrack.getIsVisible() == true)
				cntShowTracks++;
		}

		isEnabled = cntPickTracks > 0 && cntShowTracks < cntPickTracks;
		showB.setEnabled(isEnabled);

		isEnabled = cntPickTracks > 0 && cntShowTracks > 0;
		hideB.setEnabled(isEnabled);

		// Table title
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		String infoStr = "Tracks: " + cntFormat.format(cntFullTracks);
		String helpStr = "Points: " + cntFormat.format(cntFullPoints);
		if (cntPickTracks > 0)
		{
			infoStr += "  (Selected: " + cntFormat.format(cntPickTracks) + ")";
			helpStr += "  (Selected: " + cntFormat.format(cntPickPoints) + ")";
		}
		titleL.setText(infoStr);
		titleL.setToolTipText(helpStr);
	}

	/**
	 * Helper method that will synchronize the table selection to match the
	 * selected items in the refModel. If new items were selected then the table
	 * will be scrolled (to the first newly selected row).
	 */
	private void updateTableSelection()
	{
		// Form a reverse lookup map of Track to index
		List<Track> fullTrackL = refModel.getTracks();
		Map<Track, Integer> revLookM = new HashMap<>();
		for (int aIdx = 0; aIdx < fullTrackL.size(); aIdx++)
			revLookM.put(fullTrackL.get(aIdx), aIdx);

		int[] idxArr = trackTable.getSelectedRows();
		List<Integer> oldL = Ints.asList(idxArr);
		Set<Integer> oldS = new LinkedHashSet<>(oldL);

		List<Integer> newL = new ArrayList<>();
		for (Track aTrack : refModel.getSelectedTracks())
			newL.add(revLookM.get(aTrack));
		Set<Integer> newS = new LinkedHashSet<>(newL);

		// Bail if nothing has changed
		if (newS.equals(oldS) == true)
			return;

		// Update the table's selection
		GuiUtil.setSelection(trackTable, this, newL);
		trackTable.repaint();
		updateGui();

		// Bail if there are no new items selected
		Set<Integer> addS = Sets.difference(newS, oldS);
		if (addS.size() == 0)
			return;

		// Ensure the table shows the (newly) selected items
		int idx = addS.iterator().next();
		Rectangle cellBounds = trackTable.getCellRect(idx, 0, true);
		if (cellBounds != null)
			trackTable.scrollRectToVisible(cellBounds);
	}

}
