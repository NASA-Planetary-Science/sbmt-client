package edu.jhuapl.sbmt.image2.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;

import vtk.vtkActor;
import vtk.vtkProp;
import vtk.vtkProperty;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipeline.active.ColorImageGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.active.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.active.cylindricalImages.RenderableCylindricalImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.active.pointedImages.RenderablePointedImageActorPipeline;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageType;

import crucible.crust.logging.SimpleLogger;
import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.SettableMetadata;

public class PerspectiveImageCollection<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends SaavtkItemManager<G1> implements PropertyChangeListener
{
	private HashMap<IImagingInstrument, List<G1>> imagesByInstrument;
	private List<G1> userImages;
	private List<SmallBodyModel> smallBodyModels;
	private HashMap<G1, List<vtkActor>> imageRenderers;
	private HashMap<G1, List<vtkActor>> boundaryRenderers;
	private HashMap<G1, List<vtkActor>> frustumRenderers;
	private HashMap<G1, List<vtkActor>> offLimbRenderers;
	private HashMap<G1, List<vtkActor>> offLimbBoundaryRenderers;
	private HashMap<G1, PerspectiveImageRenderingState> renderingStates;
	private SimpleLogger logger = SimpleLogger.getInstance();
	private IImagingInstrument imagingInstrument;

	class PerspectiveImageRenderingState
	{
		boolean isMapped = false;
		boolean isFrustumShowing = false;
		boolean isBoundaryShowing = false;
		boolean isOfflimbShowing = false;
		boolean isOffLimbBoundaryShowing = false;
		Color boundaryColor;
		Color offLimbBoundaryColor = Color.red;
		Color frustumColor = Color.green;
		double offLimbFootprintDepth;
		boolean contrastSynced = false;
		IntensityRange imageContrastRange;
		IntensityRange offLimbContrastRange;
	}


	public PerspectiveImageCollection(List<SmallBodyModel> smallBodyModels)
	{
		this.imagesByInstrument = new HashMap<IImagingInstrument, List<G1>>();
		this.userImages = Lists.newArrayList();
		this.imageRenderers = new HashMap<G1, List<vtkActor>>();
		this.boundaryRenderers = new HashMap<G1, List<vtkActor>>();
		this.frustumRenderers = new HashMap<G1, List<vtkActor>>();
		this.offLimbRenderers = new HashMap<G1, List<vtkActor>>();
		this.offLimbBoundaryRenderers = new HashMap<G1, List<vtkActor>>();
		this.renderingStates = new HashMap<G1, PerspectiveImageRenderingState>();
		this.smallBodyModels = smallBodyModels;
	}

	public void addUserImage(G1 image)
	{
		userImages.add(image);
		PerspectiveImageRenderingState state = new PerspectiveImageRenderingState();
		Color color = ColorUtil.generateColor(userImages.indexOf(image)%100, 100);
		state.boundaryColor = color;
		renderingStates.put(image,state);
		updateUserList();	//update the user created list, stored in metadata
		//TODO merge with searched for images, which will cause a refresh.  Put custom images at top?
//		List<PerspectiveImage> combined = Lists.newArrayList();
//		combined.addAll(userImages);
//		for (List<PerspectiveImage> imgs : imagesByInstrument.values())
//			combined.addAll(imgs);
//		setAllItems(userImages);

	}

