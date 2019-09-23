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
    JScrollPane scrollPane;
    JPanel containerPanel;

    public SpectrumSearchPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane();
        add(scrollPane);
        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        scrollPane.setViewportView(containerPanel);
    }

    public void addSubPanel(JPanel panel)
    {
        containerPanel.add(panel);
    }
}