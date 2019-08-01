//package edu.jhuapl.sbmt.spectrum.controllers;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Locale;
//import java.util.Set;
//import java.util.TimeZone;
//import java.util.TreeSet;
//
//import javax.swing.DefaultListCellRenderer;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JList;
//import javax.swing.JOptionPane;
//import javax.swing.JSpinner;
//import javax.swing.SpinnerDateModel;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
//
//import org.apache.commons.io.FilenameUtils;
//import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
//
//import com.jidesoft.swing.CheckBoxTree;
//
//import vtk.vtkActor;
//import vtk.vtkCubeSource;
//import vtk.vtkFunctionParser;
//import vtk.vtkPolyData;
//import vtk.vtkPolyDataNormals;
//
//import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
//import edu.jhuapl.saavtk.model.Model;
//import edu.jhuapl.saavtk.model.ModelManager;
//import edu.jhuapl.saavtk.model.ModelNames;
//import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
//import edu.jhuapl.saavtk.model.structure.EllipsePolygon;
//import edu.jhuapl.saavtk.pick.PickEvent;
//import edu.jhuapl.saavtk.pick.PickManager;
//import edu.jhuapl.saavtk.pick.PickManager.PickMode;
//import edu.jhuapl.saavtk.util.BoundingBox;
//import edu.jhuapl.saavtk.util.FileUtil;
//import edu.jhuapl.saavtk.util.IdPair;
//import edu.jhuapl.saavtk.util.PolyDataUtil;
//import edu.jhuapl.saavtk.util.Properties;
//import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
//import edu.jhuapl.sbmt.client.SmallBodyModel;
//import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
//import edu.jhuapl.sbmt.model.bennu.SearchSpec;
//import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
//import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.BoundedObjectHyperTreeSkeleton;
//import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.HyperBoundedObject;
//import edu.jhuapl.sbmt.spectrum.model.core.AbstractSpectrumSearchModel;
//import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
//import edu.jhuapl.sbmt.spectrum.model.core.SpectrumListIO;
//import edu.jhuapl.sbmt.spectrum.model.core.SpectrumStandardSearch;
//import edu.jhuapl.sbmt.spectrum.model.core.simplified.SpectrumSearchParametersModel;
//import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectraSearchDataCollection;
//import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectrumHypertreeSearch;
//import edu.jhuapl.sbmt.spectrum.model.rendering.IBasicSpectrumRenderer;
//import edu.jhuapl.sbmt.spectrum.model.rendering.SpectraCollection;
//import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
//import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
//import edu.jhuapl.sbmt.spectrum.ui.SpectrumBrowseView;
//import edu.jhuapl.sbmt.spectrum.ui.SpectrumMathPanel;
//import edu.jhuapl.sbmt.spectrum.ui.SpectrumPopupMenu;
//import edu.jhuapl.sbmt.spectrum.ui.SpectrumSearchView;
//import edu.jhuapl.sbmt.spectrum.ui.SpectrumView;
//import edu.jhuapl.sbmt.util.TimeUtil;
//
//public abstract class AbstractSpectrumSearchController implements PropertyChangeListener, MouseListener, KeyListener
//{
//    protected SpectrumView view;
//    protected AbstractSpectrumSearchModel model;
//    PickEvent lastPickEvent=null;
//    protected CheckBoxTree checkBoxTree;
//    protected final ISpectralInstrument instrument;
//    SpectraHierarchicalSearchSpecification<?> spectraSpec;
//    private boolean isSearchView;
//    private boolean hasHierarchicalSpectraSearch;
//    private SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification;
//    private boolean hasHypertreeBasedSpectraSearch;
//    protected SpectraCollection collection;
//
//    public AbstractSpectrumSearchController(boolean hasHierarchicalSpectraSearch, SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification,
//    		boolean hasHypertreeBasedSpectraSearch,
//    		final ModelManager modelManager,
//            SbmtInfoWindowManager infoPanelManager,
//            final PickManager pickManager, final Renderer renderer, AbstractSpectrumSearchModel model, ISpectralInstrument instrument, boolean search)
//    {
//        this.modelManager = modelManager;
//        this.collection = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
//
//        this.model = model;
//        this.instrument=instrument;
//        this.isSearchView = search;
//        if (search)
//            this.view = new SpectrumSearchView(modelManager, pickManager, renderer, instrument, model);
//        else
//            this.view = new SpectrumBrowseView(modelManager, pickManager, renderer, instrument);
//        SpectrumPopupMenu popup = new SpectrumPopupMenu(collection, modelManager, infoPanelManager, renderer);
//        popup.setInstrument(instrument);
//        view.setSpectrumPopupMenu(popup);
//        view.getSpectrumPopupMenu().addPropertyChangeListener(this);
//        spectraSpec = hierarchicalSpectraSearchSpecification;
//
//        if (!search)
//            initHierarchicalImageSearch(); // only set up hierarchical search on browse pane
//
//        initComponents();
//
//        pickManager.getDefaultPicker().addPropertyChangeListener(this);
//
//        renderer.addKeyListener(this);
//        model.setRenderer(renderer);
//    }
//
//    private void initComponents()
//    {
//
//        view.getSaveSpectraListButton().addActionListener(new ActionListener()
//        {
//
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                saveSpectrumListButtonActionPerformed(e);
//            }
//        });
//
//        view.getLoadSpectraListButton().addActionListener(new ActionListener()
//        {
//
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                loadSpectrumListButtonActionPerformed(e);
//
//            }
//        });
//
//        view.addComponentListener(new ComponentAdapter() {
//            public void componentHidden(ComponentEvent evt) {
//                formComponentHidden(evt);
//            }
//        });
//
//        if(hasHierarchicalSpectraSearch)
//        {
//            if (!isSearchView)
//            	hierarchicalSpectraSearchSpecification.processTreeSelections(
//                    checkBoxTree.getCheckBoxTreeSelectionModel().getSelectionPaths());
//        }
//
//        view.getClearRegionButton().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                clearRegionButtonActionPerformed(evt);
//            }
//        });
//
//        view.getSubmitButton().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                submitButtonActionPerformed(evt);
//            }
//        });
//
//        view.getSelectRegionButton().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                selectRegionButtonActionPerformed(evt);
//            }
//        });
//
//        if (view instanceof SpectrumSearchView) {
//            ((SpectrumSearchView) view).getStartSpinner().addChangeListener(new ChangeListener() {
//                public void stateChanged(ChangeEvent evt) {
//                    startSpinnerStateChanged(evt);
//                }
//            });
//
//            ((SpectrumSearchView) view).getEndSpinner().addChangeListener(new ChangeListener() {
//                public void stateChanged(ChangeEvent evt) {
//                    endSpinnerStateChanged(evt);
//                }
//            });
//        }
//
//        view.getResultList().addMouseListener(new MouseAdapter() {
//            public void mousePressed(MouseEvent evt) {
//                resultListMousePressed(evt);
//            }
//            public void mouseReleased(MouseEvent evt) {
//                resultListMouseReleased(evt);
//            }
//        });
//        view.getResultsScrollPanel().setViewportView(view.getResultList());
//
//        view.getNumberOfFootprintsComboBox().setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220", "230", "240", "250", " " }));
//
//        view.getNumberOfFootprintsComboBox().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                numberOfFootprintsComboBoxActionPerformed(evt);
//            }
//        });
//
//        view.getPrevButton().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                prevButtonActionPerformed(evt);
//            }
//        });
//
//        view.getNextButton().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                nextButtonActionPerformed(evt);
//            }
//        });
//
//        view.getRemoveAllFootprintsButton().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                removeAllFootprintsButtonActionPerformed(evt);
//            }
//        });
//
//        view.getRemoveAllBoundariesButton().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                removeAllBoundariesButtonActionPerformed(evt);
//            }
//        });
//
//        view.getColoringComboBox().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                coloringComboBoxActionPerformed(evt);
//            }
//        });
//
//        view.getRedComboBox().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                redComboBoxActionPerformed(evt);
//            }
//        });
//
//        view.getRedMaxSpinner().addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent evt) {
//                redMaxSpinnerStateChanged(evt);
//            }
//        });
//
//        view.getRedMinSpinner().addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent evt) {
//                redMinSpinnerStateChanged(evt);
//            }
//        });
//
//        view.getGreenComboBox().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                greenComboBoxActionPerformed(evt);
//            }
//        });
//
//        view.getGreenMaxSpinner().addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent evt) {
//                greenMaxSpinnerStateChanged(evt);
//            }
//        });
//
//        view.getGreenMinSpinner().addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent evt) {
//                greenMinSpinnerStateChanged(evt);
//            }
//        });
//
//        view.getBlueComboBox().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                blueComboBoxActionPerformed(evt);
//            }
//        });
//
//        view.getBlueMaxSpinner().addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent evt) {
//                blueMaxSpinnerStateChanged(evt);
//            }
//        });
//
//        view.getBlueMinSpinner().addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent evt) {
//                blueMinSpinnerStateChanged(evt);
//            }
//        });
//
//        view.getGrayscaleCheckBox().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                grayscaleCheckBoxActionPerformed(evt);
//            }
//        });
//
//        view.getCustomFunctionsButton().addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                customFunctionsButtonActionPerformed(evt);
//            }
//        });
//
//        view.getResultList().setCellRenderer(new MyListCellRenderer());
//
//        view.getResultList().addListSelectionListener(new ListSelectionListener()
//        {
//
//            @Override
//            public void valueChanged(ListSelectionEvent e)
//            {
//                if (e.getValueIsAdjusting())
//                    return;
//                for (int i=0; i<view.getResultList().getModel().getSize(); i++)
//                {
////                    Spectrum spectrum=collection.getSpectrum(createSpectrumName((String)view.getResultList().getModel().getElementAt(i)));
//                    IBasicSpectrumRenderer spectrum=collection.getSpectrum(createSpectrumName(i));
//                    if (spectrum == null)
//                        continue;
//                    if (view.getResultList().isSelectedIndex(i))
//                        collection.select(spectrum);
//                    else
//                        collection.deselect(spectrum);
//                }
//            }
//        });
//    }
//
//    private void formComponentHidden(ComponentEvent evt)
//    {
//        if (view instanceof SpectrumSearchView) {
//            ((SpectrumSearchView)view).getSelectRegionButton().setSelected(false);
//        }
//        model.getPickManager().setPickMode(PickMode.DEFAULT);
//    }
//
//    private void startSpinnerStateChanged(ChangeEvent evt)
//    {
//        if (view instanceof SpectrumSearchView) {
//            Date date = ((SpinnerDateModel)((SpectrumSearchView)view).getStartSpinner().getModel()).getDate();
//        	if (date != null)
//            	model.getSearchParameters().setStartDate(date);
//    	}
//    }
//
//    private void endSpinnerStateChanged(ChangeEvent evt)
//    {
//        if (view instanceof SpectrumSearchView) {
//            Date date = ((SpinnerDateModel)((SpectrumSearchView)view).getEndSpinner().getModel()).getDate();
//        	if (date != null)
//            	model.getSearchParameters().setEndDate(date);
//   	 	}
//    }
//
//    private void selectRegionButtonActionPerformed(ActionEvent evt)
//    {
//        if (view instanceof SpectrumSearchView) {
//            if ( ((SpectrumSearchView)view).getSelectRegionButton().isSelected())
//            	model.getPickManager().setPickMode(PickMode.CIRCLE_SELECTION);
//        	else
//            	model.getPickManager().setPickMode(PickMode.DEFAULT);
//    	}
//    }
//
//    private void clearRegionButtonActionPerformed(ActionEvent evt)
//    {
//        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
//        selectionModel.removeAllStructures();
//    }
//
//    private void prevButtonActionPerformed(ActionEvent evt)
//    {
//        IdPair shown = model.getResultIntervalCurrentlyShown();
//        collection.deselectAll();
//        if (shown == null) return;
//
//        // Only get the prev block if there's something left to show.
//        if (shown.id1 <= 0) return;
//
//        shown.prevBlock(Integer.parseInt((String)view.getNumberOfFootprintsComboBox().getSelectedItem()));
//        showFootprints(shown);
//    }
//
//    private void nextButtonActionPerformed(ActionEvent evt)
//    {
//        IdPair shown = model.getResultIntervalCurrentlyShown();
//        collection.deselectAll();
//
//        if (shown != null)
//        {
//            // Only get the next block if there's something left to show.
//            if (shown.id2 < view.getResultList().getModel().getSize())
//            {
//                shown.nextBlock(Integer.parseInt((String)view.getNumberOfFootprintsComboBox().getSelectedItem()));
//                showFootprints(shown);
//            }
//        }
//        else
//        {
//            model.setResultIntervalCurrentlyShown(new IdPair(0, Integer.parseInt((String)view.getNumberOfFootprintsComboBox().getSelectedItem())));
//            shown = model.getResultIntervalCurrentlyShown();
//            showFootprints(shown);
//        }
//    }
//
//    private void removeAllFootprintsButtonActionPerformed(ActionEvent evt)
//    {
//        collection.removeAllSpectraForInstrument(instrument);
//        model.setResultIntervalCurrentlyShown(null);
//    }
//
//    private void removeAllBoundariesButtonActionPerformed(ActionEvent evt)
//    {
//        collection.deselectAll();
//        model.setResultIntervalCurrentlyShown(null);
//    }
//
//    protected TreeSet<Integer> cubeList = null;
//    private ModelManager modelManager;
//    private SpectraSearchDataCollection spectraModel;
//
//    private void submitButtonActionPerformed(ActionEvent evt)
//    {
//        List<Integer> productsSelected;
//        List<BasicSpectrum> results = new ArrayList<BasicSpectrum>();
//
//        if (view instanceof SpectrumSearchView)
//        { // only submitting for searches
//        try
//        {
//        	if (hasHypertreeBasedSpectraSearch)
//        	{
//        		SpectrumSearchParametersModel searchParameters = model.getSearchParameters();
//        		double startTime = searchParameters.getStartDate().getTime();
//                double endTime = searchParameters.getEndDate().getTime();
//                String spectraDatasourceName = model.getSpectraHypertreeSourceName();
//
//                this.spectraModel = (SpectraSearchDataCollection) modelManager
//                        .getModel(ModelNames.SPECTRA_HYPERTREE_SEARCH);
//                String spectraDatasourcePath = spectraModel
//                        .getSpectraDataSourceMap().get(spectraDatasourceName);
//
//                spectraModel.addDatasourceSkeleton(spectraDatasourceName,
//                        spectraDatasourcePath);
//                spectraModel
//                        .setCurrentDatasourceSkeleton(spectraDatasourceName);
//                spectraModel.readSkeleton();
//                BoundedObjectHyperTreeSkeleton skeleton = (BoundedObjectHyperTreeSkeleton) spectraModel
//                        .getCurrentSkeleton();
//
//                double[] selectionRegionCenter = null;
//                double selectionRegionRadius = 0.0;
//
//                AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) modelManager
//                        .getModel(ModelNames.CIRCLE_SELECTION);
//                SmallBodyModel smallBodyModel = (SmallBodyModel) modelManager
//                        .getModel(ModelNames.SMALL_BODY);
//                EllipsePolygon region = null;
//                vtkPolyData interiorPoly = new vtkPolyData();
//                if (selectionModel.getNumberOfStructures() > 0)
//                {
//                    region = (EllipsePolygon) selectionModel
//                            .getStructure(0);
//                    selectionRegionCenter = region.getCenter();
//                    selectionRegionRadius = region.radius;
//
//                    // Always use the lowest resolution model for getting the
//                    // intersection cubes list.
//                    // Therefore, if the selection region was created using a
//                    // higher resolution model,
//                    // we need to recompute the selection region using the low
//                    // res model.
//                    if (smallBodyModel.getModelResolution() > 0)
//                        smallBodyModel.drawRegularPolygonLowRes(selectionRegionCenter,
//                                region.radius, region.numberOfSides,
//                                interiorPoly, null); // this sets interiorPoly
//                    else
//                        interiorPoly = region.interiorPolyData;
//
//                }
//                else
//                {
//                    vtkCubeSource box = new vtkCubeSource();
//                    double[] bboxBounds = smallBodyModel.getBoundingBox()
//                            .getBounds();
//                    BoundingBox bbox = new BoundingBox(bboxBounds);
//                    bbox.increaseSize(0.01);
//                    box.SetBounds(bbox.getBounds());
//                    box.Update();
//                    interiorPoly.DeepCopy(box.GetOutput());
//                }
//
//                Set<String> files = new HashSet<String>();
//                HashMap<String, HyperBoundedObject> fileSpecMap = new HashMap<String, HyperBoundedObject>();
//                double[] times = new double[] { startTime, endTime };
//                double[] spectraLims = new double[] { searchParameters.getMinEmissionQuery(), searchParameters.getMaxEmissionQuery(),
//                		searchParameters.getMinIncidenceQuery(), searchParameters.getMaxIncidenceQuery(), searchParameters.getMinPhaseQuery(), searchParameters.getMaxPhaseQuery(),
//                		searchParameters.getMinDistanceQuery(), searchParameters.getMaxDistanceQuery() };
//                double[] bounds = interiorPoly.GetBounds();
//                TreeSet<Integer> cubeList = ((SpectraSearchDataCollection) spectraModel)
//                        .getLeavesIntersectingBoundingBox(
//                                new BoundingBox(bounds), times, spectraLims);
//                HyperBox hbb = new HyperBox(
//                        new double[] { bounds[0], bounds[2], bounds[4],
//                                times[0], spectraLims[0], spectraLims[2],
//                                spectraLims[4], spectraLims[6] },
//                        new double[] { bounds[1], bounds[3], bounds[5],
//                                times[1], spectraLims[1], spectraLims[3],
//                                spectraLims[5], spectraLims[7] });
//        		SpectrumHypertreeSearch search = new SpectrumHypertreeSearch(collection, model.getSearchParameters(), model.getSpectraHypertreeDataSpecName(), hasHypertreeBasedSpectraSearch, spectraSpec);
//        		results = search.search(cubeList, skeleton, hbb, instrument);
//        	}
//        	else
//        	{
//        		SpectrumStandardSearch search = new SpectrumStandardSearch(collection, model.getSearchParameters(), hasHierarchicalSpectraSearch, spectraSpec);
//            	results = search.search(instrument, null);
//        	}
//
//
////             ((SpectrumSearchView)view).getSelectRegionButton().setSelected(false);
////           	 model.getPickManager().setPickMode(PickMode.DEFAULT);
////
////            GregorianCalendar startDateGreg = new GregorianCalendar();
////            GregorianCalendar endDateGreg = new GregorianCalendar();
////            startDateGreg.setTime(model.getSearchParameters().getStartDate());
////            endDateGreg.setTime(model.getSearchParameters().getEndDate());
////                double startTime = model.getSearchParameters().getStartDate().getTime();
////                double endTime = model.getSearchParameters().getEndDate().getTime();
////
////            DateTime startDateJoda = new DateTime(
////                    startDateGreg.get(GregorianCalendar.YEAR),
////                    startDateGreg.get(GregorianCalendar.MONTH)+1,
////                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
////                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
////                    startDateGreg.get(GregorianCalendar.MINUTE),
////                    startDateGreg.get(GregorianCalendar.SECOND),
////                    startDateGreg.get(GregorianCalendar.MILLISECOND),
////                    DateTimeZone.UTC);
////            DateTime endDateJoda = new DateTime(
////                    endDateGreg.get(GregorianCalendar.YEAR),
////                    endDateGreg.get(GregorianCalendar.MONTH)+1,
////                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
////                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
////                    endDateGreg.get(GregorianCalendar.MINUTE),
////                    endDateGreg.get(GregorianCalendar.SECOND),
////                    endDateGreg.get(GregorianCalendar.MILLISECOND),
////                    DateTimeZone.UTC);
////
////
////                if (hasHypertreeBasedSpectraSearch)
////                {
////
////                    String spectraDatasourceName = "";
////                    if (instrument.getDataTypeNames().length > 0)
////                    {
////                    	if (((SpectrumSearchView)view).getL2Button().isSelected()) // either L2 or L3 for OTES
////                            spectraDatasourceName = instrument.getDisplayName() + "_" + instrument.getDataTypeNames()[0];
////                        else
////                            spectraDatasourceName = instrument.getDisplayName() + "_" + instrument.getDataTypeNames()[1].replaceAll("/", "");
////                    }
////
////                    this.spectraModel = (SpectraSearchDataCollection)modelManager.getModel(ModelNames.SPECTRA_HYPERTREE_SEARCH);
////                    String spectraDatasourcePath = spectraModel.getSpectraDataSourceMap().get(spectraDatasourceName);
////
////                    spectraModel.addDatasourceSkeleton(spectraDatasourceName, spectraDatasourcePath);
////                    spectraModel.setCurrentDatasourceSkeleton(spectraDatasourceName);
////                    spectraModel.readSkeleton();
////                    BoundedObjectHyperTreeSkeleton skeleton = (BoundedObjectHyperTreeSkeleton) spectraModel.getCurrentSkeleton();
////
////
////                    double[] selectionRegionCenter = null;
////                    double selectionRegionRadius = 0.0;
////
////                    AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
////                    SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
////                    EllipsePolygon region=null;
////                    vtkPolyData interiorPoly=new vtkPolyData();
////            		if (selectionModel.getNumberOfStructures() > 0)
////            		{
////                        region=(EllipsePolygon)selectionModel.getStructure(0);
////                        selectionRegionCenter = region.getCenter();
////                        selectionRegionRadius = region.radius;
////
////
////                // Always use the lowest resolution model for getting the intersection cubes list.
////                // Therefore, if the selection region was created using a higher resolution model,
////                // we need to recompute the selection region using the low res model.
////                        if (smallBodyModel.getModelResolution() > 0)
////                            smallBodyModel.drawRegularPolygonLowRes(selectionRegionCenter, region.radius, region.numberOfSides, interiorPoly, null);    // this sets interiorPoly
////                        else
////                            interiorPoly=region.interiorPolyData;
////
////                    }
////                    else
////                    {
////                        vtkCubeSource box=new vtkCubeSource();
////                        double[] bboxBounds=smallBodyModel.getBoundingBox().getBounds();
////                        BoundingBox bbox=new BoundingBox(bboxBounds);
////                        bbox.increaseSize(0.01);
////                        box.SetBounds(bbox.getBounds());
////                        box.Update();
////                        interiorPoly.DeepCopy(box.GetOutput());
////                    }
////
////                    Set<String> files = new HashSet<String>();
////                    HashMap<String, HyperBoundedObject> fileSpecMap = new HashMap<String, HyperBoundedObject>();
////                    double[] times = new double[] {startTime, endTime};
////                    double[] spectraLims = new double[] {
////                            Double.valueOf(((SpectrumSearchView)view).getFromEmissionTextField().getText()), Double.valueOf(((SpectrumSearchView)view).getToEmissionTextField().getText()),
////                            Double.valueOf(((SpectrumSearchView)view).getFromIncidenceTextField().getText()), Double.valueOf(((SpectrumSearchView)view).getToIncidenceTextField().getText()),
////                            Double.valueOf(((SpectrumSearchView)view).getFromPhaseTextField().getText()), Double.valueOf(((SpectrumSearchView)view).getToPhaseTextField().getText()),
////                            Double.valueOf(((SpectrumSearchView)view).getFromDistanceTextField().getText()), Double.valueOf(((SpectrumSearchView)view).getToDistanceTextField().getText())};
////                    double[] bounds = interiorPoly.GetBounds();
////                    TreeSet<Integer> cubeList=((SpectraSearchDataCollection)spectraModel).getLeavesIntersectingBoundingBox(new BoundingBox(bounds), times, spectraLims);
////                    HyperBox hbb = new HyperBox(new double[]{bounds[0], bounds[2], bounds[4], times[0], spectraLims[0], spectraLims[2], spectraLims[4], spectraLims[6]},
////                            new double[]{bounds[1], bounds[3], bounds[5], times[1], spectraLims[1], spectraLims[3], spectraLims[5], spectraLims[7]});
////
////                    for (Integer cubeid : cubeList)
////                    {
////                        Node currNode = skeleton.getNodeById(cubeid);
////                        Path path = currNode.getPath();
////                        Path dataPath = path.resolve("data");
////                        DataInputStream instream= new DataInputStream(new BufferedInputStream(new FileInputStream(FileCache.getFileFromServer(dataPath.toString()))));
////                        try
////                        {
////                            while (instream.available() > 0) {
////                                HyperBoundedObject obj = BoundedObjectHyperTreeNode.createNewBoundedObject(instream, 8);
////                                int fileNum = obj.getFileNum();
////                                Map<Integer, String> fileMap = skeleton.getFileMap();
////                                String file = fileMap.get(fileNum);
////                                if (files.add(file)) {
////                                    fileSpecMap.put(file, obj);
////                                }
////                            }
////                        }
////                        catch (IOException e)
////                        {
////                            // TODO Auto-generated catch block
////                            e.printStackTrace();
////                        }
////
////                    }
////
////                    for (String file : files) {
////                        System.out.println(file);
////                    }
////
////                    ArrayList<String> intFiles = new ArrayList<String>();
////
////                    // NOW CHECK WHICH SPECTRA ACTUALLY INTERSECT REGION
////                    for (String fi : files) {
////                        HyperBoundedObject spec = fileSpecMap.get(fi);
////                        HyperBox bbox = spec.getBbox();
////                        try
////                        {
////                            if (hbb.intersects(bbox)) {
////                                intFiles.add(fi);
////                            }
////                        }
////                        catch (HyperException e)
////                        {
////                            // TODO Auto-generated catch block
////                            e.printStackTrace();
////                        }
////                    }
////
////
////                    // final list of spectra that intersect region
////                    // create a list of lists for the reults
////                    List<BasicSpectrum> listoflist = new ArrayList<BasicSpectrum>(intFiles.size());
////                    System.out.println("SPECTRA THAT INTERSECT SEARCH REGION: ");
////                    for (String file : intFiles) {
////                        System.out.println(file);
////                        ArrayList<String> currList = new ArrayList<String>();
////                        currList.add(file);
////                        listoflist.add(currList);
////                    }
////                    results = listoflist;
////
////                }
////                else
////                {
////                    IQueryBase queryType = instrument.getQueryBase();
////                    if (queryType instanceof FixedListQuery)
////                    {
////                        FixedListQuery query = (FixedListQuery)queryType;
////                        results = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist.txt", "spectra", query.getRootPath(), ImageSource.CORRECTED_SPICE)).getResultlist();
////	                }
////	                else
////	                {
////                        SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
////                                Ranges.closed(Double.valueOf(((SpectrumSearchView)view).getFromDistanceTextField().getText()), Double.valueOf(((SpectrumSearchView)view).getToDistanceTextField().getText())),
////                                "", null,   //TODsteelrj1O: reinstate polygon types here
////                                Ranges.closed(Double.valueOf(((SpectrumSearchView)view).getFromIncidenceTextField().getText()), Double.valueOf(((SpectrumSearchView)view).getToIncidenceTextField().getText())),
////                                Ranges.closed(Double.valueOf(((SpectrumSearchView)view).getFromEmissionTextField().getText()), Double.valueOf(((SpectrumSearchView)view).getToEmissionTextField().getText())),
////                                Ranges.closed(Double.valueOf(((SpectrumSearchView)view).getFromPhaseTextField().getText()), Double.valueOf(((SpectrumSearchView)view).getToPhaseTextField().getText())),
////                                cubeList);
////
////                        DatabaseQueryBase query = (DatabaseQueryBase)queryType;
////                        results = query.runQuery(searchMetadata).getResultlist();
////	                }
////                }
//                setSpectrumSearchResults(results);
//
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//                System.out.println(e);
//                return;
//            }
//        }
//        else if (view instanceof SpectrumBrowseView)
//        {
//        	SpectrumStandardSearch search = new SpectrumStandardSearch(collection, model.getSearchParameters(), hasHierarchicalSpectraSearch, spectraSpec);
//        	search.search(instrument, null);
//
////            if(hasHierarchicalSpectraSearch)
////            {
////                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
////                // Process the user's selections
////                hierarchicalSpectraSearchSpecification.processTreeSelections(
////                        checkBoxTree.getCheckBoxTreeSelectionModel().getSelectionPaths());
////
////                // Get the selected (camera,filter) pairs
////
////                productsSelected = spectraSpec.getSelectedDatasets();
////                InstrumentMetadata<? extends SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
////                TreeModel tree = spectraSpec.getTreeModel();
////                List<? extends SearchSpec> specs = instrumentMetadata.getSpecs();
////                for (Integer selected : productsSelected)
////                {
////                    String name = tree.getChild(tree.getRoot(), selected).toString();
////                    SearchSpec spec = specs.get(selected);
////                    FixedListSearchMetadata searchMetadata = FixedListSearchMetadata.of(spec.getDataName(),
////                                                                                        spec.getDataListFilename(),
////                                                                                        spec.getDataPath(),
////                                                                                        spec.getDataRootLocation(),
////                                                                                        spec.getSource());
////
////                    List<List<String>> thisResult = instrument.getQueryBase().runQuery(searchMetadata).getResultlist();
////                    collection.tagSpectraWithMetadata(thisResult, spec);
////                    results.addAll(thisResult);
////                }
////                setSpectrumSearchResults(results);
////            }
//        }
//    }
//
//    protected double[] getSelectedTimeLimits()
//    {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
//        double start = TimeUtil.str2et(sdf.format(model.getSearchParameters().getStartDate()).replace(' ', 'T'));
//        double end = TimeUtil.str2et(sdf.format(model.getSearchParameters().getEndDate()).replace(' ', 'T'));
//        return new double[]{start,end};
//    }
//
//    private void resultListMousePressed(MouseEvent evt)
//    {
//        maybeShowPopup(evt);
//    }
//
//    private void resultListMouseReleased(MouseEvent evt)
//    {
//        maybeShowPopup(evt);
//    }
//
//    private void coloringComboBoxActionPerformed(ActionEvent evt)
//    {
//        JComboBox box = (JComboBox)evt.getSource();
//        String coloringName = box.getSelectedItem().toString();
//        SpectrumColoringStyle style = SpectrumColoringStyle.getStyleForName(coloringName);
//        collection.setColoringStyleForInstrument(style, instrument);
//        view.getResultList().repaint();
//
//        boolean isEmissionSelected = (style == SpectrumColoringStyle.EMISSION_ANGLE);
//        view.getRgbColoringPanel().setVisible(!isEmissionSelected);
//        view.getEmissionAngleColoringPanel().setVisible(isEmissionSelected);
//    }
//
//    private void redComboBoxActionPerformed(ActionEvent evt) {
//        updateColoring();
//    }
//
//    private void greenComboBoxActionPerformed(ActionEvent evt) {
//        updateColoring();
//    }
//
//    private void blueComboBoxActionPerformed(ActionEvent evt) {
//        updateColoring();
//    }
//
//    private void redMinSpinnerStateChanged(ChangeEvent evt) {
//        checkValidMinMax(0, true);
//        updateColoring();
//    }
//
//    private void greenMinSpinnerStateChanged(ChangeEvent evt) {
//        checkValidMinMax(1, true);
//        updateColoring();
//    }
//
//    private void blueMinSpinnerStateChanged(ChangeEvent evt) {
//        checkValidMinMax(2, true);
//        updateColoring();
//    }
//
//    private void redMaxSpinnerStateChanged(ChangeEvent evt) {
//        checkValidMinMax(0, false);
//        updateColoring();
//    }
//
//    private void greenMaxSpinnerStateChanged(ChangeEvent evt) {
//        checkValidMinMax(1, false);
//        updateColoring();
//    }
//
//    private void blueMaxSpinnerStateChanged(ChangeEvent evt) {
//        checkValidMinMax(2, false);
//        updateColoring();
//    }
//
//    private void grayscaleCheckBoxActionPerformed(ActionEvent evt)
//    {
//        boolean enableColor = !view.getGrayscaleCheckBox().isSelected();
//
//        view.getGreenComboBox().setVisible(enableColor);
//        view.getGreenMinSpinner().setVisible(enableColor);
//        view.getGreenMaxSpinner().setVisible(enableColor);
//        view.getBlueComboBox().setVisible(enableColor);
//        view.getBlueMinSpinner().setVisible(enableColor);
//        view.getBlueMaxSpinner().setVisible(enableColor);
//
//        updateColoring();
//    }
//
//    private void saveSpectrumListButtonActionPerformed(ActionEvent evt) {
//        File file = CustomFileChooser.showSaveDialog(view, "Select File", "spectrumlist.txt");
//        String metadataFilename = collection.getShapeModel().getCustomDataFolder() + File.separator + file.getName() + ".metadata";
//        if (file != null)
//        {
//            try
//            {
//                FileWriter fstream = new FileWriter(file);
//                FileWriter fstream2 = new FileWriter(metadataFilename);
//                BufferedWriter out = new BufferedWriter(fstream);
//                BufferedWriter out2 = new BufferedWriter(fstream2);
//
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//                String nl = System.getProperty("line.separator");
//                out.write("#Spectrum_Name Image_Time_UTC"  + nl);
//                int size = model.getSpectrumRawResults().size();
//
//                for (int i=0; i<size; ++i)
//                {
//                    String result = createSpectrumName(i);
//                    String spectrumPath  = result; //new File(result).getAbsoluteFile().toString().substring(beginIndex);
//                    out.write(spectrumPath + nl);
//                    SearchSpec spectrumSpec = collection.getSearchSpec(spectrumPath);
//                    spectrumSpec.toFile(out2);
//                }
//
//                out.close();
//                out2.close();
//            }
//            catch (Exception e)
//            {
//                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view),
//                        "There was an error saving the file.",
//                        "Error",
//                        JOptionPane.ERROR_MESSAGE);
//
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void loadSpectrumListButtonActionPerformed(ActionEvent evt) {
//        File file = CustomFileChooser.showOpenDialog(view, "Select File");
//        String metadataFilename = collection.getShapeModel().getCustomDataFolder() + File.separator + file.getName() + ".metadata";
//        File file2 = new File(metadataFilename);
//
//        try
//		{
//			SpectrumListIO.loadSpectrumListButtonActionPerformed(file, file2, new Runnable()
//			{
//
//				@Override
//				public void run()
//				{
//			        populateSpectrumMetadata(FileUtil.getFileLinesAsStringList(file2.getAbsolutePath()));
//					AbstractSpectrumSearchController.this.fireResultsChanged();
//					model.setResultIntervalCurrentlyShown(new IdPair(0, model.getNumberOfBoundariesToShow()));
//			        showFootprints(model.getResultIntervalCurrentlyShown());
//
//				}
//			});
//		} catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//
//    abstract protected void populateSpectrumMetadata(List<String> lines);
//
//    private void customFunctionsButtonActionPerformed(ActionEvent evt) {
//        SpectrumMathPanel customFunctionsPanel = new SpectrumMathPanel(
//                JOptionPane.getFrameForComponent(view),
//                new JComboBox[]{view.getRedComboBox(), view.getGreenComboBox(), view.getBlueComboBox()}, instrument);
//        model.setCurrentlyEditingUserDefinedFunction(true);
//        customFunctionsPanel.setVisible(true);
//        model.setCurrentlyEditingUserDefinedFunction(false);
//        updateColoring();
//    }
//
//    private void numberOfFootprintsComboBoxActionPerformed(ActionEvent evt) {
//        IdPair shown = model.getResultIntervalCurrentlyShown();
//        if (shown == null) return;
//
//        // Only update if there's been a change in what is selected
//        int newMaxId = shown.id1 + Integer.parseInt((String)view.getNumberOfFootprintsComboBox().getSelectedItem());
//        if (newMaxId != shown.id2)
//        {
//            shown.id2 = newMaxId;
//            showFootprints(shown);
//        }
//    }
//
//    protected void initHierarchicalImageSearch()
//    {
//        if (view instanceof SpectrumBrowseView) {
//            SpectrumBrowseView browseView = (SpectrumBrowseView) view;
//        // Show/hide panels depending on whether this body has hierarchical image search capabilities
//        if(hasHierarchicalSpectraSearch)
//        {
//            // Has hierarchical search capabilities, these replace the camera and filter checkboxes so hide them
//
//            // Create the tree
//            spectraSpec.clearTreeLeaves();
//            spectraSpec.readHierarchyForInstrument(instrument.getDisplayName());
//            checkBoxTree = new CheckBoxTree(spectraSpec.getTreeModel());
//
//            // Place the tree in the panel
//            browseView.getDataSourcesScrollPane().setViewportView(checkBoxTree);
//        }
//        else
//        {
//            // No hierarchical search capabilities, hide the scroll pane
//            browseView.getDataSourcesScrollPane().setVisible(false);
//        }
//    }
//    }
//
//    protected void setupComboBoxes()
//    {
//        for (int i=1; i<=instrument.getBandCenters().length; ++i)
//        {
//            String channel = new String("(" + i + ") " + instrument.getBandCenters()[i-1] + " " + instrument.getBandCenterUnit());
//            view.getRedComboBox().addItem(channel);
//            view.getGreenComboBox().addItem(channel);
//            view.getBlueComboBox().addItem(channel);
//        }
//
//        String[] derivedParameters = instrument.getSpectrumMath().getDerivedParameters();
//        for (int i=0; i<derivedParameters.length; ++i)
//        {
//            view.getRedComboBox().addItem(derivedParameters[i]);
//            view.getGreenComboBox().addItem(derivedParameters[i]);
//            view.getBlueComboBox().addItem(derivedParameters[i]);
//        }
//
//        for (vtkFunctionParser fp: instrument.getSpectrumMath().getAllUserDefinedDerivedParameters())
//        {
//            view.getRedComboBox().addItem(fp.GetFunction());
//            view.getGreenComboBox().addItem(fp.GetFunction());
//            view.getBlueComboBox().addItem(fp.GetFunction());
//        }
//    }
//
//    protected void setColoringComboBox()
//    {
//        for (SpectrumColoringStyle style : SpectrumColoringStyle.values())
//        {
//            view.getColoringComboBox().addItem(style);
//        }
//    }
//
//
//
//    public void mouseClicked(MouseEvent e)
//    {
//    }
//
//    public void mouseEntered(MouseEvent e)
//    {
//    }
//
//    public void mouseExited(MouseEvent e)
//    {
//    }
//
//    public void mousePressed(MouseEvent e)
//    {
//        maybeShowPopup(e);
//    }
//
//    public void mouseReleased(MouseEvent e)
//    {
//        maybeShowPopup(e);
//    }
//
//    private void maybeShowPopup(MouseEvent e)
//    {
//        JList resultList = view.getResultList();
//        SpectrumPopupMenu spectrumPopupMenu = view.getSpectrumPopupMenu();
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
//    }
//
//
//    @Override
//    public void keyTyped(KeyEvent e)
//    {
//
//    }
//
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
////        ModelManager modelManager = model.getModelManager();
//        Renderer renderer = model.getRenderer();
//
//        if (e.getKeyChar()=='a')
//        {
//            renderer.removeKeyListener(this);
//            collection.toggleSelectAll();
//            renderer.addKeyListener(this);
//        }
//        else if (e.getKeyChar()=='s')
//        {
//            view.getSpectrumPopupMenu().showStatisticsWindow();
//        }
//        else if (e.getKeyChar()=='i' || e.getKeyChar()=='v')    // 'i' sets the lighting direction based on time of a single NIS spectrum, and 'v' looks from just above the footprint toward the sun
//        {
//            List<IBasicSpectrumRenderer> selection=collection.getSelectedSpectra();
//            if (selection.size()!=1)
//            {
//                JOptionPane.showMessageDialog(view, "Please select only one spectrum to specify lighting or viewpoint");
//                return;
//            }
//            IBasicSpectrumRenderer spectrum=selection.get(0);
//            renderer.setLighting(LightingType.FIXEDLIGHT);
//            Path fullPath=Paths.get(spectrum.getSpectrum().getFullPath());
//            Path relativePath=fullPath.subpath(fullPath.getNameCount()-2, fullPath.getNameCount());
//            //Vector3D toSunVector=getToSunUnitVector(relativePath.toString());
//            renderer.setFixedLightDirection(spectrum.getSpectrum().getToSunUnitVector()); // the fixed light direction points to the light
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
//                Vector3D focalPoint=cameraPosition.add((new Vector3D(spectrum.getSpectrum().getToSunUnitVector())).scalarMultiply(lookLength));
//                //
//                renderer.setCameraOrientation(cameraPosition.toArray(), focalPoint.toArray(), renderer.getRenderWindowPanel().getActiveCamera().GetViewUp(), renderer.getCameraViewAngle());
//            }
//        }
//        else if (e.getKeyChar()=='h')
//        {
//            collection.decreaseFootprintSeparation(0.001);
//        }
//        else if (e.getKeyChar()=='H')
//        {
//            collection.increaseFootprintSeparation(0.001);
//        }
//        else if (e.getKeyChar()=='+')
//        {
//            SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//            collection.setOffset(collection.getOffset()+body.getBoundingBoxDiagonalLength()/50);
//        }
//        else if (e.getKeyChar()=='-')
//        {
//            SmallBodyModel body=(SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
//            collection.setOffset(collection.getOffset()-body.getBoundingBoxDiagonalLength()/50);
//        }
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e)
//    {
//        // TODO Auto-generated method stub
//
//    }
//
//    protected void showFootprints(IdPair idPair)
//    {
//    	System.out.println("SpectrumSearchController: showFootprints: showing footprints");
//        int startId = idPair.id1;
//        int endId = idPair.id2;
//
//        SpectrumColoringStyle style = SpectrumColoringStyle.getStyleForName(view.getColoringComboBox().getSelectedItem().toString());
//
//        for (int i=startId; i<endId; ++i)
//        {
//            if (i < 0)
//                continue;
//            else if(i >= model.getSpectrumRawResults().size())
//                break;
//
//            try
//            {
////                String currentSpectrum = model.getSpectrumRawResults().get(i).get(0);
//            	BasicSpectrum spectrum = model.getSpectrumRawResults().get(i);
//                IBasicSpectrumRenderer spectrumRenderer = collection.addSpectrum(spectrum.getFullPath(), instrument, style);
//                spectrumRenderer.setSelected();
//            }
//            catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
//        updateColoring();
//    }
//
//
//    public abstract String createSpectrumName(int index);
//
//
//    private void checkValidMinMax(int channel, boolean minimunStateChange)
//    {
//        JSpinner minSpinner = null;
//        JSpinner maxSpinner = null;
//
//        if (channel == 0)
//        {
//            minSpinner = view.getRedMinSpinner();
//            maxSpinner = view.getRedMaxSpinner();
//        }
//        else if (channel == 1)
//        {
//            minSpinner = view.getGreenMinSpinner();
//            maxSpinner = view.getGreenMaxSpinner();
//        }
//        else if (channel == 2)
//        {
//            minSpinner = view.getBlueMinSpinner();
//            maxSpinner = view.getBlueMaxSpinner();
//        }
//
//        Double minVal = (Double)minSpinner.getValue();
//        Double maxVal = (Double)maxSpinner.getValue();
//        if (minVal > maxVal)
//        {
//            if (minimunStateChange)
//                minSpinner.setValue(maxSpinner.getValue());
//            else
//                maxSpinner.setValue(minSpinner.getValue());
//        }
//    }
//
//    public void updateColoring()
//    {
//        // If we are currently editing user defined functions
//        // (i.e. the dialog is open), do not update the coloring
//        // since we may be in an inconsistent state.
//        if (model.isCurrentlyEditingUserDefinedFunction())
//            return;
//
//
//        Double redMinVal = (Double)view.getRedMinSpinner().getValue();
//        Double redMaxVal = (Double)view.getRedMaxSpinner().getValue();
//
//        Double greenMinVal = (Double)view.getGreenMinSpinner().getValue();
//        Double greenMaxVal = (Double)view.getGreenMaxSpinner().getValue();
//
//        Double blueMinVal = (Double)view.getBlueMinSpinner().getValue();
//        Double blueMaxVal = (Double)view.getBlueMaxSpinner().getValue();
//
//        if (view.getGrayscaleCheckBox().isSelected())
//        {
//            collection.setChannelColoring(
//                    new int[]{view.getRedComboBox().getSelectedIndex(), view.getRedComboBox().getSelectedIndex(), view.getRedComboBox().getSelectedIndex()},
//                    new double[]{redMinVal, redMinVal, redMinVal},
//                    new double[]{redMaxVal, redMaxVal, redMaxVal},
//                    instrument);
//        }
//        else
//        {
//            collection.setChannelColoring(
//                    new int[]{view.getRedComboBox().getSelectedIndex(), view.getGreenComboBox().getSelectedIndex(), view.getBlueComboBox().getSelectedIndex()},
//                    new double[]{redMinVal, greenMinVal, blueMinVal},
//                    new double[]{redMaxVal, greenMaxVal, blueMaxVal},
//                    instrument);
//        }
//    }
//
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
//            Model spectraModel = modelManager.getModel(e.getPickedProp());
//            if (spectraModel instanceof SpectraCollection)
//            {
//                SpectraCollection coll=(SpectraCollection)spectraModel;
//                String name = coll.getSpectrumName((vtkActor)e.getPickedProp());
//                IBasicSpectrumRenderer spectrum=coll.getSpectrum(name);
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
//                        IBasicSpectrumRenderer spectrum_=coll.getSpectrum(createSpectrumName(i));
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
//
//
//    class MyListCellRenderer extends DefaultListCellRenderer
//    {
//        public MyListCellRenderer()
//        {
//            setOpaque(true);
//        }
//
//        public Component getListCellRendererComponent(JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus)
//        {
//            JLabel label = (JLabel) super.getListCellRendererComponent(paramlist, value, index, isSelected, cellHasFocus);
//            label.setOpaque(true /* was isSelected */); // Highlight only when selected
//            if(isSelected) { // I faked a match for the second index, put you matching condition here.
//                label.setBackground(Color.YELLOW);
//                label.setEnabled(false);
//            }
//            else
//            {
//                String spectrumFile=createSpectrumName(index);
//                IBasicSpectrumRenderer spectrum=collection.getSpectrum(spectrumFile);
//                setBackground(Color.LIGHT_GRAY);
//                if (spectrum==null)
//                    setForeground(Color.black);
//                else
//                {
//                    double[] color=spectrum.getSpectrum().getChannelColor();
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
//
//    protected abstract void setSpectrumSearchResults(List<BasicSpectrum> results);
//
//    public SpectrumView getView()
//    {
//        return view;
//    }
//
//}
