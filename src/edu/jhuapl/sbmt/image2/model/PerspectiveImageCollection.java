package edu.jhuapl.sbmt.image2.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.swing.SwingUtilities;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import vtk.vtkActor;
import vtk.vtkProp;
import vtk.vtkProperty;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.pipeline.active.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.active.cylindricalImages.RenderableCylindricalImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.active.pointedImages.RenderablePointedImageActorPipeline;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageType;

import crucible.crust.logging.SimpleLogger;
import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

public class PerspectiveImageCollection extends SaavtkItemManager<PerspectiveImage> implements PropertyChangeListener
{
	private List<PerspectiveImage> images;
	private List<PerspectiveImage> userImages;
	private List<SmallBodyModel> smallBodyModels;
	private HashMap<PerspectiveImage, List<vtkActor>> imageRenderers;
	private HashMap<PerspectiveImage, List<vtkActor>> boundaryRenderers;
	private HashMap<PerspectiveImage, List<vtkActor>> frustumRenderers;
	private HashMap<PerspectiveImage, List<vtkActor>> offLimbRenderers;
	private HashMap<PerspectiveImage, PerspectiveImageRenderingState> renderingStates;
	private SimpleLogger logger = SimpleLogger.getInstance();
	private IImagingInstrument imagingInstrument;

	class PerspectiveImageRenderingState
	{
		boolean isMapped = false;
		boolean isFrustumShowing = false;
		boolean isBoundaryShowing = false;
		boolean isOfflimbShowing = false;
		Color boundaryColor;
		Color frustumColor = Color.green;
	}


	public PerspectiveImageCollection(List<SmallBodyModel> smallBodyModels)
	{
		this.images = Lists.newArrayList();
		this.userImages = Lists.newArrayList();
		this.imageRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
		this.boundaryRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
		this.frustumRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
		this.offLimbRenderers = new HashMap<PerspectiveImage, List<vtkActor>>();
		this.renderingStates = new HashMap<PerspectiveImage, PerspectiveImageRenderingState>();
		this.smallBodyModels = smallBodyModels;
	}

	public void addUserImage(PerspectiveImage image)
	{
		userImages.add(image);
		PerspectiveImageRenderingState state = new PerspectiveImageRenderingState();
		renderingStates.put(image,state);
		updateUserList();	//update the user created list, stored in metadata
		//TODO merge with searched for images, which will cause a refresh.  Put custom images at top?
		List<PerspectiveImage> combined = Lists.newArrayList();
		combined.addAll(userImages);
		combined.addAll(images);
		setAllItems(combined);

	}

