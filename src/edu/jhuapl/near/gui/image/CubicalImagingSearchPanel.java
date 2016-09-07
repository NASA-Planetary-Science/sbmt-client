/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImagingSearchPanel.java
 *
 * Created on May 5, 2011, 3:15:17 PM
 */
package edu.jhuapl.near.gui.image;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhuapl.near.app.SbmtInfoWindowManager;
import edu.jhuapl.near.app.SbmtSpectrumWindowManager;
import edu.jhuapl.near.app.SmallBodyViewConfig;
import edu.jhuapl.near.model.image.ImageCube;
import edu.jhuapl.near.model.image.ImageCubeCollection;
import edu.jhuapl.near.model.image.ImagingInstrument;
import edu.jhuapl.near.model.image.PerspectiveImage;
import edu.jhuapl.near.model.image.Image.ImageKey;
import edu.jhuapl.near.model.image.ImageCube.ImageCubeKey;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;


public class CubicalImagingSearchPanel extends ImagingSearchPanel implements PropertyChangeListener, ChangeListener, ListSelectionListener
{
    private JPanel bandPanel;
    private JLabel bandValue;
    private JSlider monoSlider;
    private JCheckBox defaultFrustum;
    private BoundedRangeModel monoBoundedRangeModel;
    private javax.swing.JList imageList;

    private int nbands = 1;
    private int currentSlice = 0;

    public int getCurrentSlice() { return currentSlice; }

    public String getCurrentBand() { return Integer.toString(currentSlice); }

    /** Creates new form ImagingSearchPanel */
    public CubicalImagingSearchPanel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager,
            Renderer renderer,
            ImagingInstrument instrument)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument);
    }


    public void initComponents()
    {

    }

    protected void populateMonochromePanel(JPanel panel)
    {
        panel.setLayout(new BorderLayout());
        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        bandPanel.add(new JLabel("Layer:"));
        int midband = nbands / 2;
        String midbandString = Integer.toString(midband);
        bandValue = new JLabel(midbandString);
        bandPanel.add(bandValue);
        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
        monoSlider = new JSlider(monoBoundedRangeModel);
        monoSlider.addChangeListener(this);

        panel.add(bandPanel, BorderLayout.NORTH);
        panel.add(monoSlider, BorderLayout.SOUTH);
    }

    private void setNumberOfBands(int nbands)
    {
        this.nbands = nbands;
        int midband = nbands / 2;
        String midbandString = Integer.toString(midband);
        bandValue.setText(midbandString);
        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
        monoSlider.setModel(monoBoundedRangeModel);

    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
//        System.out.println("Cubical Images Panel Slider Moved");
        JSlider source = (JSlider)e.getSource();
        currentSlice = (int)source.getValue();
        bandValue.setText(Integer.toString(currentSlice));

        ImageCubeCollection images = (ImageCubeCollection)getModelManager().getModel(getImageCubeCollectionModelName());

        Set<ImageCube> imageSet = images.getImages();
        for (ImageCube i : imageSet)
        {
            if (i instanceof PerspectiveImage)
            {
                PerspectiveImage image = (PerspectiveImage)i;
                ImageKey key = image.getKey();
//                ImageType type = key.instrument.type;
                String name = i.getImageName();
                Boolean isVisible = i.isVisible();
//                System.out.println(name + ", " + type + ", " + isVisible);
//                System.out.println(name + ", " + isVisible);
                if (image.getImageDepth() > 1)
                {
                    if (image.isVisible())
                    {
                       image.setCurrentSlice(currentSlice);
//                       image.setDisplayedImageRange(image.getDisplayedRange());
                       image.setDisplayedImageRange(null);
                       if (!source.getValueIsAdjusting())
                       {
//                            System.out.println("Recalculate footprint...");
                            image.loadFootprint();
                            image.firePropertyChange();
                       }
                    }
                }
            }
        }

//            System.out.println("State changed: " + fps);
    }


    @Override
    public void valueChanged(ListSelectionEvent e)
    {
//        System.out.println("Cubical Images Panel Item Selected");
        JList imageList = (JList)e.getSource();

        int index = imageList.getSelectedIndex();
        ImageCubeKey selectedValue = (ImageCubeKey)imageList.getSelectedValue();
        if (selectedValue == null)
            return;

        String imagestring = selectedValue.fileNameString();
        String[]tokens = imagestring.split(",");
        String imagename = tokens[0].trim();
//        System.out.println("Image: " + index + ", " + imagename);

        ImageCubeCollection images = (ImageCubeCollection)getModelManager().getModel(getImageCubeCollectionModelName());

        Set<ImageCube> imageSet = images.getImages();
        for (ImageCube i : imageSet)
        {
            if (i instanceof PerspectiveImage)
            {
                PerspectiveImage image = (PerspectiveImage)i;
                ImageKey key = image.getKey();
                String name = i.getImageName();
                Boolean isVisible = i.isVisible();
//                System.out.println(name + ", " + isVisible);
                if (name.equals(imagename))
                {
                    int depth = image.getImageDepth();
//                    System.out.println("Found image: " + name + ", depth = " + depth);
                    if (image.isVisible())
                    {
                       setNumberOfBands(depth);
                       image.setCurrentSlice(currentSlice);
                       image.setDisplayedImageRange(null);
                       return;
                    }
                }
            }
        }

        // if no multi-band image found, set number of bands in slider to 1
        setNumberOfBands(1);
    }

//    @Override
//    public void stateChanged(ChangeEvent e)
//    {
//        JSlider source = (JSlider)e.getSource();
//        currentSlice = (int)source.getValue();
//        bandValue.setText(Integer.toString(currentSlice));
//
//        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
//
//        Set<Image> imageSet = images.getImages();
//        for (Image i : imageSet)
//        {
//            PerspectiveImage image = (PerspectiveImage)i;
//            ImageKey key = image.getKey();
//            ImageType type = key.instrument.type;
////            String name = i.getImageName();
////            Boolean isVisible = i.isVisible();
////            System.out.println(name + ", " + type + ", " + isVisible);
////            if (type == ImageType.LEISA_JUPITER_IMAGE) // this should not be specific to a given image type, should it? -turnerj1
////            {
//                if (image.isVisible())
//                {
//                   image.setCurrentSlice(currentSlice);
////                   image.setDisplayedImageRange(image.getDisplayedRange());
//                   image.setDisplayedImageRange(null);
//                   if (!source.getValueIsAdjusting())
//                   {
////                        System.out.println("Recalculate footprint...");
//                        image.loadFootprint();
//                        image.firePropertyChange();
//                   }
//                }
////            }
//        }
//
////            System.out.println("State changed: " + fps);
//    }
//
}
