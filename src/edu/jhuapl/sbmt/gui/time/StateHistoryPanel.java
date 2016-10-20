/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomImageLoaderPanel.java
 *
 * Created on Jun 5, 2012, 3:56:56 PM
 */
package edu.jhuapl.sbmt.gui.time;

import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;

import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.jogl.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.gui.time.StateHistoryImporterDialog.RunInfo;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.model.time.StateHistoryCollection;
import edu.jhuapl.sbmt.model.time.StateHistoryModel;
import edu.jhuapl.sbmt.model.time.StateHistoryModel.StateHistoryKey;
import edu.jhuapl.sbmt.model.time.StateHistoryModel.StateHistorySource;
import edu.jhuapl.sbmt.model.time.SurfacePatch;
import edu.jhuapl.sbmt.model.time.Trajectory;

import nom.tam.fits.FitsException;


public class StateHistoryPanel extends javax.swing.JPanel implements PropertyChangeListener
{

    private ModelManager modelManager;
    private StateHistoryPopupMenu runPopupMenu;
    private boolean initialized = false;
    private StateHistoryCollection runs;

    private javax.swing.JScrollPane simulationRunScrollPane;
    private javax.swing.JList simulationRunList;

    private javax.swing.JScrollPane trajectoryScrollPane;
    private javax.swing.JList trajectoryList;

//    private javax.swing.JScrollPane areaCalculationScrollPane;
//    private javax.swing.JList areaCalculationList;
//
//    private javax.swing.JScrollPane surfacePatchPane;
//    private javax.swing.JList surfacePatchList;

    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;

    private javax.swing.JLabel simulationRunLabel;
    private javax.swing.JLabel trajectoryLabel;
//    private javax.swing.JLabel areaCalculationLabel;
//    private javax.swing.JLabel surfacePatchLabel;

//    private SurfaceDataPane surfaceDataPane;

    private javax.swing.JPanel simulationRunButtonPanel;
//    private javax.swing.JPanel jPanel2;

    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton removeAllButton;

    private Renderer renderer;
//    private vtkRenderWindowPanel renWin;
    private vtksbmtJoglCanvas renWin;
    private vtkCellPicker smallBodyCellPicker; // only includes small body prop

    /** Creates new form CustomImageLoaderPanel */
    public StateHistoryPanel(
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            final PickManager pickManager,
            Renderer renderer)
    {
        this.modelManager = modelManager;
        this.renderer = renderer;
        this.renWin = renderer.getRenderWindowPanel();

        initComponents();

        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        simulationRunList.setModel(new DefaultListModel());

        runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
        runPopupMenu = new StateHistoryPopupMenu(runs, infoPanelManager, renderer, this);

        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        smallBodyCellPicker = new vtkCellPicker();
        smallBodyCellPicker.PickFromListOn();
        smallBodyCellPicker.InitializePickList();
        List<vtkProp> actors = smallBodyModel.getProps();
        vtkPropCollection smallBodyPickList = smallBodyCellPicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        for (vtkProp act : actors)
        {
            smallBodyCellPicker.AddPickList(act);
        }
        smallBodyCellPicker.AddLocator(smallBodyModel.getCellLocator());

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                try
                {
                    initializeRunList();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });


        // We need to update the scale bar whenever there is a render or whenever
        // the window gets resized. Although resizing a window results in a render,
        // we still need to listen to a resize event since only listening to render
        // results in the scale bar not being positioned correctly when during the
        // resize for some reason. Thus we need to register a component
        // listener on the renderer panel as well to listen explicitly to resize events.
        // Note also that this functionality is in this class since picking is required
        // to compute the value of the scale bar.
        renWin.getRenderWindow().AddObserver("EndEvent", this, "updateTimeBarPosition");
        renWin.getComponent().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                updateTimeBarValue();
                updateTimeBarPosition();
            }
        });

    }

    /**
     * Computes the size of a pixel in body fixed coordinates. This is only meaningful
     * when the user is zoomed in a lot. To compute a result all 4 corners of the
     * view window must intersect the asteroid.
     *
     * @return
     */
