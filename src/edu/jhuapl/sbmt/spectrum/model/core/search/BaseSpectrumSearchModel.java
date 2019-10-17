package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.tree.TreePath;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.core.listeners.SearchProgressListener;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;
import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.BoundedObjectHyperTreeSkeleton;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.color.SpectrumColoringModel;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumAppearanceListener;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumSearchResultsListener;
import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectrumHypertreeSearch;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumListIO;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

/**
 * Base model for dealing with spectrum searches.  Contains references to the coloring model, as well as listeners for results and appearance listeners.
 * @author steelrj1
 *
 */
public class BaseSpectrumSearchModel<S extends BasicSpectrum> implements ISpectrumSearchModel<S>, MetadataManager
{
	protected SpectrumColoringModel coloringModel;
    protected BasicSpectrumInstrument instrument;
    protected List<S> results = new ArrayList<S>();
    protected IdPair resultIntervalCurrentlyShown = null;
    private Vector<SpectrumSearchResultsListener<S>> resultsListeners;
    private Vector<SpectrumAppearanceListener> appearanceListeners;
    protected ImmutableSet<S> selectedSpectra;
    protected int[] selectedSpectraIndices;

    protected boolean currentlyEditingUserDefinedFunction = false;
    private TreePath[] selectedPaths;

    private int numberOfBoundariesToShow;
    final Key<List<S>> spectraKey = Key.of("spectraResults");
    protected String customDataFolder;

    public BaseSpectrumSearchModel(
    		ModelManager modelManager,
    		BasicSpectrumInstrument instrument)
    {
        this.instrument = instrument;
        this.resultsListeners = new Vector<SpectrumSearchResultsListener<S>>();
        this.appearanceListeners = new Vector<SpectrumAppearanceListener>();
        coloringModel = new SpectrumColoringModel();
    }

