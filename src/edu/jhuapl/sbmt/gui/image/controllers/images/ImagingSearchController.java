package edu.jhuapl.sbmt.gui.image.controllers.images;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.gui.image.controllers.cubes.ImageCubeController;
import edu.jhuapl.sbmt.gui.image.controllers.search.ImageSearchParametersController;
import edu.jhuapl.sbmt.gui.image.model.color.ColorImageModel;
import edu.jhuapl.sbmt.gui.image.model.cubes.ImageCubeModel;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.gui.image.ui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;


public class ImagingSearchController implements Controller<ImageSearchModel, ImagingSearchPanel>
{
    private ImageResultsTableController imageResultsTableController;
    private ImageSearchParametersController searchParametersController;
    private ImageCubeController imageCubeController;
    private ColorImageController colorImageController;
    private ImagingSearchPanel panel;
    protected final ModelManager modelManager;
    protected final Renderer renderer;
	private final ImageSearchModel imageSearchModel;


    public ImagingSearchController(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager, Renderer renderer,
            ImagingInstrument instrument)
    {
        this.modelManager = modelManager;
        this.renderer = renderer;

        this.imageSearchModel = new ImageSearchModel(smallBodyConfig, modelManager, renderer, instrument);
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName());
        PerspectiveImageBoundaryCollection imageBoundaryCollection = (PerspectiveImageBoundaryCollection)modelManager.getModel(imageSearchModel.getImageBoundaryCollectionModelName());

        this.imageResultsTableController = new OfflimbImageResultsTableController(instrument, imageCollection, imageSearchModel, renderer, infoPanelManager, spectrumPanelManager);
        this.imageResultsTableController.setImageResultsPanel();

        this.searchParametersController = new ImageSearchParametersController(imageSearchModel, pickManager);
        if (instrument.searchQuery instanceof FixedListQuery)
        	this.searchParametersController.setFixedListSearch(true);
        this.searchParametersController.setupSearchParametersPanel();

        ImageCubeModel cubeModel = new ImageCubeModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)imageSearchModel.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        cubeModel.setColorImageCollection(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, imageBoundaryCollection, infoPanelManager, spectrumPanelManager, renderer, getPanel());
        this.imageCubeController = new ImageCubeController(imageSearchModel, cubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);

        ColorImageModel colorModel = new ColorImageModel();
        this.colorImageController = new ColorImageController(imageSearchModel, colorModel, infoPanelManager, renderer);

        init();
    }

    public void init()
    {
        panel = new ImagingSearchPanel();
        panel.addSubPanel(searchParametersController.getPanel());
        panel.addSubPanel(imageResultsTableController.getPanel());
        panel.addSubPanel(imageCubeController.getPanel());
        panel.addSubPanel(colorImageController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }

	@Override
	public ImageSearchModel getModel()
	{
		return imageSearchModel;
	}

	@Override
	public ImagingSearchPanel getView()
	{
		return panel;
	}
}
