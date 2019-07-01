package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.jhuapl.saavtk.gui.ColorCellRenderer;
import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.IconUtil;
import edu.jhuapl.saavtk.gui.RadialOffsetChanger;
import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileCache.UnauthorizedAccessException;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.model.lidar.LidarBrowseUtil;
import edu.jhuapl.sbmt.model.lidar.LidarFileSpec;
import edu.jhuapl.sbmt.model.lidar.LidarFileSpecManager;

import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.NumberRenderer;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemManagerUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to display a list of lidar FileSpecs.
 * <P>
 * The following functionality is supported:
 * <UL>
 * <LI>Display list of FileSpecs in a table
 * <LI>Allow user to show or hide associated tracks
 * <LI>Allow user to save files corresponding to FileSpecs
 * <LI>Allow user to show corresponding spacecraft positions (for all
 * LidarPoints)
 * </UL>
 *
 * @author lopeznr1
 */
public class LidarFileSpecPanel extends JPanel implements ActionListener, ItemEventListener
{
	// Ref vars
	private LidarFileSpecManager refManager;

	// State vars
	private String refDataSourceName;
	private String failFetchMsg;

	// GUI vars
	private ItemListPanel<LidarFileSpec> lidarILP;
	private JLabel titleL;
	private JButton selectAllB, selectInvertB, selectNoneB;
	private JButton hideB, showB;
	private JButton saveB;
	private PercentIntervalChanger timeIntervalChanger;
	private RadialOffsetChanger radialOffsetChanger;
	private JCheckBox showSpacecraftCB;

	public LidarFileSpecPanel(LidarFileSpecManager aLidarModel, SmallBodyViewConfig aBodyViewConfig,
			String aDataSourceName)
	{
		refManager = aLidarModel;

		refDataSourceName = aDataSourceName;
		failFetchMsg = null;

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

		// Table content
		boolean isShortMode = refDataSourceName == null;
		String timeMaxStr = "2019-08-08T08:08:08.080";
		if (isShortMode == false)
			timeMaxStr = "2019-08-08T08:08:08.080808";

		QueryComposer<LookUp> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(LookUp.IsVisible, Boolean.class, "Show", 30);
		tmpComposer.addAttribute(LookUp.Color, Color.class, "Color", 30);
		tmpComposer.addAttribute(LookUp.NumPoints, Integer.class, "# pts", "987,");
		tmpComposer.addAttribute(LookUp.Name, String.class, "Name", null);
		tmpComposer.addAttribute(LookUp.BegTime, Double.class, "Start Time", timeMaxStr);
		tmpComposer.addAttribute(LookUp.EndTime, Double.class, "End Time", timeMaxStr);
		tmpComposer.getItem(LookUp.NumPoints).maxSize *= 3;
		tmpComposer.getItem(LookUp.Name).defaultSize *= 3;

		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(isShortMode);
		tmpComposer.setEditor(LookUp.IsVisible, new BooleanCellEditor());
		tmpComposer.setRenderer(LookUp.IsVisible, new BooleanCellRenderer());
		tmpComposer.setRenderer(LookUp.Color, new ColorCellRenderer(false));
		tmpComposer.setRenderer(LookUp.NumPoints, new NumberRenderer("###,###,###", "---"));
		tmpComposer.setRenderer(LookUp.BegTime, tmpTimeRenderer);
		tmpComposer.setRenderer(LookUp.EndTime, tmpTimeRenderer);

		ItemHandler<LidarFileSpec> tmpIH = new FileSpecItemHandler(refManager, tmpComposer);
		ItemProcessor<LidarFileSpec> tmpIP = refManager;
		lidarILP = new ItemListPanel<>(tmpIH, tmpIP, true);
		lidarILP.setSortingEnabled(true);

		JTable lidarTable = lidarILP.getTable();
		lidarTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		add(new JScrollPane(lidarTable), "growx,growy,pushx,pushy,span,wrap");

		// Action buttons: hide / save / show
		hideB = GuiUtil.formButton(this, "Hide");
		hideB.setToolTipText("Hide Items");
		saveB = new JButton("Save Files");
		saveB.addActionListener(this);
		showB = GuiUtil.formButton(this, "Show");
		showB.setToolTipText("Show Items");

		// Show spacecraft checkbox
		showSpacecraftCB = new JCheckBox("Show spacecraft position");
		showSpacecraftCB.setSelected(true);
		showSpacecraftCB.addActionListener(this);

		// Row 1: hideB, showSpacecraftCB
		add(hideB, "sg g1,span,split");
		add(showSpacecraftCB, "gapleft 20,wrap");

		// Row 2: showB, saveB
		add(showB, "sg g1,span,split");
		add(saveB, "gapleft 20,sg g2,wrap");

		// Displayed lidar time interval
		timeIntervalChanger = new PercentIntervalChanger("Displayed Lidar Data");
		timeIntervalChanger.addActionListener(this);
		add(timeIntervalChanger, "growx,span,wrap 0");

		// Radial offset section
		radialOffsetChanger = new RadialOffsetChanger();
		radialOffsetChanger.setModel(refManager);
		radialOffsetChanger.setOffsetScale(aBodyViewConfig.lidarOffsetScale);
		add(radialOffsetChanger, "growx,span,wrap 0");

		// Populate the table with the initialize data source
		populate(aBodyViewConfig, aDataSourceName);

		updateGui();

		// Register for events of interest
		refManager.addListener(this);
	}

