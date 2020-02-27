package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import vtk.vtkActor;
import vtk.vtkMatrix4x4;
import vtk.vtkProp;
import vtk.vtkScalarBarActor;
import vtk.vtkTransform;

import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;
//import edu.jhuapl.sbmt.client.ModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.HasTime;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.EarthDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftFieldOfView;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftLabel;
import edu.jhuapl.sbmt.stateHistory.rendering.StatusBarTextActor;
import edu.jhuapl.sbmt.stateHistory.rendering.SunDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.TimeBarTextActor;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;

public class StateHistoryCollection extends SaavtkItemManager<StateHistory> /*AbstractModel*/ implements PropertyChangeListener, HasTime
{
    private SmallBodyModel smallBodyModel;
    private ArrayList<StateHistoryKey> keys = new ArrayList<StateHistoryKey>();
    private List<StateHistory> simRuns = new ArrayList<StateHistory>();
    private StateHistory currentRun = null;
    private HashMap<StateHistory, TrajectoryActor> stateHistoryToRendererMap = new HashMap<StateHistory, TrajectoryActor>();
    private SpacecraftBody spacecraft;
    private double[] spacecraftPosition;
    private double[] earthPosition;
    private double[] sunPosition;

    //Text Actors
    private TimeBarTextActor timeBarActor;
    private StatusBarTextActor statusBarTextActor;
    private vtkScalarBarActor scalarBarActor;
	private SpacecraftLabel spacecraftLabelActor;

	//FOV Actors
	private SpacecraftFieldOfView spacecraftFov;

	//Direction markers
    private SpacecraftDirectionMarker scDirectionMarker;
	private SunDirectionMarker sunDirectionMarker;
	private EarthDirectionMarker earthDirectionMarker;

	double[] zAxis = {1,0,0};
	double markerRadius, markerHeight;

    private static final double JupiterScale = 75000;
    private double[] sunDirection;

    private double[] currentLookFromDirection;
//    private double[] currentLookToDirection;

    public StateHistoryCollection(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
        BoundingBox bb = smallBodyModel.getBoundingBox();
        double width = Math.max((bb.xmax-bb.xmin), Math.max((bb.ymax-bb.ymin), (bb.zmax-bb.zmin)));
        markerRadius = 0.02 * width;
        markerHeight = markerRadius * 3.0;
        this.spacecraftLabelActor = new SpacecraftLabel();
        this.spacecraftLabelActor.VisibilityOff();

        this.spacecraft = new SpacecraftBody(ConvertResourceToFile.convertResourceToRealFile(this, "/edu/jhuapl/sbmt/data/cassini-9k.stl", Configuration.getApplicationDataDir()).getAbsolutePath());
        this.spacecraft.getActor().VisibilityOff();

        this.scDirectionMarker = new SpacecraftDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
        this.scDirectionMarker.getActor().VisibilityOff();

        this.earthDirectionMarker = new EarthDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
        this.earthDirectionMarker.getActor().VisibilityOff();

        this.sunDirectionMarker = new SunDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
        this.sunDirectionMarker.getActor().VisibilityOff();
    }

    private boolean containsKey(StateHistoryKey key)
    {
        for (StateHistory run : simRuns)
        {
            if (run.getKey().equals(key))
                return true;
        }

        return false;
    }

    private StateHistory getRunFromKey(StateHistoryKey key)
    {
        for (StateHistory run : simRuns)
        {
            if (run.getKey().equals(key))
                return run;
        }

        return null;
    }

    public StateHistoryKey getKeyFromRow(int row)
    {
        if (keys.size() > row) {
            return keys.get(row);
        }
        return null;
    }

    public StateHistory getRunFromRow(int row)
    {
        return getRunFromKey(getKeyFromRow(row));
    }

    public StateHistory getCurrentRun()
    {
        return currentRun;
    }

    public void setCurrentRun(StateHistoryKey key)
    {
        StateHistory run = getRunFromKey(key);
        if (run != null && run != currentRun)
        {
            currentRun = run;
        }

    }

    public void setCurrentRun(StateHistory run)
    {
    	currentRun = run;
    }

    public void addRunToList(StateHistory run)
    {
         simRuns.add(run);
         keys.add(run.getKey());
         this.currentRun = run;
         setAllItems(simRuns);
    }

    public void setSpacecraft(SpacecraftBody spacecraft)
    {
    	this.spacecraft = spacecraft;
    }

