package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

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
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.model.lidar.LidarGeoUtil;
import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.model.lidar.LidarTrack;

import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.NumberRenderer;
import glum.gui.table.PrePendRenderer;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemGroup;
import glum.item.ItemManagerUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to display a list of lidar Tracks.
 * <P>
 * The following functionality is supported:
 * <UL>
 * <LI>Display list of tracks in a table
 * <LI>Allow user to show, hide, or remove tracks
 * <LI>Allow user to drag or manually translate tracks
 * </UL>
 *
 * @author lopeznr1
 */
public class LidarTrackPanel extends JPanel
		implements ActionListener, ChangeListener, PickManagerListener, ItemEventListener
{
	// Ref vars
	private final LidarTrackManager refTrackManager;
	private final PickManager refPickManager;

	// State vars
	private LidarShiftPicker lidarPicker;

	// GUI vars
	private LidarTrackTranslateDialog translateDialog;
	private LidarSaveDialog saveDialog;
	private LidarPopupMenu lidarPopupMenu;
	private ItemListPanel<LidarTrack> lidarILP;
	private JLabel titleL;
	private JButton selectAllB, selectInvertB, selectNoneB;
	private JButton hideB, showB;
	private JButton removeB, saveB;
	private JButton translateB;
	private JToggleButton dragB;

	private JCheckBox showErrorCB;
	private JComboBox<ItemGroup> errorModeBox;
	private JLabel errorModeL, errorValueL;

	private RadialOffsetChanger radialOffsetChanger;
	private JSpinner pointSizeSpinner;

	public LidarTrackPanel(ModelManager aModelManager, LidarTrackManager aTrackManager, PickManager aPickManager,
			Renderer aRenderer)
	{
		refTrackManager = aTrackManager;
		refPickManager = aPickManager;

		lidarPicker = new LidarShiftPicker(aRenderer, aModelManager, refTrackManager);

		lidarPopupMenu = new LidarPopupMenu(refTrackManager, aRenderer);

		setLayout(new MigLayout());

		// Table header
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
		add(titleL, "growx,span,split");
		add(selectInvertB, "w 24!,h 24!");
		add(selectNoneB, "w 24!,h 24!");
		add(selectAllB, "w 24!,h 24!,wrap 2");

		// Table Content
		QueryComposer<LookUp> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(LookUp.IsVisible, Boolean.class, "Show", null);
		tmpComposer.addAttribute(LookUp.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(LookUp.Name, String.class, "Track", null);
		tmpComposer.addAttribute(LookUp.NumPoints, Integer.class, "# pts", null);
		tmpComposer.addAttribute(LookUp.BegTime, Double.class, "Start Time", null);
		tmpComposer.addAttribute(LookUp.EndTime, Double.class, "End Time", null);
		tmpComposer.addAttribute(LookUp.Source, Double.class, "Source", null);

		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);
		tmpComposer.setEditor(LookUp.IsVisible, new BooleanCellEditor());
		tmpComposer.setRenderer(LookUp.IsVisible, new BooleanCellRenderer());
		tmpComposer.setRenderer(LookUp.Color, new ColorCellRenderer(false));
		tmpComposer.setRenderer(LookUp.Name, new PrePendRenderer("Trk "));
		tmpComposer.setRenderer(LookUp.NumPoints, new NumberRenderer("###,###,###", "---"));
		tmpComposer.setRenderer(LookUp.BegTime, tmpTimeRenderer);
		tmpComposer.setRenderer(LookUp.EndTime, tmpTimeRenderer);

		ItemHandler<LidarTrack> tmpIH = new TrackItemHandler(refTrackManager, tmpComposer);
		ItemProcessor<LidarTrack> tmpIP = refTrackManager;
		lidarILP = new ItemListPanel<>(tmpIH, tmpIP, true);
		lidarILP.setSortingEnabled(true);

		JTable lidarTable = lidarILP.getTable();
		lidarTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		add(new JScrollPane(lidarTable), "growx,growy,pushx,pushy,span,wrap");
		installTrackTableMouseListener(lidarTable);

		// Action buttons: hide / show
		hideB = GuiUtil.formButton(this, "Hide");
		hideB.setToolTipText("Hide Tracks");
		showB = GuiUtil.formButton(this, "Show");
		showB.setToolTipText("Show Tracks");

		// Action buttons: drag / translate
		dragB = new JToggleButton("Drag Tracks");
		dragB.addActionListener(this);
		translateB = new JButton("Translate Tracks");
		translateB.addActionListener(this);

		// Action buttons: remove / save
		removeB = new JButton("Remove Tracks");
		removeB.addActionListener(this);
		saveB = new JButton("Save Tracks");
		saveB.addActionListener(this);

		// Row 1: hideB, dragB, removeB
		add(hideB, "sg g1,span,split");
		add(dragB, "gapleft 20,sg g2");
		add(removeB, "gapleft 20,sg g3,wrap");

		// Row 2: showB, translateB, saveB
		add(showB, "sg g1,span,split");
		add(translateB, "gapleft 20,sg g2");
		add(saveB, "gapleft 20,sg g3,wrap");

		// Radial offset section
		radialOffsetChanger = new RadialOffsetChanger();
		radialOffsetChanger.setModel(refTrackManager);
		radialOffsetChanger.setOffsetScale(LidarGeoUtil.getOffsetScale(refTrackManager));

		add(radialOffsetChanger, "growx,span,wrap");

		// Point size section
		JLabel tmpL = new JLabel("Point Size:");
		pointSizeSpinner = new JSpinner();
		pointSizeSpinner.addChangeListener(this);
		pointSizeSpinner.setModel(new SpinnerNumberModel(2, 1, 100, 1));
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
		refTrackManager.addListener(this);
		refPickManager.addListener(this);

		// TODO: This registration should be done by the refModel
		refTrackManager.handleDefaultPickerManagement(refPickManager.getDefaultPicker(), aModelManager);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		List<LidarTrack> tmpL = refTrackManager.getSelectedItems();
		if (source == selectAllB)
			ItemManagerUtil.selectAll(refTrackManager);
		else if (source == selectNoneB)
			ItemManagerUtil.selectNone(refTrackManager);
		else if (source == selectInvertB)
			ItemManagerUtil.selectInvert(refTrackManager);
		else if (source == hideB)
			refTrackManager.setIsVisible(tmpL, false);
		else if (source == showB)
			refTrackManager.setIsVisible(tmpL, true);
		else if (source == removeB)
			refTrackManager.removeItems(tmpL);
		else if (source == translateB)
			doActionTranslate(tmpL);
		else if (source == dragB)
			doActionDrag();
		else if (source == saveB)
			doActionSave();
		else if (source == errorModeBox)
			updateErrorUI();
		else if (source == showErrorCB)
			updateErrorUI();

		updateGui();
		updateErrorUI();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		if (aEventType == ItemEventType.ItemsChanged)
		{
			radialOffsetChanger.setOffset(refTrackManager.getOffset());
			configureColumnWidths();
		}
		else if (aEventType == ItemEventType.ItemsSelected)
		{
			List<LidarTrack> pickL = refTrackManager.getSelectedItems();

			LidarTrack tmpTrack = null;
			if (pickL.size() > 0)
				tmpTrack = pickL.get(pickL.size() - 1);

			lidarILP.scrollToItem(tmpTrack);
		}

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
		refTrackManager.setPointSize(val.intValue());
	}

	/**
	 * Helper method to configure the column widths on the track table
	 */
	private void configureColumnWidths()
	{
		int maxPts = 99;
		String sourceStr = "Data Source";
		for (LidarTrack aTrack : refTrackManager.getAllItems())
		{
			maxPts = Math.max(maxPts, aTrack.getNumberOfPoints());
			String tmpStr = TrackItemHandler.getSourceFileString(aTrack);
			if (tmpStr.length() > sourceStr.length())
				sourceStr = tmpStr;
		}

		JTable tmpTable = lidarILP.getTable();
		String trackStr = "" + tmpTable.getRowCount();
		String pointStr = "" + maxPts;
		String begTimeStr = "9999-88-88T00:00:00.000000";
		String endTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		Object[] nomArr = { true, Color.BLACK, trackStr, pointStr, begTimeStr, endTimeStr, sourceStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
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
			refTrackManager.setSelectedPoint(null, null);
	}

	/**
	 * Helper method that handles the save action
	 */
	private void doActionSave()
	{
		if (saveDialog == null)
			saveDialog = new LidarSaveDialog(this, refTrackManager);

		saveDialog.setVisible(true);
	}

	/**
	 * Helper method that handles the drag action.
	 */
	private void doActionTranslate(List<LidarTrack> aTrackL)
	{
		if (translateDialog == null)
			translateDialog = new LidarTrackTranslateDialog(this, refTrackManager);

		translateDialog.setVisible(true);
	}

	/**
	 * Helper method to install the custom listener on the table. This listener
	 * will allow the user to change the Color associated with the lidar track
	 * and provide a popup menu.
	 */
	private void installTrackTableMouseListener(JTable aTable)
	{
		aTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent aEvent)
			{
				// Handle the Color customization
				int row = aTable.rowAtPoint(aEvent.getPoint());
				int col = aTable.columnAtPoint(aEvent.getPoint());
				if (aEvent.getClickCount() == 2 && row >= 0 && col == 1)
				{
					LidarTrack tmpTrack = refTrackManager.getTrack(row);
					Color oldColor = refTrackManager.getColor(tmpTrack);
					Color tmpColor = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(aTable), oldColor);
					if (tmpColor != null)
						refTrackManager.setColor(tmpTrack, tmpColor);

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
		Set<LidarTrack> selectedS = new HashSet<>(refTrackManager.getSelectedItems());

		// Calculate the cumulative track error and number of lidar points
		double errorSum = 0.0;
		int cntPoints = 0;
		int cntTracks = 0;
		for (LidarTrack aTrack : refTrackManager.getAllItems())
		{
			// Skip over Tracks that we are not interested in
			if (errorMode == ItemGroup.Visible && refTrackManager.getIsVisible(aTrack) == false)
				continue;
			if (errorMode == ItemGroup.Selected && selectedS.contains(aTrack) == false)
				continue;

			errorSum += refTrackManager.getTrackError(aTrack);
			cntPoints += aTrack.getNumberOfPoints();
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
		int cntFullItems = refTrackManager.getAllItems().size();
		boolean isEnabled = cntFullItems > 0;
		dragB.setEnabled(isEnabled);
		selectInvertB.setEnabled(isEnabled);

		int cntFullPoints = 0;
		for (LidarTrack aTrack : refTrackManager.getAllItems())
			cntFullPoints += aTrack.getNumberOfPoints();

		List<LidarTrack> pickL = refTrackManager.getSelectedItems();
		int cntPickItems = pickL.size();
		isEnabled = cntPickItems > 0;
		selectNoneB.setEnabled(isEnabled);

		isEnabled = cntFullItems > 0 && cntPickItems < cntFullItems;
		selectAllB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0;
		removeB.setEnabled(isEnabled);
		translateB.setEnabled(isEnabled);
		saveB.setEnabled(isEnabled);

		int cntPickPoints = 0;
		int cntShowItems = 0;
		for (LidarTrack aTrack : pickL)
		{
			cntPickPoints += aTrack.getNumberOfPoints();
			if (refTrackManager.getIsVisible(aTrack) == true)
				cntShowItems++;
		}

		isEnabled = cntPickItems > 0 && cntShowItems < cntPickItems;
		showB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0 && cntShowItems > 0;
		hideB.setEnabled(isEnabled);

		// Table title
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		String infoStr = "Tracks: " + cntFormat.format(cntFullItems);
		String helpStr = "Points: " + cntFormat.format(cntFullPoints);
		if (cntPickItems > 0)
		{
			infoStr += "  (Selected: " + cntFormat.format(cntPickItems) + ")";
			helpStr += "  (Selected: " + cntFormat.format(cntPickPoints) + ")";
		}
		titleL.setText(infoStr);
		titleL.setToolTipText(helpStr);
	}

}
