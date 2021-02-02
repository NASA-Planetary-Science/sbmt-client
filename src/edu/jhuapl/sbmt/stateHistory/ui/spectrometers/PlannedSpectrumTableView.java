package edu.jhuapl.sbmt.stateHistory.ui.spectrometers;

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
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrum;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemManagerUtil;

public class PlannedSpectrumTableView extends JPanel
{
	/**
	 * JButton to load state history from file
	 */
	private JButton loadPlannedSpectrumButton;

    /**
     * JButton to remove state history from table
     */
    private JButton hidePlannedSpectrumButton;

    /**
     * JButton to show state history in renderer
     */
    private JButton showPlannedSpectrumButton;

    /**
     * JButton to save state history to file
     */
    private JButton savePlannedSpectrumButton;

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
     * The collection of loaded state history objects
     */
    private PlannedSpectrumCollection plannedSpectrumCollection;

    /**
     *	The state history item list panel, used to help handle interactions with the table
     */
    private ItemListPanel<PlannedSpectrum> plannedSpectrumILP;

    /**
     * The state history table handler, used to help populate the table
     */
    private ItemHandler<PlannedSpectrum> plannedSpectrumTableHandler;

    private JLabel processingLabel;

	public PlannedSpectrumTableView(PlannedSpectrumCollection plannedSpectrumCollection)
	{
		this.plannedSpectrumCollection = plannedSpectrumCollection;
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
        setBorder(new TitledBorder(null, "Planned Spectrums", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
				ItemManagerUtil.selectAll(plannedSpectrumCollection);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(plannedSpectrumCollection);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(plannedSpectrumCollection);
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

//		saveStateHistoryButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.floppyDriveIcon"));
//		saveStateHistoryButton.setToolTipText(ToolTipUtil.getItemSave());
//		saveStateHistoryButton.setEnabled(false);

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
		QueryComposer<PlannedSpectrumColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(PlannedSpectrumColumnLookup.Show, Boolean.class, "Show", null);
//		tmpComposer.addAttribute(PlannedSpectrumColumnLookup.Frus, Boolean.class, "Frus", null);
		tmpComposer.addAttribute(PlannedSpectrumColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(PlannedSpectrumColumnLookup.Instrument, String.class, "Instrument", null);
		tmpComposer.addAttribute(PlannedSpectrumColumnLookup.SpectrumTime, String.class, "Spectrum Time", null);


		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);

		tmpComposer.setEditor(PlannedSpectrumColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedSpectrumColumnLookup.Show, new BooleanCellRenderer());
//		tmpComposer.setEditor(PlannedSpectrumColumnLookup.Frus, new BooleanCellEditor());
//		tmpComposer.setRenderer(PlannedSpectrumColumnLookup.Frus, new BooleanCellRenderer());
		tmpComposer.setEditor(PlannedSpectrumColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
		tmpComposer.setRenderer(PlannedSpectrumColumnLookup.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setRenderer(PlannedSpectrumColumnLookup.SpectrumTime, tmpTimeRenderer);

		plannedSpectrumTableHandler = new PlannedSpectrumItemHandler(plannedSpectrumCollection, tmpComposer);
		ItemProcessor<PlannedSpectrum> tmpIP = plannedSpectrumCollection;
		plannedSpectrumILP = new ItemListPanel<>(plannedSpectrumTableHandler, tmpIP, true);
		plannedSpectrumILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable plannedSpectrumTable = plannedSpectrumILP.getTable();
		plannedSpectrumTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//TODO: Fix the popup menu
		plannedSpectrumTable.addMouseListener(new TablePopupHandler(plannedSpectrumCollection, null));

		return plannedSpectrumTable;
    }

	/**
	 * Configures the appropriate table colun width for the given expected type of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = plannedSpectrumILP.getTable();
		String dateTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, /*true,*/ blackCP, dateTimeStr, dateTimeStr };
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
	 * @return the savePlannedSpectrumButton
	 */
	public JButton getSavePlannedSpectrumButton()
	{
		return savePlannedSpectrumButton;
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
