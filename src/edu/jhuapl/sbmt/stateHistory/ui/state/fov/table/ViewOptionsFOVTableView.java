package edu.jhuapl.sbmt.stateHistory.ui.state.fov.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import edu.jhuapl.saavtk.color.gui.ColorProviderCellEditor;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellRenderer;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.panel.JComboBoxWithItemState;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.ItemManagerUtil;

public class ViewOptionsFOVTableView extends JPanel
{

	/**
	 * JTable to display loaded state histories
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
	 * The state history item list panel, used to help handle interactions with
	 * the table
	 */
	private ItemListPanel<String> viewOptionsFOVILP;

	/**
	 * The state history table handler, used to help populate the table
	 */
	private ItemHandler<String> viewOptionsFOVTableHandler;

	private StateHistoryRendererManager rendererManager;

	private JComboBoxWithItemState<String> plateColorings = new JComboBoxWithItemState<String>();

	/**
	 * @wbp.parser.constructor
	 */
	/**
	 * @param stateHistoryCollection
	 */
	public ViewOptionsFOVTableView(StateHistoryRendererManager rendererManager, JComboBoxWithItemState<String> plateColorings)
	{
		this.stateHistoryCollection = rendererManager.getHistoryCollection();
		this.rendererManager = rendererManager;
		this.plateColorings = plateColorings;

		init();
	}

	/**
	 * Initializes UI elements
	 */
	protected void init()
	{
		resultList = buildTable();
	}

	/**
	 * Sets up the UI elements
	 */
	public void setup()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(150, 200));
		scrollPane.setPreferredSize(new Dimension(150, 200));
		add(scrollPane);
		resultList.setPreferredSize(new Dimension(400, 200));
		scrollPane.setViewportView(resultList);
	}

	/**
	 * Builds the JTable.
	 *
	 * @return
	 */
	private JTable buildTable()
	{
		ActionListener listener = e ->
		{
			Object source = e.getSource();

			if (source == selectAllB)
				ItemManagerUtil.selectAll(rendererManager);
			else if (source == selectNoneB)
				ItemManagerUtil.selectNone(rendererManager);
			else if (source == selectInvertB)
			{
				ItemManagerUtil.selectInvert(rendererManager);
			}
		};

		// Popup menu
//		StateHistoryPopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil
//				.formStateHistoryFileSpecPopupMenu(rendererManager, this);

		// Table header
		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

//		JPanel buttonPanel = new JPanel();
//		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
//
//		buttonPanel.add(Box.createHorizontalGlue());
//		buttonPanel.add(selectInvertB, "w 24!,h 24!");
//		buttonPanel.add(selectNoneB, "w 24!,h 24!");
//		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
//		add(buttonPanel);

		// Table Content
		QueryComposer<ViewOptionsFOVColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(ViewOptionsFOVColumnLookup.Frustum, Boolean.class, "Frustum", null);
		tmpComposer.addAttribute(ViewOptionsFOVColumnLookup.Border, Boolean.class, "Border", null);
		tmpComposer.addAttribute(ViewOptionsFOVColumnLookup.Footprint, Boolean.class, "Footprint", null);
		tmpComposer.addAttribute(ViewOptionsFOVColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(ViewOptionsFOVColumnLookup.Name, String.class, "Name", null);
		tmpComposer.addAttribute(ViewOptionsFOVColumnLookup.SetAsCurrent, Boolean.class, "Use for Traj Color", null);
		tmpComposer.addAttribute(ViewOptionsFOVColumnLookup.FPPlateColoring, String.class, "Footprint Plate Coloring", null);

		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);
		tmpComposer.setEditor(ViewOptionsFOVColumnLookup.Frustum, new BooleanCellEditor());
		tmpComposer.setRenderer(ViewOptionsFOVColumnLookup.Frustum, new BooleanCellRenderer());
		tmpComposer.setEditor(ViewOptionsFOVColumnLookup.Border, new BooleanCellEditor());
		tmpComposer.setRenderer(ViewOptionsFOVColumnLookup.Border, new BooleanCellRenderer());
		tmpComposer.setEditor(ViewOptionsFOVColumnLookup.Footprint, new BooleanCellEditor());
		tmpComposer.setRenderer(ViewOptionsFOVColumnLookup.Footprint, new BooleanCellRenderer());
		tmpComposer.setEditor(ViewOptionsFOVColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
		tmpComposer.setRenderer(ViewOptionsFOVColumnLookup.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setEditor(ViewOptionsFOVColumnLookup.Name, new DefaultCellEditor(new JTextField()));
		tmpComposer.setEditor(ViewOptionsFOVColumnLookup.SetAsCurrent, new BooleanCellEditor());
		tmpComposer.setRenderer(ViewOptionsFOVColumnLookup.SetAsCurrent, new BooleanCellRenderer());

//		JComboBox<String> fpColoringCombo = new JComboBox<String>(new Double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0});
		plateColorings.setSelectedIndex(0);
		tmpComposer.setEditor(ViewOptionsFOVColumnLookup.FPPlateColoring, new DefaultCellEditor(plateColorings));

		viewOptionsFOVTableHandler = new ViewOptionsFOVItemHandler(rendererManager, tmpComposer, plateColorings);
		ItemProcessor<String> tmpIP = stateHistoryCollection.getAllFOVProcessor();
		viewOptionsFOVILP = new ItemListPanel<>(viewOptionsFOVTableHandler, tmpIP, true);
		viewOptionsFOVILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable stateHistoryTable = viewOptionsFOVILP.getTable();
		stateHistoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		stateHistoryTable.addMouseListener(new TablePopupHandler(rendererManager, stateHistoryPopupMenu));

		return stateHistoryTable;
	}

	/**
	 * Returns the JTable used to display the list of loaded state histories
	 *
	 * @return the JTable used to display the list of loaded state histories
	 */
	public JTable getTable()
	{
		return resultList;
	}


	/**
	 * Configures the appropriate table colun width for the given expected type
	 * of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = viewOptionsFOVILP.getTable();
		int minW = 50;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr =
		{ true, true, true, blackCP, "Segment000000000000000", true, "Footprint Plate ColoringFootprint Plate Coloring"};
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0,
					aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
			if (aCol == 5) tmpTable.getColumnModel().getColumn(aCol).setMinWidth(110);
		}
	}

	/**
	 * @param plateColorings the plateColorings to set
	 */
	public void setPlateColorings(JComboBoxWithItemState<String> plateColorings)
	{
		this.plateColorings = plateColorings;
	}
}