package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import vtk.rendering.jogl.vtkJoglPanelComponent;

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
import edu.jhuapl.sbmt.stateHistory.model.interfaces.HasTime;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
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
                StateHistory currentRun = stateHistoryCollection.getCurrentRun();
                if (currentRun != null)
                {
                    currentRun.updateStatusBarPosition(e.getComponent().getWidth(), e.getComponent().getHeight());
                }
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

    public void setModel(HasTime model)
    {
        this.model = model;
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


    public StateHistoryPanel2 getView()
    {
        return view;
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
}
