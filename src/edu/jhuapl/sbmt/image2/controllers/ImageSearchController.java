package edu.jhuapl.sbmt.image2.controllers;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.ui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image2.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.table.popup.ImageListPopupMenu;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

import glum.gui.action.PopupMenu;

public class ImageSearchController implements Controller<ImageSearchParametersModel, ImagingSearchPanel>
{
	private ImageListTableController imageListTableController;
	private ImageSearchParametersController searchParametersController;
	private ImagingSearchPanel panel;
	private ImageSearchParametersModel imageSearchModel;

	public ImageSearchController(SmallBodyViewConfig config, PerspectiveImageCollection collection,
								IImagingInstrument instrument, ModelManager modelManager, Renderer renderer,
								PickManager pickManager, SbmtInfoWindowManager infoPanelManager,
					            SbmtSpectrumWindowManager spectrumPanelManager)
	{
		this.imageSearchModel = new ImageSearchParametersModel(config, modelManager, renderer, instrument);
        this.searchParametersController = new ImageSearchParametersController(config, collection, imageSearchModel, modelManager, pickManager);
        this.searchParametersController.setupSearchParametersPanel();

        //TODO update this
        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);

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
