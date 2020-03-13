package edu.jhuapl.sbmt.spectrum.controllers.standard;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.ProgressMonitor;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import com.jidesoft.swing.CheckBoxTree;
import com.jidesoft.utils.SwingWorker;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.listeners.SearchProgressListener;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchParametersModel;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.ui.search.SpectrumSearchParametersPanel;

/**
 * Controller to handle the Spectrum Search Parameters panel
 * @author steelrj1
 *
 */
public class SpectrumSearchParametersController<S extends BasicSpectrum>
{
    protected SpectrumSearchParametersPanel panel;
    protected BaseSpectrumSearchModel<S> model;
    protected PickManager pickManager;
    protected SpectraHierarchicalSearchSpecification<SpectrumSearchSpec> spectraSpec;
    private boolean hasHierarchicalSpectraSearch;
    private double imageSearchDefaultMaxSpacecraftDistance;
    private Date imageSearchDefaultStartDate;
    private Date imageSearchDefaultEndDate;
    private ModelManager modelManager;
    private SpectrumSearchParametersModel searchParameters;
    private ProgressMonitor searchProgressMonitor;
    private TreeSet<Integer> cubeList = null;
    private boolean isFixedListSearch = false;

    /**
     * @param imageSearchDefaultStartDate				The search start date
     * @param imageSearchDefaultEndDate					The search end date
     * @param hasHierarchicalSpectraSearch				Boolean describing if hierarchical search is enabled
     * @param imageSearchDefaultMaxSpacecraftDistance	The default max spacecraft distance
     * @param spectraSpec								The spectrum metadata
     * @param model										The spectrum search model
     * @param pickManager								The System pick manager
     * @param modelManager								The system model manager
     */
    public SpectrumSearchParametersController(Date imageSearchDefaultStartDate, Date imageSearchDefaultEndDate, boolean hasHierarchicalSpectraSearch, double imageSearchDefaultMaxSpacecraftDistance, SpectraHierarchicalSearchSpecification<SpectrumSearchSpec> spectraSpec, BaseSpectrumSearchModel<S> model, PickManager pickManager, ModelManager modelManager)
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

