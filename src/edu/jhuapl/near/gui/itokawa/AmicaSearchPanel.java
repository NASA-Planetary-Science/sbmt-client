/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ItokawaSearchPanel3.java
 *
 * Created on May 5, 2011, 3:15:17 PM
 */
package edu.jhuapl.near.gui.itokawa;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;

import nom.tam.fits.FitsException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkActor;
import vtk.vtkPolyData;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ColorImage.ColorImageKey;
import edu.jhuapl.near.model.ColorImage.NoOverlapException;
import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.itokawa.AmicaBoundaryCollection;
import edu.jhuapl.near.model.itokawa.AmicaColorImageCollection;
import edu.jhuapl.near.model.itokawa.AmicaImageCollection;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.query.ItokawaQuery;
import edu.jhuapl.near.util.IdPair;
import edu.jhuapl.near.util.Properties;

/**
 *
 * @author kahneg1
 */
public class AmicaSearchPanel extends javax.swing.JPanel implements PropertyChangeListener
{
    private final ModelManager modelManager;
    private final PickManager pickManager;
    private java.util.Date startDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
    private java.util.Date endDate = new GregorianCalendar(2005, 10, 31, 0, 0, 0).getTime();
    private IdPair resultIntervalCurrentlyShown = null;
    private ImageKey selectedRedKey;
    private ImageKey selectedGreenKey;
    private ImageKey selectedBlueKey;

    // The source of the images of the most recently executed query
    private Image.ImageSource sourceOfLastQuery = Image.ImageSource.PDS;

    private ArrayList<String> amicaRawResults = new ArrayList<String>();
    private ImagePopupMenu amicaPopupMenu;
    private ColorImagePopupMenu amicaColorPopupMenu;

    /** Creates new form ItokawaSearchPanel3 */
    public AmicaSearchPanel(final ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            final PickManager pickManager,
            Renderer renderer)
    {
        this.modelManager = modelManager;
        this.pickManager = pickManager;

        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        initComponents();

    }

    private void resultsListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = resultList.locationToIndex(e.getPoint());

            if (index >= 0 && resultList.getCellBounds(index, index).contains(e.getPoint()))
            {
                resultList.setSelectedIndex(index);
                String name = amicaRawResults.get(index);
                amicaPopupMenu.setCurrentImage(new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery));
                amicaPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


    private void colorImagesDisplayedListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = colorImagesDisplayedList.locationToIndex(e.getPoint());

            if (index >= 0 && colorImagesDisplayedList.getCellBounds(index, index).contains(e.getPoint()))
            {
                colorImagesDisplayedList.setSelectedIndex(index);
                ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).get(index);
                amicaColorPopupMenu.setCurrentImage(colorKey);
                amicaColorPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void setAmicaResults(ArrayList<String> results)
    {
        resultsLabel.setText(results.size() + " images matched");
        amicaRawResults = results;

        String[] formattedResults = new String[results.size()];

        // add the results to the list
        int i=0;
        for (String str : results)
        {
            formattedResults[i] = new String(
                    (i+1) + ": " +
                    str.substring(23, 32)
                    + ", day: " + str.substring(10, 13) + "/" + str.substring(5, 9)
                    + ", type: " + str.substring(14, 20)
                    + ", filter: " + str.substring(33, 34)
                    + ", source: " + sourceOfLastQuery
                    );

            ++i;
        }

        resultList.setListData(formattedResults);

        // Show the first set of boundaries
        this.resultIntervalCurrentlyShown = new IdPair(0, (Integer)this.numberOfBoundariesComboBox.getSelectedItem());
        this.showAmicaBoundaries(resultIntervalCurrentlyShown);
    }


    private void showAmicaBoundaries(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        AmicaBoundaryCollection model = (AmicaBoundaryCollection)modelManager.getModel(ModelNames.AMICA_BOUNDARY);
        model.removeAllBoundaries();

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= amicaRawResults.size())
                break;

