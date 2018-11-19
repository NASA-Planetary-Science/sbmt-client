package edu.jhuapl.sbmt.gui.dtm.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk2.table.TableSwingWrapper;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;
import edu.jhuapl.sbmt.model.dtm.DEMTable;

public class DEMResultsTableController
{
	private DEMTable table;
	protected final DEMCollection dems;
    protected final DEMBoundaryCollection boundaries;
    private TableSwingWrapper tableWrapper;
    protected PropertyChangeListener propertyChangeListener;
    protected TableModelListener tableModelListener;

	public DEMResultsTableController(ModelManager modelManager, PickManager pickManager)
	{
		this.dems = (DEMCollection) modelManager.getModel(ModelNames.DEM);
        this.boundaries = (DEMBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY);
		table = new DEMTable();
        table.addListener(dems);
        table.addListener(boundaries);
        tableWrapper = DEMTable.createSwingWrapper(table);

        propertyChangeListener = new DtmResultsPropertyChangeListener();

        this.dems.addPropertyChangeListener(propertyChangeListener);
        boundaries.addPropertyChangeListener(propertyChangeListener);
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

	class DtmResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                JTable resultList = getJTable();
                resultList.getModel().removeTableModelListener(tableModelListener);
                for (int i=0; i<table.getNumberOfRows(); ++i)
                {
                	DEMKey key = table.getKey(i);
                	resultList.setValueAt(dems.containsDEM(key), i, 0);
                	resultList.setValueAt(boundaries.containsBoundary(key), i, 2);
                	if (dems.getDEM(key) == null) continue;
                	resultList.setValueAt(dems.getDEM(key).isVisible(), i, 1);

                }
                getJTable().getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
            }
        }
    }
}
