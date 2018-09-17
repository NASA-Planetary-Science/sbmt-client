package edu.jhuapl.sbmt.gui.image.controllers;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.ImageCubePopupMenu;
import edu.jhuapl.sbmt.gui.image.model.ColorImageModel;
import edu.jhuapl.sbmt.gui.image.model.ImageCubeGenerationModel;
import edu.jhuapl.sbmt.gui.image.model.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.panels.ImagingSearchPanel;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;


public class ImagingSearchController
{
//    ColorImageGenerationPanel colorImageGenerationPanel;
//    ImageCubeGenerationPanel imageCubeGenerationPanel;
//    ImageResultsTableView imageResultsTableView;
    ImageResultsTableController imageResultsTableController;
    ImageSearchParametersController searchParametersController;
    ImageCubeGenerationController imageCubeController;
    ColorImageController colorImageController;
//    ImageSearchParametersPanel imageSearchParametersPanel;
//    ImageSearchModel imageSearchModel;
//    ImageCubeGenerationModel imageCubeGenerationModel;
//    ColorImageModel colorImageModel;

    private ImagingSearchPanel panel;

    private SmallBodyViewConfig smallBodyConfig;
    protected final ModelManager modelManager;
    private final SbmtInfoWindowManager infoPanelManager;
    private final SbmtSpectrumWindowManager spectrumPanelManager;
    private final PickManager pickManager;
    protected final Renderer renderer;


    public ImagingSearchController(SmallBodyViewConfig smallBodyConfig,
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

        this.imageResultsTableController = new ImageResultsTableController(instrument, imageCollection, imageSearchModel, renderer, infoPanelManager, spectrumPanelManager);

        this.searchParametersController = new ImageSearchParametersController(imageSearchModel, pickManager);

        ImageCubeGenerationModel cubeModel = new ImageCubeGenerationModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)imageSearchModel.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, imageBoundaryCollection, infoPanelManager, spectrumPanelManager, renderer, getPanel());
        this.imageCubeController = new ImageCubeGenerationController(imageSearchModel, cubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);

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

//        pickManager.getDefaultPicker().addPropertyChangeListener(this);
//
//        initComponents();
//
//        initExtraComponents();
//
//        populateMonochromePanel(monochromePanel);
//
//        postInitComponents(instrument);
//
//        ImageCollection images = (ImageCollection) modelManager
//                .getModel(getImageCollectionModelName());
//
////        imageResultsTableView = new ImageResultsTableView(instrument, images);
//
//        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) modelManager
//                .getModel(getImageBoundaryCollectionModelName());
//        imagePopupMenu = new ImagePopupMenu(modelManager, images, boundaries,
//                infoPanelManager, spectrumPanelManager, renderer, this);
//        boundaries.addPropertyChangeListener(this);
//        images.addPropertyChangeListener(this);
//
//        viewResultsGalleryButton.setEnabled(enableGallery);
//
//        // Setup hierarchical image search
//        initHierarchicalImageSearch();
//
//        final List<List<String>> emptyList = new ArrayList<>();
//        setImageResults(emptyList);


//        //setup Image results panel components
//        setupImageResultsPanel();
//
//        // setup Image Cube Panel components
//        setupImageCubePanel();
//
//        // setup Color Image panel components
//        setupColorImagePanel();
//
//        // setup image search parameters
//        setupSearchParametersPanel();
    }

//    private void setupImageResultsPanel()
//    {
//
//    }
//
//    private void setupImageCubePanel()
//    {
//
//    }
//
//    private void setupColorImagePanel()
//    {
//
//    }
//
//    private void setupSearchParametersPanel()
//    {
//
//    }

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