            try
            {
                String currentImage = amicaRawResults.get(i);
                //String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_BOUNDARY.VTK";
                //String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_DDR.LBL";
                String boundaryName = currentImage.substring(0,currentImage.length()-4);
                model.addBoundary(new ImageKey(boundaryName, sourceOfLastQuery));
            }
            catch (FitsException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private void showColorImage(ActionEvent e)
    {
        AmicaColorImageCollection model = (AmicaColorImageCollection)modelManager.getModel(ModelNames.AMICA_COLOR_IMAGES);

        if (selectedRedKey != null && selectedGreenKey != null && selectedBlueKey != null)
        {
            ColorImageKey colorKey = new ColorImageKey(selectedRedKey, selectedGreenKey, selectedBlueKey);
            try
            {
                DefaultListModel listModel = (DefaultListModel)colorImagesDisplayedList.getModel();
                if (!model.containsImage(colorKey))
                {
                    model.addImage(colorKey);

                    listModel.addElement(colorKey);
                    int idx = listModel.size()-1;
                    colorImagesDisplayedList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = colorImagesDisplayedList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        colorImagesDisplayedList.scrollRectToVisible(cellBounds);
                }
                else
                {
                    int idx = listModel.indexOf(colorKey);
                    colorImagesDisplayedList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = colorImagesDisplayedList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        colorImagesDisplayedList.scrollRectToVisible(cellBounds);
                }
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            catch (FitsException e1)
            {
                e1.printStackTrace();
            }
            catch (NoOverlapException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "The 3 images you selected do not overlap.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof AmicaImageCollection || model instanceof AmicaBoundaryCollection)
            {
                String name = null;

                if (model instanceof AmicaImageCollection)
                    name = ((AmicaImageCollection)model).getImageName((vtkActor)e.getPickedProp());
                else if (model instanceof AmicaBoundaryCollection)
                    name = ((AmicaBoundaryCollection)model).getBoundaryName((vtkActor)e.getPickedProp());

                int idx = amicaRawResults.indexOf(name + ".FIT");

                resultList.setSelectionInterval(idx, idx);
                Rectangle cellBounds = resultList.getCellBounds(idx, idx);
                if (cellBounds != null)
                    resultList.scrollRectToVisible(cellBounds);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        startDateLabel = new javax.swing.JLabel();
        startSpinner = new javax.swing.JSpinner();
        endDateLabel = new javax.swing.JLabel();
        endSpinner = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        filter1CheckBox = new javax.swing.JCheckBox();
        filter2CheckBox = new javax.swing.JCheckBox();
        filter3CheckBox = new javax.swing.JCheckBox();
        filter4CheckBox = new javax.swing.JCheckBox();
        filter5CheckBox = new javax.swing.JCheckBox();
        filter6CheckBox = new javax.swing.JCheckBox();
        filter7CheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        fromDistanceLabel = new javax.swing.JLabel();
        fromDistanceTextField = new javax.swing.JFormattedTextField();
        toDistanceLabel = new javax.swing.JLabel();
        toDistanceTextField = new javax.swing.JFormattedTextField();
        endDistanceLabel = new javax.swing.JLabel();
        fromResolutionLabel = new javax.swing.JLabel();
        fromResolutionTextField = new javax.swing.JFormattedTextField();
        toResolutionLabel = new javax.swing.JLabel();
        toResolutionTextField = new javax.swing.JFormattedTextField();
        endResolutionLabel = new javax.swing.JLabel();
        fromIncidenceLabel = new javax.swing.JLabel();
        fromIncidenceTextField = new javax.swing.JFormattedTextField();
        toIncidenceLabel = new javax.swing.JLabel();
        toIncidenceTextField = new javax.swing.JFormattedTextField();
        endIncidenceLabel = new javax.swing.JLabel();
        fromEmissionLabel = new javax.swing.JLabel();
        fromEmissionTextField = new javax.swing.JFormattedTextField();
        toEmissionLabel = new javax.swing.JLabel();
        toEmissionTextField = new javax.swing.JFormattedTextField();
        endEmissionLabel = new javax.swing.JLabel();
        fromPhaseLabel = new javax.swing.JLabel();
        fromPhaseTextField = new javax.swing.JFormattedTextField();
        toPhaseLabel = new javax.swing.JLabel();
        toPhaseTextField = new javax.swing.JFormattedTextField();
        endPhaseLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        searchByNumberCheckBox = new javax.swing.JCheckBox();
        searchByNumberTextField = new javax.swing.JFormattedTextField();
        jPanel5 = new javax.swing.JPanel();
        clearRegionButton = new javax.swing.JButton();
        submitButton = new javax.swing.JButton();
        selectRegionButton = new javax.swing.JToggleButton();
        jPanel6 = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultList = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        numberOfBoundariesComboBox = new javax.swing.JComboBox();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        removeAllImagesButton = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel10 = new javax.swing.JPanel();
        redButton = new javax.swing.JButton();
        redLabel = new javax.swing.JLabel();
        greenButton = new javax.swing.JButton();
        blueButton = new javax.swing.JButton();
        greenLabel = new javax.swing.JLabel();
        blueLabel = new javax.swing.JLabel();
        generateColorImageButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        colorImagesDisplayedList = new javax.swing.JList();
        removeColorImageButton = new javax.swing.JButton();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        startDateLabel.setText("Start Date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 6, 0, 0);
        jPanel1.add(startDateLabel, gridBagConstraints);

        startSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 28));
        startSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(startSpinner, gridBagConstraints);

        endDateLabel.setText("End Date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 6, 0, 0);
        jPanel1.add(endDateLabel, gridBagConstraints);

        endSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1132462800000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(endSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel8.add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        filter1CheckBox.setText("Filter 1 (550 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 0);
        jPanel2.add(filter1CheckBox, gridBagConstraints);

        filter2CheckBox.setText("Filter 2 (450 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        jPanel2.add(filter2CheckBox, gridBagConstraints);

        filter3CheckBox.setText("Filter 3 (760 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        jPanel2.add(filter3CheckBox, gridBagConstraints);

        filter4CheckBox.setText("Filter 4 (950 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 6, 0);
        jPanel2.add(filter4CheckBox, gridBagConstraints);

        filter5CheckBox.setText("Filter 5 (900 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 8, 0, 0);
        jPanel2.add(filter5CheckBox, gridBagConstraints);

        filter6CheckBox.setText("Filter 6 (1000 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        jPanel2.add(filter6CheckBox, gridBagConstraints);

        filter7CheckBox.setText("Filter 7 (1050 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 0);
        jPanel2.add(filter7CheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        fromDistanceLabel.setText("S/C Distance from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel3.add(fromDistanceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(fromDistanceTextField, gridBagConstraints);

        toDistanceLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel3.add(toDistanceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(toDistanceTextField, gridBagConstraints);

        endDistanceLabel.setText("km");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 27;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 0);
        jPanel3.add(endDistanceLabel, gridBagConstraints);

        fromResolutionLabel.setText("Resolution from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 19, 0, 0);
        jPanel3.add(fromResolutionLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        jPanel3.add(fromResolutionTextField, gridBagConstraints);

        toResolutionLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 6, 0, 0);
        jPanel3.add(toResolutionLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 45;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        jPanel3.add(toResolutionTextField, gridBagConstraints);

        endResolutionLabel.setText("mpp");
        endResolutionLabel.setToolTipText("meters per pixel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 6);
        jPanel3.add(endResolutionLabel, gridBagConstraints);

        fromIncidenceLabel.setText("Incidence from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 25, 0, 0);
        jPanel3.add(fromIncidenceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(fromIncidenceTextField, gridBagConstraints);

        toIncidenceLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel3.add(toIncidenceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 45;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(toIncidenceTextField, gridBagConstraints);

        endIncidenceLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 0);
        jPanel3.add(endIncidenceLabel, gridBagConstraints);

        fromEmissionLabel.setText("Emission from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 30, 0, 0);
        jPanel3.add(fromEmissionLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(fromEmissionTextField, gridBagConstraints);

        toEmissionLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel3.add(toEmissionLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(toEmissionTextField, gridBagConstraints);

        endEmissionLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 0);
        jPanel3.add(endEmissionLabel, gridBagConstraints);

        fromPhaseLabel.setText("Phase from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 46, 0, 0);
        jPanel3.add(fromPhaseLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(fromPhaseTextField, gridBagConstraints);

        toPhaseLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel3.add(toPhaseLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel3.add(toPhaseTextField, gridBagConstraints);

        endPhaseLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 0);
        jPanel3.add(endPhaseLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        searchByNumberCheckBox.setText("Search by number");
        searchByNumberCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchByNumberCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 6, 0, 0);
        jPanel4.add(searchByNumberCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 122;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        jPanel4.add(searchByNumberTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(jPanel4, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        clearRegionButton.setText("Clear Region");
        clearRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel5.add(clearRegionButton, gridBagConstraints);

        submitButton.setText("Search");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 40);
        jPanel5.add(submitButton, gridBagConstraints);

        selectRegionButton.setText("Select Region");
        selectRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel5.add(selectRegionButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        jPanel8.add(jPanel5, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 316;
        gridBagConstraints.ipady = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(resultsLabel, gridBagConstraints);

        resultList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                resultListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resultListMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(resultList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel6.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel8.add(jPanel6, gridBagConstraints);

        jPanel7.setBorder(null);
        jPanel7.setLayout(new java.awt.GridBagLayout());

        jLabel6.setText("Number Boundaries:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(jLabel6, gridBagConstraints);

        numberOfBoundariesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220", "230", "240", "250", " " }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(numberOfBoundariesComboBox, gridBagConstraints);

        prevButton.setText("<");
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(prevButton, gridBagConstraints);

        nextButton.setText(">");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(nextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(jPanel7, gridBagConstraints);

        removeAllButton.setText("Remove All Boundaries");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        jPanel8.add(removeAllButton, gridBagConstraints);

        removeAllImagesButton.setText("Remove All Images");
        removeAllImagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllImagesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        jPanel8.add(removeAllImagesButton, gridBagConstraints);

        jPanel9.setLayout(new java.awt.GridBagLayout());

        jLabel20.setText("Color Image Generation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel9.add(jLabel20, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel9.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel8.add(jPanel9, gridBagConstraints);

        jPanel10.setLayout(new java.awt.GridBagLayout());

        redButton.setBackground(new java.awt.Color(255, 0, 0));
        redButton.setText("Red");
        redButton.setToolTipText("Select an image from the list above and then press this button");
        redButton.setEnabled(false);
        redButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(redButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(redLabel, gridBagConstraints);

        greenButton.setBackground(new java.awt.Color(0, 255, 0));
        greenButton.setText("Green");
        greenButton.setToolTipText("Select an image from the list above and then press this button");
        greenButton.setEnabled(false);
        greenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(greenButton, gridBagConstraints);

        blueButton.setBackground(new java.awt.Color(0, 0, 255));
        blueButton.setText("Blue");
        blueButton.setToolTipText("Select an image from the list above and then press this button");
        blueButton.setEnabled(false);
        blueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blueButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(blueButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(greenLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(blueLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel8.add(jPanel10, gridBagConstraints);

        generateColorImageButton.setText("Generate Color Image");
        generateColorImageButton.setEnabled(false);
        generateColorImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateColorImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        jPanel8.add(generateColorImageButton, gridBagConstraints);

        colorImagesDisplayedList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                colorImagesDisplayedListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                colorImagesDisplayedListMouseReleased(evt);
            }
        });
        jScrollPane3.setViewportView(colorImagesDisplayedList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(jScrollPane3, gridBagConstraints);

        removeColorImageButton.setText("Remove");
        removeColorImageButton.setEnabled(false);
        removeColorImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColorImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        jPanel8.add(removeColorImageButton, gridBagConstraints);

        jScrollPane2.setViewportView(jPanel8);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }//GEN-END:initComponents

    private void formComponentHidden(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentHidden
    {//GEN-HEADEREND:event_formComponentHidden
        selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);
    }//GEN-LAST:event_formComponentHidden

    private void startSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_startSpinnerStateChanged
    {//GEN-HEADEREND:event_startSpinnerStateChanged
        java.util.Date date =
                ((SpinnerDateModel)startSpinner.getModel()).getDate();
        if (date != null)
            startDate = date;
    }//GEN-LAST:event_startSpinnerStateChanged

    private void endSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_endSpinnerStateChanged
    {//GEN-HEADEREND:event_endSpinnerStateChanged
        java.util.Date date =
                ((SpinnerDateModel)endSpinner.getModel()).getDate();
        if (date != null)
            endDate = date;

    }//GEN-LAST:event_endSpinnerStateChanged

    private void searchByNumberCheckBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_searchByNumberCheckBoxItemStateChanged
    {//GEN-HEADEREND:event_searchByNumberCheckBoxItemStateChanged
        boolean enable = evt.getStateChange() == ItemEvent.SELECTED;
        searchByNumberTextField.setEnabled(enable);
        startDateLabel.setEnabled(!enable);
        startSpinner.setEnabled(!enable);
        endDateLabel.setEnabled(!enable);
        endSpinner.setEnabled(!enable);
        filter1CheckBox.setEnabled(!enable);
        filter2CheckBox.setEnabled(!enable);
        filter3CheckBox.setEnabled(!enable);
        filter4CheckBox.setEnabled(!enable);
        filter5CheckBox.setEnabled(!enable);
        filter6CheckBox.setEnabled(!enable);
        filter7CheckBox.setEnabled(!enable);
        fromDistanceLabel.setEnabled(!enable);
        fromDistanceTextField.setEnabled(!enable);
        toDistanceLabel.setEnabled(!enable);
        toDistanceTextField.setEnabled(!enable);
        endDistanceLabel.setEnabled(!enable);
        fromResolutionLabel.setEnabled(!enable);
        fromResolutionTextField.setEnabled(!enable);
        toResolutionLabel.setEnabled(!enable);
        toResolutionTextField.setEnabled(!enable);
        endResolutionLabel.setEnabled(!enable);
        fromIncidenceLabel.setEnabled(!enable);
        fromIncidenceTextField.setEnabled(!enable);
        toIncidenceLabel.setEnabled(!enable);
        toIncidenceTextField.setEnabled(!enable);
        endIncidenceLabel.setEnabled(!enable);
        fromEmissionLabel.setEnabled(!enable);
        fromEmissionTextField.setEnabled(!enable);
        toEmissionLabel.setEnabled(!enable);
        toEmissionTextField.setEnabled(!enable);
        endEmissionLabel.setEnabled(!enable);
        fromPhaseLabel.setEnabled(!enable);
        fromPhaseTextField.setEnabled(!enable);
        toPhaseLabel.setEnabled(!enable);
        toPhaseTextField.setEnabled(!enable);
        endPhaseLabel.setEnabled(!enable);
    }//GEN-LAST:event_searchByNumberCheckBoxItemStateChanged

    private void selectRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectRegionButtonActionPerformed
    {//GEN-HEADEREND:event_selectRegionButtonActionPerformed
        if (selectRegionButton.isSelected())
            pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
        else
            pickManager.setPickMode(PickMode.DEFAULT);
    }//GEN-LAST:event_selectRegionButtonActionPerformed

    private void clearRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearRegionButtonActionPerformed
    {//GEN-HEADEREND:event_clearRegionButtonActionPerformed
        RegularPolygonModel selectionModel = (RegularPolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        selectionModel.removeAllStructures();
    }//GEN-LAST:event_clearRegionButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevButtonActionPerformed
    {//GEN-HEADEREND:event_prevButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
                resultIntervalCurrentlyShown.prevBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
                showAmicaBoundaries(resultIntervalCurrentlyShown);
            }
        }

    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextButtonActionPerformed
    {//GEN-HEADEREND:event_nextButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < resultList.getModel().getSize())
            {
                resultIntervalCurrentlyShown.nextBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
                showAmicaBoundaries(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, (Integer)numberOfBoundariesComboBox.getSelectedItem());
            showAmicaBoundaries(resultIntervalCurrentlyShown);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void redButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_redButtonActionPerformed
    {//GEN-HEADEREND:event_redButtonActionPerformed
        int index = resultList.getSelectedIndex();
        if (index >= 0)
        {
            String image = amicaRawResults.get(index);
            String name = image.substring(23, 32);
            image = image.substring(0,image.length()-4);
            redLabel.setText(name);
            selectedRedKey = new ImageKey(image, sourceOfLastQuery);
        }
    }//GEN-LAST:event_redButtonActionPerformed

    private void greenButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_greenButtonActionPerformed
    {//GEN-HEADEREND:event_greenButtonActionPerformed
        int index = resultList.getSelectedIndex();
        if (index >= 0)
        {
            String image = amicaRawResults.get(index);
            String name = image.substring(23, 32);
            image = image.substring(0,image.length()-4);
            greenLabel.setText(name);
            selectedGreenKey = new ImageKey(image, sourceOfLastQuery);
        }
    }//GEN-LAST:event_greenButtonActionPerformed

    private void blueButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_blueButtonActionPerformed
    {//GEN-HEADEREND:event_blueButtonActionPerformed
        int index = resultList.getSelectedIndex();
        if (index >= 0)
        {
            String image = amicaRawResults.get(index);
            String name = image.substring(23, 32);
            image = image.substring(0,image.length()-4);
            blueLabel.setText(name);
            selectedBlueKey = new ImageKey(image, sourceOfLastQuery);
        }
    }//GEN-LAST:event_blueButtonActionPerformed

    private void generateColorImageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateColorImageButtonActionPerformed
    {//GEN-HEADEREND:event_generateColorImageButtonActionPerformed
        showColorImage(evt);
    }//GEN-LAST:event_generateColorImageButtonActionPerformed

    private void removeColorImageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeColorImageButtonActionPerformed
    {//GEN-HEADEREND:event_removeColorImageButtonActionPerformed
        int index = colorImagesDisplayedList.getSelectedIndex();
        if (index >= 0)
        {
            ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).remove(index);
            AmicaColorImageCollection model = (AmicaColorImageCollection)modelManager.getModel(ModelNames.AMICA_COLOR_IMAGES);
            model.removeImage(colorKey);

            // Select the element in its place (unless it's the last one in which case
            // select the previous one)
            if (index >= colorImagesDisplayedList.getModel().getSize())
                --index;
            if (index >= 0)
                colorImagesDisplayedList.setSelectionInterval(index, index);
        }
    }//GEN-LAST:event_removeColorImageButtonActionPerformed

    private void colorImagesDisplayedListMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_colorImagesDisplayedListMousePressed
    {//GEN-HEADEREND:event_colorImagesDisplayedListMousePressed
        colorImagesDisplayedListMaybeShowPopup(evt);
    }//GEN-LAST:event_colorImagesDisplayedListMousePressed

    private void colorImagesDisplayedListMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_colorImagesDisplayedListMouseReleased
    {//GEN-HEADEREND:event_colorImagesDisplayedListMouseReleased
        colorImagesDisplayedListMaybeShowPopup(evt);
    }//GEN-LAST:event_colorImagesDisplayedListMouseReleased

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllButtonActionPerformed
        AmicaBoundaryCollection model = (AmicaBoundaryCollection)modelManager.getModel(ModelNames.AMICA_BOUNDARY);
        model.removeAllBoundaries();
        resultIntervalCurrentlyShown = null;
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void removeAllImagesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllImagesButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllImagesButtonActionPerformed
        AmicaImageCollection model = (AmicaImageCollection)modelManager.getModel(ModelNames.AMICA_IMAGES);
        model.removeAllImages();
    }//GEN-LAST:event_removeAllImagesButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_submitButtonActionPerformed
    {//GEN-HEADEREND:event_submitButtonActionPerformed
        try
        {
            selectRegionButton.setSelected(false);
            pickManager.setPickMode(PickMode.DEFAULT);

            ArrayList<Integer> filtersChecked = new ArrayList<Integer>();

            if (filter1CheckBox.isSelected())
                filtersChecked.add(1);
            if (filter2CheckBox.isSelected())
                filtersChecked.add(2);
            if (filter3CheckBox.isSelected())
                filtersChecked.add(3);
            if (filter4CheckBox.isSelected())
                filtersChecked.add(4);
            if (filter5CheckBox.isSelected())
                filtersChecked.add(5);
            if (filter6CheckBox.isSelected())
                filtersChecked.add(6);
            if (filter7CheckBox.isSelected())
                filtersChecked.add(7);

            String searchField = null;
            if (searchByNumberCheckBox.isSelected())
                searchField = searchByNumberTextField.getText();

            GregorianCalendar startDateGreg = new GregorianCalendar();
            GregorianCalendar endDateGreg = new GregorianCalendar();
            startDateGreg.setTime(startDate);
            endDateGreg.setTime(endDate);
            DateTime startDateJoda = new DateTime(
                    startDateGreg.get(GregorianCalendar.YEAR),
                    startDateGreg.get(GregorianCalendar.MONTH)+1,
                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    startDateGreg.get(GregorianCalendar.MINUTE),
                    startDateGreg.get(GregorianCalendar.SECOND),
                    startDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);
            DateTime endDateJoda = new DateTime(
                    endDateGreg.get(GregorianCalendar.YEAR),
                    endDateGreg.get(GregorianCalendar.MONTH)+1,
                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    endDateGreg.get(GregorianCalendar.MINUTE),
                    endDateGreg.get(GregorianCalendar.SECOND),
                    endDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);

            TreeSet<Integer> cubeList = null;
            RegularPolygonModel selectionModel = (RegularPolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
            SmallBodyModel itokawaModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
            if (selectionModel.getNumberOfStructures() > 0)
            {
                RegularPolygonModel.RegularPolygon region = (RegularPolygonModel.RegularPolygon)selectionModel.getStructure(0);

                // Always use the lowest resolution model for getting the intersection cubes list.
                // Therefore, if the selection region was created using a higher resolution model,
                // we need to recompute the selection region using the low res model.
                if (itokawaModel.getModelResolution() > 0)
                {
                    vtkPolyData interiorPoly = new vtkPolyData();
                    itokawaModel.drawPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
                    cubeList = itokawaModel.getIntersectingCubes(interiorPoly);
                }
                else
                {
                    cubeList = itokawaModel.getIntersectingCubes(region.interiorPolyData);
                }
            }

            Image.ImageSource amicaSource = Image.ImageSource.GASKELL;
            System.out.println(amicaSource.toString());
            ArrayList<String> results = ItokawaQuery.getInstance().runQuery(
                    ItokawaQuery.Datatype.AMICA,
                    startDateJoda,
                    endDateJoda,
                    filtersChecked,
                    Double.parseDouble(fromDistanceTextField.getText()),
                    Double.parseDouble(toDistanceTextField.getText()),
                    Double.parseDouble(fromResolutionTextField.getText()),
                    Double.parseDouble(toResolutionTextField.getText()),
                    searchField,
                    null,
                    Double.parseDouble(fromIncidenceTextField.getText()),
                    Double.parseDouble(toIncidenceTextField.getText()),
                    Double.parseDouble(fromEmissionTextField.getText()),
                    Double.parseDouble(toEmissionTextField.getText()),
                    Double.parseDouble(fromPhaseTextField.getText()),
                    Double.parseDouble(toPhaseTextField.getText()),
                    cubeList,
                    amicaSource,
                    0);

            sourceOfLastQuery = Image.ImageSource.GASKELL;

            setAmicaResults(results);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    private void resultListMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_resultListMousePressed
    {//GEN-HEADEREND:event_resultListMousePressed
        resultsListMaybeShowPopup(evt);
    }//GEN-LAST:event_resultListMousePressed

    private void resultListMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_resultListMouseReleased
    {//GEN-HEADEREND:event_resultListMouseReleased
        resultsListMaybeShowPopup(evt);
    }//GEN-LAST:event_resultListMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton blueButton;
    private javax.swing.JLabel blueLabel;
    private javax.swing.JButton clearRegionButton;
    private javax.swing.JList colorImagesDisplayedList;
    private javax.swing.JLabel endDateLabel;
    private javax.swing.JLabel endDistanceLabel;
    private javax.swing.JLabel endEmissionLabel;
    private javax.swing.JLabel endIncidenceLabel;
    private javax.swing.JLabel endPhaseLabel;
    private javax.swing.JLabel endResolutionLabel;
    private javax.swing.JSpinner endSpinner;
    private javax.swing.JCheckBox filter1CheckBox;
    private javax.swing.JCheckBox filter2CheckBox;
    private javax.swing.JCheckBox filter3CheckBox;
    private javax.swing.JCheckBox filter4CheckBox;
    private javax.swing.JCheckBox filter5CheckBox;
    private javax.swing.JCheckBox filter6CheckBox;
    private javax.swing.JCheckBox filter7CheckBox;
    private javax.swing.JLabel fromDistanceLabel;
    private javax.swing.JFormattedTextField fromDistanceTextField;
    private javax.swing.JLabel fromEmissionLabel;
    private javax.swing.JFormattedTextField fromEmissionTextField;
    private javax.swing.JLabel fromIncidenceLabel;
    private javax.swing.JFormattedTextField fromIncidenceTextField;
    private javax.swing.JLabel fromPhaseLabel;
    private javax.swing.JFormattedTextField fromPhaseTextField;
    private javax.swing.JLabel fromResolutionLabel;
    private javax.swing.JFormattedTextField fromResolutionTextField;
    private javax.swing.JButton generateColorImageButton;
    private javax.swing.JButton greenButton;
    private javax.swing.JLabel greenLabel;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton nextButton;
    private javax.swing.JComboBox numberOfBoundariesComboBox;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton redButton;
    private javax.swing.JLabel redLabel;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeAllImagesButton;
    private javax.swing.JButton removeColorImageButton;
    private javax.swing.JList resultList;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JCheckBox searchByNumberCheckBox;
    private javax.swing.JFormattedTextField searchByNumberTextField;
    private javax.swing.JToggleButton selectRegionButton;
    private javax.swing.JLabel startDateLabel;
    private javax.swing.JSpinner startSpinner;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel toDistanceLabel;
    private javax.swing.JFormattedTextField toDistanceTextField;
    private javax.swing.JLabel toEmissionLabel;
    private javax.swing.JFormattedTextField toEmissionTextField;
    private javax.swing.JLabel toIncidenceLabel;
    private javax.swing.JFormattedTextField toIncidenceTextField;
    private javax.swing.JLabel toPhaseLabel;
    private javax.swing.JFormattedTextField toPhaseTextField;
    private javax.swing.JLabel toResolutionLabel;
    private javax.swing.JFormattedTextField toResolutionTextField;
    // End of variables declaration//GEN-END:variables
}
