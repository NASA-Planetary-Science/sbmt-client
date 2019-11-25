package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.HasTime;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable;
import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable.TimeIntervalTableModel;
import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable.columns;
import edu.jhuapl.sbmt.stateHistory.ui.version2.IStateHistoryPanel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryPanel2;

public class StateHistoryController implements TableModelListener, ItemListener, IStateHistoryPanel
{
    public static class RunInfo
    {
        public String name = "";        // name to call this run for display purposes
        public String runfilename = ""; // filename of run on disk

        @Override
        public String toString()
        {
            return name;
        }
    }

    private JTable optionsTable;

    private StateHistoryPanel2 view;
    private ModelManager modelManager;

    private boolean initialized = false;
    private StateHistoryCollection runs;
    private File path;
    final int lineLength = 121;
    private SmallBodyModel bodyModel;
    private SmallBodyViewConfig config;
    private Renderer renderer;
    private vtkJoglPanelComponent renWin;

    //time control pane
    private StateHistoryCollection stateHistoryCollection;
    Date newStart;
    Date newEnd;
    private int finalValue = 900;
    private int defaultValue = 0; // 15;

    public double currentOffsetTime = 0.0;
    private HasTime model;
    private double offsetScale = 1.0; // 0.025;

    private Timer timer;
    public static final int timerInterval = 100;
    private boolean playChecked = false;
    private boolean manualSetTime = false;
    public boolean earthEnabled = true;

    public StateHistoryController(
            final ModelManager modelManager,
            Renderer renderer, boolean earthEnabled)
    {
        view = new StateHistoryPanel2();
        this.earthEnabled = earthEnabled;
        stateHistoryCollection = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);

