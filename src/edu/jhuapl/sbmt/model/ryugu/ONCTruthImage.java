package edu.jhuapl.sbmt.model.ryugu;

import java.io.IOException;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;

import nom.tam.fits.FitsException;

public class ONCTruthImage extends ONCImage
{

    public ONCTruthImage(ImageKeyInterface key, SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // There is a -90 degree rotation in the data.
        ImageDataUtil.rotateImage(rawImage, -90);
    }
}
