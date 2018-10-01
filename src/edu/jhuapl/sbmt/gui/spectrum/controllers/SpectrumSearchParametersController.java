package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import com.jidesoft.swing.CheckBoxTree;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.ui.SpectrumSearchParametersPanel;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;

public class SpectrumSearchParametersController
{
    protected SpectrumSearchParametersPanel panel;
    protected SpectrumSearchModel model;
    private JPanel auxPanel;
    protected PickManager pickManager;
    protected SmallBodyViewConfig smallBodyConfig;
    protected SpectraHierarchicalSearchSpecification spectraSpec;

    public SpectrumSearchParametersController(SpectrumSearchModel model, PickManager pickManager)
    {
        this.model = model;
        this.spectraSpec = model.getSpectraSpec();
        this.panel = new SpectrumSearchParametersPanel();
        this.pickManager = pickManager;
        this.smallBodyConfig = model.getSmallBodyConfig();
    }

    private void postInitComponents()
    {
        //startDate = getDefaultStartDate();
        ((SpinnerDateModel)panel.getStartSpinner().getModel()).setValue(model.getStartDate());
        //endDate = getDefaultEndDate();
        ((SpinnerDateModel)panel.getEndSpinner().getModel()).setValue(model.getEndDate());

        //toDistanceTextField.setValue(getDefaultMaxSpacecraftDistance());

//        polygonType3CheckBox.setVisible(false);

//       setupComboBoxes();
    }

//    protected void initHierarchicalImageSearch()
//    {
//        // Show/hide panels depending on whether this body has hierarchical image search capabilities
//        if(model.getSmallBodyConfig().hasHierarchicalSpectraSearch)
//        {
//            // Has hierarchical search capabilities, these replace the camera and filter checkboxes so hide them
////            filterCheckBoxPanel.setVisible(false);
////            userDefinedCheckBoxPanel.setVisible(false);
//
//            // Create the tree
//            spectraSpec.clearTreeLeaves();
////            spectraSpec.setRootName(instrument.getDisplayName());
//            spectraSpec.readHierarchyForInstrument(instrument.getDisplayName());
//            checkBoxTree = new CheckBoxTree(spectraSpec.getTreeModel());
//
//            // Place the tree in the panel
//            panel.getDataSourcesScrollPane().setViewportView(checkBoxTree);
//        }
//        else
//        {
//            // No hierarchical search capabilities, hide the scroll pane
//            panel.getDataSourcesScrollPane().setVisible(false);
//        }
//    }

    public void setupSearchParametersPanel()
    {

        if(model.getSmallBodyConfig().hasHierarchicalSpectraSearch)
        {
            model.getSmallBodyConfig().hierarchicalSpectraSearchSpecification.processTreeSelections(
                    panel.getCheckBoxTree().getCheckBoxTreeSelectionModel().getSelectionPaths());
        }

        panel.getClearRegionButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });

        JSpinner startSpinner = panel.getStartSpinner();
        startSpinner.setModel(new javax.swing.SpinnerDateModel(smallBodyConfig.imageSearchDefaultStartDate, null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.setPreferredSize(new java.awt.Dimension(180, 22));
        startSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startSpinnerStateChanged(evt);
            }
        });


        panel.getEndDateLabel().setText("End Date:");
        JSpinner endSpinner = panel.getEndSpinner();
        endSpinner.setModel(new javax.swing.SpinnerDateModel(smallBodyConfig.imageSearchDefaultEndDate, null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setPreferredSize(new java.awt.Dimension(180, 22));
        endSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endSpinnerStateChanged(evt);
            }
        });

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });

        JFormattedTextField toPhaseTextField = panel.getToPhaseTextField();
        toPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
        toPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromPhaseTextField = panel.getFromPhaseTextField();
        fromPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField toEmissionTextField = panel.getToEmissionTextField();
        toEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
        toEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromEmissionTextField = panel.getFromEmissionTextField();
        fromEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField toIncidenceTextField = panel.getToIncidenceTextField();
        toIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromIncidenceTextField = panel.getFromIncidenceTextField();
        fromIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField toDistanceTextField = panel.getToDistanceTextField();
        toDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toDistanceTextField.setText("1000");
        toDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromDistanceTextField = panel.getFromDistanceTextField();
        fromDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        panel.getClearRegionButton().setText("Clear Region");
        panel.getClearRegionButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });


        panel.getSubmitButton().setText("Search");
        panel.getSubmitButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                submitButtonActionPerformed(evt);
                model.performSearch();
            }
        });



        panel.getSelectRegionButton().setText("Select Region");
        panel.getSelectRegionButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });



        model.setStartDate(smallBodyConfig.imageSearchDefaultStartDate);
        ((SpinnerDateModel)startSpinner.getModel()).setValue(model.getStartDate());
        model.setEndDate(smallBodyConfig.imageSearchDefaultEndDate);
        ((SpinnerDateModel)endSpinner.getModel()).setValue(model.getEndDate());



        toDistanceTextField.setValue(smallBodyConfig.imageSearchDefaultMaxSpacecraftDistance);

        initHierarchicalImageSearch();


    }

    // Sets up everything related to hierarchical image searches
    protected void initHierarchicalImageSearch()
    {
        // Show/hide panels depending on whether this body has hierarchical image search capabilities
        if(model.getSmallBodyConfig().hasHierarchicalImageSearch)
        {
            // Has hierarchical search capabilities, these replace the camera and filter checkboxes so hide them
//            panel.getFilterCheckBoxPanel().setVisible(false);
//            panel.getUserDefinedCheckBoxPanel().setVisible(false);
            panel.getAuxPanel().setVisible(false);

            // Create the tree
            panel.setCheckBoxTree(new CheckBoxTree(model.getSmallBodyConfig().hierarchicalImageSearchSpecification.getTreeModel()));

            // Place the tree in the panel
            panel.getHierarchicalSearchScrollPane().setViewportView(panel.getCheckBoxTree());
        }
        else
        {
            // No hierarchical search capabilities, hide the scroll pane
            if (panel.getHierarchicalSearchScrollPane() != null)
                panel.getHierarchicalSearchScrollPane().setVisible(false);
        }
    }

