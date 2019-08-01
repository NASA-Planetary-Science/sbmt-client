package edu.jhuapl.sbmt.spectrum.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.TreeSet;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jidesoft.swing.CheckBoxTree;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.EllipsePolygon;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchParametersModel;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumSearchParametersPanel;

public class SpectrumSearchParametersController
{
    protected SpectrumSearchParametersPanel panel;
    protected BaseSpectrumSearchModel model;
    private JPanel auxPanel;
    protected PickManager pickManager;
    protected SpectraHierarchicalSearchSpecification spectraSpec;
    private boolean hasHierarchicalSpectraSearch;
    private double imageSearchDefaultMaxSpacecraftDistance;
    private Date imageSearchDefaultStartDate;
    private Date imageSearchDefaultEndDate;
    private ModelManager modelManager;
    private SpectrumSearchParametersModel searchParameters;

    public SpectrumSearchParametersController(Date imageSearchDefaultStartDate, Date imageSearchDefaultEndDate, boolean hasHierarchicalSpectraSearch, double imageSearchDefaultMaxSpacecraftDistance, SpectraHierarchicalSearchSpecification spectraSpec, BaseSpectrumSearchModel model, PickManager pickManager, ModelManager modelManager)
    {
        this.model = model;
        searchParameters = new SpectrumSearchParametersModel();
        this.modelManager = modelManager;
        this.spectraSpec = spectraSpec;
        this.panel = new SpectrumSearchParametersPanel(hasHierarchicalSpectraSearch);
        this.pickManager = pickManager;
        this.hasHierarchicalSpectraSearch = hasHierarchicalSpectraSearch;
        this.imageSearchDefaultMaxSpacecraftDistance = imageSearchDefaultMaxSpacecraftDistance;
        this.imageSearchDefaultEndDate = imageSearchDefaultEndDate;
        this.imageSearchDefaultStartDate = imageSearchDefaultStartDate;
    }

