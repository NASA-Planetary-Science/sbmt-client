package edu.jhuapl.sbmt.gui.spectrum.model;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Ranges;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.bennu.InstrumentMetadata;
import edu.jhuapl.sbmt.model.bennu.SearchSpec;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.Spectrum.SpectrumKey;
import edu.jhuapl.sbmt.model.spectrum.coloring.SpectrumColoringStyle;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.query.database.DatabaseQueryBase;
import edu.jhuapl.sbmt.query.database.SpectraDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;

public abstract class SpectrumSearchModel implements ISpectrumSearchModel
{
    protected SpectralInstrument instrument;
    protected SpectraHierarchicalSearchSpecification spectraSpec;
    protected ModelManager modelManager;
    protected PickManager pickManager;
    protected Date startDate = new GregorianCalendar(2000, 0, 11, 0, 0, 0).getTime();
    protected Date endDate = new GregorianCalendar(2000, 4, 14, 0, 0, 0).getTime();
    protected List<List<String>> results = new ArrayList<List<String>>();
    protected IdPair resultIntervalCurrentlyShown = null;
    protected SmallBodyViewConfig smallBodyConfig;
    protected Renderer renderer;
    protected boolean currentlyEditingUserDefinedFunction = false;
    protected SbmtInfoWindowManager infoPanelManager;
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
    Double redMinVal;
    Double redMaxVal;
    Double greenMinVal;
    Double greenMaxVal;
    Double blueMinVal;
    Double blueMaxVal;
    boolean greyScaleSelected;
    int redIndex;
    int greenIndex;
    int blueIndex;
    private String spectrumColoringStyleName;

    public SpectrumSearchModel(SmallBodyViewConfig smallBodyConfig, final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            final PickManager pickManager, final Renderer renderer, SpectralInstrument instrument)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.infoPanelManager = infoPanelManager;
        this.pickManager = pickManager;
        this.pickManager = pickManager;
        this.renderer = renderer;
        this.instrument = instrument;
        this.resultsListeners = new Vector<SpectrumSearchResultsListener>();
        this.colorChangedListeners = new Vector<SpectrumColoringChangedListener>();
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

    public SmallBodyViewConfig getSmallBodyConfig()
    {
        return smallBodyConfig;
    }

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

    public SpectralInstrument getInstrument()
    {
        return instrument;
    }

    public SpectraHierarchicalSearchSpecification getSpectraSpec()
    {
        return spectraSpec;
    }

    public SbmtInfoWindowManager getInfoPanelManager()
    {
        return infoPanelManager;
    }

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
        return ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES;
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
    }

    public void showFootprints(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        SpectrumColoringStyle style = SpectrumColoringStyle.getStyleForName(spectrumColoringStyleName);

        SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
//        model.removeAllSpectra();

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= getSpectrumRawResults().size())
                break;

            try
            {
//                String currentSpectrum = results.get(i);
//                collection.addSpectrum(createSpectrumName(currentSpectrum), instrument);
                collection.addSpectrum(createSpectrumName(i), instrument, style);
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        updateColoring();
    }



    public void saveSpectrumListButtonActionPerformed(Component view) throws Exception
    {
        File file = CustomFileChooser.showSaveDialog(view, "Select File", "spectrumlist.txt");
        String metadataFilename = getModelManager().getPolyhedralModel().getCustomDataFolder() + File.separator + file.getName() + ".metadata";
        if (file != null)
        {
            FileWriter fstream = new FileWriter(file);
            FileWriter fstream2 = new FileWriter(metadataFilename);
            BufferedWriter out = new BufferedWriter(fstream);
            BufferedWriter out2 = new BufferedWriter(fstream2);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            String nl = System.getProperty("line.separator");
            out.write("#Spectrum_Name Image_Time_UTC"  + nl);
            int size = getSpectrumRawResults().size();
            SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);

            for (int i=0; i<size; ++i)
            {
                String result = createSpectrumName(i);
                String spectrumPath  = result;
                out.write(spectrumPath + nl);
                SearchSpec spectrumSpec = collection.getSearchSpec(spectrumPath);
                spectrumSpec.toFile(out2);
            }

            out.close();
            out2.close();
        }
    }

    public void loadSpectrumListButtonActionPerformed(ActionEvent evt) throws Exception
    {
        File file = CustomFileChooser.showOpenDialog(null, "Select File");
        String metadataFilename = getModelManager().getPolyhedralModel().getCustomDataFolder() + File.separator + file.getName() + ".metadata";
        File file2 = new File(metadataFilename);

        if (file != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            List<List<String>> results = new ArrayList<List<String>>();
            List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
            List<String> lines2 = FileUtil.getFileLinesAsStringList(file2.getAbsolutePath());
            for (int i=0; i<lines.size(); ++i)
            {
                if (lines.get(i).startsWith("#")) continue;
                String[] words = lines.get(i).trim().split("\\s+");
                List<String> result = new ArrayList<String>();
                result.add(words[0]);
                results.add(result);
            }
//                setSpectrumSearchResults(results);

            populateSpectrumMetadata(lines2);

            fireResultsChanged();
        }
    }

    public void performSearch()
    {
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

//            TreeSet<Integer> cubeList = null;
            if (cubeList != null)
                cubeList.clear();
            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
            SmallBodyModel bodyModel = (SmallBodyModel)getModelManager().getModel(ModelNames.SMALL_BODY);
            if (selectionModel.getNumberOfStructures() > 0)
            {
                AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);

                // Always use the lowest resolution model for getting the intersection cubes list.
                // Therefore, if the selection region was created using a higher resolution model,
                // we need to recompute the selection region using the low res model.
                if (bodyModel.getModelResolution() > 0)
                {
                    vtkPolyData interiorPoly = new vtkPolyData();
                    bodyModel.drawRegularPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
                    cubeList = bodyModel.getIntersectingCubes(interiorPoly);
                }
                else
                {
                    cubeList = bodyModel.getIntersectingCubes(region.interiorPolyData);
                }
            }

            List<Integer> productsSelected;