	private void loadUserList()
	{
//		String filename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "userImages" + imagingInstrument.getType() + ".txt";
//        if (!new File(filename).exists()) return;
//		FixedMetadata metadata;
//        try
//        {
//        	final Key<List<PerspectiveImage>> userImagesKey = Key.of("UserImages");
//            metadata = Serializers.deserialize(new File(filename), "UserImages");
//            userImages = read(userImagesKey, metadata);
//            for (PerspectiveImage image : userImages)
//            {
//            	PerspectiveImageRenderingState state = new PerspectiveImageRenderingState();
//            	state.isMapped = image.isMapped();
//            	if (image.isMapped()) image.setStatus("Loaded");
//            	state.isFrustumShowing = image.isFrustumShowing();
//            	state.isBoundaryShowing = image.isBoundaryShowing();
//            	state.isOfflimbShowing = image.isOfflimbShowing();
//        		renderingStates.put(image,state);
//        		updateImage(image);
//            }
//            List<PerspectiveImage> combined = Lists.newArrayList();
//    		combined.addAll(userImages);
////    		for (List<PerspectiveImage> imgs : imagesByInstrument.values())
////    			combined.addAll(imgs);
//    		setAllItems(combined);
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
	}

	private void updateUserList()
	{
//		String filename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "userImages" + imagingInstrument.getType() + ".txt";
//		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
//        final Key<List<PerspectiveImage>> userImagesKey = Key.of("UserImages");
//        write(userImagesKey, userImages, configMetadata);
//        try
//        {
//            Serializers.serialize("UserImages", configMetadata, new File(filename));
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
	}

	public void removeUserImage(G1 image)
	{
		setImageMapped(image, false);
		setImageBoundaryShowing(image, false);
		userImages.remove(image);
		setAllItems(userImages);
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

	public void setImages(List<G1> images)
	{
//		List<G1> combined = Lists.newArrayList();
////		combined.addAll(userImages);
//		combined.addAll(images);
		setAllItems(images);
		this.imagesByInstrument.put(imagingInstrument, images);
		for (G1 image : images)
		{
			if (renderingStates.get(image) != null) continue;
			PerspectiveImageRenderingState state = new PerspectiveImageRenderingState();
			renderingStates.put(image,state);
		}
	}

	@Override
	public List<vtkProp> getProps()
	{
		List<vtkProp> props = Lists.newArrayList();
		if (imagesByInstrument.isEmpty() && userImages.isEmpty()) return props;
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
		for (List<vtkActor> actors : offLimbBoundaryRenderers.values())
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

	private void updatePipeline(G1 image, RenderableImageActorPipeline pipeline)
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
				if (imagesByInstrument.get(imagingInstrument) == null) return;
				Color color = ColorUtil.generateColor(imagesByInstrument.get(imagingInstrument).indexOf(image)%100, 100);
				renderingStates.get(image).boundaryColor = color;
			}
			actor.GetProperty().SetColor(colorToDoubleArray(renderingStates.get(image).boundaryColor));
		});

		offLimbBoundaryRenderers.put(image, pipeline.getRenderableOffLimbBoundaryActors());
		offLimbBoundaryRenderers.get(image).forEach(actor -> {
			actor.SetVisibility(renderingStates.get(image).isOffLimbBoundaryShowing ? 1 : 0);
			if (renderingStates.get(image).offLimbBoundaryColor == null)
			{
				if (imagesByInstrument.get(imagingInstrument) == null) return;
				Color color = ColorUtil.generateColor(imagesByInstrument.get(imagingInstrument).indexOf(image)%100, 100);
				renderingStates.get(image).offLimbBoundaryColor = color;
			}
			actor.GetProperty().SetColor(colorToDoubleArray(renderingStates.get(image).offLimbBoundaryColor));
		});
	}

	public void updateImage(G1 image)
	{
		Thread thread = getPipelineThread(image, (Void v) -> {
			image.setStatus("Loaded");
			return null;
		});
		thread.start();

		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, image);
	}

	public void updateUserImage(G1 image)
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

	public void setImageMapped(G1 image, boolean mapped)
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
							if (image.getNumberOfLayers() == 1)
								pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);
							else if (image.getNumberOfLayers() == 3)
							{
								pipeline = new ColorImageGeneratorPipeline(image.getImages(), smallBodyModels);
							}
							else
							{

							}

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

	public boolean getImageMapped(G1 image)
	{
		return image.isMapped();
	}

	public String getImageStatus(G1 image)
	{
		return image.getStatus();
	}

