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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;

import nom.tam.fits.FitsException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkActor;
import vtk.vtkPolyData;

import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.ColorImage.ColorImageKey;
import edu.jhuapl.near.model.ColorImage.NoOverlapException;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.util.IdPair;
import edu.jhuapl.near.util.Properties;


public class ImagingSearchPanel extends javax.swing.JPanel implements PropertyChangeListener
{
    private SmallBodyConfig smallBodyConfig;
    private final ModelManager modelManager;
    private final PickManager pickManager;
    private java.util.Date startDate = null;
    private java.util.Date endDate = null;
    private IdPair resultIntervalCurrentlyShown = null;
    private ImageKey selectedRedKey;
    private ImageKey selectedGreenKey;
    private ImageKey selectedBlueKey;
    private JCheckBox[] filterCheckBoxes;
    private JCheckBox[] userDefinedCheckBoxes;

    private boolean isMultispectral = false;

    // The source of the images of the most recently executed query
    private PerspectiveImage.ImageSource sourceOfLastQuery = PerspectiveImage.ImageSource.SPICE;

    private ArrayList<ArrayList<String>> imageRawResults = new ArrayList<ArrayList<String>>();
    private ImagePopupMenu imagePopupMenu;
    private ColorImagePopupMenu colorImagePopupMenu;

    /** Creates new form ImagingSearchPanel */
    public ImagingSearchPanel(SmallBodyConfig smallBodyConfig,
            final ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            final PickManager pickManager,
            Renderer renderer,
            boolean isMultispectral)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.pickManager = pickManager;

        this.isMultispectral = isMultispectral;

        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        initComponents();