    /**
     * Sets up the panel components after initialization
     */
    public void setupSearchParametersPanel()
    {

        if(hasHierarchicalSpectraSearch)
        {
        	spectraSpec.clearTreeLeaves();
            spectraSpec.readHierarchyForInstrument(model.getInstrument().getDisplayName());
            panel.setCheckBoxTree(new CheckBoxTree(spectraSpec.getTreeModel()));

            // Place the tree in the panel
            panel.getHierarchicalSearchScrollPane().setViewportView(panel.getCheckBoxTree());

            spectraSpec.processTreeSelections(
                    panel.getCheckBoxTree().getCheckBoxTreeSelectionModel().getSelectionPaths());
            panel.getSelectRegionButton().setVisible(false);
            panel.getClearRegionButton().setVisible(false);
        }
        else
        {
            panel.getSelectRegionButton().setVisible(true);
            panel.getClearRegionButton().setVisible(true);
            panel.getClearRegionButton().addActionListener(evt -> clearRegionButtonActionPerformed());

            JSpinner startSpinner = panel.getStartSpinner();
            startSpinner.setModel(new SpinnerDateModel(searchParameters.getStartDate(), null, null, Calendar.DAY_OF_MONTH));
            startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
            startSpinner.setMinimumSize(new Dimension(36, 22));
            startSpinner.setPreferredSize(new Dimension(180, 22));
            startSpinner.addChangeListener(evt -> startSpinnerStateChanged());

            panel.getEndDateLabel().setText("End Date:");
            JSpinner endSpinner = panel.getEndSpinner();
            endSpinner.setModel(new SpinnerDateModel(searchParameters.getEndDate(), null, null, Calendar.DAY_OF_MONTH));
            endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
            endSpinner.setMinimumSize(new Dimension(36, 22));
            endSpinner.setPreferredSize(new Dimension(180, 22));
            endSpinner.addChangeListener(evt -> endSpinnerStateChanged());

            panel.addComponentListener(new ComponentAdapter() {
                public void componentHidden(java.awt.event.ComponentEvent evt) {
                    formComponentHidden();
                }
            });

            DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory(new NumberFormatter(new java.text.DecimalFormat("#0.###")));
            Dimension textFieldPreferredDimension = new Dimension(0, 22);

            JFormattedTextField toPhaseTextField = panel.getToPhaseTextField();
            toPhaseTextField.setFormatterFactory(formatterFactory);
            toPhaseTextField.setText("180");
            toPhaseTextField.setPreferredSize(textFieldPreferredDimension);

            JFormattedTextField fromPhaseTextField = panel.getFromPhaseTextField();
            fromPhaseTextField.setFormatterFactory(formatterFactory);
            fromPhaseTextField.setText("0");
            fromPhaseTextField.setPreferredSize(textFieldPreferredDimension);

            JFormattedTextField toEmissionTextField = panel.getToEmissionTextField();
            toEmissionTextField.setFormatterFactory(formatterFactory);
            toEmissionTextField.setText("180");
            toEmissionTextField.setPreferredSize(textFieldPreferredDimension);

            JFormattedTextField fromEmissionTextField = panel.getFromEmissionTextField();
            fromEmissionTextField.setFormatterFactory(formatterFactory);
            fromEmissionTextField.setText("0");
            fromEmissionTextField.setPreferredSize(textFieldPreferredDimension);

            JFormattedTextField toIncidenceTextField = panel.getToIncidenceTextField();
            toIncidenceTextField.setFormatterFactory(formatterFactory);
            toIncidenceTextField.setText("180");
            toIncidenceTextField.setPreferredSize(textFieldPreferredDimension);

            JFormattedTextField fromIncidenceTextField = panel.getFromIncidenceTextField();
            fromIncidenceTextField.setFormatterFactory(formatterFactory);
            fromIncidenceTextField.setText("0");
            fromIncidenceTextField.setPreferredSize(textFieldPreferredDimension);

            JFormattedTextField toDistanceTextField = panel.getToDistanceTextField();
            toDistanceTextField.setFormatterFactory(formatterFactory);
            toDistanceTextField.setText("1000");
            toDistanceTextField.setPreferredSize(textFieldPreferredDimension);

            JFormattedTextField fromDistanceTextField = panel.getFromDistanceTextField();
            fromDistanceTextField.setFormatterFactory(formatterFactory);
            fromDistanceTextField.setText("0");
            fromDistanceTextField.setPreferredSize(textFieldPreferredDimension);

            searchParameters.setStartDate(imageSearchDefaultStartDate);
            ((SpinnerDateModel)startSpinner.getModel()).setValue(searchParameters.getStartDate());
            searchParameters.setEndDate(imageSearchDefaultEndDate);
            ((SpinnerDateModel)endSpinner.getModel()).setValue(searchParameters.getEndDate());

            panel.getFullCheckBox().addActionListener(evt -> searchParameters.addToPolygonsSelected(0));

            panel.getPartialCheckBox().addActionListener(evt -> searchParameters.addToPolygonsSelected(0));

            panel.getDegenerateCheckBox().addActionListener(evt -> searchParameters.addToPolygonsSelected(0));

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

            panel.getFromIncidenceTextField().addActionListener(evt -> searchParameters.setMinIncidenceQuery(Integer.parseInt(panel.getFromIncidenceTextField().getText())));
            panel.getToIncidenceTextField().addActionListener(evt -> searchParameters.setMaxIncidenceQuery(Integer.parseInt(panel.getToIncidenceTextField().getText())));
            panel.getFromEmissionTextField().addActionListener(evt -> searchParameters.setMinEmissionQuery(Integer.parseInt(panel.getFromEmissionTextField().getText())));
            panel.getToEmissionTextField().addActionListener(evt -> searchParameters.setMaxEmissionQuery(Integer.parseInt(panel.getToEmissionTextField().getText())));
            panel.getFromPhaseTextField().addActionListener(evt -> searchParameters.setMinPhaseQuery(Integer.parseInt(panel.getFromPhaseTextField().getText())));
            panel.getToPhaseTextField().addActionListener(evt -> searchParameters.setMaxPhaseQuery(Integer.parseInt(panel.getToPhaseTextField().getText())));

            toDistanceTextField.setValue(imageSearchDefaultMaxSpacecraftDistance);

        	setSearchParameters();

        }

        panel.getClearRegionButton().setText("Clear Region");
        panel.getClearRegionButton().addActionListener(evt -> clearRegionButtonActionPerformed());

        panel.getSubmitButton().setText("Search");
        panel.getSubmitButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	setSearchParameters();
                if (hasHierarchicalSpectraSearch)
                    model.setSelectedPath(panel.getCheckBoxTree().getCheckBoxTreeSelectionModel().getSelectionPaths());
                panel.getSelectRegionButton().setSelected(false);
                model.clearSpectraFromDisplay();
                pickManager.setActivePicker(pickManager.getPickerForPickMode(PickMode.DEFAULT));

                AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
                SmallBodyModel bodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
                if (selectionModel.getAllItems().size() > 0)
                {
               	  int numberOfSides = selectionModel.getNumberOfSides();
                    Ellipse region = selectionModel.getItem(0);

                    // Always use the lowest resolution model for getting the intersection cubes list.
                    // Therefore, if the selection region was created using a higher resolution model,
                    // we need to recompute the selection region using the low res model.
                    if (bodyModel.getModelResolution() > 0)
                    {
                        vtkPolyData interiorPoly = new vtkPolyData();
                        bodyModel.drawRegularPolygonLowRes(region.getCenter().toArray(), region.getRadius(), numberOfSides, interiorPoly, null);
                        cubeList = bodyModel.getIntersectingCubes(interiorPoly);
                    }
                    else
                    {
                        cubeList = bodyModel.getIntersectingCubes(selectionModel.getVtkInteriorPolyDataFor(region));
                    }
                }

                SwingWorker<Void, Void> searchTask = new SwingWorker<Void, Void>()
				{

					@Override
					protected Void doInBackground() throws Exception
					{
						model.performSearch(searchParameters, cubeList, hasHierarchicalSpectraSearch, spectraSpec, model.getSelectedPath(), new SearchProgressListener()
						{
							@Override
							public void searchStarted()
							{
								 searchProgressMonitor = new ProgressMonitor(null, "Performing Spectra Search...", "", 0, 100);
								 searchProgressMonitor.setMillisToDecideToPopup(0);
								 searchProgressMonitor.setMillisToPopup(0);
							     searchProgressMonitor.setProgress(0);
							}

							public void searchNoteUpdated(String note)
							{
								searchProgressMonitor.setNote(note);
							}

							@Override
							public void searchProgressChanged(int percentComplete)
							{
								searchProgressMonitor.setProgress(percentComplete);
							}

							@Override
							public void searchEnded()
							{
								searchProgressMonitor.setProgress(100);
							}

							@Override
							public void searchIndeterminate()
							{
								searchProgressMonitor = new ProgressMonitor(null, "Performing Spectra Search...", "", 0, 100);
								searchProgressMonitor.setMillisToDecideToPopup(0);
								searchProgressMonitor.setMillisToPopup(0);
								searchProgressMonitor.setProgress(99);
								searchProgressMonitor.setNote("Waiting for results");
							}
						});
						return null;
					}
				};
				searchTask.addPropertyChangeListener(new PropertyChangeListener()
				{

					@Override
					public void propertyChange(PropertyChangeEvent evt)
					{
						// TODO Auto-generated method stub

					}
				});
				searchTask.execute();
            }
        });


        panel.getSelectRegionButton().setText("Select Region");
        panel.getSelectRegionButton().addActionListener(evt -> selectRegionButtonActionPerformed());
    }

    private void setSearchParameters()
    {
    	if (panel.getStartSpinner() == null) return;
        if ((panel.getFilenameRadioButton() != null) && panel.getFilenameRadioButton().isSelected())
        {
        	searchParameters.setSearchByFilename(panel.getSearchByNumberTextField().getText());
        }

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

    /**
     * Used to clear the picker state if this tab disappears
     */
    private void formComponentHidden()
    {
        panel.getSelectRegionButton().setSelected(false);
        pickManager.setActivePicker(pickManager.getPickerForPickMode(PickMode.DEFAULT));
    }

    /**
     * Handles the change listener for the start date spinner
     */
    private void startSpinnerStateChanged()
    {
        Date date = ((SpinnerDateModel)panel.getStartSpinner().getModel()).getDate();
        if (date != null)
            searchParameters.setStartDate(date);
    }

    /**
     * Handles the change listener for the end date spinner
     */
    private void endSpinnerStateChanged()
    {
        Date date = ((SpinnerDateModel)panel.getEndSpinner().getModel()).getDate();
        if (date != null)
            searchParameters.setEndDate(date);
    }

    /**
     * Handles the action for the select region button
     */
    private void selectRegionButtonActionPerformed()
    {
    	//TODO Need to confirm if this new way of doing things is correct
    	if (panel.getSelectRegionButton().isSelected()) pickManager.setActivePicker(pickManager.getPickerForPickMode(PickMode.CIRCLE_SELECTION));
    	else pickManager.setActivePicker(pickManager.getPickerForPickMode(PickMode.DEFAULT));
    }

    /**
     * Handles the action for the clear region button
     */
    private void clearRegionButtonActionPerformed()
    {
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        selectionModel.removeAllStructures();
    }

    /**
     * Returns the panel component of the controller so it can be displayed in a container view
     * @return
     */
    public SpectrumSearchParametersPanel getPanel()
    {
        return panel;
    }

    public boolean isFixedListSearch()
    {
    	return isFixedListSearch;
    }


    public void setFixedListSearch(boolean isFixedListSearch)
    {
    	this.isFixedListSearch = isFixedListSearch;
    	panel.setFixedListSearch(isFixedListSearch);
    }
}
