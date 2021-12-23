package edu.jhuapl.sbmt.image2.controllers;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk2.gui.BasicFrame;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.ui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.custom.importer.CustomImageImporterDialog;
import edu.jhuapl.sbmt.image2.ui.custom.importer.CustomImageImporterDialog2;
import edu.jhuapl.sbmt.image2.ui.table.popup.ImageListPopupMenu;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageType;

import glum.gui.action.PopupMenu;

public class ImageSearchController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> implements PickListener //implements Controller<ImageSearchParametersModel, JTabbedPane>
{
	private ImageListTableController imageListTableController;
	private CustomImageListTableController customImageListTableController;
	private SpectralImageSearchParametersController searchParametersController;
	private ImagingSearchPanel panel;
	private ImagingSearchPanel customPanel;
	private ImageSearchParametersModel imageSearchModel;
	private IImagingInstrument instrument;
	private PerspectiveImageCollection<G1> collection;
	private ModelManager modelManager;
	private List<SmallBodyModel> smallBodyModels;
	private JTabbedPane pane;
	private PopupMenu popupMenu;

	public ImageSearchController(SmallBodyViewConfig config, PerspectiveImageCollection<G1> collection,
								IImagingInstrument instrument, ModelManager modelManager, Renderer renderer,
								PickManager pickManager, SbmtInfoWindowManager infoPanelManager,
					            SbmtSpectrumWindowManager spectrumPanelManager)
	{
		this.instrument = instrument;
		this.collection = collection;
		this.collection.setImagingInstrument(instrument);
		this.modelManager = modelManager;
		this.imageSearchModel = new ImageSearchParametersModel(config, modelManager, renderer, instrument);
        this.searchParametersController = new SpectralImageSearchParametersController(config, collection, imageSearchModel, modelManager, pickManager);
        this.searchParametersController.setupSearchParametersPanel();
        pane = new JTabbedPane();
        //TODO update this
//        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        smallBodyModels = List.of(smallBodyModel);
        popupMenu =
        		new ImageListPopupMenu(modelManager, collection, infoPanelManager,
        							spectrumPanelManager, renderer, panel);
        this.imageListTableController = new ImageListTableController(collection, popupMenu);
        this.customImageListTableController = new CustomImageListTableController(collection, popupMenu);
//		popupMenu = new ImageListPopupMenu<>(modelManager, collection, null, infoPanelManager, spectrumPanelManager, renderer, renderer);
		pickManager.getDefaultPicker().addListener(this);

		collection.addPropertyChangeListener(evt ->
		{
			imageListTableController.getPanel().getResultList().repaint();
			customImageListTableController.getPanel().getResultList().repaint();
			updateButtonState();
		});

		collection.addListener((aSource, aEventType) ->
		{
//			imageListTableController.getPanel().getResultList().repaint();
//			customImageListTableController.getPanel().getResultList().repaint();
			updateButtonState();
		});

        initGUI();
	}

	private void initGUI()
	{
		initImageGUI();
		initCustomGUI();

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
				collection.setImagingInstrument(instrument);
			}
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
				System.out
						.println("ImageSearchController.initGUI().new AncestorListener() {...}: ancestorAdded: added custom panel");
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
				collection.setImagingInstrument(instrument);
				imageListTableController.getPanel().getResultList().repaint();
			}
		});

		panel = new ImagingSearchPanel();
		panel.addSubPanel(searchParametersController.getPanel());
		panel.addSubPanel(imageListTableController.getPanel());

		imageListTableController.getPanel().getColorImageButton().addActionListener(e -> {

			ColorImageBuilderController controller = new ColorImageBuilderController(smallBodyModels, collection, Optional.empty());
			BasicFrame frame = new BasicFrame();
			frame.add(controller.getView());
			frame.setSize(775, 900);
			frame.setTitle("Create Color Image");
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		});

		imageListTableController.getPanel().getImageCubeButton().addActionListener(e -> {

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
			System.out.println("ImageSearchController: initImageGUI: number of selected images " + selectedImages.size());
			if (selectedImages.size() == 0) return;
			for (G1 image : selectedImages) {
				collection.setImageMapped(image, false);
			}
		});

		imageListTableController.getPanel().getLoadImageButton().addActionListener(e -> {

		});

		imageListTableController.getPanel().getSaveImageButton().addActionListener(e -> {

		});

