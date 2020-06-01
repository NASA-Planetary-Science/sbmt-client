package edu.jhuapl.sbmt.stateHistory.ui.imagers;

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
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImage;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemManagerUtil;

public class PlannedImageTableView extends JPanel
{
	/**
	 * JButton to load planned image from file
	 */
	private JButton loadPlannedImageButton;

    /**
     * JButton to remove planned image from table
     */
    private JButton removePlannedImageButton;

    /**
     * JButton to show planned image in renderer
     */
    private JButton showPlannedImageButton;

    /**
     * JButton to save planned image to file
     */
    private JButton savePlannedImageButton;

    /**
     *	JTable to display loaded state histories
     */
    protected JTable resultList;

    /**
     * JButtons for selection in the table
     */
    private JButton selectAllB, selectInvertB, selectNoneB;

    /**
     * The collection of loaded planned image objects
     */
    private PlannedImageCollection plannedImageCollection;

    /**
     *	The planned image item list panel, used to help handle interactions with the table
     */
    private ItemListPanel<PlannedImage> plannedImageILP;

    /**
     * The planned image table handler, used to help populate the table
     */
    private ItemHandler<PlannedImage> plannedImageTableHandler;

	public PlannedImageTableView(PlannedImageCollection plannedImageCollection)
	{
		this.plannedImageCollection = plannedImageCollection;
		init();
	}

    /**
     * Initializes UI elements
     */
    protected void init()
    {
        resultList = buildTable();
        removePlannedImageButton = new JButton("Hide Planned Image");
        showPlannedImageButton = new JButton("Show Planned Image");
        removePlannedImageButton.setEnabled(false);
        showPlannedImageButton.setEnabled(false);
        loadPlannedImageButton = new JButton("Load...");
        savePlannedImageButton = new JButton("Save...");
        savePlannedImageButton.setEnabled(false);
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

        scrollPane.setViewportView(resultList);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
        panel_1.add(showPlannedImageButton);
        panel_1.add(removePlannedImageButton);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
        panel_2.add(loadPlannedImageButton);
        panel_2.add(savePlannedImageButton);
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
				ItemManagerUtil.selectAll(plannedImageCollection);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(plannedImageCollection);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(plannedImageCollection);
			}
		};

		//Popup menu
		//TODO restore this
//		StateHistoryPopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil.formStateHistoryFileSpecPopupMenu(plannedImageCollection, this);

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
		QueryComposer<PlannedImageColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(PlannedImageColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(PlannedImageColumnLookup.Frus, Boolean.class, "Frus", null);
		tmpComposer.addAttribute(PlannedImageColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(PlannedImageColumnLookup.Instrument, String.class, "Instrument", null);
		tmpComposer.addAttribute(PlannedImageColumnLookup.ImageTime, String.class, "Image Time", null);


		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);

		tmpComposer.setEditor(PlannedImageColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedImageColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(PlannedImageColumnLookup.Frus, new BooleanCellEditor());
		tmpComposer.setRenderer(PlannedImageColumnLookup.Frus, new BooleanCellRenderer());
		tmpComposer.setEditor(PlannedImageColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
		tmpComposer.setRenderer(PlannedImageColumnLookup.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setRenderer(PlannedImageColumnLookup.ImageTime, tmpTimeRenderer);

		plannedImageTableHandler = new PlannedImageItemHandler(plannedImageCollection, tmpComposer);
		ItemProcessor<PlannedImage> tmpIP = plannedImageCollection;
		plannedImageILP = new ItemListPanel<>(plannedImageTableHandler, tmpIP, true);
		plannedImageILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable plannedImageTable = plannedImageILP.getTable();
		plannedImageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//TODO: Fix the popup menu
		plannedImageTable.addMouseListener(new TablePopupHandler(plannedImageCollection, null));

		return plannedImageTable;
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
     * Returns the load planned image button
     * @return the load planned image button
     */
    public JButton getLoadPlannedImageButton()
    {
        return loadPlannedImageButton;
    }

    /**
     * Returns the show planned image button
     * @return the show planned image button
     */
    public JButton getShowPlannedImageButton()
    {
        return showPlannedImageButton;
    }

    /**
     * Returns the remove planned image button
     * @return the remove planned image button
     */
    public JButton getRemovePlannedImageButton()
    {
        return removePlannedImageButton;
    }

    /**
     * Returns the save planned image button
     * @return the save planned image button
     */
    public JButton getSavePlannedImageButton()
    {
        return savePlannedImageButton;
    }

	/**
	 * Configures the appropriate table colun width for the given expected type of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = plannedImageILP.getTable();
		String dateTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, true, blackCP, dateTimeStr, dateTimeStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}

}
