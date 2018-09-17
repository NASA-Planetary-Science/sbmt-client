package edu.jhuapl.sbmt.gui.image.model;

import edu.jhuapl.saavtk.model.ModelNames;

public class ImageCubeGenerationModel
{
    int nbands = 0;

    public ImageCubeGenerationModel()
    {
        // TODO Auto-generated constructor stub
    }


    public ModelNames getImageCubeCollectionModelName()
    {
        return ModelNames.CUBE_IMAGES;
    }


    public int getNbands()
    {
        return nbands;
    }


    public void setNbands(int nbands)
    {
        this.nbands = nbands;
    }
}
