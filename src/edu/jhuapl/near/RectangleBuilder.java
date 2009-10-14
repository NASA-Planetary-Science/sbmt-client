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
public class RectangleBuilder extends ShapeBuilder
{
    private final WorldWindow wwd;
    private ArrayList<Position> positions = new ArrayList<Position>();
    private Polyline rectangle;

    /**
     * Construct a new rectangle builder using the specified polyline and layer and drawing events from the specified world
     * window. Either or both the polyline and the layer may be null, in which case the necessary object is created.
     *
     * @param wwd       the world window to draw events from.
     */
    public RectangleBuilder(final WorldWindow wwd)
    {
    	super(wwd);
    	
        this.wwd = wwd;

        this.rectangle = new Polyline();
        this.rectangle.setFollowTerrain(true);
        this.rectangle.setClosed(true);
        this.getLayer().addRenderable(this.rectangle);
    }

    public void mouseClicked(MouseEvent mouseEvent)
    {
        if (this.isArmed() && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            this.setActive(!this.isActive());
            
            // Create a rectangle with all corners at the same point
            if (this.isActive())
            {
                clear();
                addPosition();
                addPosition();
                addPosition();
                addPosition();
            }
        }
        mouseEvent.consume();
    }

    public void moved(PositionEvent event)
    {
        if (!this.isActive())
            return;
        setOppositeCornerPosition();
    }

    /**
     * Returns the layer currently used to display the polyline.
     *
     * @return the layer holding the polyline.
     */
    public Polyline getPolyline()
    {
        return this.rectangle;
    }

    public ArrayList<Position> getPositions()
    {
        return positions;
    }

    /**
     * Removes all positions from the polyline.
     */
    public void clear()
    {
        while (this.positions.size() > 0)
            this.removePosition();
    }

    private void addPosition()
    {
        Position curPos = this.wwd.getCurrentPosition();
        if (curPos == null)
            return;

        this.positions.add(curPos);
        this.rectangle.setPositions(this.positions);
//        this.firePropertyChange("RectangleBuilder.AddPosition", null, curPos);
        this.wwd.redraw();
    }

    private void setOppositeCornerPosition()
    {
        if (this.positions.size() != 4)
            return;

        Position curPos = this.wwd.getCurrentPosition();
        if (curPos == null)
            return;
        
        Position p1 = this.positions.get(1);
        Position p2 = this.positions.get(2);
        Position p3 = this.positions.get(3);
        p1 = new Position(p1.getLatitude(), curPos.getLongitude(), p1.getElevation());
        p2 = new Position(curPos.getLatitude(), curPos.getLongitude(), curPos.getElevation());
        p3 = new Position(curPos.getLatitude(), p3.getLongitude(), p3.getElevation());
        this.positions.set(1, p1);
        this.positions.set(2, p2);
        this.positions.set(3, p3);
        this.rectangle.setPositions(this.positions);
//        this.firePropertyChange("RectangleBuilder.setOppositeCornerPosition", 1, p1);
//        this.firePropertyChange("RectangleBuilder.setOppositeCornerPosition", 2, p2);
//        this.firePropertyChange("RectangleBuilder.setOppositeCornerPosition", 3, p3);
        this.wwd.redraw();
    }

    private void removePosition()
    {
        if (this.positions.size() == 0)
            return;

//        Position currentLastPosition = this.positions.get(this.positions.size() - 1);
        this.positions.remove(this.positions.size() - 1);
        this.rectangle.setPositions(this.positions);
//        this.firePropertyChange("RectangleBuilder.RemovePosition", currentLastPosition, null);
        this.wwd.redraw();
    }
    
    public void createRectangle(ArrayList<Position> positions)
    {
    	this.positions = positions;
        this.rectangle.setPositions(this.positions);
        this.wwd.redraw();
    }
}
