package edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedSpectrumIOHelper;
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

	private PlannedInstrumentRendererManager renderManager;

	public PlannedSpectrumCollection()
	{
		renderManager = new PlannedInstrumentRendererManager(this.pcs);
	}

	@Override
	public List<vtkProp> getProps()
	{
		// TODO Auto-generated method stub
		return null;
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
	}

	/**
	 * @param run
	 */
	public void addSpectrumToList(PlannedSpectrum image)
	{
		plannedSpectra.add(image);
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
	public vtkActor addSpectrum(PlannedSpectrum run, SmallBodyModel model)
	{
		return null;
//		return renderManager.addPlannedData(run, model);
	}

	/**
	 * @param key
	 */
	public void removeSpectrum(PlannedSpectrum image)
	{
		renderManager.removePlannedData(image);
	}

	/**
	 * @param keys
	 */
	public void removeRuns(PlannedSpectrum[] keys)
	{
		for (PlannedSpectrum key : keys)
		{
			removeSpectrum(key);
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
		for (PlannedSpectrum image : getAllItems())
		{
			renderManager.setVisibility(image, plannedSpectra.contains(image));
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

}
