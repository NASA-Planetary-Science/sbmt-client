package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import vtk.vtkActor;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.feature.FeatureAttr;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.sbmt.lidar.BasicLidarPoint;
import edu.jhuapl.sbmt.lidar.LidarManager;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.vtk.VtkPointPainter;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrack;

import glum.item.ItemEventListener;

public class PlannedLidarActor implements PlannedDataActor, LidarManager
{

    private boolean staticFootprint = false;
    private boolean staticFootprintSet = false;
    private VtkPointPainter lidarPointPainter;
    private PlannedLidarTrack plannedTrack;
    private Color color;
    private double time;
    private String instrumentName;

	public PlannedLidarActor(PlannedLidarTrack plannedTrack)
	{
		this.plannedTrack = plannedTrack;
		lidarPointPainter = new VtkPointPainter(this);
	}

	@Override
	public void updatePointing(double[] scPos, double[] frus1, double[] frus2, double[] frus3, double[] frus4,
			int height, int width, int depth)
	{
//		updatePointing(scPos, tgtPos, time, range);
	}

	public void updatePointing(double[] scPos, double[] tgtPos, double time, double range)
	{
		if (isStaticFootprintSet() == true) return;
		System.out.println("PlannedLidarActor: updatePointing: setting pointing " + new Vector3D(tgtPos));
		BasicLidarPoint surfacePoint = new BasicLidarPoint(tgtPos, scPos, time, range, 1.0);
		lidarPointPainter.setData(plannedTrack, surfacePoint);
		lidarPointPainter.setPointSize(3.0);
		lidarPointPainter.vtkUpdateState();
//		setTime(time);
		if (tgtPos[0] != 0 || tgtPos[1] != 0 || tgtPos[2] != 0)
		{
			System.out.println("PlannedLidarActor: updatePointing: locking");
			setStaticFootprintSet(true);
		}
	}

	@Override
	public void SetVisibility(int visible)
	{
		if (lidarPointPainter.getActor() == null) return;
		lidarPointPainter.getActor().SetVisibility(visible);
	}

	@Override
	public vtkActor getFootprintActor()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public vtkActor getFootprintBoundaryActor()
	{
		return lidarPointPainter.getActor();
	}

	@Override
	public double getTime()
	{
		return time;
	}

	/**
	 * @return the staticFootprint
	 */
	public boolean isStaticFootprint()
	{
		return staticFootprint;
	}

	/**
	 * @param staticFootprint the staticFootprint to set
	 */
	public void setStaticFootprint(boolean staticFootprint)
	{
		this.staticFootprint = staticFootprint;
	}

	/**
	 * @return the staticFootprintSet
	 */
	public boolean isStaticFootprintSet()
	{
		return staticFootprintSet;
	}

	/**
	 * @param staticFootprintSet the staticFootprintSet to set
	 */
	public void setStaticFootprintSet(boolean staticFootprintSet)
	{
		this.staticFootprintSet = staticFootprintSet;
	}

	/**
	 * @return the instrumentName
	 */
	public String getInstrumentName()
	{
		return instrumentName;
	}

	/**
	 * @param instrumentName the instrumentName to set
	 */
	public void setInstrumentName(String instrumentName)
	{
		this.instrumentName = instrumentName;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color)
	{
		this.color = color;
//		setBoundaryColor(color);
	}

	public void setTime(double time)
	{
		this.time = time;
	}

	//--------------------

	@Override
	public ImmutableSet getSelectedItems()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeItems(Collection aItemC)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setAllItems(Collection aItemC)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectedItems(Collection aItemC)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(ItemEventListener aListener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delListener(ItemEventListener aListener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public ImmutableList getAllItems()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumItems()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clearCustomColorProvider(List aItemL)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public ColorProvider getColorProviderSource(Object aItem)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ColorProvider getColorProviderTarget(Object aItem)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FeatureAttr getFeatureAttrFor(Object aItem, FeatureType aFeatureType)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getIsVisible(Object aItem)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public LidarPoint getLidarPointAt(Object aItem, int aIdx)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3D getTargetPosition(Object aItem, int aIdx)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getRadialOffset()
	{
		return 0;
	}

	@Override
	public Vector3D getTranslation(Object aItem)
	{
		return Vector3D.ZERO;
	}

	@Override
	public boolean hasCustomColorProvider(Object aItem)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void installCustomColorProviders(Collection aItemC, ColorProvider aSrcCP, ColorProvider aTgtCP)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void installGroupColorProviders(GroupColorProvider aSrcGCP, GroupColorProvider aTgtGCP)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setIsVisible(List aItemL, boolean aBool)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setOthersHiddenExcept(List aItemL)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setRadialOffset(double aRadialOffset)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setShowSourcePoints(boolean aShowSourcePoints)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setTranslation(Collection aItemC, Vector3D aVect)
	{
		// TODO Auto-generated method stub

	}



}
