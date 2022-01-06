package edu.jhuapl.sbmt.image2.ui.offlimb;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.modules.preview.ImageContrastController;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.modules.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class OfflimbControlsController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	OfflimbImageControlPanel controlsPanel;
//	OfflimbControlsModel controlsModel;
	PerspectiveImageCollection<G1> collection;
	ImageContrastController contrastController;
	G1 image;
	List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
	List<RenderablePointedImage> renderableImages;

	public OfflimbControlsController(PerspectiveImageCollection<G1> collection, G1 image) throws Exception
	{
		this.image = image;

		PerspectiveImageToRenderableImagePipeline pipeline1 = new PerspectiveImageToRenderableImagePipeline(List.of(image));
		renderableImages = pipeline1.getRenderableImages();
		Just.of(renderableImages.get(0).getLayer())
			.operate(new VtkImageRendererOperator())
			.subscribe(Sink.of(displayedImages))
			.run();
		System.out.println("OfflimbControlsController: OfflimbControlsController: offlimb depth " + image.getOfflimbDepth());
		System.out.println("OfflimbControlsController: OfflimbControlsController: renderable image depth " + renderableImages.get(0).getOfflimbDepth());
		this.collection = collection;
		//TODO need displayed image here

		this.contrastController = new ImageContrastController(displayedImages.get(0), new IntensityRange(0, 255), new Function<vtkImageData, Void>() {

			@Override
			public Void apply(vtkImageData t)
			{
				try
				{
					collection.setOffLimbContrastRange(image, contrastController.getIntensityRange());
//					displayedImages.set(0, t);
//					updateImage(t);
//					setIntensity(null);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});

//		controlsModel = new OfflimbControlsModel(image, currentSlice);

		controlsPanel = new OfflimbImageControlPanel();

//		controlsModel.addModelChangedListener(new OfflimbModelChangedListener()
//		{
//
//			@Override
//			public void currentSliceChanged(int slice)
//			{
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void currentDepthChanged(int depth)
//			{
//				controlsPanel.getFootprintDepthValue().setText("" + controlsPanel.getFootprintDepthSlider().getValue());
//			}
//
//			@Override
//			public void currentAlphaChanged(int alpha)
//			{
//				controlsPanel.getFootprintTransparencyValue().setText("" + controlsPanel.getFootprintTransparencySlider().getValue());
//			}
//
//			@Override
//			public void showBoundaryChanged() {
//				ShowBoundaryButton showBoundaryButton = controlsPanel.getShowBoundaryButton();
//				showBoundaryButton.showBoundary(showBoundaryButton.isSelected());
//				collection.setOffLimbBoundaryShowing(image, showBoundaryButton.isSelected());
////				controlsModel.setShowBoundary(showBoundaryButton.isSelected());
//			}
//
//			@Override
//			public void syncContrastChanged() {
//				boolean isSelected = controlsPanel.getSyncContrastButton().isSelected();
//				controlsPanel.getSyncContrastButton().syncContrast(isSelected);
//				collection.setContrastSynced(image, isSelected);
////				controlsModel.setSyncContrast(isSelected);
//			}
//
//
//		});

		init();
	}

//	private void updateImage(vtkImageData displayedImage)
//	{
//		double[] center = displayedImage.GetCenter();
//		int[] dims = displayedImage.GetDimensions();
//		// Rotate image by 90 degrees so it appears the same way as when you
//		// use the Center in Image option.
//		vtkTransform imageTransform = new vtkTransform();
//		imageTransform.Translate(center[0], center[1], 0.0);
//		imageTransform.RotateZ(-90.0);
//		imageTransform.Translate(-center[1], -center[0], 0.0);
//
//		vtkImageReslice reslice = new vtkImageReslice();
//		reslice.SetInputData(displayedImage);
//		reslice.SetResliceTransform(imageTransform);
//		reslice.SetInterpolationModeToNearestNeighbor();
//		reslice.SetOutputSpacing(1.0, 1.0, 1.0);
//		reslice.SetOutputOrigin(0.0, 0.0, 0.0);
//		reslice.SetOutputExtent(0, dims[1] - 1, 0, dims[0] - 1, 0, 0);
//		reslice.Update();
//
//		vtkImageSliceMapper imageSliceMapper = new vtkImageSliceMapper();
//		imageSliceMapper.SetInputConnection(reslice.GetOutputPort());
//		imageSliceMapper.Update();
//
////		actor.SetMapper(imageSliceMapper);
////		actor.GetProperty().SetInterpolationTypeToLinear();
//	}

//	private void setIntensity(IntensityRange range) throws IOException, Exception
//	{
//		VtkImageContrastPipeline pipeline = new VtkImageContrastPipeline(displayedImages.get(0), null);
//		displayedImages.set(0, pipeline.getUpdatedData().get(0));
//		updateImage(displayedImages.get(0));
//	}

	private void init()
	{
		ChangeListener changeListener = new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (e.getSource() == controlsPanel.getFootprintDepthSlider() && !controlsPanel.getFootprintDepthSlider().getValueIsAdjusting())
				{
					DepthSlider<G1> depthSlider = controlsPanel.getFootprintDepthSlider();
					double depthValue = depthSlider.getDepthValue(renderableImages.get(0).getMinFrustumLength(), renderableImages.get(0).getMaxFrustumLength());
					collection.setOffLimbDepth(image, depthValue);
					controlsPanel.getFootprintDepthValue().setText(" " + depthValue);
				}
				else if (e.getSource() == controlsPanel.getFootprintTransparencySlider() && !controlsPanel.getFootprintTransparencySlider().getValueIsAdjusting())
				{
					AlphaSlider<G1> alphaSlider = controlsPanel.getFootprintTransparencySlider();
					double alphaValue = alphaSlider.getAlphaValue();
					collection.setOfflimbOpacity(image, alphaValue);
					controlsPanel.getFootprintTransparencyValue().setText(" " + alphaValue*100 + "%");
				}
//				else if (e.getSource() == controlsPanel.getImageContrastSlider())
//				{
//					ContrastSlider contrastSlider = controlsPanel.getImageContrastSlider();
//					if(!controlsPanel.getImageContrastSlider().getValueIsAdjusting()) {
//						contrastSlider.sliderStateChanged(e);
//						collection.setOffLimbContrastRange(image, new IntensityRange(contrastSlider.getLowValue(), contrastSlider.getHighValue()));
////						controlsModel.setContrastLow(contrastSlider.getLowValue());
////						controlsModel.setContrastHigh(contrastSlider.getHighValue());
//					}
//					if (collection.getContrastSynced(image)) {
//						// adjust image contrast slider also
//						collection.setImageContrastRange(image, new IntensityRange(contrastSlider.getLowValue(), contrastSlider.getHighValue()));
//
////						imageContrastSlider.setHighValue(contrastSlider.getHighValue());
////						imageContrastSlider.setLowValue(contrastSlider.getLowValue());
//					}
//				}
				else if (e.getSource() == controlsPanel.getShowBoundaryButton())
				{
					ShowBoundaryButton<G1> showBoundaryButton = controlsPanel.getShowBoundaryButton();
					collection.setOffLimbBoundaryShowing(image, showBoundaryButton.isSelected());
				}
				else if (e.getSource() == controlsPanel.getSyncContrastButton())
				{
					// let everyone know that we're syncing or unsyncing
					SyncContrastSlidersButton<G1> syncButton = controlsPanel.getSyncContrastButton();
//					ContrastSlider contrastSlider = controlsPanel.getImageContrastSlider();
//					syncButton.syncContrast(syncButton.isSelected());
//					controlsModel.setSyncContrast(syncButton.isSelected());
					collection.setContrastSynced(image, syncButton.isSelected());
					if (controlsPanel.getSyncContrastButton().isSelected()) {
						// if we're syncing, set the slider values to that of the img slider
						collection.setOffLimbContrastRange(image, new IntensityRange(contrastController.getLowValue(), contrastController.getHighValue()));
						collection.setImageContrastRange(image, new IntensityRange(contrastController.getLowValue(), contrastController.getHighValue()));

//						contrastSlider.setLowValue(imageContrastSlider.getLowValue());
//						contrastSlider.setHighValue(imageContrastSlider.getHighValue());
//						controlsModel.setContrastLow(contrastSlider.getLowValue());
//						controlsModel.setContrastHigh(contrastSlider.getHighValue());
					}
				}
				else if (e.getSource() == controlsPanel.getResetButton())
				{
					// let everyone know that we're syncing or unsyncing
					controlsPanel.getSyncContrastButton().setSelected(false);
					controlsPanel.getShowBoundaryButton().setSelected(true);
					controlsPanel.getFootprintDepthSlider().setValue(0);
					controlsPanel.getFootprintTransparencySlider().setValue(50);
				}
			}
		};

		controlsPanel.getFootprintDepthSlider().addChangeListener(changeListener);
		controlsPanel.getFootprintTransparencySlider().addChangeListener(changeListener);
//		controlsPanel.getImageContrastSlider().addChangeListener(changeListener);
		controlsPanel.getShowBoundaryButton().addChangeListener(changeListener);
		controlsPanel.getSyncContrastButton().addChangeListener(changeListener);
		controlsPanel.getResetButton().addChangeListener(changeListener);

		controlsPanel.getBoundaryColorBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color currColor =  collection.getOffLimbBoundaryColor(image);
				//image.getOfflimbBoundaryColor();
				Color color = ColorChooser.showColorChooser(
	                    null, new int[] {0,0});
//	                    new int[]{currColor.getRed(), currColor.getGreen(), currColor.getBlue()});
//				 image.setOfflimbBoundaryColor(color);
				 collection.setOffLimbBoundaryColor(image, color);
			}

		});


	}

	public JPanel getControlsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(contrastController.getView());
		panel.add(controlsPanel);
		return panel;
//		return controlsPanel;
	}

//	public OfflimbControlsModel getControlsModel()
//	{
//		return controlsModel;
//	}



}
