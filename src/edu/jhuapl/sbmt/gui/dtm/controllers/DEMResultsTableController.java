package edu.jhuapl.sbmt.gui.dtm.controllers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk2.table.TableSwingWrapper;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dtm.DEMTable;

public class DEMResultsTableController
{
	private DEMTable table;
	protected final DEMCollection dems;
    protected final DEMBoundaryCollection boundaries;
    private TableSwingWrapper tableWrapper;

	public DEMResultsTableController(ModelManager modelManager, PickManager pickManager)
	{
		this.dems = (DEMCollection) modelManager.getModel(ModelNames.DEM);
        this.boundaries = (DEMBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY);
		table = new DEMTable();
        table.addListener(dems);
        table.addListener(boundaries);
        tableWrapper = DEMTable.createSwingWrapper(table);
	}


	public JPanel getPanel()
	{
		JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(tableWrapper.getComponent());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(scrollPane);
        return panel;
	}


	public DEMTable getTable()
	{
		return table;
	}

	public JTable getJTable()
	{
		return tableWrapper.getComponent();
	}

}
