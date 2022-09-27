package edu.jhuapl.sbmt.stateHistory.ui.state.displayItems.table;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.jhuapl.saavtk.color.gui.ColorProviderCellEditor;
import edu.jhuapl.saavtk.color.gui.ColorProviderCellRenderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.DisplayableItem;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.ItemManagerUtil;

public class DisplayOptionsTableView extends JPanel
{
	/**
	 * JTable to display loaded state histories
	 */
	protected JTable resultList;

	/**
	 * JButtons for selection in the table
	 */
	private JButton selectAllB, selectInvertB, selectNoneB, showDisplayItemButton, hideDisplayItemButton;

	private JButton fontButton;

	/**
	 * The state history item list panel, used to help handle interactions with
	 * the table
	 */
	private ItemListPanel<DisplayableItem> displayOptionsILP;

	/**
	 * The state history table handler, used to help populate the table
	 */
	private ItemHandler<DisplayableItem> displayOptionsTableHandler;

	/**
	 * @param stateHistoryCollection
	 */
	public DisplayOptionsTableView(StateHistoryRendererManager rendererManager)
	{
		init(rendererManager);
	}

	/**
	 * Initializes UI elements
	 */
	protected void init(StateHistoryRendererManager rendererManager)
	{
		resultList = buildTable(rendererManager);
	}

	/**
	 * Sets up the UI elements
	 */
	public void setup()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//		JPanel panel_4 = new JPanel();
//		add(panel_4);
//		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
//
//		Component horizontalGlue = Box.createHorizontalGlue();
//		panel_4.add(horizontalGlue);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new java.awt.Dimension(150, 200));
		add(scrollPane);

		scrollPane.setViewportView(resultList);
	}

	/**
	 * Builds the JTable.
	 *
	 * @return
	 */
	private JTable buildTable(StateHistoryRendererManager rendererManager)
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
			else if (source == showDisplayItemButton)
			{
				rendererManager.getDisplayableItems().stream().filter(item -> item.isVisible() == false).forEach(item -> { item.setVisible(true); });
			}
			else if (source == hideDisplayItemButton)
			{
				rendererManager.getDisplayableItems().stream().filter(item -> item.isVisible() == true).forEach(item -> { item.setVisible(false); });
			}
		};

		// Popup menu
//		StateHistoryPopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil
//				.formStateHistoryFileSpecPopupMenu(rendererManager, this);

		showDisplayItemButton = GuiUtil.formButton(listener, IconUtil.getItemShow());
		showDisplayItemButton.setToolTipText(ToolTipUtil.getItemShow());
		showDisplayItemButton.setEnabled(false);

		hideDisplayItemButton = GuiUtil.formButton(listener, IconUtil.getItemHide());
		hideDisplayItemButton.setToolTipText(ToolTipUtil.getItemHide());
		hideDisplayItemButton.setEnabled(false);


		// Table header
		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		fontButton = GuiUtil.formButton(listener, IconUtil.getFont());
		fontButton.setToolTipText(ToolTipUtil.getFont());

		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(fontButton);
		add(buttonPanel);

		// Table Content
		QueryComposer<DisplayOptionsColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Label, Boolean.class, "Label", null);
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.LabelString, String.class, "Label Text", null);
		tmpComposer.addAttribute(DisplayOptionsColumnLookup.Size, Double.class, "Marker Size", null);

		tmpComposer.getItem(DisplayOptionsColumnLookup.LabelString).defaultSize *= 2;

		tmpComposer.setEditor(DisplayOptionsColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(DisplayOptionsColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(DisplayOptionsColumnLookup.Label, new BooleanCellEditor());
		tmpComposer.setRenderer(DisplayOptionsColumnLookup.Label, new BooleanCellRenderer());
		JComboBox<Double> sizeCombo = new JComboBox<Double>(new Double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0});
		sizeCombo.setSelectedIndex(5);
		tmpComposer.setEditor(DisplayOptionsColumnLookup.Size, new DefaultCellEditor(sizeCombo));

		tmpComposer.setEditor(DisplayOptionsColumnLookup.Color, new ColorProviderCellEditor<StateHistory>());
		tmpComposer.setRenderer(DisplayOptionsColumnLookup.Color, new ColorProviderCellRenderer(false));

		displayOptionsTableHandler = new DisplayOptionsItemHandler(rendererManager, tmpComposer);
		ItemProcessor<DisplayableItem> tmpIP = rendererManager.getDisplayItemsProcessor();
		displayOptionsILP = new ItemListPanel<>(displayOptionsTableHandler, tmpIP, true);
		displayOptionsILP.setSortingEnabled(true);
//		configureColumnWidths();
		JTable stateHistoryTable = displayOptionsILP.getTable();
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
	 * @return the showDisplayItemButton
	 */
	public JButton getShowDisplayItemButton()
	{
		return showDisplayItemButton;
	}

	/**
	 * @return the hideDisplayItemButton
	 */
	public JButton getHideDisplayItemButton()
	{
		return hideDisplayItemButton;
	}

	/**
	 * @return the hideDisplayItemButton
	 */
	public JButton getFontButton()
	{
		return fontButton;
	}

//	/**
//	 * Configures the appropriate table colun width for the given expected type
//	 * of data
//	 */
//	private void configureColumnWidths()
//	{
//		JTable tmpTable = displayOptionsILP.getTable();
//		int minW = 30;
//
//		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
//		Object[] nomArr =
//		{ true,  true, blackCP, /*"Segment000000000000000",*/ "Segment000000000000000", 100000};
//		for (int aCol = 0; aCol < nomArr.length; aCol++)
//		{
//			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
//			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0,
//					aCol);
//			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
//			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
//		}
//	}
}