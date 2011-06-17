package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import nom.tam.fits.FitsException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkActor;
import vtk.vtkPolyData;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.SearchPanelUtil;
import edu.jhuapl.near.model.ColorImage.ColorImageKey;
import edu.jhuapl.near.model.ColorImage.NoOverlapException;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection;
import edu.jhuapl.near.model.eros.MSIColorImageCollection;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.MSIImageCollection;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.query.ErosQuery;
import edu.jhuapl.near.util.IdPair;
import edu.jhuapl.near.util.Properties;


/**
 * Panel with options for searching for MSI images. Two databases can be searched.
 * 1. The original one submitted to PDS with over 100000 images. Spice kernels for
 *    these images are not perfect and as a result, the images may be slightly misaligned
 *    with the asteroid.
 * 2. Bob Gaskell's list with high quality pointing information. This list is a subset of
 *    the first with about 20000 images which he used to create the shape models.
 *    Images are much better registered with the asteroid.
 *
 * @author eli
 *
 */
public class MSISearchPanel extends JPanel implements ActionListener, MouseListener, PropertyChangeListener
{
    private final String MSI_REMOVE_ALL_BUTTON_TEXT = "Remove All Boundaries";

    private final ModelManager modelManager;
    private PickManager pickManager;
    private JComboBox msiSourceComboBox;
    private java.util.Date startDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
    private java.util.Date endDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date:";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JCheckBox filter1CheckBox;
    private JCheckBox filter2CheckBox;
    private JCheckBox filter3CheckBox;
    private JCheckBox filter4CheckBox;
    private JCheckBox filter5CheckBox;
    private JCheckBox filter6CheckBox;
    private JCheckBox filter7CheckBox;

    private JCheckBox iofdblCheckBox;
    private JCheckBox cifdblCheckBox;

    private JComboBox hasLimbComboBox;

    private JFormattedTextField fromDistanceTextField;
    private JFormattedTextField toDistanceTextField;
    private JFormattedTextField fromResolutionTextField;
    private JFormattedTextField toResolutionTextField;
    private JFormattedTextField fromIncidenceTextField;
    private JFormattedTextField toIncidenceTextField;
    private JFormattedTextField fromEmissionTextField;
    private JFormattedTextField toEmissionTextField;
    private JFormattedTextField fromPhaseTextField;
    private JFormattedTextField toPhaseTextField;

    private JToggleButton selectRegionButton;

    private JFormattedTextField searchByNumberTextField;
    private JCheckBox searchByNumberCheckBox;

    private JList resultList;
    private ImagePopupMenu msiPopupMenu;
    private ArrayList<String> msiRawResults = new ArrayList<String>();
    private JLabel resultsLabel;
    private String msiResultsLabelText = " ";
    private JButton nextButton;
    private JButton prevButton;
    private JButton removeAllButton;
    private JButton removeAllImagesButton;
    private JComboBox numberOfBoundariesComboBox;
    private IdPair resultIntervalCurrentlyShown = null;

    private JButton redButton;
    private JButton greenButton;
    private JButton blueButton;
    private JLabel redLabel;
    private JLabel greenLabel;
    private JLabel blueLabel;
    private JButton generateColorImageButton;
    private ImageKey selectedRedKey;
    private ImageKey selectedGreenKey;
    private ImageKey selectedBlueKey;
    private JList colorImagesDisplayedList;
    private JButton removeColorImageButton;
    private ColorImagePopupMenu msiColorPopupMenu;

    /**
     * The source of the msi images of the most recently executed query
     */
    private MSIImage.ImageSource msiSourceOfLastQuery = MSIImage.ImageSource.PDS;

