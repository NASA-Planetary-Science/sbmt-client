package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import vtk.vtkProp;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedImageIOHelper;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedInstrumentDataActor;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedInstrumentRendererManager;

import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import glum.item.ItemEventType;

public class PlannedImageCollection extends SaavtkItemManager<PlannedImage>
		implements PropertyChangeListener, MetadataManager
{
	/**
	 *
	 */
	private List<PlannedImage> plannedImages = new ArrayList<PlannedImage>();

	private PlannedInstrumentRendererManager renderManager;

	public PlannedImageCollection()
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
	public void addImageToList(PlannedImage image)
	{
		System.out.println("PlannedImageCollection: addImageToList: adding image");
		plannedImages.add(image);
		setAllItems(plannedImages);
	}

	public void notify(Object obj, ItemEventType type)
	{
		notifyListeners(obj, type);
	}

	/**
	 * @param run
	 * @return
	 */
	public PlannedInstrumentDataActor addImage(PlannedImage run, SmallBodyModel model)
	{
		return renderManager.addPlannedData(run, model);
	}

	/**
	 * @param key
	 */
	public void removeImage(PlannedImage image)
	{
		renderManager.removePlannedData(image);
	}

	/**
	 * @param keys
	 */
	public void removeRuns(PlannedImage[] keys)
	{
		for (PlannedImage key : keys)
		{
			removeImage(key);
		}
	}

	/**
	 *
	 */
	@Override
	public ImmutableList<PlannedImage> getAllItems()
	{
		return ImmutableList.copyOf(plannedImages);
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return plannedImages.size();
	}

	/**
	 * @param history
	 */
	public void setOthersHiddenExcept(List<PlannedImage> plannedImages)
	{
		for (PlannedImage image : getAllItems())
		{
			renderManager.setVisibility(image, plannedImages.contains(image));
		}
	}

	public void loadPlannedImagesFromFileWithName(String filename) throws IOException
	{
		PlannedImageIOHelper.loadPlannedImagesFromFileWithName(filename, this);
		System.out.println("PlannedImageCollection: loadPlannedImagesFromFileWithName: number of images " + getNumItems());
	}

	public void savePlannedImagesToFileWithName(String filename) throws IOException
	{
		PlannedImageIOHelper.savePlannedImagesToFileWithName(filename, this);
	}

}