//            List<List<String>> results = new ArrayList<List<String>>();
            if(getSmallBodyConfig().hasHierarchicalSpectraSearch)
            {
                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
//                sumOfProductsSearch = true;
                SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
                // Process the user's selections
                getSmallBodyConfig().hierarchicalSpectraSearchSpecification.processTreeSelections(selectedPaths);

                // Get the selected (camera,filter) pairs

                productsSelected = spectraSpec.getSelectedDatasets();
                InstrumentMetadata<SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
//                ArrayList<ArrayList<String>> specs = spectraSpec.getSpecs();
                TreeModel tree = spectraSpec.getTreeModel();
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
                    results.addAll(thisResult);
                }
//                results = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist.txt", "spectra", ImageSource.CORRECTED_SPICE)).getResultlist();
            }
            else
            {
                QueryBase queryType = instrument.getQueryBase();
                if (queryType instanceof FixedListQuery)
                {
                    FixedListQuery query = (FixedListQuery)queryType;
                    results = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist", "spectra", query.getRootPath(), ImageSource.CORRECTED_SPICE)).getResultlist();
                }
                else
                {
                    SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                            Ranges.closed(minDistanceQuery, maxDistanceQuery),
                            "", null,   //TODO: reinstate polygon types here
                            Ranges.closed(minIncidenceQuery, maxIncidenceQuery),
                            Ranges.closed(minEmissionQuery, maxEmissionQuery),
                            Ranges.closed(minPhaseQuery, maxPhaseQuery),
                            cubeList);

                    DatabaseQueryBase query = (DatabaseQueryBase)queryType;
                    results = query.runQuery(searchMetadata).getResultlist();
                }
            }
//            setSpectrumSearchResults(results);
            fireResultsChanged();
//            SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
//            collection.tagSpectraWithMetadata(results, spec);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }

    private void fireResultsChanged()
    {
        for (SpectrumSearchResultsListener listener : resultsListeners)
        {
            listener.resultsChanged(results);
        }
    }

    private void fireResultsCountChanged(int count)
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

    public SpectrumKey[] getSelectedImageKeys()
    {
        int[] indices = selectedImageIndices;
        SpectrumKey[] selectedKeys = new SpectrumKey[indices.length];
        if (indices.length > 0)
        {
            int i=0;
            for (int index : indices)
            {
                String image = results.get(index).get(0);
                String name = new File(image).getName();
                image = image.substring(0,image.length()-4);
//                SpectrumKey selectedKey = createImageKey(image, imageSourceOfLastQuery, instrument);
//                if (!selectedKey.band.equals("0"))
//                    name = selectedKey.band + ":" + name;
//                selectedKeys[i++] = selectedKey;
            }
        }
        return selectedKeys;
    }


    public void setSelectedImageIndex(int[] selectedImageIndex)
    {
        this.selectedImageIndices = selectedImageIndex;
    }

    public int[] getSelectedImageIndex()
    {
        return selectedImageIndices;
    }

    public List<SpectrumKey> createSpectrumKeys(String boundaryName, SpectralInstrument instrument)
    {
        List<SpectrumKey> result = new ArrayList<SpectrumKey>();
        result.add(createSpectrumKey(boundaryName, instrument));
        return result;
    }

    public SpectrumKey createSpectrumKey(String imagePathName, SpectralInstrument instrument)
    {
        SpectrumKey key = new SpectrumKey(imagePathName, null, null, instrument);
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

    abstract public void populateSpectrumMetadata(List<String> lines);

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


    public int isRedIndex()
    {
        return redIndex;
    }


    public void setRedIndex(int redIndex)
    {
        this.redIndex = redIndex;
    }


    public int isGreenIndex()
    {
        return greenIndex;
    }


    public void setGreenIndex(int greenIndex)
    {
        this.greenIndex = greenIndex;
    }


    public int isBlueIndex()
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
}
