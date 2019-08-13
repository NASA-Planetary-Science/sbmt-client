package edu.jhuapl.sbmt.spectrum.controllers.standard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.swing.JTable;

import edu.jhuapl.saavtk.util.Properties;
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

            int startIndex = 0;
            int endIndex = Math.min(10, size);

            if (this.spectrumResultsTableController.model.getResultIntervalCurrentlyShown() != null)
            {
            	startIndex = this.spectrumResultsTableController.model.getResultIntervalCurrentlyShown().id1;
            	endIndex = Math.min(size, this.spectrumResultsTableController.model.getResultIntervalCurrentlyShown().id2);
            }

            if (this.spectrumResultsTableController.modifiedTableRow > size) this.spectrumResultsTableController.modifiedTableRow = -1;
            if (this.spectrumResultsTableController.modifiedTableRow != -1)
            {
            	startIndex = this.spectrumResultsTableController.modifiedTableRow;
            	endIndex = startIndex + 1;
            }

            if ((resultsTable.getModel().getRowCount() == 0) || (size != this.spectrumResultsTableController.panel.getResultList().getRowCount()))  return;
            if (size > 0)
            {
                for (int i=startIndex; i<endIndex; ++i)
                {
                    int j = (Integer)this.spectrumResultsTableController.panel.getResultList().getValueAt(i, this.spectrumResultsTableController.panel.getIdColumnIndex())-1;
                    String name = this.spectrumResultsTableController.model.getSpectrumRawResults().get(j).getDataName();
                    SpectrumKeyInterface key = this.spectrumResultsTableController.model.createSpectrumKey(name, this.spectrumResultsTableController.model.getInstrument());
                    IBasicSpectrumRenderer spectrum = this.spectrumResultsTableController.spectrumCollection.getSpectrumFromKey(key);
                    if (this.spectrumResultsTableController.spectrumCollection.containsKey(key))
                    {
                        resultsTable.setValueAt(true, i, this.spectrumResultsTableController.panel.getMapColumnIndex());
                        resultsTable.setValueAt(spectrum.isVisible(), i, this.spectrumResultsTableController.panel.getShowFootprintColumnIndex());
                        resultsTable.setValueAt(spectrum.isFrustumShowing(), i, this.spectrumResultsTableController.panel.getFrusColumnIndex());
                        resultsTable.setValueAt(sdf.format(spectrum.getSpectrum().getDateTime().toDate().getTime()), i, this.spectrumResultsTableController.panel.getDateColumnIndex());
                    }
                    else
                    {
                        resultsTable.setValueAt(false, i, this.spectrumResultsTableController.panel.getMapColumnIndex());
                        resultsTable.setValueAt(false, i, this.spectrumResultsTableController.panel.getShowFootprintColumnIndex());
                        resultsTable.setValueAt(false, i, this.spectrumResultsTableController.panel.getFrusColumnIndex());
                    }

                    if (this.spectrumResultsTableController.boundaries.containsBoundary(key))
                        resultsTable.setValueAt(true, i, this.spectrumResultsTableController.panel.getBndrColumnIndex());
                    else
                        resultsTable.setValueAt(false, i, this.spectrumResultsTableController.panel.getBndrColumnIndex());

                }
            }
            this.spectrumResultsTableController.panel.getResultList().getModel().addTableModelListener(this.spectrumResultsTableController.tableModelListener);
            // Repaint the list in case the boundary colors has changed
            resultsTable.repaint();
        }
    }
}