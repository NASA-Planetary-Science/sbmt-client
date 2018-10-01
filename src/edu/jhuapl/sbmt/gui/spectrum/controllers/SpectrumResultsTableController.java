package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.gui.spectrum.SpectrumPopupMenu;
import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchResultsListener;
import edu.jhuapl.sbmt.gui.spectrum.ui.SpectrumResultsTableView;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.Spectrum.SpectrumKey;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

public class SpectrumResultsTableController
{
    protected SpectrumResultsTableView panel;
    protected SpectrumSearchModel model;
    protected List<List<String>> spectrumRawResults;
    private ModelManager modelManager;
    protected SpectralInstrument instrument;
    protected Renderer renderer;
    protected SpectrumStringRenderer stringRenderer;
    protected PropertyChangeListener propertyChangeListener;
    protected TableModelListener tableModelListener;
    protected SpectraCollection spectrumCollection;
    protected PerspectiveImageBoundaryCollection boundaries;
    protected SpectrumPopupMenu spectrumPopupMenu;
    protected SpectraHierarchicalSearchSpecification spectraSpec;
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

    public SpectrumResultsTableController(SpectralInstrument instrument, SpectraCollection spectrumCollection, SpectrumSearchModel model, Renderer renderer, SbmtInfoWindowManager infoPanelManager, SbmtSpectrumWindowManager spectrumPanelManager)
    {
        this.modelManager = model.getModelManager();
        boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(model.getSpectrumBoundaryCollectionModelName());
        spectrumPopupMenu = new SpectrumPopupMenu(modelManager, /*spectrumCollection, boundaries, */infoPanelManager, /*spectrumPanelManager,*/ renderer/*, panel*/);
        panel = new SpectrumResultsTableView(instrument, spectrumCollection, spectrumPopupMenu);
        panel.setup();

        spectrumRawResults = model.getSpectrumRawResults();
        this.spectrumCollection = spectrumCollection;
        modelManager = model.getModelManager();
        this.model = model;
        this.spectraSpec = model.getSpectraSpec();
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
        boundaries.addPropertyChangeListener(propertyChangeListener);
    }

    private void init()
    {
//        panel.setSpectrumPopupMenu(new SpectrumPopupMenu(model.getModelManager(), infoPanelManager, renderer));
//        panel.getSpectrumPopupMenu().addPropertyChangeListener(this);
        spectraSpec = model.getSmallBodyConfig().hierarchicalSpectraSearchSpecification;
        try
        {
            spectraSpec.loadMetadata();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


//        pickManager.getDefaultPicker().addPropertyChangeListener(this);

//        renderer.addKeyListener(this);
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

    private void saveSelectedSpectrumListButtonActionPerformed(ActionEvent evt) {
        File file = CustomFileChooser.showSaveDialog(panel, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing"  + nl);
                int[] selectedIndices = panel.getResultList().getSelectedRows();
                for (int selectedIndex : selectedIndices)
                {
                    String image = new File(spectrumRawResults.get(selectedIndex).get(0)).getName();
                    String dtStr = spectrumRawResults.get(selectedIndex).get(1);
                    Date dt = new Date(Long.parseLong(dtStr));

                    out.write(image + " " + sdf.format(dt) + nl);
                }

                out.close();
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
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
                resultIntervalCurrentlyShown.prevBlock(Integer.parseInt((String)panel.getNumberOfBoundariesComboBox().getSelectedItem()));
                model.showFootprints(resultIntervalCurrentlyShown);
            }
        }

    }

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        IdPair resultIntervalCurrentlyShown = model.getResultIntervalCurrentlyShown();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < panel.getResultList().getModel().getRowCount())
            {
                resultIntervalCurrentlyShown.nextBlock(Integer.parseInt((String)panel.getNumberOfBoundariesComboBox().getSelectedItem()));
                model.showFootprints(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)panel.getNumberOfBoundariesComboBox().getSelectedItem()));
            model.showFootprints(resultIntervalCurrentlyShown);
        }
    }

    private void removeAllFootprintsButtonActionPerformed(ActionEvent evt)
    {
        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
        collection.removeAllSpectraForInstrument(instrument);
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

        // Only update if there's been a change in what is selected
        int newMaxId = shown.id1 + Integer.parseInt((String)panel.getNumberOfBoundariesComboBox().getSelectedItem());
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
//                ImageSource sourceOfLastQuery = imageSearchModel.getImageSourceOfLastQuery();
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
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
                    SpectrumKey key = model.createSpectrumKey(name.substring(0, name.length()-4), model.getInstrument());
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
        panel.getResultsLabel().setText(results.size() + " images matched");
        spectrumRawResults = results;
        stringRenderer.setSpectrumRawResults(spectrumRawResults);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        panel.getResultList().getModel().removeTableModelListener(tableModelListener);
        spectrumCollection.removePropertyChangeListener(propertyChangeListener);
        boundaries.removePropertyChangeListener(propertyChangeListener);

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
                Date dt = new Date(Long.parseLong(str.get(1)));

                String name = spectrumRawResults.get(i).get(0);
                SpectrumKey key = model.createSpectrumKey(name.substring(0, name.length()-4),  instrument);
                if (spectrumCollection.containsKey(key))
                {
                    resultTable.setValueAt(true, i, mapColumnIndex);
//                    PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
//                    resultTable.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
//                    resultTable.setValueAt(!image.isFrustumShowing(), i, frusColumnIndex);
                }
                else
                {
                    resultTable.setValueAt(false, i, mapColumnIndex);
                    resultTable.setValueAt(false, i, showFootprintColumnIndex);
                    resultTable.setValueAt(false, i, frusColumnIndex);
                }

                //TODO fix this - currently this work with list contiguous ranges, not selectable rows in the table
//                if (boundaries.containsBoundary(key))
//                    resultTable.setValueAt(true, i, bndrColumnIndex);
//                else
//                    resultTable.setValueAt(false, i, bndrColumnIndex);

                resultTable.setValueAt(i+1, i, idColumnIndex);
                resultTable.setValueAt(str.get(0).substring(str.get(0).lastIndexOf("/") + 1), i, filenameColumnIndex);
                resultTable.setValueAt(sdf.format(dt), i, dateColumnIndex);

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
            boundaries.addPropertyChangeListener(propertyChangeListener);
        }

        // Show the first set of boundaries
        model.setResultIntervalCurrentlyShown( new IdPair(0, Integer.parseInt((String)panel.getNumberOfBoundariesComboBox().getSelectedItem())));
