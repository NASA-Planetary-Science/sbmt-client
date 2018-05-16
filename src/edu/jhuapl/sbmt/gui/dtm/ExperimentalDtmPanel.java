package edu.jhuapl.sbmt.gui.dtm;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class ExperimentalDtmPanel extends JPanel
{
    DtmCreationPanel createPanel=new DtmCreationPanel();
    DtmSearchPanel searchPanel=new DtmSearchPanel();
    DtmBrowsePanel browsePanel=new DtmBrowsePanel();


    public ExperimentalDtmPanel() {
        setLayout(new BorderLayout(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        add(tabbedPane);
        tabbedPane.addTab("Create", createPanel);
        tabbedPane.addTab("Search", searchPanel);
        tabbedPane.addTab("Browse", browsePanel);
    }

}
