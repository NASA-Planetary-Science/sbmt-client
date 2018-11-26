package edu.jhuapl.sbmt.gui.image.controllers;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

public class StringRenderer extends DefaultTableCellRenderer
{
    public PerspectiveImageBoundaryCollection model;
    private ImageSearchModel imageSearchModel;
    private List<List<String>> imageRawResults;

    public StringRenderer(ImageSearchModel imageSearchModel, List<List<String>> imageRawResults)
    {
        this.imageSearchModel = imageSearchModel;
        this.imageRawResults = imageRawResults;
        model = (PerspectiveImageBoundaryCollection)imageSearchModel.getModelManager().getModel(imageSearchModel.getImageBoundaryCollectionModelName());

    }

    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        Component co = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String name = imageRawResults.get(row).get(0);
//        ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
        ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
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

    public void setImageRawResults(List<List<String>> imageRawResults)
    {
        this.imageRawResults = imageRawResults;
    }
}