//        if (boundaries.getProps().size() > 0)
            model.showFootprints(model.getResultIntervalCurrentlyShown());

    }





//    @Override
//    public void keyTyped(KeyEvent e)
//    {
//
//    }

//    @Override
//    public void keyPressed(KeyEvent e)
//    {
//        // 2018-02-08 JP. Turn this method into a no-op for now. The reason is that
//        // currently all listeners respond to all key strokes, and VTK keyboard events
//        // do not have a means to determine their source, so there is no way for listeners
//        // to be more selective. The result is, e.g., if one types "s", statistics windows show
//        // up even if we're not looking at a spectrum tab.
//        //
//        // Leave it in the code (don't comment it out) so Eclipse can find references to this,
//        // and so that we don't unknowingly break this code.
//        boolean disableKeyResponses = true;
//        if (disableKeyResponses) return;
//        ModelManager modelManager = model.getModelManager();
//        Renderer renderer = model.getRenderer();
//
//        if (e.getKeyChar()=='a')
//        {
//            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//            renderer.removeKeyListener(this);
//            model.toggleSelectAll();
//            renderer.addKeyListener(this);
//        }
//        else if (e.getKeyChar()=='s')
//        {
//            view.getSpectrumPopupMenu().showStatisticsWindow();
//        }
//        else if (e.getKeyChar()=='i' || e.getKeyChar()=='v')    // 'i' sets the lighting direction based on time of a single NIS spectrum, and 'v' looks from just above the footprint toward the sun
//        {
//            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//            List<Spectrum> selection=model.getSelectedSpectra();
//            if (selection.size()!=1)
//            {
//                JOptionPane.showMessageDialog(panel, "Please select only one spectrum to specify lighting or viewpoint");
//                return;
//            }
//            Spectrum spectrum=selection.get(0);
//            renderer.setLighting(LightingType.FIXEDLIGHT);
//            Path fullPath=Paths.get(spectrum.getFullPath());
//            Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
//            //Vector3D toSunVector=getToSunUnitVector(relativePath.toString());
//            renderer.setFixedLightDirection(spectrum.getToSunUnitVector()); // the fixed light direction points to the light
//            if (e.getKeyChar()=='v')
//            {
//                Vector3D footprintCenter=new Vector3D(spectrum.getShiftedFootprint().GetCenter());
//                SmallBodyModel smallBodyModel=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//                //
//                vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
//                normalsFilter.SetInputData(spectrum.getUnshiftedFootprint());
//                normalsFilter.SetComputeCellNormals(0);
//                normalsFilter.SetComputePointNormals(1);
//                normalsFilter.SplittingOff();
//                normalsFilter.Update();
//                Vector3D upVector=new Vector3D(PolyDataUtil.computePolyDataNormal(normalsFilter.GetOutput())).normalize();  // TODO: fix this for degenerate cases, i.e. normal parallel to to-sun direction
//                double viewHeight=0.01; // km
//                Vector3D cameraPosition=footprintCenter.add(upVector.scalarMultiply(viewHeight));
//                double lookLength=footprintCenter.subtract(cameraPosition).getNorm();
//                Vector3D focalPoint=cameraPosition.add((new Vector3D(spectrum.getToSunUnitVector())).scalarMultiply(lookLength));
//                //
//                renderer.setCameraOrientation(cameraPosition.toArray(), focalPoint.toArray(), renderer.getRenderWindowPanel().getActiveCamera().GetViewUp(), renderer.getCameraViewAngle());
//            }
//        }
//        else if (e.getKeyChar()=='h')
//        {
//            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//            model.decreaseFootprintSeparation(0.001);
//        }
//        else if (e.getKeyChar()=='H')
//        {
//            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//            model.increaseFootprintSeparation(0.001);
//        }
//        else if (e.getKeyChar()=='+')
//        {
//            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//            SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//            model.setOffset(model.getOffset()+body.getBoundingBoxDiagonalLength()/50);
//        }
//        else if (e.getKeyChar()=='-')
//        {
//            SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//            SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//            model.setOffset(model.getOffset()-body.getBoundingBoxDiagonalLength()/50);
//        }
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e)
//    {
//        // TODO Auto-generated method stub
//
//    }




