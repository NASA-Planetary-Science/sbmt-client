package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchModel;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.model.spectrum.SpectrumKeyInterface;

public class SpectrumStringRenderer extends DefaultTableCellRenderer
{
    public SpectrumBoundaryCollection model;
    private SpectrumSearchModel spectrumSearchModel;
    private List<List<String>> spectrumRawResults;
    private SpectraCollection collection;

    public SpectrumStringRenderer(SpectrumSearchModel spectrumSearchModel, List<List<String>> spectrumRawResults, SpectraCollection collection)
    {
        this.spectrumSearchModel = spectrumSearchModel;
        this.spectrumRawResults = spectrumRawResults;
        this.collection = collection;
        model = (SpectrumBoundaryCollection)spectrumSearchModel.getModelManager().getModel(spectrumSearchModel.getSpectrumBoundaryCollectionModelName());
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column)
    {
    	int actualRow = table.getRowSorter().convertRowIndexToView(row);
        Component co = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, actualRow, column);
        if (spectrumRawResults.size() == 0) return co;
        String name = spectrumRawResults.get(actualRow).get(0);
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

    public void setSpectrumRawResults(List<List<String>> spectrumRawResults)
    {
        this.spectrumRawResults = spectrumRawResults;
    }
}