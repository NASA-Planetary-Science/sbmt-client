package edu.jhuapl.sbmt.gui.image.controllers.custom;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTable;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.gui.image.controllers.cubes.ImageCubeController;
import edu.jhuapl.sbmt.gui.image.controllers.cubes.SpectralImageCubeController;
import edu.jhuapl.sbmt.gui.image.model.CustomImageResultsListener;
import edu.jhuapl.sbmt.gui.image.model.ImageSearchModelListener;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomColorImageModel;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomImageCubeModel;
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

        ImageCollection customImageCollection = customImageModel.getImageCollection();
        this.imageResultsTableController = new CustomImageResultsTableController(instrument, customImageCollection, customImageModel, renderer, infoPanelManager, spectrumPanelManager);
        this.imageResultsTableController.setImageResultsPanel();

        this.controlController = new CustomImagesControlController(customImageModel);

        CustomImageCubeModel customCubeModel = new CustomImageCubeModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)customImageModel.getModelManager().getModel(customCubeModel.getImageCubeCollectionModelName());
        customCubeModel.setImageSearchModel(customImageModel);
        customCubeModel.setColorImageCollection(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, imageBoundaryCollection, infoPanelManager, spectrumPanelManager, renderer, getPanel());
        this.imageCubeController = new SpectralImageCubeController(customImageModel, customCubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);
        CustomColorImageModel colorModel = new CustomColorImageModel();
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