//    public abstract String createSpectrumName(String currentSpectrumRaw);






//    @Override
//    public void propertyChange(PropertyChangeEvent evt)
//    {
//        JList resultList = view.getResultList();
//
//        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
//        {
//            PickEvent e = (PickEvent)evt.getNewValue();
//
//
//            Model spectraModel = model.getModelManager().getModel(e.getPickedProp());
//            if (spectraModel instanceof SpectraCollection)
//            {
//                SpectraCollection coll=(SpectraCollection)spectraModel;
//                String name = coll.getSpectrumName((vtkActor)e.getPickedProp());
//                Spectrum spectrum=coll.getSpectrum(name);
//                if (spectrum==null)
//                    return;
//
//                resultList.getSelectionModel().clearSelection();
//                for (int i=0; i<resultList.getModel().getSize(); i++)
//                {
//                    if (FilenameUtils.getBaseName(name).equals(resultList.getModel().getElementAt(i)))
//                    {
//                        resultList.getSelectionModel().setSelectionInterval(i, i);
//                        resultList.ensureIndexIsVisible(i);
//                        coll.select(coll.getSpectrum(name));//.setShowOutline(true);
//                    }
//                }
//
//                for (int i=0; i<resultList.getModel().getSize(); i++)
//                {
//                    if (!resultList.getSelectionModel().isSelectedIndex(i))
//                    {
////                        Spectrum spectrum_=coll.getSpectrum(createSpectrumName((String)resultList.getModel().getElementAt(i)));
//                        Spectrum spectrum_=coll.getSpectrum(createSpectrumName(i));
//                        if (spectrum_ != null)
//                            coll.deselect(spectrum_);
//                    }
//                }
//                resultList.repaint();
//            }
//       }
//        else
//        {
//            resultList.repaint();
//        }
//
//    }


//    class MyListCellRenderer extends DefaultListCellRenderer
//    {
//        public MyListCellRenderer()
//        {
//            setOpaque(true);
//        }
//
//        public Component getListCellRendererComponent(JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus)
//        {
//            //setText(value.toString());
//            JLabel label = (JLabel) super.getListCellRendererComponent(paramlist, value, index, isSelected, cellHasFocus);
//            label.setOpaque(true /* was isSelected */); // Highlight only when selected
//            if(isSelected) { // I faked a match for the second index, put you matching condition here.
//                label.setBackground(Color.YELLOW);
//                label.setEnabled(false);
//            }
//            else
//            {
////                String spectrumFile=createSpectrumName(value.toString());
//                String spectrumFile=createSpectrumName(index);
//                SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
//                Spectrum spectrum=collection.getSpectrum(spectrumFile);
//                setBackground(Color.LIGHT_GRAY);
//                if (spectrum==null)
//                    setForeground(Color.black);
//                else
//                {
//                    double[] color=spectrum.getChannelColor();
//                    for (int i=0; i<3; i++)
//                    {
//                        if (color[i]>1)
//                            color[i]=1;
//                        if (color[i]<0)
//                            color[i]=0;
//                    }
//                    setForeground(new Color((float)color[0],(float)color[1],(float)color[2]));
//                }
//            }
//            return label;
//        }
//    }

