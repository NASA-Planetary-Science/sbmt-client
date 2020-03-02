package edu.jhuapl.sbmt.dtm.ui.creation;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DtmCreationPanel extends JPanel
{
	JScrollPane scrollPane;
    JPanel containerPanel;

    public DtmCreationPanel()
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
