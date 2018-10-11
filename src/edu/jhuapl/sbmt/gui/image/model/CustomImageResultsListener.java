package edu.jhuapl.sbmt.gui.image.model;

import java.util.List;

import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ImageInfo;

public interface CustomImageResultsListener
{
    public void resultsChanged(List<ImageInfo> results);
}
