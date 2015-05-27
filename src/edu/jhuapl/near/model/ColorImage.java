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
import edu.jhuapl.near.util.IntensityRange;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class ColorImage extends Model implements PropertyChangeListener
{
    private SmallBodyModel smallBodyModel;
    private PerspectiveImage redImage;
    private PerspectiveImage greenImage;
    private PerspectiveImage blueImage;
    private int redImageSlice;
    private int greenImageSlice;
    private int blueImageSlice;
    private vtkImageData colorImage;
    private vtkPolyData footprint;
    private vtkPolyData shiftedFootprint;
    private vtkActor footprintActor;
    private ArrayList<vtkProp> footprintActors = new ArrayList<vtkProp>();
    private float[][] redPixelData;
    private float[][] greenPixelData;
    private float[][] bluePixelData;
    private ColorImageKey colorKey;
    private IntensityRange redIntensityRange = new IntensityRange(0, 255);
    private IntensityRange greenIntensityRange = new IntensityRange(0, 255);
    private IntensityRange blueIntensityRange = new IntensityRange(0, 255);
    private double offset;
    private double imageOpacity = 1.0;

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

    public ColorImage(ColorImageKey key, SmallBodyModel smallBodyModel, ModelManager modelManager) throws FitsException, IOException, NoOverlapException
    {
        this.colorKey = key;
        this.smallBodyModel = smallBodyModel;

        this.offset = getDefaultOffset();

        redImage = createImage(colorKey.redImageKey, smallBodyModel, modelManager);
        greenImage = createImage(colorKey.greenImageKey, smallBodyModel, modelManager);
        blueImage = createImage(colorKey.blueImageKey, smallBodyModel, modelManager);

        redImageSlice = colorKey.redImageKey.slice;
        greenImageSlice = colorKey.greenImageKey.slice;
        blueImageSlice = colorKey.blueImageKey.slice;

        int rslice = colorKey.redImageKey.slice;
        redPixelData = ImageDataUtil.vtkImageDataToArray2D(redImage.getRawImage(), rslice);

        int gslice = colorKey.greenImageKey.slice;
        greenPixelData = ImageDataUtil.vtkImageDataToArray2D(greenImage.getRawImage(), gslice);

        int bslice = colorKey.blueImageKey.slice;
        bluePixelData = ImageDataUtil.vtkImageDataToArray2D(blueImage.getRawImage(), bslice);

        colorImage = new vtkImageData();
        colorImage.SetScalarTypeToUnsignedChar();
        colorImage.SetDimensions(redImage.getImageWidth(), redImage.getImageHeight(), 1);
        colorImage.SetSpacing(1.0, 1.0, 1.0);
        colorImage.SetOrigin(0.0, 0.0, 0.0);
        colorImage.SetNumberOfScalarComponents(3);

        shiftedFootprint = new vtkPolyData();

        computeFootprintAndColorImage();
    }

    protected PerspectiveImage createImage(ImageKey key, SmallBodyModel smallBodyModel, ModelManager modelManager) throws FitsException, IOException
    {
        ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
        PerspectiveImage result = (PerspectiveImage)images.getImage(key);
        if (result == null)
            result = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
        return result;
    }

    private void computeFootprintAndColorImage() throws NoOverlapException
    {
        Frustum redFrustum = redImage.getFrustum(redImageSlice);
        Frustum greenFrustum = greenImage.getFrustum(greenImageSlice);
        Frustum blueFrustum = blueImage.getFrustum(blueImageSlice);

        double[] redRange = redImage.getScalarRange(redImageSlice);
        double[] greenRange = greenImage.getScalarRange(greenImageSlice);
        double[] blueRange = blueImage.getScalarRange(blueImageSlice);

        double redfullRange = redRange[1] - redRange[0];
        double reddx = redfullRange / 255.0f;
        double redmin = redRange[0] + redIntensityRange.min*reddx;
        double redmax = redRange[0] + redIntensityRange.max*reddx;
        double redstretchRange = redmax - redmin;

        double greenfullRange = greenRange[1] - greenRange[0];
        double greendx = greenfullRange / 255.0f;
        double greenmin = greenRange[0] + greenIntensityRange.min*greendx;
        double greenmax = greenRange[0] + greenIntensityRange.max*greendx;
        double greenstretchRange = greenmax - greenmin;

        double bluefullRange = blueRange[1] - blueRange[0];
        double bluedx = bluefullRange / 255.0f;
        double bluemin = blueRange[0] + blueIntensityRange.min*bluedx;
        double bluemax = blueRange[0] + blueIntensityRange.max*bluedx;
        double bluestretchRange = bluemax - bluemin;

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
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, offset);

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
                    double redValue = redPixelData[j][i];

                    double[] uv = new double[2];

                    greenFrustum.computeTextureCoordinates(x, IMAGE_WIDTH, IMAGE_HEIGHT, uv);
                    double greenValue = ImageDataUtil.interpolateWithinImage(
                            greenPixelData,
                            IMAGE_WIDTH,
                            IMAGE_HEIGHT,
                            uv[1],
                            uv[0]);

                    blueFrustum.computeTextureCoordinates(x, IMAGE_WIDTH, IMAGE_HEIGHT, uv);
                    double blueValue = ImageDataUtil.interpolateWithinImage(
                            bluePixelData,
                            IMAGE_WIDTH,
                            IMAGE_HEIGHT,
                            uv[1],
                            uv[0]);

                    if (redValue < redmin)
                        redValue = redmin;
                    if (redValue > redmax)
                        redValue = redmax;

                    if (greenValue < greenmin)
                        greenValue = greenmin;
                    if (greenValue > greenmax)
                        greenValue = greenmax;

                    if (blueValue < bluemin)
                        blueValue = bluemin;
                    if (blueValue > bluemax)
                        blueValue = bluemax;

                    colorImage.SetScalarComponentFromFloat(j, i, 0, 0, 255.0 * (redValue - redmin) / redstretchRange);
                    colorImage.SetScalarComponentFromFloat(j, i, 0, 1, 255.0 * (greenValue - greenmin) / greenstretchRange);
                    colorImage.SetScalarComponentFromFloat(j, i, 0, 2, 255.0 * (blueValue - bluemin) / bluestretchRange);
                }
            }
        }

        colorImage.Modified();
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

    public PerspectiveImage getRedImage()
    {
        return redImage;
    }

    public PerspectiveImage getGreenImage()
    {
        return greenImage;
    }

    public PerspectiveImage getBlueImage()
    {
        return blueImage;
    }

    public void setDisplayedImageRange(IntensityRange redRange, IntensityRange greenRange, IntensityRange blueRange)
    {
        try
        {
            redIntensityRange = redRange;
            greenIntensityRange = greenRange;
            blueIntensityRange = blueRange;
            computeFootprintAndColorImage();

            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
        catch (NoOverlapException e)
        {
            e.printStackTrace();
        }
    }

    public double getOpacity()
    {
        return imageOpacity;
    }

    public void setOpacity(double imageOpacity)
    {
        this.imageOpacity  = imageOpacity;
        vtkProperty smallBodyProperty = footprintActor.GetProperty();
        smallBodyProperty.SetOpacity(imageOpacity);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setVisible(boolean b)
    {
        footprintActor.SetVisibility(b ? 1 : 0);
        super.setVisible(b);
    }

    public double getDefaultOffset()
    {
        return 4.0*smallBodyModel.getMinShiftAmount();
    }

    public void setOffset(double offset)
    {
        this.offset = offset;

        shiftedFootprint.DeepCopy(footprint);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, offset);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public double getOffset()
    {
        return offset;
    }

}
