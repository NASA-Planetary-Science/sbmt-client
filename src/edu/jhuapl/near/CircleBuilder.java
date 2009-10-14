package edu.jhuapl.near;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;

import java.awt.event.MouseEvent;
import java.util.*;

/**
 *
 */
public class CircleBuilder extends ShapeBuilder
{
    private final WorldWindow wwd;
    private ArrayList<Position> positions = new ArrayList<Position>();
    private Polyline circle;
    protected Position center;
    private double radius;
    private CircleCenterValidator validator;

    public static final String RADIUS_CHANGED_PROPERTY = "RADIUS_CHANGED";

    public static interface CircleCenterValidator
    {
    	public Position validate(Position pos);
    }
    
    /**
     * Construct a new circle builder using the specified polyline and layer and drawing events from the specified world
     * window. Either or both the polyline and the layer may be null, in which case the necessary object is created.
     *
     * @param wwd       the world window to draw events from.
     */
    public CircleBuilder(final WorldWindow wwd)
    {
    	this(wwd, null);
    }

    public CircleBuilder(final WorldWindow wwd, CircleCenterValidator validator)
    {
    	super(wwd);
    	
        this.wwd = wwd;

        this.circle = new Polyline();
        this.circle.setFollowTerrain(true);
        this.circle.setClosed(true);
        this.getLayer().addRenderable(this.circle);
        
        this.validator = validator;
    }
    
    public void mouseClicked(MouseEvent mouseEvent)
    {
        if (this.isArmed() && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
        	if (!this.isActive())
        	{
    			Position newCenterPosition = this.wwd.getCurrentPosition();
        		if (this.validator != null)
        			newCenterPosition =	this.validator.validate(newCenterPosition);
        			
        		if (this.validator == null || newCenterPosition != null)
        		{
        			this.setActive(true);
        			clear();
        			this.center = newCenterPosition;
        		}
        	}
        	else
        	{
        		this.setActive(false);
        	}
        }
        mouseEvent.consume();
    }

    public void moved(PositionEvent event)
    {
        if (!this.isActive())
            return;
        
        createCircle(null, this.center);
    }

    /**
     * Returns the layer currently used to display the polyline.
     *
     * @return the layer holding the polyline.
     */
    public Polyline getPolyline()
    {
        return this.circle;
    }

    public ArrayList<Position> getPositions()
    {
        return positions;
    }

    public Position getCenter()
    {
    	return this.center;
    }
    
    public double getRadius()
    {
    	return this.radius;
    }
    
    /**
     * Removes all positions from the polyline.
     */
    public void clear()
    {
        if (this.positions.size() == 0)
            return;

        this.positions.clear();
        this.circle.setPositions(this.positions);
        this.center = null;
        this.radius = 0.0;
		this.firePropertyChange(RADIUS_CHANGED_PROPERTY, null, this.radius);
        this.wwd.redraw();
    }

    
    public void createCircle(Double radius, Position center)
    {
        Position curPos = this.wwd.getCurrentPosition();
        if ((curPos == null && radius == null) || center == null)
            return;
        
        this.positions.clear();

        /*
          The following code was adapted from
          
           This is the original JavaScript code:
           
        var R = 6371; // earth's mean radius in km
        var lat = (latlong.Latitude * Math.PI) / 180; //rad
        var lon = (latlong.Longitude * Math.PI) / 180; //rad
        var d = parseFloat(radius)/R;  // d = angular distance covered on earth's surface
        var points= new Array();
        for (x = 0; x <= 360; x++)
        {
            var p2 = new VELatLong(0,0)           
            brng = x * Math.PI / 180; //rad
            p2.Latitude = Math.asin(Math.sin(lat)*Math.cos(d) + Math.cos(lat)*Math.sin(d)*Math.cos(brng));
            p2.Longitude = ((lon + Math.atan2(Math.sin(brng)*Math.sin(d)*Math.cos(lat), Math.cos(d)-Math.sin(lat)*Math.sin(p2.Latitude))) * 180) / Math.PI;
            p2.Latitude = (p2.Latitude * 180) / Math.PI;
            points.push(p2);
        }
        */

        double lat = center.getLatitude().getRadians();
        double lon = center.getLongitude().getRadians();
        double d = 0.0;
        if (radius == null)
        	d = LatLon.greatCircleDistance(curPos.getLatLon(), center.getLatLon()).getRadians();
        else
        	d = radius;
        
        for (int x = 0; x < 360; x++)
        {
            double brng = x * Math.PI / 180.0; // radians
            double pLatitude = Math.asin(Math.sin(lat)*Math.cos(d) + Math.cos(lat)*Math.sin(d)*Math.cos(brng));
            double pLongitude = ((lon + Math.atan2(Math.sin(brng)*Math.sin(d)*Math.cos(lat), Math.cos(d)-Math.sin(lat)*Math.sin(pLatitude))) );// * 180.0) / Math.PI;
            //pLatitude = (pLatitude * 180.0) / Math.PI;

            Position p = new Position(Angle.fromRadians(pLatitude), Angle.fromRadians(pLongitude), 0.0);
            this.positions.add(p);
        }
        	
        this.circle.setPositions(this.positions);

        double prevRadius = this.radius;
        this.radius = d;

        this.firePropertyChange(RADIUS_CHANGED_PROPERTY, prevRadius, this.radius);
        
        this.wwd.redraw();
    }

}
