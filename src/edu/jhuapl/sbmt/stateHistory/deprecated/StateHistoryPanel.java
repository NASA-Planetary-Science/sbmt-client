///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
///*
// * CustomImageLoaderPanel.java
// *
// * Created on Jun 5, 2012, 3:56:56 PM
// */
//package edu.jhuapl.sbmt.stateHistory.deprecated;
//
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.GridLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
//import java.io.File;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//import javax.swing.BoxLayout;
//import javax.swing.DefaultListModel;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JSpinner;
//import javax.swing.JTextField;
//import javax.swing.JTextPane;
//import javax.swing.table.DefaultTableModel;
//
//import org.joda.time.DateTime;
//import org.joda.time.format.ISODateTimeFormat;
//
//import vtk.rendering.jogl.vtkJoglPanelComponent;
//
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
//import edu.jhuapl.saavtk.model.ModelManager;
//import edu.jhuapl.saavtk.model.ModelNames;
//import edu.jhuapl.saavtk.pick.PickManager;
//import edu.jhuapl.saavtk.util.FileCache;
//import edu.jhuapl.saavtk.util.MapUtil;
//import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
//import edu.jhuapl.sbmt.client.SmallBodyModel;
//import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
//import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryCollection;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel.StateHistoryKey;
//import edu.jhuapl.sbmt.stateHistory.ui.version2.IStateHistoryPanel;
//
//
//public class StateHistoryPanel extends javax.swing.JPanel implements ItemListener, IStateHistoryPanel
//{
//
//    private ModelManager modelManager;
//
//    private boolean initialized = false;
//    private StateHistoryCollection runs;
//
//    private javax.swing.JTable optionsTable;
//
//    private javax.swing.JCheckBox showEarthMarker;
//    private javax.swing.JCheckBox showSunMarker;
//    private javax.swing.JCheckBox showSpacecraftMarkerHead;
//    private javax.swing.JCheckBox showSpacecraftMarker;
//    private javax.swing.JCheckBox showLighting;
//    private javax.swing.JRadioButton showSpacecraftView;
//    private javax.swing.JRadioButton showEarthView;
//    private javax.swing.JRadioButton showSunView;
//    private javax.swing.JRadioButton showFreeView;
//    private JTextField viewAngleInput;
//    private JCheckBox showTrajectory;
//    private JCheckBox mapTrajectory;
//    private JComboBox<String> distanceOptions;
//    private JSpinner startTime, endTime;
//    private JButton getQueryButton;
//    private File path;
//    final int lineLength = 121;
//    private SmallBodyModel bodyModel;
//    private SmallBodyViewConfig config;
//    private JButton saveAnimationButton;
//
//    private ViewOptionsPanel simulationMarkerPanel;
//    private TimeControlPane timeControlPane;
//
//    private Renderer renderer;
//    private vtkJoglPanelComponent renWin;
//
//    /** Creates new form CustomImageLoaderPanel */
//    public StateHistoryPanel(
//            final ModelManager modelManager,
//            SbmtInfoWindowManager infoPanelManager,
//            final PickManager pickManager,
//            Renderer renderer)
//    {
//        this.modelManager = modelManager;
//        this.renderer = renderer;
//        this.renWin = renderer.getRenderWindowPanel();
//        //        this.pickManager = pickManager;
//        //        this.infoManager = infoPanelManager;
//
//        bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
//        config = (SmallBodyViewConfig) bodyModel.getConfig();
//
//        runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
//
//        initComponents();
//
//        // We need to update the scale bar whenever there is a render or whenever
//        // the window gets resized. Although resizing a window results in a render,
//        // we still need to listen to a resize event since only listening to render
//        // results in the scale bar not being positioned correctly when during the
//        // resize for some reason. Thus we need to register a component
//        // listener on the renderer panel as well to listen explicitly to resize events.
//        // Note also that this functionality is in this class since picking is required
//        // to compute the value of the scale bar.
//        renWin.getRenderWindow().AddObserver("EndEvent", this, "updateTimeBarPosition");
//        renWin.getComponent().addComponentListener(new ComponentAdapter()
//        {
//            @Override
//            public void componentResized(ComponentEvent e)
//            {
//                updateTimeBarValue();
//                updateTimeBarPosition();
//            }
//        });
//
//    }
//
//    private void updateTimeBarValue()
//    {
//        if (runs != null)
//        {
//            StateHistoryModel currentRun = runs.getCurrentRun();
//            if (currentRun != null)
//            {
//                try
//                {
//                    Double time = currentRun.getTime();
//                    currentRun.updateTimeBarValue(time);
//                }catch(Exception ex){
//
//                }
//            }
//        }
//    }
//
////    private void updateScalarBar()
////    {
////        StateHistoryModel currentRun = runs.getCurrentRun();
////        if (currentRun != null)
////        {
////            currentRun.updateScalarBar();
////        }
////    }
//
//    public void updateTimeBarPosition()
//    {
//        if (runs != null)
//        {
//            StateHistoryCollection runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
//            StateHistoryModel currentRun = runs.getCurrentRun();
//            if (currentRun != null)
//                currentRun.updateTimeBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
//        }
//    }
//
//
//    private JLabel lblStartLabel;
//    private JLabel lblStop;
//    private TimeIntervalTablePanel timeTablePanel;
//
//    private GridBagConstraints gridBagConstraints_1;
//    private JPanel panel;
//
//    private String getConfigFilename()
//    {
//        return modelManager.getPolyhedralModel().getConfigFilename();
//    }
//
//    private RunInfo getRunInfo(int index)
//    {
//        return (RunInfo)((DefaultListModel)optionsTable.getModel()).get(index);
//    }
//
//    private String getFileName(RunInfo runInfo)
//    {
//        //        return getCustomDataFolder() + File.separator + runInfo.runfilename;
//        return runInfo.runfilename;
//    }
//
//    private String getFileName(int index)
//    {
//        return getFileName(getRunInfo(index));
//    }
//
//    private void updateConfigFile()
//    {
//        MapUtil configMap = new MapUtil(getConfigFilename());
//
//        String runNames = "";
//        String runFilenames = "";
//
//        DefaultListModel runListModel = (DefaultListModel)optionsTable.getModel();
//        for (int i=0; i<runListModel.size(); ++i)
//        {
//            RunInfo runInfo = (RunInfo)runListModel.get(i);
//
//            runFilenames += runInfo.runfilename;
//            runNames += runInfo.name;
//
//            if (i < runListModel.size()-1)
//            {
//                runNames += CustomShapeModel.LIST_SEPARATOR;
//                runFilenames += CustomShapeModel.LIST_SEPARATOR;
//            }
//        }
//
//        Map<String, String> newMap = new LinkedHashMap<String, String>();
//
//        newMap.put(StateHistoryModel.RUN_NAMES, runNames);
//        newMap.put(StateHistoryModel.RUN_FILENAMES, runFilenames);
//
//        configMap.put(newMap);
//    }
//
//    private void initComponents() {
//
//        //
//        // time set panel
//        //
//
//        // Calculate the beginning and end of available time
//        try {
//            path = FileCache.getFileFromServer(config.timeHistoryFile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // get range of available dates
//        DateTime start = ISODateTimeFormat.dateTimeParser().parseDateTime(readString(lineLength));//oldDate.parse(currentRun.getIntervalTime()[0]);
//        DateTime end = ISODateTimeFormat.dateTimeParser().parseDateTime(readString((int)getBinaryFileLength()*lineLength-lineLength));
//        Date newStart = start.toDate();
//        Date newEnd = end.toDate();
//
//
//        GridBagLayout gridBagLayout = new GridBagLayout();
//        gridBagLayout.rowHeights = new int[]{0, 0, 121, 0, 0, 0, 0, 0, 0};
//        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
//        gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0};
//        gridBagLayout.columnWidths = new int[]{0, 0, 474};
//        setLayout(gridBagLayout);
//        java.awt.GridBagConstraints gridBagConstraints;
//        revalidate();
//
//        JTextPane queryInfo = new JTextPane();
//        GridBagConstraints gbc_queryInfo = new GridBagConstraints();
//        gbc_queryInfo.fill = GridBagConstraints.HORIZONTAL;
//        gbc_queryInfo.gridwidth = 2;
//        gbc_queryInfo.insets = new Insets(0, 0, 5, 5);
//        gbc_queryInfo.gridx = 1;
//        gbc_queryInfo.gridy = 0;
//        add(queryInfo, gbc_queryInfo);
//        queryInfo.setText("<html>Hello World</html>"); // showing off
//        queryInfo.setEditable(false); // as before
//        queryInfo.setBackground(null); // this is the same as a JLabel
//        queryInfo.setBorder(null);
//        queryInfo.setText("Available Time Range: " + new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(newStart)+ " to " + new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(newEnd));
//
//        panel = new JPanel();
//        GridBagConstraints gbc_panel = new GridBagConstraints();
//        gbc_panel.fill = GridBagConstraints.BOTH;
//        gbc_panel.gridwidth = 2;
//        gbc_panel.insets = new Insets(0, 0, 5, 0);
//        gbc_panel.gridx = 1;
//        gbc_panel.gridy = 1;
//        add(panel, gbc_panel);
//        GridBagLayout gbl_panel = new GridBagLayout();
//        gbl_panel.columnWidths = new int[]{96, 102, 329, 0};
//        gbl_panel.rowHeights = new int[]{0, 25, 0, 0};
//        gbl_panel.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
//        gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
//        panel.setLayout(gbl_panel);
//
//        lblStartLabel = new JLabel("Start Time:");
//        GridBagConstraints gbc_lblStartLabel = new GridBagConstraints();
//        gbc_lblStartLabel.insets = new Insets(0, 0, 5, 5);
//        gbc_lblStartLabel.gridx = 1;
//        gbc_lblStartLabel.gridy = 0;
//        panel.add(lblStartLabel, gbc_lblStartLabel);
//        startTime = new JSpinner();
//        GridBagConstraints gbc_startTime = new GridBagConstraints();
//        gbc_startTime.fill = GridBagConstraints.HORIZONTAL;
//        gbc_startTime.insets = new Insets(0, 0, 5, 0);
//        gbc_startTime.gridx = 2;
//        gbc_startTime.gridy = 0;
//        panel.add(startTime, gbc_startTime);
//
//                startTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(newStart.getTime()), null, null, java.util.Calendar.DAY_OF_MONTH));
//                startTime.setEditor(new javax.swing.JSpinner.DateEditor(startTime, "yyyy-MMM-dd HH:mm:ss.SSS"));
//                startTime.setMinimumSize(new java.awt.Dimension(36, 22));
//                startTime.setPreferredSize(new java.awt.Dimension(200, 28));
//
//        lblStop = new JLabel("Stop Time:");
//        GridBagConstraints gbc_lblStop = new GridBagConstraints();
//        gbc_lblStop.insets = new Insets(0, 0, 5, 5);
//        gbc_lblStop.gridx = 1;
//        gbc_lblStop.gridy = 1;
//        panel.add(lblStop, gbc_lblStop);
//        endTime = new JSpinner();
//        GridBagConstraints gbc_endTime = new GridBagConstraints();
//        gbc_endTime.fill = GridBagConstraints.HORIZONTAL;
//        gbc_endTime.insets = new Insets(0, 0, 5, 0);
//        gbc_endTime.gridx = 2;
//        gbc_endTime.gridy = 1;
//        panel.add(endTime, gbc_endTime);
//
//                endTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(newEnd.getTime()), null, null, java.util.Calendar.DAY_OF_MONTH));
//                endTime.setEditor(new javax.swing.JSpinner.DateEditor(endTime, "yyyy-MMM-dd HH:mm:ss.SSS"));
//                endTime.setMinimumSize(new java.awt.Dimension(36, 22));
//                endTime.setPreferredSize(new java.awt.Dimension(200, 28));
//        getQueryButton = new JButton("Get Interval");
//        GridBagConstraints gbc_getQueryButton = new GridBagConstraints();
//        gbc_getQueryButton.gridwidth = 2;
//        gbc_getQueryButton.insets = new Insets(0, 0, 0, 5);
//        gbc_getQueryButton.fill = GridBagConstraints.HORIZONTAL;
//        gbc_getQueryButton.gridx = 1;
//        gbc_getQueryButton.gridy = 2;
//        panel.add(getQueryButton, gbc_getQueryButton);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 5;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.insets = new Insets(5, 5, 5, 0);
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//
//        // add view options panel
//        simulationMarkerPanel = new ViewOptionsPanel(runs, renderer);
//        simulationMarkerPanel.setEnabled(false);
//        add(simulationMarkerPanel, gridBagConstraints);
//        simulationMarkerPanel.repaint();
//
//
//        // time table panel
//        timeTablePanel = new TimeIntervalTablePanel(runs, modelManager, renderer, simulationMarkerPanel);
//        GridBagConstraints gbc_timeTablePanel = new GridBagConstraints();
//        gbc_timeTablePanel.fill = GridBagConstraints.BOTH;
//        gbc_timeTablePanel.gridwidth = 2;
//        gbc_timeTablePanel.insets = new Insets(0, 0, 5, 0);
//        gbc_timeTablePanel.gridx = 1;
//        gbc_timeTablePanel.gridy = 2;
//        add(timeTablePanel, gbc_timeTablePanel);
//
//
//        getQueryButton.addActionListener(new ActionListener()
//        {
//
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                Date beginTime = (Date) startTime.getModel().getValue();
//                Date stopTime = (Date) endTime.getModel().getValue();
//                double total = (stopTime.getTime()-beginTime.getTime())/ (24.0 * 60.0 * 60.0 * 1000.0);
//                DateTime dtStart = new DateTime(beginTime);
//                DateTime dtEnd = new DateTime(stopTime);
//                DateTime dtStart2 = ISODateTimeFormat.dateTimeParser()
//                        .parseDateTime(dtStart.toString());
//                DateTime dtEnd2 = ISODateTimeFormat.dateTimeParser()
//                        .parseDateTime(dtEnd.toString());
//
//                // TODO check key generation
//                // generate random stateHistoryKey to use for this interval
//                StateHistoryKey key = new StateHistoryKey(runs);
//                StateHistoryModel newInterval = new StateHistoryModel(key, dtStart2, dtEnd2, bodyModel, renderer);
//                if(newInterval.createNewTimeInterval(StateHistoryPanel.this, total, "") > 0) {
//                    timeTablePanel.addIntervalToTable(newInterval, renderer);
//                }
//
//                // once we add an interval, enable the view options
//
//                    simulationMarkerPanel.setEnabled(true);
//
//            }
//        });
//
//        //
//        // time control pane
//        //
//        timeControlPane = new TimeControlPane(modelManager, this);
//        gridBagConstraints_1 = new java.awt.GridBagConstraints();
//        gridBagConstraints_1.gridwidth = 2;
//        gridBagConstraints_1.insets = new Insets(0, 0, 5, 0);
//        gridBagConstraints_1.gridx = 1;
//        gridBagConstraints_1.gridy = 3;
//        gridBagConstraints_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints_1.anchor = java.awt.GridBagConstraints.WEST;
//        add(timeControlPane, gridBagConstraints_1);
//
//
//        // Now create time panels
//        JPanel timeSetPanel = new JPanel();
//        timeSetPanel.setLayout(new BoxLayout(timeSetPanel, BoxLayout.LINE_AXIS));
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 6;
//        gridBagConstraints.gridwidth = 3;
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new Insets(5, 5, 5, 0);
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//
//        add(timeSetPanel, gridBagConstraints);
//
//        //
//        // time query panel
//        //
//
//        JPanel timeQueryPanel = new JPanel();
//        timeQueryPanel.setLayout(new GridLayout(0, 3));
//
//        //
//        // Save time animations
//        //
//
//        saveAnimationButton = new JButton("Save Movie Frames");
//        GridBagConstraints gbc_saveAnimationButton = new GridBagConstraints();
//        gbc_saveAnimationButton.gridwidth = 2;
//        gbc_saveAnimationButton.insets = new Insets(0, 0, 5, 0);
//        gbc_saveAnimationButton.gridx = 1;
//        gbc_saveAnimationButton.gridy = 7;
//        add(saveAnimationButton, gbc_saveAnimationButton);
//        saveAnimationButton.setEnabled(true);
//        saveAnimationButton.addActionListener(new ActionListener()
//        {
//
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                StateHistoryModel currentRun = runs.getCurrentRun();
//                if (currentRun != null)
//                {
//                    currentRun.saveAnimation(StateHistoryPanel.this, startTime.getModel().getValue().toString(), endTime.getModel().getValue().toString());
//                }else{
//                    JOptionPane.showMessageDialog(null, "No History Interval selected.", "Error",
//                            JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//            }
//        });
//
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 8;
//        gridBagConstraints.gridwidth = 3;
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new Insets(5, 5, 0, 0);
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        timeQueryPanel.add(new JLabel());
//        timeQueryPanel.add(new JLabel());
//
//        add(timeQueryPanel, gridBagConstraints);
//    }
//
//
//    //
//    // Operations
//    //
//
//    @Override
//    public void itemStateChanged(ItemEvent e) throws NullPointerException
//    {
//        Object source = e.getItemSelectable();
//        StateHistoryModel currentRun = runs.getCurrentRun();
//
//        //
//        // handles changes in options to show/hide different parts of the model. Ex. pointers, lighting, trajectory
//        //
//
//        try
//        {
//            if(e.getStateChange() == ItemEvent.SELECTED){
//                if(source == showEarthMarker){
//                    currentRun.setActorVisibility("Earth", true);
//                } else if(source == showSunMarker){
//                    currentRun.setActorVisibility("Sun", true);
//                } else if(source == showSpacecraftMarkerHead){
//                    currentRun.setActorVisibility("SpacecraftMarkerHead", true);
//                } else if(source == showSpacecraftMarker){
//                    distanceOptions.setEnabled(true);
//                    currentRun.setDistanceText(distanceOptions.getSelectedItem().toString());
//                    currentRun.setActorVisibility("SpacecraftMarker", true);
//                } else if(source == showLighting){
//                    currentRun.setActorVisibility("Lighting", true);
//                    renderer.setFixedLightDirection(currentRun.getSunPosition());
//                    renderer.setLighting(LightingType.FIXEDLIGHT);
//                } else if(source == showSpacecraftView){
//                    currentRun.setSpacecraftMovement(true);
//                    currentRun.setActorVisibility("SpacecraftMarker", false);
//                    showSpacecraftMarker.setSelected(false);
//                    showSpacecraftMarker.setEnabled(false);
//                    distanceOptions.setEnabled(false);
//                    viewAngleInput.setText(Double.toString(currentRun.getRenderer().getCameraViewAngle()));
//                    renderer.setCameraViewAngle(currentRun.getRenderer().getCameraViewAngle());
//                }else if(source == showEarthView){
//                    if(!showSpacecraftMarker.isEnabled()){
//                        showSpacecraftMarker.setSelected(false);
//                        showSpacecraftMarker.setEnabled(true);
//                        currentRun.setSpacecraftMovement(false);
//                    }
//                    currentRun.setSpacecraftMovement(false);
//                    currentRun.setEarthView(true, showSpacecraftView.isSelected());
//                    viewAngleInput.setText(Double.toString(renderer.getCameraViewAngle()));
//                }else if(source == showSunView){
//                    currentRun.setSpacecraftMovement(false);
//                    currentRun.setEarthView(false, showSpacecraftView.isSelected());
//                    currentRun.setSunView(true, showSpacecraftView.isSelected());
//                    viewAngleInput.setText(Double.toString(renderer.getCameraViewAngle()));
//                }else if(source == showFreeView){
//
//                }else if(source == mapTrajectory){
//                    currentRun.setActorVisibility("Trajectory", true);
//                    showTrajectory.setEnabled(true);
//                    showTrajectory.setSelected(true);
//                }else if(source == showTrajectory){
//                    currentRun.showTrajectory(true);
//                }
//
//            }
//            if(e.getStateChange() == ItemEvent.DESELECTED){
//                if(source == showEarthMarker){
//                    currentRun.setActorVisibility("Earth", false);
//                } else if(source == showSunMarker){
//                    currentRun.setActorVisibility("Sun", false);
//                } else if(source == showSpacecraftMarkerHead){
//                    currentRun.setActorVisibility("SpacecraftMarkerHead", false);
//                } else if(source == showSpacecraftMarker){
//                    distanceOptions.setEnabled(false);
//                    currentRun.setActorVisibility("SpacecraftMarker", false);
//                } else if(source == showLighting){
//                    currentRun.setActorVisibility("Lighting", false);
//                    renderer.setLighting(LightingType.LIGHT_KIT);
//                } else if(source == showSpacecraftView){
//                    currentRun.setSpacecraftMovement(false);
//                    showSpacecraftMarker.setSelected(false);
//                    showSpacecraftMarker.setEnabled(true);
//                    distanceOptions.setEnabled(false);
//                } else if(source == showEarthView){
//                    currentRun.setSpacecraftMovement(false);
//                    currentRun.setEarthView(false, showSpacecraftView.isSelected());
//                } else if(source == showSunView){
//                    currentRun.setSpacecraftMovement(false);
//                    currentRun.setEarthView(false, showSpacecraftView.isSelected());
//                    currentRun.setSunView(false, showSpacecraftView.isSelected());
//                } else if(source == mapTrajectory){
//                    currentRun.setActorVisibility("Trajectory", false);
//                    showTrajectory.setEnabled(false);
//                    showTrajectory.setSelected(false);
//                } else if(source == showTrajectory){
//                    currentRun.showTrajectory(false);
//                }
//            }
//            currentRun.updateActorVisibility();
//        }catch(Exception ex){
//
//        }
//
//    }
//
//    public JPanel getView() { return this; }
//
//    //
//    // used to set the time for the slider and its time fraction.
//    //
//    public void setTimeSlider(double tf){
//        StateHistoryModel currentRun = runs.getCurrentRun();
//        TimeChanger tc = timeControlPane.getTimeChanger();
//        tc.setSliderValue(tf);
//        tc.currentOffsetTime = tf;
//    }
//
//    private int binarySearch(int first, int last, String target, boolean pos){
//        if(first > last){
//            if(pos){
//                return (last + 1) * lineLength;
//            }
//            return (last) * lineLength;
//        }else{
//            int middle = (first+last)/2;
//            int compResult = target.compareTo(readString((middle) * lineLength));
//            if(compResult == 0)
//                return (middle) * lineLength;
//            else if(compResult < 0)
//                return binarySearch(first, middle - 1, target, pos);
//            else
//                return binarySearch(middle + 1, last, target, pos);
//        }
//    }
//
//    private long getBinaryFileLength(){
//        long length = 0;
//        try {
//            RandomAccessFile fileStore = new RandomAccessFile(path, "r");
//            length = fileStore.length()/lineLength;
//            fileStore.close();
//        } catch (Exception e) {
//            return length;
//        }
//        return length;
//    }
//
//    private String readString(int postion){
//        String string = "";
//        try {
//            RandomAccessFile fileStore = new RandomAccessFile(path, "r");
//            fileStore.seek(postion);
//            string = fileStore.readUTF();
//            fileStore.close();
//        } catch (Exception e) {
//            return "";
//        }
//        return string;
//    }
//
//    private double readBinary(int postion){
//        double num = 0;
//        try {
//            RandomAccessFile fileStore = new RandomAccessFile(path, "r");
//            fileStore.seek(postion);
//            num = fileStore.readDouble();
//            fileStore.close();
//        } catch (Exception e) {
//            return 0;
//        }
//        return  num;
//    }
//
//    //
//    // a custom table model that was tried to display map and show options for multiple trajectories. Not used because multiple trajectory showing would mess up the animation.
//    //
//
//    class OptionsTableModel extends DefaultTableModel
//    {
//        public OptionsTableModel(Object[][] data, String[] columnNames)
//        {
//            super(data, columnNames);
//        }
//
//        public boolean isCellEditable(int row, int column)
//        {
//            if (column == 2 && !(boolean)optionsTable.getValueAt(0, 1)) //|| column ==6 || column ==7)
//                return false;
//            else
//                return true;
//        }
//    }
//
//    public void initializeRunList() throws IOException
//    {
//        if (initialized)
//            return;
//
//        MapUtil configMap = new MapUtil(getConfigFilename());
//
//        boolean needToUpgradeConfigFile = false;
//        String[] runNames = configMap.getAsArray(StateHistoryModel.RUN_NAMES);
//        String[] runFilenames = configMap.getAsArray(StateHistoryModel.RUN_FILENAMES);
//        if (runFilenames == null)
//        {
//            // Mark that we need to upgrade config file to latest version
//            // which we'll do at end of function.
//            needToUpgradeConfigFile = true;
//            initialized = true;
//            return;
//        }
//
//        int numRuns = runFilenames.length;
//        for (int i=0; i<numRuns; ++i)
//        {
//            RunInfo runInfo = new RunInfo();
//            runInfo.name = runNames[i];
//            runInfo.runfilename = runFilenames[i];
//
//        }
//
//        if (needToUpgradeConfigFile)
//            updateConfigFile();
//
//        initialized = true;
//    }
//
//}
