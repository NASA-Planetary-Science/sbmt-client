package edu.jhuapl.sbmt.gui.image.model;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;

public class ColorImageModel
{

    private ImageKey selectedRedKey;
    private ImageKey selectedGreenKey;
    private ImageKey selectedBlueKey;

    public ColorImageModel()
    {
        // TODO Auto-generated constructor stub
    }

    public ModelNames getColorImageCollectionModelName()
    {
        return ModelNames.COLOR_IMAGES;
    }

    public ImageKey getSelectedRedKey()
    {
        return selectedRedKey;
    }

    public ImageKey getSelectedGreenKey()
    {
        return selectedGreenKey;
    }

    public ImageKey getSelectedBlueKey()
    {
        return selectedBlueKey;
    }

    public void setSelectedRedKey(ImageKey selectedRedKey)
    {
        this.selectedRedKey = selectedRedKey;
    }

    public void setSelectedGreenKey(ImageKey selectedGreenKey)
    {
        this.selectedGreenKey = selectedGreenKey;
    }

    public void setSelectedBlueKey(ImageKey selectedBlueKey)
    {
        this.selectedBlueKey = selectedBlueKey;
    }


}
