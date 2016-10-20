package edu.jhuapl.sbmt.client;

import java.awt.event.ItemEvent;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import nom.tam.fits.FitsException;

import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.sbmt.model.image.CylindricalImage;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.util.PolyDataUtil2;
import edu.jhuapl.sbmt.util.PolyDataUtil2.PolyDataStatistics;


public class SmallBodyControlPanel extends SbmtPolyhedralModelControlPanel
{
    public SmallBodyControlPanel(ModelManager modelManager, String bodyName)
    {
        super(modelManager, bodyName);
    }

    private ImageKey createImageMapKey()
    {
        PolyhedralModel smallBodyModel = getModelManager().getPolyhedralModel();
        String name = smallBodyModel.getImageMapNames()[0];
        return new ImageKey(name, ImageSource.IMAGE_MAP);
    }

    protected void showImageMap(boolean show)
    {
        PolyhedralModel smallBodyModel = getModelManager().getPolyhedralModel();
        ImageCollection imageCollection = (ImageCollection)getModelManager().getModel(ModelNames.IMAGES);

        try
        {
            if (show)
            {
                if (smallBodyModel.isImageMapAvailable())
                {
                    imageCollection.addImage(createImageMapKey());
                }
            }
            else
            {
                imageCollection.removeImage(createImageMapKey());
            }
        }
        catch (FitsException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stateChanged(ChangeEvent e)
    {
        ImageCollection imageCollection =
            (ImageCollection)getModelManager().getModel(ModelNames.IMAGES);

        double val = (Double)getImageMapOpacitySpinner().getValue();

        CylindricalImage image = (CylindricalImage)imageCollection.getImage(createImageMapKey());
        image.setOpacity(val);
    }


    public void itemStateChanged(ItemEvent e)
    {
        Picker.setPickingEnabled(false);

        PolyhedralModel smallBodyModel = getModelManager().getPolyhedralModel();

        if (e.getItemSelectable() == this.getModelCheckBox())
        {
            // In the following we ensure that the graticule and image map are shown
            // only if the shape model is shown
            Graticule graticule = (Graticule)getModelManager().getModel(ModelNames.GRATICULE);
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                smallBodyModel.setShowSmallBody(true);
                if (graticule != null && getGridCheckBox().isSelected())
                    graticule.setShowGraticule(true);
                if (getImageMapCheckBox().isSelected())
                    showImageMap(true);
            }
            else
            {
                smallBodyModel.setShowSmallBody(false);
                if (graticule != null && getGridCheckBox().isSelected())
                    graticule.setShowGraticule(false);
                if (getImageMapCheckBox().isSelected())
                    showImageMap(false);
            }
        }
        else if (e.getItemSelectable() == this.getGridCheckBox())
        {
            Graticule graticule = (Graticule)getModelManager().getModel(ModelNames.GRATICULE);
            if (graticule != null)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    graticule.setShowGraticule(true);
                else
                    graticule.setShowGraticule(false);
            }
        }
        else if (e.getItemSelectable() == this.getImageMapCheckBox())
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                showImageMap(true);
                getOpacityLabel().setEnabled(true);
                getImageMapOpacitySpinner().setEnabled(true);
            }
            else
            {
                showImageMap(false);
                getOpacityLabel().setEnabled(false);
                getImageMapOpacitySpinner().setEnabled(false);
            }
        }
        else if (this.getResModelButtons().contains(e.getItemSelectable()))
        {
            if (((AbstractButton)e.getItemSelectable()).isSelected())
                try {
                    int level = this.getResModelButtons().indexOf(e.getItemSelectable());
                    smallBodyModel.setModelResolution(level);
                    setStatisticsLabel();
                    getAdditionalStatisticsButton().setVisible(true);
                    updateColoringComboBoxes();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        else if (e.getItemSelectable() == this.getNoColoringButton())
        {
            updateColoringControls();

            try
            {
                smallBodyModel.setColoringIndex(-1);
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else if (e.getItemSelectable() == this.getStandardColoringButton())
        {
            updateColoringControls();
            setColoring();
        }
        else if (e.getItemSelectable() == this.getRgbColoringButton())
        {
            updateColoringControls();
            setColoring();
        }
        else if (e.getItemSelectable() == this.getColoringComboBox())
        {
            setColoring();
        }
        else if (e.getItemSelectable() == getRgbColoringButton())
        {
            setColoring();
        }
        else if (e.getItemSelectable() == getCustomColorRedComboBox() ||
                e.getItemSelectable() == getCustomColorGreenComboBox() ||
                e.getItemSelectable() == getCustomColorBlueComboBox())
        {
            setColoring();
        }

        Picker.setPickingEnabled(true);
    }


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


}
