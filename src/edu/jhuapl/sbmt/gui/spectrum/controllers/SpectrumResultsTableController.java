package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.gui.spectrum.SpectrumPopupMenu;
import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchResultsListener;
import edu.jhuapl.sbmt.gui.spectrum.ui.SpectrumResultsTableView;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.Spectrum;
import edu.jhuapl.sbmt.model.spectrum.Spectrum.SpectrumKey;
import edu.jhuapl.sbmt.model.spectrum.coloring.SpectrumColoringStyle;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

public class SpectrumResultsTableController
{
    protected SpectrumResultsTableView panel;
    protected SpectrumSearchModel model;
    protected List<List<String>> spectrumRawResults;
    protected SpectralInstrument instrument;
    protected Renderer renderer;
    protected SpectrumStringRenderer stringRenderer;
    protected PropertyChangeListener propertyChangeListener;
    protected TableModelListener tableModelListener;
    protected SpectraCollection spectrumCollection;
    protected SpectrumPopupMenu spectrumPopupMenu;
    protected DefaultTableModel tableModel;
    protected String[] columnNames = new String[]{
            "Map",
            "Show",
            "Frus",
            "Bndr",
            "Id",
            "Filename",
            "Date"
    };

    public SpectrumResultsTableController(SpectralInstrument instrument, SpectraCollection spectrumCollection, SpectrumSearchModel model, Renderer renderer, SbmtInfoWindowManager infoPanelManager)
    {
        spectrumPopupMenu = new SpectrumPopupMenu(model.getModelManager(), infoPanelManager, renderer);
        spectrumPopupMenu.setInstrument(instrument);
        panel = new SpectrumResultsTableView(instrument, spectrumCollection, spectrumPopupMenu);
        panel.setup();

        spectrumRawResults = model.getSpectrumRawResults();
        this.spectrumCollection = spectrumCollection;
        this.model = model;
        this.instrument = instrument;
        this.renderer = renderer;
        model.addResultsChangedListener(new SpectrumSearchResultsListener()
        {

            @Override
            public void resultsChanged(List<List<String>> results)
            {
                setSpectrumResults(results);
            }

            @Override
            public void resultsCountChanged(int count)
            {
                panel.getResultsLabel().setText(count + " Spectra Found");
            }
        });

        propertyChangeListener = new SpectrumResultsPropertyChangeListener();
        tableModelListener = new SpectrumResultsTableModeListener();

        this.spectrumCollection.addPropertyChangeListener(propertyChangeListener);
    }

    public void setSpectrumResultsPanel()
    {
        setupWidgets();
        setupTable();
    }