    public TrajectoryActor addRun(StateHistory run)
    {
        if (stateHistoryToRendererMap.get(run) != null)
        {
        	TrajectoryActor trajActor = stateHistoryToRendererMap.get(run);
            trajActor.SetVisibility(1);
            return trajActor;
        }

        // Get the trajectory actor the state history segment
        TrajectoryActor trajectoryActor = new TrajectoryActor(run.getTrajectory());
        trajectoryActor.setColoringFunction(StateHistoryColoringFunctions.PER_TABLE.getColoringFunction(), Colormaps.getNewInstanceOfBuiltInColormap("Rainbow"));

        trajectoryActor.setMinMaxFraction(run.getMinDisplayFraction(), run.getMaxDisplayFraction());
        trajectoryActor.VisibilityOn();
        trajectoryActor.GetMapper().Update();

        stateHistoryToRendererMap.put(run, trajectoryActor);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, trajectoryActor);

        return trajectoryActor;
    }

    public void removeRun(StateHistoryKey key)
    {
        if (!containsKey(key))
            return;

        StateHistory run = getRunFromKey(key);
        stateHistoryToRendererMap.remove(run);

        this.currentRun = null;
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, run);
    }

    public void removeRuns(StateHistoryKey[] keys)
    {
        for (StateHistoryKey key : keys) {
            removeRun(key);
        }
    }

    /**
     * Remove all images of the specified source
     * @param source
     */
//    public void removeRuns(StateHistorySource source)
//    {
//        for (StateHistoryModel run : simRuns)
//            if (run.getKey().source == source)
//                removeRun(run.getKey());
//    }

    public void setShowTrajectories(boolean show)
    {
    	//TODO fix
//        for (StateHistory run : simRuns)
//            run.setShowSpacecraft(show);
    }

    public ArrayList<vtkProp> getProps()
    {
    	ArrayList<vtkProp> props = new ArrayList<vtkProp>();
    	for (StateHistory history : stateHistoryToRendererMap.keySet())
    		props.add(stateHistoryToRendererMap.get(history));

   		props.add(spacecraft.getActor());
   		props.add(scDirectionMarker.getActor());
   		props.add(spacecraftLabelActor);
   		props.add(earthDirectionMarker.getActor());
   		props.add(sunDirectionMarker.getActor());

//   		for (vtkProp prop : props)
//   		{
//   			prop.SetDragable(0);
//   			prop.SetPickable(0);
//   		}

    	return props;

    	//TODO fix
//        if (currentRun != null)
//            return currentRun.getProps();
//        else
//            return new ArrayList<vtkProp>();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
    	//TODO fix
//        if (currentRun != null)
//            return currentRun.getClickStatusBarText(prop, cellId, pickPosition);
//        else
            return "No simulation run selected";
    }

//    public String getRunName(vtkActor actor)
//    {
//        if (currentRun != null)
//            return currentRun.getKey().name;
//        else
//            return "No simulation run selected";
//    }

    public StateHistory getRun(vtkActor actor)
    {
        return currentRun;
    }

    public StateHistory getRun(StateHistoryKey key)
    {
        return getRunFromKey(key);
    }

    public boolean containsRun(StateHistoryKey key)
    {
        return containsKey(key);
    }

//    public void setTimeFraction(Double timeFraction)
//    {
//        if (currentRun != null)
//           currentRun.setTimeFraction(timeFraction);
//    }

    public Double getTimeFraction()
    {
        if (currentRun!= null)
            return currentRun.getTimeFraction();
        else
            return null;
    }

    public TrajectoryActor getTrajectoryActorForStateHistory(StateHistory segment)
    {
    	return stateHistoryToRendererMap.get(segment);
    }

