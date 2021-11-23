package edu.jhuapl.sbmt.image2.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipeline.active.RenderableImageActorPipeline;

import crucible.crust.logging.SimpleLogger;

public class PerspectiveImageCollection extends SaavtkItemManager<PerspectiveImage> implements PropertyChangeListener
{
	private List<PerspectiveImage> images;
	private List<SmallBodyModel> smallBodyModels;
	private HashMap<PerspectiveImage, List<vtkActor>> imageRenderers;
	private SimpleLogger logger = SimpleLogger.getInstance();


	public PerspectiveImageCollection(List<SmallBodyModel> smallBodyModels)
	{
		this.imageRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
		this.smallBodyModels = smallBodyModels;
	}

	public void setImages(List<PerspectiveImage> images)
	{
		this.images = images;
		setAllItems(images);
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public List<vtkProp> getProps()
	{
		List<vtkProp> props = Lists.newArrayList();
		if (images == null) return props;
		for (List<vtkActor> actors : imageRenderers.values())
		{
			props.addAll(actors);
		}
		return props;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void setImageMapped(PerspectiveImage image, boolean mapped)
	{
		image.setMapped(mapped);
		if (imageRenderers.get(image) == null)
		{
			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					image.setStatus("Loading...");
					RenderableImageActorPipeline pipeline = null;
					try
					{
						pipeline = new RenderableImageActorPipeline(image, smallBodyModels);
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (pipeline == null) return;
					List<vtkActor> actors = pipeline.getRenderableImageActors();
					imageRenderers.put(image, actors);
					for (vtkActor actor : actors)
					{
						actor.SetVisibility(mapped ? 1 : 0);
					}
					image.setStatus("Loaded");
					SwingUtilities.invokeLater( () -> {
//						renderer.shiftFootprint();
						pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
					});
				}
			});
			thread.start();
		}
		else
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public boolean getImageMapped(PerspectiveImage image)
	{
		return image.isMapped();
	}

	public void setImageFrustumVisible(PerspectiveImage image, boolean visible)
	{
		image.setFrustumShowing(visible);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public boolean getFrustumShowing(PerspectiveImage image)
	{
		return image.isFrustumShowing();
	}

	public void setImageOfflimbShowing(PerspectiveImage image, boolean showing)
	{
		image.setOfflimbShowing(showing);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public boolean getImageOfflimbShowing(PerspectiveImage image)
	{
		return image.isOfflimbShowing();
	}

	public void setImageBoundaryShowing(PerspectiveImage image, boolean showing)
	{
		image.setBoundaryShowing(showing);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public boolean getImageBoundaryShowing(PerspectiveImage image)
	{
		return image.isBoundaryShowing();
	}

	public void setImageStatus(PerspectiveImage image, String status)
	{
		image.setStatus(status);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public int size()
	{
		return getAllItems().size();
	}

}
