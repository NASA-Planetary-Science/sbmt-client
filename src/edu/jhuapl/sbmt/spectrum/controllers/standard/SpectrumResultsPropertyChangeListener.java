package edu.jhuapl.sbmt.spectrum.controllers.standard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.swing.JTable;

import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumBoundary;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class SpectrumResultsPropertyChangeListener implements PropertyChangeListener
{
    /**
	 *
	 */
	private final SpectrumResultsTableController spectrumResultsTableController;

	/**
	 * @param spectrumResultsTableController
	 */
	public SpectrumResultsPropertyChangeListener(SpectrumResultsTableController spectrumResultsTableController)
	{
		this.spectrumResultsTableController = spectrumResultsTableController;
	}

	@Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            JTable resultsTable = this.spectrumResultsTableController.panel.getResultList();
            this.spectrumResultsTableController.panel.getResultList().getModel().removeTableModelListener(this.spectrumResultsTableController.tableModelListener);
            int size = this.spectrumResultsTableController.model.getSpectrumRawResults().size();

            if (evt.getNewValue() instanceof SpectrumBoundary)
            {
            	SpectrumBoundary boundary = (SpectrumBoundary)evt.getNewValue();
            	SpectrumKeyInterface key = boundary.getKey();
        		int i = this.spectrumResultsTableController.getSpectrumKeys().indexOf(key);
            	if (this.spectrumResultsTableController.boundaries.containsBoundary(key))
                    resultsTable.setValueAt(true, i, this.spectrumResultsTableController.panel.getBndrColumnIndex());
                else
                    resultsTable.setValueAt(false, i, this.spectrumResultsTableController.panel.getBndrColumnIndex());
            }
            else
            {
        		IBasicSpectrumRenderer spectrumRenderer = (IBasicSpectrumRenderer)evt.getNewValue();
        		SpectrumKeyInterface key = this.spectrumResultsTableController.model.createSpectrumKey(spectrumRenderer.getSpectrum().getDataName(), this.spectrumResultsTableController.model.getInstrument());
        		int i = this.spectrumResultsTableController.getSpectrumKeys().indexOf(key);
                if (this.spectrumResultsTableController.spectrumCollection.containsKey(key))
                {
                    resultsTable.setValueAt(true, i, this.spectrumResultsTableController.panel.getMapColumnIndex());
                    resultsTable.setValueAt(spectrumRenderer.isVisible(), i, this.spectrumResultsTableController.panel.getShowFootprintColumnIndex());
                    resultsTable.setValueAt(spectrumRenderer.isFrustumShowing(), i, this.spectrumResultsTableController.panel.getFrusColumnIndex());
                    resultsTable.setValueAt(sdf.format(spectrumRenderer.getSpectrum().getDateTime().toDate().getTime()), i, this.spectrumResultsTableController.panel.getDateColumnIndex());
                }
                else
                {
                    resultsTable.setValueAt(false, i, this.spectrumResultsTableController.panel.getMapColumnIndex());
                    resultsTable.setValueAt(false, i, this.spectrumResultsTableController.panel.getShowFootprintColumnIndex());
                    resultsTable.setValueAt(false, i, this.spectrumResultsTableController.panel.getFrusColumnIndex());
                }


            }
            this.spectrumResultsTableController.panel.getResultList().getModel().addTableModelListener(this.spectrumResultsTableController.tableModelListener);
            // Repaint the list in case the boundary colors has changed
            resultsTable.repaint();
        }
    }
}