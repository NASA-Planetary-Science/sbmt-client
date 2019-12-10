package edu.jhuapl.sbmt.spectrum.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.gui.lidar.color.ColorProvider;
import edu.jhuapl.sbmt.gui.lidar.color.ConstColorProvider;
import edu.jhuapl.sbmt.gui.table.EphemerisTimeRenderer;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumPopupMenu;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumTablePopupListener;

import glum.gui.GuiUtil;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.ItemManagerUtil;

/**
 * Panel that holds the Spectrum Results table, and associated controls
 * @author steelrj1
 *
 */
public class SpectrumResultsTableView<S extends BasicSpectrum> extends JPanel
{
    private JButton loadSpectraListButton;
    private JPanel monochromePanel;
    private JButton nextButton;
    private JComboBox<Integer> numberOfBoundariesComboBox;
    private JButton prevButton;
    private JButton removeBoundariesButton;
    private JButton removeSpectraButton;
    private JButton showBoundariesButton;
    private JButton showSpectraButton;
    private JButton saveSpectraListButton;
    private JButton saveSelectedSpectraListButton;
    private SpectrumPopupMenu spectrumPopupMenu;
    protected JTable resultList;
    private JLabel resultsLabel;
    private JLabel lblNumberBoundaries;

    //for table
    private JLabel titleL;
    private JButton selectAllB, selectInvertB, selectNoneB;
    private SpectraCollection<S> spectrumCollection;
    private SpectrumBoundaryCollection<S> boundaryCollection;
    private ItemListPanel<S> spectrumILP;
    private ItemHandler<S> spectrumTableHandler;

    /**
     * @wbp.parser.constructor
     */
    public SpectrumResultsTableView(SpectraCollection<S> spectrumCollection, SpectrumBoundaryCollection<S> boundaryCollection, SpectrumPopupMenu spectrumPopupMenu)
    {
        this.spectrumPopupMenu = spectrumPopupMenu;
        this.spectrumCollection = spectrumCollection;
        this.boundaryCollection = boundaryCollection;
        init();
    }

    protected void init()
    {
        resultsLabel = new JLabel("0 Results");
        resultList = buildTable();
        lblNumberBoundaries = new JLabel("Number Boundaries:");
        numberOfBoundariesComboBox = new JComboBox<Integer>();
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        removeSpectraButton = new JButton("Remove Spectra");
        removeBoundariesButton = new JButton("Remove Boundaries");
        showSpectraButton = new JButton("Show Spectra");
        showBoundariesButton = new JButton("Show Boundaries");
        loadSpectraListButton = new JButton("Load...");
        saveSpectraListButton = new JButton("Save...");
        saveSelectedSpectraListButton = new JButton("Save Selected...");
    }

    public void setup()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder(null, "Available Histories", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(lblNumberBoundaries);

        panel.add(numberOfBoundariesComboBox);

        panel.add(prevButton);

        panel.add(nextButton);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        panel_1.add(showSpectraButton);

        panel_1.add(showBoundariesButton);

        panel_1.add(removeSpectraButton);

        panel_1.add(removeBoundariesButton);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        panel_2.add(loadSpectraListButton);

        panel_2.add(saveSpectraListButton);

        panel_2.add(saveSelectedSpectraListButton);


    }

