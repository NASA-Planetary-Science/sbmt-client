package edu.jhuapl.sbmt.stateHistory.ui.spectrometers.schedule;

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
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumScheduleCollection;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemManagerUtil;

public class PlannedSpectrumScheduleTableView extends JPanel
{
	/**
	 * JButton to load planned spectrum from file
	 */
	private JButton loadPlannedSpectrumButton;

    /**
     * JButton to remove planned spectrum from table
     */
    private JButton hidePlannedSpectrumButton;

    /**
     * JButton to show planned spectrum in renderer
     */
    private JButton showPlannedSpectrumButton;

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
     * The collection of loaded planned spectrum objects
     */
    private PlannedSpectrumScheduleCollection plannedSpectrumScheduleCollection;

    /**
     *	The planned spectrum item list panel, used to help handle interactions with the table
     */
    private ItemListPanel<PlannedSpectrumCollection> plannedSpectrumILP;

    /**
     * The planned spectrum table handler, used to help populate the table
     */
    private ItemHandler<PlannedSpectrumCollection> plannedSpectrumScheduleTableHandler;

    private JLabel processingLabel;

	public PlannedSpectrumScheduleTableView(PlannedSpectrumScheduleCollection plannedSpectrumCollection)
	{
		this.plannedSpectrumScheduleCollection = plannedSpectrumCollection;
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
        setBorder(new TitledBorder(null, "Planned Spectra", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
				ItemManagerUtil.selectAll(plannedSpectrumScheduleCollection);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(plannedSpectrumScheduleCollection);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(plannedSpectrumScheduleCollection);
			}
		};

		//Popup menu
		//TODO restore this
//		StateHistoryPopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil.formStateHistoryFileSpecPopupMenu(plannedSpectrumCollection, this);

    	// Table header
		loadPlannedSpectrumButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.directoryIcon"));
		loadPlannedSpectrumButton.setToolTipText(ToolTipUtil.getItemLoad());

		processingLabel = new JLabel("Ready.");

		syncWithTimelineButton = GuiUtil.formToggleButton(listener, IconUtil.getItemSyncFalse(), IconUtil.getItemSyncTrue());
		syncWithTimelineButton.setToolTipText("Sync Visibility with Time slider");

		showPlannedSpectrumButton = GuiUtil.formButton(listener, IconUtil.getItemShow());
		showPlannedSpectrumButton.setToolTipText(ToolTipUtil.getItemShow());
		showPlannedSpectrumButton.setEnabled(false);

		hidePlannedSpectrumButton = GuiUtil.formButton(listener, IconUtil.getItemHide());
		hidePlannedSpectrumButton.setToolTipText(ToolTipUtil.getItemHide());
		hidePlannedSpectrumButton.setEnabled(false);

		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(loadPlannedSpectrumButton);
		buttonPanel.add(processingLabel);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(syncWithTimelineButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(showPlannedSpectrumButton);
		buttonPanel.add(hidePlannedSpectrumButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<PlannedSpectrumScheduleColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(PlannedSpectrumScheduleColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(PlannedSpectrumScheduleColumnLookup.Details, Boolean.class, "Details", null);
//		tmpComposer.addAttribute(PlannedSpectrumScheduleColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(PlannedSpectrumScheduleColumnLookup.Filename, String.class, "Filename", null);
		tmpComposer.addAttribute(PlannedSpectrumScheduleColumnLookup.StateHistory, String.class, "History Segment", null);

		tmpComposer.setEditor(PlannedSpectrumScheduleColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedSpectrumScheduleColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(PlannedSpectrumScheduleColumnLookup.Details, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedSpectrumScheduleColumnLookup.Details, new BooleanCellRenderer());
//		tmpComposer.setEditor(PlannedSpectrumScheduleColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
//		tmpComposer.setRenderer(PlannedSpectrumScheduleColumnLookup.Color, new ColorProviderCellRenderer(false));


		plannedSpectrumScheduleTableHandler = new PlannedSpectrumScheduleItemHandler(plannedSpectrumScheduleCollection, tmpComposer);
		ItemProcessor<PlannedSpectrumCollection> tmpIP = plannedSpectrumScheduleCollection;
		plannedSpectrumILP = new ItemListPanel<>(plannedSpectrumScheduleTableHandler, tmpIP, true);
		plannedSpectrumILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable plannedSpectrumTable = plannedSpectrumILP.getTable();
		plannedSpectrumTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//TODO: Fix the popup menu
		plannedSpectrumTable.addMouseListener(new TablePopupHandler(plannedSpectrumScheduleCollection, null));

		return plannedSpectrumTable;
    }

	/**
	 * Configures the appropriate table colun width for the given expected type of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = plannedSpectrumILP.getTable();
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
	 * @return the loadPlannedSpectrumButton
	 */
	public JButton getLoadPlannedSpectrumButton()
	{
		return loadPlannedSpectrumButton;
	}

	/**
	 * @return the removePlannedSpectrumButton
	 */
	public JButton getHidePlannedSpectrumButton()
	{
		return hidePlannedSpectrumButton;
	}

	/**
	 * @return the showPlannedSpectrumButton
	 */
	public JButton getShowPlannedSpectrumButton()
	{
		return showPlannedSpectrumButton;
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