package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import edu.jhuapl.saavtk.colormap.SigFigNumberFormat;
import edu.jhuapl.saavtk.gui.ColorCellRenderer;
import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.IconUtil;
import edu.jhuapl.saavtk.gui.RadialOffsetChanger;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.util.Properties;
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
		PickManagerListener, PropertyChangeListener, TableModelListener
{
	// Ref vars
	private ModelManager refModelManager;
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
	private JLabel errorL;

	private RadialOffsetChanger radialOffsetChanger;
	private JSpinner pointSizeSpinner;

	public LidarListPanel(ModelManager aModelManager, LidarSearchDataCollection aModel, PickManager aPickManager,
			Renderer aRenderer)
	{
		refModelManager = aModelManager;
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
		showErrorCB = new JCheckBox("Show Error");
		showErrorCB.addActionListener(this);
		showErrorCB.setToolTipText(
				"<html>\nIf checked, the track error will be calculated and shown to the right of this checkbox.<br>\nWhenever a change is made to the tracks, the track error will be updated. This can be<br>\na slow operation which is why this checkbox is provided to disable it.<br>\n<br>\nThe track error is computed as the mean distance between each lidar point and its<br>\nclosest point on the asteroid.");
		errorL = new JLabel("");
		add(showErrorCB, "span,split");
		add(errorL, "growx,w 0::");

		updateGui();
		configureColumnWidths();

		// Register for events of interest
		PickUtil.autoDeactivatePickerWhenComponentHidden(refPickManager, lidarPicker, this);
		tableModel.addTableModelListener(this);
		refModel.addPropertyChangeListener(this);
		refPickManager.addListener(this);
		refPickManager.getDefaultPicker().addPropertyChangeListener(this);
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

		int[] idArr = getSelectedTracks();
		if (source == selectAllB)
			trackTable.selectAll();
		else if (source == selectNoneB)
			trackTable.clearSelection();
		else if (source == selectInvertB)
			GuiUtil.invertSelection(trackTable, this);
		else if (source == hideB)
			refModel.setTrackVisible(idArr, false);
		else if (source == showB)
			refModel.setTrackVisible(idArr, true);
		else if (source == removeB)
			refModel.removeAllLidarData();
		else if (source == translateB)
			doActionTranslate(idArr);
		else if (source == dragB)
			doActionDrag();
		else if (source == showErrorCB)
			doActionError();

		updateGui();
	}

	@Override
	public void pickerChanged()
	{
		boolean tmpBool = lidarPicker == refPickManager.getActivePicker();
		dragB.setSelected(tmpBool);
	}

	@Override
	public void propertyChange(PropertyChangeEvent aEvent)
	{
		if (Properties.MODEL_CHANGED.equals(aEvent.getPropertyName()) == true)
		{
			updateErrorUI();
			updateGui();
		}
		else if (Properties.MODEL_PICKED.equals(aEvent.getPropertyName()) == true)
		{
			handleTrackPicked(aEvent);
		}

		trackTable.repaint();
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
		updateGui();
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
			refModel.deselectSelectedPoint();
	}

	/**
	 * Helper method that handles the show error action.
	 */
	private void doActionError()
	{
		boolean tmpBool = showErrorCB.isSelected();
		refModel.setEnableTrackErrorComputation(tmpBool);

		updateErrorUI();
	}

	/**
	 * Helper method that handles the drag action.
	 */
	private void doActionTranslate(int[] aIdArr)
	{
		if (translateDialog == null)
			translateDialog = new LidarTrackTranslateDialog(this, refModel);

		translateDialog.setTracks(aIdArr);
		translateDialog.setVisible(true);
	}

	/**
	 * Helper method that returns an array corresponding to the selected track
	 * indexes.
	 */
	private int[] getSelectedTracks()
	{
		return trackTable.getSelectedRows();
	}

	/**
	 * Helper method to determine which track is picked and select the
	 * corresponding table row (and scroll to the selected row).
	 */
	private void handleTrackPicked(PropertyChangeEvent aEvent)
	{
		PickEvent pickEvent = (PickEvent) aEvent.getNewValue();
		boolean isPass = refModelManager.getModel(pickEvent.getPickedProp()) == refModel;
		isPass &= refModel.isDataPointsProp(pickEvent.getPickedProp()) == true;
		if (isPass == false)
			return;

		int id = pickEvent.getPickedCellId();
		refModel.selectPoint(id);

		int idx = refModel.getTrackIdFromCellId(id);
		if (idx < 0)
			return;

		trackTable.setRowSelectionInterval(idx, idx);
		Rectangle cellBounds = trackTable.getCellRect(idx, 0, true);
		if (cellBounds != null)
			trackTable.scrollRectToVisible(cellBounds);
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
			public void mouseClicked(MouseEvent e)
			{
				// Handle the Color customization
				int row = trackTable.rowAtPoint(e.getPoint());
				int col = trackTable.columnAtPoint(e.getPoint());
				if (e.getClickCount() == 2 && row >= 0 && col == 1)
				{
					Color oldColor = refModel.getTrack(row).getColor();
					Color tmpColor = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(trackTable), oldColor);
					if (tmpColor != null)
						refModel.setTrackColor(row, tmpColor);

					return;
				}

				maybeShowPopup(e);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				maybeShowPopup(e);
			}
		});
	}

	/**
	 * Helper method to handle the showing of the table popup menu.
	 */
	private void maybeShowPopup(MouseEvent aEvent)
	{
		if (aEvent.isPopupTrigger() == false)
			return;

		// TODO: Is this necessary?
		// Force the menu to be hidden by default
		lidarPopupMenu.setVisible(false);

		// Update the selection to include the selected row
		int index = trackTable.rowAtPoint(aEvent.getPoint());
		if (index > 0)
		{
			if (aEvent.isControlDown() == true)
				trackTable.addRowSelectionInterval(index, index);
			else
				trackTable.setRowSelectionInterval(index, index);
		}

		// Bail if there is no selection
		int[] idArr = getSelectedTracks();
		if (idArr.length == 0)
			return;

		lidarPopupMenu.setSelectedTracks(idArr);
		lidarPopupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
	}

	/**
	 * Helper method to update the error UI
	 */
	private void updateErrorUI()
	{
		String errorStr = "";

		boolean tmpBool = showErrorCB.isSelected();
		if (tmpBool == true)
		{
			SigFigNumberFormat errFormat = new SigFigNumberFormat(7);
			DecimalFormat cntFormat = new DecimalFormat("#,###");
			double errVal = refModel.getTrackError();
			int numTracks = refModel.getNumberOfVisibleTracks();
			int numPoints = refModel.getLastNumberOfPointsForTrackError();
			errorStr = "" + errFormat.format(errVal) + " RMS: ";
			errorStr += cntFormat.format(numTracks) + " visible tracks / ";
			errorStr += "(" + cntFormat.format(numPoints) + " points )";
		}
		errorL.setText(errorStr);
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

		int[] idArr = getSelectedTracks();
		int cntPickTracks = idArr.length;
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
		for (int aId : idArr)
		{
			Track tmpTrack = refModel.getTrack(aId);
			cntPickPoints += tmpTrack.getNumberOfPoints();

			if (tmpTrack.getIsVisible() == true)
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

}
