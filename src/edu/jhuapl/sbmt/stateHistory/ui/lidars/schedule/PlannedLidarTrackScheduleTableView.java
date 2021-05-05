package edu.jhuapl.sbmt.stateHistory.ui.lidars.schedule;

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

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackScheduleCollection;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemManagerUtil;

public class PlannedLidarTrackScheduleTableView extends JPanel
{
	/**
	 * JButton to load planned LidarTrack from file
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
    private PlannedLidarTrackScheduleCollection plannedLidarTrackScheduleCollection;

    /**
     *	The planned LidarTrack item list panel, used to help handle interactions with the table
     */
    private ItemListPanel<PlannedLidarTrackCollection> plannedLidarTrackILP;

    /**
     * The planned LidarTrack table handler, used to help populate the table
     */
    private ItemHandler<PlannedLidarTrackCollection> plannedLidarTrackScheduleTableHandler;

    private JLabel processingLabel;

	public PlannedLidarTrackScheduleTableView(PlannedLidarTrackScheduleCollection plannedLidarTrackCollection)
	{
		this.plannedLidarTrackScheduleCollection = plannedLidarTrackCollection;
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
        setBorder(new TitledBorder(null, "Planned Lidar Tracks", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
				ItemManagerUtil.selectAll(plannedLidarTrackScheduleCollection);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(plannedLidarTrackScheduleCollection);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(plannedLidarTrackScheduleCollection);
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
		syncWithTimelineButton.setSelected(true);

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
		QueryComposer<PlannedLidarTrackScheduleColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(PlannedLidarTrackScheduleColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(PlannedLidarTrackScheduleColumnLookup.Details, Boolean.class, "Details", null);
//		tmpComposer.addAttribute(PlannedLidarTrackScheduleColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(PlannedLidarTrackScheduleColumnLookup.Filename, String.class, "Filename", null);
		tmpComposer.addAttribute(PlannedLidarTrackScheduleColumnLookup.StateHistory, String.class, "History Segment", null);

		tmpComposer.setEditor(PlannedLidarTrackScheduleColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedLidarTrackScheduleColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(PlannedLidarTrackScheduleColumnLookup.Details, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedLidarTrackScheduleColumnLookup.Details, new BooleanCellRenderer());
//		tmpComposer.setEditor(PlannedLidarTrackScheduleColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
//		tmpComposer.setRenderer(PlannedLidarTrackScheduleColumnLookup.Color, new ColorProviderCellRenderer(false));


		plannedLidarTrackScheduleTableHandler = new PlannedLidarTrackScheduleItemHandler(plannedLidarTrackScheduleCollection, tmpComposer);
		ItemProcessor<PlannedLidarTrackCollection> tmpIP = plannedLidarTrackScheduleCollection;
		plannedLidarTrackILP = new ItemListPanel<>(plannedLidarTrackScheduleTableHandler, tmpIP, true);
		plannedLidarTrackILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable plannedLidarTrackTable = plannedLidarTrackILP.getTable();
		plannedLidarTrackTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//TODO: Fix the popup menu
		plannedLidarTrackTable.addMouseListener(new TablePopupHandler(plannedLidarTrackScheduleCollection, null));

		return plannedLidarTrackTable;
    }

	/**
	 * Configures the appropriate table colun width for the given expected type of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = plannedLidarTrackILP.getTable();
		String dateTimeStr = "9999-88-88T00:00:00.000000";
		String fileStr = "9999-88-88T00:00:00.0000009999-88-88T00:00:00.000000";
		int minW = 40;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, true, /*blackCP,*/ fileStr, dateTimeStr };
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