package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.io.IOException;

import javax.swing.JPanel;

/**
 * @author steelrj1
 *
 */
public interface IStateHistoryPanel
{
    /**
     * @param tf
     */
    public void setTimeSlider(double tf);

    /**
     * @return
     */
    public JPanel getView();

    /**
     * @throws IOException
     */
    public void initializeRunList() throws IOException;
}
