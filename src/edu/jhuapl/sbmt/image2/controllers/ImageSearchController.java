package edu.jhuapl.sbmt.image2.controllers;

import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk2.gui.BasicFrame;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.ui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image2.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.CustomImageImporterDialog;
import edu.jhuapl.sbmt.image2.ui.table.popup.ImageListPopupMenu;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

import glum.gui.action.PopupMenu;

public class ImageSearchController implements Controller<ImageSearchParametersModel, ImagingSearchPanel>
{
	private ImageListTableController imageListTableController;
	private SpectralImageSearchParametersController searchParametersController;
	private ImagingSearchPanel panel;
	private ImageSearchParametersModel imageSearchModel;
	private IImagingInstrument instrument;
	private PerspectiveImageCollection collection;
	private ModelManager modelManager;
	private List<SmallBodyModel> smallBodyModels;

	public ImageSearchController(SmallBodyViewConfig config, PerspectiveImageCollection collection,
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

        //TODO update this
        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        smallBodyModels = List.of(smallBodyModel);
        PopupMenu popupMenu =
        		new ImageListPopupMenu(modelManager, collection, boundaries, infoPanelManager,
        							spectrumPanelManager, renderer, panel);
        this.imageListTableController = new ImageListTableController(collection, popupMenu);
        initGUI();
	}

	public void initGUI()
	{
		panel = new ImagingSearchPanel();
		panel.addSubPanel(searchParametersController.getPanel());
		panel.addSubPanel(imageListTableController.getPanel());

		imageListTableController.getPanel().getColorImageButton().addActionListener(e -> {

			ColorImageBuilderController controller = new ColorImageBuilderController(smallBodyModels);
			BasicFrame frame = new BasicFrame();
			frame.add(controller.getView());
			frame.setSize(775, 900);
			frame.setTitle("Create Color Image");
			frame.setVisible(true);
		});

		imageListTableController.getPanel().getImageCubeButton().addActionListener(e -> {

		});

		imageListTableController.getPanel().getShowImageButton().addActionListener(e -> {

		});

		imageListTableController.getPanel().getHideImageButton().addActionListener(e -> {

		});

		imageListTableController.getPanel().getLoadImageButton().addActionListener(e -> {

		});

		imageListTableController.getPanel().getSaveImageButton().addActionListener(e -> {

		});

		imageListTableController.getPanel().getNewImageButton().addActionListener(e -> {
			CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, false, instrument,
					modelManager.getPolyhedralModel().isEllipsoid(), collection);
//			dialog.setCurrentImageNames(model.getCustomImageNames());
//	        dialog.setImageInfo(null, model.getModelManager().getPolyhedralModel().isEllipsoid());
	        dialog.setLocationRelativeTo(imageListTableController.getPanel());
	        dialog.setVisible(true);
		});
	}

	@Override
	public ImageSearchParametersModel getModel()
	{
		return imageSearchModel;
	}

	@Override
	public ImagingSearchPanel getView()
	{
		return panel;
	}
}
