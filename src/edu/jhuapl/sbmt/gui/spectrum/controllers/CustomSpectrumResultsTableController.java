package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.gui.spectrum.CustomSpectrumImporterDialog.SpectrumInfo;
import edu.jhuapl.sbmt.gui.spectrum.model.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

public class CustomSpectrumResultsTableController
        extends SpectrumResultsTableController
{
    private List<SpectrumInfo> results;
    private CustomSpectraSearchModel model;

    public CustomSpectrumResultsTableController(SpectralInstrument instrument,
            SpectraCollection spectrumCollection, CustomSpectraSearchModel model,
            Renderer renderer, SbmtInfoWindowManager infoPanelManager)
    {
        super(instrument, spectrumCollection, model, renderer,
                infoPanelManager);
        this.model = model;
        this.results = model.getcustomSpectra();
    }

    @Override
    public void setSpectrumResultsPanel()
    {
        // TODO Auto-generated method stub
        super.setSpectrumResultsPanel();

        super.setSpectrumResultsPanel();
        panel.getResultList().getModel().removeTableModelListener(tableModelListener);
        tableModelListener = new SpectrumResultsTableModeListener();
        panel.getResultList().getModel().addTableModelListener(tableModelListener);

        this.spectrumCollection.removePropertyChangeListener(propertyChangeListener);
//        boundaries.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = new SpectrumResultsPropertyChangeListener();
        this.spectrumCollection.addPropertyChangeListener(propertyChangeListener);
//        boundaries.addPropertyChangeListener(propertyChangeListener);

        tableModel = new SpectrumTableModel(new Object[0][7], columnNames);
        panel.getResultList().setModel(tableModel);
        panel.getResultList().getColumnModel().getColumn(panel.getMapColumnIndex()).setPreferredWidth(31);
        panel.getResultList().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setPreferredWidth(35);
        panel.getResultList().getColumnModel().getColumn(panel.getFrusColumnIndex()).setPreferredWidth(31);
        panel.getResultList().getColumnModel().getColumn(panel.getBndrColumnIndex()).setPreferredWidth(31);
        panel.getResultList().getColumnModel().getColumn(panel.getMapColumnIndex()).setResizable(true);
        panel.getResultList().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setResizable(true);
        panel.getResultList().getColumnModel().getColumn(panel.getFrusColumnIndex()).setResizable(true);
        panel.getResultList().getColumnModel().getColumn(panel.getBndrColumnIndex()).setResizable(true);

        panel.getRemoveAllSpectraButton().removeActionListener(panel.getRemoveAllSpectraButton().getActionListeners()[0]);
        panel.getRemoveAllSpectraButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                removeAllFootprintsForAllInstrumentsButtonActionPerformed(e);
            }
        });

//        panel.getResultList().addMouseListener(new MouseAdapter()
//        {
//            public void mousePressed(MouseEvent e)
//            {
//                resultsListMaybeShowPopup(e);
//                panel.getSaveSelectedImageListButton().setEnabled(panel.getResultList().getSelectedRowCount() > 0);
//            }
//
//            public void mouseReleased(MouseEvent e)
//            {
//                resultsListMaybeShowPopup(e);
//                panel.getSaveSelectedImageListButton().setEnabled(panel.getResultList().getSelectedRowCount() > 0);
//            }
//        });
//
//
//        panel.getResultList().getSelectionModel().addListSelectionListener(new ListSelectionListener()
//        {
//            @Override
//            public void valueChanged(ListSelectionEvent e)
//            {
//                if (!e.getValueIsAdjusting())
//                {
//                    model.setSelectedImageIndex(panel.getResultList().getSelectedRows());
//                }
//            }
//        });

        try
        {
            model.initializeSpecList();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
