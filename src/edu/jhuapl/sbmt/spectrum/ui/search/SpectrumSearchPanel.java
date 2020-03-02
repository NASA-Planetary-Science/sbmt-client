package edu.jhuapl.sbmt.spectrum.ui.search;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Top level container panel for spectrum search elements
 * @author steelrj1
 *
 */
public class SpectrumSearchPanel extends JPanel
{
    JPanel containerPanel;

    public SpectrumSearchPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
    }

    public void addSubPanel(JPanel panel)
    {
    	add(panel);
    }
}