    protected void setupWidgets()
    {
        // setup Image Results Table view components
        panel.getNumberOfBoundariesComboBox().setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220", "230", "240", "250", " " }));
        panel.getNumberOfBoundariesComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                numberOfBoundariesComboBoxActionPerformed(evt);
            }
        });
        model.setNumberOfBoundariesToShow(10);

        panel.getPrevButton().setText("<");
        panel.getPrevButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });

        panel.getNextButton().setText(">");
        panel.getNextButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        panel.getRemoveAllButton().setText("Remove All Boundaries");
        panel.getRemoveAllButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllBoundariesButtonActionPerformed(evt);
            }
        });


        panel.getRemoveAllSpectraButton().setText("Remove All Spectra");
        panel.getRemoveAllSpectraButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllFootprintsButtonActionPerformed(evt);
            }
        });

        panel.getSaveSpectraListButton().setText("Save List...");
        panel.getSaveSpectraListButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try
                {
                    model.saveSpectrumListButtonActionPerformed(panel);
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                            "There was an error saving the file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e.printStackTrace();
                }
            }
        });


        panel.getSaveSelectedSpectraListButton().setText("Save Selected List...");
        panel.getSaveSelectedSpectraListButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSelectedSpectrumListButtonActionPerformed(evt);
            }
        });

        panel.getLoadSpectraListButton().setText("Load List...");
        panel.getLoadSpectraListButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try
                {
                    model.loadSpectrumListButtonActionPerformed(evt);
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                            "There was an error reading the file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e.printStackTrace();
                }

            }
        });

    }

    private void saveSelectedSpectrumListButtonActionPerformed(ActionEvent evt)
    {
        try
        {
            model.saveSelectedSpectrumListButtonActionPerformed(this.getPanel(), panel.getResultList().getSelectedRows());
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error saving the file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();
        }
    }

    protected void setupTable()
    {
        tableModel = new SpectrumTableModel(new Object[0][7], columnNames);

        panel.getResultList().setModel(tableModel);
        panel.getResultList().getTableHeader().setReorderingAllowed(false);
        panel.getResultList().getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        panel.getResultList().getModel().addTableModelListener(tableModelListener);

        panel.getResultList().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                maybeShowPopup(e);
                panel.getSaveSelectedSpectraListButton().setEnabled(panel.getResultList().getSelectedRowCount() > 0);
            }

            public void mouseReleased(MouseEvent e)
            {
                maybeShowPopup(e);
                panel.getSaveSelectedSpectraListButton().setEnabled(panel.getResultList().getSelectedRowCount() > 0);
            }
        });

        panel.getResultList().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    model.setSelectedImageIndex(panel.getResultList().getSelectedRows());
                }
            }
        });

        stringRenderer = new SpectrumStringRenderer(model, spectrumRawResults);
        panel.getResultList().setDefaultRenderer(String.class, stringRenderer);
        panel.getResultList().getColumnModel().getColumn(panel.getMapColumnIndex()).setPreferredWidth(31);
        panel.getResultList().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setPreferredWidth(35);
        panel.getResultList().getColumnModel().getColumn(panel.getFrusColumnIndex()).setPreferredWidth(31);
        panel.getResultList().getColumnModel().getColumn(panel.getBndrColumnIndex()).setPreferredWidth(31);
        panel.getResultList().getColumnModel().getColumn(panel.getMapColumnIndex()).setResizable(true);
        panel.getResultList().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setResizable(true);
        panel.getResultList().getColumnModel().getColumn(panel.getFrusColumnIndex()).setResizable(true);
        panel.getResultList().getColumnModel().getColumn(panel.getBndrColumnIndex()).setResizable(true);
    }

    protected JTable getResultList()
    {
        return panel.getResultList();
    }

    public SpectrumResultsTableView getPanel()
    {
        return panel;
    }

    private void prevButtonActionPerformed(ActionEvent evt)
    {
        IdPair resultIntervalCurrentlyShown = model.getResultIntervalCurrentlyShown();
        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
        collection.deselectAll();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 >= 0)
            {
                resultIntervalCurrentlyShown.prevBlock(model.getNumberOfBoundariesToShow());
                model.showFootprints(resultIntervalCurrentlyShown);
            }
        }
    }

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        IdPair resultIntervalCurrentlyShown = model.getResultIntervalCurrentlyShown();
        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
        collection.deselectAll();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < panel.getResultList().getModel().getRowCount())
            {
                resultIntervalCurrentlyShown.nextBlock(model.getNumberOfBoundariesToShow());
                model.showFootprints(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, model.getNumberOfBoundariesToShow());
            model.showFootprints(resultIntervalCurrentlyShown);
        }
    }

    private void removeAllFootprintsButtonActionPerformed(ActionEvent evt)
    {
        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
        collection.removeAllSpectraForInstrument(instrument);
        model.setResultIntervalCurrentlyShown(null);
    }

    protected void removeAllFootprintsForAllInstrumentsButtonActionPerformed(ActionEvent evt)
    {
        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
        collection.removeAllSpectra();
        model.setResultIntervalCurrentlyShown(null);
    }

    private void removeAllBoundariesButtonActionPerformed(ActionEvent evt)
    {
        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
        collection.deselectAll();
        model.setResultIntervalCurrentlyShown(null);
    }

    private void numberOfBoundariesComboBoxActionPerformed(ActionEvent evt) {
        IdPair shown = model.getResultIntervalCurrentlyShown();
        if (shown == null) return;
        model.setNumberOfBoundariesToShow(Integer.parseInt((String)panel.getNumberOfBoundariesComboBox().getSelectedItem()));
        // Only update if there's been a change in what is selected
        int newMaxId = shown.id1 + model.getNumberOfBoundariesToShow();
        if (newMaxId != shown.id2)
        {
            shown.id2 = newMaxId;
            model.showFootprints(shown);
        }
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTable resultList = panel.getResultList();
            int index = resultList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                List<List<String>> imageRawResults = model.getSpectrumRawResults();
                if (!resultList.isRowSelected(index))
                {
                    resultList.clearSelection();
                    resultList.setRowSelectionInterval(index, index);
                }

                int[] selectedIndices = resultList.getSelectedRows();
                List<SpectrumKey> spectrumKeys = new ArrayList<SpectrumKey>();
                for (int selectedIndex : selectedIndices)
                {
                    String name = imageRawResults.get(selectedIndex).get(0);
                    SpectrumKey key = model.createSpectrumKey(name, model.getInstrument());
                    spectrumKeys.add(key);
                }
                panel.getSpectrumPopupMenu().setCurrentSpectra(spectrumKeys);
                panel.getSpectrumPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }


