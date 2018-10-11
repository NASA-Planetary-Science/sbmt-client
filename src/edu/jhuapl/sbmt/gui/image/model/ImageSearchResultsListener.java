package edu.jhuapl.sbmt.gui.image.model;

import java.util.List;

public interface ImageSearchResultsListener
{
    public void resultsChanged(List<List<String>> results);
}
