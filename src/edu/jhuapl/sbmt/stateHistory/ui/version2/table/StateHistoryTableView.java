package edu.jhuapl.sbmt.stateHistory.ui.version2.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.gui.table.ColorProviderCellEditor;
import edu.jhuapl.sbmt.gui.table.ColorProviderCellRenderer;
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.lidar.gui.color.ColorProvider;
import edu.jhuapl.sbmt.lidar.gui.color.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.popup.StateHistoryGuiUtil;
import edu.jhuapl.sbmt.stateHistory.ui.popup.StateHistoryPopupMenu;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemManagerUtil;

/**
 * @author steelrj1
 *
 */
public class StateHistoryTableView extends JPanel
{
	/**
	 * JButton to load state history from file
	 */
	private JButton loadStateHistoryButton;

    /**
     * JButton to remove state history from table
     */
    private JButton removeStateHistoryButton;

    /**
     * JButton to show state history in renderer
     */
    private JButton showStateHistoryButton;

    /**
     * JButton to save state history to file
     */
    private JButton saveStateHistoryButton;

    /**
     *	JTable to display loaded state histories
     */
    protected JTable resultList;

    /**
     * JButtons for selection in the table
     */
    private JButton selectAllB, selectInvertB, selectNoneB;

    /**
     * The collection of loaded state history objects
     */
    private StateHistoryCollection stateHistoryCollection;

    /**
     *	The state history item list panel, used to help handle interactions with the table
     */
    private ItemListPanel<StateHistory> stateHistoryILP;

    /**
     * The state history table handler, used to help populate the table
     */
    private ItemHandler<StateHistory> stateHistoryTableHandler;


    /**
     * @wbp.parser.constructor
     */
    /**
     * @param stateHistoryCollection
     */
    public StateHistoryTableView(StateHistoryCollection stateHistoryCollection/*, SpectrumPopupMenu spectrumPopupMenu*/)
    {
        this.stateHistoryCollection = stateHistoryCollection;
        init();
    }

    /**
     * Initializes UI elements
     */
    protected void init()
    {
        resultList = buildTable();
        removeStateHistoryButton = new JButton("Hide State History");
        showStateHistoryButton = new JButton("Show State History");
        removeStateHistoryButton.setEnabled(false);
        showStateHistoryButton.setEnabled(false);
        loadStateHistoryButton = new JButton("Load...");
        saveStateHistoryButton = new JButton("Save...");
        saveStateHistoryButton.setEnabled(false);
    }

    /**
     *	Sets up the UI elements
     */
    public void setup()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder(null, "Available Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(150, 150));
        add(scrollPane);

        scrollPane.setViewportView(resultList);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
        panel_1.add(showStateHistoryButton);
        panel_1.add(removeStateHistoryButton);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
        panel_2.add(loadStateHistoryButton);
        panel_2.add(saveStateHistoryButton);
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
				ItemManagerUtil.selectAll(stateHistoryCollection);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(stateHistoryCollection);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(stateHistoryCollection);
			}
		};

		//Popup menu
		StateHistoryPopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil.formStateHistoryFileSpecPopupMenu(stateHistoryCollection, this);

    	// Table header
		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<StateHistoryColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(StateHistoryColumnLookup.Map, Boolean.class, "Map", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.Description, String.class, "Description", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.StartTime, String.class, "Start Time", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.EndTime, String.class, "End Time", null);


		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);
		tmpComposer.setEditor(StateHistoryColumnLookup.Map, new BooleanCellEditor());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Map, new BooleanCellRenderer());
		tmpComposer.setEditor(StateHistoryColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(StateHistoryColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(StateHistoryColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setRenderer(StateHistoryColumnLookup.Description, tmpTimeRenderer);
		tmpComposer.setRenderer(StateHistoryColumnLookup.StartTime, tmpTimeRenderer);
		tmpComposer.setRenderer(StateHistoryColumnLookup.EndTime, tmpTimeRenderer);

		stateHistoryTableHandler = new StateHistoryItemHandler(stateHistoryCollection, tmpComposer);
		ItemProcessor<StateHistory> tmpIP = stateHistoryCollection;
		stateHistoryILP = new ItemListPanel<>(stateHistoryTableHandler, tmpIP, true);
		stateHistoryILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable stateHistoryTable = stateHistoryILP.getTable();
		stateHistoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		stateHistoryTable.addMouseListener(new TablePopupHandler(stateHistoryCollection, stateHistoryPopupMenu));

		return stateHistoryTable;
    }

    /**
     * Returns the JTable used to display the list of loaded state histories
     * @return the JTable used to display the list of loaded state histories
     */
    public JTable getTable()
    {
        return resultList;
    }

    /**
     * Returns the load state history button
     * @return the load state history button
     */
    public JButton getLoadStateHistoryButton()
    {
        return loadStateHistoryButton;
    }

    /**
     * Returns the show state history button
     * @return the show state history button
     */
    public JButton getShowStateHistoryButton()
    {
        return showStateHistoryButton;
    }

    /**
     * Returns the remove state history button
     * @return the remove state history button
     */
    public JButton getRemoveStateHistoryButton()
    {
        return removeStateHistoryButton;
    }

    /**
     * Returns the save state history button
     * @return the save state history button
     */
    public JButton getSaveStateHistoryButton()
    {
        return saveStateHistoryButton;
    }

	/**
	 * Configures the appropriate table colun width for the given expected type of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = stateHistoryILP.getTable();
		String dateTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, true, blackCP, "Description", dateTimeStr, dateTimeStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}
}