//    private void submitButtonActionPerformed(ActionEvent evt)
//    {
//        try
//        {
////            view.getSelectRegionButton().setSelected(false);
//            model.getPickManager().setPickMode(PickMode.DEFAULT);
//
//
//
//            GregorianCalendar startDateGreg = new GregorianCalendar();
//            GregorianCalendar endDateGreg = new GregorianCalendar();
//            startDateGreg.setTime(model.getStartDate());
//            endDateGreg.setTime(model.getEndDate());
//            DateTime startDateJoda = new DateTime(
//                    startDateGreg.get(GregorianCalendar.YEAR),
//                    startDateGreg.get(GregorianCalendar.MONTH)+1,
//                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
//                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
//                    startDateGreg.get(GregorianCalendar.MINUTE),
//                    startDateGreg.get(GregorianCalendar.SECOND),
//                    startDateGreg.get(GregorianCalendar.MILLISECOND),
//                    DateTimeZone.UTC);
//            DateTime endDateJoda = new DateTime(
//                    endDateGreg.get(GregorianCalendar.YEAR),
//                    endDateGreg.get(GregorianCalendar.MONTH)+1,
//                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
//                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
//                    endDateGreg.get(GregorianCalendar.MINUTE),
//                    endDateGreg.get(GregorianCalendar.SECOND),
//                    endDateGreg.get(GregorianCalendar.MILLISECOND),
//                    DateTimeZone.UTC);
//
////            TreeSet<Integer> cubeList = null;
//            if (cubeList != null)
//                cubeList.clear();
//            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)model.getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
//            SmallBodyModel bodyModel = (SmallBodyModel)model.getModelManager().getModel(ModelNames.SMALL_BODY);
//            if (selectionModel.getNumberOfStructures() > 0)
//            {
//                AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);
//
//                // Always use the lowest resolution model for getting the intersection cubes list.
//                // Therefore, if the selection region was created using a higher resolution model,
//                // we need to recompute the selection region using the low res model.
//                if (bodyModel.getModelResolution() > 0)
//                {
//                    vtkPolyData interiorPoly = new vtkPolyData();
//                    bodyModel.drawRegularPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
//                    cubeList = bodyModel.getIntersectingCubes(interiorPoly);
//                }
//                else
//                {
//                    cubeList = bodyModel.getIntersectingCubes(region.interiorPolyData);
//                }
//            }
//
//            List<Integer> productsSelected;
//            List<List<String>> results = new ArrayList<List<String>>();
//            if(model.getSmallBodyConfig().hasHierarchicalSpectraSearch)
//            {
//                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
////                sumOfProductsSearch = true;
//                SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
//                // Process the user's selections
//                model.getSmallBodyConfig().hierarchicalSpectraSearchSpecification.processTreeSelections(
//                        checkBoxTree.getCheckBoxTreeSelectionModel().getSelectionPaths());
//
//                // Get the selected (camera,filter) pairs
//
//                productsSelected = spectraSpec.getSelectedDatasets();
//                InstrumentMetadata<SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
////                ArrayList<ArrayList<String>> specs = spectraSpec.getSpecs();
//                TreeModel tree = spectraSpec.getTreeModel();
//                List<SearchSpec> specs = instrumentMetadata.getSpecs();
//                for (Integer selected : productsSelected)
//                {
//                    String name = tree.getChild(tree.getRoot(), selected).toString();
//                    SearchSpec spec = specs.get(selected);
//                    FixedListSearchMetadata searchMetadata = FixedListSearchMetadata.of(spec.getDataName(),
//                                                                                        spec.getDataListFilename(),
//                                                                                        spec.getDataPath(),
//                                                                                        spec.getDataRootLocation(),
//                                                                                        spec.getSource());
//
//                    List<List<String>> thisResult = instrument.getQueryBase().runQuery(searchMetadata).getResultlist();
//                    collection.tagSpectraWithMetadata(thisResult, spec);
//                    results.addAll(thisResult);
//                }
////                results = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist.txt", "spectra", ImageSource.CORRECTED_SPICE)).getResultlist();
//            }
//            else
//            {
//                QueryBase queryType = instrument.getQueryBase();
//                if (queryType instanceof FixedListQuery)
//                {
//                    FixedListQuery query = (FixedListQuery)queryType;
//                    results = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist", "spectra", query.getRootPath(), ImageSource.CORRECTED_SPICE)).getResultlist();
//                }
//                else
//                {
//                    SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
//                            Ranges.closed(Double.valueOf(view.getFromDistanceTextField().getText()), Double.valueOf(view.getToDistanceTextField().getText())),
//                            "", null,   //TODO: reinstate polygon types here
//                            Ranges.closed(Double.valueOf(view.getFromIncidenceTextField().getText()), Double.valueOf(view.getToIncidenceTextField().getText())),
//                            Ranges.closed(Double.valueOf(view.getFromEmissionTextField().getText()), Double.valueOf(view.getToEmissionTextField().getText())),
//                            Ranges.closed(Double.valueOf(view.getFromPhaseTextField().getText()), Double.valueOf(view.getToPhaseTextField().getText())),
//                            cubeList);
//
//                    DatabaseQueryBase query = (DatabaseQueryBase)queryType;
//                    results = query.runQuery(searchMetadata).getResultlist();
//                }
//            }
//            setSpectrumSearchResults(results);
////            SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
////            collection.tagSpectraWithMetadata(results, spec);
//
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            System.out.println(e);
//            return;
//        }
//    }

    public void formComponentHidden(java.awt.event.ComponentEvent evt)
    {
        panel.getSelectRegionButton().setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);
    }

    public void startSpinnerStateChanged(javax.swing.event.ChangeEvent evt)
    {
        Date date =
                ((SpinnerDateModel)panel.getStartSpinner().getModel()).getDate();
        if (date != null)
            model.setStartDate(date);
    }

    public void endSpinnerStateChanged(javax.swing.event.ChangeEvent evt)
    {
        Date date =
                ((SpinnerDateModel)panel.getEndSpinner().getModel()).getDate();
        if (date != null)
            model.setEndDate(date);

    }

    public void selectRegionButtonActionPerformed(ActionEvent evt)
    {
        if (panel.getSelectRegionButton().isSelected())
            pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
        else
            pickManager.setPickMode(PickMode.DEFAULT);
    }

    public void clearRegionButtonActionPerformed(ActionEvent evt)
    {
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)model.getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
        selectionModel.removeAllStructures();
    }


    public SpectrumSearchParametersPanel getPanel()
    {
        return panel;
    }

    public void setPanel(SpectrumSearchParametersPanel panel)
    {
        this.panel = panel;
    }


    public JPanel getAuxPanel()
    {
        return auxPanel;
    }

    public void setAuxPanel(JPanel auxPanel)
    {
        this.auxPanel = auxPanel;
        panel.setAuxPanel(auxPanel);
    }

//    private void formComponentHidden(ComponentEvent evt)
//    {
//        panel.getSelectRegionButton().setSelected(false);
//        model.getPickManager().setPickMode(PickMode.DEFAULT);
//    }
//
//    private void startSpinnerStateChanged(ChangeEvent evt)
//    {
//        Date date = ((SpinnerDateModel)panel.getStartSpinner().getModel()).getDate();
//        if (date != null)
//            model.setStartDate(date);
//    }
//
//    private void endSpinnerStateChanged(ChangeEvent evt)
//    {
//        Date date = ((SpinnerDateModel)panel.getEndSpinner().getModel()).getDate();
//        if (date != null)
//            model.setEndDate(date);
//    }
//
//    private void selectRegionButtonActionPerformed(ActionEvent evt)
//    {
//        if (panel.getSelectRegionButton().isSelected())
//            model.getPickManager().setPickMode(PickMode.CIRCLE_SELECTION);
//        else
//            model.getPickManager().setPickMode(PickMode.DEFAULT);
//    }
//
//    private void clearRegionButtonActionPerformed(ActionEvent evt)
//    {
//        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)model.getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
//        selectionModel.removeAllStructures();
//    }
}
