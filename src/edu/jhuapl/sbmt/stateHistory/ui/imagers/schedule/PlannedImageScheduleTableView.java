package edu.jhuapl.sbmt.stateHistory.ui.imagers.schedule;

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
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageScheduleCollection;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemManagerUtil;

public class PlannedImageScheduleTableView extends JPanel
{
	/**
	 * JButton to load planned image from file
	 */
	private JButton loadPlannedImageButton;

    /**
     * JButton to remove planned image from table
     */
    private JButton hidePlannedImageButton;

    /**
     * JButton to show planned image in renderer
     */
    private JButton showPlannedImageButton;

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
     * The collection of loaded planned image objects
     */
    private PlannedImageScheduleCollection plannedImageScheduleCollection;

    /**
     *	The planned image item list panel, used to help handle interactions with the table
     */
    private ItemListPanel<PlannedImageCollection> plannedImageILP;

    /**
     * The planned image table handler, used to help populate the table
     */
    private ItemHandler<PlannedImageCollection> plannedImageScheduleTableHandler;

    private JLabel processingLabel;

	public PlannedImageScheduleTableView(PlannedImageScheduleCollection plannedImageCollection)
	{
		this.plannedImageScheduleCollection = plannedImageCollection;
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
        setBorder(new TitledBorder(null, "Planned Images", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
				ItemManagerUtil.selectAll(plannedImageScheduleCollection);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(plannedImageScheduleCollection);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(plannedImageScheduleCollection);
			}
		};

		//Popup menu
		//TODO restore this
//		StateHistoryPopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil.formStateHistoryFileSpecPopupMenu(plannedImageCollection, this);

    	// Table header
		loadPlannedImageButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.directoryIcon"));
		loadPlannedImageButton.setToolTipText(ToolTipUtil.getItemLoad());

		processingLabel = new JLabel("Ready.");

		syncWithTimelineButton = GuiUtil.formToggleButton(listener, IconUtil.getItemSyncFalse(), IconUtil.getItemSyncTrue());
		syncWithTimelineButton.setToolTipText("Sync Visibility with Time slider");

		showPlannedImageButton = GuiUtil.formButton(listener, IconUtil.getItemShow());
		showPlannedImageButton.setToolTipText(ToolTipUtil.getItemShow());
		showPlannedImageButton.setEnabled(false);

		hidePlannedImageButton = GuiUtil.formButton(listener, IconUtil.getItemHide());
		hidePlannedImageButton.setToolTipText(ToolTipUtil.getItemHide());
		hidePlannedImageButton.setEnabled(false);

		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(loadPlannedImageButton);
		buttonPanel.add(processingLabel);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(syncWithTimelineButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(showPlannedImageButton);
		buttonPanel.add(hidePlannedImageButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<PlannedImageScheduleColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(PlannedImageScheduleColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(PlannedImageScheduleColumnLookup.Details, Boolean.class, "Details", null);
//		tmpComposer.addAttribute(PlannedImageScheduleColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(PlannedImageScheduleColumnLookup.Filename, String.class, "Filename", null);
		tmpComposer.addAttribute(PlannedImageScheduleColumnLookup.StateHistory, String.class, "History Segment", null);

		tmpComposer.setEditor(PlannedImageScheduleColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedImageScheduleColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(PlannedImageScheduleColumnLookup.Details, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedImageScheduleColumnLookup.Details, new BooleanCellRenderer());
//		tmpComposer.setEditor(PlannedImageScheduleColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
//		tmpComposer.setRenderer(PlannedImageScheduleColumnLookup.Color, new ColorProviderCellRenderer(false));


		plannedImageScheduleTableHandler = new PlannedImageScheduleItemHandler(plannedImageScheduleCollection, tmpComposer);
		ItemProcessor<PlannedImageCollection> tmpIP = plannedImageScheduleCollection;
		plannedImageILP = new ItemListPanel<>(plannedImageScheduleTableHandler, tmpIP, true);
		plannedImageILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable plannedImageTable = plannedImageILP.getTable();
		plannedImageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//TODO: Fix the popup menu
		plannedImageTable.addMouseListener(new TablePopupHandler(plannedImageScheduleCollection, null));

		return plannedImageTable;
    }

	/**
	 * Configures the appropriate table colun width for the given expected type of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = plannedImageILP.getTable();
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
	 * @return the loadPlannedImageButton
	 */
	public JButton getLoadPlannedImageButton()
	{
		return loadPlannedImageButton;
	}

	/**
	 * @return the removePlannedImageButton
	 */
	public JButton getHidePlannedImageButton()
	{
		return hidePlannedImageButton;
	}

	/**
	 * @return the showPlannedImageButton
	 */
	public JButton getShowPlannedImageButton()
	{
		return showPlannedImageButton;
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