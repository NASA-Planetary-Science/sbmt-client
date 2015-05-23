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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
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


public class HyperspectralImagingSearchPanel extends ImagingSearchPanel implements ActionListener, ChangeListener
{
    private JPanel bandPanel;
    private JLabel bandValue;
    private JSlider monoSlider;
    private BoundedRangeModel monoBoundedRangeModel;

    private ComboBoxModel redComboBoxModel;
    private ComboBoxModel greenComboBoxModel;
    private ComboBoxModel blueComboBoxModel;

    private Set<ImageKey> mapped = new HashSet<ImageKey>();
    private Set<ImageKey> visible = new HashSet<ImageKey>();

    private int nbands;

    private String[] bandNames = { "125", "126", "127", "128" };
    private String[] bandPrefixes = { "125", "126", "127", "128" };
    private Map<String, String> bandNamesToPrefixes = new HashMap<String, String>();

    /** Creates new form ImagingSearchPanel */
    public HyperspectralImagingSearchPanel(SmallBodyConfig smallBodyConfig,
            final ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            final PickManager pickManager,
            Renderer renderer,
            ImagingInstrument instrument,
            int nbands)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, pickManager, renderer, instrument);

        this.nbands = nbands;

        redComboBoxModel = new DefaultComboBoxModel(bandNames);
        greenComboBoxModel = new DefaultComboBoxModel(bandNames);
        blueComboBoxModel = new DefaultComboBoxModel(bandNames);

        for (int i=0; i<bandNames.length; i++)
            bandNamesToPrefixes.put(bandNames[i], bandPrefixes[i]);

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
        int midband = nbands / 2;
        String midbandString = Integer.toString(midband);
        bandValue = new JLabel(midbandString);
        bandPanel.add(bandValue);
        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
        monoSlider = new JSlider(monoBoundedRangeModel);
        monoSlider.addChangeListener(this);
        bandPanel.add(monoSlider);

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

    protected void loadImage(ImageKey key, ImageCollection images) throws FitsException, IOException
    {
        super.loadImage(key, images);
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
        System.out.println("ComboBox Value Changed: " + newBandName);
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
            System.out.println(name + ": " + isVisible);
            PerspectiveImage image = (PerspectiveImage)i;

            if (image.isVisible())
            {
               image.setCurrentSlice(fps);
               image.setDisplayedImageRange(null);
               if (!source.getValueIsAdjusting())
                {
                    System.out.println("Recalculate footprint...");
                    image.loadFootprint();
                    image.firePropertyChange();
                }
            }
        }

//            System.out.println("State changed: " + fps);
    }

}