        this.modelManager = modelManager;
        this.renderer = renderer;
        this.renWin = renderer.getRenderWindowPanel();
        bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
        config = (SmallBodyViewConfig) bodyModel.getConfig();
        this.model = stateHistoryCollection;
        runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);

        try {
            path = FileCache.getFileFromServer(config.timeHistoryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

     // get range of available dates
        DateTime start = ISODateTimeFormat.dateTimeParser().parseDateTime(readString(lineLength));//oldDate.parse(currentRun.getIntervalTime()[0]);
        DateTime end = ISODateTimeFormat.dateTimeParser().parseDateTime(readString((int)getBinaryFileLength()*lineLength-lineLength));
        newStart = start.toDate();
        newEnd = end.toDate();
        createTimer();

        initializeIntervalGenerationPanel();
        initializeIntervalSelectionPanel();
        initializeIntervalPlaybackPanel();
        initializeViewControlPanel();


        renWin.getRenderWindow().AddObserver("EndEvent", this, "updateTimeBarPosition");
        renWin.getComponent().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                updateTimeBarValue();
                updateTimeBarPosition();
                StateHistoryModel currentRun = stateHistoryCollection.getCurrentRun();
                if (currentRun != null)
                {
                    currentRun.updateStatusBarPosition(e.getComponent().getWidth(), e.getComponent().getHeight());
                }
            }
        });
    }

    private void initializeIntervalGenerationPanel()
    {
        view.getAvailableTimeLabel().setEditable(false); // as before
        view.getAvailableTimeLabel().setBackground(null); // this is the same as a JLabel
        view.getAvailableTimeLabel().setBorder(null);
        view.getAvailableTimeLabel().setText(new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(newStart)+ " to\n " + new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(newEnd));


        view.getStartTimeSpinner().setModel(new javax.swing.SpinnerDateModel(new java.util.Date(newStart.getTime()), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getStartTimeSpinner().setEditor(new javax.swing.JSpinner.DateEditor(view.getStartTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getStopTimeSpinner().setModel(new javax.swing.SpinnerDateModel(new java.util.Date(newEnd.getTime()), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getStopTimeSpinner().setEditor(new javax.swing.JSpinner.DateEditor(view.getStopTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS"));


        view.getGetIntervalButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                Date beginTime = (Date) view.getStartTimeSpinner().getModel()
                        .getValue();
                Date stopTime = (Date) view.getStopTimeSpinner().getModel()
                        .getValue();
                double total = (stopTime.getTime() - beginTime.getTime())
                        / (24.0 * 60.0 * 60.0 * 1000.0);
                DateTime dtStart = new DateTime(beginTime);
                DateTime dtEnd = new DateTime(stopTime);
                DateTime dtStart2 = ISODateTimeFormat.dateTimeParser()
                        .parseDateTime(dtStart.toString());
                DateTime dtEnd2 = ISODateTimeFormat.dateTimeParser()
                        .parseDateTime(dtEnd.toString());
                // TODO check key generation
                // generate random stateHistoryKey to use for this interval
                StateHistoryKey key = new StateHistoryKey(runs);
                StateHistoryModel newInterval = new StateHistoryModel(key,
                        dtStart2, dtEnd2, bodyModel, renderer);
                if (newInterval.createNewTimeInterval(
                        StateHistoryController.this, total, "") > 0)
                {
                    view.getTable().addInterval(newInterval, renderer);
                    //                    timeTablePanel.addIntervalToTable(newInterval, renderer);
                }
                // once we add an interval, enable the view options
                //                view.getViewControlPanel().setEnabled(true);
                setViewControlPanelEnabled(true);
                String currentView = (String)view.getViewOptions().getSelectedItem();
                if (currentView.equals(viewChoices.SPACECRAFT.toString()))
                    view.getShowSpacecraft().setEnabled(false); // show spacecraft should be disabled if spacecraft view
                view.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void setViewControlPanelEnabled(boolean enabled)
    {
        view.getDistanceOptions().setEnabled(enabled);
        view.getViewOptions().setEnabled(enabled);
        view.getShowEarthPointer().setEnabled(enabled);
        view.getShowSunPointer().setEnabled(enabled);
        view.getShowSpacecraftMarker().setEnabled(enabled);
        view.getShowSpacecraft().setEnabled(enabled);
        view.getShowLighting().setEnabled(enabled);
        view.getEarthText().setEnabled(enabled);
        view.getSunText().setEnabled(enabled);
        view.getSpacecraftText().setEnabled(enabled);
        view.getEarthSlider().setEnabled(enabled);
        view.getSunSlider().setEnabled(enabled);
        view.getSpacecraftSlider().setEnabled(enabled);
        view.getViewOptions().setEnabled(enabled);
        view.getBtnResetCameraTo().setEnabled(enabled);
        view.getViewInputAngle().setEnabled(enabled);
        view.getSetViewAngle().setEnabled(enabled);
        view.getSaveAnimationButton().setEnabled(enabled);
        view.getLblSelectView().setEnabled(enabled);
        view.getLblVerticalFov().setEnabled(enabled);
        view.getDistanceOptions().setEnabled(enabled);
    }

    private void initializeIntervalSelectionPanel()
    {
        view.setTable(new TimeIntervalTable(runs, bodyModel, renderer));
        view.getTable().getModel().addTableModelListener(this);
        view.getTable().addMouseListener(new TableMouseHandler());
        view.getLoadButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TimeIntervalTableModel model = (TimeIntervalTableModel) view.getTable().getModel();

                JFileChooser fc = new JFileChooser();
                fc.addChoosableFileFilter(new FileNameExtensionFilter("timefiles", "csv"));

                int returnVal = fc.showOpenDialog(view);

                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    File file = fc.getSelectedFile();
                    SmallBodyModel bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
                    model.loadIntervalFromFile(file, bodyModel);
                    setViewControlPanelEnabled(true);

                }
            }
        });
        view.getSaveButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (view.getTable().getSelectedRowCount() == 1)
                {
                    TimeIntervalTableModel model = (TimeIntervalTableModel) view.getTable().getModel();
                    // TODO set default filename to "name" in name column
                    JFileChooser fc = new JFileChooser();

                    int returnVal = fc.showSaveDialog(view);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fc.getSelectedFile();
                        if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("csv")) {
                            // filename is OK as-is
                        } else {
                            // remove the extension (if any) and replace it with ".csv"
                            file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".csv");
                        }
                        model.saveRowToFile(view.getTable().getSelectedRow(), file);
                    }

                }
                else {
                    JOptionPane.showMessageDialog(null, "You must have exactly one row of the table selected to save an interval", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }


            }

        });

        view.getRemoveButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (view.getTable().getSelectedRowCount() != 0)
                {
                    TimeIntervalTableModel model = (TimeIntervalTableModel) view.getTable().getModel();
                    model.removeRows(view.getTable().getSelectedRows());
                }
            }
        });
    }

    public void setModel(HasTime model)
    {
        this.model = model;
    }

    private void initializeIntervalPlaybackPanel()
    {
        final JSlider slider = view.getSlider();
        slider.addChangeListener(new ChangeListener() {         //ADD TO CONTROLLER
            public void stateChanged(ChangeEvent evt) {
                if(slider.getValueIsAdjusting()){
                    int val = slider.getValue();
                    int max = slider.getMaximum();
                    int min = slider.getMinimum();
                    currentOffsetTime = (double)(val - min) / (double)(max-min) * offsetScale;
                    ((HasTime)model).setTimeFraction(currentOffsetTime);
                }
            }
        });

        view.getRewindButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                if(playChecked){
                    try
                    {
                        Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
                        play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
                        Icon playIcon = new ImageIcon(play);
                        view.getPlayButton().setIcon(playIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
                    timer.stop();
                    playChecked = false;
                }

                slider.setValue(defaultValue);
                currentOffsetTime = 0.0;
                if (model != null)
                    model.setTimeFraction(currentOffsetTime);
            }
        });

        view.getFastForwardButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                if(playChecked){
                    try
                    {
                        Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
                        play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
                        Icon playIcon = new ImageIcon(play);
                        view.getPlayButton().setIcon(playIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
                    timer.stop();
                    playChecked = false;
                }

                slider.setValue(finalValue);
                currentOffsetTime = 1.0;
                if (model != null)
                    model.setTimeFraction(currentOffsetTime);

            }
        });

        view.getPlayButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                if(playChecked){
                    try
                    {
                        Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
                        play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
                        Icon playIcon = new ImageIcon(play);
                        view.getPlayButton().setIcon(playIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
                    timer.stop();
                    renderer.setMouseEnabled(true);
                    if (runs.getCurrentRun() != null)
                    {
                        runs.getCurrentRun().updateStatusBarValue("");
                    }
                    playChecked = false;
                }
                else
                {
                    if (view.getTable().getSelectedRowCount() == 0)
                    {
                        JOptionPane.showMessageDialog(null, "Please select a row from the table before playing", "Choose Interval",
                                JOptionPane.OK_OPTION);
                        return;
                    }
                    try
                    {
                        Image pause = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PauseButton.png"));
                        Icon pauseIcon = new ImageIcon(pause);
                        view.getPlayButton().setIcon(pauseIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
                    timer.start();
                    renderer.setMouseEnabled(false);
                    if (runs.getCurrentRun() != null)
                    {
                        runs.getCurrentRun().updateStatusBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
                        runs.getCurrentRun().updateStatusBarValue("Playing (mouse disabled)");
                    }
                    playChecked = true;
                }
            }
        });

        view.getTimeBox().setModel(new SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getTimeBox().setEditor(new javax.swing.JSpinner.DateEditor(view.getTimeBox(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getSetTimeButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistoryModel currentRun = stateHistoryCollection.getCurrentRun();
                if (currentRun != null)
                {
                    Date enteredTime = (Date) view.getTimeBox().getModel().getValue();
                    DateTime dt = new DateTime(enteredTime);
                    DateTime dt1 = ISODateTimeFormat.dateTimeParser()
                            .parseDateTime(dt.toString());
                    boolean success = currentRun.setInputTime(dt1, StateHistoryController.this);
                    if (success) // only call again if the first call was a success
                        currentRun.setInputTime(dt1, StateHistoryController.this); //The method needs to run twice because running once gets it close to the input but not exact. Twice shows the exact time. I don't know why.
                }else{
                    JOptionPane.showMessageDialog(null, "No Time Interval selected.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

            }
        });
    }

    public void createTimer()
    {
        timer = new Timer(timerInterval, new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                double period = model.getPeriod();
                double deltaRealTime = timer.getDelay() / 1000.0;
                double playRate = 1.0;
                try {
                   playRate = Double.parseDouble(view.getRateTextField().getText());
                } catch (Exception ex) { playRate = 1.0; }

                double deltaSimulationTime = deltaRealTime * playRate;
                double deltaOffsetTime = deltaSimulationTime / period;
                //System.out.println("Delta time: " + deltaSimulationTime + " Delta offset time: " + deltaOffsetTime);

                currentOffsetTime += deltaOffsetTime;
                // time looping
                if (currentOffsetTime > 1.0)
                    currentOffsetTime = 0.0;

                model.setTimeFraction(currentOffsetTime);

                int max = view.getSlider().getMaximum();
                int min = view.getSlider().getMinimum();

                int val = (int)Math.round((currentOffsetTime / offsetScale) * ((double)(max - min)) + min);
                view.getSlider().setValue(val);

            }
        });
        timer.setDelay(timerInterval);
    }

    private enum viewChoices {
//      FREE("Free View"),
      SPACECRAFT("Spacecraft View"),
      EARTH("Earth View"),
      SUN("Sun View");

        private final String text;

        private viewChoices(final String text)
        {
            this.text = text;
        }

        public String toString()
        {
            return text;
        }

        public static String[] valuesAsStrings(boolean earthIncluded)
        {
            viewChoices[] values = values();
            if (earthIncluded == false)
            {
                values = new viewChoices[]{viewChoices.SPACECRAFT, viewChoices.SUN};
            }
            String[] asStrings = new String[values.length];
            for (int ix = 0; ix < values.length; ix++) {
                asStrings[ix] = values[ix].toString();
            }
            return asStrings;
        }
    }

    private void initializeViewControlPanel()
    {
        String[] distanceChoices = {"Distance to Center", "Distance to Surface"};
        DefaultComboBoxModel<String> comboModelDistance = new DefaultComboBoxModel<String>(distanceChoices);
        DefaultComboBoxModel<String> comboModelView = new DefaultComboBoxModel<String>(viewChoices.valuesAsStrings(earthEnabled));
        view.getDistanceOptions().setModel(comboModelDistance);
        view.getViewOptions().setModel(comboModelView);
        view.getShowEarthPointer().addItemListener(this);
        view.getShowSunPointer().addItemListener(this);
        view.getShowSpacecraftMarker().addItemListener(this);
        view.getShowSpacecraft().addItemListener(this);
        view.getShowLighting().addItemListener(this);


        view.getViewOptions().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null) { // can't do any view things if we don't have a trajectory / time history
                    String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
                    view.getShowSpacecraft().setEnabled(true);
                    /*if(selectedItem.equals(viewChoices.FREE.toString())){

                    } else*/ if(selectedItem.equals(viewChoices.EARTH.toString())){
                        currentRun.setSpacecraftMovement(false);
                        currentRun.setEarthView(true, view.getShowSpacecraft().isSelected());
                        view.getViewInputAngle().setText(Double.toString(renderer.getCameraViewAngle()));
                    } else if(selectedItem.equals(viewChoices.SUN.toString())){
                        currentRun.setSpacecraftMovement(false);
                        currentRun.setEarthView(false, view.getShowSpacecraft().isSelected());
                        currentRun.setSunView(true, view.getShowSpacecraft().isSelected());
                        view.getViewInputAngle().setText(Double.toString(renderer.getCameraViewAngle()));
                    } else if(selectedItem.equals(viewChoices.SPACECRAFT.toString())){
                       setSpacecraftView(currentRun);
                    }
                }
            }
        });

        view.getViewOptions().setSelectedIndex(0);

        view.getBtnResetCameraTo().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if( currentRun != null){
                    currentRun.getRenderer().setCameraFocalPoint(new double[] {0,0,0});
                }
            }
        });


        view.getDistanceOptions().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
                if(selectedItem.equals("Distance to Center")){
                    currentRun.setDistanceText("Distance to Center");
                }else if(selectedItem.equals("Distance to Surface")){
                    currentRun.setDistanceText("Distance to Surface");
                }
            }
        });

        view.getEarthSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider) e.getSource();
                    currentRun.setEarthPointerSize(slider.getValue());
                }

            }
        });

        view.getSunSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider) e.getSource();
                    currentRun.setSunPointerSize(slider.getValue());
                }

            }
        });

        view.getSpacecraftSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider)e.getSource();
                    currentRun.setSpacecraftPointerSize(slider.getValue());
                }
            }
        });

        view.getSetViewAngle().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(e.getSource() == view.getSetViewAngle()){
                    StateHistoryModel currentRun = runs.getCurrentRun();
                    if(!(Double.parseDouble(view.getViewInputAngle().getText())>120.0 || Double.parseDouble(view.getViewInputAngle().getText())<1.0)){
                        renderer.setCameraViewAngle(Double.parseDouble(view.getViewInputAngle().getText()));
                    }else if(Double.parseDouble(view.getViewInputAngle().getText())>120){
                        view.getViewInputAngle().setText("120.0");
                        currentRun.setViewAngle(120.0);
                    }else{
                        view.getViewInputAngle().setText("1.0");
                        currentRun.setViewAngle(1.0);
                    }
                }

            }
        });

        view.getSaveAnimationButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    DateTime startTime = currentRun.getStartTime();
                    DateTime endTime = currentRun.getEndTime();
                    currentRun.saveAnimation(StateHistoryController.this,
                            "" + startTime, "" + endTime);
                    view.setCursor(Cursor.getDefaultCursor());
