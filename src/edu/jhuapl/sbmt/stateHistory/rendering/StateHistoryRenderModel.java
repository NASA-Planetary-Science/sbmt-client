package edu.jhuapl.sbmt.stateHistory.rendering;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.sbmt.client.SmallBodyModel;

public class StateHistoryRenderModel
{
	List<StateHistoryRenderModelChangedListener> listeners = new ArrayList<StateHistoryRenderModelChangedListener>();
    private double markerRadius = 0.5;
    private double markerHeight = 0.5;

    private boolean move;
    private double iconScale = 10.0;

    private boolean showSpacecraft;
    private boolean showSpacecraftBody;
    private boolean showSpacecraftLabel;
    private boolean showSpacecraftFov;

    private boolean showSpacecraftMarker;
    private boolean showEarthMarker;
    private boolean showSunMarker;
    private boolean showLighting;
    private boolean earthView;
    private boolean sunView;

    private int earthPointerSize;
    private int sunPointerSize;
    private int spacecraftPointerSize;

    private double[] sunDirection = { 0.0, 1.0, 0.0 };
    public double[] getSunDirection() { return sunDirection; }

    private SmallBodyModel smallBodyModel;

    double cameraViewAngle;
    double viewAngle;

    private boolean showTimeBar = true;
    private boolean showScalarBar = false;

    private double[] fixedLightDirection;
    private double[] cameraFocalPoint;
    private LightingType lightingType;

	public StateHistoryRenderModel()
	{
		// TODO Auto-generated constructor stub
	}

	public void addStateHistoryRenderModelChangedListener(StateHistoryRenderModelChangedListener listener)
    {
    	listeners.add(listener);
    }

    /*****
    *
    * View Controls
    *
    *
    *****/

   public void setShowSpacecraft(boolean show)
   {
       this.showSpacecraft = show;
       this.showSpacecraftFov = false;
       this.showSpacecraftLabel = show;
       fireShowSpacecraftChangedListener(show);
//       updateActorVisibility();
   }


   // toggle the visibility of trajectories - Alex W
      public void showTrajectory(boolean show)
      {
      	fireShowTrajectoryChangedListener(show);
//          if(show)
//          {
//              for(int i = 0; i<stateHistoryActors.size(); i++)
//              {
//                  if(trajectoryActor.equals(stateHistoryActors.get(i)))
//                  {
//                      stateHistoryActors.get(i).VisibilityOn();
//                      stateHistoryActors.get(i).Modified();
//                  }
//              }
//              showing = true;
//          }
//          else
//          {
//              for(int i = 0; i<stateHistoryActors.size(); i++)
//              {
//                  if(trajectoryActor.equals(stateHistoryActors.get(i)))
//                  {
//                      stateHistoryActors.get(i).VisibilityOff();
//                      stateHistoryActors.get(i).Modified();
//                  }
  //
//              }
//              showing = false;
//          }
  //
//          this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
      }

  	private void fireShowLightingChangedListener(boolean showLighting)
  	{
  		for (StateHistoryRenderModelChangedListener listener : listeners)
  		{
  			listener.showLightingChanged(showLighting);
  		}
  	}

  	private void fireShowSpacecraftChangedListener(boolean showSpacecraft)
  	{
  		for (StateHistoryRenderModelChangedListener listener : listeners)
  		{
  			listener.showSpacecraftChanged(showSpacecraft);
  		}
  	}

  	private void fireShowSpacecraftDirectionChangedListener(boolean showSpacecraftDirection)
  	{
  		for (StateHistoryRenderModelChangedListener listener : listeners)
  		{
  			listener.showSpacecraftDirectionChanged(showSpacecraftDirection);
  		}
  	}

  	private void fireShowSunChangedListener(boolean showSun)
  	{
  		for (StateHistoryRenderModelChangedListener listener : listeners)
  		{
  			listener.showSunChanged(showSun);
  		}
  	}

  	private void fireShowEarthChangedListener(boolean showEarth)
  	{
  		for (StateHistoryRenderModelChangedListener listener : listeners)
  		{
  			listener.showEarthChanged(showEarth);
  		}
  	}

  	private void fireShowScalarBarChangedListener(boolean showScalarBar)
  	{
  		for (StateHistoryRenderModelChangedListener listener : listeners)
  		{
  			listener.showScalarBarChanged(showScalarBar);
  		}
  	}

  	private void fireShowTrajectoryChangedListener(boolean showTrajectory)
  	{
  		for (StateHistoryRenderModelChangedListener listener : listeners)
  		{
  			listener.showTrajectoryChanged(showTrajectory);
  		}
  	}


      /*****
       *
       * Renderer Updates
       *
       *****/

      // sets the visibility of actors, called in StateHistoryPanel and gets the toggle info in it - Alex W
      public void setActorVisibility(String actorName, boolean visibility)
      {
          //maps the actors+data
          if(actorName.equalsIgnoreCase("Earth"))
              fireShowEarthChangedListener(visibility);
          if(actorName.equalsIgnoreCase("Sun"))
              fireShowSunChangedListener(visibility);
          if(actorName.equalsIgnoreCase("SpacecraftMarker"))
          	fireShowSpacecraftDirectionChangedListener(visibility);
          if(actorName.equalsIgnoreCase("Spacecraft"))
          	fireShowSpacecraftChangedListener(visibility);
          if(actorName.equalsIgnoreCase("Trajectory"))
          	fireShowTrajectoryChangedListener(visibility);
          if(actorName.equalsIgnoreCase("Lighting"))
              fireShowLightingChangedListener(visibility);
      }

//      public void setColoringIndex(int index) throws IOException
//      {
//          if (coloringIndex != index)
//          {
//              coloringIndex = index;
//          }
//      }
  //
//      public int getColoringIndex()
//      {
//          return coloringIndex;
//      }