//        JList resultList = panel.getResultList();
//        SpectrumPopupMenu spectrumPopupMenu = panel.getSpectrumPopupMenu();
//        if (e.isPopupTrigger())
//        {
//            int index = resultList.locationToIndex(e.getPoint());
//
//            if (index >= 0 && resultList.getCellBounds(index, index).contains(e.getPoint()))
//            {
//                resultList.setSelectedIndex(index);
////                spectrumPopupMenu.setCurrentSpectrum(createSpectrumName(model.getSpectrumRawResults().get(index)));
//                spectrumPopupMenu.setCurrentSpectrum(createSpectrumName(index));
//                spectrumPopupMenu.setInstrument(instrument);
//                spectrumPopupMenu.show(e.getComponent(), e.getX(), e.getY());
//                spectrumPopupMenu.setSearchPanel(this);
//            }
//        }
    }

    public void setSpectrumResults(List<List<String>> results)
    {
        JTable resultTable = panel.getResultList();
        panel.getResultsLabel().setText(results.size() + " spectra found");
        spectrumRawResults = results;
        stringRenderer.setSpectrumRawResults(spectrumRawResults);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        panel.getResultList().getModel().removeTableModelListener(tableModelListener);
        spectrumCollection.removePropertyChangeListener(propertyChangeListener);

        try
        {
            int mapColumnIndex = panel.getMapColumnIndex();
            int showFootprintColumnIndex = panel.getShowFootprintColumnIndex();
            int frusColumnIndex = panel.getFrusColumnIndex();
            int idColumnIndex = panel.getIdColumnIndex();
            int filenameColumnIndex = panel.getFilenameColumnIndex();
            int dateColumnIndex = panel.getDateColumnIndex();
            int bndrColumnIndex = panel.getBndrColumnIndex();
            int[] widths = new int[resultTable.getColumnCount()];
            int[] columnsNeedingARenderer=new int[]{idColumnIndex,filenameColumnIndex,dateColumnIndex};

            // add the results to the list
            ((DefaultTableModel)resultTable.getModel()).setRowCount(results.size());
            int i=0;
            for (List<String> str : results)
            {
//                Date dt = new Date(Long.parseLong(str.get(1)));

                String name = spectrumRawResults.get(i).get(0);
                SpectrumKey key = model.createSpectrumKey(name,  instrument);
                Spectrum spectrum = (Spectrum) spectrumCollection.getSpectrumFromKey(key);
                if (spectrum != null)
                {
                    resultTable.setValueAt(true, i, mapColumnIndex);
//                    resultTable.setValueAt(true, i, panel.getShowFootprintColumnIndex());
                    resultTable.setValueAt(spectrum.isVisible(), i, panel.getShowFootprintColumnIndex());
                    resultTable.setValueAt(spectrum.isFrustumShowing(), i, panel.getFrusColumnIndex());
                    resultTable.setValueAt(true, i, bndrColumnIndex);
                }
                else
                {
                    resultTable.setValueAt(false, i, mapColumnIndex);
                    resultTable.setValueAt(false, i, showFootprintColumnIndex);
                    resultTable.setValueAt(false, i, frusColumnIndex);
                }

                resultTable.setValueAt(i+1, i, idColumnIndex);
//                if (str.size() == 3) {
//                    System.out.println(
//                            "SpectrumResultsTableController: setSpectrumResults: str 1 is " + str.get(1));
//                    resultTable.setValueAt(str.get(0).substring(str.get(0).lastIndexOf("/") + 1), i, filenameColumnIndex);
//                    resultTable.setValueAt(str.get(1), i, dateColumnIndex);
//                }
//                else {
                    resultTable.setValueAt(str.get(0).substring(str.get(0).lastIndexOf("/") + 1), i, filenameColumnIndex);
                    resultTable.setValueAt(sdf.format(Long.parseLong(str.get(1))), i, dateColumnIndex);
//                    if (spectrum != null)
//                        resultTable.setValueAt(sdf.format(spectrum.getDateTime().toDate().getTime()), i, dateColumnIndex);
//                }



                for (int j : columnsNeedingARenderer)
                {
                    TableCellRenderer renderer = resultTable.getCellRenderer(i, j);
                    Component comp = resultTable.prepareRenderer(renderer, i, j);
                    widths[j] = Math.max (comp.getPreferredSize().width, widths[j]);
                }

                ++i;
            }

            for (int j : columnsNeedingARenderer)
                panel.getResultList().getColumnModel().getColumn(j).setPreferredWidth(widths[j] + 5);

            boolean enablePostSearchButtons = resultTable.getModel().getRowCount() > 0;
            panel.getSaveSpectraListButton().setEnabled(enablePostSearchButtons);
            panel.getSaveSelectedSpectraListButton().setEnabled(resultTable.getSelectedRowCount() > 0);
        }
        finally
        {
            panel.getResultList().getModel().addTableModelListener(tableModelListener);
            spectrumCollection.addPropertyChangeListener(propertyChangeListener);
        }
    }

    class SpectrumResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                JTable resultsTable = panel.getResultList();
                panel.getResultList().getModel().removeTableModelListener(tableModelListener);
                int size = model.getSpectrumRawResults().size();
