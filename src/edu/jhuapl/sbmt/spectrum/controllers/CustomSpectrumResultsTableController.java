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
import edu.jhuapl.sbmt.spectrum.model.core.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.model.core.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKeyInterface;

public class CustomSpectrumResultsTableController
        extends SpectrumResultsTableController
{
    private List<CustomSpectrumKeyInterface> results;
    private CustomSpectraSearchModel model;

    public CustomSpectrumResultsTableController(ISpectralInstrument instrument,
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
        tableModelListener = new CustomSpectrumResultsTableModeListener();
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

//        panel.addComponentListener(new ComponentListener()
//		{
//
//			@Override
//			public void componentShown(ComponentEvent e)
//			{
//				spectrumResultsTableController.addResultListener();
//			}
//
//			@Override
//			public void componentResized(ComponentEvent e)
//			{
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void componentMoved(ComponentEvent e)
//			{
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void componentHidden(ComponentEvent e)
//			{
//				spectrumResultsTableController.removeResultListener();
//			}
//		});

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

    class CustomSpectrumResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            List<List<String>> spectrumRawResults = model.getSpectrumRawResults();
            results = model.getcustomSpectra();
            ModelManager modelManager = model.getModelManager();
            SpectraCollection spectra = (SpectraCollection)modelManager.getModel(model.getSpectrumCollectionModelName());
            if (panel.getResultList().getModel().getRowCount() == 0) return;
            int actualRow = panel.getResultList().getRowSorter().convertRowIndexToView(e.getFirstRow());
            int row = (Integer)panel.getResultList().getValueAt(actualRow, panel.getIdColumnIndex())-1;
            String name = spectrumRawResults.get(row).get(0);
//            SpectrumKeyInterface key = model.createSpectrumKey(name, model.getInstrument());
//            Spectrum spectrum = (Spectrum) spectra.getSpectrumFromKey(key);
            CustomSpectrumKeyInterface key = getConvertedKey(results.get(actualRow));
//            if (spectrum == null)
//            {
//                if (e.getColumn() == panel.getMapColumnIndex() && (Boolean)panel.getResultList().getValueAt(actualRow, panel.getMapColumnIndex()))
//                {
//                    try
//                    {
////                        spectra.addSpectrum(key.getName(), key.getInstrument(), SpectrumColoringStyle.getStyleForName(model.getSpectrumColoringStyleName()));
//
//                        model.loadSpectra(name, key);
//                        model.updateColoring();
//                    }
//                    catch (IOException e1)
//                    {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    }
//                }
//                return;
//            }
            if (e.getColumn() == panel.getMapColumnIndex())
            {
                if ((Boolean)panel.getResultList().getValueAt(actualRow, panel.getMapColumnIndex()))
                {
//                    try
//                    {
                    	model.loadSpectra(name, key);
//                        spectra.addSpectrum(key);
//                    }
//                    catch (IOException e1)
//                    {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    }
                    	model.updateColoring();
                }
                else
                {
                    spectra.removeSpectrum(key);
                    renderer.setLighting(LightingType.LIGHT_KIT);
                }
            }
            else if (e.getColumn() == panel.getShowFootprintColumnIndex())
            {
                boolean visible = (Boolean)panel.getResultList().getValueAt(row, panel.getShowFootprintColumnIndex());
                 model.setSpectrumVisibility(key, visible);
//                boolean visible = (Boolean)panel.getResultList().getValueAt(actualRow, panel.getShowFootprintColumnIndex());
//                spectra.setVisibility(spectrum, visible);
//                if (visible == false)
//                {
//                    spectra.deselect(spectrum);
//                }
//                else
//                {
//                    spectra.select(spectrum);
//                }
            }
            else if (e.getColumn() == panel.getFrusColumnIndex())
            {
            	 if (spectra.containsKey(key))
                 {
                     Spectrum spectrum = spectra.getSpectrumFromKey(key);
                     spectra.setFrustumVisibility(spectrum, !spectrum.isFrustumShowing());
                 }
//                spectra.setFrustumVisibility(spectrum, !spectrum.isFrustumShowing());
            }
            else if (e.getColumn() == panel.getBndrColumnIndex())
            {
                try
                {
//                    if (spectrum.isSelected())
//                        spectra.deselect(spectrum);
//                    else
//                        spectra.select(spectrum);

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
//		info = new CustomSpectrumKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getSpectrumFilename()), key.getSpectrumFilename(), key.getSpectrumType(), key.getName());
		String expandedNameString = SafeURLPaths.instance().getString(getCustomDataFolder() + File.separator + key.getSpectrumFilename());
        info = new CustomSpectrumKey(expandedNameString, key.getFileType(), instrument, key.getSpectrumType(), SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getSpectrumFilename()), key.getPointingFilename());
        return info;
    }

    public String getCustomDataFolder()
    {
        return model.getModelManager().getPolyhedralModel().getCustomDataFolder();
    }

}
