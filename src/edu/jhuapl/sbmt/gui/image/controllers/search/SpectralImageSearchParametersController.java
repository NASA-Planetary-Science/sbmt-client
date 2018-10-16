package edu.jhuapl.sbmt.gui.image.controllers.search;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.search.SpectralImageSearchParametersPanel;

public class SpectralImageSearchParametersController
        extends ImageSearchParametersController
{
    SpectralImageSearchParametersPanel specPanel = new SpectralImageSearchParametersPanel();

    public SpectralImageSearchParametersController(ImageSearchModel model,
            PickManager pickManager)
    {
        super(model, pickManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setupSearchParametersPanel()
    {
        // TODO Auto-generated method stub
        setPanel(specPanel);
        super.setupSearchParametersPanel();

        String[] filterNames = smallBodyConfig.imageSearchFilterNames;
        JTable filterTable = specPanel.getFilterTable();
        ((DefaultTableModel)filterTable.getModel()).setRowCount(filterNames.length);
        int i = 0;
        for (String name : filterNames)
        {
            filtersSelected.add(i);
            filterTable.setValueAt(true, i, 0);
            filterTable.setValueAt(name, i++, 1);
        }

        String[] userSearchNames = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
        JTable userTable = specPanel.getUserParamTable();
        ((DefaultTableModel)userTable.getModel()).setRowCount(userSearchNames.length);
        i = 0;
        for (String name : userSearchNames)
        {
            camerasSelected.add(i);
            userTable.setValueAt(true, i, 0);
            userTable.setValueAt(name, i++, 1);
        }

        specPanel.getFilterTable().getModel().addTableModelListener(new TableModelListener()
        {

            @Override
            public void tableChanged(TableModelEvent e)
            {
                filtersSelected.clear();
                for (int i=0; i < specPanel.getFilterTable().getModel().getRowCount(); i++)
                {
                    if ((Boolean)specPanel.getFilterTable().getValueAt(i, 0))
                    {
                        filtersSelected.add(i);
                    }
                }
            }
        });


        specPanel.getUserParamTable().getModel().addTableModelListener(new TableModelListener()
        {

            @Override
            public void tableChanged(TableModelEvent e)
            {
                camerasSelected.clear();
                for (int i=0; i < specPanel.getUserParamTable().getModel().getRowCount(); i++)
                {
                    if ((Boolean)specPanel.getUserParamTable().getValueAt(i, 0))
                    {
                        camerasSelected.add(i);
                    }
                }
            }
        });
    }

}
