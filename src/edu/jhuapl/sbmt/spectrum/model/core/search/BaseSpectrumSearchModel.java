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
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumSearchModel;
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
    protected BasicSpectrumInstrument instrument;
    protected List<S> results = new ArrayList<S>();
    protected IdPair resultIntervalCurrentlyShown = null;
    private Vector<SpectrumSearchResultsListener<S>> resultsListeners;
    protected ImmutableSet<S> selectedSpectra;
    protected int[] selectedSpectraIndices;

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
    	results.clear();

    	Preconditions.checkNotNull(customDataFolder);
    	SpectrumListIO.loadSpectrumListButtonActionPerformed(file, results, instrument, new Runnable()
		{
			@Override
			public void run()
			{
				fireResultsChanged();
				setResultIntervalCurrentlyShown(new IdPair(0, getNumberOfBoundariesToShow()));
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