//                ((DefaultTableModel)panel.getResultList().getModel()).setRowCount(size);
                if ((resultsTable.getModel().getRowCount() == 0) || (size != panel.getResultList().getRowCount()))  return;
                for (int i=0; i<size; ++i)
                {
//                    int actualRow = panel.getResultList().getRowSorter().convertRowIndexToView(i);
//                    System.out.println(
//                            "SpectrumResultsTableController.SpectrumResultsPropertyChangeListener: propertyChange: actual row " + actualRow);
                    int j = (Integer)panel.getResultList().getValueAt(i, panel.getIdColumnIndex())-1;
                    String name = model.getSpectrumRawResults().get(j).get(0);
                    SpectrumKey key = model.createSpectrumKey(name, model.getInstrument());
                    Spectrum spectrum = (Spectrum) spectrumCollection.getSpectrumFromKey(key);
                    if (spectrumCollection.containsKey(key))
                    {
                        resultsTable.setValueAt(true, i, panel.getMapColumnIndex());
//                        resultList.setValueAt(true, i, panel.getShowFootprintColumnIndex());
                        resultsTable.setValueAt(spectrum.isVisible(), i, panel.getShowFootprintColumnIndex());
                        resultsTable.setValueAt(spectrum.isFrustumShowing(), i, panel.getFrusColumnIndex());
                        resultsTable.setValueAt(sdf.format(spectrum.getDateTime().toDate().getTime()), i, panel.getDateColumnIndex());
                    }
                    else
                    {
                        resultsTable.setValueAt(false, i, panel.getMapColumnIndex());
                        resultsTable.setValueAt(false, i, panel.getShowFootprintColumnIndex());
                        resultsTable.setValueAt(false, i, panel.getFrusColumnIndex());
                    }
                    if (spectrum != null && (spectrum.isSelected() == true))
                    {
                        resultsTable.setValueAt(true, i, panel.getBndrColumnIndex());
                    }
                    else
                    {
                        resultsTable.setValueAt(false, i, panel.getBndrColumnIndex());
                    }
                }
                panel.getResultList().getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultsTable.repaint();