//    private double computeSizeOfPixel()
//    {
//        // Do a pick at each of the 4 corners of the renderer
//        long currentTime = System.currentTimeMillis();
//        int width = renWin.getComponent().getWidth();
//        int height = renWin.getComponent().getHeight();
//
//        int[][] corners = { {0, 0}, {width-1, 0}, {width-1, height-1}, {0, height-1} };
//        double[][] points = new double[4][3];
//        for (int i=0; i<4; ++i)
//        {
//            int pickSucceeded = doPick(currentTime, corners[i][0], corners[i][1], smallBodyCellPicker, renWin);
//
//            if (pickSucceeded == 1)
//            {
//                points[i] = smallBodyCellPicker.GetPickPosition();
//            }
//            else
//            {
//                return -1.0;
//            }
//        }
//
//        // Compute the scale if all 4 points intersect by averaging the distance of all 4 sides
//        double bottom = MathUtil.distanceBetweenFast(points[0], points[1]);
//        double right  = MathUtil.distanceBetweenFast(points[1], points[2]);
//        double top    = MathUtil.distanceBetweenFast(points[2], points[3]);
//        double left   = MathUtil.distanceBetweenFast(points[3], points[0]);
//
//        double sizeOfPixel =
//                ( bottom / (double)(width-1)  +
//                  right  / (double)(height-1) +
//                  top    / (double)(width-1)  +
//                  left   / (double)(height-1) ) / 4.0;
//
//        return sizeOfPixel;
//    }

    private void updateTimeBarValue()
    {
        if (runs != null)
        {
            StateHistoryModel currentRun = runs.getCurrentRun();
            if (currentRun != null)
            {
                Double time = currentRun.getTime();
                currentRun.updateTimeBarValue(time);
            }
        }
    }

    private void updateScalarBar()
    {
        StateHistoryModel currentRun = runs.getCurrentRun();
        if (currentRun != null)
        {
            currentRun.updateScalarBar();
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

    private static volatile boolean pickingEnabled = true;

    public static final double DEFAULT_PICK_TOLERANCE = 0.002;

    private double pickTolerance = DEFAULT_PICK_TOLERANCE;

    protected int doPick(MouseEvent e, vtkCellPicker picker, vtksbmtJoglCanvas renWin)
    {
        return doPick(e.getWhen(), e.getX(), e.getY(), picker, renWin);
    }

    protected int doPick(final long when, int x, int y, vtkCellPicker picker, vtksbmtJoglCanvas renWin)
    {
        if (pickingEnabled == false)
            return 0;

        // Don't do a pick if the event is more than a third of a second old
        final long currentTime = System.currentTimeMillis();

        //System.err.println("elapsed time " + (currentTime - when));
        if (currentTime - when > 333)
            return 0;

//        renWin.getVTKLock().lock();
//        picker.SetTolerance(pickTolerance);
//        int pickSucceeded = picker.Pick(x, renWin.getHeight()-y-1, 0.0, renWin.GetRenderer());
//        renWin.getVTKLock().unlock();
//        return pickSucceeded;

        int pickSucceeded = 0;
        try
        {
            renWin.getComponent().getContext().makeCurrent();
            renWin.getVTKLock().lock();
            // Note that on some displays, such as a retina display, the height used by
            // OpenGL is different than the height used by Java. Therefore we need
            // scale the mouse coordinates to get the right position for OpenGL.
//            double openGlHeight = renWin.getComponent().getSurfaceHeight();
            double openGlHeight = renWin.getComponent().getHeight();
            double javaHeight = renWin.getComponent().getHeight();
            double scale = openGlHeight / javaHeight;
//            pickSucceeded = picker.Pick(scale*e.getX(), scale*(javaHeight-e.getY()-1), 0.0, renWin.getRenderer());
            pickSucceeded = picker.Pick(scale*x, scale*(javaHeight-y-1), 0.0, renWin.getRenderer());
            renWin.getVTKLock().unlock();
        }
        finally
        {
            renWin.getComponent().getContext().release();
        }

        return pickSucceeded;


    }



    private String getCustomDataFolder()
    {
        return modelManager.getPolyhedralModel().getCustomDataFolder();
    }

    private String getConfigFilename()
    {
        return modelManager.getPolyhedralModel().getConfigFilename();
    }

    private void initializeRunList() throws IOException
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

            ((DefaultListModel)simulationRunList.getModel()).addElement(runInfo);
        }

        if (needToUpgradeConfigFile)
            updateConfigFile();

        initialized = true;
    }

    private void saveStateHistory(int index, RunInfo oldRunInfo, RunInfo newRunInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

            // If newRunInfo.runfilename is null, that means we are in edit mode
            // and should continue to use the existing run
            if (newRunInfo.runfilename == null)
            {
                newRunInfo.runfilename = oldRunInfo.runfilename;
            }
            else
            {
                System.out.println("Added new time interval run: " + newRunInfo.runfilename);
            }

        DefaultListModel model = (DefaultListModel)simulationRunList.getModel();
        if (index >= model.getSize())
        {
            model.addElement(newRunInfo);
        }
        else
        {
            model.set(index, newRunInfo);
        }

        updateConfigFile();
    }

    private RunInfo getRunInfo(int index)
    {
        return (RunInfo)((DefaultListModel)simulationRunList.getModel()).get(index);
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

    private StateHistoryKey getRunKey(String filename)
    {
        return new StateHistoryKey(filename, StateHistorySource.CLIPPER);
    }

    private StateHistoryKey getRunKey(int i)
    {
        return getRunKey(getFileName(getRunInfo(i)));
    }

    /**
     * This function unmaps the run from the renderer and maps it again,
     * if it is currently shown.
     * @throws IOException
     * @throws FitsException
     */
    private void remapRunToRenderer(int index) throws FitsException, IOException
    {
//        RunInfo runInfo = (RunInfo)((DefaultListModel)runList.getModel()).get(index);
//        String filename = getCustomDataFolder() + File.separator + runInfo.runfilename;
//        StateHistoryKey runKey = new StateHistoryKey(filename, StateHistorySource.CLIPPER);
        StateHistoryKey runKey = getRunKey(index);

        // Remap the run on the renderer
        if (runs.containsRun(runKey))
        {
            runs.removeRun(runKey);
            runs.addRun(runKey, renderer);
        }
    }

    private void removeAllRunsFromRenderer()
    {
        runs.removeRuns(StateHistorySource.CLIPPER);
    }

    private void removeRun(int index)
    {
        StateHistoryKey runKey = getRunKey(index);
        runs.removeRun(runKey);

        ((DefaultListModel)simulationRunList.getModel()).remove(index);
    }

    private void moveDown(int i)
    {
        DefaultListModel model = (DefaultListModel)simulationRunList.getModel();

        if (i >= model.getSize())
            return;

        Object o = model.get(i);

        model.remove(i);
        model.add(i+1, o);
    }

    private void updateConfigFile()
    {
        MapUtil configMap = new MapUtil(getConfigFilename());

        String runNames = "";
        String runFilenames = "";

        DefaultListModel runListModel = (DefaultListModel)simulationRunList.getModel();
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

    private void runListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = simulationRunList.locationToIndex(e.getPoint());

            if (index >= 0 && simulationRunList.getCellBounds(index, index).contains(e.getPoint()))
            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!simulationRunList.isSelectedIndex(index))
                {
                    simulationRunList.clearSelection();
                    simulationRunList.setSelectedIndex(index);
                }

                int[] selectedIndices = simulationRunList.getSelectedIndices();
                ArrayList<StateHistoryKey> runKeys = new ArrayList<StateHistoryKey>();
                for (int selectedIndex : selectedIndices)
                {
                    StateHistoryKey runKey = getRunKey(selectedIndex);
                    runKeys.add(runKey);
                }
                runPopupMenu.setCurrentRuns(runKeys);
                runPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
//        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
//        {
//            PickEvent e = (PickEvent)evt.getNewValue();
//            Model model = modelManager.getModel(e.getPickedProp());
//            if (model instanceof StateHistoryCollection)
//            {
//                String name = null;
//                System.out.println("Model Picked: " + evt.getPropertyName());
//
//                StateHistoryKey runKey = ((StateHistoryCollection)model).getRun((vtkActor)e.getPickedProp()).getKey();
//                name = runKey.name;
////                System.out.println("Picked " + runKey.name);
//                StateHistory run = runs.getRun(runKey);
//                int cellId = e.getPickedCellId();
//                vtkProp prop = e.getPickedProp();
//
////                Trajectory traj = run.getTrajectoryByCellId(cellId);
//                Trajectory traj = run.getTrajectory(prop);
//                if (traj != null)
//                {
//                    String trajectoryName = traj.getName();
//
//                    int idx = -1;
//                    int size = trajectoryList.getModel().getSize();
//                    for (int i=0; i<size; ++i)
//                    {
//                        String trajname = (String)((ListModel)trajectoryList.getModel()).getElementAt(i);
//                        if (trajname.equals(trajectoryName))
//                        {
//                            idx = i;
//                            System.out.println(", " + idx +  " trajectory: " + name);
//                            break;
//                        }
//                    }
//
//                    StateHistory currentRun = runs.getCurrentRun();
//                    if (currentRun != null)
//                    {
//                        Trajectory selectedTrajectory = currentRun.getTrajectoryByIndex(idx);
//                        System.out.println("Selected Trajectory " + selectedTrajectory.getName());
//                        currentRun.setCurrentTrajectoryIndex(idx);
//                        currentRun.setShowSpacecraft(true);
//                        currentRun.setTimeFraction(0.0);
//                    }
//                }
//                if (idx >= 0)
//                {
//                    passList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = runList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        runList.scrollRectToVisible(cellBounds);
//                }
//            }
//        }
    }

    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints;

        //
        // Simulation Run list
        //

        // simulation run list label
        simulationRunLabel = new JLabel();
        simulationRunLabel.setText("History");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(simulationRunLabel, gridBagConstraints);

        // run JList
        simulationRunScrollPane = new javax.swing.JScrollPane();

        simulationRunList = new javax.swing.JList();
        simulationRunList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                runListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                runListMouseReleased(evt);
            }
        });
        simulationRunList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                simulationRunListValueChanged(evt);
            }
        });

        simulationRunScrollPane.setViewportView(simulationRunList);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(simulationRunScrollPane, gridBagConstraints);

        //
        // Trajectories list
        //

        // trajectory list label
        trajectoryLabel = new JLabel();
        trajectoryLabel.setText("Time Interval");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(trajectoryLabel, gridBagConstraints);

        // trajectory list JList
        trajectoryScrollPane = new javax.swing.JScrollPane();

        trajectoryList = new javax.swing.JList();
        trajectoryList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                trajectoryListValueChanged(evt);
            }
        });
        trajectoryList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
              trajectoryList = (JList) mouseEvent.getSource();
              if (mouseEvent.getClickCount() == 2) {
                  trajectoryListDoubleClicked(mouseEvent);
              }
            }
          });

        trajectoryScrollPane.setViewportView(trajectoryList);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(trajectoryScrollPane, gridBagConstraints);

        //
        // run list buttons
        //

        // create the button panel
        simulationRunButtonPanel = new javax.swing.JPanel();
        simulationRunButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));

