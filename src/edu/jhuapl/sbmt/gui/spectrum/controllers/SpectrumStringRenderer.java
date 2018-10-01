package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchModel;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.spectrum.Spectrum.SpectrumKey;

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
        String name = spectrumRawResults.get(row).get(0);
//        ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
        SpectrumKey key = spectrumSearchModel.createSpectrumKey(name.substring(0, name.length()-4), spectrumSearchModel.getInstrument());
//        if (model.containsBoundary(key))
//        {
//            int[] c = model.getBoundary(key).getBoundaryColor();
//            if (isSelected)
//            {
//                co.setForeground(new Color(c[0], c[1], c[2]));
//                co.setBackground(table.getSelectionBackground());
//            }
//            else
//            {
//                co.setForeground(new Color(c[0], c[1], c[2]));
//                co.setBackground(table.getBackground());
//            }
//        }
//        else
//        {
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
//        }

        return co;
    }

    public void setSpectrumRawResults(List<List<String>> spectrumRawResults)
    {
        this.spectrumRawResults = spectrumRawResults;
    }
}