package edu.jhuapl.sbmt.gui.image.model.custom;

import java.util.List;

import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ImageInfo;

public interface CustomImageResultsListener
{
    public void resultsChanged(List<ImageInfo> results);
}