	private void loadUserList()
	{
		String filename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "userImages" + imagingInstrument.getType() + ".txt";
        if (!new File(filename).exists()) return;
		FixedMetadata metadata;
        try
        {
        	final Key<List<PerspectiveImage>> userImagesKey = Key.of("UserImages");
            metadata = Serializers.deserialize(new File(filename), "UserImages");
            userImages = read(userImagesKey, metadata);
            for (PerspectiveImage image : userImages)
            {
            	PerspectiveImageRenderingState state = new PerspectiveImageRenderingState();
            	state.isMapped = image.isMapped();
            	if (image.isMapped()) image.setStatus("Loaded");
            	state.isFrustumShowing = image.isFrustumShowing();
            	state.isBoundaryShowing = image.isBoundaryShowing();
            	state.isOfflimbShowing = image.isOfflimbShowing();
        		renderingStates.put(image,state);
        		updateImage(image);
            }
            List<PerspectiveImage> combined = Lists.newArrayList();
    		combined.addAll(userImages);
    		combined.addAll(images);
    		setAllItems(combined);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	private void updateUserList()
	{
		String filename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "userImages" + imagingInstrument.getType() + ".txt";
		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        final Key<List<PerspectiveImage>> userImagesKey = Key.of("UserImages");
        write(userImagesKey, userImages, configMetadata);
        try
        {
            Serializers.serialize("UserImages", configMetadata, new File(filename));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	protected <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value);
        }
    }

    protected <T> T read(Key<T> key, Metadata configMetadata)
    {
        T value = configMetadata.get(key);
        if (value != null)
            return value;
        return null;
    }

	public void setImages(List<PerspectiveImage> images)
	{
		List<PerspectiveImage> combined = Lists.newArrayList();
		combined.addAll(userImages);
		combined.addAll(images);
		setAllItems(combined);
		for (PerspectiveImage image : images)
		{
			if (renderingStates.get(image) != null) continue;
			PerspectiveImageRenderingState state = new PerspectiveImageRenderingState();
			renderingStates.put(image,state);
		}
	}

	@Override
	public ImmutableList<PerspectiveImage> getAllItems()
	{
		return ImmutableList.copyOf(super.getAllItems().stream().filter(image -> image.getImageType() == imagingInstrument.getType() || image.getImageType() == ImageType.GENERIC_IMAGE).toList());
	}

	@Override
	public ImmutableSet<PerspectiveImage> getSelectedItems()
	{
		return ImmutableSet.copyOf(super.getSelectedItems().stream().filter(image -> image.getImageType() == imagingInstrument.getType() || image.getImageType() == ImageType.GENERIC_IMAGE).toList());
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

	public static double[] colorToDoubleArray(Color color) {
		return new double[] { (double) color.getRed() / 255., (double) color.getGreen() / 255.,
				(double) color.getBlue() / 255. };
	}

	private void updatePipeline(PerspectiveImage image, RenderableImageActorPipeline pipeline)
	{
		imageRenderers.put(image, pipeline.getRenderableImageActors());
		imageRenderers.get(image).forEach(actor -> actor.SetVisibility(renderingStates.get(image).isMapped ? 1 : 0));

		frustumRenderers.put(image, pipeline.getRenderableImageFrustumActors());
		frustumRenderers.get(image).forEach(actor -> {
			actor.SetVisibility(renderingStates.get(image).isFrustumShowing ? 1 : 0);
			actor.GetProperty().SetColor(colorToDoubleArray(renderingStates.get(image).frustumColor));
		});
		offLimbRenderers.put(image, pipeline.getRenderableOfflimbImageActors());
		offLimbRenderers.get(image).forEach(actor -> actor.SetVisibility(renderingStates.get(image).isOfflimbShowing ? 1 : 0));

		boundaryRenderers.put(image, pipeline.getRenderableImageBoundaryActors());
		boundaryRenderers.get(image).forEach(actor -> {
			actor.SetVisibility(renderingStates.get(image).isBoundaryShowing ? 1 : 0);
			if (renderingStates.get(image).boundaryColor == null)
			{
				Color color = ColorUtil.generateColor(images.indexOf(image)%100, 100);
				renderingStates.get(image).boundaryColor = color;
			}
			actor.GetProperty().SetColor(colorToDoubleArray(renderingStates.get(image).boundaryColor));
		});
	}

	public void updateImage(PerspectiveImage image)
	{
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				RenderableImageActorPipeline pipeline = null;
				image.setStatus("Loading...");
				try
				{
					if (image.getImageType() != ImageType.GENERIC_IMAGE)
					{
						pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);

					}
					else
					{
						pipeline = new RenderableCylindricalImageActorPipeline(image.getFilename(), image.getBounds(), smallBodyModels);

					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (pipeline == null) return;
				updatePipeline(image, pipeline);
				image.setStatus("Loaded");
				SwingUtilities.invokeLater(() -> { pcs.firePropertyChange(Properties.MODEL_CHANGED, null, image); });
			}
		});
		thread.start();

		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, image);
	}

	public void updateUserImage(PerspectiveImage image)
	{
		RenderablePointedImageActorPipeline pipeline = null;
		try
		{
			pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pipeline == null) return;
		updatePipeline(image, pipeline);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, image);
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
						if (image.getImageType() != ImageType.GENERIC_IMAGE)
						{
							pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);

						}
						else
						{
							pipeline = new RenderableCylindricalImageActorPipeline(image.getFilename(), image.getBounds(), smallBodyModels);

						}
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (pipeline == null) return;
					updatePipeline(image, pipeline);
					for (vtkActor actor : imageRenderers.get(image))
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
		renderingStates.get(image).isMapped = mapped;
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
					RenderablePointedImageActorPipeline pipeline = null;
					try
					{
						pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (pipeline == null) return;
					updatePipeline(image, pipeline);
					for (vtkActor actor : frustumRenderers.get(image))
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
		renderingStates.get(image).isFrustumShowing = visible;
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
					RenderablePointedImageActorPipeline pipeline = null;
					try
					{
						pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (pipeline == null) return;
					updatePipeline(image, pipeline);
					for (vtkActor actor : offLimbRenderers.get(image))
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
		renderingStates.get(image).isOfflimbShowing = showing;
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
						if (image.getImageType() != ImageType.GENERIC_IMAGE)
						{
							pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);

						}
						else
						{
							pipeline = new RenderableCylindricalImageActorPipeline(image.getFilename(), image.getBounds(), smallBodyModels);

						}
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (pipeline == null) return;
					updatePipeline(image, pipeline);
					for (vtkActor actor : boundaryRenderers.get(image))
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
		renderingStates.get(image).isBoundaryShowing = showing;
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

	@Override
	public void setOpacity(double opacity)
	{
		List<vtkActor> actors = imageRenderers.get(getSelectedItems().asList().get(0));
		actors.forEach(actor -> {
			vtkProperty interiorProperty = actor.GetProperty();
			interiorProperty.SetOpacity(opacity);
		});
		super.setOpacity(opacity);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public double getOpacity()
	{
		List<vtkActor> actors = imageRenderers.get(getSelectedItems().asList().get(0));
		vtkActor actor = actors.get(0);
		vtkProperty interiorProperty = actor.GetProperty();
		return interiorProperty.GetOpacity();

	}

	public List<SmallBodyModel> getSmallBodyModels()
	{
		return smallBodyModels;
	}

	public void setImagingInstrument(IImagingInstrument imagingInstrument)
	{
		this.imagingInstrument = imagingInstrument;
		loadUserList();
	}

}
