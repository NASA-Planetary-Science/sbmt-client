package edu.jhuapl.sbmt.gui.image.panels;

import java.awt.Cursor;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SpinnerDateModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Ranges;
import com.jidesoft.swing.CheckBoxTree;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.query.database.ImageDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;

public class ImageSearchParametersPanel extends JPanel
{
    private java.util.Date startDate = null;
    private java.util.Date endDate = null;
    protected IdPair resultIntervalCurrentlyShown = null;
    private ImageKey selectedRedKey;
    private ImageKey selectedGreenKey;
    private ImageKey selectedBlueKey;
    private JCheckBox[] filterCheckBoxes;
    private JCheckBox[] userDefinedCheckBoxes;
    protected CheckBoxTree checkBoxTree;
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
    private javax.swing.JPanel userDefinedCheckBoxPanel;
    private javax.swing.JLabel endDateLabel;
    private javax.swing.JLabel endDistanceLabel;
    private javax.swing.JLabel endEmissionLabel;
    private javax.swing.JLabel endIncidenceLabel;
    private javax.swing.JLabel endPhaseLabel;
    private javax.swing.JLabel endResolutionLabel;
    private javax.swing.JSpinner endSpinner;
    private javax.swing.JCheckBox excludeGaskellCheckBox;
    private javax.swing.JCheckBox filter10CheckBox;
    private javax.swing.JCheckBox filter11CheckBox;
    private javax.swing.JCheckBox filter12CheckBox;
    private javax.swing.JCheckBox filter13CheckBox;
    private javax.swing.JCheckBox filter14CheckBox;
    private javax.swing.JCheckBox filter15CheckBox;
    private javax.swing.JCheckBox filter16CheckBox;
    private javax.swing.JCheckBox filter17CheckBox;
    private javax.swing.JCheckBox filter18CheckBox;
    private javax.swing.JCheckBox filter19CheckBox;
    private javax.swing.JCheckBox filter1CheckBox;
    private javax.swing.JCheckBox filter20CheckBox;
    private javax.swing.JCheckBox filter21CheckBox;
    private javax.swing.JCheckBox filter22CheckBox;
    private javax.swing.JCheckBox filter2CheckBox;
    private javax.swing.JCheckBox filter3CheckBox;
    private javax.swing.JCheckBox filter4CheckBox;
    private javax.swing.JCheckBox filter5CheckBox;
    private javax.swing.JCheckBox filter6CheckBox;
    private javax.swing.JCheckBox filter7CheckBox;
    private javax.swing.JCheckBox filter8CheckBox;
    private javax.swing.JCheckBox filter9CheckBox;
    private javax.swing.JPanel filterCheckBoxPanel;
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
    private javax.swing.JComboBox hasLimbComboBox;
    private javax.swing.JLabel hasLimbLabel;
    private javax.swing.JScrollPane hierarchicalSearchScrollPane;

    public ImageSearchParametersPanel()
    {
        // TODO Auto-generated constructor stub
        excludeGaskellCheckBox.setVisible(false);
        enableGallery = instrument.searchQuery.getGalleryPath() != null;
        ImageSource imageSources[] = instrument.searchImageSources;
        DefaultComboBoxModel sourceComboBoxModel = new DefaultComboBoxModel(imageSources);
        sourceComboBox.setModel(sourceComboBoxModel);

        boolean showSourceLabelAndComboBox = true; //imageSources.length > 1 ? true : false;
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
                filter11CheckBox,
                filter12CheckBox,
                filter13CheckBox,
                filter14CheckBox,
                filter15CheckBox,
                filter16CheckBox,
                filter17CheckBox,
                filter18CheckBox,
                filter19CheckBox,
                filter20CheckBox,
                filter21CheckBox,
                filter22CheckBox
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
            {
                if (filterNames[i].startsWith("*"))
                {
                    filterCheckBoxes[i].setText(filterNames[i].substring(1));
                    filterCheckBoxes[i].setSelected(false);
                }
                else
                {
                    filterCheckBoxes[i].setText(filterNames[i]);
                }
            }
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
            {
                if (userDefinedNames[i].startsWith("*"))
                {
                    userDefinedCheckBoxes[i].setText(userDefinedNames[i].substring(1));
                    userDefinedCheckBoxes[i].setSelected(false);
                }
                else
                {
                    userDefinedCheckBoxes[i].setText(userDefinedNames[i]);
                }
            }
        }



