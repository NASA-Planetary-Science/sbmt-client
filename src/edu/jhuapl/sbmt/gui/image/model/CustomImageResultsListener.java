package edu.jhuapl.sbmt.gui.image.model;

import java.util.List;

import edu.jhuapl.sbmt.model.image.keys.CustomImageKeyInterface;

public interface CustomImageResultsListener
{
    public void resultsChanged(List<CustomImageKeyInterface> results);
}