//                for (int i=0; i<panel.getResultList().getRowCount(); i++)
//                {
//                    System.out.println(
//                            "SpectrumResultsTableController.SpectrumResultsPropertyChangeListener: propertyChange: name is " + panel.getResultList().getValueAt(i, panel.getFilenameColumnIndex()));
//                }
            }
        }
    }

    class SpectrumResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            List<List<String>> spectrumRawResults = model.getSpectrumRawResults();
            ModelManager modelManager = model.getModelManager();
            SpectraCollection spectra = (SpectraCollection)modelManager.getModel(model.getSpectrumCollectionModelName());
            int actualRow = panel.getResultList().getRowSorter().convertRowIndexToView(e.getFirstRow());
            int row = (Integer)panel.getResultList().getValueAt(actualRow, panel.getIdColumnIndex())-1;
            String name = spectrumRawResults.get(row).get(0);
            SpectrumKey key = model.createSpectrumKey(name, model.getInstrument());
            Spectrum spectrum = (Spectrum) spectra.getSpectrumFromKey(key);
            if (spectrum == null)
            {
                if (e.getColumn() == panel.getMapColumnIndex() && (Boolean)panel.getResultList().getValueAt(actualRow, panel.getMapColumnIndex()))
                {
                    try
                    {
                        spectra.addSpectrum(key.name, key.instrument, SpectrumColoringStyle.getStyleForName(model.getSpectrumColoringStyleName()));
                        model.updateColoring();
                    }
                    catch (IOException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                return;
            }
            if (e.getColumn() == panel.getMapColumnIndex())
            {
                if ((Boolean)panel.getResultList().getValueAt(actualRow, panel.getMapColumnIndex()))
                {
                    try
                    {
                        spectra.addSpectrum(key);
                    }
                    catch (IOException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                else
                {
                    spectra.removeSpectrum(key);
                    renderer.setLighting(LightingType.LIGHT_KIT);
                }
            }
            else if (e.getColumn() == panel.getShowFootprintColumnIndex())
            {
                boolean visible = (Boolean)panel.getResultList().getValueAt(actualRow, panel.getShowFootprintColumnIndex());
                spectra.setVisibility(spectrum, visible);
                if (visible == false)
                {
                    spectra.deselect(spectrum);
                }
                else
                {
                    spectra.select(spectrum);
                }
            }
            else if (e.getColumn() == panel.getFrusColumnIndex())
            {
                spectra.setFrustumVisibility(spectrum, !spectrum.isFrustumShowing());
            }
            else if (e.getColumn() == panel.getBndrColumnIndex())
            {
                try
                {
                    if (spectrum.isSelected())
                        spectra.deselect(spectrum);
                    else
                        spectra.select(spectrum);
                }
                catch (Exception e1) {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                            "There was an error mapping the boundary.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e1.printStackTrace();
                }
            }
        }
    }

    public class SpectrumTableModel extends DefaultTableModel
    {
        public SpectrumTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
//            int sortedRow = panel.getResultList().getRowSorter().convertRowIndexToModel(row);
            int actualRow = panel.getResultList().getRowSorter().convertRowIndexToView(row);

            int sortedRow = (Integer)panel.getResultList().getValueAt(actualRow, panel.getIdColumnIndex())-1;
            if (column == panel.getShowFootprintColumnIndex() || column == panel.getFrusColumnIndex())
            {
                String name = spectrumRawResults.get(sortedRow).get(0);
                SpectrumKey key = model.createSpectrumKey(name, model.getInstrument());
                return spectrumCollection.containsKey(key);
            }
            else
            {
                return column == panel.getMapColumnIndex() || column == panel.getBndrColumnIndex();
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= panel.getBndrColumnIndex())
                return Boolean.class;
            else if (columnIndex == panel.getIdColumnIndex())
                return Integer.class;
            else
                return String.class;
        }
    }
}



