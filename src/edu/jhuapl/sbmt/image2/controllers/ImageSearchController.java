package edu.jhuapl.sbmt.image2.controllers;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableSet;

import vtk.vtkActor;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk2.gui.BasicFrame;
import edu.jhuapl.sbmt.common.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.IImagingInstrument;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.core.imageui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image2.controllers.custom.CustomImageEditingController;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.CustomImageListToSavedFilePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.ImageGalleryPipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.LoadFileToCustomImageListPipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.LoadImagesFromSavedFilePipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.SaveImagesToSavedFilePipeline;
import edu.jhuapl.sbmt.image2.ui.custom.importer.CustomImageImporterDialog2;
import edu.jhuapl.sbmt.image2.ui.table.popup.ImageListPopupMenu;
import edu.jhuapl.sbmt.util.ImageGalleryGenerator;

import glum.gui.action.PopupMenu;

public class ImageSearchController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> implements PickListener //implements Controller<ImageSearchParametersModel, JTabbedPane>
{
	private ImageListTableController<G1> imageListTableController;
	private CustomImageListTableController<G1> customImageListTableController;
	private SpectralImageSearchParametersController<G1> searchParametersController;
	private ImagingSearchPanel panel;
	private ImagingSearchPanel customPanel;
	private ImageSearchParametersModel imageSearchModel;
	private Optional<IImagingInstrument> instrument;
	private PerspectiveImageCollection<G1> collection;
	private ModelManager modelManager;
	private List<SmallBodyModel> smallBodyModels;
	private JTabbedPane pane;
	private PopupMenu<G1> popupMenu;
	private SmallBodyViewConfig config;

	public ImageSearchController(SmallBodyViewConfig config, PerspectiveImageCollection<G1> collection,
								Optional<IImagingInstrument> instrument, ModelManager modelManager,
								PopupManager popupManager, Renderer renderer,
								PickManager pickManager, SbmtInfoWindowManager infoPanelManager,
					            SbmtSpectrumWindowManager spectrumPanelManager)
	{
		this.config = config;
		this.instrument = instrument;
		this.collection = collection;
		this.collection.setImagingInstrument(instrument.orElse(null));
		this.modelManager = modelManager;
		this.instrument = instrument;
		this.imageSearchModel = new ImageSearchParametersModel(config, modelManager, renderer, instrument.orElse(null));
		instrument.ifPresent(inst -> {
			this.searchParametersController = new SpectralImageSearchParametersController(config, collection, imageSearchModel, modelManager, pickManager);
			this.searchParametersController.setupSearchParametersPanel();
		});
        pane = new JTabbedPane();
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        smallBodyModels = List.of(smallBodyModel);
        popupMenu = new ImageListPopupMenu<G1>(modelManager, collection, infoPanelManager,
        										spectrumPanelManager, renderer, panel);
        this.imageListTableController = new ImageListTableController<G1>(collection, popupMenu);
        this.customImageListTableController = new CustomImageListTableController<G1>(collection, popupMenu);
		pickManager.getDefaultPicker().addListener(this);

		this.collection.addPropertyChangeListener(evt ->
		{
			imageListTableController.getPanel().getResultList().repaint();
			customImageListTableController.getPanel().getResultList().repaint();
			updateButtonState();
		});

		this.collection.addListener((aSource, aEventType) -> { updateButtonState(); });
//		popupManager.registerPopup(modelManager.getAllModels().get(ModelNames.IMAGES_V2), new edu.jhuapl.saavtk.popup.PopupMenu()
//		{
//			@Override
//			public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
//			{
//				System.out.println(
//						"ImageSearchController.ImageSearchController(...).new PopupMenu() {...}: showPopup: showing popup");
//                popupMenu.show(e.getComponent(), e.getX(), e.getY());
//			}
//		});
        initGUI();
	}

	private void initGUI()
	{
		instrument.ifPresent(inst -> {initImageGUI();});
//		initImageGUI();
		initCustomGUI();

		instrument.ifPresent(inst -> {
			panel.addAncestorListener(new AncestorListener()
			{

				@Override
				public void ancestorRemoved(AncestorEvent event)
				{
					// TODO Auto-generated method stub

				}

				@Override
				public void ancestorMoved(AncestorEvent event)
				{
					// TODO Auto-generated method stub

				}

				@Override
				public void ancestorAdded(AncestorEvent event)
				{
					collection.setImagingInstrument(inst);
				}
			});
		});

		customPanel.addAncestorListener(new AncestorListener()
		{

			@Override
			public void ancestorRemoved(AncestorEvent event)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void ancestorMoved(AncestorEvent event)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void ancestorAdded(AncestorEvent event)
			{
				collection.setImagingInstrument(null);
			}
		});
	}

