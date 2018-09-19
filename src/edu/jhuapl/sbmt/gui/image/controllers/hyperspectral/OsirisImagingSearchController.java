package edu.jhuapl.sbmt.gui.image.controllers.hyperspectral;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.gui.image.controllers.cubes.OfflimbImageCubeGenerationController;
import edu.jhuapl.sbmt.gui.image.controllers.images.OfflimbImageResultsTableController;
import edu.jhuapl.sbmt.gui.image.controllers.search.SpectralImageSearchParametersController;
import edu.jhuapl.sbmt.gui.image.model.color.ColorImageModel;
import edu.jhuapl.sbmt.gui.image.model.cubes.ImageCubeGenerationModel;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.gui.image.ui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;


public class OsirisImagingSearchController
{
    OfflimbImageResultsTableController imageResultsTableController;
    SpectralImageSearchParametersController searchParametersController;
    OfflimbImageCubeGenerationController imageCubeController;
    ColorImageController colorImageController;

    private ImagingSearchPanel panel;

    private SmallBodyViewConfig smallBodyConfig;
    protected final ModelManager modelManager;
    private final SbmtInfoWindowManager infoPanelManager;
    private final SbmtSpectrumWindowManager spectrumPanelManager;
    private final PickManager pickManager;
    protected final Renderer renderer;


    public OsirisImagingSearchController(SmallBodyViewConfig smallBodyConfig,
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
        PerspectiveImageBoundaryCollection imageBoundaryCollection = (PerspectiveImageBoundaryCollection)modelManager.getModel(imageSearchModel.getImageBoundaryCollectionModelName());

        this.imageResultsTableController = new OfflimbImageResultsTableController(instrument, imageCollection, imageSearchModel, renderer, infoPanelManager, spectrumPanelManager);
        this.imageResultsTableController.setImageResultsPanel();

        this.searchParametersController = new SpectralImageSearchParametersController(imageSearchModel, pickManager);
        this.searchParametersController.setupSearchParametersPanel();

        ImageCubeGenerationModel cubeModel = new ImageCubeGenerationModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)imageSearchModel.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        cubeModel.setImages(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, imageBoundaryCollection, infoPanelManager, spectrumPanelManager, renderer, getPanel());
        this.imageCubeController = new OfflimbImageCubeGenerationController(imageSearchModel, cubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);

        ColorImageModel colorModel = new ColorImageModel();
        this.colorImageController = new ColorImageController(imageSearchModel, colorModel, infoPanelManager);

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

    protected void initExtraComponents()
    {
        // to be overridden by subclasses
    }

    protected void populateMonochromePanel(JPanel panel)
    {
        // to be overridden by subclasses
    }

    public JPanel getPanel()
    {
        return panel;
    }
}
