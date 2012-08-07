package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkGenericCell;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTexture;
import vtk.vtksbCellLocator;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.util.Frustum;
import edu.jhuapl.near.util.ImageDataUtil;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class ColorImage extends Model implements PropertyChangeListener
{
    private SmallBodyModel smallBodyModel;
    private PerspectiveImage redImage;
    private PerspectiveImage greenImage;
    private PerspectiveImage blueImage;
    private vtkImageData colorImage;
    private vtkPolyData footprint;
    private vtkPolyData shiftedFootprint;
    private vtkActor footprintActor;
    private ArrayList<vtkProp> footprintActors = new ArrayList<vtkProp>();
    private float[][] redPixelData;
    private float[][] greenPixelData;
    private float[][] bluePixelData;
    private ColorImageKey colorKey;

    static public class NoOverlapException extends Exception
    {
        public NoOverlapException()
        {
            super("No overlap in 3 images");
        }
    }

    public static class ColorImageKey
    {
        public PerspectiveImage.ImageKey redImageKey;
        public PerspectiveImage.ImageKey greenImageKey;
        public PerspectiveImage.ImageKey blueImageKey;

        public ColorImageKey(PerspectiveImage.ImageKey redImage, PerspectiveImage.ImageKey greenImage, PerspectiveImage.ImageKey blueImage)
        {
            this.redImageKey = redImage;
            this.greenImageKey = greenImage;
            this.blueImageKey = blueImage;
        }

        @Override
        public boolean equals(Object obj)
        {
            return redImageKey.equals(((ColorImageKey)obj).redImageKey) &&
            greenImageKey.equals(((ColorImageKey)obj).greenImageKey) &&
            blueImageKey.equals(((ColorImageKey)obj).blueImageKey);
        }

        @Override
        public String toString()
        {
            // Find the start and stop indices of number part of the name. Should be
            // the same for all 3 images.
            String name = new File(redImageKey.name).getName();
            char[] buf = name.toCharArray();
            int ind0 = -1;
            int ind1 = -1;
            for (int i = 0; i<buf.length; ++i)
            {
                if (Character.isDigit(buf[i]) && ind0 == -1)
                    ind0 = i;
                else if(!Character.isDigit(buf[i]) && ind0 >= 0)
                {
                    ind1 = i;
                    break;
                }
            }

            if (buf[ind0] == '0')
                ++ind0;

            return
            "R: " + new File(redImageKey.name).getName().substring(ind0, ind1) + ", " +
            "G: " + new File(greenImageKey.name).getName().substring(ind0, ind1) + ", " +
            "B: " + new File(blueImageKey.name).getName().substring(ind0, ind1);
        }
    }

    public ColorImage(ColorImageKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException, NoOverlapException
    {
        this.colorKey = key;
        this.smallBodyModel = smallBodyModel;

        redImage = createImage(colorKey.redImageKey, smallBodyModel);
        greenImage = createImage(colorKey.greenImageKey, smallBodyModel);
        blueImage = createImage(colorKey.blueImageKey, smallBodyModel);

        redPixelData = ImageDataUtil.vtkImageDataToArray2D(redImage.getRawImage());
        greenPixelData = ImageDataUtil.vtkImageDataToArray2D(greenImage.getRawImage());
        bluePixelData = ImageDataUtil.vtkImageDataToArray2D(blueImage.getRawImage());

        colorImage = new vtkImageData();
        colorImage.SetScalarTypeToUnsignedChar();
        colorImage.SetDimensions(redImage.getImageWidth(), redImage.getImageHeight(), 1);
        colorImage.SetSpacing(1.0, 1.0, 1.0);
        colorImage.SetOrigin(0.0, 0.0, 0.0);
        colorImage.SetNumberOfScalarComponents(3);

        shiftedFootprint = new vtkPolyData();

        computeFootprintAndColorImage();
    }

    protected PerspectiveImage createImage(ImageKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException
    {
        return (PerspectiveImage)ImageFactory.createImage(key, smallBodyModel, false, null);
    }

    private void computeFootprintAndColorImage() throws NoOverlapException
    {
        Frustum redFrustum = redImage.getFrustum();
        Frustum greenFrustum = greenImage.getFrustum();
        Frustum blueFrustum = blueImage.getFrustum();

        double[] redRange = redImage.getRawImage().GetScalarRange();
        double[] greenRange = greenImage.getRawImage().GetScalarRange();
        double[] blueRange = blueImage.getRawImage().GetScalarRange();
//        double redScalarExtent = redRange[1] - redRange[0];
//        double greenScalarExtent = greenRange[1] - greenRange[0];
//        double blueScalarExtent = blueRange[1] - blueRange[0];
        double redScalarExtent = redRange[1] - 0.0;
        double greenScalarExtent = greenRange[1] - 0.0;
        double blueScalarExtent = blueRange[1] - 0.0;

        ArrayList<Frustum> frustums = new ArrayList<Frustum>();
        frustums.add(redFrustum);
        frustums.add(greenFrustum);
        frustums.add(blueFrustum);

        footprint = smallBodyModel.computeMultipleFrustumIntersection(frustums);

        if (footprint == null)
            throw new NoOverlapException();

        // Need to clear out scalar data since if coloring data is being shown,
        // then the color might mix-in with the image.
        footprint.GetCellData().SetScalars(null);
        footprint.GetPointData().SetScalars(null);

        int IMAGE_WIDTH = redImage.getImageWidth();
        int IMAGE_HEIGHT = redImage.getImageHeight();

        PolyDataUtil.generateTextureCoordinates(redFrustum, IMAGE_WIDTH, IMAGE_HEIGHT, footprint);

        shiftedFootprint.DeepCopy(footprint);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, 2.0*smallBodyModel.getMinShiftAmount());

        // Now compute a color image with each channel one of these images.
        // To do that go through each pixel of the red image, and intersect a ray into the asteroid in
        // the direction of that pixel. Then compute the texture coordinates that the intersection
        // point would have for the green and blue images. Do linear interpolation in
        // the green and blue images to compute the green and blue channels.

        vtksbCellLocator cellLocator = new vtksbCellLocator();
        cellLocator.SetDataSet(footprint);
        cellLocator.CacheCellBoundsOn();
        cellLocator.AutomaticOn();
        //cellLocator.SetMaxLevel(10);
        //cellLocator.SetNumberOfCellsPerNode(15);
        cellLocator.BuildLocator();

        vtkGenericCell cell = new vtkGenericCell();

        double[] spacecraftPosition = redFrustum.origin;
        double[] frustum1 = redFrustum.ul;
        double[] frustum2 = redFrustum.lr;
        double[] frustum3 = redFrustum.ur;

        double[] corner1 = {
                spacecraftPosition[0] + frustum1[0],
                spacecraftPosition[1] + frustum1[1],
                spacecraftPosition[2] + frustum1[2]
        };
        double[] corner2 = {
                spacecraftPosition[0] + frustum2[0],
                spacecraftPosition[1] + frustum2[1],
                spacecraftPosition[2] + frustum2[2]
        };
        double[] corner3 = {
                spacecraftPosition[0] + frustum3[0],
                spacecraftPosition[1] + frustum3[1],
                spacecraftPosition[2] + frustum3[2]
        };
        double[] vec12 = {
                corner2[0] - corner1[0],
                corner2[1] - corner1[1],
                corner2[2] - corner1[2]
        };
        double[] vec13 = {
                corner3[0] - corner1[0],
                corner3[1] - corner1[1],
                corner3[2] - corner1[2]
        };


        double scdist = MathUtil.vnorm(spacecraftPosition);

        for (int i=0; i<IMAGE_HEIGHT; ++i)
        {
            // Compute the vector on the left of the row.
            double fracHeight = ((double)i / (double)(IMAGE_HEIGHT-1));
            double[] left = {
                    corner1[0] + fracHeight*vec13[0],
                    corner1[1] + fracHeight*vec13[1],
                    corner1[2] + fracHeight*vec13[2]
            };

            for (int j=0; j<IMAGE_WIDTH; ++j)
            {
                double fracWidth = ((double)j / (double)(IMAGE_WIDTH-1));
                double[] vec = {
                        left[0] + fracWidth*vec12[0],
                        left[1] + fracWidth*vec12[1],
                        left[2] + fracWidth*vec12[2]
                };
                vec[0] -= spacecraftPosition[0];
                vec[1] -= spacecraftPosition[1];
                vec[2] -= spacecraftPosition[2];
                MathUtil.unorm(vec, vec);

                double[] lookPt = {
                        spacecraftPosition[0] + 2.0*scdist*vec[0],
                        spacecraftPosition[1] + 2.0*scdist*vec[1],
                        spacecraftPosition[2] + 2.0*scdist*vec[2]
                };

                double tol = 1e-6;
                double[] t = new double[1];
                double[] x = new double[3];
                double[] pcoords = new double[3];
                int[] subId = new int[1];
                int[] cellId = new int[1];
                int result = cellLocator.IntersectWithLine(spacecraftPosition, lookPt, tol, t, x, pcoords, subId, cellId, cell);

                if (result > 0)
                {
                    float redValue = redPixelData[j][i];

                    double[] uv = new double[2];

                    greenFrustum.computeTextureCoordinates(x, IMAGE_WIDTH, IMAGE_HEIGHT, uv);
                    float greenValue = ImageDataUtil.interpolateWithinImage(
                            greenPixelData,
                            IMAGE_WIDTH,
                            IMAGE_HEIGHT,
                            uv[1],
                            uv[0]);

                    blueFrustum.computeTextureCoordinates(x, IMAGE_WIDTH, IMAGE_HEIGHT, uv);
                    float blueValue = ImageDataUtil.interpolateWithinImage(
                            bluePixelData,
                            IMAGE_WIDTH,
                            IMAGE_HEIGHT,
                            uv[1],
                            uv[0]);

                    colorImage.SetScalarComponentFromFloat(j, i, 0, 0, 255.0 * redValue / redScalarExtent);
                    colorImage.SetScalarComponentFromFloat(j, i, 0, 1, 255.0 * greenValue / greenScalarExtent);
                    colorImage.SetScalarComponentFromFloat(j, i, 0, 2, 255.0 * blueValue / blueScalarExtent);

//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 0, 255.0 * redValue / redScalarExtent);
//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 1, 255.0 * redValue / redScalarExtent);
//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 2, 255.0 * redValue / redScalarExtent);
//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 0, 255.0 * greenValue / greenScalarExtent);
//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 1, 255.0 * greenValue / greenScalarExtent);
//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 2, 255.0 * greenValue / greenScalarExtent);
//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 0, 255.0 * blueValue / blueScalarExtent);
//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 1, 255.0 * blueValue / blueScalarExtent);
//                    colorImage.SetScalarComponentFromFloat(j, i, 0, 2, 255.0 * blueValue / blueScalarExtent);

                }
            }
        }
    }

    @Override
    public ArrayList<vtkProp> getProps()
    {
        if (footprintActor == null)
        {
            vtkTexture texture = new vtkTexture();
            texture.InterpolateOn();
            texture.RepeatOff();
            texture.EdgeClampOn();
            texture.SetInput(colorImage);

            vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
            footprintMapper.SetInput(shiftedFootprint);
            footprintMapper.Update();

            footprintActor = new vtkActor();
            footprintActor.SetMapper(footprintMapper);
            footprintActor.SetTexture(texture);
            vtkProperty footprintProperty = footprintActor.GetProperty();
            footprintProperty.LightingOff();

            footprintActors.add(footprintActor);
        }
        return footprintActors;
    }

    public ColorImageKey getKey()
    {
        return colorKey;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            try
            {
                computeFootprintAndColorImage();

                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
