package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.tree.TreePath;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.IdPair;
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
import edu.jhuapl.sbmt.spectrum.model.key.SpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumKeyInterface;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class BaseSpectrumSearchModel implements ISpectrumSearchModel, MetadataManager
{
	protected SpectrumColoringModel coloringModel;
    protected BasicSpectrumInstrument instrument;
    protected List<BasicSpectrum> results = new ArrayList<BasicSpectrum>();
    protected IdPair resultIntervalCurrentlyShown = null;
    private Vector<SpectrumSearchResultsListener> resultsListeners;
    private Vector<SpectrumAppearanceListener> appearanceListeners;
    protected int[] selectedImageIndices;

    protected boolean currentlyEditingUserDefinedFunction = false;
    private TreePath[] selectedPaths;

    private int numberOfBoundariesToShow;
    final Key<List<BasicSpectrum>> spectraKey = Key.of("spectraResults");
    protected String customDataFolder;

    public BaseSpectrumSearchModel(
    		ModelManager modelManager,
    		BasicSpectrumInstrument instrument)
    {
        this.instrument = instrument;
        this.resultsListeners = new Vector<SpectrumSearchResultsListener>();
        this.appearanceListeners = new Vector<SpectrumAppearanceListener>();
        coloringModel = new SpectrumColoringModel();
    }

    public List<BasicSpectrum> getSpectrumRawResults()
    {
        return results;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.gui.spectrum.model.ISpectrumSearchModel#setSpectrumRawResults(java.util.List)
     */
    @Override
    public void setSpectrumRawResults(List<BasicSpectrum> spectrumRawResults)
    {
        this.results = spectrumRawResults;
        this.resultIntervalCurrentlyShown = new IdPair(0, numberOfBoundariesToShow);
        showFootprints(resultIntervalCurrentlyShown);
        fireResultsChanged();
        fireResultsCountChanged(this.results.size());
    }

    public void setCustomDataFolder(String customFolderName)
    {
    	this.customDataFolder = customFolderName;
    }

    public IdPair getResultIntervalCurrentlyShown()
    {
        return resultIntervalCurrentlyShown;
    }

    public void setResultIntervalCurrentlyShown(IdPair resultIntervalCurrentlyShown)
    {
        this.resultIntervalCurrentlyShown = resultIntervalCurrentlyShown;
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

    public BasicSpectrumInstrument getInstrument()
    {
        return instrument;
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
        if (isCurrentlyEditingUserDefinedFunction())
            return;
        coloringModel.updateColoring(instrument);
    }

    public void showFootprints(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        SpectrumColoringStyle style = SpectrumColoringStyle.getStyleForName(coloringModel.getSpectrumColoringStyleName());
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

    public void saveSelectedSpectrumListToFile(File file, int[] selectedIndices) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	SpectrumListIO.saveSelectedSpectrumListButtonActionPerformed(customDataFolder, file, results, selectedIndices);
    }

    public void saveSpectrumListToFile(File file) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	SpectrumListIO.saveSpectrumListButtonActionPerformed(customDataFolder, file, results);
    }

    public void loadSpectrumListFromFile(File file) throws Exception
    {
    	Preconditions.checkNotNull(customDataFolder);
    	File metadataFile = new File(customDataFolder + File.separator + file.getName() + ".metadata");
    	SpectrumListIO.loadSpectrumListButtonActionPerformed(file, new ArrayList<BasicSpectrum>(), instrument, new Runnable()
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

    public void performSearch(SpectrumSearchParametersModel searchParameters, TreeSet<Integer> cubeList,
    							boolean hasHierarchicalSpectraSearch, SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification,
    							TreePath[] selectedPath)
    {
        results.clear();
        SpectraHierarchicalSearchSpecification spectraSpec = null;
        if (hasHierarchicalSpectraSearch)
        {
        	spectraSpec = hierarchicalSpectraSearchSpecification;
            try
            {
                spectraSpec.loadMetadata();
                spectraSpec = spectraSpec.clone();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                spectraSpec = null;
            }
        }

        setSpectrumRawResults(new SpectrumStandardSearch(searchParameters, hasHierarchicalSpectraSearch, spectraSpec).search(instrument, cubeList, selectedPath));
    }

    public void performHypertreeSearch(SpectrumSearchParametersModel searchParameters, TreeSet<Integer> cubeList,
    									BoundedObjectHyperTreeSkeleton skeleton, HyperBox hbb,
    									String spectraHypertreeDataSpecName, boolean hasHypertreeBasedSpectraSearch, boolean hasHierarchicalSpectraSearch,
    									SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification) throws HyperDimensionMismatchException
    {
		results.clear();
		SpectraHierarchicalSearchSpecification spectraSpec = null;
		if (hasHierarchicalSpectraSearch)
		{
			spectraSpec = hierarchicalSpectraSearchSpecification;
			try
			{
				spectraSpec.loadMetadata();
				spectraSpec = spectraSpec.clone();
			} catch (Exception e)
			{
				e.printStackTrace();
				spectraSpec = null;
			}
		}
		setSpectrumRawResults(new SpectrumHypertreeSearch(searchParameters, spectraHypertreeDataSpecName, hasHypertreeBasedSpectraSearch, spectraSpec).search(cubeList, skeleton, hbb, instrument));

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

    protected void fireResultsCleared()
    {
        for (SpectrumSearchResultsListener listener : resultsListeners)
        {
            listener.resultsRemoved();
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

    public void fireFootprintVisibilityChanged(BasicSpectrum spectrum, boolean isVisible)
    {
        for (SpectrumAppearanceListener listener : appearanceListeners)
        {
            listener.spectrumFootprintVisbilityChanged(spectrum, isVisible);
        }
    }

    public void fireBoundaryVisibilityCountChanged(BasicSpectrum spectrum, boolean isVisible)
    {
        for (SpectrumAppearanceListener listener : appearanceListeners)
        {
            listener.spectrumBoundaryVisibilityChanged(spectrum, isVisible);
        }
    }

    public void addAppearanceChangedListener(SpectrumAppearanceListener listener)
    {
        appearanceListeners.add(listener);
    }

    public void removeAppearanceChangedListener(SpectrumAppearanceListener listener)
    {
        appearanceListeners.remove(listener);
    }

    public void removeAllAppearanceChangedListeners()
    {
        appearanceListeners.removeAllElements();
    }

    public void coloringOptionChanged()
    {
        coloringModel.fireColoringChanged();
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
                String image = results.get(index).getSpectrumName();
                SpectrumKeyInterface selectedKey = createSpectrumKey(image, instrument);
                selectedKeys[i++] = selectedKey;
            }
        }
        return selectedKeys;
    }

    public void clearSpectraFromDisplay()
    {
        fireResultsCleared();
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

    public List<SpectrumKeyInterface> createSpectrumKeys(String boundaryName, BasicSpectrumInstrument instrument)
    {
        List<SpectrumKeyInterface> result = new ArrayList<SpectrumKeyInterface>();
        result.add(createSpectrumKey(boundaryName, instrument));
        return result;
    }

    public SpectrumKeyInterface createSpectrumKey(String imagePathName, BasicSpectrumInstrument instrument)
    {
        SpectrumKeyInterface key = new SpectrumKey(imagePathName, null, null, instrument, "");
        return key;
    }

    public TreePath[] getSelectedPath()
    {
        return selectedPaths;
    }

    public void setSelectedPath(TreePath[] selectedPath)
    {
        this.selectedPaths = selectedPath;
    }

    public int getNumberOfBoundariesToShow()
    {
        return numberOfBoundariesToShow;
    }

    public void setNumberOfBoundariesToShow(int numberOfBoundariesToShow)
    {
        this.numberOfBoundariesToShow = numberOfBoundariesToShow;
    }

	public SpectrumColoringModel getColoringModel()
	{
		return coloringModel;
	}

	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    	result.put(spectraKey, results);
    	return result;
	}

	@Override
	public void retrieve(Metadata source)
	{
		results = source.get(spectraKey);
	}
}
