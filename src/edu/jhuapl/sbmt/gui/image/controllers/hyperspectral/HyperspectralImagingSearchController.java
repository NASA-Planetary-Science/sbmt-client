package edu.jhuapl.sbmt.gui.image.controllers.hyperspectral;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.core.imageui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.gui.image.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.gui.image.controllers.cubes.ImageCubeController;
import edu.jhuapl.sbmt.gui.image.controllers.images.OfflimbImageResultsTableController;
import edu.jhuapl.sbmt.gui.image.controllers.search.SpectralImageSearchParametersController;
import edu.jhuapl.sbmt.gui.image.model.color.ColorImageModel;
import edu.jhuapl.sbmt.gui.image.model.cubes.ImageCubeModel;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;


public class HyperspectralImagingSearchController
{
    OfflimbImageResultsTableController imageResultsTableController;
    SpectralImageSearchParametersController searchParametersController;
    ImageCubeController imageCubeController;
    ColorImageController colorImageController;

    private ImagingSearchPanel panel;

    private SmallBodyViewConfig smallBodyConfig;
    protected final ModelManager modelManager;
    private final SbmtInfoWindowManager infoPanelManager;
    private final SbmtSpectrumWindowManager spectrumPanelManager;
    private final PickManager pickManager;
    protected final Renderer renderer;


    public HyperspectralImagingSearchController(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager, Renderer renderer,
            ImagingInstrument instrument)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.infoPanelManager = infoPanelManager;
        this.spectrumPanelManager = spectrumPanelManager;
        this.renderer = renderer;
        this.pickManager = pickManager;

        ImageSearchModel imageSearchModel = new ImageSearchModel(smallBodyConfig, modelManager, renderer, instrument);
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName());

        this.imageResultsTableController = new OfflimbImageResultsTableController(instrument, imageCollection, imageSearchModel, renderer, infoPanelManager, spectrumPanelManager);
        this.imageResultsTableController.setImageResultsPanel();

        this.searchParametersController = new SpectralImageSearchParametersController(imageSearchModel, pickManager);
        this.searchParametersController.setupSearchParametersPanel();

        ImageCubeModel cubeModel = new ImageCubeModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)imageSearchModel.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        cubeModel.setColorImageCollection(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, infoPanelManager, spectrumPanelManager, renderer, getPanel());
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
}
