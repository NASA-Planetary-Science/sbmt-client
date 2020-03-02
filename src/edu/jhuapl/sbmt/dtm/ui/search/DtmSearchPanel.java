package edu.jhuapl.sbmt.dtm.ui.search;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class DtmSearchPanel extends JPanel
{
	JScrollPane scrollPane;
    JPanel containerPanel;

    public DtmSearchPanel()
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
