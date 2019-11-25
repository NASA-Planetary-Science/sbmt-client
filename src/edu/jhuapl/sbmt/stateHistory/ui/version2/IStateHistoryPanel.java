package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.io.IOException;

import javax.swing.JPanel;

public interface IStateHistoryPanel
{
    public void setTimeSlider(double tf);

    public JPanel getView();

    public void initializeRunList() throws IOException;
}