        toDistanceTextField.setValue(smallBodyConfig.imageSearchDefaultMaxSpacecraftDistance);
        toResolutionTextField.setValue(smallBodyConfig.imageSearchDefaultMaxResolution);

//        colorImagesDisplayedList.setModel(new DefaultListModel());
//        imageCubesDisplayedList.setModel(new DefaultListModel());

        redComboBox.setVisible(false);
        greenComboBox.setVisible(false);
        blueComboBox.setVisible(false);

        ComboBoxModel redModel = getRedComboBoxModel();
        ComboBoxModel greenModel = getGreenComboBoxModel();
        ComboBoxModel blueModel = getBlueComboBoxModel();
        if (redModel != null && greenModel != null && blueModel != null)
        {
            redComboBox.setModel(redModel);
            greenComboBox.setModel(greenModel);
            blueComboBox.setModel(blueModel);

            redComboBox.setVisible(true);
            greenComboBox.setVisible(true);
            blueComboBox.setVisible(true);

            redButton.setVisible(false);
            greenButton.setVisible(false);
            blueButton.setVisible(false);
        }

        searchByFilenameCheckBox.setText("Search by Filename");
        searchByFilenameCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchByFilenameCheckBoxItemStateChanged(evt);
            }
        });

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

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });

        sourceComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceComboBoxItemStateChanged(evt);
            }
        });
        hasLimbComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "with or without", "with only", "without only" }));

        toPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
        toPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        fromPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        toEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
        toEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        fromEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        toIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        fromIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("180");
        fromIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        endResolutionLabel.setText("mpp");
        endResolutionLabel.setToolTipText("meters per pixel");

        toResolutionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toResolutionTextField.setText("3");
        toResolutionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        fromResolutionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromResolutionTextField.setText("3");
        fromResolutionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        toDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toDistanceTextField.setText("26");
        toDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        fromDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromDistanceTextField.setText("26");
        fromDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
    }

    public ImageSearchParametersPanel(LayoutManager layout)
    {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public ImageSearchParametersPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public ImageSearchParametersPanel(LayoutManager layout,
            boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    private void sourceComboBoxItemStateChanged(java.awt.event.ItemEvent evt)
    {//GEN-FIRST:event_sourceComboBoxItemStateChanged
        ImageSource imageSource = ImageSource.valueOf(((Enum)sourceComboBox.getSelectedItem()).name());
        for (int i=0; i< sourceComboBox.getModel().getSize(); i++)
        {
            ImageSource source = ImageSource.valueOf(((Enum)sourceComboBox.getItemAt(i)).name());
            if (source == ImageSource.GASKELL_UPDATED)
            {
                excludeGaskellCheckBox.setVisible(imageSource == ImageSource.SPICE);
            }
        }
    }//GEN-LAST:event_sourceComboBoxItemStateChanged

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_submitButtonActionPerformed
    {//GEN-HEADEREND:event_submitButtonActionPerformed
        try
        {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            selectRegionButton.setSelected(false);
            pickManager.setPickMode(PickMode.DEFAULT);

            String searchField = null;
            if (searchByFilenameCheckBox.isSelected())
                searchField = searchByNumberTextField.getText().trim();

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

            // Populate camera and filter list differently based on if we are doing sum-of-products or product-of-sums search
            boolean sumOfProductsSearch;
            List<Integer> camerasSelected;
            List<Integer> filtersSelected;
            if(smallBodyConfig.hasHierarchicalImageSearch)
            {
                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
                sumOfProductsSearch = true;

                // Process the user's selections
                smallBodyConfig.hierarchicalImageSearchSpecification.processTreeSelections(
                        checkBoxTree.getCheckBoxTreeSelectionModel().getSelectionPaths());

                // Get the selected (camera,filter) pairs
                camerasSelected = smallBodyConfig.hierarchicalImageSearchSpecification.getSelectedCameras();
                filtersSelected = smallBodyConfig.hierarchicalImageSearchSpecification.getSelectedFilters();
            }
            else
            {
                // Product of sums (legacy) search: (CAMERA 1 OR ... OR CAMERA N) AND (FILTER 1 OR ... FILTER M)
                sumOfProductsSearch = false;

                // Populate list of selected cameras
                camerasSelected = new LinkedList<Integer>();
                int numberOfCameras = getNumberOfUserDefinedCheckBoxesActuallyUsed();
                for (int i=0; i<numberOfCameras; i++)
                {
                    if(userDefinedCheckBoxes[i].isSelected())
                    {
                        camerasSelected.add(i);
                    }
                }

                // Populate list of selected filters
                filtersSelected = new LinkedList<Integer>();
                int numberOfFilters = getNumberOfFiltersActuallyUsed();
                for (int i=0; i<numberOfFilters; i++)
                {
                    if(filterCheckBoxes[i].isSelected())
                    {
                        filtersSelected.add(i);
                    }
                }
            }

            List<List<String>> results = null;
//            System.out.println(
//                    "ImagingSearchPanel: submitButtonActionPerformed: search query type " + instrument.searchQuery.getClass());
            if (instrument.searchQuery instanceof FixedListQuery)
            {
                FixedListQuery query = (FixedListQuery) instrument.searchQuery;
                results = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist", "images", query.getRootPath(), imageSource)).getResultlist();
            }
            else
            {
                // Run queries based on user specifications
                ImageDatabaseSearchMetadata searchMetadata = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                        Ranges.closed(Double.valueOf(fromDistanceTextField.getText()), Double.valueOf(toDistanceTextField.getText())),
                        searchField, null,
                        Ranges.closed(Double.valueOf(fromIncidenceTextField.getText()), Double.valueOf(toIncidenceTextField.getText())),
                        Ranges.closed(Double.valueOf(fromEmissionTextField.getText()), Double.valueOf(toEmissionTextField.getText())),
                        Ranges.closed(Double.valueOf(fromPhaseTextField.getText()), Double.valueOf(toPhaseTextField.getText())),
                        sumOfProductsSearch, camerasSelected, filtersSelected,
                        Ranges.closed(Double.valueOf(fromResolutionTextField.getText()), Double.valueOf(toResolutionTextField.getText())),
                        cubeList, imageSource, hasLimbComboBox.getSelectedIndex());

                results = instrument.searchQuery.runQuery(searchMetadata).getResultlist();
           }

            //ALL OF THE BRANCHES BELOW CALL IDENTICAL CODE!
//            if (instrument.spectralMode == SpectralMode.MULTI)
//            {
//                ImageDatabaseSearchMetadata searchMetadata = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
//                        Ranges.closed(Double.valueOf(fromDistanceTextField.getText()), Double.valueOf(toDistanceTextField.getText())),
//                        searchField, null,
//                        Ranges.closed(Double.valueOf(fromIncidenceTextField.getText()), Double.valueOf(toIncidenceTextField.getText())),
//                        Ranges.closed(Double.valueOf(fromEmissionTextField.getText()), Double.valueOf(toEmissionTextField.getText())),
//                        Ranges.closed(Double.valueOf(fromPhaseTextField.getText()), Double.valueOf(toPhaseTextField.getText())),
//                        sumOfProductsSearch, camerasSelected, filtersSelected,
//                        Ranges.closed(Double.valueOf(fromResolutionTextField.getText()), Double.valueOf(toResolutionTextField.getText())),
//                        cubeList, imageSource, hasLimbComboBox.getSelectedIndex());
//
//                results = instrument.searchQuery.runQuery(searchMetadata).getResultlist();
//
//                results = instrument.searchQuery.runQuery(
//                    "",
//                    startDateJoda,
//                    endDateJoda,
//                    sumOfProductsSearch,
//                    camerasSelected,
//                    filtersSelected,
//                    Double.parseDouble(fromDistanceTextField.getText()),
//                    Double.parseDouble(toDistanceTextField.getText()),
//                    Double.parseDouble(fromResolutionTextField.getText()),
//                    Double.parseDouble(toResolutionTextField.getText()),
//                    searchField,
//                    null,
//                    Double.parseDouble(fromIncidenceTextField.getText()),
//                    Double.parseDouble(toIncidenceTextField.getText()),
//                    Double.parseDouble(fromEmissionTextField.getText()),
//                    Double.parseDouble(toEmissionTextField.getText()),
//                    Double.parseDouble(fromPhaseTextField.getText()),
//                    Double.parseDouble(toPhaseTextField.getText()),
//                    cubeList,
//                    imageSource,
//                    hasLimbComboBox.getSelectedIndex());
//            }
//            else if (instrument.spectralMode == SpectralMode.HYPER)
//            {
//                results = instrument.searchQuery.runQuery(
//                    "",
//                    startDateJoda,
//                    endDateJoda,
//                    sumOfProductsSearch,
//                    camerasSelected,
//                    filtersSelected,
//                    Double.parseDouble(fromDistanceTextField.getText()),
//                    Double.parseDouble(toDistanceTextField.getText()),
//                    Double.parseDouble(fromResolutionTextField.getText()),
//                    Double.parseDouble(toResolutionTextField.getText()),
//                    searchField,
//                    null,
//                    Double.parseDouble(fromIncidenceTextField.getText()),
//                    Double.parseDouble(toIncidenceTextField.getText()),
//                    Double.parseDouble(fromEmissionTextField.getText()),
//                    Double.parseDouble(toEmissionTextField.getText()),
//                    Double.parseDouble(fromPhaseTextField.getText()),
//                    Double.parseDouble(toPhaseTextField.getText()),
//                    cubeList,
//                    imageSource,
//                    hasLimbComboBox.getSelectedIndex());
//            }
//            else
//            {
//                results = instrument.searchQuery.runQuery(
//                    "",
//                    startDateJoda,
//                    endDateJoda,
//                    sumOfProductsSearch,
//                    camerasSelected,
//                    filtersSelected,
//                    Double.parseDouble(fromDistanceTextField.getText()),
//                    Double.parseDouble(toDistanceTextField.getText()),
//                    Double.parseDouble(fromResolutionTextField.getText()),
//                    Double.parseDouble(toResolutionTextField.getText()),
//                    searchField,
//                    null,
//                    Double.parseDouble(fromIncidenceTextField.getText()),
//                    Double.parseDouble(toIncidenceTextField.getText()),
//                    Double.parseDouble(fromEmissionTextField.getText()),
//                    Double.parseDouble(toEmissionTextField.getText()),
//                    Double.parseDouble(fromPhaseTextField.getText()),
//                    Double.parseDouble(toPhaseTextField.getText()),
//                    cubeList,
//                    imageSource,
//                    hasLimbComboBox.getSelectedIndex());
//            }

            // If SPICE Derived (exclude Gaskell) or Gaskell Derived (exlude SPICE) is selected,
            // then remove from the list images which are contained in the other list by doing
            // an additional search.
            if (imageSource == ImageSource.SPICE && excludeGaskellCheckBox.isSelected())
            {
                List<List<String>> resultsOtherSource = null;
                if (instrument.searchQuery instanceof FixedListQuery)
                {
                    FixedListQuery query = (FixedListQuery)instrument.searchQuery;
//                    FileInfo info = FileCache.getFileInfoFromServer(query.getRootPath() + "/" /*+ dataListPrefix + "/"*/ + imageListName);
//                    if (!info.isExistsOnServer().equals(YesOrNo.YES))
//                    {
//                        System.out.println("Could not find " + imageListName + ". Using imagelist.txt instead");
//                        imageListName = "imagelist.txt";
//                    }
                    resultsOtherSource = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist" /*imageListName*/, "images", query.getRootPath(), imageSource)).getResultlist();
                }
                else
                {

                    ImageDatabaseSearchMetadata searchMetadataOther = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                            Ranges.closed(Double.valueOf(fromDistanceTextField.getText()), Double.valueOf(toDistanceTextField.getText())),
                            searchField, null,
                            Ranges.closed(Double.valueOf(fromIncidenceTextField.getText()), Double.valueOf(toIncidenceTextField.getText())),
                            Ranges.closed(Double.valueOf(fromEmissionTextField.getText()), Double.valueOf(toEmissionTextField.getText())),
                            Ranges.closed(Double.valueOf(fromPhaseTextField.getText()), Double.valueOf(toPhaseTextField.getText())),
                            sumOfProductsSearch, camerasSelected, filtersSelected,
                            Ranges.closed(Double.valueOf(fromResolutionTextField.getText()), Double.valueOf(toResolutionTextField.getText())),
                            cubeList, imageSource == ImageSource.SPICE ? ImageSource.GASKELL_UPDATED : ImageSource.SPICE, hasLimbComboBox.getSelectedIndex());

                        resultsOtherSource = instrument.searchQuery.runQuery(searchMetadataOther).getResultlist();

                }


//                List<List<String>> resultsOtherSource = instrument.searchQuery.runQuery(
//                        "",
//                        startDateJoda,
//                        endDateJoda,
//                        sumOfProductsSearch,
//                        camerasSelected,
//                        filtersSelected,
//                        Double.parseDouble(fromDistanceTextField.getText()),
//                        Double.parseDouble(toDistanceTextField.getText()),
//                        Double.parseDouble(fromResolutionTextField.getText()),
//                        Double.parseDouble(toResolutionTextField.getText()),
//                        searchField,
//                        null,
//                        Double.parseDouble(fromIncidenceTextField.getText()),
//                        Double.parseDouble(toIncidenceTextField.getText()),
//                        Double.parseDouble(fromEmissionTextField.getText()),
//                        Double.parseDouble(toEmissionTextField.getText()),
//                        Double.parseDouble(fromPhaseTextField.getText()),
//                        Double.parseDouble(toPhaseTextField.getText()),
//                        cubeList,
//                        imageSource == ImageSource.SPICE ? ImageSource.GASKELL_UPDATED : ImageSource.SPICE,
//                        hasLimbComboBox.getSelectedIndex());

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

            setImageResults(processResults(results));
            setCursor(Cursor.getDefaultCursor());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    protected List<List<String>> processResults(List<List<String>> input)
    {
        return input;
    }

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
        filter11CheckBox.setEnabled(!enable);
        filter12CheckBox.setEnabled(!enable);
        filter13CheckBox.setEnabled(!enable);
        filter14CheckBox.setEnabled(!enable);
        filter15CheckBox.setEnabled(!enable);
        filter16CheckBox.setEnabled(!enable);
        filter17CheckBox.setEnabled(!enable);
        filter18CheckBox.setEnabled(!enable);
        filter19CheckBox.setEnabled(!enable);
        filter20CheckBox.setEnabled(!enable);
        filter21CheckBox.setEnabled(!enable);
        filter22CheckBox.setEnabled(!enable);
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

    // Sets up everything related to hierarchical image searches
    protected void initHierarchicalImageSearch()
    {
        // Show/hide panels depending on whether this body has hierarchical image search capabilities
        if(smallBodyConfig.hasHierarchicalImageSearch)
        {
            // Has hierarchical search capabilities, these replace the camera and filter checkboxes so hide them
            filterCheckBoxPanel.setVisible(false);
            userDefinedCheckBoxPanel.setVisible(false);

            // Create the tree
            checkBoxTree = new CheckBoxTree(smallBodyConfig.hierarchicalImageSearchSpecification.getTreeModel());

            // Place the tree in the panel
            this.hierarchicalSearchScrollPane.setViewportView(checkBoxTree);
        }
        else
        {
            // No hierarchical search capabilities, hide the scroll pane
            hierarchicalSearchScrollPane.setVisible(false);
        }
    }

}
