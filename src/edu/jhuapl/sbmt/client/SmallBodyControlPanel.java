package edu.jhuapl.sbmt.client;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import edu.jhuapl.saavtk.gui.panel.PolyhedralModelControlPanel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.sbmt.model.image.CylindricalImage;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.util.PolyDataUtil2;
import edu.jhuapl.sbmt.util.PolyDataUtil2.PolyDataStatistics;

public class SmallBodyControlPanel extends PolyhedralModelControlPanel
{
    private static final long serialVersionUID = 518373430237465750L;
    private static final String IMAGE_MAP_TEXT = "Show Image Map";
    private static final String EMPTY_SELECTION = "None";

    private final Map<String, ImageKeyInterface> imageMapKeys;
    private final JCheckBox imageMapCheckBox;
    private final JComboBox<String> imageMapComboBox;
    private final JSpinner imageMapOpacitySpinner;
    private final JLabel opacityLabel;
    private final List<OpacityChangeListener> imageChangeListeners;

    public SmallBodyControlPanel(ModelManager modelManager, String bodyName)
    {
        super(modelManager, bodyName);
        this.imageMapKeys = new HashMap<>();
        this.imageChangeListeners = new ArrayList<>();

        SmallBodyModel smallBodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();

        if (!smallBodyModel.getImageMapKeys().isEmpty())
        {
            imageMapCheckBox = configureImageMapCheckBox(smallBodyModel);

            imageMapComboBox = configureImageMapComboBox(smallBodyModel);

            opacityLabel = new JLabel("Image opacity");
            imageMapOpacitySpinner = createOpacitySpinner();
            imageMapOpacitySpinner.addChangeListener(this);
            opacityLabel.setEnabled(false);
            imageMapOpacitySpinner.setEnabled(false);

            JPanel panel = (JPanel) getScrollPane().getViewport().getView();
            if (imageMapComboBox != null)
            {
                panel.add(new JLabel(IMAGE_MAP_TEXT), "wrap");
                panel.add(imageMapComboBox, "wrap");
            }
            else
            {
                panel.add(imageMapCheckBox, "wrap");
            }
            panel.add(opacityLabel, "gapleft 25, split 2");
            panel.add(imageMapOpacitySpinner, "wrap");
        }
        else
        {
            imageMapCheckBox = null;
            imageMapComboBox = null;
            imageMapOpacitySpinner = null;
            opacityLabel = null;
        }
    }

    private ImageKeyInterface getCurrentImageMapKey()
    {
        ImageKeyInterface result = null;

        if (imageMapComboBox != null)
        {
            Object selection = imageMapComboBox.getSelectedItem();
            if (selection != null)
            {
                result = imageMapKeys.get(selection);
            }
        }
        else if (!imageMapKeys.isEmpty())
        {
            result = imageMapKeys.values().iterator().next();
        }

        return result;
    }

    public JSpinner getImageMapOpacitySpinner()
    {
        return imageMapOpacitySpinner;
    }

    protected boolean isImageMapEnabled()
    {
        if (imageMapComboBox != null)
            return !EMPTY_SELECTION.equals(imageMapComboBox.getSelectedItem());
        return imageMapCheckBox.isSelected();
    }

    public JLabel getOpacityLabel()
    {
        return opacityLabel;
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        super.itemStateChanged(e);

        PickUtil.setPickingEnabled(false);

        ItemSelectable selectedItem = e.getItemSelectable();
        if (selectedItem == imageMapCheckBox)
        {
            boolean show = imageMapCheckBox.isSelected();
            showImageMap(getCurrentImageMapKey(), show);
        }
        else if (selectedItem == this.imageMapComboBox)
        {
            String item = (String) e.getItem();
            boolean show = e.getStateChange() == ItemEvent.SELECTED;
            showImageMap(imageMapKeys.get(item), show);
        }
        else
        {
            PickUtil.setPickingEnabled(true);
        }
    }

    private final JCheckBox configureImageMapCheckBox(SmallBodyModel model)
    {
        JCheckBox result = null;
        List<ImageKeyInterface> mapKeys = model.getImageMapKeys();
        if (mapKeys.size() == 1)
        {
            ImageKeyInterface key = mapKeys.get(0);
            imageMapKeys.put(key.getOriginalName(), key);

            result = new JCheckBox();
            result.setText(IMAGE_MAP_TEXT);
            result.setSelected(false);
            result.addItemListener(this);
        }

        return result;
    }
    private final JComboBox<String> configureImageMapComboBox(SmallBodyModel model)
    {
        JComboBox<String> result = null;
        List<ImageKeyInterface> mapKeys = model.getImageMapKeys();
        if (mapKeys.size() > 1)
        {
            String[] allOptions = new String[mapKeys.size() + 1];
            int index = 0;
            allOptions[index] = EMPTY_SELECTION;
            imageMapKeys.put(EMPTY_SELECTION, null);
            for (; index < mapKeys.size(); ++index)
            {
                ImageKeyInterface key = mapKeys.get(index);
                String name = key.getOriginalName();
                allOptions[index + 1] = name;
                imageMapKeys.put(name, key);
            }
            result = new JComboBox<>(allOptions);
            result.addItemListener(this);
        }

        return result;
    }