      public void setShowTimeBar(boolean enabled)
      {
          this.showTimeBar = enabled;
          fireShowTimeBarChangedListener(enabled);
//          // The following forces the scale bar to be redrawn.
//          this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//          // Note that we call firePropertyChange *twice*. Not really sure why.
//          this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
      }

      public boolean isShowTimeBar()
      {
          return showTimeBar;
      }

      public void setShowScalarBar(boolean enabled)
      {
          this.showScalarBar = enabled;
          fireShowScalarBarChangedListener(enabled);
//          this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//          // Note that we call firePropertyChange *twice*. Not really sure why.
//          this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
      }

      public boolean getShowScalarBar()
      {
          return showScalarBar;
      }



      private void fireShowTimeBarChangedListener(boolean showTimeBar)
  	{
  		for (StateHistoryRenderModelChangedListener listener : listeners)
  		{
  			listener.showTimeBarChanged(showTimeBar);
  		}
  	}

	public double getMarkerRadius()
	{
		return markerRadius;
	}

	public void setMarkerRadius(double markerRadius)
	{
		this.markerRadius = markerRadius;
	}

	public double getMarkerHeight()
	{
		return markerHeight;
	}

	public void setMarkerHeight(double markerHeight)
	{
		this.markerHeight = markerHeight;
	}

	public boolean isMove()
	{
		return move;
	}

	public void setMove(boolean move)
	{
		this.move = move;
	}

	public double getIconScale()
	{
		return iconScale;
	}

	public void setIconScale(double iconScale)
	{
		this.iconScale = iconScale;
	}

	public boolean isShowSpacecraftBody()
	{
		return showSpacecraftBody;
	}

	public void setShowSpacecraftBody(boolean showSpacecraftBody)
	{
		this.showSpacecraftBody = showSpacecraftBody;
	}

	public boolean isShowSpacecraftLabel()
	{
		return showSpacecraftLabel;
	}

	public void setShowSpacecraftLabel(boolean showSpacecraftLabel)
	{
		this.showSpacecraftLabel = showSpacecraftLabel;
	}

	public boolean isShowSpacecraftFov()
	{
		return showSpacecraftFov;
	}

	public void setShowSpacecraftFov(boolean showSpacecraftFov)
	{
		this.showSpacecraftFov = showSpacecraftFov;
	}

	public boolean isShowSpacecraftMarker()
	{
		return showSpacecraftMarker;
	}

	public void setShowSpacecraftMarker(boolean showSpacecraftMarker)
	{
		this.showSpacecraftMarker = showSpacecraftMarker;
	}

	public boolean isShowEarthMarker()
	{
		return showEarthMarker;
	}

	public void setShowEarthMarker(boolean showEarthMarker)
	{
		this.showEarthMarker = showEarthMarker;
	}

	public boolean isShowSunMarker()
	{
		return showSunMarker;
	}

	public void setShowSunMarker(boolean showSunMarker)
	{
		this.showSunMarker = showSunMarker;
	}

	public boolean isShowLighting()
	{
		return showLighting;
	}

	public void setShowLighting(boolean showLighting)
	{
		this.showLighting = showLighting;
	}

	public boolean isShowEarthView()
	{
		return earthView;
	}

	public void setShowEarthView(boolean earthView)
	{
		this.earthView = earthView;
	}

	public boolean isShowSunView()
	{
		return sunView;
	}

	public void setShowSunView(boolean sunView)
	{
		this.sunView = sunView;
	}

	public SmallBodyModel getSmallBodyModel()
	{
		return smallBodyModel;
	}

	public void setSmallBodyModel(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
	}

	public void setSunDirection(double[] sunDirection)
	{
		this.sunDirection = sunDirection;
	}

	public double getCameraViewAngle()
	{
		return cameraViewAngle;
	}

	public void setCameraViewAngle(double cameraViewAngle)
	{
		this.cameraViewAngle = cameraViewAngle;
	}

	public int getEarthPointerSize()
	{
		return earthPointerSize;
	}

	public void setEarthPointerSize(int earthPointerSize)
	{
		this.earthPointerSize = earthPointerSize;
	}

	public int getSunPointerSize()
	{
		return sunPointerSize;
	}

	public void setSunPointerSize(int sunPointerSize)
	{
		this.sunPointerSize = sunPointerSize;
	}

	public int getSpacecraftPointerSize()
	{
		return spacecraftPointerSize;
	}

	public void setSpacecraftPointerSize(int spacecraftPointerSize)
	{
		this.spacecraftPointerSize = spacecraftPointerSize;
	}

	public double getViewAngle()
	{
		return viewAngle;
	}

	public void setViewAngle(double viewAngle)
	{
		this.viewAngle = viewAngle;
	}

	public double[] getFixedLightDirection()
	{
		return fixedLightDirection;
	}

	public void setFixedLightDirection(double[] fixedLightDirection)
	{
		this.fixedLightDirection = fixedLightDirection;
	}

	public LightingType getLightingType()
	{
		return lightingType;
	}

	public void setLightingType(LightingType lightingType)
	{
		this.lightingType = lightingType;
	}

	public double[] getCameraFocalPoint()
	{
		return cameraFocalPoint;
	}

	public void setCameraFocalPoint(double[] cameraFocalPoint)
	{
		this.cameraFocalPoint = cameraFocalPoint;
	}

}