    public MSISearchPanel(
            final ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            final PickManager pickManager,
            Renderer renderer)
    {
        setLayout(new MigLayout("wrap 1, insets 0", "[grow,fill]"));
        JPanel topPanel = new JPanel();
        //topPanel.setLayout(new BoxLayout(topPanel,
        //        BoxLayout.PAGE_AXIS));

        topPanel.setLayout(new MigLayout("wrap 1, insets 0"));
        //setLayout(new BoxLayout(this,
        //        BoxLayout.PAGE_AXIS));

        this.modelManager = modelManager;
        this.pickManager = pickManager;

        this.addComponentListener(new ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                selectRegionButton.setSelected(false);
                pickManager.setPickMode(PickMode.DEFAULT);
            }
        });

        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane,
                BoxLayout.PAGE_AXIS));

        //pane.setBorder(
        //        new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
        //                           new TitledBorder("Query Editor")));

        final JPanel msiSourcePanel = new JPanel();
        JLabel msiSourceLabel = new JLabel("MSI Source:");
        Object[] msiSourceOptions = {MSIImage.ImageSource.PDS, MSIImage.ImageSource.GASKELL};
        msiSourceComboBox = new JComboBox(msiSourceOptions);
        //msiSourceComboBox.setMaximumSize(new Dimension(1000, 23));
        msiSourcePanel.add(msiSourceLabel);
        msiSourcePanel.add(msiSourceComboBox);
        pane.add(msiSourcePanel);

        final JPanel startDatePanel = new JPanel();
        this.startDateLabel = new JLabel(START_DATE_LABEL_TEXT);
        startDatePanel.add(this.startDateLabel);
        startSpinner = new JSpinner(new SpinnerDateModel(startDate, null, null, Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    java.util.Date date =
                        ((SpinnerDateModel)startSpinner.getModel()).getDate();
                    if (date != null)
                        startDate = date;
                }
            });
        startDatePanel.add(startSpinner);
        startSpinner.setEnabled(true);
        pane.add(startDatePanel);

        final JPanel endDatePanel = new JPanel();
        this.endDateLabel = new JLabel(END_DATE_LABEL_TEXT);
        endDatePanel.add(this.endDateLabel);
        endSpinner = new JSpinner(new SpinnerDateModel(endDate, null, null, Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    java.util.Date date =
                        ((SpinnerDateModel)endSpinner.getModel()).getDate();
                    if (date != null)
                        endDate = date;
                }
            });
        endDatePanel.add(endSpinner);
        endSpinner.setEnabled(true);
        pane.add(endDatePanel);



        final JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel,
                BoxLayout.LINE_AXIS));
        filter1CheckBox = new JCheckBox();
        filter1CheckBox.setText("Filter 1 (550 nm)");
        filter1CheckBox.setSelected(true);
        filter2CheckBox = new JCheckBox();
        filter2CheckBox.setText("Filter 2 (450 nm)");
        filter2CheckBox.setSelected(true);
        filter3CheckBox = new JCheckBox();
        filter3CheckBox.setText("Filter 3 (760 nm)");
        filter3CheckBox.setSelected(true);
        filter4CheckBox = new JCheckBox();
        filter4CheckBox.setText("Filter 4 (950 nm)");
        filter4CheckBox.setSelected(true);
        filter5CheckBox = new JCheckBox();
        filter5CheckBox.setText("Filter 5 (900 nm)");
        filter5CheckBox.setSelected(true);
        filter6CheckBox = new JCheckBox();
        filter6CheckBox.setText("Filter 6 (1000 nm)");
        filter6CheckBox.setSelected(true);
        filter7CheckBox = new JCheckBox();
        filter7CheckBox.setText("Filter 7 (1050 nm)");
        filter7CheckBox.setSelected(true);

        JPanel filtersSub1Panel = new JPanel();
        filtersSub1Panel.setLayout(new BoxLayout(filtersSub1Panel,
                BoxLayout.PAGE_AXIS));
        filtersSub1Panel.add(filter1CheckBox);
        filtersSub1Panel.add(filter2CheckBox);
        filtersSub1Panel.add(filter3CheckBox);
        filtersSub1Panel.add(filter4CheckBox);

        JPanel filtersSub2Panel = new JPanel();
        filtersSub2Panel.setLayout(new BoxLayout(filtersSub2Panel,
                BoxLayout.PAGE_AXIS));
        filtersSub2Panel.add(filter5CheckBox);
        filtersSub2Panel.add(filter6CheckBox);
        filtersSub2Panel.add(filter7CheckBox);

        filtersPanel.add(filtersSub1Panel);
        filtersPanel.add(Box.createHorizontalStrut(15));
        filtersPanel.add(filtersSub2Panel);

        //filtersPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

        final JPanel iofcifPanel = new JPanel();
        iofcifPanel.setLayout(new BoxLayout(iofcifPanel,
                BoxLayout.LINE_AXIS));
        //iofcifPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

        iofdblCheckBox = new JCheckBox();
        iofdblCheckBox.setText("iofdbl");
        iofdblCheckBox.setSelected(true);
        cifdblCheckBox = new JCheckBox();
        cifdblCheckBox.setText("cifdbl");
        cifdblCheckBox.setSelected(true);

        iofcifPanel.add(iofdblCheckBox);
        iofcifPanel.add(Box.createHorizontalStrut(15));
        iofcifPanel.add(cifdblCheckBox);

        iofcifPanel.add(Box.createHorizontalStrut(25));

        final JPanel hasLimbPanel = new JPanel();
        hasLimbPanel.setLayout(new BoxLayout(hasLimbPanel,
                BoxLayout.LINE_AXIS));

        final JLabel hasLimbLabel = new JLabel("Limb: ");
        Object[] hasLimbOptions = {"with or without", "with only", "without only"};
        hasLimbComboBox = new JComboBox(hasLimbOptions);
        hasLimbComboBox.setMaximumSize(new Dimension(150, 23));

        hasLimbPanel.add(hasLimbLabel);
        hasLimbPanel.add(hasLimbComboBox);

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);

        final JPanel distancePanel = new JPanel();
        distancePanel.setLayout(new BoxLayout(distancePanel,
                BoxLayout.LINE_AXIS));
        final JLabel fromDistanceLabel = new JLabel("S/C Distance from ");
        fromDistanceTextField = new JFormattedTextField(nf);
        fromDistanceTextField.setValue(0.0);
        fromDistanceTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel toDistanceLabel = new JLabel(" to ");
        toDistanceTextField = new JFormattedTextField(nf);
        toDistanceTextField.setValue(100.0);
        toDistanceTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel endDistanceLabel = new JLabel(" km");

        distancePanel.add(fromDistanceLabel);
        distancePanel.add(fromDistanceTextField);
        distancePanel.add(toDistanceLabel);
        distancePanel.add(toDistanceTextField);
        distancePanel.add(endDistanceLabel);


        final JPanel resolutionPanel = new JPanel();
        resolutionPanel.setLayout(new BoxLayout(resolutionPanel,
                BoxLayout.LINE_AXIS));
        final JLabel fromResolutionLabel = new JLabel("Resolution from ");
        fromResolutionTextField = new JFormattedTextField(nf);
        fromResolutionTextField.setValue(0.0);
        fromResolutionTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel toResolutionLabel = new JLabel(" to ");
        toResolutionTextField = new JFormattedTextField(nf);
        toResolutionTextField.setValue(50.0);
        toResolutionTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel endResolutionLabel = new JLabel(" mpp");
        endResolutionLabel.setToolTipText("meters per pixel");

        resolutionPanel.add(fromResolutionLabel);
        resolutionPanel.add(fromResolutionTextField);
        resolutionPanel.add(toResolutionLabel);
        resolutionPanel.add(toResolutionTextField);
        resolutionPanel.add(endResolutionLabel);


        fromIncidenceTextField = new JFormattedTextField(nf);
        toIncidenceTextField = new JFormattedTextField(nf);
        final JPanel incidencePanel = SearchPanelUtil.createFromToPanel(
                fromIncidenceTextField,
                toIncidenceTextField,
                0.0,
                180.0,
                "Incidence from",
                "to",
                "degrees");

        fromEmissionTextField = new JFormattedTextField(nf);
        toEmissionTextField = new JFormattedTextField(nf);
        final JPanel emissionPanel = SearchPanelUtil.createFromToPanel(
                fromEmissionTextField,
                toEmissionTextField,
                0.0,
                180.0,
                "Emissiom from",
                "to",
                "degrees");

        fromPhaseTextField = new JFormattedTextField(nf);
        toPhaseTextField = new JFormattedTextField(nf);
        final JPanel phasePanel = SearchPanelUtil.createFromToPanel(
                fromPhaseTextField,
                toPhaseTextField,
                0.0,
                180.0,
                "Phase from",
                "to",
                "degrees");


        final JPanel searchByNumberPanel = new JPanel();
        searchByNumberPanel.setLayout(new BoxLayout(searchByNumberPanel,
                BoxLayout.LINE_AXIS));
        searchByNumberCheckBox = new JCheckBox();
        searchByNumberCheckBox.setText("Search by number");
        searchByNumberCheckBox.setSelected(false);
        nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        searchByNumberTextField = new JFormattedTextField(nf);
        searchByNumberTextField.setMaximumSize(new Dimension(100, 23));
        searchByNumberTextField.setEnabled(false);
        searchByNumberCheckBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                boolean enable = e.getStateChange() == ItemEvent.SELECTED;
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
                iofdblCheckBox.setEnabled(!enable);
                cifdblCheckBox.setEnabled(!enable);
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
                for (Component comp : incidencePanel.getComponents())
                    comp.setEnabled(!enable);
                for (Component comp : emissionPanel.getComponents())
                    comp.setEnabled(!enable);
                for (Component comp : phasePanel.getComponents())
                    comp.setEnabled(!enable);
            }
        });

        searchByNumberPanel.add(searchByNumberCheckBox);
        searchByNumberPanel.add(searchByNumberTextField);

        JPanel selectRegionPanel = new JPanel();
        //selectRegionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        selectRegionButton = new JToggleButton("Select Region");
        selectRegionButton.setEnabled(true);
        selectRegionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (selectRegionButton.isSelected())
                    pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
                else
                    pickManager.setPickMode(PickMode.DEFAULT);
            }
        });
        selectRegionPanel.add(selectRegionButton);

        final JButton clearRegionButton = new JButton("Clear Region");
        clearRegionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
                selectionModel.removeAllStructures();
            }
        });
        selectRegionPanel.add(clearRegionButton);


        final JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Search");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        submitPanel.add(submitButton);


        pane.add(filtersPanel);
        pane.add(iofcifPanel);
        pane.add(hasLimbPanel);
        pane.add(distancePanel);
        pane.add(resolutionPanel);
        pane.add(incidencePanel);
        pane.add(emissionPanel);
        pane.add(phasePanel);
        pane.add(Box.createVerticalStrut(10));
        pane.add(searchByNumberPanel);
        pane.add(selectRegionPanel);
        pane.add(submitPanel);

        topPanel.add(pane);







        JPanel resultsPanel = new JPanel(new BorderLayout());

        MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
        MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
        msiPopupMenu = new ImagePopupMenu(msiImages, msiBoundaries, infoPanelManager, renderer, this);

        resultsLabel = new JLabel(" ");

        //Create the list and put it in a scroll pane.
        resultList = new JList();
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(this);

        JScrollPane listScrollPane = new JScrollPane(resultList);
        listScrollPane.setPreferredSize(new Dimension(300, 200));

        //listScrollPane.setBorder(
        //       new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
        //                           new TitledBorder("Query Results")));

        resultsPanel.add(resultsLabel, BorderLayout.NORTH);
        resultsPanel.add(listScrollPane, BorderLayout.CENTER);

        final JPanel resultControlsPanel = new JPanel(new BorderLayout());

        final JPanel resultSub1ControlsPanel = new JPanel();

        resultSub1ControlsPanel.setLayout(new BoxLayout(resultSub1ControlsPanel,
                BoxLayout.LINE_AXIS));

        final JLabel showLabel = new JLabel("Number Boundaries");
        Object [] options2 = {
                10, 20, 30, 40, 50, 60, 70, 80, 90, 100,
                110, 120, 130, 140, 150, 160, 170, 180, 190, 200,
                210, 220, 230, 240, 250
                };
        numberOfBoundariesComboBox = new JComboBox(options2);
        numberOfBoundariesComboBox.setMaximumSize(new Dimension(100, 23));

        nextButton = new JButton(">");
        nextButton.setActionCommand(">");
        nextButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (resultIntervalCurrentlyShown != null)
                {
                    // Only get the next block if there's something left to show.
                    if (resultIntervalCurrentlyShown.id2 < resultList.getModel().getSize())
                    {
                        resultIntervalCurrentlyShown.nextBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
                        showMSIBoundaries(resultIntervalCurrentlyShown);
                    }
                }
                else
                {
                    resultIntervalCurrentlyShown = new IdPair(0, (Integer)numberOfBoundariesComboBox.getSelectedItem());
                    showMSIBoundaries(resultIntervalCurrentlyShown);
                }
            }
        });
        nextButton.setEnabled(true);

        prevButton = new JButton("<");
        prevButton.setActionCommand("<");
        prevButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (resultIntervalCurrentlyShown != null)
                {
                    // Only get the prev block if there's something left to show.
                    if (resultIntervalCurrentlyShown.id1 > 0)
                    {
                        resultIntervalCurrentlyShown.prevBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
                        showMSIBoundaries(resultIntervalCurrentlyShown);
                    }
                }
            }
        });
        prevButton.setEnabled(true);

        resultSub1ControlsPanel.add(showLabel);
        resultSub1ControlsPanel.add(numberOfBoundariesComboBox);
        resultSub1ControlsPanel.add(Box.createHorizontalStrut(10));
        resultSub1ControlsPanel.add(prevButton);
        resultSub1ControlsPanel.add(nextButton);

        //------------------------------------------------------
        // setup color image generation controls

        JPanel resultSub25ControlsPanel = new JPanel();
        resultSub25ControlsPanel.setLayout(new MigLayout());
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        JLabel colorImagelabel = new JLabel("Color Image Generation");

        String tooltip = "Select an image from the list above and then press this button";

        redButton = new JButton("Red");
        redButton.setBackground(Color.RED);
        redButton.setToolTipText(tooltip);
        redLabel = new JLabel();
        greenButton = new JButton("Green");
        greenButton.setBackground(Color.GREEN);
        greenButton.setToolTipText(tooltip);
        greenLabel = new JLabel();
        blueButton = new JButton("Blue");
        blueButton.setBackground(Color.BLUE);
        blueButton.setToolTipText(tooltip);
        blueLabel = new JLabel();

        ActionListener colorButtonsListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int index = resultList.getSelectedIndex();
                if (index >= 0)
                {
                    String image = msiRawResults.get(index);
                    String name = image.substring(23, 32);
                    image = image.substring(0,image.length()-4);
                    if (e.getSource() == redButton)
                    {
                        redLabel.setText(name);
                        selectedRedKey = new ImageKey(image, msiSourceOfLastQuery);
                    }
                    else if (e.getSource() == greenButton)
                    {
                        greenLabel.setText(name);
                        selectedGreenKey = new ImageKey(image, msiSourceOfLastQuery);
                    }
                    else if (e.getSource() == blueButton)
                    {
                        blueLabel.setText(name);
                        selectedBlueKey = new ImageKey(image, msiSourceOfLastQuery);
                    }
                }
            }
        };

        redButton.addActionListener(colorButtonsListener);
        greenButton.addActionListener(colorButtonsListener);
        blueButton.addActionListener(colorButtonsListener);

        generateColorImageButton = new JButton("Generate Color Image");
        generateColorImageButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                showColorImage(e);
            }
        });

        MSIColorImageCollection msiColorImages = (MSIColorImageCollection)modelManager.getModel(ModelNames.MSI_COLOR_IMAGES);
        msiColorPopupMenu = new ColorImagePopupMenu(msiColorImages, infoPanelManager);

        colorImagesDisplayedList = new JList();
        colorImagesDisplayedList.setModel(new DefaultListModel());
        colorImagesDisplayedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane colorImagesDisplayedListScrollPane = new JScrollPane(colorImagesDisplayedList);
        colorImagesDisplayedListScrollPane.setPreferredSize(new Dimension(300, 100));
        colorImagesDisplayedList.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    int index = colorImagesDisplayedList.locationToIndex(e.getPoint());

                    if (index >= 0 && colorImagesDisplayedList.getCellBounds(index, index).contains(e.getPoint()))
                    {
                        colorImagesDisplayedList.setSelectedIndex(index);
                        ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).get(index);
                        msiColorPopupMenu.setCurrentImage(colorKey);
                        msiColorPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        removeColorImageButton = new JButton("Remove");
        removeColorImageButton.setActionCommand("Remove");
        removeColorImageButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int index = colorImagesDisplayedList.getSelectedIndex();
                if (index >= 0)
                {
                    ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).remove(index);
                    MSIColorImageCollection model = (MSIColorImageCollection)modelManager.getModel(ModelNames.MSI_COLOR_IMAGES);
                    model.removeImage(colorKey);

                    // Select the element in its place (unless it's the last one in which case
                    // select the previous one)
                    if (index >= colorImagesDisplayedList.getModel().getSize())
                        --index;
                    if (index >= 0)
                        colorImagesDisplayedList.setSelectionInterval(index, index);
                }
            }
        });
        removeColorImageButton.setEnabled(true);

        resultSub25ControlsPanel.add(separator, "growx, span, wrap, gaptop 10");
        resultSub25ControlsPanel.add(colorImagelabel, "span, wrap");
        resultSub25ControlsPanel.add(redButton, "w 60!");
        resultSub25ControlsPanel.add(redLabel, "wrap");
        resultSub25ControlsPanel.add(greenButton, "w 60!");
        resultSub25ControlsPanel.add(greenLabel, "wrap");
        resultSub25ControlsPanel.add(blueButton, "w 60!");
        resultSub25ControlsPanel.add(blueLabel, "wrap");
        resultSub25ControlsPanel.add(generateColorImageButton, "align center, span");
        resultSub25ControlsPanel.add(colorImagesDisplayedListScrollPane, "align center, span, h 100!");
        resultSub25ControlsPanel.add(removeColorImageButton, "align center, span");


        //------------------------------------------------------


        JPanel resultSub2ControlsPanel = new JPanel();
        resultSub2ControlsPanel.setLayout(new BoxLayout(resultSub2ControlsPanel,
                BoxLayout.PAGE_AXIS));

        removeAllButton = new JButton(MSI_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand("Remove All Boundaries");
        removeAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
                model.removeAllBoundaries();
                resultIntervalCurrentlyShown = null;
            }
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        removeAllImagesButton = new JButton("Remove All Images");
        removeAllImagesButton.setActionCommand("Remove All Images");
        removeAllImagesButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                MSIImageCollection model = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
                model.removeAllImages();
            }
        });
        removeAllImagesButton.setEnabled(true);
        removeAllImagesButton.setAlignmentX(Component.CENTER_ALIGNMENT);

