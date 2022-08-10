package edu.jhuapl.sbmt.image2.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkProp;
import vtk.vtkProperty;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.pipeline.ColorImageGeneratorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.cylindricalImages.RenderCylindricalImageToScenePipeline;
import edu.jhuapl.sbmt.image2.pipeline.pointedImages.RenderablePointedImageActorPipeline;

import crucible.crust.logging.SimpleLogger;
import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.SettableMetadata;

public class BasemapImageCollection<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends SaavtkItemManager<G1> implements PropertyChangeListener
{
	private List<SmallBodyModel> smallBodyModels;
	private HashMap<G1, List<vtkActor>> imageRenderers;
	private HashMap<G1, PerspectiveImageRenderingState> renderingStates;
	private SimpleLogger logger = SimpleLogger.getInstance();

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

	public BasemapImageCollection(List<SmallBodyModel> smallBodyModels)
	{
		this.imageRenderers = new HashMap<G1, List<vtkActor>>();
		this.renderingStates = new HashMap<G1, PerspectiveImageRenderingState>();
		this.smallBodyModels = smallBodyModels;
	}

	public void removeUserImage(G1 image)
	{
		setImageMapped(image, false);
	}

	public void hideAllImages()
	{
		List<G1> allImages = getAllItems();
		allImages.forEach(img -> setImageMapped(img, false));
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

    public G1 addImage(ImageKeyInterface key)
    {
    	List<G1> allImages = Lists.newArrayList();
    	allImages.addAll(getAllItems());
    	G1 image = null;
    	if (key instanceof CustomCylindricalImageKey)
    	{
    		image = addCylindricalImage(key);
    	}
    	else	//perspective image
    	{
    		image = addPerspectiveImage(key);
    	}
    	allImages.add(image);
    	setAllItems(allImages);
    	PerspectiveImageRenderingState state = new PerspectiveImageRenderingState();
    	renderingStates.put(image, state);
    	return image;
    }

    private G1 addCylindricalImage(ImageKeyInterface key)
    {
    	CustomCylindricalImageKey cylKey = (CustomCylindricalImageKey)key;
    	String filename = FileCache.getFileFromServer(cylKey.getImageFilename()).getAbsolutePath();
    	ImageType imageType = cylKey.getImageType();
    	ImageSource pointingSource = ImageSource.LOCAL_CYLINDRICAL;
    	double[] fillValues = new double[] {};
		PerspectiveImage image = new PerspectiveImage(filename, imageType, pointingSource, null, fillValues);
//		image.setFlip(instrument.getFlip());
//		image.setRotation(instrument.getRotation());
		image.setName(filename);
		image.setImageOrigin(ImageOrigin.LOCAL);
		image.setLongTime(new Date().getTime());
		double minLat = cylKey.getLllat();
		double maxLat = cylKey.getUrlat();
		double minLon = cylKey.getLllon();
		double maxLon = cylKey.getUrlon();
		image.setBounds(new CylindricalBounds(minLat, maxLat, minLon, maxLon));

		CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
		compImage.setName(FilenameUtils.getBaseName(filename));
		return (G1)compImage;
    }

    private G1 addPerspectiveImage(ImageKeyInterface key)
    {
    	return null;
    }

    public G1 getImage(String name)
    {
    	G1 image = null;
    	List<G1> matchedImages = getAllItems().stream().filter(img -> img.getFilename().equals(name)).toList();
    	if (matchedImages.size() == 1) image = matchedImages.get(0);
    	return image;
    }

	public void setImages(List<G1> images)
	{
		setAllItems(images);
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

	public static double[] colorToDoubleArray(Color color) {
		return new double[] { (double) color.getRed() / 255., (double) color.getGreen() / 255.,
				(double) color.getBlue() / 255. };
	}

	private void updatePipeline(G1 image, RenderableImageActorPipeline pipeline)
	{
		imageRenderers.put(image, pipeline.getRenderableImageActors());
		imageRenderers.get(image).forEach(actor -> actor.SetVisibility(renderingStates.get(image).isMapped ? 1 : 0));
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

	public void setImageMapped(G1 image, boolean mapped)
	{
		image.setMapped(mapped);
		List<vtkActor> actors = imageRenderers.get(image);
		if (actors == null && mapped == true)
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
								if (image.getPointingSourceType() == ImageSource.LOCAL_CYLINDRICAL)
								{
									pipeline = new RenderCylindricalImageToScenePipeline(image.getFilename(), image.getBounds(), smallBodyModels);
								}
								else
								{
									pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);
								}
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
							if (image.getPointingSourceType() == ImageSource.LOCAL_CYLINDRICAL)
							{
								pipeline = new RenderCylindricalImageToScenePipeline(image.getFilename(), image.getBounds(), smallBodyModels);
							}
							else
							{
								pipeline = new RenderablePointedImageActorPipeline(image, smallBodyModels);
							}

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
						pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
					});
				}
			});
			thread.start();
		}
		else
		{
			if (actors != null)
			{
				for (vtkActor actor : actors)
				{
					actor.SetVisibility(mapped ? 1 : 0);
				}
			}
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

	public List<SmallBodyModel> getSmallBodyModels()
	{
		return smallBodyModels;
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
			System.out.println("PerspectiveImageCollection.PipelineThread: PipelineThread: making pipeline thread ");
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
					pipeline = new RenderCylindricalImageToScenePipeline(image.getFilename(), image.getBounds(), smallBodyModels);

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