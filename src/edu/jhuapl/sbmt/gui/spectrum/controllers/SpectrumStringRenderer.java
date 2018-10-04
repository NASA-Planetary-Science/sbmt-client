package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchModel;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

public class SpectrumStringRenderer extends DefaultTableCellRenderer
{
    public PerspectiveImageBoundaryCollection model;
    private SpectrumSearchModel spectrumSearchModel;
    private List<List<String>> spectrumRawResults;

    public SpectrumStringRenderer(SpectrumSearchModel spectrumSearchModel, List<List<String>> spectrumRawResults)
    {
        this.spectrumSearchModel = spectrumSearchModel;
        this.spectrumRawResults = spectrumRawResults;
        model = (PerspectiveImageBoundaryCollection)spectrumSearchModel.getModelManager().getModel(spectrumSearchModel.getSpectrumBoundaryCollectionModelName());
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        Component co = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
        return co;
    }

    public void setSpectrumRawResults(List<List<String>> spectrumRawResults)
    {
        this.spectrumRawResults = spectrumRawResults;
    }
}