//        jPanel2 = new javax.swing.JPanel();
//        jPanel2.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));

        // create the buttons
        newButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();

        // new
        newButton.setText("New...");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        simulationRunButtonPanel.add(newButton);

        // edit
        editButton.setText("Edit...");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        simulationRunButtonPanel.add(editButton);

//        // move up
//        moveUpButton.setText("Move Up");
//        moveUpButton.setEnabled(false);
//        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                moveUpButtonActionPerformed(evt);
//            }
//        });
//        jPanel1.add(moveUpButton);
//
//        // move down
//        moveDownButton.setText("Move Down");
//        moveDownButton.setEnabled(false);
//        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                moveDownButtonActionPerformed(evt);
//            }
//        });
//        jPanel1.add(moveDownButton);
//
        // delete from list
        deleteButton.setText("Remove");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        simulationRunButtonPanel.add(deleteButton);

        // add the button panel 1
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(simulationRunButtonPanel, gridBagConstraints);

        //
        // Area Calculation List
        //

        // area calculation label
//        areaCalculationLabel = new JLabel();
//        areaCalculationLabel.setText("Area Calculation");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 5;
//        gridBagConstraints.gridwidth = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        add(areaCalculationLabel, gridBagConstraints);
//
//        // Area Calculation scroll pane and list
//        areaCalculationScrollPane = new javax.swing.JScrollPane();
//
//        // create JList and bind to model
//        areaCalculationList = new javax.swing.JList();
////        TorsoBodyModel torsoBodyModel = (TorsoBodyModel)modelManager.getSmallBodyModel();
////        torsoBodyModel.getProps();
////        organList.setModel(torsoBodyModel);
//
//        // add listeners
//        areaCalculationList.addListSelectionListener(new ListSelectionListener() {
//            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
//                areaCalculationListValueChanged(evt);
//            }
//        });
//
//        areaCalculationList.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent mouseEvent) {
//                areaCalculationList = (JList) mouseEvent.getSource();
//              if (mouseEvent.getClickCount() == 2) {
//                  areaCalculationListDoubleClicked(mouseEvent);
//              }
//            }
//          });
//
//        areaCalculationScrollPane.setViewportView(areaCalculationList);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 6;
//        gridBagConstraints.gridwidth = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//        add(areaCalculationScrollPane, gridBagConstraints);
//
//        //
//        // SurfacePatch List
//        //
//
//        // label
//        surfacePatchLabel = new JLabel();
//        surfacePatchLabel.setText("Images");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 5;
//        gridBagConstraints.gridwidth = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        add(surfacePatchLabel, gridBagConstraints);
//
//        // scroll pane and list
//        surfacePatchPane = new javax.swing.JScrollPane();
//
//        // create JList and bind to model
//        surfacePatchList = new javax.swing.JList();
//
//        // add listeners
//        surfacePatchList.addListSelectionListener(new ListSelectionListener() {
//            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
//                surfacePatchListValueChanged(evt);
//            }
//        });
//
//        surfacePatchList.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent mouseEvent) {
//                surfacePatchList = (JList) mouseEvent.getSource();
//              if (mouseEvent.getClickCount() == 2) {
//                  surfacePatchListDoubleClicked(mouseEvent);
//              }
//            }
//          });
//
//        surfacePatchPane.setViewportView(surfacePatchList);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 6;
//        gridBagConstraints.gridwidth = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//        add(surfacePatchPane, gridBagConstraints);
//
//        //
//        // surface data pane
//        //
//        surfaceDataPane = new SurfaceDataPane(modelManager);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 7;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        add(surfaceDataPane, gridBagConstraints);
//
//        //
//        // offset pane
//        //
//        SurfacePatchOffsetPane surfaceOffsetPane = new SurfacePatchOffsetPane(modelManager);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 8;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        add(surfaceOffsetPane, gridBagConstraints);

        //
        // passes pane
        //
        TimeControlPane timeControlPane = new TimeControlPane(modelManager);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(timeControlPane, gridBagConstraints);



