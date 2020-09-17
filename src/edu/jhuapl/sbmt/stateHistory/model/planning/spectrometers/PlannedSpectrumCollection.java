package edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import vtk.vtkProp;

import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.CustomizableColoringDataManager;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedSpectrumIOHelper;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedInstrumentRendererManager;

import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import glum.item.ItemEventType;

public class PlannedSpectrumCollection extends SaavtkItemManager<PlannedSpectrum>
		implements PropertyChangeListener, MetadataManager
{
	/**
	 *
	 */
	private List<PlannedSpectrum> plannedSpectra = new ArrayList<PlannedSpectrum>();
	private List<vtkProp> footprintActors = new ArrayList<vtkProp>();

	private List<PlannedDataActor> plannedDataActors = new ArrayList<PlannedDataActor>();

	private HashMap<String, ColoringData> nameToColoringMap = new HashMap<String, ColoringData>();

	private PlannedInstrumentRendererManager renderManager;

	private SmallBodyModel smallBodyModel;

	private StateHistory stateHistorySource;

	private CustomizableColoringDataManager coloringDataManager;

	public PlannedSpectrumCollection(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
		this.coloringDataManager = smallBodyModel.getColoringDataManager();
		renderManager = new PlannedInstrumentRendererManager(this.pcs);
	}

	@Override
	public List<vtkProp> getProps()
	{
		if (!footprintActors.isEmpty()) return footprintActors;
		for (PlannedDataActor actor : plannedDataActors)
		{
			footprintActors.add(actor.getFootprintBoundaryActor());

		}
		return footprintActors;
	}

	@Override
	public void retrieve(Metadata arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Metadata store()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

		if (PlannedDataProperties.TIME_CHANGED.equals(evt.getPropertyName()))
		{
			double time = (double)evt.getNewValue();
			if (stateHistorySource == null) return;
			for (PlannedDataActor actor : plannedDataActors)
			{
				//make spectrum version?
				if (((PerspectiveImageFootprint)actor).isStaticFootprintSet() == false)
					StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveImageFootprint)actor);
				actor.getFootprintBoundaryActor().SetVisibility(time > actor.getTime() ? 1 : 0);
			}
		}
	}

	/**
	 * @param run
	 */
	public void addSpectrumToList(PlannedSpectrum spectrum)
	{
		plannedSpectra.add(spectrum);
		PerspectiveImageFootprint actor = (PerspectiveImageFootprint)addSpectrumToRenderer(spectrum);
		actor.setStaticFootprint(true);
		plannedDataActors.add(actor);
		setAllItems(plannedSpectra);
	}

	public void notify(Object obj, ItemEventType type)
	{
		notifyListeners(obj, type);
	}

	/**
	 * @param run
	 * @return
	 */
	public PlannedDataActor addSpectrumToRenderer(PlannedSpectrum spectrum)
	{
		return renderManager.addPlannedData(spectrum, smallBodyModel);
	}

	/**
	 * @param key
	 */
	public void removeSpectrumFromRenderer(PlannedSpectrum spectrum)
	{
		renderManager.removePlannedData(spectrum);
	}


	/**
	 * @param keys
	 */
	public void removeRuns(PlannedSpectrum[] keys)
	{
		for (PlannedSpectrum key : keys)
		{
			removeSpectrumFromRenderer(key);
		}
	}


	/**
	 *
	 */
	@Override
	public ImmutableList<PlannedSpectrum> getAllItems()
	{
		return ImmutableList.copyOf(plannedSpectra);
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return plannedSpectra.size();
	}

	/**
	 * @param history
	 */
	public void setOthersHiddenExcept(List<PlannedSpectrum> plannedSpectra)
	{
		for (PlannedSpectrum spectrum : getAllItems())
		{
			renderManager.setVisibility(spectrum, plannedSpectra.contains(spectrum));
		}
	}

	public void loadPlannedSpectraFromFileWithName(String filename) throws IOException
	{
		PlannedSpectrumIOHelper.loadPlannedSpectraFromFileWithName(filename, this);
	}

	public void savePlannedSpectraToFileWithName(String filename) throws IOException
	{
		PlannedSpectrumIOHelper.savePlannedSpectraToFileWithName(filename, this);
	}

	public void updateStateHistorySource(StateHistory stateHistory)
	{
		this.stateHistorySource = stateHistory;
	}

}