//		imageListTableController.getPanel().getNewImageButton().addActionListener(e -> {
//			CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, false, instrument,
//					modelManager.getPolyhedralModel().isEllipsoid(), collection);
////			dialog.setCurrentImageNames(model.getCustomImageNames());
////	        dialog.setImageInfo(null, model.getModelManager().getPolyhedralModel().isEllipsoid());
//	        dialog.setLocationRelativeTo(imageListTableController.getPanel());
//	        dialog.setVisible(true);
//		});
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

		});

		customImageListTableController.getPanel().getSaveImageButton().addActionListener(e -> {

		});

		customImageListTableController.getPanel().getNewImageButton().addActionListener(e -> {
			CustomImageImporterDialog2 dialog = new CustomImageImporterDialog2(null, false, instrument,
					modelManager.getPolyhedralModel().isEllipsoid(), collection);
//			dialog.setCurrentImageNames(model.getCustomImageNames());
//	        dialog.setImageInfo(null, model.getModelManager().getPolyhedralModel().isEllipsoid());
	        dialog.setLocationRelativeTo(imageListTableController.getPanel());
	        dialog.setVisible(true);
		});

		customImageListTableController.getPanel().getEditImageButton().addActionListener(e -> {
			ImmutableSet<G1> selectedItems = collection.getSelectedItems();
			if (selectedItems.size() != 1) return;
			IPerspectiveImage image = selectedItems.asList().get(0);
			if (image.getImageType() == ImageType.GENERIC_IMAGE) return;
			if (image.getNumberOfLayers() == 1)	//editing custom single layer image
			{
				CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, true, instrument,
						modelManager.getPolyhedralModel().isEllipsoid(), collection, Optional.of(image));
		        dialog.setLocationRelativeTo(imageListTableController.getPanel());
		        dialog.setVisible(true);
			}
			else if (image.getNumberOfLayers() == 3) //editing custom color image
			{
				ColorImageBuilderController controller = new ColorImageBuilderController(smallBodyModels, collection, Optional.of(image));
				controller.setImages(image.getImages());
				BasicFrame frame = new BasicFrame();
				frame.add(controller.getView());
				frame.setSize(775, 900);
				frame.setTitle("Edit Color Image");
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
			else //editing custom n > 1, n!=3 spectral image
			{

			}
		});

		customImageListTableController.getPanel().getDeleteImageButton().addActionListener(e -> {
			for (G1 image : collection.getSelectedItems())
			{
				collection.setImageMapped(image, false);
				collection.setImageBoundaryShowing(image, false);
				collection.setImageFrustumVisible(image, false);
				collection.setImageOfflimbShowing(image, false);
			}
			collection.removeItems(collection.getSelectedItems());
		});


	}

	private void updateButtonState()
	{
		ImmutableSet<G1> selectedItems = collection.getSelectedItems();
		boolean allMapped = true;
		for (G1 image : selectedItems)
		{
			if (image.isMapped() == false) allMapped = false;
		}
		if (collection.getInstrument() != null)
		{
			imageListTableController.getPanel().getSaveImageButton().setEnabled(selectedItems.size() == 1);
			imageListTableController.getPanel().getHideImageButton().setEnabled((selectedItems.size() > 0) && allMapped);
			imageListTableController.getPanel().getShowImageButton().setEnabled((selectedItems.size() > 0) && !allMapped);
		}
		else
		{
			customImageListTableController.getPanel().getEditImageButton().setEnabled(selectedItems.size() == 1);
			customImageListTableController.getPanel().getHideImageButton().setEnabled((selectedItems.size() > 0) && allMapped);
			customImageListTableController.getPanel().getShowImageButton().setEnabled((selectedItems.size() > 0) && !allMapped);
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

		// Show the popup
		Component tmpComp = aEvent.getComponent();
		int posX = ((MouseEvent) aEvent).getX();
		int posY = ((MouseEvent) aEvent).getY();
		popupMenu.show(tmpComp, posX, posY);
	}

//	@Override
	public ImageSearchParametersModel getModel()
	{
		return imageSearchModel;
	}

//	@Override
	public JTabbedPane getView()
	{

		pane.add(panel, "Server");
		pane.add(customPanel, "Custom");
		return pane;
	}
}