	/**
	 * Method that will populate the table with LidarFileSpecs relative to the
	 * specified data source.
	 */
	public void populate(SmallBodyViewConfig aBodyViewConfig, String aDataSourceName)
	{
		String browseFileList = aBodyViewConfig.lidarBrowseDataSourceMap.get(aDataSourceName);

		try
		{
			FileCache.isFileGettable(aBodyViewConfig.lidarBrowseFileListResourcePath);

			List<LidarFileSpec> tmpL;
			if (aDataSourceName == null)
				tmpL = LidarBrowseUtil.loadLidarFileSpecListFor(aBodyViewConfig);
			else
				tmpL = LidarBrowseUtil.loadLidarFileSpecList(browseFileList);

			refManager.setAllItems(tmpL);

			failFetchMsg = null;
		}
		catch (IOException aExp)
		{
			failFetchMsg = "Failure: " + aExp.getMessage();
			aExp.printStackTrace();
		}
		catch (UnauthorizedAccessException aExp)
		{
			failFetchMsg = "No Results Available: Access Not Authorized";
		}
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		List<LidarFileSpec> tmpL = refManager.getSelectedItems();
		if (source == selectAllB)
			ItemManagerUtil.selectAll(refManager);
		else if (source == selectNoneB)
			ItemManagerUtil.selectNone(refManager);
		else if (source == selectInvertB)
			ItemManagerUtil.selectInvert(refManager);
		else if (source == hideB)
			refManager.setIsVisible(tmpL, false);
		else if (source == showB)
			refManager.setIsVisible(tmpL, true);
		else if (source == saveB)
			doActionSave();
		else if (source == timeIntervalChanger)
			refManager.setPercentageShown(timeIntervalChanger.getLowValue(), timeIntervalChanger.getHighValue());
		else if (source == showSpacecraftCB)
			refManager.setShowSpacecraftPosition(showSpacecraftCB.isSelected());

		updateGui();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		updateGui();
	}

	/**
	 * Helper method that handles the save action.
	 */
	private void doActionSave()
	{
		Component rootComp = JOptionPane.getFrameForComponent(this);
		List<LidarFileSpec> workL = refManager.getSelectedItems();

		// Prompt the user for the save folder
		String title = "Specify the folder to save " + workL.size() + " lidar files";
		File targPath = DirectoryChooser.showOpenDialog(rootComp, title);
		if (targPath == null)
			return;

		// Save all of the selected items into the target folder
		LidarFileSpec workFileSpec = null;
		int passCnt = 0;
		try
		{
			for (LidarFileSpec aFileSpec : workL)
			{
				workFileSpec = aFileSpec;
				File srcFile = FileCache.getFileFromServer(aFileSpec.getPath());
				File dstFile = new File(targPath, srcFile.getName());
				FileUtil.copyFile(srcFile, dstFile);
				passCnt++;
			}
		}
		catch (Exception aExp)
		{
			String errMsg = "Failed to save " + (workL.size() - passCnt) + "files. Failed on lidar file: ";
			errMsg += workFileSpec.getName();
			JOptionPane.showMessageDialog(rootComp, errMsg, "Error Saving Lidar Files", JOptionPane.ERROR_MESSAGE);
			aExp.printStackTrace();
		}
	}

	/**
	 * Updates the various UI elements to keep them synchronized
	 */
	private void updateGui()
	{
		// Update various buttons
		int cntFullItems = refManager.getAllItems().size();
		boolean isEnabled = cntFullItems > 0;
		selectInvertB.setEnabled(isEnabled);

		List<LidarFileSpec> pickL = refManager.getSelectedItems();
		int cntPickItems = pickL.size();
		isEnabled = cntPickItems > 0;
		selectNoneB.setEnabled(isEnabled);

		isEnabled = cntFullItems > 0 && cntPickItems < cntFullItems;
		selectAllB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0;
		saveB.setEnabled(isEnabled);

		int cntFullPts = 0;
		for (LidarFileSpec aFileSpec : refManager.getAllItems())
			cntFullPts += refManager.getNumberOfPoints(aFileSpec);

		int cntShowItems = 0;
		int cntPickPoints = 0;
		for (LidarFileSpec aItem : pickL)
		{
			if (refManager.getIsVisible(aItem) == true)
				cntShowItems++;
			if (refManager.isLoaded(aItem) == true)
				cntPickPoints += refManager.getNumberOfPoints(aItem);
		}

		isEnabled = cntPickItems > 0 && cntShowItems < cntPickItems;
		showB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0 && cntShowItems > 0;
		hideB.setEnabled(isEnabled);

		String extraTag = ": ";
		if (refDataSourceName != null)
			extraTag = " (" + refDataSourceName + ") : ";

		// Table title
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		String infoStr = "Files" + extraTag + cntFormat.format(cntFullItems);
		String helpStr = "Points " + cntFormat.format(cntFullPts);
		if (cntPickItems > 0)
		{
			infoStr += "  (Selected: " + cntFormat.format(cntPickItems) + ")";
			helpStr += "  (Selected: " + cntFormat.format(cntPickPoints) + ")";
		}
		if (failFetchMsg != null)
		{
			infoStr = failFetchMsg;
			helpStr = null;
		}
		titleL.setText(infoStr);
		titleL.setToolTipText(helpStr);
	}

}
