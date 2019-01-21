package edu.jhuapl.sbmt.client;

import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.sbmt.model.image.CylindricalImage;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.util.PolyDataUtil2;
import edu.jhuapl.sbmt.util.PolyDataUtil2.PolyDataStatistics;

import nom.tam.fits.FitsException;


public class SmallBodyControlPanel extends SbmtPolyhedralModelControlPanel
{
    private final List<OpacityChangeListener> imageChangeListeners;
    private ImageKeyInterface currentImageMapKey;

    public SmallBodyControlPanel(ModelManager modelManager, String bodyName)
    {
        super(modelManager, bodyName);
        this.imageChangeListeners = Lists.newArrayList();
        this.currentImageMapKey = null;
    }

    private ImageKey createImageMapKey()
    {
        String name = getSelectedImageMapName();
        return new ImageKey(name, ImageSource.IMAGE_MAP);
    }

    @Override
    protected void showImageMap(boolean show)
    {
        PolyhedralModel smallBodyModel = getModelManager().getPolyhedralModel();
        if (smallBodyModel.isImageMapAvailable())
        {
            try
            {
                if (show)
                {
                    ImageKey key = createImageMapKey();
                    if (key != currentImageMapKey)
                    {
                        hideImageMap();
                    }
                    ImageCollection imageCollection = (ImageCollection) getModelManager().getModel(ModelNames.IMAGES);
                    Image image = imageCollection.getImage(key);
                    if (image == null)
                    {
                        imageCollection.addImage(key);
                        image = imageCollection.getImage(key);
                        getImageMapOpacitySpinner().setValue(image.getOpacity());
                        image.addPropertyChangeListener(new OpacityChangeListener(image));
                    }
                    else
                    {
                        image.setVisible(true);
                    }
                    currentImageMapKey = key;
                }
                else
                {
                    hideImageMap();
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
    }

    private void hideImageMap() {
        if (currentImageMapKey != null)
        {
            try
            {
                ImageCollection imageCollection = (ImageCollection) getModelManager().getModel(ModelNames.IMAGES);
                Image image = imageCollection.getImage(currentImageMapKey);
                if (image != null) image.setVisible(false);
//                removeListeners(imageCollection.getImage(currentImageMapKey));
//                imageCollection.removeImage(currentImageMapKey);
            }
            finally
            {
                currentImageMapKey = null;
            }
        }
    }

    @Override
    public void stateChanged(@SuppressWarnings("unused") ChangeEvent e)
    {
        ImageCollection imageCollection =
                (ImageCollection)getModelManager().getModel(ModelNames.IMAGES);

            double val = (Double)getImageMapOpacitySpinner().getValue();

            CylindricalImage image = (CylindricalImage)imageCollection.getImage(createImageMapKey());
            image.setOpacity(val);
    }


    // This method overrides the base method in PolyhedralModelControlPanel -- then proceeds to
    // do EXACTLY the same thing, using method calls to access private members of the base class.
    // It has the look of a refactoring that was halfway completed. Discovered this while adding
    // opacity control for the model itself. Since presently can find no reason the base class method
    // should be overridden, changing it here to just call the base class method. If someone
    // ever remembers why this method exists, they can resurrect it easily enough, though in that
    // case, need to be sure it correctly supersedes the CURRENT VERSION of the base class code.
    @Override
    public void itemStateChanged(ItemEvent e)
    {
        super.itemStateChanged(e);
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

    private class OpacityChangeListener implements PropertyChangeListener {
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