//                    currentRun.saveAnimation(StateHistoryController.this, view.getStartTimeSpinner().getModel().getValue().toString(), view.getStopTimeSpinner().getModel().getValue().toString());
                }else
                {
                    JOptionPane.showMessageDialog(null, "No History Interval selected.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

            }
        });

    }

    private void setSpacecraftView(StateHistoryModel currentRun)
    {
        currentRun.setEarthView(false, view.getShowSpacecraft().isSelected());
        currentRun.setSunView(false, view.getShowSpacecraft().isSelected());
        currentRun.setSpacecraftMovement(true);
        currentRun.setActorVisibility("Spacecraft", false);
        view.getShowSpacecraft().setEnabled(false);
        view.getDistanceOptions().setEnabled(false);
        view.getViewInputAngle().setText(Double.toString(currentRun.getRenderer().getCameraViewAngle()));
        renderer.setCameraViewAngle(currentRun.getRenderer().getCameraViewAngle());
        view.getViewOptions().setSelectedIndex(0);
    }

    public StateHistoryPanel2 getView()
    {
        return view;
    }

    private void updateTimeBarValue()
    {
        if (runs != null)
        {
            StateHistoryModel currentRun = runs.getCurrentRun();
            if (currentRun != null)
            {
                try
                {
                    Double time = currentRun.getTime();
                    currentRun.updateTimeBarValue(time);
                }catch(Exception ex){

                }
            }
        }
    }

    public void updateTimeBarPosition()
    {
        if (runs != null)
        {
            StateHistoryCollection runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
            StateHistoryModel currentRun = runs.getCurrentRun();
            if (currentRun != null)
                currentRun.updateTimeBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
        }
    }

    private String getConfigFilename()
    {
        return modelManager.getPolyhedralModel().getConfigFilename();
    }

    private RunInfo getRunInfo(int index)
    {
        return (RunInfo)((DefaultListModel)optionsTable.getModel()).get(index);
    }

    private String getFileName(RunInfo runInfo)
    {
        //        return getCustomDataFolder() + File.separator + runInfo.runfilename;
        return runInfo.runfilename;
    }

    private String getFileName(int index)
    {
        return getFileName(getRunInfo(index));
    }

    private void updateConfigFile()
    {
        MapUtil configMap = new MapUtil(getConfigFilename());

        String runNames = "";
        String runFilenames = "";

        DefaultListModel runListModel = (DefaultListModel)optionsTable.getModel();
        for (int i=0; i<runListModel.size(); ++i)
        {
            RunInfo runInfo = (RunInfo)runListModel.get(i);

            runFilenames += runInfo.runfilename;
            runNames += runInfo.name;

            if (i < runListModel.size()-1)
            {
                runNames += CustomShapeModel.LIST_SEPARATOR;
                runFilenames += CustomShapeModel.LIST_SEPARATOR;
            }
        }

        Map<String, String> newMap = new LinkedHashMap<String, String>();

        newMap.put(StateHistoryModel.RUN_NAMES, runNames);
        newMap.put(StateHistoryModel.RUN_FILENAMES, runFilenames);

        configMap.put(newMap);
    }

    @Override
    public void itemStateChanged(ItemEvent e) throws NullPointerException
    {
        Object source = e.getItemSelectable();
        StateHistoryModel currentRun = runs.getCurrentRun();
        JCheckBox showEarthMarker = view.getShowEarthPointer();
        JCheckBox showSunMarker = view.getShowSunPointer();
        JCheckBox showSpacecraft = view.getShowSpacecraft();
        JCheckBox showSpacecraftMarker = view.getShowSpacecraftMarker();
        JComboBox distanceOptions = view.getDistanceOptions();
        JCheckBox showLighting = view.getShowLighting();
        JTextField viewAngleInput = view.getViewInputAngle();
        JSlider earthSlider = view.getEarthSlider();
        JSlider sunSlider = view.getSunSlider();
        JSlider spacecraftSlider = view.getSpacecraftSlider();
        JLabel earthText = view.getEarthText();
        JLabel sunText = view.getSunText();
        JLabel spacecraftText = view.getSpacecraftText();
        //
        // handles changes in options to show/hide different parts of the model. Ex. pointers, lighting, trajectory
        //

        try
        {
            if(e.getStateChange() == ItemEvent.SELECTED){
                if(source == showEarthMarker){
                    currentRun.setActorVisibility("Earth", true);
                    earthSlider.setEnabled(true);
                    earthText.setEnabled(true);
                } else if(source == showSunMarker){
                    currentRun.setActorVisibility("Sun", true);
                    sunSlider.setEnabled(true);
                    sunText.setEnabled(true);
                } else if(source == showSpacecraftMarker){
                    currentRun.setActorVisibility("SpacecraftMarker", true);
                    spacecraftSlider.setEnabled(true);
                    spacecraftText.setEnabled(true);
                } else if(source == showSpacecraft){
                    distanceOptions.setEnabled(true);
                    currentRun.setDistanceText(distanceOptions.getSelectedItem().toString());
                    currentRun.setActorVisibility("Spacecraft", true);
                } else if(source == showLighting){
                    currentRun.setActorVisibility("Lighting", true);
                    renderer.setFixedLightDirection(currentRun.getSunPosition());
                    renderer.setLighting(LightingType.FIXEDLIGHT);
                }

            }
            if(e.getStateChange() == ItemEvent.DESELECTED){
                if(source == showEarthMarker){
                    currentRun.setActorVisibility("Earth", false);
                    earthSlider.setEnabled(false);
                    earthText.setEnabled(false);
                } else if(source == showSunMarker){
                    currentRun.setActorVisibility("Sun", false);
                    sunSlider.setEnabled(false);
                    sunText.setEnabled(false);
                } else if(source == showSpacecraftMarker){
                    currentRun.setActorVisibility("SpacecraftMarker", false);
                    spacecraftSlider.setEnabled(false);
                    spacecraftText.setEnabled(false);
                } else if(source == showSpacecraft){
                    distanceOptions.setEnabled(false);
                    currentRun.setActorVisibility("Spacecraft", false);
                } else if(source == showLighting){
                    currentRun.setActorVisibility("Lighting", false);
                    renderer.setLighting(LightingType.LIGHT_KIT);
                }
            }
            currentRun.updateActorVisibility();
        }catch(Exception ex){

        }


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
////                } else if(source == showSpacecraftView){
////                    currentRun.setSpacecraftMovement(true);
////                    currentRun.setActorVisibility("SpacecraftMarker", false);
////                    showSpacecraftMarker.setSelected(false);
////                    showSpacecraftMarker.setEnabled(false);
////                    distanceOptions.setEnabled(false);
////                    viewAngleInput.setText(Double.toString(currentRun.getRenderer().getCameraViewAngle()));
////                    renderer.setCameraViewAngle(currentRun.getRenderer().getCameraViewAngle());
////                }else if(source == showEarthView){
////                    if(!showSpacecraftMarker.isEnabled()){
////                        showSpacecraftMarker.setSelected(false);
////                        showSpacecraftMarker.setEnabled(true);
////                        currentRun.setSpacecraftMovement(false);
////                    }
////                    currentRun.setSpacecraftMovement(false);
////                    currentRun.setEarthView(true);
////                    viewAngleInput.setText(Double.toString(renderer.getCameraViewAngle()));
////                }else if(source == showSunView){
////                    currentRun.setSpacecraftMovement(false);
////                    currentRun.setEarthView(false);
////                    currentRun.setSunView(true);
////                    viewAngleInput.setText(Double.toString(renderer.getCameraViewAngle()));
////                }else if(source == showFreeView){
////
////                }else if(source == mapTrajectory){
////                    currentRun.setActorVisibility("Trajectory", true);
////                    showTrajectory.setEnabled(true);
////                    showTrajectory.setSelected(true);
////                }else if(source == showTrajectory){
////                    currentRun.showTrajectory(true);
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
////                } else if(source == showSpacecraftView){
////                    currentRun.setSpacecraftMovement(false);
////                    showSpacecraftMarker.setSelected(false);
////                    showSpacecraftMarker.setEnabled(true);
////                    distanceOptions.setEnabled(false);
////                } else if(source == showEarthView){
////                    currentRun.setSpacecraftMovement(false);
////                    currentRun.setEarthView(false);
////                } else if(source == showSunView){
////                    currentRun.setSpacecraftMovement(false);
////                    currentRun.setEarthView(false);
////                    currentRun.setSunView(false);
////                } else if(source == mapTrajectory){
////                    currentRun.setActorVisibility("Trajectory", false);
////                    showTrajectory.setEnabled(false);
////                    showTrajectory.setSelected(false);
////                } else if(source == showTrajectory){
////                    currentRun.showTrajectory(false);
//                }
//            }
//            currentRun.updateActorVisibility();
//        }catch(Exception ex){
//
//        }

    }

    //
    // used to set the time for the slider and its time fraction.
    //
    public void setTimeSlider(double tf){
//        StateHistoryModel currentRun = runs.getCurrentRun();
//        TimeChanger tc = timeControlPane.getTimeChanger();
//        tc.setSliderValue(tf);
        setSliderValue(tf);
        currentOffsetTime = tf;
    }

    public void setSliderValue(double tf){
        manualSetTime = true;
        int max = view.getSlider().getMaximum();

        int val = (int)Math.round(max * tf);

        view.getSlider().setValue(val);
    }

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

    private long getBinaryFileLength(){
        long length = 0;
        try {
            RandomAccessFile fileStore = new RandomAccessFile(path, "r");
            length = fileStore.length()/lineLength;
            fileStore.close();
        } catch (Exception e) {
            return length;
        }
        return length;
    }

    private String readString(int postion){
        String string = "";
        try {
            RandomAccessFile fileStore = new RandomAccessFile(path, "r");
            fileStore.seek(postion);
            string = fileStore.readUTF();
            fileStore.close();
        } catch (Exception e) {
            return "";
        }
        return string;
    }

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
    // a custom table model that was tried to display map and show options for multiple trajectories. Not used because multiple trajectory showing would mess up the animation.
    //

    class OptionsTableModel extends DefaultTableModel
    {
        public OptionsTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            if (column == 2 && !(boolean)optionsTable.getValueAt(0, 1)) //|| column ==6 || column ==7)
                return false;
            else
                return true;
        }
    }

    public void initializeRunList() throws IOException
    {
        if (initialized)
            return;

        MapUtil configMap = new MapUtil(getConfigFilename());

        boolean needToUpgradeConfigFile = false;
        String[] runNames = configMap.getAsArray(StateHistoryModel.RUN_NAMES);
        String[] runFilenames = configMap.getAsArray(StateHistoryModel.RUN_FILENAMES);
        if (runFilenames == null)
        {
            // Mark that we need to upgrade config file to latest version
            // which we'll do at end of function.
            needToUpgradeConfigFile = true;
            initialized = true;
            return;
        }

        int numRuns = runFilenames.length;
        for (int i=0; i<numRuns; ++i)
        {
            RunInfo runInfo = new RunInfo();
            runInfo.name = runNames[i];
            runInfo.runfilename = runFilenames[i];

        }

        if (needToUpgradeConfigFile)
            updateConfigFile();

        initialized = true;
    }

    @Override
    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        int col = e.getColumn();

        if (runs == null || row + 1 > runs.size()) { // adding a new row
            // do nothing, we just added a row.
        }
        else {
            StateHistoryKey key = runs.getKeyFromRow(row);
            StateHistoryModel currentRun = runs.getRunFromRow(row);

            if (col == columns.MAP.ordinal()) { // map trajectory
                if ((Boolean)view.getTable().getValueAt(row, col)) {
                    currentRun.setActorVisibility("Trajectory", true);
                    currentRun.showTrajectory(((Boolean)view.getTable().getValueAt(row, columns.SHOW.ordinal())));
                    view.getViewControlPanel().setEnabled(true);
                }
                else {
                    currentRun.setActorVisibility("Trajectory", false);
                    view.getTable().setValueAt(false, row, columns.SHOW.ordinal());
                    view.getViewControlPanel().setEnabled(false);
                }
            }
            else if (col == columns.SHOW.ordinal()) // show trajectory
            {
                if ((Boolean)view.getTable().getValueAt(row, columns.MAP.ordinal()))
                {
                    currentRun.setActorVisibility("Trajectory", true);

                }
                if ((Boolean)view.getTable().getValueAt(row, col))
                {
                    runs.setCurrentRun(key);
                    currentRun = runs.getCurrentRun();
                    currentRun.showTrajectory(true);
//                    viewOptionsPanel.setEnabled(true);
                    // uncheck show for all other rows
                    int numRows = view.getTable().getRowCount();
                    for (int iRow = 0; iRow < numRows; iRow++)
                    {
                        if (iRow != row)
                            view.getTable().setValueAt(false, iRow, columns.SHOW.ordinal());
                    }
                    setSpacecraftView(runs.getCurrentRun());
                    return;
                }
                else // hide trajectory
                {
                    currentRun.showTrajectory(false);
                    // TODO uncheck all of the viewOptions
                    // (show spacecraft, show lighting, etc) ??
//                    viewOptionsPanel.setEnabled(false);
                }
            } else if (col == columns.COLOR.ordinal()){
                Color newColor = (Color)view.getTable().getValueAt(row, col);
                currentRun.setTrajectoryColor(new double[]{newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha()});
            } else if (col == columns.NAME.ordinal()) {
                currentRun.setTrajectoryName((String)view.getTable().getValueAt(row, col));
            } else if (col == columns.DESC.ordinal()) {
                currentRun.setDescription((String)view.getTable().getValueAt(row, col));
            } else if (col == columns.LINE.ordinal()) {
                currentRun.setTrajectoryLineThickness(Double.parseDouble((String)view.getTable().getValueAt(row, col)));
            }
        }
    }

    class TableMouseHandler extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            TimeIntervalTable timeTable = view.getTable();
            int row = timeTable.rowAtPoint(e.getPoint());
            int col = timeTable.columnAtPoint(e.getPoint());

            if (e.getClickCount() == 1 && row >=0)
            { // clicked any row, set currentRun to that row (all view options and such)
                runs.setCurrentRun(runs.getKeyFromRow(row));
            }

            if (e.getClickCount() == 2 && row >= 0 && col == columns.COLOR.ordinal())
            {
                StateHistoryKey key = runs.getKeyFromRow(row);
                StateHistoryModel currentRun = runs.getRun(key);
                double[] currColor = currentRun.getTrajectoryColor();
                Color color = ColorChooser.showColorChooser(
                        JOptionPane.getFrameForComponent(timeTable),
                        new int[]{(int) currColor[0], (int)currColor[1], (int)currColor[2], (int)currColor[3]});

                if (color == null)
                    return;

                int[] c = new int[4];
                c[0] = color.getRed();
                c[1] = color.getGreen();
                c[2] = color.getBlue();
                c[3] = color.getAlpha();

                timeTable.setValueAt(new Color(c[0], c[1], c[2], c[3]), row, columns.COLOR.ordinal());
            }
        }
    }
}
