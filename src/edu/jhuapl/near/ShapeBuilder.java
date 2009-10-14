package edu.jhuapl.near;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;

import java.awt.event.*;
import java.util.*;

/**
 *
 */
abstract public class ShapeBuilder extends AVListImpl implements MouseListener, 
														MouseMotionListener, 
														PositionListener
{
	private final WorldWindow wwd;
    private boolean armed = false;
    private final RenderableLayer layer;
    private boolean active = false;

    /**
     * Construct a new rectangle builder using the specified polyline and layer and drawing events from the specified world
     * window. Either or both the polyline and the layer may be null, in which case the necessary object is created.
     *
     * @param wwd       the world window to draw events from.
     */
    public ShapeBuilder(final WorldWindow wwd)
    {
        this.wwd = wwd;

        this.layer = new RenderableLayer();

        this.wwd.getModel().getLayers().add(this.layer);

        this.wwd.getInputHandler().addMouseListener(this);

        this.wwd.getInputHandler().addMouseMotionListener(this);

        this.wwd.addPositionListener(this);
    }

    public void mousePressed(MouseEvent mouseEvent)
    {
    }

    public void mouseReleased(MouseEvent mouseEvent)
    {
    }

    public void mouseEntered(MouseEvent mouseEvent)
    {
    }

    public void mouseExited(MouseEvent mouseEvent)
    {
    }

    public void mouseClicked(MouseEvent mouseEvent)
    {
    }

    public void mouseMoved(MouseEvent mouseEvent)
    {
        if (this.isArmed() && (mouseEvent.getButton() == MouseEvent.BUTTON1))
        {
            // Don't update the polyline here because the wwd current cursor position will not
            // have been updated to reflect the current mouse position. Wait to update in the
            // position listener, but consume the event so the view doesn't respond to it.
            if (this.active)
                mouseEvent.consume();
        }
    }

    public void mouseDragged(MouseEvent mouseEvent)
    {
    }

    public void moved(PositionEvent event)
    {
    }

    /**
     * Returns the layer holding the polyline being created.
     *
     * @return the layer containing the polyline.
     */
    public RenderableLayer getLayer()
    {
        return this.layer;
    }

    /**
     * Returns the layer currently used to display the polyline.
     *
     * @return the layer holding the polyline.
     */
    abstract public Polyline getPolyline();

    /**
     * Removes all positions from the polyline.
     */
    abstract public void clear();

    /**
     * Identifies whether the rectangle builder is armed.
     *
     * @return true if armed, false if not armed.
     */
    public boolean isArmed()
    {
        return this.armed;
    }

    /**
     * Arms and disarms the rectangle builder. When armed, the rectangle builder monitors user input and builds the polyline in
     * response to the actions mentioned in the overview above. When disarmed, the rectangle builder ignores all user input.
     *
     * @param armed true to arm the rectangle builder, false to disarm it.
     */
    public void setArmed(boolean armed)
    {
        this.armed = armed;
    }

    /**
     * Identifies whether the rectangle builder is active.
     *
     * @return true if active, false if not active.
     */
    public boolean isActive()
    {
        return this.active;
    }

    /**
     * Activates the rectangle builder.
     *
     * @param active true to activate the rectangle builder, false to deactivate it.
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    abstract public ArrayList<Position> getPositions();

}
