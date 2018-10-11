package edu.jhuapl.sbmt.gui.image.controllers.custom;

import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.gui.image.controllers.cubes.ImageCubeController;
import edu.jhuapl.sbmt.gui.image.model.CustomImageResultsListener;
import edu.jhuapl.sbmt.gui.image.model.color.ColorImageModel;
import edu.jhuapl.sbmt.gui.image.model.cubes.ImageCubeModel;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomImagesModel;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ImageInfo;
import edu.jhuapl.sbmt.gui.image.ui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

public class CustomImageController
{
    CustomImageResultsTableController imageResultsTableController;
    CustomImagesControlController controlController;
    ImageCubeController imageCubeController;
    ColorImageController colorImageController;

    private ImagingSearchPanel panel;

    protected final ModelManager modelManager;
    protected final Renderer renderer;
    private CustomImagesModel customImageModel;


    public CustomImageController(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager, Renderer renderer,
            ImagingInstrument instrument)
    {
        this.modelManager = modelManager;
        this.renderer = renderer;

        ImageSearchModel imageSearchModel = new ImageSearchModel(smallBodyConfig, modelManager, renderer, instrument);
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName());
        PerspectiveImageBoundaryCollection imageBoundaryCollection = (PerspectiveImageBoundaryCollection)modelManager.getModel(imageSearchModel.getImageBoundaryCollectionModelName());

        customImageModel = new CustomImagesModel(imageSearchModel);
        customImageModel.addResultsChangedListener(new CustomImageResultsListener()
        {
            @Override
            public void resultsChanged(List<ImageInfo> results)
            {
                List<List<String>> resultList = new Vector<List<String>>();
                for (ImageInfo info : results)
                {
                    resultList.add(info.toList());
                }
                customImageModel.setImageResults(resultList);
                imageResultsTableController.setImageResults(resultList);
            }
        });

        this.imageResultsTableController = new CustomImageResultsTableController(instrument, imageCollection, customImageModel, renderer, infoPanelManager, spectrumPanelManager);
        this.imageResultsTableController.setImageResultsPanel();

        this.controlController = new CustomImagesControlController(customImageModel);

        ImageCubeModel cubeModel = new ImageCubeModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)customImageModel.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        cubeModel.setColorImageCollection(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, imageBoundaryCollection, infoPanelManager, spectrumPanelManager, renderer, getPanel());
        this.imageCubeController = new ImageCubeController(customImageModel, cubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);

        ColorImageModel colorModel = new ColorImageModel();
        this.colorImageController = new ColorImageController(customImageModel, colorModel, infoPanelManager);

        init();
    }

    public void init()
    {
        panel = new ImagingSearchPanel();
        panel.addSubPanel(controlController.getPanel());
        panel.addSubPanel(imageResultsTableController.getPanel());
        panel.addSubPanel(imageCubeController.getPanel());
        panel.addSubPanel(colorImageController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }
}