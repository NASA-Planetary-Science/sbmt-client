package edu.jhuapl.near.pick;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.Timer;

import vtk.vtkCellPicker;
import vtk.vtkRenderWindowPanel;

/**
 * A picker is a class that listens on mouse events on the renderer and
 * responds appropriately. There can be more than 1 picker active at any
 * given time. The PickManager class (also a subclass of this) is responsible
 * for initializing and managing all the pickers.
 *
 * @author eli
 *
 */
public abstract class Picker implements
    MouseListener,
    MouseMotionListener,
    MouseWheelListener,
    KeyListener,
    PropertyChangeListener
{
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport( this );
    public void addPropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.addPropertyChangeListener( listener ); }
    public void removePropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.removePropertyChangeListener( listener ); }

    // not sure if volatile is really needed, but just to be sure
    private static volatile boolean pickingEnabled = true;

    public static final double DEFAULT_PICK_TOLERANCE = 0.002;

    private double pickTolerance = DEFAULT_PICK_TOLERANCE;


    public double getPickTolerance()
    {
        return pickTolerance;
    }

    public void setPickTolerance(double pickTolerance)
    {
        this.pickTolerance = pickTolerance;
    }

    /**
     * Unfortunately, crashes sometimes occur if the user drags around the mouse during
     * a long running operation (e.g. changing to a high resolution). To prevent this,
     * the following global function is provided to allow disabling of picking during such
     * operations. Note that if the picking is requested to be enabled, a delay of half
     * a second is made before enabling picking.
     *
     * TODO This is just a hack, investigate the cause of the crash more fully.
     *
     * @param b
     */
    public static void setPickingEnabled(boolean b)
    {
        if (b == false)
        {
            pickingEnabled = false;
        }
        else
        {
            // Delay half a second before enabling picking. This helps prevent some crashes.

            int delay = 500; //milliseconds
            ActionListener taskPerformer = new ActionListener() {
                public void actionPerformed(ActionEvent evt)
                {
                    pickingEnabled = true;
                }
            };

            Timer timer = new Timer(delay, taskPerformer);
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseDragged(MouseEvent e)
    {
    }

    public void mouseMoved(MouseEvent e)
    {
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
    }

    public void keyReleased(KeyEvent e)
    {
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
    }

    /**
     * Get the default cursor to be used when this picker is active.
     * Note this may be different than the value of Cursor.DEFAULT_CURSOR
     * or Cursor.getDefaultCursor().
     *
     * @return
     */
    public int getDefaultCursor()
    {
        return Cursor.DEFAULT_CURSOR;
    }

/*
// Old version:
    protected int doPick(MouseEvent e, vtkCellPicker picker, vtkRenderWindowPanel renWin)
    {
        if (pickingEnabled == false)
            return 0;

        // Don't do a pick if the event is more than a third of a second old
        final long currentTime = System.currentTimeMillis();
        final long when = e.getWhen();

        //System.err.println("elapsed time " + (currentTime - when));
        if (currentTime - when > 333)
            return 0;

        // When picking, choosing the right tolerance is not simple. If it's too small, then
        // the pick will only work well if we are zoomed in very close to the object. If it's
        // too large, the pick will only work well when we are zoomed out a lot. To deal
        // with this situation, do a series of picks starting out with a low tolerance
        // and increase the tolerance after each new pick. Stop as soon as the pick succeeds
        // or we reach the maximum tolerance.

        int pickSucceeded = 0;
        double tolerance = 0.0002;
        final double originalTolerance = picker.GetTolerance();
        final double maxTolerance = 0.004;
        final double incr = 0.0002;
        renWin.lock();
        picker.SetTolerance(tolerance);
        while (tolerance <= maxTolerance)
        {
            picker.SetTolerance(tolerance);

            pickSucceeded = picker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());

            if (pickSucceeded == 1)
                break;

            tolerance += incr;
        }
        picker.SetTolerance(originalTolerance);
        renWin.unlock();

        return pickSucceeded;
    }
*/

    protected int doPick(MouseEvent e, vtkCellPicker picker, vtkRenderWindowPanel renWin)
    {
        return doPick(e.getWhen(), e.getX(), e.getY(), picker, renWin);
    }

    protected int doPick(final long when, int x, int y, vtkCellPicker picker, vtkRenderWindowPanel renWin)
    {
        if (pickingEnabled == false)
            return 0;

        // Don't do a pick if the event is more than a third of a second old
        final long currentTime = System.currentTimeMillis();

        //System.err.println("elapsed time " + (currentTime - when));
        if (currentTime - when > 333)
            return 0;

        renWin.lock();

        picker.SetTolerance(pickTolerance);

        int pickSucceeded = picker.Pick(x, renWin.getHeight()-y-1, 0.0, renWin.GetRenderer());

        renWin.unlock();

        return pickSucceeded;
    }
}
