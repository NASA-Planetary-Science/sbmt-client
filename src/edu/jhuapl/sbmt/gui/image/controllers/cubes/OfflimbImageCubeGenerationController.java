//package edu.jhuapl.sbmt.gui.image.controllers.cubes;
//
//import java.util.Set;
//
//import javax.swing.DefaultBoundedRangeModel;
//import javax.swing.JSlider;
//import javax.swing.JTable;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
//import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
//import edu.jhuapl.sbmt.gui.image.model.cubes.ImageCubeGenerationModel;
//import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
//import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubePopupMenu;
//import edu.jhuapl.sbmt.gui.image.ui.cubical.SpectralImageCubeGenerationPanel;
//import edu.jhuapl.sbmt.gui.image.ui.images.OfflimbImageControlPanel;
//import edu.jhuapl.sbmt.model.image.Image.ImageKey;
//import edu.jhuapl.sbmt.model.image.ImageCube;
//import edu.jhuapl.sbmt.model.image.ImageCube.ImageCubeKey;
//import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
//
//public class OfflimbImageCubeGenerationController extends ImageCubeGenerationController
//{
//    private int currentSlice = 0;
//
//    public OfflimbImageCubeGenerationController(ImageSearchModel model,
//            ImageCubeGenerationModel cubeModel,
//            SbmtInfoWindowManager infoPanelManager,
//            ImageCubePopupMenu imageCubePopupMenu,
//            SbmtSpectrumWindowManager spectrumPanelManager, Renderer renderer)
//    {
//        super(model, cubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);
//    }
//
//    @Override
//    protected void setupPanel()
//    {
//        super.setupPanel();
//        this.panel = new OfflimbImageControlPanel();
//        ((OfflimbImageControlPanel)panel).getFootprintDepthSlider().addChangeListener(new ChangeListener()
//        {
//
//            @Override
//            public void stateChanged(ChangeEvent e)
//            {
//                JTable imageList = panel.getImageCubeTable();
//
//                int index = imageList.getSelectedRow();
//                if (index == -1)
//                {
//                    setNumberOfBands(1);
//                    return;
//                }
//
//                ImageCube selectedImage = imageCubes.getLoadedImages().get(panel.getImageCubeTable().getSelectedRow());
//                ImageCubeKey selectedValue = selectedImage.getImageCubeKey();
//
//                String imagestring = selectedValue.fileNameString();
//                String[]tokens = imagestring.split(",");
//                String imagename = tokens[0].trim();
//
//                JSlider source = (JSlider)e.getSource();
//                currentSlice = (int)source.getValue();
//                ((SpectralImageCubeGenerationPanel)panel).getLayerValue().setText(Integer.toString(currentSlice));
//
//                ImageCubeCollection images = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
//
//                Set<ImageCube> imageSet = images.getImages();
//                for (ImageCube image : imageSet)
//                {
//                    ImageKey key = image.getKey();
//                    String name = image.getImageName();
//
//                    if(name.equals(imagename))
//                    {
//                       image.setCurrentSlice(currentSlice);
//                       image.setDisplayedImageRange(null);
//                       if (!source.getValueIsAdjusting())
//                       {
//                            image.loadFootprint();
//                            image.firePropertyChange();
//                       }
//                       return; // twupy1: Only change band for a single image now even if multiple ones are highlighted since different cubical images can have different numbers of bands.
//                    }
//                }
//            }
//        });
//
//    }
//
//    private void setNumberOfBands(int nbands)
//    {
//        // Select midband by default
//        setNumberOfBands(nbands, (nbands-1)/2);
//    }
//
//    private void setNumberOfBands(int nbands, int activeBand)
//    {
//        OfflimbImageControlPanel specPanel = (OfflimbImageControlPanel)panel;
//        cubeModel.setNbands(nbands);
//        specPanel.setNBands(nbands);
//        String activeBandString = Integer.toString(activeBand);
//        specPanel.getFootprintDepthValue().setText(activeBandString);
//        DefaultBoundedRangeModel monoBoundedRangeModel = new DefaultBoundedRangeModel(activeBand, 0, 0, nbands-1);
//        specPanel.getFootprintDepthSlider().setModel(monoBoundedRangeModel);
//    }
//
//
//}