//        final JCheckBox showFrustumsCheckBox = new JCheckBox("Show Frustums");
//        showFrustumsCheckBox.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                MSIImageCollection model = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
//                model.setShowFrustums(showFrustumsCheckBox.isSelected());
//            }
//        });
//
//        resultSub2ControlsPanel.add(showFrustumsCheckBox); // for now don't show this
        resultSub2ControlsPanel.add(removeAllButton);
        resultSub2ControlsPanel.add(removeAllImagesButton);

        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.NORTH);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub25ControlsPanel, BorderLayout.SOUTH);

        resultsPanel.add(resultControlsPanel, BorderLayout.SOUTH);

        topPanel.add(resultsPanel);

        JScrollPane topScrollPane = new JScrollPane(topPanel);

        add(topScrollPane);

    }

    public void actionPerformed(ActionEvent actionEvent)
    {
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
            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
            SmallBodyModel erosModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
            if (selectionModel.getNumberOfStructures() > 0)
            {
                AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);

                // Always use the lowest resolution model for getting the intersection cubes list.
                // Therefore, if the selection region was created using a higher resolution model,
                // we need to recompute the selection region using the low res model.
                if (erosModel.getModelResolution() > 0)
                {
                    vtkPolyData interiorPoly = new vtkPolyData();
                    erosModel.drawPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
                    cubeList = erosModel.getIntersectingCubes(interiorPoly);
                }
                else
                {
                    cubeList = erosModel.getIntersectingCubes(region.interiorPolyData);
                }
            }

            MSIImage.ImageSource msiSource = null;
            if (msiSourceComboBox.getSelectedItem().equals(MSIImage.ImageSource.PDS))
                msiSource = MSIImage.ImageSource.PDS;
            else
                msiSource = MSIImage.ImageSource.GASKELL;
            System.out.println(msiSource.toString());
            ArrayList<String> results = ErosQuery.getInstance().runQuery(
                    ErosQuery.Datatype.MSI,
                    startDateJoda,
                    endDateJoda,
                    filtersChecked,
                    iofdblCheckBox.isSelected(),
                    cifdblCheckBox.isSelected(),
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
                    msiSource,
                    hasLimbComboBox.getSelectedIndex());

            if (msiSourceComboBox.getSelectedItem().equals(MSIImage.ImageSource.PDS))
                msiSourceOfLastQuery = MSIImage.ImageSource.PDS;
            else
                msiSourceOfLastQuery = MSIImage.ImageSource.GASKELL;

            setMSIResults(results);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }

    private void setMSIResults(ArrayList<String> results)
    {
        msiResultsLabelText = results.size() + " images matched";
        resultsLabel.setText(msiResultsLabelText);
        msiRawResults = results;

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
                    + ", source: " + msiSourceOfLastQuery
                    );

            ++i;
        }

        resultList.setListData(formattedResults);

        // Show the first set of boundaries
        this.resultIntervalCurrentlyShown = new IdPair(0, (Integer)this.numberOfBoundariesComboBox.getSelectedItem());
        this.showMSIBoundaries(resultIntervalCurrentlyShown);
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = resultList.locationToIndex(e.getPoint());

            if (index >= 0 && resultList.getCellBounds(index, index).contains(e.getPoint()))
            {
                resultList.setSelectedIndex(index);
                String name = msiRawResults.get(index);
                msiPopupMenu.setCurrentImage(new ImageKey(name.substring(0, name.length()-4), msiSourceOfLastQuery));
                msiPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void showMSIBoundaries(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
        model.removeAllBoundaries();

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= msiRawResults.size())
                break;

            try
            {
                String currentImage = msiRawResults.get(i);
                //String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_BOUNDARY.VTK";
                //String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_DDR.LBL";
                String boundaryName = currentImage.substring(0,currentImage.length()-4);
                model.addBoundary(new ImageKey(boundaryName, msiSourceOfLastQuery));
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
        MSIColorImageCollection model = (MSIColorImageCollection)modelManager.getModel(ModelNames.MSI_COLOR_IMAGES);

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
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (FitsException e1)
            {
                // TODO Auto-generated catch block
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

//    private void resetColorImageSelection()
//    {
//        selectedRedKey = null;
//        selectedGreenKey = null;
//        selectedBlueKey = null;
//        redLabel.setText("");
//        greenLabel.setText("");
//        blueLabel.setText("");
//    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof MSIImageCollection || model instanceof MSIBoundaryCollection)
            {
                String name = null;

                if (model instanceof MSIImageCollection)
                    name = ((MSIImageCollection)model).getImageName((vtkActor)e.getPickedProp());
                else if (model instanceof MSIBoundaryCollection)
                    name = ((MSIBoundaryCollection)model).getBoundaryName((vtkActor)e.getPickedProp());

                int idx = msiRawResults.indexOf(name + ".FIT");

                resultList.setSelectionInterval(idx, idx);
                Rectangle cellBounds = resultList.getCellBounds(idx, idx);
                if (cellBounds != null)
                    resultList.scrollRectToVisible(cellBounds);
            }
        }
    }
}