        postInitComponents();

        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
        imagePopupMenu = new ImagePopupMenu(images, boundaries, infoPanelManager, renderer, this);

        ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());
        colorImagePopupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager);
    }

    private int getNumberOfFiltersActuallyUsed()
    {
        String[] names = smallBodyConfig.imageSearchFilterNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    private int getNumberOfUserDefinedCheckBoxesActuallyUsed()
    {
        String[] names = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    private ModelNames getImageCollectionModelName()
    {
        return ModelNames.IMAGES;
    }

    private ModelNames getImageBoundaryCollectionModelName()
    {
        return ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES;
    }

    private ModelNames getColorImageCollectionModelName()
    {
        return ModelNames.COLOR_IMAGES;
    }

    private void postInitComponents()
    {
        excludeGaskellCheckBox.setVisible(false);

        ImageSource imageSources[] = isMultispectral ? smallBodyConfig.multispectralImageSearchImageSources : smallBodyConfig.imageSearchImageSources;
        DefaultComboBoxModel sourceComboBoxModel = new DefaultComboBoxModel(imageSources);
        sourceComboBox.setModel(sourceComboBoxModel);

        boolean showSourceLabelAndComboBox = imageSources.length > 1 ? true : false;
        sourceLabel.setVisible(showSourceLabelAndComboBox);
        sourceComboBox.setVisible(showSourceLabelAndComboBox);

        startDate = smallBodyConfig.imageSearchDefaultStartDate;
        ((SpinnerDateModel)startSpinner.getModel()).setValue(startDate);
        endDate = smallBodyConfig.imageSearchDefaultEndDate;
        ((SpinnerDateModel)endSpinner.getModel()).setValue(endDate);



        filterCheckBoxes = new JCheckBox[]{
                filter1CheckBox,
                filter2CheckBox,
                filter3CheckBox,
                filter4CheckBox,
                filter5CheckBox,
                filter6CheckBox,
                filter7CheckBox,
                filter8CheckBox,
                filter9CheckBox,
                filter10CheckBox,
        };

        String[] filterNames = smallBodyConfig.imageSearchFilterNames;
        int numberOfFiltersActuallyUsed = getNumberOfFiltersActuallyUsed();
        for (int i=filterCheckBoxes.length-1; i>=0; --i)
        {
            if (numberOfFiltersActuallyUsed < i+1)
            {
                filterCheckBoxes[i].setSelected(false);
                filterCheckBoxes[i].setVisible(false);
            }
        }

        for (int i=0; i<filterCheckBoxes.length; ++i)
        {
            if (numberOfFiltersActuallyUsed > i)
                filterCheckBoxes[i].setText(filterNames[i]);
        }



        userDefinedCheckBoxes = new JCheckBox[]{
                userDefined1CheckBox,
                userDefined2CheckBox,
                userDefined3CheckBox,
                userDefined4CheckBox,
                userDefined5CheckBox,
                userDefined6CheckBox,
                userDefined7CheckBox,
                userDefined8CheckBox
        };

        String[] userDefinedNames = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
        int numberOfUserDefinedCheckBoxesActuallyUsed = getNumberOfUserDefinedCheckBoxesActuallyUsed();

        for (int i=userDefinedCheckBoxes.length-1; i>=0; --i)
        {
            if (numberOfUserDefinedCheckBoxesActuallyUsed < i+1)
            {
                userDefinedCheckBoxes[i].setSelected(false);
                userDefinedCheckBoxes[i].setVisible(false);
            }
        }

        for (int i=0; i<userDefinedCheckBoxes.length; ++i)
        {
            if (numberOfUserDefinedCheckBoxesActuallyUsed > i)
                userDefinedCheckBoxes[i].setText(userDefinedNames[i]);
        }



        toDistanceTextField.setValue(smallBodyConfig.imageSearchDefaultMaxSpacecraftDistance);
        toResolutionTextField.setValue(smallBodyConfig.imageSearchDefaultMaxResolution);

        colorImagesDisplayedList.setModel(new DefaultListModel());
    }

    private void resultsListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = resultList.locationToIndex(e.getPoint());

            if (index >= 0 && resultList.getCellBounds(index, index).contains(e.getPoint()))
            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!resultList.isSelectedIndex(index))
                {
                    resultList.clearSelection();
                    resultList.setSelectedIndex(index);
                }

                int[] selectedIndices = resultList.getSelectedIndices();
                ArrayList<ImageKey> imageKeys = new ArrayList<ImageKey>();
                for (int selectedIndex : selectedIndices)
                {
                    String name = imageRawResults.get(selectedIndex).get(0);
                    imageKeys.add(new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery));
                }
                imagePopupMenu.setCurrentImages(imageKeys);
                imagePopupMenu.show(e.getComponent(), e.getX(), e.getY());
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
                colorImagePopupMenu.setCurrentImage(colorKey);
                colorImagePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void setImageResults(ArrayList<ArrayList<String>> results)
    {
        resultsLabel.setText(results.size() + " images matched");
        imageRawResults = results;

        String[] formattedResults = new String[results.size()];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // add the results to the list
        int i=0;
        for (ArrayList<String> str : results)
        {
            Date dt = new Date(Long.parseLong(str.get(1)));
            formattedResults[i] = new String(
                    (i+1) + ": " +
                    str.get(0).substring(str.get(0).lastIndexOf("/") + 1) +
                    " " + sdf.format(dt)
                    );

            ++i;
        }

        resultList.setListData(formattedResults);

        // Show the first set of boundaries
        this.resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
        this.showImageBoundaries(resultIntervalCurrentlyShown);
    }


    private void showImageBoundaries(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
        model.removeAllBoundaries();

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= imageRawResults.size())
                break;

            try
            {
                String currentImage = imageRawResults.get(i).get(0);
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
        ColorImageCollection model = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());

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
            if (model instanceof ImageCollection || model instanceof PerspectiveImageBoundaryCollection)
            {
                String name = null;

                if (model instanceof ImageCollection)
                    name = ((ImageCollection)model).getImageName((vtkActor)e.getPickedProp());
                else if (model instanceof PerspectiveImageBoundaryCollection)
                    name = ((PerspectiveImageBoundaryCollection)model).getBoundaryName((vtkActor)e.getPickedProp());

                int idx = -1;
                int size = imageRawResults.size();
                for (int i=0; i<size; ++i)
                {
                    // Ignore extension (The name returned from getImageName or getBoundary
                    // is the same as the first element of each list with the imageRawResults
                    // but without the extension).
                    String imagePath = imageRawResults.get(i).get(0);
                    imagePath = imagePath.substring(0, imagePath.lastIndexOf("."));
                    if (name.equals(imagePath))
                    {
                        idx = i;
                        break;
                    }
                }

                if (idx >= 0)
                {
                    resultList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = resultList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        resultList.scrollRectToVisible(cellBounds);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        startDateLabel = new javax.swing.JLabel();
        startSpinner = new javax.swing.JSpinner();
        endDateLabel = new javax.swing.JLabel();
        endSpinner = new javax.swing.JSpinner();
        sourceLabel = new javax.swing.JLabel();
        sourceComboBox = new javax.swing.JComboBox();
        excludeGaskellCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        filter1CheckBox = new javax.swing.JCheckBox();
        filter2CheckBox = new javax.swing.JCheckBox();
        filter3CheckBox = new javax.swing.JCheckBox();
        filter4CheckBox = new javax.swing.JCheckBox();
        filter5CheckBox = new javax.swing.JCheckBox();
        filter6CheckBox = new javax.swing.JCheckBox();
        filter7CheckBox = new javax.swing.JCheckBox();
        filter8CheckBox = new javax.swing.JCheckBox();
        filter9CheckBox = new javax.swing.JCheckBox();
        filter10CheckBox = new javax.swing.JCheckBox();
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
        searchByFilenameCheckBox = new javax.swing.JCheckBox();
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
        jPanel11 = new javax.swing.JPanel();
        hasLimbComboBox = new javax.swing.JComboBox();
        hasLimbLabel = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        userDefined1CheckBox = new javax.swing.JCheckBox();
        userDefined2CheckBox = new javax.swing.JCheckBox();
        userDefined3CheckBox = new javax.swing.JCheckBox();
        userDefined4CheckBox = new javax.swing.JCheckBox();
        userDefined5CheckBox = new javax.swing.JCheckBox();
        userDefined6CheckBox = new javax.swing.JCheckBox();
        userDefined7CheckBox = new javax.swing.JCheckBox();
        userDefined8CheckBox = new javax.swing.JCheckBox();

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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(startDateLabel, gridBagConstraints);

        startSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        jPanel1.add(startSpinner, gridBagConstraints);

        endDateLabel.setText("End Date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(endDateLabel, gridBagConstraints);

        endSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1132462800000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setPreferredSize(new java.awt.Dimension(162, 22));
        endSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanel1.add(endSpinner, gridBagConstraints);

        sourceLabel.setText("Pointing:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(sourceLabel, gridBagConstraints);

        sourceComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(sourceComboBox, gridBagConstraints);

        excludeGaskellCheckBox.setText("Exclude Gaskell derived");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel1.add(excludeGaskellCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel8.add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        filter1CheckBox.setSelected(true);
        filter1CheckBox.setText("Filter ul (381 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 0);
        jPanel2.add(filter1CheckBox, gridBagConstraints);

        filter2CheckBox.setSelected(true);
        filter2CheckBox.setText("Filter b (429 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        jPanel2.add(filter2CheckBox, gridBagConstraints);

        filter3CheckBox.setSelected(true);
        filter3CheckBox.setText("Filter v (553 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        jPanel2.add(filter3CheckBox, gridBagConstraints);

        filter4CheckBox.setSelected(true);
        filter4CheckBox.setText("Filter w (700 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        jPanel2.add(filter4CheckBox, gridBagConstraints);

        filter5CheckBox.setSelected(true);
        filter5CheckBox.setText("Filter x (861 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 8, 0, 0);
        jPanel2.add(filter5CheckBox, gridBagConstraints);

        filter6CheckBox.setSelected(true);
        filter6CheckBox.setText("Filter p (960 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        jPanel2.add(filter6CheckBox, gridBagConstraints);

        filter7CheckBox.setSelected(true);
        filter7CheckBox.setText("Filter zs (1008 nm)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        jPanel2.add(filter7CheckBox, gridBagConstraints);

        filter8CheckBox.setSelected(true);
        filter8CheckBox.setText("Filter 8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        jPanel2.add(filter8CheckBox, gridBagConstraints);

        filter9CheckBox.setSelected(true);
        filter9CheckBox.setText("Filter 9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 6, 0);
        jPanel2.add(filter9CheckBox, gridBagConstraints);

        filter10CheckBox.setSelected(true);
        filter10CheckBox.setText("Filter 10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 6, 6);
        jPanel2.add(filter10CheckBox, gridBagConstraints);

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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromDistanceLabel, gridBagConstraints);

        fromDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromDistanceTextField, gridBagConstraints);

        toDistanceLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toDistanceLabel, gridBagConstraints);

        toDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toDistanceTextField.setText("26");
        toDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toDistanceTextField, gridBagConstraints);

        endDistanceLabel.setText("km");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endDistanceLabel, gridBagConstraints);

        fromResolutionLabel.setText("Resolution from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromResolutionLabel, gridBagConstraints);

        fromResolutionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromResolutionTextField.setText("0");
        fromResolutionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromResolutionTextField, gridBagConstraints);

        toResolutionLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toResolutionLabel, gridBagConstraints);

        toResolutionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toResolutionTextField.setText("3");
        toResolutionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toResolutionTextField, gridBagConstraints);

        endResolutionLabel.setText("mpp");
        endResolutionLabel.setToolTipText("meters per pixel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endResolutionLabel, gridBagConstraints);

        fromIncidenceLabel.setText("Incidence from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromIncidenceLabel, gridBagConstraints);

        fromIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromIncidenceTextField, gridBagConstraints);

        toIncidenceLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toIncidenceLabel, gridBagConstraints);

        toIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toIncidenceTextField, gridBagConstraints);

        endIncidenceLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endIncidenceLabel, gridBagConstraints);

        fromEmissionLabel.setText("Emission from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromEmissionLabel, gridBagConstraints);

        fromEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromEmissionTextField, gridBagConstraints);

        toEmissionLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toEmissionLabel, gridBagConstraints);

        toEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
        toEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toEmissionTextField, gridBagConstraints);

        endEmissionLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endEmissionLabel, gridBagConstraints);

        fromPhaseLabel.setText("Phase from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromPhaseLabel, gridBagConstraints);

        fromPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromPhaseTextField, gridBagConstraints);

        toPhaseLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toPhaseLabel, gridBagConstraints);

        toPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
        toPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toPhaseTextField, gridBagConstraints);

        endPhaseLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endPhaseLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel8.add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        searchByFilenameCheckBox.setText("Search by Filename");
        searchByFilenameCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchByFilenameCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel4.add(searchByFilenameCheckBox, gridBagConstraints);

        searchByNumberTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 122;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel4.add(searchByNumberTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 0, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        jPanel5.add(selectRegionButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanel8.add(jPanel5, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        resultsLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(resultsLabel, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 200));

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
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel8.add(jPanel6, gridBagConstraints);

        jPanel7.setLayout(new java.awt.GridBagLayout());

        jLabel6.setText("Number Boundaries:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(jLabel6, gridBagConstraints);

        numberOfBoundariesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220", "230", "240", "250", " " }));
        numberOfBoundariesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberOfBoundariesComboBoxActionPerformed(evt);
            }
        });
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
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel8.add(jPanel7, gridBagConstraints);

        removeAllButton.setText("Remove All Boundaries");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel8.add(removeAllButton, gridBagConstraints);

        removeAllImagesButton.setText("Remove All Images");
        removeAllImagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllImagesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
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
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel8.add(jPanel9, gridBagConstraints);

        jPanel10.setLayout(new java.awt.GridBagLayout());

        redButton.setBackground(new java.awt.Color(255, 0, 0));
        redButton.setText("Red");
        redButton.setToolTipText("Select an image from the list above and then press this button");
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
        greenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel10.add(greenButton, gridBagConstraints);

        blueButton.setBackground(new java.awt.Color(0, 0, 255));
        blueButton.setText("Blue");
        blueButton.setToolTipText("Select an image from the list above and then press this button");
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
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel8.add(jPanel10, gridBagConstraints);

        generateColorImageButton.setText("Generate Color Image");
        generateColorImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateColorImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel8.add(generateColorImageButton, gridBagConstraints);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(300, 100));

        colorImagesDisplayedList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(jScrollPane3, gridBagConstraints);

        removeColorImageButton.setText("Remove");
        removeColorImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColorImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel8.add(removeColorImageButton, gridBagConstraints);

        jPanel11.setLayout(new java.awt.GridBagLayout());

        hasLimbComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "with or without", "with only", "without only" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 2, 0);
        jPanel11.add(hasLimbComboBox, gridBagConstraints);

        hasLimbLabel.setText("Limb:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel11.add(hasLimbLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jPanel8.add(jPanel11, gridBagConstraints);

        jPanel12.setLayout(new java.awt.GridBagLayout());

        userDefined1CheckBox.setSelected(true);
        userDefined1CheckBox.setText("userDefined1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel12.add(userDefined1CheckBox, gridBagConstraints);

        userDefined2CheckBox.setSelected(true);
        userDefined2CheckBox.setText("userDefined2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel12.add(userDefined2CheckBox, gridBagConstraints);

        userDefined3CheckBox.setSelected(true);
        userDefined3CheckBox.setText("userDefined3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel12.add(userDefined3CheckBox, gridBagConstraints);

        userDefined4CheckBox.setSelected(true);
        userDefined4CheckBox.setText("userDefined4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel12.add(userDefined4CheckBox, gridBagConstraints);

        userDefined5CheckBox.setSelected(true);
        userDefined5CheckBox.setText("userDefined5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel12.add(userDefined5CheckBox, gridBagConstraints);

        userDefined6CheckBox.setSelected(true);
        userDefined6CheckBox.setText("userDefined6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel12.add(userDefined6CheckBox, gridBagConstraints);

        userDefined7CheckBox.setSelected(true);
        userDefined7CheckBox.setText("userDefined7");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel12.add(userDefined7CheckBox, gridBagConstraints);

        userDefined8CheckBox.setSelected(true);
        userDefined8CheckBox.setText("userDefined8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel12.add(userDefined8CheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel8.add(jPanel12, gridBagConstraints);

        jScrollPane2.setViewportView(jPanel8);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

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

    private void searchByFilenameCheckBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_searchByFilenameCheckBoxItemStateChanged
    {//GEN-HEADEREND:event_searchByFilenameCheckBoxItemStateChanged
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
        filter8CheckBox.setEnabled(!enable);
        filter9CheckBox.setEnabled(!enable);
        filter10CheckBox.setEnabled(!enable);
        userDefined1CheckBox.setEnabled(!enable);
        userDefined2CheckBox.setEnabled(!enable);
        userDefined3CheckBox.setEnabled(!enable);
        userDefined4CheckBox.setEnabled(!enable);
        userDefined5CheckBox.setEnabled(!enable);
        userDefined6CheckBox.setEnabled(!enable);
        userDefined7CheckBox.setEnabled(!enable);
        userDefined8CheckBox.setEnabled(!enable);
        hasLimbLabel.setEnabled(!enable);
        hasLimbComboBox.setEnabled(!enable);
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
    }//GEN-LAST:event_searchByFilenameCheckBoxItemStateChanged

    private void selectRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectRegionButtonActionPerformed
    {//GEN-HEADEREND:event_selectRegionButtonActionPerformed
        if (selectRegionButton.isSelected())
            pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
        else
            pickManager.setPickMode(PickMode.DEFAULT);
    }//GEN-LAST:event_selectRegionButtonActionPerformed

    private void clearRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearRegionButtonActionPerformed
    {//GEN-HEADEREND:event_clearRegionButtonActionPerformed
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        selectionModel.removeAllStructures();
    }//GEN-LAST:event_clearRegionButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevButtonActionPerformed
    {//GEN-HEADEREND:event_prevButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
                resultIntervalCurrentlyShown.prevBlock(Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
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
                resultIntervalCurrentlyShown.nextBlock(Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
            showImageBoundaries(resultIntervalCurrentlyShown);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void redButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_redButtonActionPerformed
    {//GEN-HEADEREND:event_redButtonActionPerformed
        int index = resultList.getSelectedIndex();
        if (index >= 0)
        {
            String image = imageRawResults.get(index).get(0);
            String name = new File(image).getName();
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
            String image = imageRawResults.get(index).get(0);
            String name = new File(image).getName();
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
            String image = imageRawResults.get(index).get(0);
            String name = new File(image).getName();
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
            ColorImageCollection model = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());
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
        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
        model.removeAllBoundaries();
        resultIntervalCurrentlyShown = null;
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void removeAllImagesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllImagesButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllImagesButtonActionPerformed
        ImageCollection model = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        model.removeImages(ImageSource.GASKELL);
        model.removeImages(ImageSource.SPICE);
        model.removeImages(ImageSource.CORRECTED);
    }//GEN-LAST:event_removeAllImagesButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_submitButtonActionPerformed
    {//GEN-HEADEREND:event_submitButtonActionPerformed
        try
        {
            selectRegionButton.setSelected(false);
            pickManager.setPickMode(PickMode.DEFAULT);

            ArrayList<Boolean> filtersChecked = new ArrayList<Boolean>();
            int numberOfFilters = getNumberOfFiltersActuallyUsed();
            for (int i=0; i<numberOfFilters; ++i)
            {
                filtersChecked.add(filterCheckBoxes[i].isSelected());
            }

            String searchField = null;
            if (searchByFilenameCheckBox.isSelected())
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
            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
            SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
            if (selectionModel.getNumberOfStructures() > 0)
            {
                AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);

                // Always use the lowest resolution model for getting the intersection cubes list.
                // Therefore, if the selection region was created using a higher resolution model,
                // we need to recompute the selection region using the low res model.
                if (smallBodyModel.getModelResolution() > 0)
                {
                    vtkPolyData interiorPoly = new vtkPolyData();
                    smallBodyModel.drawRegularPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
                    cubeList = smallBodyModel.getIntersectingCubes(interiorPoly);
                }
                else
                {
                    cubeList = smallBodyModel.getIntersectingCubes(region.interiorPolyData);
                }
            }

            ImageSource imageSource = ImageSource.valueOf(((Enum)sourceComboBox.getSelectedItem()).name());

            ArrayList<Boolean> userDefinedChecked = new ArrayList<Boolean>();
            int numberOfUserDefined = getNumberOfUserDefinedCheckBoxesActuallyUsed();
            for (int i=0; i<numberOfUserDefined; ++i)
            {
                userDefinedChecked.add(userDefinedCheckBoxes[i].isSelected());
            }

            ArrayList<ArrayList<String>> results = null;

            if (isMultispectral)
            {
                results = smallBodyConfig.multispectralImageSearchQuery.runQuery(
                    "",
                    startDateJoda,
                    endDateJoda,
                    filtersChecked,
                    userDefinedChecked,
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
                    imageSource,
                    hasLimbComboBox.getSelectedIndex());
            }
            else
            {
                results = smallBodyConfig.imageSearchQuery.runQuery(
                    "",
                    startDateJoda,
                    endDateJoda,
                    filtersChecked,
                    userDefinedChecked,
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
                    imageSource,
                    hasLimbComboBox.getSelectedIndex());
            }

            // If SPICE Derived (exclude Gaskell) or Gaskell Derived (exlude SPICE) is selected,
            // then remove from the list images which are contained in the other list by doing
            // an additional search.
            if (imageSource == ImageSource.SPICE && excludeGaskellCheckBox.isSelected())
            {
                ArrayList<ArrayList<String>> resultsOtherSource = smallBodyConfig.imageSearchQuery.runQuery(
                        "",
                        startDateJoda,
                        endDateJoda,
                        filtersChecked,
                        userDefinedChecked,
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
                        imageSource == PerspectiveImage.ImageSource.SPICE ? PerspectiveImage.ImageSource.GASKELL : PerspectiveImage.ImageSource.SPICE,
                        hasLimbComboBox.getSelectedIndex());

                int numOtherResults = resultsOtherSource.size();
                for (int i=0; i<numOtherResults; ++i)
                {
                    String imageName = resultsOtherSource.get(i).get(0);
                    int numResults = results.size();
                    for (int j=0; j<numResults; ++j)
                    {
                        if (results.get(j).get(0).startsWith(imageName))
                        {
                            results.remove(j);
                            break;
                        }
                    }
                }
            }

            sourceOfLastQuery = imageSource;

            setImageResults(results);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

    private void sourceComboBoxItemStateChanged(java.awt.event.ItemEvent evt)
    {//GEN-FIRST:event_sourceComboBoxItemStateChanged
        ImageSource imageSource = ImageSource.valueOf(((Enum)sourceComboBox.getSelectedItem()).name());
        excludeGaskellCheckBox.setVisible(imageSource == ImageSource.SPICE);
    }//GEN-LAST:event_sourceComboBoxItemStateChanged

    private void numberOfBoundariesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberOfBoundariesComboBoxActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only update if there's been a change in what is selected
            int newMaxId = resultIntervalCurrentlyShown.id1 + Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem());
            if (newMaxId != resultIntervalCurrentlyShown.id2)
            {
                resultIntervalCurrentlyShown.id2 = newMaxId;
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
    }//GEN-LAST:event_numberOfBoundariesComboBoxActionPerformed

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
    private javax.swing.JCheckBox excludeGaskellCheckBox;
    private javax.swing.JCheckBox filter10CheckBox;
    private javax.swing.JCheckBox filter1CheckBox;
    private javax.swing.JCheckBox filter2CheckBox;
    private javax.swing.JCheckBox filter3CheckBox;
    private javax.swing.JCheckBox filter4CheckBox;
    private javax.swing.JCheckBox filter5CheckBox;
    private javax.swing.JCheckBox filter6CheckBox;
    private javax.swing.JCheckBox filter7CheckBox;
    private javax.swing.JCheckBox filter8CheckBox;
    private javax.swing.JCheckBox filter9CheckBox;
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
    private javax.swing.JComboBox hasLimbComboBox;
    private javax.swing.JLabel hasLimbLabel;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
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
    private javax.swing.JCheckBox searchByFilenameCheckBox;
    private javax.swing.JFormattedTextField searchByNumberTextField;
    private javax.swing.JToggleButton selectRegionButton;
    private javax.swing.JComboBox sourceComboBox;
    private javax.swing.JLabel sourceLabel;
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
    private javax.swing.JCheckBox userDefined1CheckBox;
    private javax.swing.JCheckBox userDefined2CheckBox;
    private javax.swing.JCheckBox userDefined3CheckBox;
    private javax.swing.JCheckBox userDefined4CheckBox;
    private javax.swing.JCheckBox userDefined5CheckBox;
    private javax.swing.JCheckBox userDefined6CheckBox;
    private javax.swing.JCheckBox userDefined7CheckBox;
    private javax.swing.JCheckBox userDefined8CheckBox;
    // End of variables declaration//GEN-END:variables
}
