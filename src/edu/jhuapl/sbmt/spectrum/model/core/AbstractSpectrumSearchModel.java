package edu.jhuapl.sbmt.spectrum.model.core;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Ranges;

import vtk.vtkCubeSource;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.EllipsePolygon;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton.Node;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException;
import edu.jhuapl.sbmt.model.bennu.InstrumentMetadata;
import edu.jhuapl.sbmt.model.bennu.SearchSpec;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.BoundedObjectHyperTreeNode;
import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.BoundedObjectHyperTreeSkeleton;
import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.HyperBoundedObject;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.query.IQueryBase;
import edu.jhuapl.sbmt.query.database.DatabaseQueryBase;
import edu.jhuapl.sbmt.query.database.SpectraDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;
import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectraSearchDataCollection;
import edu.jhuapl.sbmt.spectrum.model.key.SpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.key.SpectrumKeyInterface;

import crucible.crust.metadata.api.MetadataManager;

public abstract class AbstractSpectrumSearchModel implements ISpectrumSearchModel, MetadataManager
{
    protected ISpectralInstrument instrument;
    protected SpectraHierarchicalSearchSpecification spectraSpec;
    protected ModelManager modelManager;
    protected PickManager pickManager;
    protected Date startDate = new GregorianCalendar(2000, 0, 11, 0, 0, 0).getTime();
    protected Date endDate = new GregorianCalendar(2000, 4, 14, 0, 0, 0).getTime();
    protected List<List<String>> results = new ArrayList<List<String>>();
    protected IdPair resultIntervalCurrentlyShown = null;
//    protected SmallBodyViewConfig smallBodyConfig;
    protected Renderer renderer;
    protected boolean currentlyEditingUserDefinedFunction = false;
//    protected SbmtInfoWindowManager infoPanelManager;
    protected PickEvent lastPickEvent=null;
    protected TreeSet<Integer> cubeList = null;
    private Vector<SpectrumSearchResultsListener> resultsListeners;
    private Vector<SpectrumColoringChangedListener> colorChangedListeners;
    protected int[] selectedImageIndices;
    private double minDistanceQuery;
    private double maxDistanceQuery;
    private double minIncidenceQuery;
    private double maxIncidenceQuery;
    private double minEmissionQuery;
    private double maxEmissionQuery;
    private double minPhaseQuery;
    private double maxPhaseQuery;
    private TreePath[] selectedPaths;
    private Double redMinVal = 0.0;
    private Double redMaxVal;
    private Double greenMinVal = 0.0;
    private Double greenMaxVal;
    private Double blueMinVal = 0.0;
    private Double blueMaxVal;
    private boolean greyScaleSelected;
    private int redIndex;
    private int greenIndex;
    private int blueIndex;
    protected String spectrumColoringStyleName = "RGB";
    private int numberOfBoundariesToShow;
    private List<Integer> polygonTypesChecked = new ArrayList<Integer>();
    protected SpectraCollection spectrumCollection;
    private String spectraHypertreeSourceName;
    private SpectraSearchDataCollection spectraModel;
    private String spectraHypertreeDataSpecName;
    private boolean hasHierarchicalSpectraSearch;
    private boolean hasHypertreeBasedSpectraSearch;

    public AbstractSpectrumSearchModel(boolean hasHierarchicalSpectraSearch, boolean hasHypertreeBasedSpectraSearch,
    		SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification,
    		final ModelManager modelManager,
//            SbmtInfoWindowManager infoPanelManager,
            final PickManager pickManager, final Renderer renderer, ISpectralInstrument instrument)
    {
//        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
//        this.infoPanelManager = infoPanelManager;
        this.pickManager = pickManager;
        this.pickManager = pickManager;
        this.renderer = renderer;
        this.instrument = instrument;
        this.resultsListeners = new Vector<SpectrumSearchResultsListener>();
        this.colorChangedListeners = new Vector<SpectrumColoringChangedListener>();
        this.hasHierarchicalSpectraSearch = hasHierarchicalSpectraSearch;
        this.hasHypertreeBasedSpectraSearch = hasHypertreeBasedSpectraSearch;

        SpectraHierarchicalSearchSpecification<?> searchSpec = null;
        if (hasHierarchicalSpectraSearch)
        {
            searchSpec = hierarchicalSpectraSearchSpecification;
            try
            {
                searchSpec.loadMetadata();
                searchSpec = searchSpec.clone();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                searchSpec = null;
            }
        }

        this.spectraSpec = searchSpec;
        this.spectrumCollection = (SpectraCollection) getModelManager().getModel(ModelNames.SPECTRA);
    }

