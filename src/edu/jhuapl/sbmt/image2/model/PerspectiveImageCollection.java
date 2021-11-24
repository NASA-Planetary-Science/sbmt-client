package edu.jhuapl.sbmt.image2.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
	private HashMap<PerspectiveImage, List<vtkActor>> boundaryRenderers;
	private HashMap<PerspectiveImage, List<vtkActor>> frustumRenderers;
	private HashMap<PerspectiveImage, List<vtkActor>> offLimbRenderers;
	private SimpleLogger logger = SimpleLogger.getInstance();


	public PerspectiveImageCollection(List<SmallBodyModel> smallBodyModels)
	{
		this.imageRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
		this.boundaryRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
		this.frustumRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
		this.offLimbRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
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
		for (List<vtkActor> actors : boundaryRenderers.values())
		{
			props.addAll(actors);
		}
		for (List<vtkActor> actors : frustumRenderers.values())
		{
			props.addAll(actors);
		}
		for (List<vtkActor> actors : offLimbRenderers.values())
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
		List<vtkActor> actors = imageRenderers.get(image);
		if (actors == null)
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
		{
			for (vtkActor actor : actors)
			{
				actor.SetVisibility(mapped ? 1 : 0);
			}
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public boolean getImageMapped(PerspectiveImage image)
	{
		return image.isMapped();
	}

	public String getImageStatus(PerspectiveImage image)
	{
		return image.getStatus();
	}

	public String getImageOrigin(PerspectiveImage image)
	{
		return image.getImageOrigin();
	}

	public int getImageNumberOfLayers(PerspectiveImage image)
	{
		return image.getNumberOfLayers();
	}

	public void setImageFrustumVisible(PerspectiveImage image, boolean visible)
	{
		image.setFrustumShowing(visible);
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		List<vtkActor> actors = frustumRenderers.get(image);
		if (actors == null)
		{
			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
//					image.setStatus("Loading...");
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
					List<vtkActor> actors = pipeline.getRenderableImageFrustumActors();
					frustumRenderers.put(image, actors);
					for (vtkActor actor : actors)
					{
						actor.SetVisibility(visible? 1 : 0);
					}
//					image.setStatus("Loaded");
					SwingUtilities.invokeLater( () -> {
//						renderer.shiftFootprint();
						pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
					});
				}
			});
			thread.start();
		}
		else
		{
			for (vtkActor actor : actors)
			{
				actor.SetVisibility(visible ? 1 : 0);
			}
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	public boolean getFrustumShowing(PerspectiveImage image)
	{
		return image.isFrustumShowing();
	}

	public void setImageOfflimbShowing(PerspectiveImage image, boolean showing)
	{
		image.setOfflimbShowing(showing);
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		List<vtkActor> actors = offLimbRenderers.get(image);
		if (actors == null)
		{
			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
//					image.setStatus("Loading...");
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
					List<vtkActor> actors = pipeline.getRenderableOfflimbImageActors();
					offLimbRenderers.put(image, actors);
					for (vtkActor actor : actors)
					{
						actor.SetVisibility(showing? 1 : 0);
					}
//					image.setStatus("Loaded");
					SwingUtilities.invokeLater( () -> {
//						renderer.shiftFootprint();
						pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
					});
				}
			});
			thread.start();
		}
		else
		{
			for (vtkActor actor : actors)
			{
				actor.SetVisibility(showing ? 1 : 0);
			}
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	public boolean getImageOfflimbShowing(PerspectiveImage image)
	{
		return image.isOfflimbShowing();
	}

	public void setImageBoundaryShowing(PerspectiveImage image, boolean showing)
	{
		image.setBoundaryShowing(showing);
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		List<vtkActor> actors = boundaryRenderers.get(image);
		if (actors == null)
		{
			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
//					image.setStatus("Loading...");
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
					List<vtkActor> actors = pipeline.getRenderableImageBoundaryActors();
					boundaryRenderers.put(image, actors);
					for (vtkActor actor : actors)
					{
						actor.SetVisibility(showing? 1 : 0);
					}
//					image.setStatus("Loaded");
					SwingUtilities.invokeLater( () -> {
//						renderer.shiftFootprint();
						pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
					});
				}
			});
			thread.start();
		}
		else
		{
			for (vtkActor actor : actors)
			{
				actor.SetVisibility(showing ? 1 : 0);
			}
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
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

	public Optional<PerspectiveImage> getImage(vtkActor actor)
	{
		Optional<PerspectiveImage> matchingImage = Optional.empty();
		for (PerspectiveImage image : imageRenderers.keySet())
		{
			List<vtkActor> actors = imageRenderers.get(image);
			if (actors.contains(actor))
			{
				matchingImage = Optional.of(image);
			}
		}

		return matchingImage;
	}

}