//@Override
//public void keyTyped(KeyEvent e)
//{
//
//}

//@Override
//public void keyPressed(KeyEvent e)
//{
//  // 2018-02-08 JP. Turn this method into a no-op for now. The reason is that
//  // currently all listeners respond to all key strokes, and VTK keyboard events
//  // do not have a means to determine their source, so there is no way for listeners
//  // to be more selective. The result is, e.g., if one types "s", statistics windows show
//  // up even if we're not looking at a spectrum tab.
//  //
//  // Leave it in the code (don't comment it out) so Eclipse can find references to this,
//  // and so that we don't unknowingly break this code.
//  boolean disableKeyResponses = true;
//  if (disableKeyResponses) return;
//  ModelManager modelManager = model.getModelManager();
//  Renderer renderer = model.getRenderer();
//
//  if (e.getKeyChar()=='a')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      renderer.removeKeyListener(this);
//      model.toggleSelectAll();
//      renderer.addKeyListener(this);
//  }
//  else if (e.getKeyChar()=='s')
//  {
//      view.getSpectrumPopupMenu().showStatisticsWindow();
//  }
//  else if (e.getKeyChar()=='i' || e.getKeyChar()=='v')    // 'i' sets the lighting direction based on time of a single NIS spectrum, and 'v' looks from just above the footprint toward the sun
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      List<Spectrum> selection=model.getSelectedSpectra();
//      if (selection.size()!=1)
//      {
//          JOptionPane.showMessageDialog(panel, "Please select only one spectrum to specify lighting or viewpoint");
//          return;
//      }
//      Spectrum spectrum=selection.get(0);
//      renderer.setLighting(LightingType.FIXEDLIGHT);
//      Path fullPath=Paths.get(spectrum.getFullPath());
//      Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
//      //Vector3D toSunVector=getToSunUnitVector(relativePath.toString());
//      renderer.setFixedLightDirection(spectrum.getToSunUnitVector()); // the fixed light direction points to the light
//      if (e.getKeyChar()=='v')
//      {
//          Vector3D footprintCenter=new Vector3D(spectrum.getShiftedFootprint().GetCenter());
//          SmallBodyModel smallBodyModel=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//          //
//          vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
//          normalsFilter.SetInputData(spectrum.getUnshiftedFootprint());
//          normalsFilter.SetComputeCellNormals(0);
//          normalsFilter.SetComputePointNormals(1);
//          normalsFilter.SplittingOff();
//          normalsFilter.Update();
//          Vector3D upVector=new Vector3D(PolyDataUtil.computePolyDataNormal(normalsFilter.GetOutput())).normalize();  // TODO: fix this for degenerate cases, i.e. normal parallel to to-sun direction
//          double viewHeight=0.01; // km
//          Vector3D cameraPosition=footprintCenter.add(upVector.scalarMultiply(viewHeight));
//          double lookLength=footprintCenter.subtract(cameraPosition).getNorm();
//          Vector3D focalPoint=cameraPosition.add((new Vector3D(spectrum.getToSunUnitVector())).scalarMultiply(lookLength));
//          //
//          renderer.setCameraOrientation(cameraPosition.toArray(), focalPoint.toArray(), renderer.getRenderWindowPanel().getActiveCamera().GetViewUp(), renderer.getCameraViewAngle());
//      }
//  }
//  else if (e.getKeyChar()=='h')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      model.decreaseFootprintSeparation(0.001);
//  }
//  else if (e.getKeyChar()=='H')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      model.increaseFootprintSeparation(0.001);
//  }
//  else if (e.getKeyChar()=='+')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//      model.setOffset(model.getOffset()+body.getBoundingBoxDiagonalLength()/50);
//  }
//  else if (e.getKeyChar()=='-')
//  {
//      SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//      SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//      model.setOffset(model.getOffset()-body.getBoundingBoxDiagonalLength()/50);
//  }
//}
//
//@Override
//public void keyReleased(KeyEvent e)
//{
//  // TODO Auto-generated method stub
//
//}
