//package edu.jhuapl.sbmt.stateHistory.model;
//
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import java.io.File;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import javax.swing.JFileChooser;
//import javax.swing.JOptionPane;
//
//import org.joda.time.DateTime;
//import org.joda.time.Interval;
//
//import vtk.vtkActor;
//import vtk.vtkAssembly;
//import vtk.vtkConeSource;
//import vtk.vtkMatrix4x4;
//import vtk.vtkProp;
//import vtk.vtkScalarBarActor;
//import vtk.vtkTransform;
//
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
//import edu.jhuapl.saavtk.model.AbstractModel;
//import edu.jhuapl.saavtk.util.BoundingBox;
//import edu.jhuapl.saavtk.util.Configuration;
//import edu.jhuapl.saavtk.util.ConvertResourceToFile;
//import edu.jhuapl.saavtk.util.FileCache;
//import edu.jhuapl.saavtk.util.MathUtil;
//import edu.jhuapl.saavtk.util.Preferences;
//import edu.jhuapl.saavtk.util.Properties;
//import edu.jhuapl.sbmt.client.SmallBodyModel;
//import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
//import edu.jhuapl.sbmt.stateHistory.model.animator.AnimationFrame;
//import edu.jhuapl.sbmt.stateHistory.model.interfaces.HasTime;
//import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
//import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
//import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
//import edu.jhuapl.sbmt.stateHistory.model.scState.CsvState;
//import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StandardStateHistory;
//import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
//import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;
//import edu.jhuapl.sbmt.stateHistory.rendering.EarthDirectionMarker;
//import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
//import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftDirectionMarker;
//import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftFieldOfView;
//import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftLabel;
//import edu.jhuapl.sbmt.stateHistory.rendering.StatusBarTextActor;
//import edu.jhuapl.sbmt.stateHistory.rendering.SunDirectionMarker;
//import edu.jhuapl.sbmt.stateHistory.rendering.TimeBarTextActor;
//import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
//import edu.jhuapl.sbmt.stateHistory.rendering.animator.Animator;
//import edu.jhuapl.sbmt.stateHistory.ui.AnimationFileDialog;
//import edu.jhuapl.sbmt.stateHistory.ui.version2.IStateHistoryPanel;
//
//
//public class StateHistoryModel2 extends AbstractModel implements PropertyChangeListener, /*TableModel,*/ HasTime//, ActionListener
//{
//    private Double time;
//
//    //Use approximate radius of largest solar system body as scale for surface intercept vector.
//    private static final double JupiterScale = 75000;
//
//    private double markerRadius = 0.5;
//    private double markerHeight = 0.5;
//    private double scalingFactor = 0.0;
//
//    private boolean move;
//    private double iconScale = 10.0;
//
//    public static final String RUN_NAMES = "RunNames"; // What name to give this image for display
//    public static final String RUN_FILENAMES = "RunFilenames"; // Filename of image on disk
//    private Trajectory trajectory;
//    private TrajectoryActor trajectoryActor;
//
//    //the 3D representation of the s/c
//    private SpacecraftBody spacecraftBody;
//
//    //the cone pointing to where the spacecraft current is
//    private SpacecraftDirectionMarker spacecraftMarkerHead;
//
//    private SpacecraftFieldOfView spacecraftFov;
//
//    private SpacecraftLabel spacecraftLabelActor;
//
//    private vtkConeSource earthMarkerHead;
//    private vtkActor earthMarkerHeadActor;
//
//    private SunDirectionMarker sunDirectionMarker;
//    private SpacecraftDirectionMarker scDirectionMarker;
//    private EarthDirectionMarker earthDirectionMarker;
//    private vtkAssembly sunAssembly;
//
//    private ArrayList<vtkProp> stateHistoryActors = new ArrayList<vtkProp>();
//
//    private boolean showSpacecraft;
//    private boolean showSpacecraftBody;
//    private boolean showSpacecraftLabel;
//    private boolean showSpacecraftFov;
//    private boolean initialized =false;
//    private String description = "desc";
//    private File path = null;
//    final int lineLength = 121;
//
//    private double timeStep;
//
//    private boolean showSpacecraftMarker;
//    private boolean showEarthMarker;
//    private boolean showSunMarker;
//    private boolean showLighting;
//    private boolean earthView;
//    private boolean sunView;
//    private ArrayList<String[]> timeArray = new ArrayList<>(3);
//
//    private boolean visible; // able to be shown
//    private boolean showing = false; // currently showing
//    private double offset = offsetHeight;
//
//    private double[] sunDirection = { 0.0, 1.0, 0.0 };
//    public double[] getSunDirection() { return sunDirection; }
//
//    protected final StateHistoryKey key;
//    private SmallBodyModel smallBodyModel;
//    private Renderer renderer;
//
//    private StateHistory currentFlybyStateHistory;
//    private DateTime startTime;
//    private DateTime endTime;
//
//    public static final double offsetHeight = 2.0;
//
//    // variables related to the scale bar (note the scale bar is different
//    // from the scalar bar)
//
//    private TimeBarTextActor timeBarTextActor;
//    private StatusBarTextActor statusBarTextActor;
//
//    private boolean showTimeBar = true;
//    private boolean showScalarBar = false;
//
//    // variables related to the scalar bar
//    private vtkScalarBarActor scalarBarActor;
//    private int coloringIndex = 1;
//
//
//    /**
//     * A StateHistoryKey should be used to uniquely distinguish one trajectory from another.
//     * No two trajectories will have the same values for the fields of this class.
//     */
//    public static class StateHistoryKey
//    {
//        public static final Random RAND = new Random();
//        public Integer value;
//
//        public StateHistoryKey(StateHistoryCollection runs)
//        {
//            value = RAND.nextInt(1000);
//            while (runs.getKeys().contains(value)) {
//                value = RAND.nextInt(1000);
//            }
//        }
//
//        @Override
//        public boolean equals(Object obj)
//        {
//            return value.equals(((StateHistoryKey)obj).value);
//        }
//
//    }
//
//    static public StateHistoryModel2 createStateHistory(StateHistoryKey key, DateTime start, DateTime end, SmallBodyModel smallBodyModel, Renderer renderer)
//    {
//        return new StateHistoryModel2(key, start, end, smallBodyModel, renderer);
//    }
//
//    public StateHistoryModel2(StateHistoryKey key, DateTime start, DateTime end, SmallBodyModel smallBodyModel, Renderer renderer)
//    {
//        this.key = key;
//        this.smallBodyModel = smallBodyModel;
//        this.renderer = renderer;
//        this.startTime = start;
//        this.endTime = end;
//
//        initialize();
//
//    }
//
//    public StateHistoryModel2(StateHistoryKey key, SmallBodyModel smallBodyModel, Renderer renderer)
//    {
//        this.key = key;
//        this.renderer = renderer;
//        this.smallBodyModel = smallBodyModel;
//    }
//
//    private List<String> passFileNames = new ArrayList<String>();
//
//    private void initialize()
//    {
//        initialized = true;
//        BoundingBox bb = smallBodyModel.getBoundingBox();
//        double width = Math.max((bb.xmax-bb.xmin), Math.max((bb.ymax-bb.ymin), (bb.zmax-bb.zmin)));
//        scalingFactor = 30.62*width + -0.0002237;
//        if (trajectory == null) {
//            trajectory = new StandardTrajectory();
//        }
//        if (trajectoryActor == null)
//        {
//        	trajectoryActor = new TrajectoryActor(trajectory);
//        }
//
//        if (spacecraftBody == null)
//        {
//            try
//            {
//                createSpacecraftPolyData();
//                // spacecraft label
//                spacecraftLabelActor = new SpacecraftLabel();
//            } catch (NumberFormatException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            showSpacecraftMarker = false;
//            showSpacecraft = false;
//            updateActorVisibility();
//        }
//        if (statusBarTextActor == null)
//        {
//            setupStatusBar();
//        }
//
//        statusBarTextActor.updateStatusBarPosition(renderer.getPanelWidth(), renderer.getPanelHeight());
//
//        if (timeBarTextActor == null)
//            setupTimeBar();
//    }
//
//
//
//    private void initializeSpacecraftBody(File modelFile)
//    {
//    	spacecraftBody = new SpacecraftBody(modelFile.getAbsolutePath());
//    }
//
//    private void createSpacecraftPolyData()
//    {
//        String spacecraftFileName = "/edu/jhuapl/sbmt/data/cassini-9k.stl";
//        initializeSpacecraftBody(ConvertResourceToFile.convertResourceToRealFile(this, spacecraftFileName, Configuration.getApplicationDataDir()));
//
//        spacecraftFov = new SpacecraftFieldOfView();
//
//        //Scale subsolar and subearth point markers to body size
//        BoundingBox bb = smallBodyModel.getBoundingBox();
//        double width = Math.max((bb.xmax-bb.xmin), Math.max((bb.ymax-bb.ymin), (bb.zmax-bb.zmin)));
//        markerRadius = 0.02 * width;
//        markerHeight = markerRadius * 3.0;
//
//        earthDirectionMarker = new EarthDirectionMarker(markerRadius/16.0, markerHeight, 0, 0, 0);
//        sunDirectionMarker = new SunDirectionMarker(markerRadius/16.0, markerHeight, 0, 0, 0);
//        scDirectionMarker = new SpacecraftDirectionMarker(markerRadius/16.0, markerHeight, 0, 0, 0);
//    }
//
//    public Double getTime()
//    {
//        return time;
//    }
//
//    public void setTime(Double time)
//    {
//        this.time = time;
//        if (currentFlybyStateHistory != null)
//            currentFlybyStateHistory.setTime(time);
//    }
//
//    public Double getTimeFraction()
//    {
//        if (currentFlybyStateHistory != null)
//            return currentFlybyStateHistory.getTimeFraction();
//        else
//            return null;
//    }
//
//    public Double getPeriod()
//    {
//        if (currentFlybyStateHistory != null)
//            return currentFlybyStateHistory.getPeriod();
//        else
//            return 0.0;
//    }
//
//    public void setTimeFraction(Double timeFraction)
//    {
//        if (currentFlybyStateHistory != null && spacecraftBody.getActor() != null)
//        {
//            // set the time
//            currentFlybyStateHistory.setTimeFraction(timeFraction);
//            setTime(currentFlybyStateHistory.getTime());
//
//            if (timeBarTextActor != null)
//            {
//                timeBarTextActor.updateTimeBarPosition(renderer.getPanelWidth(), renderer.getPanelHeight());
//                timeBarTextActor.updateTimeBarValue(getTime());
//            }
//
//            // get the current FlybyState
//            State state = currentFlybyStateHistory.getCurrentValue();
//            double[] spacecraftPosition = currentFlybyStateHistory.getSpacecraftPosition();
//
//
//            double[] sunPosition = currentFlybyStateHistory.getSunPosition();
//            double[] sunMarkerPosition = new double[3];
//            sunDirection = new double[3];
//            double[] sunViewpoint = new double[3];
//            double[] sunViewDirection = new double[3];
//            MathUtil.unorm(sunPosition, sunDirection);
//            MathUtil.vscl(JupiterScale, sunDirection, sunViewpoint);
//            MathUtil.vscl(-1.0, sunDirection, sunViewDirection);
//            int result = smallBodyModel.computeRayIntersection(sunViewpoint, sunViewDirection, sunMarkerPosition);
//
//
//            // toggle for lighting - Alex W
//            if (timeFraction >= 0.0 && showLighting)
//            {
//                renderer.setFixedLightDirection(sunDirection);
//                renderer.setLighting(LightingType.FIXEDLIGHT);
//                updateActorVisibility();
//            }
//            else
//                renderer.setLighting(LightingType.LIGHT_KIT);
//
//
//            double[] earthPosition = currentFlybyStateHistory.getEarthPosition();
//            double[] earthMarkerPosition = new double[3];
//            double[] earthDirection = new double[3];
//            double[] earthViewpoint = new double[3];
//            double[] earthViewDirection = new double[3];
//            MathUtil.unorm(earthPosition, earthDirection);
//            MathUtil.vscl(JupiterScale, earthDirection, earthViewpoint);
//            MathUtil.vscl(-1.0, earthDirection, earthViewDirection);
//            result = smallBodyModel.computeRayIntersection(earthViewpoint, earthViewDirection, earthMarkerPosition);
//
//            //double[] spacecraftPosition = currentFlybyStateHistory.getSpacecraftPosition();
//            double[] spacecraftMarkerPosition = new double[3];
//            double[] spacecraftDirection = new double[3];
//            double[] spacecraftViewpoint = new double[3];
//            double[] spacecraftViewDirection = new double[3];
//            MathUtil.unorm(spacecraftPosition, spacecraftDirection);
//            MathUtil.vscl(JupiterScale, spacecraftDirection, spacecraftViewpoint);
//            MathUtil.vscl(-1.0, spacecraftDirection, spacecraftViewDirection);
//            result = smallBodyModel.computeRayIntersection(spacecraftViewpoint, spacecraftViewDirection, spacecraftMarkerPosition);
//
//            //rotates sun pointer to point in direction of sun - Alex W
//            double[] zAxis = {1,0,0};
//            double[] sunPos = currentFlybyStateHistory.getSunPosition();
//            sunDirectionMarker.updateSunPosition(sunPos);
////            double[] sunPosDirection = new double[3];
////            MathUtil.unorm(sunPos, sunPosDirection);
////            double[] rotationAxisSun = new double[3];
////            MathUtil.vcrss(sunPosDirection, zAxis, rotationAxisSun);
////            double rotationAngleSun = ((180.0/Math.PI)*MathUtil.vsep(zAxis, sunPosDirection));
//
////            vtkTransform sunMarkerTransform = new vtkTransform();
////            //sunMarkerTransform.PostMultiply();
////            sunMarkerTransform.Translate(sunMarkerPosition);
////            sunMarkerTransform.RotateWXYZ(-rotationAngleSun, rotationAxisSun[0], rotationAxisSun[1], rotationAxisSun[2]);
////            sunAssembly.SetUserTransform(sunMarkerTransform);
//
//
//            //rotates earth pointer to point in direction of earth - Alex W
//            double[] earthPos = currentFlybyStateHistory.getEarthPosition();
//            double[] earthPosDirection = new double[3];
//            MathUtil.unorm(earthPos, earthPosDirection);
//            double[] rotationAxisEarth = new double[3];
//            MathUtil.vcrss(earthPosDirection, zAxis, rotationAxisEarth);
//
//            double rotationAngleEarth = ((180.0/Math.PI)*MathUtil.vsep(zAxis, earthPosDirection));
//
//            vtkTransform earthMarkerTransform = new vtkTransform();
//            earthMarkerTransform.Translate(earthMarkerPosition);
//            earthMarkerTransform.RotateWXYZ(-rotationAngleEarth, rotationAxisEarth[0], rotationAxisEarth[1], rotationAxisEarth[2]);
//            earthMarkerHeadActor.SetUserTransform(earthMarkerTransform);
//
//
//            //rotates spacecraft pointer to point in direction of spacecraft - Alex W
//            double[] spacecraftPos = spacecraftMarkerPosition;
//            double[] spacecraftPosDirection = new double[3];
//            MathUtil.unorm(spacecraftPos, spacecraftPosDirection);
//            double[] rotationAxisSpacecraft = new double[3];
//            MathUtil.vcrss(spacecraftPosDirection, zAxis, rotationAxisSpacecraft);
//
//            double rotationAngleSpacecraft = ((180.0/Math.PI)*MathUtil.vsep(zAxis, spacecraftPosDirection));
//
//            vtkTransform spacecraftMarkerTransform = new vtkTransform();
//            spacecraftMarkerTransform.Translate(spacecraftPos);
//            spacecraftMarkerTransform.RotateWXYZ(-rotationAngleSpacecraft, rotationAxisSpacecraft[0], rotationAxisSpacecraft[1], rotationAxisSpacecraft[2]);
////            spacecraftMarkerHeadActor.SetUserTransform(spacecraftMarkerTransform);
//
//            // set camera to earth, spacecraft, or sun views - Alex W
//            if(earthView)
//            {
//                double[] focalpoint = {0,0,0};
//                double[] upVector = {0,1,0};
//                double[] newEarthPos = new double[3];
//                MathUtil.unorm(earthPos, newEarthPos);
//                MathUtil.vscl(scalingFactor, newEarthPos, newEarthPos);
//                renderer.setCameraOrientation(newEarthPos, renderer.getCameraFocalPoint(), upVector, renderer.getCameraViewAngle());
//            }
//            else if(move)
//            {
//                double[] focalpoint = {0,0,0};
//                double[] upVector = {0,1,0};
//                renderer.setCameraOrientation(currentFlybyStateHistory.getSpacecraftPosition(), renderer.getCameraFocalPoint(), upVector, renderer.getCameraViewAngle());
//            }
//            else if(sunView)
//            {
//                double[] focalpoint = {0,0,0};
//                double[] upVector = {0,1,0};
//                double[] newSunPos = new double[3];
//                MathUtil.unorm(sunPos, newSunPos);
//                MathUtil.vscl(scalingFactor, newSunPos, newSunPos);
//                renderer.setCameraOrientation(newSunPos, renderer.getCameraFocalPoint(), upVector, renderer.getCameraViewAngle());
//            }
//
//            double velocity[] = state.getSpacecraftVelocity();
//
//            double speed = Math.sqrt(velocity[0]*velocity[0] + velocity[1]*velocity[1] + velocity[2]*velocity[2]);
//
//            double radius = Math.sqrt(spacecraftPosition[0]*spacecraftPosition[0] + spacecraftPosition[1]*spacecraftPosition[1] + spacecraftPosition[2]*spacecraftPosition[2]);
//            result = smallBodyModel.computeRayIntersection(spacecraftViewpoint, spacecraftViewDirection, spacecraftMarkerPosition);
//            double smallBodyRadius = Math.sqrt(spacecraftMarkerPosition[0]*spacecraftMarkerPosition[0] + spacecraftMarkerPosition[1]*spacecraftMarkerPosition[1] + spacecraftMarkerPosition[2]*spacecraftMarkerPosition[2]);
////            if(distanceOption==1)
////            {
////                radius = radius - smallBodyRadius;
////            }
//
//            String speedText = String.format("%7.1f km %7.3f km/sec   .", radius, speed);
//
//            // set the current orientation
//            double[] xaxis = state.getSpacecraftXAxis();
//            double[] yaxis = state.getSpacecraftYAxis();
//            double[] zaxis = state.getSpacecraftZAxis();
//
//            // create spacecraft matrices
//            vtkMatrix4x4 spacecraftBodyMatrix = new vtkMatrix4x4();
//            vtkMatrix4x4 spacecraftIconMatrix = new vtkMatrix4x4();
//
//            vtkMatrix4x4 fovMatrix = new vtkMatrix4x4();
//            vtkMatrix4x4 fovRotateXMatrix = new vtkMatrix4x4();
//            vtkMatrix4x4 fovRotateYMatrix = new vtkMatrix4x4();
//            vtkMatrix4x4 fovRotateZMatrix = new vtkMatrix4x4();
//            vtkMatrix4x4 fovScaleMatrix = new vtkMatrix4x4();
//
//            vtkMatrix4x4 sunMarkerMatrix = new vtkMatrix4x4();
//            vtkMatrix4x4 earthMarkerMatrix = new vtkMatrix4x4();
//
//            vtkMatrix4x4 spacecraftInstrumentMatrix = new vtkMatrix4x4();
//
//            // set to identity
//            spacecraftBodyMatrix.Identity();
//            spacecraftIconMatrix.Identity();
//            fovMatrix.Identity();
//            fovRotateXMatrix.Identity();
//            fovRotateYMatrix.Identity();
//            fovRotateZMatrix.Identity();
//            sunMarkerMatrix.Identity();
//            earthMarkerMatrix.Identity();
//            //fovMatrix = new vtkMatrix4x4
//
//            // set body orientation matrix
//            for (int i=0; i<3; i++)
//            {
//                spacecraftBodyMatrix.SetElement(i, 0, xaxis[i]);
//                spacecraftBodyMatrix.SetElement(i, 1, yaxis[i]);
//                spacecraftBodyMatrix.SetElement(i, 2, zaxis[i]);
//            }
//
//            // create the icon matrix, which is just the body matrix scaled by a factor
//            for (int i=0; i<3; i++)
//                spacecraftIconMatrix.SetElement(i, i, iconScale);
//            spacecraftIconMatrix.Multiply4x4(spacecraftIconMatrix, spacecraftBodyMatrix, spacecraftIconMatrix);
//
//            // set translation
//            for (int i=0; i<3; i++)
//            {
//                spacecraftBodyMatrix.SetElement(i, 3, spacecraftPosition[i]);
//                spacecraftIconMatrix.SetElement(i, 3, spacecraftPosition[i]);
//                fovMatrix.SetElement(i, 3, spacecraftPosition[i]);
//
//                sunMarkerMatrix.SetElement(i, 3, sunMarkerPosition[i]);
//                earthMarkerMatrix.SetElement(i, 3, earthMarkerPosition[i]);
//            }
//
////            spacecraftBodyActor.SetUserMatrix(spacecraftIconMatrix);
//
//            spacecraftLabelActor.SetAttachmentPoint(spacecraftPosition);
//            spacecraftLabelActor.SetCaption(speedText);
//
////            spacecraftFovActor.SetUserMatrix(fovMatrix);
//
////            spacecraftMarkerActor.SetUserMatrix(spacecraftBodyMatrix);
////            earthMarkerActor.SetUserMatrix(earthMarkerMatrix);
//            spacecraftBody.Modified();
//            spacecraftFov.Modified();
////            spacecraftMarkerBody.Modified();
//            earthMarkerHead.Modified();
////            earthMarkerBody.Modified();
////            sunAssembly.Modified();
//            sunDirectionMarker.Modified();
//
//            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//        }
//    }
//
//    public void setShowSpacecraft(boolean show)
//    {
//        this.showSpacecraft = show;
//        this.showSpacecraftFov = false;
//        this.showSpacecraftLabel = show;
//        updateActorVisibility();
//    }
//
//    //updated method so that it is togglable for pointers, trajectory etc. - Alex W
//    public void updateActorVisibility()
//    {
//        stateHistoryActors.clear();
//        initialize();
//
//        if (visible)
//        {
//            stateHistoryActors.add(trajectoryActor);
//        }
//
//        if (showSpacecraftBody)
//            stateHistoryActors.add(spacecraftBody.getActor());
//        if (showSpacecraftLabel)
//            stateHistoryActors.add(spacecraftLabelActor);
//        if (showSpacecraftFov)
//            stateHistoryActors.add(spacecraftFov.getActor());
//        if (showEarthMarker && !(time == 0.0))
//            stateHistoryActors.add(earthMarkerHeadActor);
//        if (showSunMarker && !(time == 0.0))
//        	stateHistoryActors.add(sunDirectionMarker.getActor());
//        if (showSpacecraftMarker)
//            stateHistoryActors.add(spacecraftMarkerHead.getActor());
//
//        if (showTimeBar)
//        {
//            stateHistoryActors.add(timeBarTextActor);
//        }
//
//        //displays extra messages at the bottom of the display
//        stateHistoryActors.add(statusBarTextActor);
//
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//    }
//
//    public ArrayList<vtkProp> getProps()
//    {
//        return stateHistoryActors;
//    }
//
//    //
//    // data display legends
//    //
//
//
//
//    // sets the visibility of actors, called in StateHistoryPanel and gets the toggle info in it - Alex W
//    public void setActorVisibility(String actorName, boolean visibility)
//    {
//        //maps the actors+data
//        if(actorName.equalsIgnoreCase("Earth"))
//            showEarthMarker = visibility;
//        if(actorName.equalsIgnoreCase("Sun"))
//            showSunMarker = visibility;
//        if(actorName.equalsIgnoreCase("SpacecraftMarker"))
//            showSpacecraftMarker = visibility;
//        if(actorName.equalsIgnoreCase("Spacecraft"))
//        {
//            setShowSpacecraft(visibility);
//        }
//        if(actorName.equalsIgnoreCase("Trajectory"))
//            if(visibility)
//            {
//                this.visible = true;
//            }
//            else
//            {
//                this.visible = false;
//            }
//        if(actorName.equalsIgnoreCase("Lighting"))
//            showLighting = visibility;
//        updateActorVisibility();
//    }
//
//    public void setColoringIndex(int index) throws IOException
//    {
//        if (coloringIndex != index)
//        {
//            coloringIndex = index;
//        }
//    }
//
//    public int getColoringIndex()
//    {
//        return coloringIndex;
//    }
//
//    private void setupStatusBar()
//    {
//        statusBarTextActor = new StatusBarTextActor();
//        stateHistoryActors.add(statusBarTextActor);
//
//        statusBarTextActor.GetTextProperty().SetColor(1.0, 1.0, 1.0);
//        statusBarTextActor.GetTextProperty().SetJustificationToCentered();
//        statusBarTextActor.GetTextProperty().BoldOn();
//
//        statusBarTextActor.VisibilityOn();
//    }
//
//
//
//    private void setupTimeBar()
//    {
//        timeBarTextActor = new TimeBarTextActor();
//        stateHistoryActors.add(timeBarTextActor);
//        showTimeBar = Preferences.getInstance().getAsBoolean(Preferences.SHOW_SCALE_BAR, true);
//    }
//
//    public void setShowTimeBar(boolean enabled)
//    {
//        this.showTimeBar = enabled;
//        // The following forces the scale bar to be redrawn.
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//        // Note that we call firePropertyChange *twice*. Not really sure why.
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//    }
//
//    public boolean getShowTimeBar()
//    {
//        return showTimeBar;
//    }
//
//    public void setShowScalarBar(boolean enabled)
//    {
//        this.showScalarBar = enabled;
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//        // Note that we call firePropertyChange *twice*. Not really sure why.
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//    }
//
//    public boolean getShowScalarBar()
//    {
//        return showScalarBar;
//    }
//
//    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
//    {
//        Trajectory traj = trajectory;
//        if (traj != null)
//        {
//            // putting selection code here until we get around to implementing a FlybyPicker -turnerj1
//            //            this.setCurrentTrajectoryId(traj.id);
//            return "Trajectory " + traj.getId() + " = " + traj.getName() + " contains " + traj.getX().size() + " vertices";
//        }
//        return "";
//    }
//
//    public boolean getInitialized(){
//        return initialized;
//    }
//
//
//    public StateHistoryKey getKey()
//    {
//        return key;
//    }
//
//    public String getTrajectoryName()
//    {
//    	return trajectoryActor.getTrajectoryName();
//    }
//
//    public String defaultTrajectoryName() {
//        return startTime.toString() + "_" + endTime.toString();
//    }
//
//    // returns sun position - Alex W
//    public double[] getSunPosition()
//    {
//        return currentFlybyStateHistory.getSunPosition();
//    }
//
//    // sets the renderer to move along the spacecraft trajectory - Alex W
//    public void setSpacecraftMovement(boolean move)
//    {
//        this.move = move;
//        if(move)
//        {
//            double[] focalpoint = {0,0,0};
//            double[] upVector = {0,1,0};
//            renderer.setCameraOrientation(currentFlybyStateHistory.getSpacecraftPosition(), renderer.getCameraFocalPoint(), upVector, 30);
//        }
//    }
//
//    // sets the renderer to the earth position and plays the animation with camera fixed to earth - Alex W
//    public void setEarthView(boolean move, boolean showSpacecraft)
//    {
//        this.earthView = move;
//        if(earthView)
//        {
//            double[] focalpoint = {0,0,0};
//            double[] upVector = {0,1,0};
//            double[] earthPos = currentFlybyStateHistory.getEarthPosition();
//            double[] newEarthPos = new double[3];
//            MathUtil.unorm(earthPos, newEarthPos);
//            MathUtil.vscl(scalingFactor, newEarthPos, newEarthPos);
//            renderer.setCameraOrientation(newEarthPos, renderer.getCameraFocalPoint(), upVector, 5);
//        }
//        if (showSpacecraft) {
//            this.setActorVisibility("Spacecraft", true);
//        }
//    }
//
//    // sets the renderer to the sun position and plays the animation with camera fixed to sun - Alex W
//    public void setSunView(boolean move, boolean showSpacecraft)
//    {
//        this.sunView = move;
//        if(sunView)
//        {
//            double[] focalpoint = {0,0,0};
//            double[] upVector = {0,1,0};
//            double[] sunPos = currentFlybyStateHistory.getSunPosition();
//            double[] newSunPos = new double[3];
//            MathUtil.unorm(sunPos, newSunPos);
//            MathUtil.vscl(scalingFactor, newSunPos, newSunPos);
//            renderer.setCameraOrientation(newSunPos, renderer.getCameraFocalPoint(), upVector, 5);
//        }
//        if (showSpacecraft) {
//            this.setActorVisibility("Spacecraft", true);
//        }
//    }
//
//    //returns renderer - Alex W
//    public Renderer getRenderer()
//    {
//        return renderer;
//    }
//
//    // sets view angle - Alex W
//    public void setViewAngle(double angle)
//    {
//        renderer.setCameraViewAngle(angle);
//    }
//
//    // set time of animation - Alex W
//    public boolean setInputTime(DateTime dt, IStateHistoryPanel panel)
//    {
//        try
//        {
//
//            String dtString = dt.toString().substring(0, 23);
//            String start = timeArray.get(0)[0];
//            String end = timeArray.get(0)[1];
//
//            if(dtString.compareTo(start) < 0 || dtString.compareTo(end) > 0)
//            {
//                JOptionPane.showMessageDialog(null, "Entered time is outside the range of the selected interval.", "Error",
//                        JOptionPane.ERROR_MESSAGE);
//                return false;
//            }
//
//            Interval interval1 = new Interval(startTime, dt);
//            Interval interval2 = new Interval(startTime, endTime);
//
//            org.joda.time.Duration duration1 = interval1.toDuration();
//            org.joda.time.Duration duration2 = interval2.toDuration();
//
//            BigDecimal num1 = new BigDecimal(duration1.getMillis());
//            BigDecimal num2 = new BigDecimal(duration2.getMillis());
//            BigDecimal tf = num1.divide(num2,50,RoundingMode.UP);
//            setTimeFraction(Double.parseDouble(tf.toString()));
//            panel.setTimeSlider(Double.parseDouble(tf.toString()));
//        }
//        catch (Exception e)
//        {
//
//        }
//        return true;
//    }
//
//    // toggle the visibility of trajectories - Alex W
//    public void showTrajectory(boolean show)
//    {
////        if(show)
////        {
////            for(int i = 0; i<stateHistoryActors.size(); i++)
////            {
////                if(trajectoryActor.equals(stateHistoryActors.get(i)))
////                {
////                    stateHistoryActors.get(i).VisibilityOn();
////                    stateHistoryActors.get(i).Modified();
////                }
////            }
////            showing = true;
////        }
////        else
////        {
////            for(int i = 0; i<stateHistoryActors.size(); i++)
////            {
////                if(trajectoryActor.equals(stateHistoryActors.get(i)))
////                {
////                    stateHistoryActors.get(i).VisibilityOff();
////                    stateHistoryActors.get(i).Modified();
////                }
////
////            }
////            showing = false;
////        }
////
////        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//    }
//
//
//    /***
//     *  goes to the server and creates a new time history for the body with the given time range.
//     *
//     * @param panel - main panel
//     * @param length - length of interval
//     * @param name - name of interval
//     * @return -1 if error thrown on creation, 1 if successfully created
//     */
//    // returns
//    public int createNewTimeInterval(/*IStateHistoryPanel panel,*/ double length, String name)
//    {
//
//        // gets the history file from the server
//        SmallBodyViewConfig config = (SmallBodyViewConfig) smallBodyModel.getConfig();
//        path = FileCache.getFileFromServer(config.timeHistoryFile);
//
//        // removes the time zone from the time
//        String startString = startTime.toString().substring(0,23);
//        String endString = endTime.toString().substring(0,23);
//
//        // searches the file for the specified times
//        String queryStart = readString(lineLength);
//        String queryEnd = readString((int)getBinaryFileLength()*lineLength-lineLength);
//
//        // error checking
//        if(startTime.compareTo(endTime) > 0)
//        {
//            JOptionPane.showMessageDialog(null, "The entered times are not in the correct order.", "Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return -1;
//        }
//        if(startString.compareTo(queryStart) < 0 || endString.compareTo(queryEnd) > 0)
//        {
//            JOptionPane.showMessageDialog(null, "One or more of the query times are out of range of the available data.", "Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return -1;
//        }
//
//        // get start and stop positions in file
//        int positionStart = binarySearch(1, (int) getBinaryFileLength(), startString, false);
//        int positionEnd = binarySearch(1, (int) getBinaryFileLength(), endString, true);
//
//        // check length of time
//        if(readString(positionStart).compareTo(readString(positionEnd)) == 0)
//        {
//            JOptionPane.showMessageDialog(null, "The queried time interval is too small.", "Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return -1;
//        }
//
//        //sets the default name to "startTime_endTime"
//        if(name.equals(""))
//        {
//            name = readString(positionStart) + "_" + readString(positionEnd);
//        }
//
//        // check length of interval
//        if(length > 10.0)
//        {
//            int result = JOptionPane.showConfirmDialog(null, "The interval you selected is longer than 10 days and may take a while to generate. \nAre you sure you want to create it?");
//            if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
//                return -1;
//        }
//
//        // creates the trajectory
//        Trajectory temp = new StandardTrajectory();
//        StateHistory history = new StandardStateHistory();
//
//        this.currentFlybyStateHistory = history;
//
//        //  reads the binary file and writes the data to a CSV file
//        String[] timeSet = new String[2];
//        timeArray.add(timeSet);
//        for(int i = positionStart; i <= positionEnd; i+=lineLength)
//        {
//            int[] position = new int[12];
//            for(int j = 0; j<position.length; j++)
//            {
//                position[j] = i + 25 + (j * 8);
//            }
//            State flybyState = new CsvState(readString(i),
//                    readBinary(position[0]), readBinary(position[1]), readBinary(position[2]),
//                    readBinary(position[3]), readBinary(position[4]), readBinary(position[5]),
//                    readBinary(position[6]), readBinary(position[7]), readBinary(position[8]),
//                    readBinary(position[9]), readBinary(position[10]), readBinary(position[11]));
//
//            // add to history
//            history.put(flybyState);
//
//            double[] spacecraftPosition = flybyState.getSpacecraftPosition();
//
//            temp.getX().add(spacecraftPosition[0]);
//            temp.getY().add(spacecraftPosition[1]);
//            temp.getZ().add(spacecraftPosition[2]);
//
//            if(com.mysql.jdbc.StringUtils.isNullOrEmpty(timeArray.get(0)[0]))
//            {
//                timeArray.get(0)[0] = flybyState.getUtc();
//            }
//            timeArray.get(0)[1] = flybyState.getUtc();
//
//        }
//
//        setCurrentTrajectory(temp);
//
//        TrajectoryActor trajectoryActor = new TrajectoryActor(temp);
//
////        createTrajectoryPolyData();
////
////        trajectoryMapper.SetInputData(trajectoryPolylines);
////
////        vtkActor actor = new vtkActor();
////        trajectoryActor = actor;
////        trajectoryActor.SetMapper(trajectoryMapper);
////        trajectoryActor.GetProperty().SetLineWidth(trajectoryLineThickness);
////        setTimeFraction(0.0);
////        try
////        {
////            panel.initializeRunList();
////        }
////        catch (IOException e)
////        {
////            e.printStackTrace();
////        }
////
////        initialize();
////        spacecraftBody.Modified();
////        trajectoryActor.Modified();
//        return 1;
//    }
//
//    private void setCurrentTrajectory(Trajectory temp)
//    {
//        trajectory = temp;
//    }
//
//
//
//
//    // starts the process for creating the movie frames
//    public void saveAnimation(IStateHistoryPanel panel, String start, String end)
//    {
//        AnimationFileDialog dialog = new AnimationFileDialog(start, end);
//        int result = dialog.showSaveDialog(panel.getView());
//
//        if(result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION)
//        {
//            return;
//        }
//
//        File file = dialog.getSelectedFile();
//
//
//        int frameNum = (Integer) dialog.getNumFrames().getValue();
//        timeStep = 1.0/ (double) frameNum;
//
//        Animator animator = new Animator(renderer);
//        animator.saveAnimation(frameNum, file, new AnimatorFrameRunnable()
//		{
//        	@Override
//        	public void run(AnimationFrame frame)
//        	{
//        		// TODO Auto-generated method stub
//        		super.run(frame);
//        		run();
//        	}
//
//			@Override
//			public void run()
//			{
//				setTimeFraction(frame.timeFraction);
//		        frame.panel.setTimeSlider(frame.timeFraction);
//			}
//		});
//
//    }
//
//
//
////    // sets the renderer to the information in the animation frame
////    private void setAnimationFrame(AnimationFrame frame)
////    {
////        setTimeFraction(frame.timeFraction);
////        frame.panel.setTimeSlider(frame.timeFraction);
////    }
//
//    // binary searches the binary file for a time for the new time interval feature - Alex W
//    private int binarySearch(int first, int last, String target, boolean pos)
//    {
//        if(first > last)
//        {
//            if(pos)
//            {
//                return (last + 1) * lineLength;
//            }
//            return (last) * lineLength;
//        }
//        else
//        {
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
//    // gets the number of lines of a binary file, needed for the binary search - Alex W
//    private long getBinaryFileLength()
//    {
//        long length = 0;
//        try
//        {
//            RandomAccessFile fileStore = new RandomAccessFile(path, "r");
//            length = fileStore.length()/lineLength;
//            fileStore.close();
//        }
//        catch (Exception e)
//        {
//            return length;
//        }
//        return length;
//    }
//
//    // reads binary that represents a String - Alex W
//    private String readString(int postion)
//    {
//        String string = "";
//        try
//        {
//            RandomAccessFile fileStore = new RandomAccessFile(path, "r");
//            fileStore.seek(postion);
//            string = fileStore.readUTF();
//            fileStore.close();
//        }
//        catch (Exception e)
//        {
//            return "";
//        }
//        return string;
//    }
//
//    // reads binary that represents a double - Alex W
//    private double readBinary(int postion)
//    {
//        double num = 0;
//        try
//        {
//            RandomAccessFile fileStore = new RandomAccessFile(path, "r");
//            fileStore.seek(postion);
//            num = fileStore.readDouble();
//            fileStore.close();
//        }
//        catch (Exception e)
//        {
//            return 0;
//        }
//        return  num;
//    }
//
//    // gets the start and end times of a trajectory - Alex W
//    public String[] getIntervalTime()
//    {
//        return timeArray.get(0);
//    }
//
//    @Override
//    public void propertyChange(PropertyChangeEvent evt)
//    {
//        // TODO Auto-generated method stub
//
//    }
//
//    public DateTime getStartTime()
//    {
//        return startTime;
//    }
//    public DateTime getEndTime()
//    {
//        return endTime;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//    public void setDescription(String desc)
//    {
//        description = desc;
//    }
//}