//    protected abstract void setSpectrumSearchResults(List<List<String>> results);
//
//    public SpectrumSearchView getView()
//    {
//        return view;
//    }

    class SpectrumResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
//                JTable resultList = imageResultsTableView.getResultList();
//                imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
//                int size = imageRawResults.size();
//                for (int i=0; i<size; ++i)
//                {
//                    String name = imageRawResults.get(i).get(0);
//                    ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
//                    if (imageCollection.containsImage(key))
//                    {
//                        resultList.setValueAt(true, i, imageResultsTableView.getMapColumnIndex());
//                        PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
//                        resultList.setValueAt(image.isVisible(), i, imageResultsTableView.getShowFootprintColumnIndex());
//                        resultList.setValueAt(image.isFrustumShowing(), i, imageResultsTableView.getFrusColumnIndex());
//                    }
//                    else
//                    {
//                        resultList.setValueAt(false, i, imageResultsTableView.getMapColumnIndex());
//                        resultList.setValueAt(false, i, imageResultsTableView.getShowFootprintColumnIndex());
//                        resultList.setValueAt(false, i, imageResultsTableView.getFrusColumnIndex());
//                    }
//                    if (boundaries.containsBoundary(key))
//                        resultList.setValueAt(true, i, imageResultsTableView.getBndrColumnIndex());
//                    else
//                        resultList.setValueAt(false, i, imageResultsTableView.getBndrColumnIndex());
//                }
//                imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
//                // Repaint the list in case the boundary colors has changed
//                resultList.repaint();
            }
        }
    }

    class SpectrumResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
//            ImageSource sourceOfLastQuery = imageSearchModel.getImageSourceOfLastQuery();
//            List<List<String>> imageRawResults = imageSearchModel.getImageResults();
//            ModelManager modelManager = imageSearchModel.getModelManager();
//            if (e.getColumn() == imageResultsTableView.getMapColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                String namePrefix = name.substring(0, name.length()-4);
//                if ((Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getMapColumnIndex()))
//                    imageSearchModel.loadImages(namePrefix);
//                else
//                {
//                    imageSearchModel.unloadImages(namePrefix);
//                    renderer.setLighting(LightingType.LIGHT_KIT);
//                }
//            }
//            else if (e.getColumn() == imageResultsTableView.getShowFootprintColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                String namePrefix = name.substring(0, name.length()-4);
//                boolean visible = (Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getShowFootprintColumnIndex());
//                imageSearchModel.setImageVisibility(namePrefix, visible);
//            }
//            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, imageSearchModel.getInstrument());
//                ImageCollection images = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName());
//                if (images.containsImage(key))
//                {
//                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
//                    image.setShowFrustum(!image.isFrustumShowing());
//                }
//            }
//            else if (e.getColumn() == imageResultsTableView.getBndrColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, imageSearchModel.getInstrument());
//                try
//                {
//                    if (!boundaries.containsBoundary(key))
//                        boundaries.addBoundary(key);
//                    else
//                        boundaries.removeBoundary(key);
//                }
//                catch (Exception e1) {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
//                            "There was an error mapping the boundary.",
//                            "Error",
//                            JOptionPane.ERROR_MESSAGE);
//
//                    e1.printStackTrace();
//                }
//            }

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
            if (column == panel.getShowFootprintColumnIndex() || column == panel.getFrusColumnIndex())
            {
//                String name = spectrumRawResults.get(row).get(0);
//                SpectrumKey key = model.createImageKey(name.substring(0, name.length()-4), model.getImageSourceOfLastQuery(), model.getInstrument());
//                SpectraCollection spectrumCollection = (SpectraCollection)modelManager.getModel(model.getSpectrumCollectionModelName());
//                return spectrumCollection.containsImage(key);
                return true;
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
            else
                return String.class;
        }
    }
}
