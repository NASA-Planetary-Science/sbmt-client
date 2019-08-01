package edu.jhuapl.sbmt.spectrum.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.search.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.rendering.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.model.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;

public class CustomSpectrumResultsTableController
        extends SpectrumResultsTableController
{
    private List<CustomSpectrumKeyInterface> results;
    private CustomSpectraSearchModel model;
    private String customDataFolder;

    public CustomSpectrumResultsTableController(ISpectralInstrument instrument,
            SpectraCollection spectrumCollection, ModelManager modelManager, SpectrumBoundaryCollection boundaries, CustomSpectraSearchModel model,
            Renderer renderer, SbmtInfoWindowManager infoPanelManager)
    {
        super(instrument, spectrumCollection, modelManager, boundaries, model, renderer,
                infoPanelManager);
        this.model = model;
        this.results = model.getCustomSpectra();
        this.customDataFolder = modelManager.getPolyhedralModel().getCustomDataFolder();
    }

    @Override
    public void setSpectrumResultsPanel()
    {
        // TODO Auto-generated method stub
        super.setSpectrumResultsPanel();

        super.setSpectrumResultsPanel();
        panel.getResultList().getModel().removeTableModelListener(tableModelListener);
        tableModelListener = new CustomSpectrumResultsTableModeListener();
        panel.getResultList().getModel().addTableModelListener(tableModelListener);

        this.spectrumCollection.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = new SpectrumResultsPropertyChangeListener();
        this.spectrumCollection.addPropertyChangeListener(propertyChangeListener);

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

    class CustomSpectrumResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            List<BasicSpectrum> spectrumRawResults = model.getSpectrumRawResults();
            results = model.getCustomSpectra();
//            ModelManager modelManager = model.getModelManager();
//            SpectraCollection spectra = (SpectraCollection)modelManager.getModel(model.getSpectrumCollectionModelName());
            if (panel.getResultList().getModel().getRowCount() == 0) return;
            int actualRow = panel.getResultList().getRowSorter().convertRowIndexToView(e.getFirstRow());
            int row = (Integer)panel.getResultList().getValueAt(actualRow, panel.getIdColumnIndex())-1;
            String name = spectrumRawResults.get(row).getDataName();
            CustomSpectrumKeyInterface key = getConvertedKey(results.get(actualRow));

            if (e.getColumn() == panel.getMapColumnIndex())
            {
                if ((Boolean)panel.getResultList().getValueAt(actualRow, panel.getMapColumnIndex()))
                {
//                	model.loadSpectra(name, key);
                	try
                    {
                        if (!spectrumCollection.containsKey(key))
                        {
                        	spectrumCollection.addSpectrum(key, true);
//                            loadSpectrum(key, spectrumCollection);
                        }
                    }
                    catch (Exception e1) {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                                "There was an error mapping the spectra.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);

                        e1.printStackTrace();
                    }
                	model.updateColoring();
                }
                else
                {
                    spectrumCollection.removeSpectrum(key);
                    renderer.setLighting(LightingType.LIGHT_KIT);
                }
            }
            else if (e.getColumn() == panel.getShowFootprintColumnIndex())
            {
                boolean visible = (Boolean)panel.getResultList().getValueAt(row, panel.getShowFootprintColumnIndex());
//                model.setSpectrumVisibility(key, visible);
                model.fireBoundaryVisibilityCountChanged(spectrumCollection.getSpectrumFromKey(key).getSpectrum(), visible);

            }
            else if (e.getColumn() == panel.getFrusColumnIndex())
            {
            	 if (spectrumCollection.containsKey(key))
                 {
                     IBasicSpectrumRenderer spectrum = spectrumCollection.getSpectrumFromKey(key);
                     spectrumCollection.setFrustumVisibility(spectrum, !spectrum.isFrustumShowing());
                 }
            }
            else if (e.getColumn() == panel.getBndrColumnIndex())
            {
                try
                {
                    if (!boundaries.containsBoundary(key))
                        boundaries.addBoundary(key, spectrumCollection);
                    else
                        boundaries.removeBoundary(key);
                }
                catch (Exception e1) {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                            "There was an error mapping the boundary.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e1.printStackTrace();
                }
            }
        }
    }

    private CustomSpectrumKeyInterface getConvertedKey(CustomSpectrumKeyInterface key)
    {
        CustomSpectrumKeyInterface info;
		String expandedNameString = SafeURLPaths.instance().getString(getCustomDataFolder() + File.separator + key.getSpectrumFilename());
        info = new CustomSpectrumKey(expandedNameString, key.getFileType(), instrument, key.getSpectrumType(), SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getSpectrumFilename()), key.getPointingFilename());
        return info;
    }

    public String getCustomDataFolder()
    {
        return customDataFolder;
    }

}