	public void initImageGUI()
	{
		pane.addAncestorListener(new AncestorListener()
		{

			@Override
			public void ancestorRemoved(AncestorEvent event)
			{

			}

			@Override
			public void ancestorMoved(AncestorEvent event)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void ancestorAdded(AncestorEvent event)
			{
				collection.setImagingInstrument(instrument.orElse(null));
				imageListTableController.getPanel().getResultList().repaint();
			}
		});

		panel = new ImagingSearchPanel();
		panel.addSubPanel(searchParametersController.getPanel());
		panel.addSubPanel(imageListTableController.getPanel());

		imageListTableController.getPanel().getColorImageButton().addActionListener(e -> {

			ColorImageBuilderController<G1> controller = new ColorImageBuilderController<G1>(smallBodyModels, collection, Optional.empty());
			BasicFrame frame = new BasicFrame();
			frame.add(controller.getView());
			frame.setSize(775, 900);
			frame.setTitle("Create Color Image");
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		});

		imageListTableController.getPanel().getImageCubeButton().addActionListener(e -> {
			System.err.print("NOT IMPLEMENTED YET");
		});

		imageListTableController.getPanel().getShowImageButton().addActionListener(e -> {
			ImmutableSet<G1> selectedImages = collection.getSelectedItems();
			if (selectedImages.size() == 0) return;
			for (G1 image : selectedImages) {
				collection.setImageMapped(image, true);
			}
		});

		imageListTableController.getPanel().getHideImageButton().addActionListener(e -> {
			ImmutableSet<G1> selectedImages = collection.getSelectedItems();
			if (selectedImages.size() == 0) return;
			for (G1 image : selectedImages) {
				collection.setImageMapped(image, false);
			}
		});

		imageListTableController.getPanel().getShowImageBorderButton().addActionListener(e -> {
			ImmutableSet<G1> selectedImages = collection.getSelectedItems();
			if (selectedImages.size() == 0) return;
			for (G1 image : selectedImages) {
				collection.setImageBoundaryShowing(image, true);
			}
		});

		imageListTableController.getPanel().getHideImageBorderButton().addActionListener(e -> {
			ImmutableSet<G1> selectedImages = collection.getSelectedItems();
			if (selectedImages.size() == 0) return;
			for (G1 image : selectedImages) {
				collection.setImageBoundaryShowing(image, false);
			}
		});

		imageListTableController.getPanel().getLoadImageButton().addActionListener(e -> {

	        File file = CustomFileChooser.showOpenDialog(panel, "Select File");
	        if (file == null) return;
			try
			{
				LoadImagesFromSavedFilePipeline<G1> pipeline = new LoadImagesFromSavedFilePipeline<G1>(config, file.getAbsolutePath(), (ImagingInstrument)instrument.orElse(null));
				collection.clearSearchedImages();
				collection.setImages(pipeline.getImages());
			}
			catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		imageListTableController.getPanel().getSaveImageButton().addActionListener(e -> {

			ImmutableSet<G1> selectedImages = collection.getSelectedItems();
			try
			{
				new SaveImagesToSavedFilePipeline<>(selectedImages);
			}
			catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		imageListTableController.getPanel().getBoundaryOffsetTextField().addActionListener(e -> {
			collection.setCurrentBoundaryOffsetAmount(Integer.parseInt(imageListTableController.getPanel().getBoundaryOffsetTextField().getText()));
		});

		imageListTableController.getPanel().getBoundaryOffsetTextField().addFocusListener(new FocusListener()
		{

			@Override
			public void focusLost(FocusEvent arg0)
			{
				collection.setCurrentBoundaryOffsetAmount(Integer.parseInt(imageListTableController.getPanel().getBoundaryOffsetTextField().getText()));
			}

			@Override
			public void focusGained(FocusEvent arg0)
			{
				// TODO Auto-generated method stub

			}
		});

		imageListTableController.getPanel().getPreviousBoundariesButton().addActionListener(e -> {

			collection.offsetBoundariesRange(-Integer.parseInt(imageListTableController.getPanel().getBoundaryOffsetTextField().getText()));
		});

		imageListTableController.getPanel().getNextBoundariesButton().addActionListener(e -> {
			collection.offsetBoundariesRange(Integer.parseInt(imageListTableController.getPanel().getBoundaryOffsetTextField().getText()));
		});

		imageListTableController.getPanel().getGalleryButton().addActionListener(e -> {
			try
			{
				if (collection.getSelectedItems().size() == 0)
					ImageGalleryPipeline.of(instrument.orElse(null), collection.getAllItems());
				else
				{
					List<G1> selectedList = Lists.newArrayList();
					selectedList.addAll(collection.getSelectedItems());
					ImageGalleryPipeline.of(instrument.orElse(null), selectedList);
				}
			}
			catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

//		imageListTableController.getPanel().getNewImageButton().addActionListener(e -> {
//			CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, false, instrument,
//					modelManager.getPolyhedralModel().isEllipsoid(), collection);
////			dialog.setCurrentImageNames(model.getCustomImageNames());
////	        dialog.setImageInfo(null, model.getModelManager().getPolyhedralModel().isEllipsoid());
//	        dialog.setLocationRelativeTo(imageListTableController.getPanel());
//	        dialog.setVisible(true);
//		});

		imageListTableController.getPanel().getGalleryButton().setEnabled(false);

	}

	private void initCustomGUI()
	{
		customPanel = new ImagingSearchPanel();

		customPanel.addSubPanel(customImageListTableController.getPanel());

//		customImageListTableController.getPanel().getColorImageButton().addActionListener(e -> {
//
//			ColorImageBuilderController controller = new ColorImageBuilderController(smallBodyModels);
//			BasicFrame frame = new BasicFrame();
//			frame.add(controller.getView());
//			frame.setSize(775, 900);
//			frame.setTitle("Create Color Image");
//			frame.setVisible(true);
//		});
//
//		customImageListTableController.getPanel().getImageCubeButton().addActionListener(e -> {
//
//		});

		customImageListTableController.getPanel().getShowImageButton().addActionListener(e -> {
			ImmutableSet<G1> selectedImages = collection.getSelectedItems();
			if (selectedImages.size() == 0) return;
			for (G1 image : selectedImages) {
				collection.setImageMapped(image, true);
			}
		});

		customImageListTableController.getPanel().getHideImageButton().addActionListener(e -> {
			ImmutableSet<G1> selectedImages = collection.getSelectedItems();
			if (selectedImages.size() == 0) return;
			for (G1 image : selectedImages) {
				collection.setImageMapped(image, false);
			}
		});

		customImageListTableController.getPanel().getLoadImageButton().addActionListener(e -> {
			try
			{
				LoadFileToCustomImageListPipeline<G1> pipeline = LoadFileToCustomImageListPipeline.of();
				List<G1> images = pipeline.getResults().getLeft();
//				List<PerspectiveImageRenderingState<G1>> renderingStates = pipeline.getResults().getRight();
				collection.setImages(images);
//				collection.setRenderingStates(renderingStates);
			}
			catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		customImageListTableController.getPanel().getSaveImageButton().addActionListener(e -> {
			List<G1> selectedImages = Lists.newArrayList();
			selectedImages.addAll(collection.getSelectedItems());
			try
			{
				CustomImageListToSavedFilePipeline.of(selectedImages);
			}
			catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		customImageListTableController.getPanel().getNewImageButton().addActionListener(e -> {
			CustomImageImporterDialog2<G1> dialog = new CustomImageImporterDialog2<G1>(null, false, instrument.orElse(null),
					modelManager.getPolyhedralModel().isEllipsoid(), collection);
	        dialog.setLocationRelativeTo(customImageListTableController.getPanel());
	        dialog.setVisible(true);
		});

		customImageListTableController.getPanel().getEditImageButton().addActionListener(e -> {
			ImmutableSet<G1> selectedItems = collection.getSelectedItems();
			if (selectedItems.size() != 1) return;
			G1 image = selectedItems.asList().get(0);
			if (image.getNumberOfLayers() == 1)	//editing custom single layer image
			{
				CustomImageEditingController<G1> dialog = new CustomImageEditingController<G1>(null,
						modelManager.getPolyhedralModel().isEllipsoid(), image, () -> {});
		        dialog.getDialog().setLocationRelativeTo(customImageListTableController.getPanel());
		        dialog.getDialog().setVisible(true);
		        collection.updateUserImage(image);
			}
			else if (image.getNumberOfLayers() == 3) //editing custom color image
			{
				ColorImageBuilderController<G1> controller = new ColorImageBuilderController<G1>(smallBodyModels, collection, Optional.of(image));
				controller.setImages(image.getImages());
				BasicFrame frame = new BasicFrame();
				frame.add(controller.getView());
				frame.setSize(775, 900);
				frame.setTitle("Edit Color Image");
				frame.setVisible(true);
				frame.setAlwaysOnTop(true);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
			else //editing custom n > 1, n!=3 spectral image
			{
				System.err.println("Feature not enabled for multispectral (n>3) images");
			}
		});

		customImageListTableController.getPanel().getDeleteImageButton().addActionListener(e -> {
			for (G1 image : collection.getSelectedItems())
			{
				collection.setImageBoundaryShowing(image, false);
				collection.setImageFrustumVisible(image, false);
				collection.setImageOfflimbShowing(image, false);
				collection.setImageMapped(image, false);
				collection.removeUserImage(image);
			}
			collection.removeItems(collection.getSelectedItems());
		});
	}

	private void updateButtonState()
	{
		ImmutableSet<G1> selectedItems = collection.getSelectedItems();
		boolean allMapped = false;
		boolean allBorders = false;
		boolean someMapped = false;
		boolean someBorders = false;
		int numMapped = 0;
		int numBoundary = 0;
		for (G1 image : selectedItems)
		{
			if (image.isMapped()) numMapped++;
			if (image.isBoundaryShowing()) numBoundary++;
		}
		if (selectedItems.size() == numMapped) allMapped = true;
		if (selectedItems.size() == numBoundary) allBorders = true;
		if (numMapped > 0 && numMapped < selectedItems.size()) someMapped = true;
		if (numBoundary > 0 && numBoundary < selectedItems.size()) someBorders = true;

		if (collection.getInstrument() != null)
		{
			imageListTableController.getPanel().getSaveImageButton().setEnabled(selectedItems.size() > 0);
			imageListTableController.getPanel().getHideImageButton().setEnabled((selectedItems.size() > 0) && (allMapped || someMapped));
			imageListTableController.getPanel().getShowImageButton().setEnabled((selectedItems.size() > 0) && !allMapped || someMapped);
			imageListTableController.getPanel().getHideImageBorderButton().setEnabled((selectedItems.size() > 0) && allBorders || someBorders);
			imageListTableController.getPanel().getShowImageBorderButton().setEnabled((selectedItems.size() > 0) && !allBorders || someBorders);
			ImageGalleryGenerator.of(instrument.orElse(null), state -> {
				imageListTableController.getPanel().getGalleryButton().setEnabled(true);
			});
//			imageListTableController.getPanel().getGalleryButton().setEnabled(collection.getAllItems().size() > 0);
		}
		else
		{
			customImageListTableController.getPanel().getEditImageButton().setEnabled(selectedItems.size() == 1);
			customImageListTableController.getPanel().getHideImageButton().setEnabled((selectedItems.size() > 0) && allMapped || someMapped);
			customImageListTableController.getPanel().getShowImageButton().setEnabled((selectedItems.size() > 0) && !allMapped || someMapped);
			customImageListTableController.getPanel().getHideImageBorderButton().setEnabled((selectedItems.size() > 0) && allBorders || someBorders);
			customImageListTableController.getPanel().getShowImageBorderButton().setEnabled((selectedItems.size() > 0) && !allBorders || someBorders);
			customImageListTableController.getPanel().getDeleteImageButton().setEnabled(selectedItems.size() > 0);
		}
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
//		// Bail if we are are not associated with the PickTarget
//		if (collection.getPainterFor(aPrimaryTarg) == null)
//			return;
		// Bail if not a valid pick action
		if (PickUtil.isPopupTrigger(aEvent) == false || aMode != PickMode.ActiveSec)
			return;


		vtkActor actor = aPrimaryTarg.getActor();
		Optional<G1> image = collection.getImage(actor);
		image.ifPresent(e -> {
			if (collection.getImageMapped(e))
			{
				collection.setSelectedItems(List.of(e));
				// Show the popup
				Component tmpComp = aEvent.getComponent();
				int posX = ((MouseEvent) aEvent).getX();
				int posY = ((MouseEvent) aEvent).getY();
				popupMenu.show(tmpComp, posX, posY);
			}
		});


	}

	public ImageSearchParametersModel getModel()
	{
		return imageSearchModel;
	}

	public JTabbedPane getView()
	{
		instrument.ifPresent(inst -> {
			pane.add(panel, "Server");
		});

		pane.add(customPanel, "Custom");
		return pane;
	}
}
