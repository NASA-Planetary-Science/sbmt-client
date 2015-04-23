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
import java.util.HashMap;
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

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImagingInstrument;
import edu.jhuapl.near.model.ModelManager;
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

    private String monoBandName = "127";
    private String monoImagePrefix = "127_";
    private String redImagePrefix = "127_";
    private String greenImagePrefix = "127_";
    private String blueImagePrefix = "127_";

    private String[] bandNames = { "125", "126", "127", "128" };
    private String[] bandPrefixes = { "125", "126", "127", "128" };
    private Map<String, String> bandNamesToPrefixes = new HashMap<String, String>();
    private Map<String, Set<ImageKey>> bandNamesToKeys = new HashMap<String, Set<ImageKey>>();

    /** Creates new form ImagingSearchPanel */
    public HyperspectralImagingSearchPanel(SmallBodyConfig smallBodyConfig,
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
        bandValue = new JLabel("125");
        bandPanel.add(bandValue);
        monoBoundedRangeModel = new DefaultBoundedRangeModel(126, 0, 125, 128);
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
        if (!source.getValueIsAdjusting())
        {
            int fps = (int)source.getValue();
            bandValue.setText(Integer.toString(fps));
//            System.out.println("State changed: " + fps);
        }
    }

}
