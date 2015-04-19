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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.Image.ImagingInstrument;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.pick.PickManager;


public class MultispectralImagingSearchPanel extends ImagingSearchPanel implements ActionListener
{

    private ComboBoxModel redComboBoxModel;
    private ComboBoxModel greenComboBoxModel;
    private ComboBoxModel blueComboBoxModel;

    private String monochromeImagePrefix = "mc3_";
    private String redImagePrefix = "mc3_";
    private String greenImagePrefix = "mc3_";
    private String blueImagePrefix = "mc3_";

    /** Creates new form ImagingSearchPanel */
    public MultispectralImagingSearchPanel(SmallBodyConfig smallBodyConfig,
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

    protected List<ImageKey> createImageKeys(String imagePathName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        List<ImageKey> result = new ArrayList<ImageKey>();
        result.add(createImageKey(imagePathName, sourceOfLastQuery, instrument));
        return result;
    }


    protected ImageKey createImageKey(String imagePathName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        String[] imagePathArray = imagePathName.split("/");
        int size = imagePathArray.length;
        String fileName = imagePathArray[size-1];
        String prefixedFileName = monochromeImagePrefix + fileName;
        String fullImagePathName = "/";
        for (int i=0; i<size-1; i++)
            fullImagePathName += imagePathArray[i] + "/";
        fullImagePathName += prefixedFileName;

        return new ImageKey(fullImagePathName, sourceOfLastQuery, instrument);
    }


    private String[] bandNames = { "Red", "Blue", "NIR", "MH4" };
    private String[] bandPrefixes = { "mc0_", "mc1_", "mc2_", "mc3_" };
    private Map<String, String> bandNamesToPrefixes = new HashMap<String, String>();

    public ImagingSearchPanel init()
    {
        super.init();

        getRedComboBox().addActionListener(this);
        getGreenComboBox().addActionListener(this);
        getBlueComboBox().addActionListener(this);

        return this;
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
        String item = (String)((JComboBox)arg0.getSource()).getSelectedItem();
        System.out.println("ComboBox Value Changed: " + item);
        monochromeImagePrefix = bandNamesToPrefixes.get(item);
    }



//    protected void showColorImage(ActionEvent e)
//    {
//        ColorImageCollection model = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());
//
//        if (selectedRedKey != null && selectedGreenKey != null && selectedBlueKey != null)
//        {
//            ColorImageKey colorKey = new ColorImageKey(selectedRedKey, selectedGreenKey, selectedBlueKey);
//            try
//            {
//                DefaultListModel listModel = (DefaultListModel)colorImagesDisplayedList.getModel();
//                if (!model.containsImage(colorKey))
//                {
//                    model.addImage(colorKey);
//
//                    listModel.addElement(colorKey);
//                    int idx = listModel.size()-1;
//                    colorImagesDisplayedList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = colorImagesDisplayedList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        colorImagesDisplayedList.scrollRectToVisible(cellBounds);
//                }
//                else
//                {
//                    int idx = listModel.indexOf(colorKey);
//                    colorImagesDisplayedList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = colorImagesDisplayedList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        colorImagesDisplayedList.scrollRectToVisible(cellBounds);
//                }
//            }
//            catch (IOException e1)
//            {
//                e1.printStackTrace();
//            }
//            catch (FitsException e1)
//            {
//                e1.printStackTrace();
//            }
//            catch (NoOverlapException e1)
//            {
//                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
//                        "The 3 images you selected do not overlap.",
//                        "Error",
//                        JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }

//    protected void removeColorImage(ActionEvent e)
//    {
//        int index = colorImagesDisplayedList.getSelectedIndex();
//        if (index >= 0)
//        {
//            ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).remove(index);
//            ColorImageCollection model = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());
//            model.removeImage(colorKey);
//
//            // Select the element in its place (unless it's the last one in which case
//            // select the previous one)
//            if (index >= colorImagesDisplayedList.getModel().getSize())
//                --index;
//            if (index >= 0)
//                colorImagesDisplayedList.setSelectionInterval(index, index);
//        }
//    }



//    @Override
//    public void tableChanged(TableModelEvent e)
//    {
//        if (e.getColumn() == 0)
//        {
//            int row = e.getFirstRow();
//            String name = imageRawResults.get(row).get(0);
//            ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
//            try
//            {
//                ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
//                if (!images.containsImage(key))
//                    images.addImage(key);
//                else
//                    images.removeImage(key);
//            }
//            catch (FitsException e1) {
//                e1.printStackTrace();
//            }
//            catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
//        else if (e.getColumn() == 1)
//        {
//            int row = e.getFirstRow();
//            String name = imageRawResults.get(row).get(0);
//            ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
//            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
//            if (images.containsImage(key))
//            {
//                Image image = images.getImage(key);
//                image.setVisible(!(Boolean)resultList.getValueAt(row, 1));
//            }
//        }
//    }

}
