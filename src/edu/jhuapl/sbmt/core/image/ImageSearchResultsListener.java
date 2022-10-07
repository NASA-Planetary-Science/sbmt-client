package edu.jhuapl.sbmt.core.image;

import java.util.List;

public interface ImageSearchResultsListener
{
    public void resultsChanged(List<List<String>> results);

    public void resultsCountChanged(int count);
}