    /**
     * Returns the current search results
     * @return
     */
    public List<S> getSpectrumRawResults()
    {
        return results;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.gui.spectrum.model.ISpectrumSearchModel#setSpectrumRawResults(java.util.List)
     */
    @Override
    public void setSpectrumRawResults(List<S> spectrumRawResults)
    {
        this.results = spectrumRawResults;
        this.resultIntervalCurrentlyShown = new IdPair(0, numberOfBoundariesToShow);
        showFootprints(resultIntervalCurrentlyShown);
        fireResultsChanged();
        fireResultsCountChanged(this.results.size());
    }

    /**
     * Returns the custom data folder for this model
     * @param customFolderName
     */
    public void setCustomDataFolder(String customFolderName)
    {
    	this.customDataFolder = customFolderName;
    }

    public String getCustomDataFolder()
	{
		return customDataFolder;
	}

	/**
     * Returns the currently displayed interval of spectra shown
     * @return
     */
    public IdPair getResultIntervalCurrentlyShown()
    {
        return resultIntervalCurrentlyShown;
    }

    /**
     * Sets the displayed interval of spectra shown
     * @param resultIntervalCurrentlyShown
     */
    public void setResultIntervalCurrentlyShown(IdPair resultIntervalCurrentlyShown)
    {
        this.resultIntervalCurrentlyShown = resultIntervalCurrentlyShown;
    }

    /**
     * Returns state describing whether the user defined color function is being edited
     * @return
     */
    public boolean isCurrentlyEditingUserDefinedFunction()
    {
        return currentlyEditingUserDefinedFunction;
    }

    /**
     * Updates the state describing whether the user defined color function is being edited
     * @param currentlyEditingUserDefinedFunction
     */
    public void setCurrentlyEditingUserDefinedFunction(
            boolean currentlyEditingUserDefinedFunction)
    {
        this.currentlyEditingUserDefinedFunction = currentlyEditingUserDefinedFunction;
    }

    /**
     * Returns the instrument for this search
     * @return
     */
    public BasicSpectrumInstrument getInstrument()
    {
        return instrument;
    }

    /**
     * Returns the model name
     * @return
     */
    public ModelNames getSpectrumCollectionModelName()
    {
        return ModelNames.SPECTRA;
    }

    /**
     * Returns the boundary model name
     * @return
     */
    public ModelNames getSpectrumBoundaryCollectionModelName()
    {
        return ModelNames.SPECTRA_BOUNDARIES;
    }

    /**
     * As long as the user defined coloring is not being edit, issues the call to the coloring model to update the coloring
     */
    public void updateColoring()
    {
        if (isCurrentlyEditingUserDefinedFunction())
            return;
        coloringModel.updateColoring();
    }

    /**
     * Notifies listeners to update the currently shown footprints to those specified by the indices in <pre>idPair</pre>
     * @param idPair
     */
    public void showFootprints(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= getSpectrumRawResults().size())
                break;
            fireFootprintVisibilityChanged(results.get(i), true);
        }
        updateColoring();
    }

    /**
     * Saves the selected spectra to a file
     * @param file
     * @param selectedIndices
     * @throws Exception
     */
    public void saveSelectedSpectrumListToFile(File file, int[] selectedIndices) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	SpectrumListIO.saveSelectedSpectrumListButtonActionPerformed(customDataFolder, file, results, selectedIndices);
    }

    /**
     * Saves the entire list of spectra to a file
     * @param file
     * @throws Exception
     */
    public void saveSpectrumListToFile(File file) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	SpectrumListIO.saveSpectrumListButtonActionPerformed(customDataFolder, file, results);
    }

    /**
     * Loads spectra from the given file.  Calls a completion block upon successful load to update the displays
     * @param file
     * @throws Exception
     */
    public void loadSpectrumListFromFile(File file) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	File metadataFile = new File(customDataFolder + File.separator + file.getName() + ".metadata");
    	SpectrumListIO.loadSpectrumListButtonActionPerformed(file, new ArrayList<S>(), instrument, new Runnable()
		{

			@Override
			public void run()
			{
				fireResultsChanged();
				setResultIntervalCurrentlyShown(new IdPair(0, getNumberOfBoundariesToShow()));
		        showFootprints(getResultIntervalCurrentlyShown());
			}
		});
    }

    /**
     * Performs a basic (non-hypertree) search.  May or may not be hierarchical.
     * @param searchParameters
     * @param cubeList
     * @param hasHierarchicalSpectraSearch
     * @param hierarchicalSpectraSearchSpecification
     * @param selectedPath
     * @param progressListener
     */
    public void performSearch(SpectrumSearchParametersModel searchParameters, TreeSet<Integer> cubeList,
    							boolean hasHierarchicalSpectraSearch, SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification,
    							TreePath[] selectedPath, SearchProgressListener progressListener)
    {
        results.clear();
        SpectraHierarchicalSearchSpecification spectraSpec = null;
        if (hasHierarchicalSpectraSearch) {	spectraSpec = hierarchicalSpectraSearchSpecification.clone(); }
        setSpectrumRawResults(new SpectrumStandardSearch(searchParameters, hasHierarchicalSpectraSearch, spectraSpec).search(instrument, cubeList, selectedPath, progressListener));
    }

    /**
     * Performs a hypertree search.  May or may not be hierarchical
     * @param searchParameters
     * @param cubeList
     * @param skeleton
     * @param hbb
     * @param spectraHypertreeDataSpecName
     * @param hasHypertreeBasedSpectraSearch
     * @param hasHierarchicalSpectraSearch
     * @param hierarchicalSpectraSearchSpecification
     * @param progressListener
     * @throws HyperDimensionMismatchException
     */
    public void performHypertreeSearch(SpectrumSearchParametersModel searchParameters, TreeSet<Integer> cubeList,
    									BoundedObjectHyperTreeSkeleton skeleton, HyperBox hbb,
    									String spectraHypertreeDataSpecName, boolean hasHypertreeBasedSpectraSearch, boolean hasHierarchicalSpectraSearch,
    									SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification, SearchProgressListener progressListener) throws HyperDimensionMismatchException
    {
		results.clear();
		SpectraHierarchicalSearchSpecification spectraSpec = null;
		if (hasHierarchicalSpectraSearch) { spectraSpec = hierarchicalSpectraSearchSpecification.clone(); }
		setSpectrumRawResults(new SpectrumHypertreeSearch(searchParameters, spectraHypertreeDataSpecName, hasHypertreeBasedSpectraSearch, spectraSpec).search(cubeList, skeleton, hbb, instrument, progressListener));
    }

    /**
     * Fires listeners when results change
     */
    protected void fireResultsChanged()
    {
        for (SpectrumSearchResultsListener<S> listener : resultsListeners)
        {
            listener.resultsChanged(results);
        }
    }

    /**
     * Fires listeners when the result count changes
     * @param count
     */
    protected void fireResultsCountChanged(int count)
    {
        for (SpectrumSearchResultsListener<S> listener : resultsListeners)
        {
            listener.resultsCountChanged(count);
        }
    }

    /**
     * Fires listeners when the results are cleared
     */
    protected void fireResultsCleared()
    {
        for (SpectrumSearchResultsListener<S> listener : resultsListeners)
        {
            listener.resultsRemoved();
        }
    }

    /**
     * Adds a results changed listener
     * @param listener
     */
    public void addResultsChangedListener(SpectrumSearchResultsListener<S> listener)
    {
        resultsListeners.add(listener);
    }

    /**
     * Removes a results changed listener
     * @param listener
     */
    public void removeResultsChangedListener(SpectrumSearchResultsListener<S> listener)
    {
        resultsListeners.remove(listener);
    }

    /**
     * Removes all results changed listeners
     */
    public void removeAllResultsChangedListeners()
    {
        resultsListeners.removeAllElements();
    }

    /**
     * Fires the footprint visibility changed listener
     * @param spectrum
     * @param isVisible
     */
    public void fireFootprintVisibilityChanged(S spectrum, boolean isVisible)
    {
        for (SpectrumAppearanceListener listener : appearanceListeners)
        {
            listener.spectrumFootprintVisbilityChanged(spectrum, isVisible);
        }
    }

    /**
     * Fires the boundary visibility changed listener
     * @param spectrum
     * @param isVisible
     */
    public void fireBoundaryVisibilityCountChanged(S spectrum, boolean isVisible)
    {
        for (SpectrumAppearanceListener listener : appearanceListeners)
        {
            listener.spectrumBoundaryVisibilityChanged(spectrum, isVisible);
        }
    }

    /**
     * Adds an appearance changed listener
     * @param listener
     */
    public void addAppearanceChangedListener(SpectrumAppearanceListener listener)
    {
        appearanceListeners.add(listener);
    }

    /**
     * Removes an appearance changed listener
     * @param listener
     */
    public void removeAppearanceChangedListener(SpectrumAppearanceListener listener)
    {
        appearanceListeners.remove(listener);
    }

    /**
     * Removes all appearance changed listener
     */
    public void removeAllAppearanceChangedListeners()
    {
        appearanceListeners.removeAllElements();
    }

    /**
     * Fires the coloring changed listener on the coloring model
     */
    public void coloringOptionChanged()
    {
        coloringModel.fireColoringChanged();
    }

