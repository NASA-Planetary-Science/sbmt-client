package edu.jhuapl.sbmt.stateHistory.ui.state.intervalSelection.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.popup.StateHistoryGuiUtil;

import glum.gui.GuiUtil;
import glum.gui.action.PopupMenu;
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
	private JButton hideStateHistoryButton;

	/**
	 * JButton to show state history in renderer
	 */
	private JButton showStateHistoryButton;

	/**
	 * JButton to save state history to file
	 */
	private JButton saveStateHistoryButton;

	/**
	 * JTable to display loaded state histories
	 */
	protected JTable table;

	/**
	 * JButtons for selection in the table
	 */
	private JButton selectAllB, selectInvertB, selectNoneB, deleteStateHistoryButton, itemAddB, itemEditB;

	/**
	 * The state history item list panel, used to help handle interactions with
	 * the table
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
	public StateHistoryTableView(StateHistoryRendererManager rendererManager)
	{
		table = buildTable(rendererManager);
	}

	/**
	 * Sets up the UI elements
	 */
	public void setup()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(null, "Loaded Trajectories", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(150, 150));
		add(scrollPane);

		scrollPane.setViewportView(table);
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
		};

		// Popup menu
		PopupMenu stateHistoryPopupMenu = StateHistoryGuiUtil
				.formStateHistoryFileSpecPopupMenu(rendererManager, this);

		loadStateHistoryButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.directoryIcon"));
		loadStateHistoryButton.setToolTipText(ToolTipUtil.getItemLoad());

		saveStateHistoryButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.hardDriveIcon"));
		saveStateHistoryButton.setToolTipText(ToolTipUtil.getItemSave());
		saveStateHistoryButton.setEnabled(false);

		showStateHistoryButton = GuiUtil.formButton(listener, IconUtil.getItemShow());
		showStateHistoryButton.setToolTipText(ToolTipUtil.getItemShow());
		showStateHistoryButton.setEnabled(false);

		hideStateHistoryButton = GuiUtil.formButton(listener, IconUtil.getItemHide());
		hideStateHistoryButton.setToolTipText(ToolTipUtil.getItemHide());
		hideStateHistoryButton.setEnabled(false);

		itemAddB = GuiUtil.formButton(listener, IconUtil.getItemAdd());
		itemAddB.setToolTipText(ToolTipUtil.getItemAdd());

		deleteStateHistoryButton = GuiUtil.formButton(listener, IconUtil.getItemDel());
		deleteStateHistoryButton.setToolTipText(ToolTipUtil.getItemDel());
		deleteStateHistoryButton.setEnabled(false);

		itemEditB = GuiUtil.formButton(listener, IconUtil.getItemEdit());
		itemEditB.setToolTipText(ToolTipUtil.getItemEdit());
		itemEditB.setEnabled(false);

		// Table header
		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(loadStateHistoryButton);
		buttonPanel.add(saveStateHistoryButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(showStateHistoryButton);
		buttonPanel.add(hideStateHistoryButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(itemAddB, "w 24!,h 24!");
		buttonPanel.add(deleteStateHistoryButton, "w 24!,h 24!");
		buttonPanel.add(itemEditB, "gapright 24,w 24!,h 24!");
		Component horizontalStrut = Box.createHorizontalStrut(24);
		horizontalStrut.setMaximumSize(new Dimension(24, 5));
		buttonPanel.add(horizontalStrut);
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<StateHistoryColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(StateHistoryColumnLookup.Map, Boolean.class, "Map", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.Color, Color.class, "Color", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.Name, String.class, "Name", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.Description, String.class, "Description", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.StartTime, String.class, "Start Time (UTC)", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.EndTime, String.class, "End Time (UTC)", null);
		tmpComposer.addAttribute(StateHistoryColumnLookup.Source, String.class, "Source", null);

		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);
		tmpComposer.setEditor(StateHistoryColumnLookup.Map, new BooleanCellEditor());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Map, new BooleanCellRenderer());
		tmpComposer.setEditor(StateHistoryColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(StateHistoryColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(StateHistoryColumnLookup.Color, new ColorProviderCellEditor());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Color, new ColorProviderCellRenderer(false));
		tmpComposer.setEditor(StateHistoryColumnLookup.Name, new DefaultCellEditor(new JTextField()));
		tmpComposer.setRenderer(StateHistoryColumnLookup.Description, tmpTimeRenderer);
		tmpComposer.setEditor(StateHistoryColumnLookup.Description, new DefaultCellEditor(new JTextField()));
		tmpComposer.setRenderer(StateHistoryColumnLookup.StartTime, tmpTimeRenderer);
		tmpComposer.setRenderer(StateHistoryColumnLookup.EndTime, tmpTimeRenderer);

		stateHistoryTableHandler = new StateHistoryItemHandler(rendererManager, tmpComposer);
		ItemProcessor<StateHistory> tmpIP = rendererManager;
		stateHistoryILP = new ItemListPanel<>(stateHistoryTableHandler, tmpIP, true);
		stateHistoryILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable stateHistoryTable = stateHistoryILP.getTable();
		stateHistoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		stateHistoryTable.addMouseListener(new TablePopupHandler(rendererManager, stateHistoryPopupMenu));

		return stateHistoryTable;
	}

	/**
	 * Returns the JTable used to display the list of loaded state histories
	 *
	 * @return the JTable used to display the list of loaded state histories
	 */
	public JTable getTable()
	{
		return table;
	}

	/**
	 * Returns the load state history button
	 *
	 * @return the load state history button
	 */
	public JButton getAddStateHistoryButton()
	{
		return itemAddB;
	}

	/**
	 * Returns the load state history button
	 *
	 * @return the load state history button
	 */
	public JButton getEditStateHistoryButton()
	{
		return itemEditB;
	}

	/**
	 * Returns the load state history button
	 *
	 * @return the load state history button
	 */
	public JButton getLoadStateHistoryButton()
	{
		return loadStateHistoryButton;
	}

	/**
	 * Returns the show state history button
	 *
	 * @return the show state history button
	 */
	public JButton getShowStateHistoryButton()
	{
		return showStateHistoryButton;
	}

	/**
	 * Returns the remove state history button
	 *
	 * @return the remove state history button
	 */
	public JButton getHideStateHistoryButton()
	{
		return hideStateHistoryButton;
	}

	/**
	 * Returns the save state history button
	 *
	 * @return the save state history button
	 */
	public JButton getSaveStateHistoryButton()
	{
		return saveStateHistoryButton;
	}

	/**
	 * @return the deleteStateHistoryButton
	 */
	public JButton getDeleteStateHistoryButton()
	{
		return deleteStateHistoryButton;
	}

	/**
	 * Configures the appropriate table colun width for the given expected type
	 * of data
	 */
	private void configureColumnWidths()
	{
		JTable tmpTable = stateHistoryILP.getTable();
		String dateTimeStr = "9999-88-88T00:00:00.000";
		int minW = 40;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr =
		{ true, true, blackCP, "Segment000", "Description", dateTimeStr, dateTimeStr, "TypePlusSourceName" };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0,
					aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}
}