//        // remove all from view
//        removeAllButton.setText("Remove All From View");
//        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                removeAllButtonActionPerformed(evt);
//            }
//        });
//        jPanel2.add(removeAllButton);
//
//        // add the button panel 2
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 8;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        add(jPanel2, gridBagConstraints);
    }

    //
    // Operations
    //

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        RunInfo runInfo = new RunInfo();
        StateHistoryImporterDialog dialog = new StateHistoryImporterDialog(null, false);
        dialog.setRunInfo(runInfo, modelManager.getPolyhedralModel().isEllipsoid());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // If user clicks okay add to list
        if (dialog.getOkayPressed())
        {
            runInfo = dialog.getStateHistoryInfo();
            try
            {
                saveStateHistory(((DefaultListModel)simulationRunList.getModel()).getSize(), null, runInfo);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int[] selectedIndices = simulationRunList.getSelectedIndices();
        Arrays.sort(selectedIndices);
        for (int i=selectedIndices.length-1; i>=0; --i)
        {
            removeRun(selectedIndices[i]);
        }

        updateConfigFile();
    }

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int selectedItem = simulationRunList.getSelectedIndex();
        if (selectedItem >= 0)
        {
            RunInfo oldRunInfo = (RunInfo)((DefaultListModel)simulationRunList.getModel()).get(selectedItem);

            StateHistoryImporterDialog dialog = new StateHistoryImporterDialog(null, true);
            dialog.setRunInfo(oldRunInfo, modelManager.getPolyhedralModel().isEllipsoid());
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // If user clicks okay replace item in list
            if (dialog.getOkayPressed())
            {
                RunInfo newRunInfo = dialog.getStateHistoryInfo();
                try
                {
                    saveStateHistory(selectedItem, oldRunInfo, newRunInfo);
                    remapRunToRenderer(selectedItem);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (FitsException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_runListMousePressed
        runListMaybeShowPopup(evt);
    }

    private void runListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_runListMouseReleased
        runListMaybeShowPopup(evt);
    }

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        removeAllRunsFromRenderer();
    }

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        int minSelectedItem = simulationRunList.getMinSelectionIndex();
        if (minSelectedItem > 0)
        {
            int[] selectedIndices = simulationRunList.getSelectedIndices();
            Arrays.sort(selectedIndices);
            for (int i=0; i<selectedIndices.length; ++i)
            {
                --selectedIndices[i];
                moveDown(selectedIndices[i]);
            }

            simulationRunList.clearSelection();
            simulationRunList.setSelectedIndices(selectedIndices);
            simulationRunList.scrollRectToVisible(simulationRunList.getCellBounds(minSelectedItem-1, minSelectedItem-1));

            updateConfigFile();
        }
    }

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        int maxSelectedItem = simulationRunList.getMaxSelectionIndex();
        if (maxSelectedItem >= 0 && maxSelectedItem < simulationRunList.getModel().getSize()-1)
        {
            int[] selectedIndices = simulationRunList.getSelectedIndices();
            Arrays.sort(selectedIndices);
            for (int i=selectedIndices.length-1; i>=0; --i)
            {
                moveDown(selectedIndices[i]);
                ++selectedIndices[i];
            }

            simulationRunList.clearSelection();
            simulationRunList.setSelectedIndices(selectedIndices);
            simulationRunList.scrollRectToVisible(simulationRunList.getCellBounds(maxSelectedItem+1, maxSelectedItem+1));

            updateConfigFile();
        }
    }

    private void simulationRunListValueChanged(javax.swing.event.ListSelectionEvent evt)
    {
        int[] indices = simulationRunList.getSelectedIndices();
        if (indices == null || indices.length == 0)
        {
            editButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        else
        {
            editButton.setEnabled(indices.length == 1);
            deleteButton.setEnabled(true);
            int minSelectedItem = simulationRunList.getMinSelectionIndex();
            int maxSelectedItem = simulationRunList.getMaxSelectionIndex();
            moveUpButton.setEnabled(minSelectedItem > 0);
            moveDownButton.setEnabled(maxSelectedItem < simulationRunList.getModel().getSize()-1);

            if (indices.length == 1 && !evt.getValueIsAdjusting())
            {
                int index = indices[0];
                RunInfo runInfo = getRunInfo(index);
                System.out.println("Selected " + runInfo.name + ", " + runInfo.runfilename);

                StateHistoryKey runKey = getRunKey(index);

                // load in the new dataset
                runs.addRun(runKey, renderer);

                // set the current run
                StateHistoryModel currentRun = runs.getCurrentRun();

                if (currentRun != null)
                {
                    trajectoryList.setModel(currentRun);
//                    areaCalculationList.setModel(currentRun.getAreaCalculationCollection());
                    updateTimeBarPosition();
                }

            }
        }
    }

    private void trajectoryListDoubleClicked(MouseEvent mouseEvent)
    {
        int index = trajectoryList.locationToIndex(mouseEvent.getPoint());
        if (index >= 0) {
          Object o = trajectoryList.getModel().getElementAt(index);
          System.out.println("Time Interval Double-clicked: " + o.toString());
          StateHistoryModel currentRun = runs.getCurrentRun();
          if (currentRun != null)
          {
              Trajectory selectedTrajectory = currentRun.getTrajectoryByIndex(index);
              String currentTrajectoryName = selectedTrajectory.getName();
              System.out.println("Select Current Time Interval " + currentTrajectoryName);
              currentRun.setCurrentTrajectoryIndex(index);
              currentRun.setTimeFraction(0.0);
              currentRun.setShowSpacecraft(true);
          }
        }
    }


    private void trajectoryListValueChanged(javax.swing.event.ListSelectionEvent evt)
    {
        int[] indices = trajectoryList.getSelectedIndices();
        StateHistoryModel currentRun = runs.getCurrentRun();

        if (indices.length >= 1 && !evt.getValueIsAdjusting())
        {
            System.out.println("Time Interval List Value Changed: " + evt.toString());

            if (currentRun != null)
            {
                Set<String> trajectoryNames = new HashSet<String>();
                for (int i=0; i <indices.length; i++)
                {
                    int index = indices[i];
                    Trajectory selectedTrajectory = currentRun.getTrajectoryByIndex(index);
                    System.out.println("Show Time Interval " + selectedTrajectory.getName());
                    trajectoryNames.add(selectedTrajectory.getName());
                }

                if (currentRun != null)
                {
                    currentRun.setShowSpacecraft(false);
                    currentRun.setShowTrajectories(trajectoryNames);
                }

                // if only one item is selected, show the spacecraft and sub points
                if (indices.length == 1)
                {
                    int index = indices[0];
                    Object o = trajectoryList.getModel().getElementAt(index);
                    if (currentRun != null)
                    {
                        Trajectory selectedTrajectory = currentRun.getTrajectoryByIndex(index);
                        String currentTrajectoryName = selectedTrajectory.getName();
                        System.out.println("Select Current Time Interval " + currentTrajectoryName);
                        currentRun.setCurrentTrajectoryIndex(index);
                        currentRun.setTimeFraction(0.0);
                        currentRun.setShowSpacecraft(true);
                    }
                }
            }
        }
        else
        {
            System.out.println("Remove display of trajectories");
            if (currentRun != null)
            {
                currentRun.setCurrentTrajectory(null);
                if (currentRun != null)
                {
                    currentRun.setShowSpacecraft(false);
                    currentRun.setShowTrajectories(Collections.EMPTY_SET);
                }
                currentRun.setTimeFraction(0.0);
                currentRun.setShowSpacecraft(false);
            }
        }
    }


    private void areaCalculationListDoubleClicked(MouseEvent mouseEvent)
    {
//        int index = areaCalculationList.locationToIndex(mouseEvent.getPoint());
//        if (index >= 0) {
//          Object o = areaCalculationList.getModel().getElementAt(index);
//          System.out.println("Double-clicked on: " + o.toString());
////          Scenario currentScenario = scenarios.getCurrentScenario();
////          if (currentScenario != null)
////          {
////              System.out.println("Select Organ " + o);
////          }
//        }
    }


    private void areaCalculationListValueChanged(javax.swing.event.ListSelectionEvent evt)
    {
//        int[] indices = areaCalculationList.getSelectedIndices();
//
//        if (indices.length == 1 && !evt.getValueIsAdjusting())
//        {
//            StateHistoryModel currentRun = runs.getCurrentRun();
//            // remove currently displayed patches
//            currentRun.setShowPatches(new HashSet<String>());
//
//            int index = indices[0];
//            AreaCalculation selectedAreaCalculation = (AreaCalculation)areaCalculationList.getModel().getElementAt(index);
//            System.out.println("Select Area Calculation: " + selectedAreaCalculation);
//            AreaCalculationCollection areaCalculationCollection = runs.getCurrentRun().getAreaCalculationCollection();
//            areaCalculationCollection.setCurrentIndex(index);
//            AreaCalculation currentAreaCalculation = areaCalculationCollection.getCurrentValue();
//            if (currentAreaCalculation != null)
//            {
//                runs.getCurrentRun().setAreaCalculation(selectedAreaCalculation);
//                this.surfacePatchList.setModel(selectedAreaCalculation);
//                this.surfaceDataPane.setModel(selectedAreaCalculation.getScalarRange());
////                this.invalidate();
////                this.validate();
////                this.repaint();
//            }
//        }
    }
    private void surfacePatchListDoubleClicked(MouseEvent mouseEvent)
    {
//        int index = surfacePatchList.locationToIndex(mouseEvent.getPoint());
//        if (index >= 0) {
//          SurfacePatch selectedSurfacePatch = (SurfacePatch)surfacePatchList.getModel().getElementAt(index);
//          System.out.println("Double-clicked on surface patch: " + selectedSurfacePatch.toString());
//          AreaCalculation areaCalculation = runs.getCurrentRun().getAreaCalculation();
//          if (areaCalculation != null)
//          {
//              areaCalculation.setCurrentPatchIndex(index);
//              updateScalarBar();
//          }
//        }
    }

    private Set<String> visiblePatches = new HashSet<String>();
    private SurfacePatch selectedSurfacePatch = null;

    private void surfacePatchListValueChanged(javax.swing.event.ListSelectionEvent evt)
    {
//        int[] indices = surfacePatchList.getSelectedIndices();
//        AreaCalculationCollection areaCalculationCollection = runs.getCurrentRun().getAreaCalculationCollection();
//        AreaCalculation currentAreaCalculation = areaCalculationCollection.getCurrentValue();
//
//        if (indices.length >= 1 && !evt.getValueIsAdjusting())
//        {
//            visiblePatches.clear();
//            for (int i=0; i <indices.length; i++)
//            {
//                int index = indices[i];
//                selectedSurfacePatch = (SurfacePatch)surfacePatchList.getModel().getElementAt(index);
//                visiblePatches.add(selectedSurfacePatch.getName());
//                System.out.println("Select Surface Patch: " + selectedSurfacePatch);
//
//                // use the first of the selected surface patches as the model for the surface pane
//                if (i == 0)
//                {
//                    this.surfaceDataPane.setModel(selectedSurfacePatch);
//                    if (currentAreaCalculation != null)
//                    {
//                        currentAreaCalculation.setCurrentPatch(selectedSurfacePatch);
//                    }
//                }
//
//            }
//            runs.getCurrentRun().setShowPatches(visiblePatches);
//            updateScalarBar();
//        }
    }
}