    private JTable buildTable()
    {
    	ActionListener listener = new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();

				List<S> tmpL = spectrumCollection.getSelectedItems().asList();
				if (source == selectAllB)
					ItemManagerUtil.selectAll(spectrumCollection);
				else if (source == selectNoneB)
					ItemManagerUtil.selectNone(spectrumCollection);
				else if (source == selectInvertB)
				{
					ItemManagerUtil.selectInvert(spectrumCollection);
				}
			}
		};

    	// Table header
		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		titleL = new JLabel("Spectra: ---");
		buttonPanel.add(titleL, "growx,span,split");
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<SpectrumColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(SpectrumColumnLookup.Map, Boolean.class, "Map", null);
		tmpComposer.addAttribute(SpectrumColumnLookup.Show, Boolean.class, "Show", null);
		tmpComposer.addAttribute(SpectrumColumnLookup.Frus, Boolean.class, "Frus", null);
		tmpComposer.addAttribute(SpectrumColumnLookup.Bndr, Boolean.class, "Bndr", null);
		tmpComposer.addAttribute(SpectrumColumnLookup.Id, Integer.class, "Id", null);
		tmpComposer.addAttribute(SpectrumColumnLookup.Filename, String.class, "Filename", null);
		tmpComposer.addAttribute(SpectrumColumnLookup.Date, Double.class, "Date", null);

		EphemerisTimeRenderer tmpTimeRenderer = new EphemerisTimeRenderer(false);
		tmpComposer.setEditor(SpectrumColumnLookup.Map, new BooleanCellEditor());
		tmpComposer.setRenderer(SpectrumColumnLookup.Map, new BooleanCellRenderer());
		tmpComposer.setEditor(SpectrumColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(SpectrumColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(SpectrumColumnLookup.Show, new BooleanCellEditor());
		tmpComposer.setRenderer(SpectrumColumnLookup.Show, new BooleanCellRenderer());
		tmpComposer.setEditor(SpectrumColumnLookup.Frus, new BooleanCellEditor());
		tmpComposer.setRenderer(SpectrumColumnLookup.Frus, new BooleanCellRenderer());
		tmpComposer.setEditor(SpectrumColumnLookup.Bndr, new BooleanCellEditor());
		tmpComposer.setRenderer(SpectrumColumnLookup.Bndr, new BooleanCellRenderer());

//    	    			tmpComposer.setRenderer(SpectrumColumnLookup.Color, new ColorProviderCellRenderer(false));
//    	    			tmpComposer.setRenderer(SpectrumColumnLookup.Name, new PrePendRenderer("Trk "));
//    	    			tmpComposer.setRenderer(SpectrumColumnLookup.NumPoints, new NumberRenderer("###,###,###", "---"));
//    	    			tmpComposer.setRenderer(SpectrumColumnLookup.BegTime, tmpTimeRenderer);
//    	    			tmpComposer.setRenderer(SpectrumColumnLookup.Date, tmpTimeRenderer);

		spectrumTableHandler = new SpectrumItemHandler<S>(spectrumCollection, boundaryCollection, tmpComposer);
		ItemProcessor<S> tmpIP = spectrumCollection;
		spectrumILP = new ItemListPanel<>(spectrumTableHandler, tmpIP, true);
		spectrumILP.setSortingEnabled(true);
		configureColumnWidths();
		JTable spectrumTable = spectrumILP.getTable();
		spectrumTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		spectrumTable.addMouseListener(new SpectrumTablePopupListener<>(spectrumCollection, boundaryCollection, spectrumPopupMenu, spectrumTable));

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

		return spectrumTable;
    }

    public JTable getResultList()
    {
        return resultList;
    }

    public JLabel getResultsLabel()
    {
        return resultsLabel;
    }

    public JComboBox<Integer> getNumberOfBoundariesComboBox()
    {
        return numberOfBoundariesComboBox;
    }

    public JButton getLoadSpectraListButton()
    {
        return loadSpectraListButton;
    }

    public JPanel getMonochromePanel()
    {
        return monochromePanel;
    }

    public JButton getNextButton()
    {
        return nextButton;
    }

    public JButton getPrevButton()
    {
        return prevButton;
    }

    public JButton getShowBoundariesButton()
    {
        return showBoundariesButton;
    }

    public JButton getShowSpectraButton()
    {
        return showSpectraButton;
    }

    public JButton getRemoveBoundariesButton()
    {
        return removeBoundariesButton;
    }

    public JButton getRemoveSpectraButton()
    {
        return removeSpectraButton;
    }

    public JButton getSaveSpectraListButton()
    {
        return saveSpectraListButton;
    }

    public JButton getSaveSelectedSpectraListButton()
    {
        return saveSelectedSpectraListButton;
    }

    public void setNumberOfBoundariesComboBox(JComboBox<Integer> numberOfBoundariesComboBox)
    {
        this.numberOfBoundariesComboBox = numberOfBoundariesComboBox;
    }

    public void setResultsLabel(JLabel resultsLabel)
    {
        this.resultsLabel = resultsLabel;
    }

    public SpectrumPopupMenu getSpectrumPopupMenu()
    {
        return spectrumPopupMenu;
    }

    public void setSpectrumPopupMenu(SpectrumPopupMenu spectrumPopupMenu)
    {
        this.spectrumPopupMenu = spectrumPopupMenu;
    }

	public ItemHandler<S> getSpectrumTableHandler()
	{
		return spectrumTableHandler;
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

		JTable tmpTable = spectrumILP.getTable();
		String trackStr = "" + tmpTable.getRowCount();
//		String pointStr = "" + maxPts;
		String dateTimeStr = "9999-88-88T00:00:00.000000";
		int minW = 30;

		ColorProvider blackCP = new ConstColorProvider(Color.BLACK);
		Object[] nomArr = { true, true, true, true, minW, dateTimeStr, dateTimeStr };
		for (int aCol = 0; aCol < nomArr.length; aCol++)
		{
			TableCellRenderer tmpRenderer = tmpTable.getCellRenderer(0, aCol);
			Component tmpComp = tmpRenderer.getTableCellRendererComponent(tmpTable, nomArr[aCol], false, false, 0, aCol);
			int tmpW = Math.max(minW, tmpComp.getPreferredSize().width + 1);
			tmpTable.getColumnModel().getColumn(aCol).setPreferredWidth(tmpW + 10);
		}
	}
}
