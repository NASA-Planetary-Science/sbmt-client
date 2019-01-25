package edu.jhuapl.sbmt.gui.dtm.controllers;

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
import edu.jhuapl.sbmt.gui.dem.DEMPopupMenu;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;
import edu.jhuapl.sbmt.model.dtm.DEMTable;
import edu.jhuapl.sbmt.model.dtm.DEMTable.Columns;

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

        this.dems.addPropertyChangeListener(propertyChangeListener);
        boundaries.addPropertyChangeListener(propertyChangeListener);
        demPopupMenu = new DEMPopupMenu(modelManager.getPolyhedralModel(), dems, boundaries, renderer, getJTable());
        demPopupMenu.removeCenterMenu();
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

	private void demListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
//        	int index = tableWrapper.getComponent().getSelectedRow();
//            int index = imageList.locationToIndex(e.getPoint());
//
//            if (index >= 0 && imageList.getCellBounds(index, index).contains(e.getPoint()))
//            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
//                if (!imageList.isSelectedIndex(index))
//                {
//                    imageList.clearSelection();
//                    imageList.setSelectedIndex(index);
//                }

                int[] selectedIndices = tableWrapper.getComponent().getSelectedRows();
                List<DEMKey> demKeys = new ArrayList<DEMKey>();
                for (int selectedIndex : selectedIndices)
                {
//                    DEMInfo demInfo = (DEMInfo)((DefaultListModel)imageList.getModel()).get(selectedIndex);
                    DEMKey demKey = table.getKey(selectedIndex);
//                    String name = getCustomDataFolder() + File.separator + demInfo.demfilename;
//                    DEMKey demKey = new DEMKey(name, demInfo.name);
                    demKeys.add(demKey);
                }
                demPopupMenu.setCurrentDEMs(demKeys);
                demPopupMenu.show(e.getComponent(), e.getX(), e.getY());
//                if (demKeys.size() > 0)
//                {
//                    ((DEMInfo)imageList.getModel().getElementAt(index-1)).name = (((DEMCollection)modelManager.getModel(ModelNames.DEM)).getDEM(demKeys.get(0))).getKey().displayName;
//                    updateConfigFile();
//                }
//            }
        }
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
                	tableWrapper.setCellEditable(i, Columns.Bndr, true);
//                	tableWrapper.setColumnEditable(Columns.Bndr, true);
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
