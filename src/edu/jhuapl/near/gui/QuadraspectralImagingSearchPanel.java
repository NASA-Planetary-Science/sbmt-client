/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImagingSearchPanel.java
 *
 * Created on May 5, 2011, 3:15:17 PM
 */
package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImagingInstrument;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.pick.PickManager;


public class QuadraspectralImagingSearchPanel extends ImagingSearchPanel implements ActionListener, ChangeListener
{
    private JPanel bandPanel;
    private JLabel bandValue;

    private JComboBox monoComboBox;
    private ComboBoxModel monoComboBoxModel;

    private ComboBoxModel redComboBoxModel;
    private ComboBoxModel greenComboBoxModel;
    private ComboBoxModel blueComboBoxModel;

    private Set<ImageKey> mapped = new HashSet<ImageKey>();
    private Set<ImageKey> visible = new HashSet<ImageKey>();

    private String[] bandNames = { "Red", "Blue", "NIR", "MH4" };
    private Integer[] bandIndices = { 0, 1, 2, 3 };
    // set current slice to MH4 band
    private int currentBandIndex = 3;
    private Map<String, Integer> bandNamesToPrefixes = new HashMap<String, Integer>();

    /** Creates new form ImagingSearchPanel */
    public QuadraspectralImagingSearchPanel(SmallBodyConfig smallBodyConfig,
            final ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            final PickManager pickManager,
            Renderer renderer,
            ImagingInstrument instrument)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, pickManager, renderer, instrument);

        redComboBoxModel = new DefaultComboBoxModel(bandNames);
        greenComboBoxModel = new DefaultComboBoxModel(bandNames);
        blueComboBoxModel = new DefaultComboBoxModel(bandNames);

        for (int i=0; i<bandNames.length; i++)
            bandNamesToPrefixes.put(bandNames[i], bandIndices[i]);

    }

    public ImagingSearchPanel init()
    {
        super.init();

        getRedComboBox().addActionListener(this);
        getGreenComboBox().addActionListener(this);
        getBlueComboBox().addActionListener(this);

        return this;
    }

    protected void populateMonochromePanel(JPanel panel)
    {
        panel.setLayout(new BorderLayout());
        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

        bandPanel.add(new JLabel("Band:"));
        monoComboBoxModel = new DefaultComboBoxModel(bandNames);
        monoComboBox = new JComboBox(monoComboBoxModel);
        monoComboBox.addActionListener(this);
        // initialize for the MH4 band
        monoComboBox.setSelectedIndex(3);
        bandPanel.add(monoComboBox);

        panel.add(bandPanel, BorderLayout.WEST);
    }


    protected ComboBoxModel getRedComboBoxModel()
    {
        return redComboBoxModel;
    }

    protected ComboBoxModel getGreenComboBoxModel()
    {
        return greenComboBoxModel;
    }

    protected ComboBoxModel getBlueComboBoxModel()
    {
        return blueComboBoxModel;
    }

    @Override
    protected ArrayList<ArrayList<String>> processResults(ArrayList<ArrayList<String>> input)
    {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
        Set<String> fileSuffixes = new HashSet<String>();

        for (ArrayList<String> item : input)
        {
            String path = item.get(0);
            String time = item.get(1);
            String[] pathArray = path.split("/");
            int size = pathArray.length;
            String fileName = pathArray[size-1];
            String fileSuffix = fileName.substring(4);
            if (!fileSuffixes.contains(fileSuffix))
            {
                fileSuffixes.add(fileSuffix);
                String resultPath = "/";
                for (int i=0; i<size-1; i++)
                    resultPath += pathArray[i] + "/";
                resultPath += fileSuffix;
                ArrayList<String> newItem = new ArrayList<String>();
                newItem.add(resultPath);
                newItem.add(time);

                results.add(newItem);
            }
        }

        return results;
    }
    protected void loadImage(ImageKey key, ImageCollection images) throws FitsException, IOException
    {
        super.loadImage(key, images);
        PerspectiveImage image = (PerspectiveImage)images.getImage(key);
        image.setCurrentSlice(currentBandIndex);
        image.setDisplayedImageRange(null);
        image.loadFootprint();
        image.firePropertyChange();

        this.mapped.add(key);
        this.visible.add(key);
    }

    protected void unloadImage(ImageKey key, ImageCollection images)
    {
        super.unloadImage(key, images);
        this.mapped.remove(key);
        this.visible.remove(key);
    }

    protected void setImageVisibility(ImageKey key, ImageCollection images, boolean isVisible)
    {
        super.setImageVisibility(key, images, isVisible);
        if (isVisible)
            this.visible.add(key);
        else
            this.visible.remove(key);
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        String newBandName = (String)((JComboBox)arg0.getSource()).getSelectedItem();
        int newBandIndex = bandNamesToPrefixes.get(newBandName);
        currentBandIndex = newBandIndex;
        System.out.println("ComboBox Value Changed: " + newBandName + "=" + newBandIndex);

        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
        Set<Image> imageSet = images.getImages();

        for (Image i : imageSet)
        {
            PerspectiveImage image = (PerspectiveImage)i;
            if (image.isVisible())
            {
               image.setCurrentSlice(newBandIndex);
               image.setDisplayedImageRange(null);
               image.loadFootprint();
               image.firePropertyChange();
            }
        }

//        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
//
//        if (!visible.isEmpty())
//        {
//            Set<ImageKey> currentVisibleKeys = new HashSet<ImageKey>(visible);
//            for (ImageKey imageKey : currentVisibleKeys)
//            {
//               if (images.containsImage(imageKey))
//               {
//                 // set the previous band image to be invisible
//                 Image image = images.getImage(imageKey);
//                 setImageVisibility(imageKey, images, false);
//
//                 // find the new band image key
//                 ImageKey newImageKey = createImageKey(imageKey, newBandName);
//                 try
//                     {
//                         // if the new image hasn't been loaded in yet, do so
//                         if (!mapped.contains(newImageKey))
//                             loadImage(newImageKey, images);
//
//                         // make the new image visible
//                         setImageVisibility(newImageKey, images, true);
//                     }
//                     catch (FitsException e1) {
//                         e1.printStackTrace();
//                     }
//                     catch (IOException e1) {
//                         e1.printStackTrace();
//                     }
//                 }
//               }
//            }
//
//        monoBandName = newBandName;
//        monoImagePrefix = bandNamesToPrefixes.get(newBandName);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        JSlider source = (JSlider)e.getSource();
        int fps = (int)source.getValue();
        bandValue.setText(Integer.toString(fps));

        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());

        Set<Image> imageSet = images.getImages();
        for (Image i : imageSet)
        {
            String name = i.getImageName();
            Boolean isVisible = i.isVisible();
            PerspectiveImage image = (PerspectiveImage)i;

            if (image.isVisible())
            {
               image.setCurrentSlice(fps);
               image.setDisplayedImageRange(null);
               if (!source.getValueIsAdjusting())
                {
//                    System.out.println("Recalculate footprint...");
                    image.loadFootprint();
                    image.firePropertyChange();
                }
            }
        }

//            System.out.println("State changed: " + fps);
    }

}
