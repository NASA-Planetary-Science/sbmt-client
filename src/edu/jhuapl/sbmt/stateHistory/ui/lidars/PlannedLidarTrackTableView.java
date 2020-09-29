package edu.jhuapl.sbmt.stateHistory.ui.lidars;

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

import edu.jhuapl.saavtk.color.gui.ColorProviderCellEditor;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellRenderer;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrack;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
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
    private JButton removePlannedLidarTrackButton;

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
    protected JTable resultList;

    /**
     * JButtons for selection in the table
     */
    private JButton selectAllB, selectInvertB, selectNoneB;

    /**
     * The collection of loaded planned LidarTrack objects
     */
    private PlannedLidarTrackCollection plannedLidarTrackCollection;

    /**
     *	The planned LidarTrack item list panel, used to help handle interactions with the table
     */
    private ItemListPanel<PlannedLidarTrack> plannedLidarTrackILP;

    /**
     * The planned LidarTrack table handler, used to help populate the table
     */
    private ItemHandler<PlannedLidarTrack> plannedLidarTrackTableHandler;

	public PlannedLidarTrackTableView(PlannedLidarTrackCollection plannedLidarTrackCollection)
	{
		this.plannedLidarTrackCollection = plannedLidarTrackCollection;
		init();
	}

    /**
     * Initializes UI elements
     */
    protected void init()
    {
        resultList = buildTable();
        removePlannedLidarTrackButton = new JButton("Hide Planned Lidar Track");
        showPlannedLidarTrackButton = new JButton("Show Planned Lidar Track");
        removePlannedLidarTrackButton.setEnabled(false);
        showPlannedLidarTrackButton.setEnabled(false);
        loadPlannedLidarTrackButton = new JButton("Load...");
        savePlannedLidarTrackButton = new JButton("Save...");
        savePlannedLidarTrackButton.setEnabled(false);
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

        scrollPane.setViewportView(resultList);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
        panel_1.add(showPlannedLidarTrackButton);
        panel_1.add(removePlannedLidarTrackButton);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
        panel_2.add(loadPlannedLidarTrackButton);
        panel_2.add(savePlannedLidarTrackButton);
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
		plannedLidarTrackTable.addMouseListener(new TablePopupHandler(plannedLidarTrackCollection, null));

		return plannedLidarTrackTable;
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
     * Returns the load planned LidarTrack button
     * @return the load planned LidarTrack button
     */
    public JButton getLoadPlannedLidarTrackButton()
    {
        return loadPlannedLidarTrackButton;
    }

    /**
     * Returns the show planned LidarTrack button
     * @return the show planned LidarTrack button
     */
    public JButton getShowPlannedLidarTrackButton()
    {
        return showPlannedLidarTrackButton;
    }

    /**
     * Returns the remove planned LidarTrack button
     * @return the remove planned LidarTrack button
     */
    public JButton getRemovePlannedLidarTrackButton()
    {
        return removePlannedLidarTrackButton;
    }

    /**
     * Returns the save planned LidarTrack button
     * @return the save planned LidarTrack button
     */
    public JButton getSavePlannedLidarTrackButton()
    {
        return savePlannedLidarTrackButton;
    }

	/**
	 * Configures the appropriate table colun width for the given expected type of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = plannedLidarTrackILP.getTable();
		String dateTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, blackCP, "", dateTimeStr, dateTimeStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}

}
