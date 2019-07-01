package edu.jhuapl.sbmt.dtm.controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk2.table.TableSwingWrapper;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.dtm.ui.DEMTable;
import edu.jhuapl.sbmt.dtm.ui.DEMTable.Columns;
import edu.jhuapl.sbmt.dtm.ui.menu.DEMPopupMenu;

public class DEMResultsTableController
{
	private DEMTable table;
	protected final DEMCollection dems;
    protected final DEMBoundaryCollection boundaries;
    private TableSwingWrapper tableWrapper;
    protected PropertyChangeListener propertyChangeListener;
    protected TableModelListener tableModelListener;
    private DEMPopupMenu demPopupMenu;

	public DEMResultsTableController(ModelManager modelManager, PickManager pickManager, Renderer renderer)
	{
		this.dems = (DEMCollection) modelManager.getModel(ModelNames.DEM);
        this.boundaries = (DEMBoundaryCollection) modelManager.getModel(ModelNames.DEM_BOUNDARY);
		table = new DEMTable();
        table.addListener(dems);		//dems contains the handle method for the table
        table.addListener(boundaries);	//boundaries contains the handle method for the table
        tableWrapper = DEMTable.createSwingWrapper(table);

        propertyChangeListener = new DtmResultsPropertyChangeListener();

        demPopupMenu = new DEMPopupMenu(modelManager.getPolyhedralModel(), dems, boundaries, renderer, getJTable(), new DEMPopupMenuActionListener(dems, boundaries));
        tableWrapper.getComponent().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
            	demListMaybeShowPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
            	demListMaybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
            	demListMaybeShowPopup(e);
            }
        });
	}

	public void addKey(DEMKey key)
	{
		getTable().appendRow(key);
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

	public void addListener()
	{
		dems.addPropertyChangeListener(propertyChangeListener);
		boundaries.addPropertyChangeListener(propertyChangeListener);
	}

	public void removeListener()
	{
		dems.removePropertyChangeListener(propertyChangeListener);
		boundaries.removePropertyChangeListener(propertyChangeListener);
	}


	public DEMTable getTable()
	{
		return table;
	}

	public JTable getJTable()
	{
		return tableWrapper.getComponent();
	}

	private void demListMaybeShowPopup(MouseEvent e)
    {
        if (!e.isPopupTrigger()) return;

        int[] selectedIndices = tableWrapper.getComponent().getSelectedRows();
        List<DEMKey> demKeys = new ArrayList<DEMKey>();
        for (int selectedIndex : selectedIndices)
        {
            DEMKey demKey = table.getKey(selectedIndex);
            demKeys.add(demKey);
        }
        demPopupMenu.setCurrentDEMs(demKeys);
        demPopupMenu.show(e.getComponent(), e.getX(), e.getY());
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
                	boolean keyExists = true;
                	if (dems.getDEM(key) == null) keyExists = false;
                	resultList.setValueAt(keyExists, i, 0);
                	resultList.setValueAt(boundaries.containsBoundary(key), i, 2);
                	tableWrapper.setCellEditable(i, Columns.Bndr, keyExists);
                	resultList.setValueAt(keyExists, i, 1);

                }
                getJTable().getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
            }
        }
    }
}