//    /**
//     * @return
//     */
//    public SpectrumKeyInterface[] getSelectedSpectrumKeys()
//    {
//        int[] indices = selectedImageIndices;
//        SpectrumKeyInterface[] selectedKeys = new SpectrumKeyInterface[indices.length];
//        if (indices.length > 0)
//        {
//            int i=0;
//            for (int index : indices)
//            {
//                String image = results.get(index).getSpectrumName();
//                SpectrumKeyInterface selectedKey = createSpectrumKey(image, instrument);
//                selectedKeys[i++] = selectedKey;
//            }
//        }
//        return selectedKeys;
//    }

    /**
     * Helper method to clear the list of spectra
     */
    public void clearSpectraFromDisplay()
    {
        fireResultsCleared();
        setResultIntervalCurrentlyShown(null);
    }


    /**
     * Sets which spectra are currently selected
     * @param selectedImageIndex
     */
    public void setSelectedSpectra(ImmutableSet<S> selectedSpectra)
    {
        this.selectedSpectra = selectedSpectra;
    }

    /**
     * Gets the array of currently selected spectra
     * @return
     */
    public ImmutableSet<S> getSelectedSpectra()
    {
        return selectedSpectra;
    }

    /**
     * Sets which indices are currently selected
     * @param selectedImageIndex
     */
    public void setSelectedSpectraIndices(int[] selectedSpectraIndices)
    {
        this.selectedSpectraIndices = selectedSpectraIndices;
    }

    /**
     * Gets the array of currently selected indices
     * @return
     */
    public int[] getSelectedSpectraIndices()
    {
        return selectedSpectraIndices;
    }

//    /**
//     * @param boundaryName
//     * @param instrument
//     * @return
//     */
//    public List<SpectrumKeyInterface> createSpectrumKeys(String boundaryName, BasicSpectrumInstrument instrument)
//    {
//        List<SpectrumKeyInterface> result = new ArrayList<SpectrumKeyInterface>();
//        result.add(createSpectrumKey(boundaryName, instrument));
//        return result;
//    }
//
//    /**
//     * @param imagePathName
//     * @param instrument
//     * @return
//     */
//    public SpectrumKeyInterface createSpectrumKey(String imagePathName, BasicSpectrumInstrument instrument)
//    {
//        SpectrumKeyInterface key = new SpectrumKey(imagePathName, null, null, instrument, "");
//        return key;
//    }

    /**
     * Returns the selection path in a hierarchical display
     * @return
     */
    public TreePath[] getSelectedPath()
    {
        return selectedPaths;
    }

    /**
     * Sets the selection path in a hierarchical display
     * @param selectedPath
     */
    public void setSelectedPath(TreePath[] selectedPath)
    {
        this.selectedPaths = selectedPath;
    }

    /**
     * Returns the number of boundaries to show
     * @return
     */
    public int getNumberOfBoundariesToShow()
    {
        return numberOfBoundariesToShow;
    }

    /**
     * Sets the number of boundaries to show
     * @param numberOfBoundariesToShow
     */
    public void setNumberOfBoundariesToShow(int numberOfBoundariesToShow)
    {
        this.numberOfBoundariesToShow = numberOfBoundariesToShow;
    }

	/**
	 * Returns the coloring model
	 * @return
	 */
	public SpectrumColoringModel getColoringModel()
	{
		return coloringModel;
	}

	/**
	 * Stores the model to metadata
	 */
	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    	result.put(spectraKey, results);
    	return result;
	}

	/**
	 * Fetches the model from metadata
	 */
	@Override
	public void retrieve(Metadata source)
	{
		results = source.get(spectraKey);
	}
}
