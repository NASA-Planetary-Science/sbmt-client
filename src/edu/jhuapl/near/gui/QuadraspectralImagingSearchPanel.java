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

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImagingInstrument;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ImageType;
import edu.jhuapl.near.pick.PickManager;


public class QuadraspectralImagingSearchPanel extends ImagingSearchPanel implements ActionListener
{
    private JPanel bandPanel;
    private JLabel bandValue;

    private JComboBox monoComboBox;
    private ComboBoxModel monoComboBoxModel;

    private String[] bandNames = { "Red", "Blue", "NIR", "MH4" };
    private Integer[] bandIndices = { 0, 1, 2, 3 };

    // set current band to MH4 band
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

        for (int i=0; i<bandNames.length; i++)
            bandNamesToPrefixes.put(bandNames[i], bandIndices[i]);

    }

    public ImagingSearchPanel init()
    {
        super.init();

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
   }

    protected void unloadImage(ImageKey key, ImageCollection images)
    {
        super.unloadImage(key, images);
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        String newBandName = (String)((JComboBox)arg0.getSource()).getSelectedItem();
        int newBandIndex = bandNamesToPrefixes.get(newBandName);
        currentBandIndex = newBandIndex;
//        System.out.println("ComboBox Value Changed: " + newBandName + "=" + newBandIndex);

        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
        Set<Image> imageSet = images.getImages();

        for (Image i : imageSet)
        {
            PerspectiveImage image = (PerspectiveImage)i;
            ImageKey key = image.getKey();
            ImageType type = key.instrument.type;
//            System.out.println(image.getImageName() + ", " + type + ", " + image.isVisible());
            if (type == ImageType.MVIC_JUPITER_IMAGE) // this should not be specific to a given image type, should it? -turnerj1
            if (image.isVisible())
            {
               image.setCurrentSlice(newBandIndex);
               image.setDisplayedImageRange(null);
               image.loadFootprint();
               image.firePropertyChange();
            }
        }

    }


}