    protected void showImageMap(ImageKeyInterface key, boolean show)
    {
        if (key == null)
        {
            return;
        }

        try
        {
            ImageCollection imageCollection = (ImageCollection) getModelManager().getModel(ModelNames.IMAGES);
            Image image = imageCollection.getImage(key);

            if (show && image == null)
            {
                // The first time this image is displayed, need to load it.
                setCursor(new Cursor(Cursor.WAIT_CURSOR));

                imageCollection.addImage(key);
                image = imageCollection.getImage(key);
                imageMapOpacitySpinner.setValue(image.getOpacity());
                image.addPropertyChangeListener(new OpacityChangeListener(image));
            }

            if (image != null)
            {
                image.setVisible(show);
            }
            opacityLabel.setEnabled(show);
            imageMapOpacitySpinner.setEnabled(show);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void stateChanged(@SuppressWarnings("unused") ChangeEvent e)
    {
        ImageCollection imageCollection =
                (ImageCollection)getModelManager().getModel(ModelNames.IMAGES);

        double val = (Double)getImageMapOpacitySpinner().getValue();

        ImageKeyInterface key = getCurrentImageMapKey();
        if (key != null)
        {
            CylindricalImage image = (CylindricalImage)imageCollection.getImage(key);
            image.setOpacity(val);
        }
    }

    @Override
    protected void addAdditionalStatisticsToLabel()
    {
        PolyhedralModel smallBodyModel = getModelManager().getPolyhedralModel();
        Double refPotential = smallBodyModel.getReferencePotential();
        PolyDataStatistics stat = PolyDataUtil2.getPolyDataStatistics(smallBodyModel.getSmallBodyPolyData());
        String refPotentialString = refPotential != Double.MAX_VALUE ? String.valueOf(refPotential) : "(not available)";

        String newText =
                "&nbsp;&nbsp;&nbsp;Number of Edges: " + stat.numberEdges + " <sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Reference Potential: " + refPotentialString + " J/kg<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Plate Area Standard Deviation: " + String.format("%.7g", 1.0e6 * stat.stdCellArea) + " m<sup>2</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Edge Length Average: " + String.format("%.7g", 1.0e3 * stat.meanEdgeLength) + " m<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Edge Length Minimum: " + String.format("%.7g", 1.0e3 * stat.minEdgeLength) + " m<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Edge Length Maximum: " + String.format("%.7g", 1.0e3 * stat.maxEdgeLength) + " m<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Edge Length Standard Deviation: " + String.format("%.7g", 1.0e3 * stat.stdEdgeLength) + " m<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Is Surface Closed? " + (stat.isClosed ? "Yes" : "No") + " <sup>&nbsp;</sup><br>";
                if (stat.isClosed)
                {
                    newText += "&nbsp;&nbsp;&nbsp;Centroid:<sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[" + String.format("%.7g, %.7g, %.7g", stat.centroid[0], stat.centroid[1], stat.centroid[2]) + "] km<sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;Moment of Inertia Tensor Relative to Origin:<sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[" + String.format("%.7g, %.7g, %.7g", stat.inertiaWorld[0][0], stat.inertiaWorld[0][1], stat.inertiaWorld[0][2]) + "] <sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[" + String.format("%.7g, %.7g, %.7g", stat.inertiaWorld[1][0], stat.inertiaWorld[1][1], stat.inertiaWorld[1][2]) + "] <sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[" + String.format("%.7g, %.7g, %.7g", stat.inertiaWorld[2][0], stat.inertiaWorld[2][1], stat.inertiaWorld[2][2]) + "] <sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;Moment of Inertia Tensor Relative to Centroid:<sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[" + String.format("%.7g, %.7g, %.7g", stat.inertiaCOM[0][0], stat.inertiaCOM[0][1], stat.inertiaCOM[0][2]) + "] <sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[" + String.format("%.7g, %.7g, %.7g", stat.inertiaCOM[1][0], stat.inertiaCOM[1][1], stat.inertiaCOM[1][2]) + "] <sup>&nbsp;</sup><br>"
                            + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[" + String.format("%.7g, %.7g, %.7g", stat.inertiaCOM[2][0], stat.inertiaCOM[2][1], stat.inertiaCOM[2][2]) + "] <sup>&nbsp;</sup><br>";
                }
        try
        {

            ((HTMLEditorKit)getStatisticsLabel().getEditorKit()).insertHTML(
                    (HTMLDocument)getStatisticsLabel().getDocument(),
                    getStatisticsLabel().getDocument().getLength(),
                    newText, 0, 0, null);

            final int originalScrollBarValue = getScrollPane().getVerticalScrollBar().getValue();
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    getScrollPane().getVerticalScrollBar().setValue(originalScrollBarValue);
                }
            });
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static JSpinner createOpacitySpinner()
    {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00"));
        spinner.setPreferredSize(new Dimension(80, 21));
        return spinner;
    }

    protected class OpacityChangeListener implements PropertyChangeListener {
        private final Image image;

        OpacityChangeListener(Image image) {
            this.image = image;
            imageChangeListeners.add(this);
        }

        @Override
        public void propertyChange(@SuppressWarnings("unused") PropertyChangeEvent evt)
        {
            getImageMapOpacitySpinner().setValue(image.getOpacity());
        }

    }

}
