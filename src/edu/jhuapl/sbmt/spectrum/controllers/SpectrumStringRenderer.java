package edu.jhuapl.sbmt.spectrum.controllers;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumKeyInterface;

public class SpectrumStringRenderer extends DefaultTableCellRenderer
{
    public SpectrumBoundaryCollection model;
    private BaseSpectrumSearchModel spectrumSearchModel;
    private List<BasicSpectrum> spectrumRawResults;

    public SpectrumStringRenderer(BaseSpectrumSearchModel spectrumSearchModel, List<BasicSpectrum> spectrumRawResults, SpectrumBoundaryCollection boundaries)
    {
        this.spectrumSearchModel = spectrumSearchModel;
        this.spectrumRawResults = spectrumRawResults;
        this.model = boundaries;
//        model = (SpectrumBoundaryCollection)spectrumSearchModel.getModelManager().getModel(spectrumSearchModel.getSpectrumBoundaryCollectionModelName());
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column)
    {
    	int actualRow = table.getRowSorter().convertRowIndexToModel(row);
        Component co = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, actualRow, column);
        if (spectrumRawResults.size() == 0) return co;
        String name = spectrumRawResults.get(actualRow).getSpectrumName();
        SpectrumKeyInterface key = spectrumSearchModel.createSpectrumKey(name, spectrumSearchModel.getInstrument());
        if (model.containsBoundary(key))
        {
            int[] c = model.getBoundary(key).getBoundaryColor();
            if (isSelected)
            {
                co.setForeground(new Color(c[0], c[1], c[2]));
                co.setBackground(table.getSelectionBackground());
            }
            else
            {
                co.setForeground(new Color(c[0], c[1], c[2]));
                co.setBackground(table.getBackground());
            }
        }
        else
        {
            if (isSelected)
            {
                co.setForeground(table.getSelectionForeground());
                co.setBackground(table.getSelectionBackground());
            }
            else
            {
                co.setForeground(table.getForeground());
                co.setBackground(table.getBackground());
            }
        }
        return co;
    }

    public void setSpectrumRawResults(List<BasicSpectrum> spectrumRawResults)
    {
        this.spectrumRawResults = spectrumRawResults;
    }
}