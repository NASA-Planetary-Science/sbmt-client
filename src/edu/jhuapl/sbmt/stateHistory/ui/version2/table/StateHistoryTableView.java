package edu.jhuapl.sbmt.stateHistory.ui.version2.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import edu.jhuapl.saavtk.gui.table.TablePopupHandler;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.gui.lidar.color.ColorProvider;
import edu.jhuapl.sbmt.gui.lidar.color.ConstColorProvider;
import edu.jhuapl.sbmt.gui.table.ColorProviderCellEditor;
import edu.jhuapl.sbmt.gui.table.ColorProviderCellRenderer;
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
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
import glum.item.ItemManagerUtil;

public class StateHistoryTableView extends JPanel
{
	private JButton loadStateHistoryButton;
//    private JButton nextButton;
//    private JButton prevButton;
//    private JButton removeBoundariesButton;
    private JButton removeStateHistoryButton;
    private JButton showStateHistoryButton;
    private JButton saveStateHistoryButton;
//    private JButton saveSelectedSpectraListButton;
//    private SpectrumPopupMenu spectrumPopupMenu;
    protected JTable resultList;
    private JLabel resultsLabel;

    //for table
    private JLabel titleL;
    private JButton selectAllB, selectInvertB, selectNoneB;
    private StateHistoryCollection stateHistoryCollection;
    private ItemListPanel<StateHistory> stateHistoryILP;
    private ItemHandler<StateHistory> stateHistoryTableHandler;

    /**
     * @wbp.parser.constructor
     */
    public StateHistoryTableView(StateHistoryCollection stateHistoryCollection/*, SpectrumPopupMenu spectrumPopupMenu*/)
    {
//        this.spectrumPopupMenu = spectrumPopupMenu;
        this.stateHistoryCollection = stateHistoryCollection;
        init();
    }

    protected void init()
    {
        resultsLabel = new JLabel("0 Results");
        resultList = buildTable();
//        prevButton = new JButton("Prev");
//        nextButton = new JButton("Next");
        removeStateHistoryButton = new JButton("Hide State History");
        showStateHistoryButton = new JButton("Show State History");
        removeStateHistoryButton.setEnabled(false);
        showStateHistoryButton.setEnabled(false);
        loadStateHistoryButton = new JButton("Load...");
        saveStateHistoryButton = new JButton("Save...");
        saveStateHistoryButton.setEnabled(false);
    }

    public void setup()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder(null, "Available Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        panel_4.add(resultsLabel);

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

    private JTable buildTable()
    {
    	ActionListener listener = e -> {
			Object source = e.getSource();

			List<StateHistory> tmpL = stateHistoryCollection.getSelectedItems().asList();
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
		titleL = new JLabel("State History: ---");
		buttonPanel.add(titleL, "growx,span,split");
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
//		tmpComposer.addAttribute(StateHistoryColumnLookup.Line, Double.class, "Line", null);
//		tmpComposer.addAttribute(StateHistoryColumnLookup.Name, String.class, "Name", null);
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
		tmpComposer.setEditor(StateHistoryColumnLookup.Color, new ColorProviderCellEditor());
		tmpComposer.setRenderer(StateHistoryColumnLookup.Color, new ColorProviderCellRenderer(false));
//		tmpComposer.setRenderer(StateHistoryColumnLookup.Line, new NumberRenderer("##", "--"));
//
//		tmpComposer.setRenderer(StateHistoryColumnLookup.Name, tmpTimeRenderer);
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
//		spectrumTable.addMouseListener(new SpectrumTablePopupListener<>(stateHistoryCollection, spectrumPopupMenu, spectrumTable));

//		spectrumCollection.addListener(new ItemEventListener()
//		{
//
//			@Override
//			public void handleItemEvent(Object aSource, ItemEventType aEventType)
//			{
//				if (aEventType == ItemEventType.ItemsMutated)
//				{
//					spectrumTableHandler = new SpectrumItemHandler<S>(spectrumCollection, boundaryCollection, tmpComposer);
//					ItemProcessor<S> tmpIP = spectrumCollection;
////					spectrumILP = new ItemListPanel<>(spectrumTableHandler, tmpIP, true);
//				}
//
//			}
//		});

		return stateHistoryTable;
    }

    public JTable getTable()
    {
        return resultList;
    }

    public JLabel getResultsLabel()
    {
        return resultsLabel;
    }

    public JButton getLoadStateHistoryButton()
    {
        return loadStateHistoryButton;
    }

    public JButton getShowStateHistoryButton()
    {
        return showStateHistoryButton;
    }

    public JButton getRemoveStateHistoryButton()
    {
        return removeStateHistoryButton;
    }

    public JButton getSaveStateHistoryButton()
    {
        return saveStateHistoryButton;
    }

    public void setResultsLabel(JLabel resultsLabel)
    {
        this.resultsLabel = resultsLabel;
    }

//    public SpectrumPopupMenu getSpectrumPopupMenu()
//    {
//        return spectrumPopupMenu;
//    }
//
//    public void setSpectrumPopupMenu(SpectrumPopupMenu spectrumPopupMenu)
//    {
//        this.spectrumPopupMenu = spectrumPopupMenu;
//    }

	public ItemHandler<StateHistory> getStateHistoryTableHandler()
	{
		return stateHistoryTableHandler;
	}

	private void configureColumnWidths()
	{
//		int maxPts = 99;
//		String sourceStr = "Data Source";
//		for (BasicSpectrum spec : spectrumCollection.getAllItems())
//		{
//			maxPts = Math.max(maxPts, spec.getNumberOfPoints());
//			String tmpStr = SpectrumItemHandler.getSourceFileString(aTrack);
//			if (tmpStr.length() > sourceStr.length())
//				sourceStr = tmpStr;
//		}

		JTable tmpTable = stateHistoryILP.getTable();
		String trackStr = "" + tmpTable.getRowCount();
//		String pointStr = "" + maxPts;
		String dateTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, true, blackCP, /*0.0, "Name",*/ "Description", dateTimeStr, dateTimeStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}


}
