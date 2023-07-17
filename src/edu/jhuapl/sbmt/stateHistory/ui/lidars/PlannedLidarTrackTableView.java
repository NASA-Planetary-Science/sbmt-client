package edu.jhuapl.sbmt.stateHistory.ui.lidars;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import edu.jhuapl.saavtk.color.gui.ColorProviderCellEditor;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellRenderer;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.core.gui.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrack;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackVtkCollection;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.ItemManagerUtil;

public class PlannedLidarTrackTableView extends JPanel
{
	/**
	 * JButton to load planned image from file
	 */
	private JButton loadPlannedLidarTrackButton;

    /**
     * JButton to remove planned LidarTrack from table
     */
    private JButton hidePlannedLidarTrackButton;

    /**
     * JButton to show planned LidarTrack in renderer
     */
    private JButton showPlannedLidarTrackButton;

    /**
     * JButton to save planned LidarTrack to file
     */
    private JButton savePlannedLidarTrackButton;

    /**
     *	JTable to display loaded state histories
     */
    protected JTable table;

    /**
     * JButtons for selection in the table
     */
    private JButton selectAllB, selectInvertB, selectNoneB;

    private JToggleButton syncWithTimelineButton;

    /**
     * The collection of loaded planned LidarTrack objects
     */
    private PlannedLidarTrackVtkCollection plannedLidarTrackCollection;

    /**
     *	The planned LidarTrack item list panel, used to help handle interactions with the table
     */
    private ItemListPanel<PlannedLidarTrack> plannedLidarTrackILP;

    private JLabel processingLabel;

    /**
     * The planned LidarTrack table handler, used to help populate the table
     */
    private ItemHandler<PlannedLidarTrack> plannedLidarTrackTableHandler;

	public PlannedLidarTrackTableView(PlannedLidarTrackVtkCollection plannedLidarTrackCollection)
	{
		this.plannedLidarTrackCollection = plannedLidarTrackCollection;
		init();
	}

    /**
     * Initializes UI elements
     */
    protected void init()
    {
        table = buildTable();
    }

    /**
     *	Sets up the UI elements
     */
    public void setup()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder(null, "Planned LidarTracks", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(150, 150));
        add(scrollPane);

        scrollPane.setViewportView(table);
    }

    /**
     * Builds the JTable.
     * @return
     */
    private JTable buildTable()
    {
    	ActionListener listener = e -> {
			Object source = e.getSource();

			if (source == selectAllB)
				ItemManagerUtil.selectAll(plannedLidarTrackCollection);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(plannedLidarTrackCollection);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(plannedLidarTrackCollection);
			}
		};

		//Popup menu
		//TODO restore this
//		StateHistoryPopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil.formStateHistoryFileSpecPopupMenu(plannedLidarTrackCollection, this);

    	// Table header
		loadPlannedLidarTrackButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.directoryIcon"));
		loadPlannedLidarTrackButton.setToolTipText(ToolTipUtil.getItemLoad());

		processingLabel = new JLabel("Ready.");


		syncWithTimelineButton = GuiUtil.formToggleButton(listener, IconUtil.getItemSyncFalse(), IconUtil.getItemSyncTrue());
		syncWithTimelineButton.setToolTipText("Sync Visibility with Time slider");

//		saveStateHistoryButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.floppyDriveIcon"));
//		saveStateHistoryButton.setToolTipText(ToolTipUtil.getItemSave());
//		saveStateHistoryButton.setEnabled(false);

		showPlannedLidarTrackButton = GuiUtil.formButton(listener, IconUtil.getItemShow());
		showPlannedLidarTrackButton.setToolTipText(ToolTipUtil.getItemShow());
		showPlannedLidarTrackButton.setEnabled(false);

		hidePlannedLidarTrackButton = GuiUtil.formButton(listener, IconUtil.getItemHide());
		hidePlannedLidarTrackButton.setToolTipText(ToolTipUtil.getItemHide());
		hidePlannedLidarTrackButton.setEnabled(false);

		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(loadPlannedLidarTrackButton);
		buttonPanel.add(processingLabel);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(syncWithTimelineButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(showPlannedLidarTrackButton);
		buttonPanel.add(hidePlannedLidarTrackButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<PlannedLidarTrackColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(PlannedLidarTrackColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(PlannedLidarTrackColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(PlannedLidarTrackColumnLookup.Instrument, String.class, "Instrument", null);
		tmpComposer.addAttribute(PlannedLidarTrackColumnLookup.TrackStartTime, String.class, "Track Start Time", null);
		tmpComposer.addAttribute(PlannedLidarTrackColumnLookup.TrackStopTime, String.class, "Track Stop Time", null);

		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);

		tmpComposer.setEditor(PlannedLidarTrackColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedLidarTrackColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(PlannedLidarTrackColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
		tmpComposer.setRenderer(PlannedLidarTrackColumnLookup.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setRenderer(PlannedLidarTrackColumnLookup.TrackStartTime, tmpTimeRenderer);
		tmpComposer.setRenderer(PlannedLidarTrackColumnLookup.TrackStopTime, tmpTimeRenderer);

		plannedLidarTrackTableHandler = new PlannedLidarTrackItemHandler(plannedLidarTrackCollection, tmpComposer);
		ItemProcessor<PlannedLidarTrack> tmpIP = plannedLidarTrackCollection;
		plannedLidarTrackILP = new ItemListPanel<>(plannedLidarTrackTableHandler, tmpIP, true);
		plannedLidarTrackILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable plannedLidarTrackTable = plannedLidarTrackILP.getTable();
		plannedLidarTrackTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//TODO: Fix the popup menu
//		plannedLidarTrackTable.addMouseListener(new TablePopupHandler(plannedLidarTrackCollection, null));

		return plannedLidarTrackTable;
    }

	/**
	 * Configures the appropriate table colun width for the given expected type of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = plannedLidarTrackILP.getTable();
		String dateTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 40;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, blackCP, "Instrument", dateTimeStr, dateTimeStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}

	/**
	 * @return the loadPlannedLidarTrackButton
	 */
	public JButton getLoadPlannedLidarTrackButton()
	{
		return loadPlannedLidarTrackButton;
	}

	/**
	 * @return the removePlannedLidarTrackButton
	 */
	public JButton getHidePlannedLidarTrackButton()
	{
		return hidePlannedLidarTrackButton;
	}

	/**
	 * @return the showPlannedLidarTrackButton
	 */
	public JButton getShowPlannedLidarTrackButton()
	{
		return showPlannedLidarTrackButton;
	}

	/**
	 * @return the savePlannedLidarTrackButton
	 */
	public JButton getSavePlannedLidarTrackButton()
	{
		return savePlannedLidarTrackButton;
	}

	/**
	 * @return the syncWithTimelineButton
	 */
	public JToggleButton getSyncWithTimelineButton()
	{
		return syncWithTimelineButton;
	}

	/**
	 * @return the processingLabel
	 */
	public JLabel getProcessingLabel()
	{
		return processingLabel;
	}

	/**
	 * @return the table
	 */
	public JTable getTable()
	{
		return table;
	}
}