//    public void setOffset(double offset)
//    {
//        if (currentRun != null)
//            currentRun.setOffset(offset);
//    }
//
//    public double getOffset()
//    {
//        if (currentRun!= null)
//            return currentRun.getOffset();
//        else
//            return 0.0;
//    }

    public Double getPeriod()
    {
        if (currentRun != null)
            return ((HasTime)currentRun).getPeriod();
        else
            return 0.0;
    }

    public int size()
    {
        return simRuns.size();
    }

    public List<StateHistoryKey> getKeys()
    {
        return keys;
    }

    @Override
	public ImmutableList<StateHistory> getAllItems()
	{
    	return ImmutableList.copyOf(simRuns);
	}

    @Override
    public int getNumItems()
    {
    	return simRuns.size();
    }

    public boolean isStateHistoryMapped(StateHistory segment)
    {
    	return stateHistoryToRendererMap.get(segment) != null;
    }

    public boolean getVisibility(StateHistory segment)
    {
    	if (isStateHistoryMapped(segment) == false ) return false;
    	TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
    	if (renderer == null) return false;
        return (renderer.GetVisibility() == 1);
    }

    public void setVisibility(StateHistory segment, boolean visibility)
    {
    	TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
    	int isVisible = (visibility == true) ? 1 : 0;
        renderer.SetVisibility(isVisible);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);
    }

    public void setTrajectoryColor(StateHistory segment, Color color)
    {
    	double[] colorAsIntArray = new double[] {color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()};
    	double[] colorAsDoubleArray = new double[] {color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, color.getAlpha()/255.0};
    	segment.getTrajectory().setTrajectoryColor(colorAsIntArray);
    	TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
    	renderer.setColoringFunction(null, null);
    	renderer.setTrajectoryColor(colorAsDoubleArray);
    	refreshColoring(segment);
//    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);
    }

    public void refreshColoring(StateHistory segment)
    {
    	TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
    	if (renderer == null) return;
    	renderer.setTrajectoryColor(segment.getTrajectoryColor());
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);
    }

    //time updates
    private void updateTimeBarActor(double time)
    {
//    	if (timeBarActor != null)
//        {
//            updateTimeBarPosition(renderer.getPanelWidth(), renderer.getPanelHeight());
//            updateTimeBarValue(time);
//        }
    }

    private void updateSunPosition(StateHistory history, double time)
    {
	   sunPosition = history.getSunPosition();
       vtkMatrix4x4 sunMarkerMatrix = new vtkMatrix4x4();


       double[] sunMarkerPosition = new double[3];
       sunDirection = new double[3];
       double[] sunViewpoint = new double[3];
       double[] sunViewDirection = new double[3];
       MathUtil.unorm(sunPosition, sunDirection);
       MathUtil.vscl(JupiterScale, sunDirection, sunViewpoint);
       MathUtil.vscl(-1.0, sunDirection, sunViewDirection);
       int result = smallBodyModel.computeRayIntersection(sunViewpoint, sunViewDirection, sunMarkerPosition);
       for (int i=0; i<3; i++)
       {
           sunMarkerMatrix.SetElement(i, 3, sunMarkerPosition[i]);
       }

	   sunDirectionMarker.updateSunPosition(sunPosition, sunMarkerPosition);
    }

    private void updateEarthPosition(StateHistory history, double time)
    {
        earthPosition = history.getEarthPosition();
        vtkMatrix4x4 earthMarkerMatrix = new vtkMatrix4x4();

        double[] earthMarkerPosition = new double[3];
        double[] earthDirection = new double[3];
        double[] earthViewpoint = new double[3];
        double[] earthViewDirection = new double[3];
        MathUtil.unorm(earthPosition, earthDirection);
        MathUtil.vscl(JupiterScale, earthDirection, earthViewpoint);
        MathUtil.vscl(-1.0, earthDirection, earthViewDirection);
        int result = smallBodyModel.computeRayIntersection(earthViewpoint, earthViewDirection, earthMarkerPosition);
        for (int i=0; i<3; i++)
        {
            earthMarkerMatrix.SetElement(i, 3, earthMarkerPosition[i]);
        }

        earthDirectionMarker.updateEarthPosition(earthPosition, earthMarkerPosition);

    }

    private void updateSpacecraftPosition(StateHistory history, double time)
    {
    	vtkMatrix4x4 spacecraftBodyMatrix = new vtkMatrix4x4();
        vtkMatrix4x4 spacecraftIconMatrix = new vtkMatrix4x4();
        vtkMatrix4x4 fovMatrix = new vtkMatrix4x4();
        vtkMatrix4x4 fovRotateXMatrix = new vtkMatrix4x4();
        vtkMatrix4x4 fovRotateYMatrix = new vtkMatrix4x4();
        vtkMatrix4x4 fovRotateZMatrix = new vtkMatrix4x4();
        vtkMatrix4x4 fovScaleMatrix = new vtkMatrix4x4();

        double iconScale = 1.0;
        // set to identity
        spacecraftBodyMatrix.Identity();
        spacecraftIconMatrix.Identity();
        fovMatrix.Identity();
        fovRotateXMatrix.Identity();
        fovRotateYMatrix.Identity();
        fovRotateZMatrix.Identity();
        double[] xaxis = history.getCurrentValue().getSpacecraftXAxis();
        double[] yaxis = history.getCurrentValue().getSpacecraftYAxis();
        double[] zaxis = history.getCurrentValue().getSpacecraftZAxis();
        // set body orientation matrix
        for (int i=0; i<3; i++)
        {
            spacecraftBodyMatrix.SetElement(i, 0, xaxis[i]);
            spacecraftBodyMatrix.SetElement(i, 1, yaxis[i]);
            spacecraftBodyMatrix.SetElement(i, 2, zaxis[i]);
        }

        // create the icon matrix, which is just the body matrix scaled by a factor
        for (int i=0; i<3; i++)
            spacecraftIconMatrix.SetElement(i, i, iconScale);
        spacecraftIconMatrix.Multiply4x4(spacecraftIconMatrix, spacecraftBodyMatrix, spacecraftIconMatrix);


        spacecraftPosition = history.getSpacecraftPosition();
        double[] spacecraftMarkerPosition = new double[3];
        double[] spacecraftDirection = new double[3];
        double[] spacecraftViewpoint = new double[3];
        double[] spacecraftViewDirection = new double[3];
        MathUtil.unorm(spacecraftPosition, spacecraftDirection);
        MathUtil.vscl(JupiterScale, spacecraftDirection, spacecraftViewpoint);
        MathUtil.vscl(-1.0, spacecraftDirection, spacecraftViewDirection);
        int result = smallBodyModel.computeRayIntersection(spacecraftViewpoint, spacecraftViewDirection, spacecraftMarkerPosition);

        //rotates spacecraft pointer to point in direction of spacecraft - Alex W
        double[] spacecraftPos = spacecraftMarkerPosition;
        double[] spacecraftPosDirection = new double[3];
        MathUtil.unorm(spacecraftPos, spacecraftPosDirection);
        double[] rotationAxisSpacecraft = new double[3];
        MathUtil.vcrss(spacecraftPosDirection, zAxis, rotationAxisSpacecraft);

        double rotationAngleSpacecraft = ((180.0/Math.PI)*MathUtil.vsep(zAxis, spacecraftPosDirection));

        vtkTransform spacecraftMarkerTransform = new vtkTransform();
        spacecraftMarkerTransform.Translate(spacecraftPos);
        spacecraftMarkerTransform.RotateWXYZ(-rotationAngleSpacecraft, rotationAxisSpacecraft[0], rotationAxisSpacecraft[1], rotationAxisSpacecraft[2]);

     // set translation
        for (int i=0; i<3; i++)
        {
            spacecraftBodyMatrix.SetElement(i, 3, spacecraftPosition[i]);
            spacecraftIconMatrix.SetElement(i, 3, spacecraftPosition[i]);
//            fovMatrix.SetElement(i, 3, spacecraftPosition[i]);

        }

        spacecraft.getActor().SetUserMatrix(spacecraftIconMatrix);

        spacecraftLabelActor.SetAttachmentPoint(spacecraftPosition);
        spacecraftLabelActor.setDistanceText(history.getCurrentValue(), spacecraftPosition, smallBodyModel);

//        spacecraftFovActor.SetUserMatrix(fovMatrix);
        //            spacecraftFovActor.SetUserMatrix(spacecraftBodyMatrix);

        scDirectionMarker.getActor().SetUserTransform(spacecraftMarkerTransform);

//        spacecraftBoresight.Modified();
        spacecraft.getActor().Modified();
        scDirectionMarker.getActor().Modified();
        spacecraftLabelActor.Modified();
//        spacecraftFov.Modified();
//        spacecraftMarkerBody.Modified();
    }

    private void updateLighting(double time)
    {
//    	// toggle for lighting - Alex W
//        if (timeFraction >= 0.0 && showLighting)
//        {
//            renderer.setFixedLightDirection(sunDirection);
//            renderer.setLighting(LightingType.FIXEDLIGHT);
//            updateActorVisibility();
//        }
//        else
//            renderer.setLighting(LightingType.LIGHT_KIT);
    }

	public double[] updateLookDirection(RendererLookDirection lookDirection, double scalingFactor)
    {
		double[] focalpoint = {0,0,0};


		// set camera to earth, spacecraft, or sun views - Alex W
		if(lookDirection == RendererLookDirection.EARTH)
		{
			double[] newEarthPos = new double[3];
			MathUtil.unorm(earthPosition, newEarthPos);
			MathUtil.vscl(scalingFactor, newEarthPos, newEarthPos);
		    currentLookFromDirection = newEarthPos;

			return newEarthPos;
//			renderer.setCameraOrientation(newEarthPos, renderer.getCameraFocalPoint(), upVector, renderer.getCameraViewAngle());
		}
		else if(lookDirection == RendererLookDirection.SPACECRAFT)
		{
			double[] boresight = new double[] {currentRun.getSpacecraftPosition()[0]*0.9,
					currentRun.getSpacecraftPosition()[1]*0.9,
					currentRun.getSpacecraftPosition()[2]*0.9
			};
			currentLookFromDirection = boresight;
			return boresight;
//			renderer.setCameraOrientation(currentFlybyStateHistory.getSpacecraftPosition(), renderer.getCameraFocalPoint(), upVector, renderer.getCameraViewAngle());
		}
		else if(lookDirection == RendererLookDirection.SUN)
		{
			double[] newSunPos = new double[3];
			MathUtil.unorm(sunPosition, newSunPos);
			MathUtil.vscl(scalingFactor, newSunPos, newSunPos);
			currentLookFromDirection = newSunPos;
			return newSunPos;
//			renderer.setCameraOrientation(newSunPos, renderer.getCameraFocalPoint(), upVector, renderer.getCameraViewAngle());
		}
		else if (lookDirection == RendererLookDirection.SPACECRAFT_THIRD)
		{
			double[] thirdPerson = new double[] {currentRun.getSpacecraftPosition()[0]*1.1,
					currentRun.getSpacecraftPosition()[1]*1.1,
					currentRun.getSpacecraftPosition()[2]*1.1
			};
			currentLookFromDirection = thirdPerson;
			return thirdPerson;
		}
		else //free view mode
		{
			currentLookFromDirection = currentRun.getSpacecraftPosition();
			return currentRun.getSpacecraftPosition();
		}
    }

    public void setTimeFraction(StateHistory state, Double timeFraction)
    {
        if (state != null && spacecraft.getActor() != null)
        {
        	updateSpacecraftPosition(state, timeFraction);
        	updateEarthPosition(state, timeFraction);
        	updateSunPosition(state, timeFraction);



//
//            String speedText = String.format("%7.1f km %7.3f km/sec   .", radius, speed);
//
//            // set the current orientation
//
//
//            // create spacecraft matrices
//
//
//            vtkMatrix4x4 sunMarkerMatrix = new vtkMatrix4x4();
//            vtkMatrix4x4 earthMarkerMatrix = new vtkMatrix4x4();
//
//            vtkMatrix4x4 spacecraftInstrumentMatrix = new vtkMatrix4x4();
//
//            // set to identity
//
//            sunMarkerMatrix.Identity();
//            earthMarkerMatrix.Identity();
//            //fovMatrix = new vtkMatrix4x4
//
//
//            // rotate the FOV about the Z axis
//            //            double sinRotZ = Math.sin(spacecraftRotationZ);
//            //            double cosRotZ = Math.cos(spacecraftRotationZ);
//            //            fovRotateZMatrix.SetElement(0, 0, cosRotZ);
//            //            fovRotateZMatrix.SetElement(1, 1, cosRotZ);
//            //            fovRotateZMatrix.SetElement(0, 1, sinRotZ);
//            //            fovRotateZMatrix.SetElement(1, 0, -sinRotZ);
//
//            // scale the FOV
//            //            fovScaleMatrix.SetElement(0, 0, fovHeight);
//            //            fovScaleMatrix.SetElement(1, 1, fovWidth);
//            //            fovScaleMatrix.SetElement(2, 2, fovDepth);
//
//            // rotate the FOV about the Y axis
//            //            double sinRotY = Math.sin(spacecraftRotationY);
//            //            double cosRotY = Math.cos(spacecraftRotationY);
//            //            fovRotateYMatrix.SetElement(0, 0, cosRotY);
//            //            fovRotateYMatrix.SetElement(2, 2, cosRotY);
//            //            fovRotateYMatrix.SetElement(0, 2, sinRotY);
//            //            fovRotateYMatrix.SetElement(2, 0, -sinRotY);
//            //
//            //            fovMatrix.Multiply4x4(fovScaleMatrix, fovRotateZMatrix, spacecraftInstrumentMatrix);
//            //            fovMatrix.Multiply4x4(fovRotateYMatrix, spacecraftInstrumentMatrix, spacecraftInstrumentMatrix);
//            //
//            ////            spacecraftFovMatrix.Multiply4x4(fovRotateYMatrix, fovRotateZMatrix, spacecraftInstrumentMatrix);
//            //
//            //            fovMatrix.Multiply4x4(spacecraftBodyMatrix, spacecraftInstrumentMatrix, fovMatrix);
//
//            // set translation
//            for (int i=0; i<3; i++)
//            {
//                sunMarkerMatrix.SetElement(i, 3, sunMarkerPosition[i]);
//                earthMarkerMatrix.SetElement(i, 3, earthMarkerPosition[i]);
//            }
//
//            //            spacecraftBoresightActor.SetUserMatrix(matrix);
//
////            monolithBodyActor.SetUserMatrix(spacecraftBodyMatrix);
//
//
//            earthMarkerActor.SetUserMatrix(earthMarkerMatrix);
//            //            earthMarkerHeadActor.SetUserMatrix(earthMarkerMatrix);
//            //sunMarkerActor.SetUserMatrix(sunMarkerMatrix);
//
////          monolithBody.Modified();
//
//
//            earthMarkerHead.Modified();
//            earthMarkerBody.Modified();
//            sunAssembly.Modified();

            this.pcs.firePropertyChange("POSITION_CHANGED" /*Properties.MODEL_CHANGED*/, null, null);
        }
    }


    public void setSpacecraftColor(Color color)
    {
    	spacecraft.setColor(color);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
    }

    public void setSpacecraftVisibility(boolean visible)
    {
    	spacecraft.getActor().SetVisibility(visible == true ? 1: 0);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
    }

    public void setSpacecraftLabelVisibility(boolean visible)
    {
    	spacecraftLabelActor.SetVisibility(visible == true ? 1: 0);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftLabelActor);
    }

    public void setSpacecraftDirectionMarkerVisibility(boolean visible)
    {
    	scDirectionMarker.getActor().SetVisibility(visible == true ? 1: 0);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, scDirectionMarker);
    }

    public void setSpacecraftDirectionMarkerSize(int radius)
    {
    	scDirectionMarker.setSpacecraftPointerSize(radius);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, scDirectionMarker);
    }

    public void setEarthDirectionMarkerVisibility(boolean visible)
    {
    	earthDirectionMarker.getActor().SetVisibility(visible == true ? 1: 0);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
    }

    public void setSpacecraftSize(double scale)
    {
    	spacecraft.setScale(scale);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
    }

    public void setEarthDirectionMarkerSize(int radius)
    {
    	earthDirectionMarker.setEarthPointerSize(radius);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
    }

    public void setSunDirectionMarkerVisibility(boolean visible)
    {
    	sunDirectionMarker.getActor().SetVisibility(visible == true ? 1: 0);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, sunDirectionMarker);
    }

    public void setSunDirectionMarkerSize(int radius)
    {
    	sunDirectionMarker.setSunPointerSize(radius);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, sunDirectionMarker);
    }

    public void setDistanceText(String distanceText)
    {
    	spacecraftLabelActor.setDistanceString(distanceText);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftLabelActor);
    }

    public void setDistanceTextFont(Font distanceTextFont)
    {
    	spacecraftLabelActor.setDistanceStringFont(distanceTextFont);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftLabelActor);
    }

    public void setDistanceTextVisiblity(boolean isVisible)
    {
    	int visible = (isVisible) ? 1 : 0;
    	spacecraftLabelActor.SetVisibility(visible);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftLabelActor);
    }

    public void setEarthDirectionMarkerColor(Color color)
    {
    	earthDirectionMarker.setColor(color);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
    }

    public void setSunDirectionMarkerColor(Color color)
    {
    	sunDirectionMarker.setColor(color);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, sunDirectionMarker);
    }

    public void setScDirectionMarkerColor(Color color)
    {
    	scDirectionMarker.setColor(color);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, scDirectionMarker);
    }

	public double[] getCurrentLookFromDirection()
	{
		return currentLookFromDirection;
	}

	public void setTrajectoryMinMax(StateHistory run, double min, double max)
	{
		TrajectoryActor trajActor = stateHistoryToRendererMap.get(run);
		if (trajActor != null)
        {
			trajActor.setMinMaxFraction(min, max);
			trajActor.Modified();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, trajActor);
        }
	}

	public void setOthersHiddenExcept(List<StateHistory> history)
	{
		for (StateHistory hist : getAllItems())
		{
			setVisibility(hist, history.contains(hist));
		}
	}

	public SmallBodyModel getSmallBodyModel()
	{
		return smallBodyModel;
	}
}