//	public String getImageOrigin(PerspectiveImage image)
//	{
//		return image.getImageOrigin();
//	}

	public int getImageNumberOfLayers(G1 image)
	{
		return image.getNumberOfLayers();
	}

	public void setImageFrustumVisible(G1 image, boolean visible)
	{
		image.setFrustumShowing(visible);
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		List<vtkActor> actors = frustumRenderers.get(image);
		if (actors == null)
		{
			Thread thread = getPipelineThread(image, (Void v) -> {
				for (vtkActor actor : frustumRenderers.get(image))
				{
					actor.SetVisibility(visible? 1 : 0);
				}
				return null;
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

	public boolean getFrustumShowing(G1 image)
	{
		return image.isFrustumShowing();
	}

	public void setImageOfflimbShowing(G1 image, boolean showing)
	{
		image.setOfflimbShowing(showing);
		image.setOfflimbBoundaryShowing(showing);
		renderingStates.get(image).isOffLimbBoundaryShowing = showing;
		renderingStates.get(image).isOfflimbShowing = showing;
		List<vtkActor> actors = offLimbRenderers.get(image);
		if (actors == null)
		{
			Thread thread = getPipelineThread(image, (Void v) -> {
				for (vtkActor actor : offLimbRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
				}
				for (vtkActor actor : offLimbBoundaryRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
				}
				return null;
			});
			thread.start();
		}
		else
		{
			for (vtkActor actor : actors)
			{
				actor.SetVisibility(showing ? 1 : 0);
			}
			for (vtkActor actor : offLimbBoundaryRenderers.get(image))
			{
				actor.SetVisibility(showing? 1 : 0);
			}
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
		renderingStates.get(image).isOfflimbShowing = showing;
	}

	public boolean getImageOfflimbShowing(G1 image)
	{
		return image.isOfflimbShowing();
	}

	public void setOffLimbBoundaryShowing(G1 image, boolean showing)
	{
		image.setOfflimbBoundaryShowing(showing);
		renderingStates.get(image).isOffLimbBoundaryShowing = showing;
		List<vtkActor> actors = offLimbBoundaryRenderers.get(image);
		if (actors == null)
		{
			Thread thread = getPipelineThread(image, (Void v) -> {
				for (vtkActor actor : offLimbBoundaryRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
				}
				return null;
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
		renderingStates.get(image).isOffLimbBoundaryShowing = showing;
	}

	public boolean getOffLimbBoundaryShowing(G1 image)
	{
		return image.isOfflimbBoundaryShowing();
	}

	public void setImageBoundaryShowing(G1 image, boolean showing)
	{
		image.setBoundaryShowing(showing);
		List<vtkActor> actors = boundaryRenderers.get(image);
		if (actors == null)
		{
			Thread thread = getPipelineThread(image, (Void v) -> {
				for (vtkActor actor : boundaryRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
				}
				return null;
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

	public boolean getImageBoundaryShowing(G1 image)
	{
		return image.isBoundaryShowing();
	}

	public void setImageStatus(G1 image, String status)
	{
		image.setStatus(status);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public int size()
	{
		return getAllItems().size();
	}

	public Optional<IPerspectiveImage> getImage(vtkActor actor)
	{
		Optional<IPerspectiveImage> matchingImage = Optional.empty();
		for (IPerspectiveImage image : imageRenderers.keySet())
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

	public void setOfflimbOpacity(G1 image, double opacity)
	{
		List<vtkActor> actors = offLimbRenderers.get(image);
		actors.forEach(actor -> {
			vtkProperty interiorProperty = actor.GetProperty();
			interiorProperty.SetOpacity(opacity);
		});
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public double getOfflimbOpacity(G1 image)
	{
		List<vtkActor> actors = offLimbRenderers.get(image);
		vtkActor actor = actors.get(0);
		vtkProperty interiorProperty = actor.GetProperty();
		return interiorProperty.GetOpacity();
	}

	public void setOffLimbDepth(G1 image, double depth)
	{
		PerspectiveImageRenderingState renderingState = renderingStates.get(image);
		renderingState.offLimbFootprintDepth = depth;
		image.setOfflimbDepth(depth);
		System.out.println("PerspectiveImageCollection: setOffLimbDepth: depth set to " + depth);
		Thread thread = getPipelineThread(image, (Void v) -> { return null; });
		thread.start();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public double getOffLimbDepth(G1 image)
	{
		PerspectiveImageRenderingState renderingState = renderingStates.get(image);
		return renderingState.offLimbFootprintDepth;
	}

	public Color getOffLimbBoundaryColor(G1 image)
	{
		return renderingStates.get(image).offLimbBoundaryColor;
	}

	public void setOffLimbBoundaryColor(G1 image, Color color)
	{
		renderingStates.get(image).offLimbBoundaryColor = color;
		for (vtkActor actor : offLimbBoundaryRenderers.get(image))
		{
			actor.GetProperty().SetColor(color.getRed() / 255., color.getGreen() / 255., color.getBlue() / 255.);
	        actor.Modified();
		}
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void setImageContrastRange(G1 image, IntensityRange intensityRange)
	{
		renderingStates.get(image).imageContrastRange = intensityRange;
		Thread thread = getPipelineThread(image, (Void v) -> {

			for (vtkActor actor : offLimbBoundaryRenderers.get(image))
			{
				actor.SetVisibility(renderingStates.get(image).isOffLimbBoundaryShowing ? 1 : 0);

			}
			return null;
		});
		thread.start();
	}

	public IntensityRange getImageContrastRange(G1 image)
	{
		return renderingStates.get(image).imageContrastRange;
	}

	public void setOffLimbContrastRange(G1 image, IntensityRange intensityRange)
	{
		renderingStates.get(image).offLimbContrastRange = intensityRange;
		image.setOfflimbIntensityRange(intensityRange);
		if (renderingStates.get(image).contrastSynced)
		{
			renderingStates.get(image).imageContrastRange = intensityRange;
			image.setIntensityRange(intensityRange);
		}
		Thread thread = getPipelineThread(image, (Void v) -> { return null; });
		thread.start();
	}

	public IntensityRange getOffLimbContrastRange(G1 image)
	{
		return renderingStates.get(image).offLimbContrastRange;
	}

	public void setContrastSynced(G1 image, boolean contrastSynced)
	{
		renderingStates.get(image).contrastSynced = contrastSynced;
	}

	public boolean getContrastSynced(G1 image)
	{
		return renderingStates.get(image).contrastSynced;
	}

	public List<SmallBodyModel> getSmallBodyModels()
	{
		return smallBodyModels;
	}

	public IImagingInstrument getInstrument()
	{
		return this.imagingInstrument;
	}

	public void setImagingInstrument(IImagingInstrument imagingInstrument)
	{
		this.imagingInstrument = imagingInstrument;
		if (imagingInstrument == null)
			setAllItems(userImages);
		else if (imagesByInstrument.get(imagingInstrument) == null)
		{
			setAllItems(Lists.newArrayList());
		}
		else
		{
			ImmutableList<G1> filteredImages = ImmutableList.copyOf(imagesByInstrument.get(imagingInstrument).stream().filter(image -> image.getImageType() == imagingInstrument.getType()).toList());
			setAllItems(filteredImages);
		}
		loadUserList();
	}

	private Thread getPipelineThread(G1 image, Function<Void, Void> completionBlock)
	{
		return new PipelineThread(image, completionBlock);
	}

	class PipelineThread extends Thread
	{
		G1 image;
		Function<Void, Void> completionBlock;

		public PipelineThread(G1 image, Function<Void, Void> completionBlock)
		{
			this.image = image;
			this.completionBlock = completionBlock;
		}

		@Override
		public void run()
		{
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
			completionBlock.apply(null);
			SwingUtilities.invokeLater( () -> {
				pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			});
		}
	}
}
