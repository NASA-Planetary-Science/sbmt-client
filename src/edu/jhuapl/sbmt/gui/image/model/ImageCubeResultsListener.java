package edu.jhuapl.sbmt.gui.image.model;

import edu.jhuapl.sbmt.model.image.ImageCube.ImageCubeKey;

public interface ImageCubeResultsListener
{
    public void imageCubeAdded(ImageCubeKey image);

    public void imageCubeRemoved(ImageCubeKey image);

    public void presentErrorMessage(String message);

    public void presentInformationalMessage(String message);
}
