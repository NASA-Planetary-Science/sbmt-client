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
//        tableModelListener = new DtmResultsTableModeListener();
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
//                int size = imageRawResults.size();
                for (int i=0; i<table.getNumberOfRows(); ++i)
                {
//                    String name = imageRawResults.get(i).get(0);
//                    ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
                	DEMKey key = table.getKey(i);
//                	DEMInfo info = new DEMInfo();
//                	info.demfilename = key.fileName;
//                	info.name = key.displayName;

                	if (dems.containsDEM(key))
                    {
                        resultList.setValueAt(true, i, 0); //was imageResultsTableView.getMapColumnIndex()
//                        PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
                        //TODO FIX THIS
                        resultList.setValueAt(true, i, 1);	//was imageResultsTableView.getShowFootprintColumnIndex()
                    }
                    else
                    {
                        resultList.setValueAt(false, i, 0); //was imageResultsTableView.getMapColumnIndex()
                        resultList.setValueAt(false, i, 1); //was imageResultsTableView.getShowFootprintColumnIndex()
                    }
                    if (boundaries.containsBoundary(key))
                        resultList.setValueAt(true, i, 2);	//was imageResultsTableView.getBndrColumnIndex()
                    else
                        resultList.setValueAt(false, i, 2);
                }
                getJTable().getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
            }
        }
    }

//    class DtmResultsTableModeListener implements TableModelListener
//    {
//        public void tableChanged(TableModelEvent e)
//        {
//            ImageSource sourceOfLastQuery = imageSearchModel.getImageSourceOfLastQuery();
//            List<List<String>> imageRawResults = imageSearchModel.getImageResults();
//            ModelManager modelManager = imageSearchModel.getModelManager();
//            if (e.getColumn() == imageResultsTableView.getMapColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                String namePrefix = name.substring(0, name.length()-4);
//                if ((Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getMapColumnIndex()))
//                    imageSearchModel.loadImages(namePrefix);
//                else
//                {
//                    imageSearchModel.unloadImages(namePrefix);
//                    renderer.setLighting(LightingType.LIGHT_KIT);
//                }
//            }
//            else if (e.getColumn() == imageResultsTableView.getShowFootprintColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                String namePrefix = name.substring(0, name.length()-4);
//                boolean visible = (Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getShowFootprintColumnIndex());
//                imageSearchModel.setImageVisibility(namePrefix, visible);
//            }
//            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, imageSearchModel.getInstrument());
//                ImageCollection images = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName());
//                if (images.containsImage(key))
//                {
//                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
//                    image.setShowFrustum(!image.isFrustumShowing());
//                }
//            }
//            else if (e.getColumn() == imageResultsTableView.getBndrColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, imageSearchModel.getInstrument());
//                try
//                {
//                    if (!boundaries.containsBoundary(key))
//                        boundaries.addBoundary(key);
//                    else
//                        boundaries.removeBoundary(key);
//                }
//                catch (Exception e1) {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
//                            "There was an error mapping the boundary.",
//                            "Error",
//                            JOptionPane.ERROR_MESSAGE);
//
//                    e1.printStackTrace();
//                }
//            }
//
//        }
//    }

}