    public void loadSearchSpecMetadata()
    {
        try
        {
            if (spectraSpec != null)
                spectraSpec.loadMetadata();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public List<List<String>> getSpectrumRawResults()
    {
        return results;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.gui.spectrum.model.ISpectrumSearchModel#setSpectrumRawResults(java.util.List)
     */
    @Override
    public void setSpectrumRawResults(List<List<String>> spectrumRawResults)
    {
        this.results = spectrumRawResults;
        this.resultIntervalCurrentlyShown = new IdPair(0, numberOfBoundariesToShow);
        showFootprints(resultIntervalCurrentlyShown);
        fireResultsChanged();
        fireResultsCountChanged(this.results.size());
    }

    public IdPair getResultIntervalCurrentlyShown()
    {
        return resultIntervalCurrentlyShown;
    }

    public void setResultIntervalCurrentlyShown(IdPair resultIntervalCurrentlyShown)
    {
        this.resultIntervalCurrentlyShown = resultIntervalCurrentlyShown;
    }

    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public PickManager getPickManager()
    {
        return pickManager;
    }

//    public SmallBodyViewConfig getSmallBodyConfig()
//    {
//        return smallBodyConfig;
//    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    public boolean isCurrentlyEditingUserDefinedFunction()
    {
        return currentlyEditingUserDefinedFunction;
    }

    public void setCurrentlyEditingUserDefinedFunction(
            boolean currentlyEditingUserDefinedFunction)
    {
        this.currentlyEditingUserDefinedFunction = currentlyEditingUserDefinedFunction;
    }

    public ISpectralInstrument getInstrument()
    {
        return instrument;
    }

    public SpectraHierarchicalSearchSpecification getSpectraSpec()
    {
        return spectraSpec;
    }

//    public SbmtInfoWindowManager getInfoPanelManager()
//    {
//        return infoPanelManager;
//    }

    public PickEvent getLastPickEvent()
    {
        return lastPickEvent;
    }

    public TreeSet<Integer> getCubeList()
    {
        return cubeList;
    }

    public ModelNames getSpectrumCollectionModelName()
    {
        return ModelNames.SPECTRA;
    }

    public ModelNames getSpectrumBoundaryCollectionModelName()
    {
        return ModelNames.SPECTRA_BOUNDARIES;
    }

    public void updateColoring()
    {
        // If we are currently editing user defined functions
        // (i.e. the dialog is open), do not update the coloring
        // since we may be in an inconsistent state.
        if (isCurrentlyEditingUserDefinedFunction())
            return;
        SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
        if (isGreyScaleSelected())
        {
            collection.setChannelColoring(
                    new int[]{redIndex, redIndex, redIndex},
                    new double[]{redMinVal, redMinVal, redMinVal},
                    new double[]{redMaxVal, redMaxVal, redMaxVal},
                    instrument);
        }
        else
        {
            collection.setChannelColoring(
                    new int[]{redIndex, greenIndex, blueIndex},
                    new double[]{redMinVal, greenMinVal, blueMinVal},
                    new double[]{redMaxVal, greenMaxVal, blueMaxVal},
                    instrument);
        }
        fireColoringChanged();
    }

    public void showFootprints(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        SpectrumColoringStyle style = SpectrumColoringStyle.getStyleForName(spectrumColoringStyleName);
        SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= getSpectrumRawResults().size())
                break;

            try
            {
                collection.addSpectrum(createSpectrumName(i), instrument, style);
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        updateColoring();
    }

    public void saveSelectedSpectrumListButtonActionPerformed(Component view, int[] selectedIndices) throws Exception
    {
        File file = CustomFileChooser.showSaveDialog(view, "Select File", "spectralist.txt");
        String metadataFilename = getModelManager().getPolyhedralModel().getCustomDataFolder() + File.separator + file.getName() + ".metadata";
        if (file != null)
        {
            FileWriter fstream = new FileWriter(file);
            FileWriter fstream2 = new FileWriter(metadataFilename);
            BufferedWriter out = new BufferedWriter(fstream);
            BufferedWriter out2 = new BufferedWriter(fstream2);
            SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            String nl = System.getProperty("line.separator");

            SearchSpec spectrumSpec = collection.getSearchSpec(createSpectrumName(0));
            if (spectrumSpec != null)
                spectrumSpec.toFile(out);

            out.write("#Spectrum_Name Spectrum_Time_UTC Pointing"  + nl);
            for (int selectedIndex : selectedIndices)
            {
                String image = new File(results.get(selectedIndex).get(0)).getName();
                String dtStr = results.get(selectedIndex).get(1);
                Date dt = new Date(Long.parseLong(dtStr));

                out.write(results.get(selectedIndex).get(0) + "," + dt.getTime() + nl);

            }

            out.close();
            out2.close();
        }

    }

    public void saveSpectrumListButtonActionPerformed(Component view) throws Exception
    {
        File file = CustomFileChooser.showSaveDialog(view, "Select File", "spectrumlist.txt");
        String metadataFilename = getModelManager().getPolyhedralModel().getCustomDataFolder() + File.separator + file.getName() + ".metadata";
        if (file != null)
        {
            FileWriter fstream = new FileWriter(file);
//            FileWriter fstream2 = new FileWriter(metadataFilename);
            BufferedWriter out = new BufferedWriter(fstream);
//            BufferedWriter out2 = new BufferedWriter(fstream2);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            String nl = System.getProperty("line.separator");
            SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);

            SearchSpec spectrumSpec = collection.getSearchSpec(createSpectrumName(0));
            if (spectrumSpec != null)
                spectrumSpec.toFile(out);
//            out.write(nl);

            out.write("#Spectrum_Name Image_Time_UTC"  + nl);
            int size = getSpectrumRawResults().size();

            for (int i=0; i<size; ++i)
            {
                String result = createSpectrumName(i);
                String spectrumPath  = result;
                out.write(spectrumPath + "," + getSpectrumRawResults().get(i).get(1) + nl);
            }

            out.close();
//            out2.close();
        }
    }

    public void loadSpectrumListButtonActionPerformed(ActionEvent evt) throws Exception
    {
        results.clear();
        File file = CustomFileChooser.showOpenDialog(null, "Select File");
        if (file == null) return;
//        String metadataFilename = getModelManager().getPolyhedralModel().getCustomDataFolder() + File.separator + file.getName() + ".metadata";
//        File file2 = new File(metadataFilename);

        if (file != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

//            List<List<String>> results = new ArrayList<List<String>>();
            List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
//            List<String> lines2 = FileUtil.getFileLinesAsStringList(file2.getAbsolutePath());
            for (int i=1; i<lines.size(); ++i)
            {
                if (lines.get(i).startsWith("#")) continue;
                String[] words = lines.get(i).trim().split("[,\\[\\]]+"); //was \\s+
                List<String> result = new ArrayList<String>();
                result.add(words[0]);
                result.add(words[1].trim());
                results.add(result);
            }
            populateSpectrumMetadata(lines.get(0));

//            if (lines2.size() > 0)
//                populateSpectrumMetadata(lines2);

            fireResultsChanged();
            this.resultIntervalCurrentlyShown = new IdPair(0, numberOfBoundariesToShow);
            showFootprints(resultIntervalCurrentlyShown);
        }
    }

    public void performSearch()
    {
        results.clear();
        List<List<String>> tempResults = new ArrayList<List<String>>();
        try
        {
//            panel.getSelectRegionButton().setSelected(false);
            getPickManager().setPickMode(PickMode.DEFAULT);

            GregorianCalendar startDateGreg = new GregorianCalendar();
            GregorianCalendar endDateGreg = new GregorianCalendar();
            startDateGreg.setTime(getStartDate());
            endDateGreg.setTime(getEndDate());
            DateTime startDateJoda = new DateTime(
                    startDateGreg.get(GregorianCalendar.YEAR),
                    startDateGreg.get(GregorianCalendar.MONTH)+1,
                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    startDateGreg.get(GregorianCalendar.MINUTE),
                    startDateGreg.get(GregorianCalendar.SECOND),
                    startDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);
            DateTime endDateJoda = new DateTime(
                    endDateGreg.get(GregorianCalendar.YEAR),
                    endDateGreg.get(GregorianCalendar.MONTH)+1,
                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    endDateGreg.get(GregorianCalendar.MINUTE),
                    endDateGreg.get(GregorianCalendar.SECOND),
                    endDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);

            if (cubeList != null)
                cubeList.clear();
            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
            SmallBodyModel bodyModel = (SmallBodyModel)getModelManager().getModel(ModelNames.SMALL_BODY);
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

            List<Integer> productsSelected;
            if(hasHierarchicalSpectraSearch)
            {
                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
//                sumOfProductsSearch = true;
                SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
                // Process the user's selections
//                getSmallBodyConfig().hierarchicalSpectraSearchSpecification.processTreeSelections(selectedPaths);
                spectraSpec.processTreeSelections(selectedPaths);

                // Get the selected (camera,filter) pairs

                productsSelected = spectraSpec.getSelectedDatasets();
//                System.out.println("SpectrumSearchModel: performSearch: selected data sets size " + productsSelected.get(0));
                InstrumentMetadata<SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
//                System.out.println("SpectrumSearchModel: performSearch: " + instrumentMetadata);
//                ArrayList<ArrayList<String>> specs = spectraSpec.getSpecs();
                TreeModel tree = spectraSpec.getTreeModel();
//                System.out.println("SpectrumSearchModel: performSearch: root is " + tree.getChildCount(tree.getRoot()));
//                System.out.println("SpectrumSearchModel: performSearch: tree " + tree.getChild(tree.getRoot(), 0).toString());
                List<SearchSpec> specs = instrumentMetadata.getSpecs();
                for (Integer selected : productsSelected)
                {
                    String name = tree.getChild(tree.getRoot(), selected).toString();
                    SearchSpec spec = specs.get(selected);
                    FixedListSearchMetadata searchMetadata = FixedListSearchMetadata.of(spec.getDataName(),
                                                                                        spec.getDataListFilename(),
                                                                                        spec.getDataPath(),
                                                                                        spec.getDataRootLocation(),
                                                                                        spec.getSource());

                    List<List<String>> thisResult = instrument.getQueryBase().runQuery(searchMetadata).getResultlist();
                    collection.tagSpectraWithMetadata(thisResult, spec);
                    tempResults.addAll(thisResult);
                }
            }
            else
            {
                IQueryBase queryType = instrument.getQueryBase();
                if (queryType instanceof FixedListQuery)
                {
                    FixedListQuery query = (FixedListQuery)queryType;
                    tempResults = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist", "spectra", query.getRootPath(), ImageSource.CORRECTED_SPICE)).getResultlist();
                }
                else
                {
                    SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                            Ranges.closed(minDistanceQuery, maxDistanceQuery),
                            "", polygonTypesChecked,
                            Ranges.closed(minIncidenceQuery, maxIncidenceQuery),
                            Ranges.closed(minEmissionQuery, maxEmissionQuery),
                            Ranges.closed(minPhaseQuery, maxPhaseQuery),
                            cubeList);
                    DatabaseQueryBase query = (DatabaseQueryBase)queryType;
                    tempResults = query.runQuery(searchMetadata).getResultlist();
                }
            }
//            this.resultIntervalCurrentlyShown = new IdPair(0, numberOfBoundariesToShow);
//            showFootprints(resultIntervalCurrentlyShown);
//            fireResultsChanged();
            setSpectrumRawResults(tempResults);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }

    public void performHypertreeSearch()
    {
        results.clear();
        List<Integer> productsSelected;
        List<List<String>> results = new ArrayList<List<String>>();
        Map<String, Double> fileDateMap = new HashMap<String, Double>();

        try
        {
            getPickManager().setPickMode(PickMode.DEFAULT);

            GregorianCalendar startDateGreg = new GregorianCalendar();
            GregorianCalendar endDateGreg = new GregorianCalendar();
            startDateGreg.setTime(getStartDate());
            endDateGreg.setTime(getEndDate());
            double startTime = getStartDate().getTime();
            double endTime = getEndDate().getTime();

            DateTime startDateJoda = new DateTime(
                    startDateGreg.get(GregorianCalendar.YEAR),
                    startDateGreg.get(GregorianCalendar.MONTH) + 1,
                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    startDateGreg.get(GregorianCalendar.MINUTE),
                    startDateGreg.get(GregorianCalendar.SECOND),
                    startDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);
            DateTime endDateJoda = new DateTime(
                    endDateGreg.get(GregorianCalendar.YEAR),
                    endDateGreg.get(GregorianCalendar.MONTH) + 1,
                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    endDateGreg.get(GregorianCalendar.MINUTE),
                    endDateGreg.get(GregorianCalendar.SECOND),
                    endDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);

            if (hasHypertreeBasedSpectraSearch)
            {

                String spectraDatasourceName = spectraHypertreeSourceName;

                this.spectraModel = (SpectraSearchDataCollection) modelManager
                        .getModel(ModelNames.SPECTRA_HYPERTREE_SEARCH);
                String spectraDatasourcePath = spectraModel
                        .getSpectraDataSourceMap().get(spectraDatasourceName);

//                System.out.println("Current Spectra Datasource Name: "
//                        + spectraDatasourceName);
//                System.out.println("Current Spectra Datasource Path: "
//                        + spectraDatasourcePath);

                spectraModel.addDatasourceSkeleton(spectraDatasourceName,
                        spectraDatasourcePath);
                spectraModel
                        .setCurrentDatasourceSkeleton(spectraDatasourceName);
                spectraModel.readSkeleton();
                BoundedObjectHyperTreeSkeleton skeleton = (BoundedObjectHyperTreeSkeleton) spectraModel
                        .getCurrentSkeleton();

                double[] selectionRegionCenter = null;
                double selectionRegionRadius = 0.0;

                AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) modelManager
                        .getModel(ModelNames.CIRCLE_SELECTION);
                SmallBodyModel smallBodyModel = (SmallBodyModel) modelManager
                        .getModel(ModelNames.SMALL_BODY);
                EllipsePolygon region = null;
                vtkPolyData interiorPoly = new vtkPolyData();
                if (selectionModel.getNumberOfStructures() > 0)
                {
                    region = (EllipsePolygon) selectionModel
                            .getStructure(0);
                    selectionRegionCenter = region.getCenter();
                    selectionRegionRadius = region.radius;

                    // Always use the lowest resolution model for getting the
                    // intersection cubes list.
                    // Therefore, if the selection region was created using a
                    // higher resolution model,
                    // we need to recompute the selection region using the low
                    // res model.
                    if (smallBodyModel.getModelResolution() > 0)
                        smallBodyModel.drawRegularPolygonLowRes(selectionRegionCenter,
                                region.radius, region.numberOfSides,
                                interiorPoly, null); // this sets interiorPoly
                    else
                        interiorPoly = region.interiorPolyData;

                }
                else
                {
                    vtkCubeSource box = new vtkCubeSource();
                    double[] bboxBounds = smallBodyModel.getBoundingBox()
                            .getBounds();
                    BoundingBox bbox = new BoundingBox(bboxBounds);
                    bbox.increaseSize(0.01);
                    box.SetBounds(bbox.getBounds());
                    box.Update();
                    interiorPoly.DeepCopy(box.GetOutput());
                }

                Set<String> files = new HashSet<String>();
                HashMap<String, HyperBoundedObject> fileSpecMap = new HashMap<String, HyperBoundedObject>();
                double[] times = new double[] { startTime, endTime };
                double[] spectraLims = new double[] { minEmissionQuery, maxEmissionQuery, minIncidenceQuery, maxIncidenceQuery, minPhaseQuery, maxPhaseQuery, minDistanceQuery, maxDistanceQuery };
                double[] bounds = interiorPoly.GetBounds();
                TreeSet<Integer> cubeList = ((SpectraSearchDataCollection) spectraModel)
                        .getLeavesIntersectingBoundingBox(
                                new BoundingBox(bounds), times, spectraLims);
                HyperBox hbb = new HyperBox(
                        new double[] { bounds[0], bounds[2], bounds[4],
                                times[0], spectraLims[0], spectraLims[2],
                                spectraLims[4], spectraLims[6] },
                        new double[] { bounds[1], bounds[3], bounds[5],
                                times[1], spectraLims[1], spectraLims[3],
                                spectraLims[5], spectraLims[7] });



                for (Integer cubeid : cubeList)
                {
//                    System.out.println("cubeId: " + cubeid);
                    Node currNode = skeleton.getNodeById(cubeid);
                    Path path = currNode.getPath();
                    Path dataPath = path.resolve("data");
                    DataInputStream instream = new DataInputStream(
                            new BufferedInputStream(new FileInputStream(
                                    FileCache.getFileFromServer(
                                            dataPath.toString()))));
                    try
                    {
                        while (instream.available() > 0)
                        {
                            HyperBoundedObject spectra = BoundedObjectHyperTreeNode
                                    .createNewBoundedObject(instream, 8);
                            int fileNum = spectra.getFileNum();
                            double date_et = spectra.getDate();



                            Map<Integer, String> fileMap = skeleton
                                    .getFileMap();
                            String file = fileMap.get(fileNum);
                            if (files.add(file))
                            {
                                fileSpecMap.put(file, spectra);
                                fileDateMap.put(file, date_et);
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }


                ArrayList<String> finalFiles = new ArrayList<String>();
                ArrayList<HyperBoundedObject> finalSpectra = new ArrayList<HyperBoundedObject>();

                // NOW CHECK WHICH SPECTRA ACTUALLY INTERSECT REGION
                for (String fi : files)
                {
                    HyperBoundedObject spec = fileSpecMap.get(fi);
                    HyperBox bbox = spec.getBbox();
                    try
                    {
                        if (hbb.intersects(bbox))
                        {
                            finalFiles.add(fi);
                            finalSpectra.add(spec);
                        }
                    }
                    catch (HyperException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                // final list of spectra that intersect region
                // create a list of lists for the results
                List<List<String>> listoflist = new ArrayList<List<String>>(
                        finalFiles.size()); // why is results formatted this way?
                                          // (list of list)

                finalFiles.sort(new Comparator<String>()
                {

                    @Override
                    public int compare(String o1, String o2)
                    {
                        return o1.compareTo(o2);
                    }
                });
                for (String file : finalFiles)
                {
                    ArrayList<String> currList = new ArrayList<String>();
                    currList.add(file);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String date = sdf.format(fileDateMap.get(file));
                    Long dateLong = sdf.parse(date).getTime();
                    currList.add(""+dateLong);
                    listoflist.add(currList);
                }
                results = listoflist;
                setSpectrumRawResults(results);

            }
            else
            {
                IQueryBase queryType = instrument.getQueryBase();
                if (queryType instanceof FixedListQuery)
                {
                    FixedListQuery query = (FixedListQuery) queryType;
                    results = instrument.getQueryBase()
                            .runQuery(FixedListSearchMetadata.of(
                                    "Spectrum Search", "spectrumlist.txt",
                                    "spectra", query.getRootPath(),
                                    ImageSource.CORRECTED_SPICE))
                            .getResultlist();
                }
                else
                {
                    SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata
                            .of("", startDateJoda, endDateJoda, Ranges.closed(minDistanceQuery, maxDistanceQuery),
                                    "", null, // TODO: reinstate polygon types
                                              // here
                                    Ranges.closed(minIncidenceQuery, maxIncidenceQuery),
                                    Ranges.closed(minEmissionQuery, maxEmissionQuery),
                                    Ranges.closed(minPhaseQuery, maxPhaseQuery),
                                    cubeList);

                    DatabaseQueryBase query = (DatabaseQueryBase) queryType;
                    results = query.runQuery(searchMetadata).getResultlist();
                }
            }
             InstrumentMetadata<SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
             List<SearchSpec> specs = instrumentMetadata.getSpecs();
             for (SearchSpec spec : specs)
             {
                 if (spec.getDataName().contains(spectraHypertreeDataSpecName))
                 {
                     spectrumCollection.tagSpectraWithMetadata(results, spec);
                 }
             }
             setSpectrumRawResults(results);


        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }

    protected void fireResultsChanged()
    {
        for (SpectrumSearchResultsListener listener : resultsListeners)
        {
            listener.resultsChanged(results);
        }
    }

    protected void fireResultsCountChanged(int count)
    {
        for (SpectrumSearchResultsListener listener : resultsListeners)
        {
            listener.resultsCountChanged(count);
        }
    }

    public void addResultsChangedListener(SpectrumSearchResultsListener listener)
    {
        resultsListeners.add(listener);
    }

    public void removeResultsChangedListener(SpectrumSearchResultsListener listener)
    {
        resultsListeners.remove(listener);
    }

    public void removeAllResultsChangedListeners()
    {
        resultsListeners.removeAllElements();
    }

    private void fireColoringChanged()
    {
        for (SpectrumColoringChangedListener listener : colorChangedListeners)
        {
            listener.coloringChanged();
        }
    }

    public void addColoringChangedListener(SpectrumColoringChangedListener listener)
    {
        colorChangedListeners.add(listener);
    }

    public void removeColoringChangedListener(SpectrumColoringChangedListener listener)
    {
        colorChangedListeners.remove(listener);
    }

    public void removeAllColoringChangedListeners()
    {
        colorChangedListeners.removeAllElements();
    }

    public void coloringOptionChanged()
    {
        fireColoringChanged();
    }

    public SpectrumKeyInterface[] getSelectedSpectrumKeys()
    {
        int[] indices = selectedImageIndices;
        SpectrumKeyInterface[] selectedKeys = new SpectrumKeyInterface[indices.length];
        if (indices.length > 0)
        {
            int i=0;
            for (int index : indices)
            {
                String image = results.get(index).get(0);
                SpectrumKeyInterface selectedKey = createSpectrumKey(image, instrument);
//                if (!selectedKey.band.equals("0"))
//                    name = selectedKey.band + ":" + name;
                selectedKeys[i++] = selectedKey;
            }
        }
        return selectedKeys;
    }

    public void clearSpectraFromDisplay()
    {
        spectrumCollection.removeAllSpectraForInstrument(instrument);
        spectrumCollection.deselectAll();
        setResultIntervalCurrentlyShown(null);
    }


    public void setSelectedImageIndex(int[] selectedImageIndex)
    {
        this.selectedImageIndices = selectedImageIndex;
    }

    public int[] getSelectedImageIndex()
    {
        return selectedImageIndices;
    }

    public List<SpectrumKeyInterface> createSpectrumKeys(String boundaryName, ISpectralInstrument instrument)
    {
        List<SpectrumKeyInterface> result = new ArrayList<SpectrumKeyInterface>();
        result.add(createSpectrumKey(boundaryName, instrument));
        return result;
    }

    public SpectrumKeyInterface createSpectrumKey(String imagePathName, ISpectralInstrument instrument)
    {
        SpectrumKeyInterface key = new SpectrumKey(imagePathName, null, null, instrument, "");
        return key;
    }


    public double getMinDistanceQuery()
    {
        return minDistanceQuery;
    }


    public void setMinDistanceQuery(double minDistanceQuery)
    {
        this.minDistanceQuery = minDistanceQuery;
    }


    public double getMaxDistanceQuery()
    {
        return maxDistanceQuery;
    }


    public void setMaxDistanceQuery(double maxDistanceQuery)
    {
        this.maxDistanceQuery = maxDistanceQuery;
    }


    public double getMinIncidenceQuery()
    {
        return minIncidenceQuery;
    }


    public void setMinIncidenceQuery(double minIncidenceQuery)
    {
        this.minIncidenceQuery = minIncidenceQuery;
    }


    public double getMaxIncidenceQuery()
    {
        return maxIncidenceQuery;
    }


    public void setMaxIncidenceQuery(double maxIncidenceQuery)
    {
        this.maxIncidenceQuery = maxIncidenceQuery;
    }


    public double getMinEmissionQuery()
    {
        return minEmissionQuery;
    }


    public void setMinEmissionQuery(double minEmissionQuery)
    {
        this.minEmissionQuery = minEmissionQuery;
    }


    public double getMaxEmissionQuery()
    {
        return maxEmissionQuery;
    }


    public void setMaxEmissionQuery(double maxEmissionQuery)
    {
        this.maxEmissionQuery = maxEmissionQuery;
    }


    public double getMinPhaseQuery()
    {
        return minPhaseQuery;
    }


    public void setMinPhaseQuery(double minPhaseQuery)
    {
        this.minPhaseQuery = minPhaseQuery;
    }


    public double getMaxPhaseQuery()
    {
        return maxPhaseQuery;
    }


    public void setMaxPhaseQuery(double maxPhaseQuery)
    {
        this.maxPhaseQuery = maxPhaseQuery;
    }


    public TreePath[] getSelectedPath()
    {
        return selectedPaths;
    }


    public void setSelectedPath(TreePath[] selectedPath)
    {
        this.selectedPaths = selectedPath;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.gui.spectrum.model.ISpectrumSearchModel#createSpectrumName(int)
     */
    @Override
    public abstract String createSpectrumName(int index);

    abstract public void populateSpectrumMetadata(String line);

    public Double getRedMinVal()
    {
        return redMinVal;
    }


    public void setRedMinVal(Double redMinVal)
    {
        this.redMinVal = redMinVal;
    }


    public Double getRedMaxVal()
    {
        return redMaxVal;
    }


    public void setRedMaxVal(Double redMaxVal)
    {
        this.redMaxVal = redMaxVal;
    }


    public Double getGreenMinVal()
    {
        return greenMinVal;
    }


    public void setGreenMinVal(Double greenMinVal)
    {
        this.greenMinVal = greenMinVal;
    }


    public Double getGreenMaxVal()
    {
        return greenMaxVal;
    }


    public void setGreenMaxVal(Double greenMaxVal)
    {
        this.greenMaxVal = greenMaxVal;
    }


    public Double getBlueMinVal()
    {
        return blueMinVal;
    }


    public void setBlueMinVal(Double blueMinVal)
    {
        this.blueMinVal = blueMinVal;
    }


    public Double getBlueMaxVal()
    {
        return blueMaxVal;
    }


    public void setBlueMaxVal(Double blueMaxVal)
    {
        this.blueMaxVal = blueMaxVal;
    }


    public boolean isGreyScaleSelected()
    {
        return greyScaleSelected;
    }


    public void setGreyScaleSelected(boolean greyScaleSelected)
    {
        this.greyScaleSelected = greyScaleSelected;
    }


    public int getRedIndex()
    {
        return redIndex;
    }


    public void setRedIndex(int redIndex)
    {
        this.redIndex = redIndex;
    }


    public int getGreenIndex()
    {
        return greenIndex;
    }


    public void setGreenIndex(int greenIndex)
    {
        this.greenIndex = greenIndex;
    }


    public int getBlueIndex()
    {
        return blueIndex;
    }


    public void setBlueIndex(int blueIndex)
    {
        this.blueIndex = blueIndex;
    }


    public String getSpectrumColoringStyleName()
    {
        return spectrumColoringStyleName;
    }


    public void setSpectrumColoringStyleName(String spectrumColoringStyleName)
    {
        this.spectrumColoringStyleName = spectrumColoringStyleName;
    }


    public int getNumberOfBoundariesToShow()
    {
        return numberOfBoundariesToShow;
    }


    public void setNumberOfBoundariesToShow(int numberOfBoundariesToShow)
    {
        this.numberOfBoundariesToShow = numberOfBoundariesToShow;
    }

    public String getSpectraHypertreeSourceName()
    {
        return spectraHypertreeSourceName;
    }

    public void setSpectraHypertreeSourceName(String spectraHypertreeSourceName)
    {
        this.spectraHypertreeSourceName = spectraHypertreeSourceName;
    }

    public void setSpectraHypertreeDataSpecName(String spectraHypertreeSourceName)
    {
        this.spectraHypertreeDataSpecName = spectraHypertreeSourceName;
    }

    public void addToPolygonsSelected(int index)
    {
        polygonTypesChecked.add(index);
    }
}
