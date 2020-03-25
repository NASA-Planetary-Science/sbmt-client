package edu.jhuapl.sbmt.stateHistory.model.animator;

import java.io.File;

import edu.jhuapl.sbmt.stateHistory.ui.version2.IStateHistoryPanel;

/**
 * @author steelrj1
 *
 */
public class AnimationFrame
{
    /**
     *
     */
    public boolean staged;
    /**
     *
     */
    public boolean saved;
    /**
     *
     */
    public int delay;
    /**
     *
     */
    public double timeFraction;
    /**
     *
     */
    public File file;
    /**
     *
     */
    public IStateHistoryPanel panel;

}