    public void setupSearchParametersPanel()
    {
        initHierarchicalImageSearch();

        if(hasHierarchicalSpectraSearch)
        {
            spectraSpec.processTreeSelections(
                    panel.getCheckBoxTree().getCheckBoxTreeSelectionModel().getSelectionPaths());
            panel.getSelectRegionButton().setVisible(false);
            panel.getClearRegionButton().setVisible(false);
        }
        else
        {
            panel.getSelectRegionButton().setVisible(true);
            panel.getClearRegionButton().setVisible(true);

            panel.getClearRegionButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    clearRegionButtonActionPerformed(evt);
                }
            });

            JSpinner startSpinner = panel.getStartSpinner();
            startSpinner.setModel(new javax.swing.SpinnerDateModel(searchParameters.getStartDate(), null, null, java.util.Calendar.DAY_OF_MONTH));
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
            endSpinner.setModel(new javax.swing.SpinnerDateModel(searchParameters.getEndDate(), null, null, java.util.Calendar.DAY_OF_MONTH));
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


            searchParameters.setStartDate(imageSearchDefaultStartDate);
            ((SpinnerDateModel)startSpinner.getModel()).setValue(searchParameters.getStartDate());
            searchParameters.setEndDate(imageSearchDefaultEndDate);
            ((SpinnerDateModel)endSpinner.getModel()).setValue(searchParameters.getEndDate());

            panel.getFullCheckBox().addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.addToPolygonsSelected(0);
                }
            });

            panel.getPartialCheckBox().addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.addToPolygonsSelected(0);
                }
            });

            panel.getDegenerateCheckBox().addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.addToPolygonsSelected(0);
                }
            });

            panel.getFromDistanceTextField().getDocument().addDocumentListener(new DocumentListener()
            {

                @Override
                public void removeUpdate(DocumentEvent e)
                {
                    if (!panel.getFromDistanceTextField().getText().equals(""))
                        searchParameters.setMinDistanceQuery(Integer.parseInt(panel.getFromDistanceTextField().getText()));
                }

                @Override
                public void insertUpdate(DocumentEvent e)
                {
                    if (!panel.getFromDistanceTextField().getText().equals(""))
                        searchParameters.setMinDistanceQuery(Integer.parseInt(panel.getFromDistanceTextField().getText()));
                }

                @Override
                public void changedUpdate(DocumentEvent e)
                {
                    if (!panel.getFromDistanceTextField().getText().equals(""))
                        searchParameters.setMinDistanceQuery(Integer.parseInt(panel.getFromDistanceTextField().getText()));

                }
            });

            panel.getToDistanceTextField().getDocument().addDocumentListener(new DocumentListener()
            {

                @Override
                public void removeUpdate(DocumentEvent e)
                {
                    if (!panel.getToDistanceTextField().getText().equals(""))
                        searchParameters.setMaxDistanceQuery(Integer.parseInt(panel.getToDistanceTextField().getText()));
                }

                @Override
                public void insertUpdate(DocumentEvent e)
                {
                    if (!panel.getToDistanceTextField().getText().equals(""))
                        searchParameters.setMaxDistanceQuery(Integer.parseInt(panel.getToDistanceTextField().getText()));
                }

                @Override
                public void changedUpdate(DocumentEvent e)
                {
                    if (!panel.getToDistanceTextField().getText().equals(""))
                        searchParameters.setMaxDistanceQuery(Integer.parseInt(panel.getToDistanceTextField().getText()));
                }
            });

            panel.getFromIncidenceTextField().addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.setMinIncidenceQuery(Integer.parseInt(panel.getFromIncidenceTextField().getText()));
                }
            });

            panel.getToIncidenceTextField().addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.setMaxIncidenceQuery(Integer.parseInt(panel.getToIncidenceTextField().getText()));
                }
            });

            panel.getFromEmissionTextField().addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.setMinEmissionQuery(Integer.parseInt(panel.getFromEmissionTextField().getText()));
                }
            });

            panel.getToEmissionTextField().addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.setMaxEmissionQuery(Integer.parseInt(panel.getToEmissionTextField().getText()));
                }
            });

            panel.getFromPhaseTextField().addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.setMinPhaseQuery(Integer.parseInt(panel.getFromPhaseTextField().getText()));
                }
            });

            panel.getToPhaseTextField().addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchParameters.setMaxPhaseQuery(Integer.parseInt(panel.getToPhaseTextField().getText()));
                }
            });

            toDistanceTextField.setValue(imageSearchDefaultMaxSpacecraftDistance);

            searchParameters.setStartDate((Date)panel.getStartSpinner().getValue());
            searchParameters.setEndDate((Date)panel.getEndSpinner().getValue());
            searchParameters.setMinDistanceQuery(Integer.parseInt(panel.getFromDistanceTextField().getText()));
            searchParameters.setMaxDistanceQuery(Integer.parseInt(panel.getToDistanceTextField().getText()));
            searchParameters.setMinIncidenceQuery(Integer.parseInt(panel.getFromIncidenceTextField().getText()));
            searchParameters.setMaxIncidenceQuery(Integer.parseInt(panel.getToIncidenceTextField().getText()));
            searchParameters.setMinEmissionQuery(Integer.parseInt(panel.getFromEmissionTextField().getText()));
            searchParameters.setMaxEmissionQuery(Integer.parseInt(panel.getToEmissionTextField().getText()));
            searchParameters.setMinPhaseQuery(Integer.parseInt(panel.getFromPhaseTextField().getText()));
            searchParameters.setMaxPhaseQuery(Integer.parseInt(panel.getToPhaseTextField().getText()));

        }

        panel.getClearRegionButton().setText("Clear Region");
        panel.getClearRegionButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });


        panel.getSubmitButton().setText("Search");
        panel.getSubmitButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (hasHierarchicalSpectraSearch)
                    model.setSelectedPath(panel.getCheckBoxTree().getCheckBoxTreeSelectionModel().getSelectionPaths());
                panel.getSelectRegionButton().setSelected(false);
                model.clearSpectraFromDisplay();
                pickManager.setPickMode(PickMode.DEFAULT);
                TreeSet<Integer> cubeList = null;

                AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
                SmallBodyModel bodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
                if (selectionModel.getNumberOfStructures() > 0)
                {
                    EllipsePolygon region = (EllipsePolygon)selectionModel.getStructure(0);

                    // Always use the lowest resolution model for getting the intersection cubes list.
                    // Therefore, if the selection region was created using a higher resolution model,
                    // we need to recompute the selection region using the low res model.
                    if (bodyModel.getModelResolution() > 0)
                    {
                        vtkPolyData interiorPoly = new vtkPolyData();
                        bodyModel.drawRegularPolygonLowRes(region.getCenter(), region.radius, region.numberOfSides, interiorPoly, null);
                        cubeList = bodyModel.getIntersectingCubes(interiorPoly);
                    }
                    else
                    {
                        cubeList = bodyModel.getIntersectingCubes(region.interiorPolyData);
                    }
                }
                model.performSearch(searchParameters, cubeList, hasHierarchicalSpectraSearch, spectraSpec, model.getSelectedPath());
            }
        });


        panel.getSelectRegionButton().setText("Select Region");
        panel.getSelectRegionButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });
    }

    // Sets up everything related to hierarchical image searches
    protected void initHierarchicalImageSearch()
    {
        // Show/hide panels depending on whether this body has hierarchical image search capabilities
        if(hasHierarchicalSpectraSearch)
        {
            // Has hierarchical search capabilities, these replace the camera and filter checkboxes so hide them

            // Create the tree
            spectraSpec.clearTreeLeaves();
            spectraSpec.readHierarchyForInstrument(model.getInstrument().getDisplayName());
            panel.setCheckBoxTree(new CheckBoxTree(spectraSpec.getTreeModel()));

            // Place the tree in the panel
            panel.getHierarchicalSearchScrollPane().setViewportView(panel.getCheckBoxTree());
        }
    }

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
            searchParameters.setStartDate(date);
    }

    public void endSpinnerStateChanged(javax.swing.event.ChangeEvent evt)
    {
        Date date =
                ((SpinnerDateModel)panel.getEndSpinner().getModel()).getDate();
        if (date != null)
            searchParameters.setEndDate(date);

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
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
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
}
