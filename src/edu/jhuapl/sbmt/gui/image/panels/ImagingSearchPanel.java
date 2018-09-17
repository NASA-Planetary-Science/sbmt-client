package edu.jhuapl.sbmt.gui.image.panels;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ImagingSearchPanel extends JPanel
{
    JScrollPane scrollPane;
    JPanel containerPanel;

    public ImagingSearchPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane();
        add(scrollPane);
        containerPanel = new JPanel();
//        containerPanel.setPreferredSize(new Dimension(300,300));
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(Color.green);
        scrollPane.setViewportView(containerPanel);

//
//        containerPanel = new JPanel();
//        scrollPane.add(containerPanel);
    }

    public void addSubPanel(JPanel panel)
    {
        System.out.println("ImagingSearchPanel: addSubPanel: adding " + panel);
        containerPanel.add(panel);
    }

}
