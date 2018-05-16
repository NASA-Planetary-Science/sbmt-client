package edu.jhuapl.sbmt.gui.dtm;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.model.ModelManager;

@SuppressWarnings("serial")
public class ExperimentalDEMPanel extends JPanel
{
    DEMCreationPanel createPanel;
    DtmSearchPanel searchPanel=new DtmSearchPanel();
    DtmBrowsePanel browsePanel=new DtmBrowsePanel();

    public ExperimentalDEMPanel(ModelManager modelManager, DEMCreator creationTool) {
        setLayout(new BorderLayout(0, 0));

        createPanel=new DEMCreationPanel(modelManager, creationTool);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        add(tabbedPane);
        tabbedPane.addTab("Create", createPanel);
        tabbedPane.addTab("Search", searchPanel);
        tabbedPane.addTab("Browse", browsePanel);
    }

}
