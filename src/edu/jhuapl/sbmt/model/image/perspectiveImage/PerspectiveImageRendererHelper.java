package edu.jhuapl.sbmt.model.image.perspectiveImage;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;

import vtk.vtkCell;
import vtk.vtkCellData;
import vtk.vtkDataArray;
import vtk.vtkImageCanvasSource2D;
import vtk.vtkImageData;
import vtk.vtkImageMapToColors;
import vtk.vtkImageMask;
import vtk.vtkImageReslice;
import vtk.vtkLookupTable;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import vtk.vtkProp;
import vtk.vtkTexture;

import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;

class PerspectiveImageRendererHelper
{
	PerspectiveImage image;
	protected vtkImageData rawImage;
    private vtkImageData displayedImage;
    private PerspectiveImageFootprint footprint;
    private PerspectiveImageFrustum frustum;

    private vtkPolyDataNormals normalsFilter;

    private boolean normalsGenerated = false;
    private vtkImageCanvasSource2D maskSource;

    private boolean simulateLighting = false;

    // Always use accessors to use this field -- even within this class!
    private IntensityRange[] displayedRange = null;

    private int[] currentMask = new int[4];

    private double minIncidence = Double.MAX_VALUE;
    private double maxIncidence = -Double.MAX_VALUE;
    private double minEmission = Double.MAX_VALUE;
    private double maxEmission = -Double.MAX_VALUE;
    private double minPhase = Double.MAX_VALUE;
    private double maxPhase = -Double.MAX_VALUE;

    double minHorizontalPixelScale = Double.MAX_VALUE;
    double maxHorizontalPixelScale = -Double.MAX_VALUE;
    double meanHorizontalPixelScale = 0.0;
    double minVerticalPixelScale = Double.MAX_VALUE;
    double maxVerticalPixelScale = -Double.MAX_VALUE;
    double meanVerticalPixelScale = 0.0;

    private List<vtkProp> actors = new ArrayList<vtkProp>();


	public PerspectiveImageRendererHelper(PerspectiveImage image)
	{
		this.image = image;
		this.frustum = new PerspectiveImageFrustum(image);
		this.footprint = new PerspectiveImageFootprint(image);
//        int nslices = image.getImageDepth();
	}

	public void initialize()
	{
		footprint.initialize();
		frustum.initialize();
	}

	void resetFrustaAndFootprint(int slice)
    {
    	frustum.frusta[slice] = null;
        footprint.footprintGenerated[slice] = false;
    }

	public boolean[] getFootprintGenerated()
	{
		return footprint.getFootprintGenerated();
	}

	public void setFootprintGenerated(boolean footprintGenerated)
	{
		footprint.setFootprintGenerated(footprintGenerated);
	}

	public void setFootprintGenerated(boolean footprintGenerated, int slice)
	{
		footprint.setFootprintGenerated(footprintGenerated, slice);
	}

	public static boolean isGenerateFootprint()
	{
		return PerspectiveImageFootprint.isGenerateFootprint();
	}

	public double getOpacity()
    {
        return footprint.getOpacity();
    }

    public void setCurrentMask(int[] masking)
    {
        int topMask = masking[0];
        int rightMask = masking[1];
        int bottomMask = masking[2];
        int leftMask = masking[3];
        // Initialize the mask to black which masks out the image
        maskSource.SetDrawColor(0.0, 0.0, 0.0, 0.0);
        maskSource.FillBox(0, image.imageWidth - 1, 0, image.imageHeight - 1);
        // Create a square inside mask which passes through the image.
        maskSource.SetDrawColor(255.0, 255.0, 255.0, 255.0);
        maskSource.FillBox(leftMask, image.imageWidth - 1 - rightMask, bottomMask, image.imageHeight - 1 - topMask);
        maskSource.Update();

        image.firePropertyChange(Properties.MODEL_CHANGED, null, this);
        setDisplayedImageRange(null);

        for (int i = 0; i < masking.length; ++i)
            currentMask[i] = masking[i];
    }

    int[] getCurrentMask()
    {
        return currentMask.clone();
    }

    public boolean isNormalsGenerated()
    {
        return normalsGenerated;
    }

    public void setNormalsGenerated(boolean normalsGenerated)
    {
        this.normalsGenerated = normalsGenerated;
    }

    public void imageAboutToBeRemoved()
    {
        frustum.setShowFrustum(false);
    }

    int getNumberOfComponentsOfOriginalImage()
    {
        return rawImage.GetNumberOfScalarComponents();
    }

    /**
     * Return surface area of footprint (unshifted) of image.
     *
     * @return
     */
    double getSurfaceArea()
    {
        return PolyDataUtil.getSurfaceArea(footprint.getFootprint(image.currentSlice));
    }

    public void setOpacity(double imageOpacity)
    {
        footprint.setOpacity(imageOpacity);
    }

    double getMinFrustumDepth(int slice)
    {
        return frustum.getMinFrustumDepth(slice);
    }

    double getMaxFrustumDepth(int slice)
    {
        return frustum.getMaxFrustumDepth(slice);
    }

    public void setVisible(boolean b)
    {
        footprint.setVisible(b);
    }

    void Delete()
    {
        displayedImage.Delete();
        rawImage.Delete();
        footprint.Delete();
        normalsFilter.Delete();
        maskSource.Delete();
    }

    void computeCellNormals()
    {
        if (normalsGenerated == false)
        {
        	vtkPolyData[] fprint = footprint.getFootprint();
        	int currentSlice = image.currentSlice;
            normalsFilter.SetInputData(fprint[currentSlice]);
            normalsFilter.SetComputeCellNormals(1);
            normalsFilter.SetComputePointNormals(0);
            // normalsFilter.AutoOrientNormalsOn();
            // normalsFilter.ConsistencyOn();
            normalsFilter.SplittingOff();
            normalsFilter.Update();

            if (fprint != null && fprint[currentSlice] != null)
            {
                vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
                fprint[currentSlice].DeepCopy(normalsFilterOutput);
                normalsGenerated = true;
            }
        }
    }

    // Computes the incidence, emission, and phase at a point on the footprint with
    // a given normal.
    // (I.e. the normal of the plate which the point is lying on).
    // The output is a 3-vector with the first component equal to the incidence,
    // the second component equal to the emission and the third component equal to
    // the phase.
    double[] computeIlluminationAnglesAtPoint(double[] pt, double[] normal)
    {
    	int currentSlice = image.currentSlice;
    	double[][] spacecraftPositionAdjusted = image.getSpacecraftPositionAdjusted();
        double[] scvec = {
        		spacecraftPositionAdjusted[currentSlice][0] - pt[0],
        		spacecraftPositionAdjusted[currentSlice][1] - pt[1],
        		spacecraftPositionAdjusted[currentSlice][2] - pt[2] };

        double[] sunVectorAdjusted = image.getSunVector();
        double incidence = MathUtil.vsep(normal, sunVectorAdjusted) * 180.0 / Math.PI;
        double emission = MathUtil.vsep(normal, scvec) * 180.0 / Math.PI;
        double phase = MathUtil.vsep(sunVectorAdjusted, scvec) * 180.0 / Math.PI;

        double[] angles = { incidence, emission, phase };

        return angles;
    }

    void computeIlluminationAngles()
    {
    	int currentSlice = image.getCurrentSlice();
    	vtkPolyData currentFootprint = footprint.getFootprint()[currentSlice];
        if (footprint.getFootprintGenerated()[currentSlice] == false)
            footprint.loadFootprint();

        computeCellNormals();

        int numberOfCells = currentFootprint.GetNumberOfCells();

        vtkPoints points = currentFootprint.GetPoints();
        vtkCellData footprintCellData = currentFootprint.GetCellData();
        vtkDataArray normals = footprintCellData.GetNormals();

        this.minEmission = Double.MAX_VALUE;
        this.maxEmission = -Double.MAX_VALUE;
        this.minIncidence = Double.MAX_VALUE;
        this.maxIncidence = -Double.MAX_VALUE;
        this.minPhase = Double.MAX_VALUE;
        this.maxPhase = -Double.MAX_VALUE;

        for (int i = 0; i < numberOfCells; ++i)
        {
            vtkCell cell = currentFootprint.GetCell(i);
            double[] pt0 = points.GetPoint(cell.GetPointId(0));
            double[] pt1 = points.GetPoint(cell.GetPointId(1));
            double[] pt2 = points.GetPoint(cell.GetPointId(2));
            double[] centroid = {
                    (pt0[0] + pt1[0] + pt2[0]) / 3.0,
                    (pt0[1] + pt1[1] + pt2[1]) / 3.0,
                    (pt0[2] + pt1[2] + pt2[2]) / 3.0
            };
            double[] normal = normals.GetTuple3(i);

            double[] angles = computeIlluminationAnglesAtPoint(centroid, normal);
            double incidence = angles[0];
            double emission = angles[1];
            double phase = angles[2];

            if (incidence < minIncidence)
                minIncidence = incidence;
            if (incidence > maxIncidence)
                maxIncidence = incidence;
            if (emission < minEmission)
                minEmission = emission;
            if (emission > maxEmission)
                maxEmission = emission;
            if (phase < minPhase)
                minPhase = phase;
            if (phase > maxPhase)
                maxPhase = phase;
            cell.Delete();
        }

        points.Delete();
        footprintCellData.Delete();
        if (normals != null)
            normals.Delete();
    }

    void computePixelScale()
    {
    	double[][] spacecraftPositionAdjusted = image.getSpacecraftPositionAdjusted();
    	int currentSlice = image.currentSlice;
    	vtkPolyData currentFootprint = footprint.getFootprint()[currentSlice];
        if (footprint.getFootprintGenerated()[currentSlice] == false)
            footprint.loadFootprint();

        int numberOfPoints = currentFootprint.GetNumberOfPoints();

        vtkPoints points = currentFootprint.GetPoints();

        minHorizontalPixelScale = Double.MAX_VALUE;
        maxHorizontalPixelScale = -Double.MAX_VALUE;
        meanHorizontalPixelScale = 0.0;
        minVerticalPixelScale = Double.MAX_VALUE;
        maxVerticalPixelScale = -Double.MAX_VALUE;
        meanVerticalPixelScale = 0.0;

        double horizScaleFactor = 2.0 * Math.tan(MathUtil.vsep(image.getFrustum1Adjusted()[currentSlice], image.getFrustum3Adjusted()[currentSlice]) / 2.0) / image.imageHeight;
        double vertScaleFactor = 2.0 * Math.tan(MathUtil.vsep(image.getFrustum1Adjusted()[currentSlice], image.getFrustum2Adjusted()[currentSlice]) / 2.0) / image.imageWidth;

        double[] vec = new double[3];

        for (int i = 0; i < numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            vec[0] = pt[0] - spacecraftPositionAdjusted[currentSlice][0];
            vec[1] = pt[1] - spacecraftPositionAdjusted[currentSlice][1];
            vec[2] = pt[2] - spacecraftPositionAdjusted[currentSlice][2];
            double dist = MathUtil.vnorm(vec);

            double horizPixelScale = dist * horizScaleFactor;
            double vertPixelScale = dist * vertScaleFactor;

            if (horizPixelScale < minHorizontalPixelScale)
                minHorizontalPixelScale = horizPixelScale;
            if (horizPixelScale > maxHorizontalPixelScale)
                maxHorizontalPixelScale = horizPixelScale;
            if (vertPixelScale < minVerticalPixelScale)
                minVerticalPixelScale = vertPixelScale;
            if (vertPixelScale > maxVerticalPixelScale)
                maxVerticalPixelScale = vertPixelScale;

            meanHorizontalPixelScale += horizPixelScale;
            meanVerticalPixelScale += vertPixelScale;
        }

        meanHorizontalPixelScale /= (double) numberOfPoints;
        meanVerticalPixelScale /= (double) numberOfPoints;

        points.Delete();
    }

    float[][][] convertvtkImageToArray3D(vtkImageData image)
    {
    	return ImageDataUtil.vtkImageDataToArray3D(rawImage);
    }

    void setSimulateLighting(boolean b)
    {
        simulateLighting = b;
    }

    boolean isSimulatingLighingOn()
    {
        return simulateLighting;
    }

    /**
     * Set the displayed image range of the currently selected slice of the image.
     * As a side-effect, this method also MAYBE CREATES the displayed image.
     *
     * @param range the new displayed range of the image. If null is passed,
     */
    void setDisplayedImageRange(IntensityRange range)
    {
    	int currentSlice = image.currentSlice;
        if (rawImage != null)
        {
            if (rawImage.GetNumberOfScalarComponents() > 1)
            {
                displayedImage = rawImage;
                return;
            }
        }

        IntensityRange displayedRange = getDisplayedRange(currentSlice);
        if (range == null || displayedRange.min != range.min || displayedRange.max != range.max)
        {
            if (range != null)
            {
                this.displayedRange[currentSlice] = range;
                image.saveImageInfo();
            }

            if (rawImage != null)
            {
                vtkImageData img = getImageWithDisplayedRange(range, false);
                if (displayedImage == null)
                    displayedImage = new vtkImageData();
                displayedImage.DeepCopy(img);
            }
        }

        image.firePropertyChange(Properties.MODEL_CHANGED, null, this);
    }

    vtkImageData getImageWithDisplayedRange(IntensityRange range, boolean offlimb)
    {
    	int currentSlice = image.currentSlice;
        float minValue = image.getMinValue();
        float maxValue = image.getMaxValue();
        float dx = (maxValue - minValue) / 255.0f;

        float min = minValue;
        float max = maxValue;
        if (!offlimb)
        {
            IntensityRange displayedRange = getDisplayedRange(currentSlice);
            min = minValue + displayedRange.min * dx;
            max = minValue + displayedRange.max * dx;
        }
        else
        {
            IntensityRange offLimbDisplayedRange = image.getOfflimbPlaneHelper().getOffLimbDisplayedRange();
            min = minValue + offLimbDisplayedRange.min * dx;
            max = minValue + offLimbDisplayedRange.max * dx;
        }

        // Update the displayed image
        vtkLookupTable lut = new vtkLookupTable();
        lut.SetTableRange(min, max);
        lut.SetValueRange(0.0, 1.0);
        lut.SetHueRange(0.0, 0.0);
        lut.SetSaturationRange(0.0, 0.0);
        // lut.SetNumberOfTableValues(402);
        lut.SetRampToLinear();
        lut.Build();

        // for 3D images, take the current slice
        vtkImageData image2D = rawImage;
        if (image.getImageDepth() > 1)
        {
            vtkImageReslice slicer = new vtkImageReslice();
            slicer.SetInputData(rawImage);
            slicer.SetOutputDimensionality(2);
            slicer.SetInterpolationModeToNearestNeighbor();
            slicer.SetOutputSpacing(1.0, 1.0, 1.0);
            slicer.SetResliceAxesDirectionCosines(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);

            slicer.SetOutputOrigin(0.0, 0.0, (double) currentSlice);
            slicer.SetResliceAxesOrigin(0.0, 0.0, (double) currentSlice);

            slicer.SetOutputExtent(0, image.imageWidth - 1, 0, image.imageHeight - 1, 0, 0);

            slicer.Update();
            image2D = slicer.GetOutput();
        }

        vtkImageMapToColors mapToColors = new vtkImageMapToColors();
        mapToColors.SetInputData(image2D);
        mapToColors.SetOutputFormatToRGBA();
        mapToColors.SetLookupTable(lut);
        mapToColors.Update();

        vtkImageData mapToColorsOutput = mapToColors.GetOutput();
        vtkImageData maskSourceOutput = maskSource.GetOutput();

        vtkImageMask maskFilter = new vtkImageMask();
        maskFilter.SetImageInputData(mapToColorsOutput);
        maskFilter.SetMaskInputData(maskSourceOutput);
        maskFilter.Update();

        vtkImageData maskFilterOutput = maskFilter.GetOutput();
        mapToColors.Delete();
        lut.Delete();
        mapToColorsOutput.Delete();
        maskSourceOutput.Delete();
        maskFilter.Delete();
        return maskFilterOutput;
    }

    /**
     * This getter lazily initializes the range field as necessary to
     * ensure this returns a valid, non-null range as long as the argument
     * is in range for this image.
     *
     * @param slice the number of the slice whose displayed range to return.
     */
    IntensityRange getDisplayedRange(int slice)
    {
        int nslices = image.getImageDepth();

        Preconditions.checkArgument(slice < nslices);

        if (displayedRange == null)
        {
            displayedRange = new IntensityRange[nslices];
        }
        if (displayedRange[slice] == null)
        {
            displayedRange[slice] = new IntensityRange(0, 255);
        }

        return displayedRange[slice];
    }

	vtkImageData getRawImage()
	{
		return rawImage;
	}

	vtkImageData getDisplayedImage()
	{
		return displayedImage;
	}

    vtkTexture getTexture()
    {
        return footprint.getTexture();
    }

    List<vtkProp> getProps()
    {
    	actors.addAll(footprint.getProps());
    	actors.addAll(frustum.getProps());
        // for offlimb
        actors.addAll(image.getOfflimbPlaneHelper().getProps());

        return actors;
    }

    double getMinIncidence()
    {
        return minIncidence;
    }

    double getMaxIncidence()
    {
        return maxIncidence;
    }

    double getMinEmission()
    {
        return minEmission;
    }

    double getMaxEmission()
    {
        return maxEmission;
    }

    double getMinPhase()
    {
        return minPhase;
    }

    double getMaxPhase()
    {
        return maxPhase;
    }

    IntensityRange getDisplayedRange()
    {
        return getDisplayedRange(image.currentSlice);
    }


    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            footprint.loadFootprint();
            normalsGenerated = false;
            this.minEmission = Double.MAX_VALUE;
            this.maxEmission = -Double.MAX_VALUE;
            this.minIncidence = Double.MAX_VALUE;
            this.maxIncidence = -Double.MAX_VALUE;
            this.minPhase = Double.MAX_VALUE;
            this.maxPhase = -Double.MAX_VALUE;
            this.minHorizontalPixelScale = Double.MAX_VALUE;
            this.maxHorizontalPixelScale = -Double.MAX_VALUE;
            this.minVerticalPixelScale = Double.MAX_VALUE;
            this.maxVerticalPixelScale = -Double.MAX_VALUE;
            this.meanHorizontalPixelScale = 0.0;
            this.meanVerticalPixelScale = 0.0;

            image.firePropertyChange(Properties.MODEL_CHANGED, null, this);
        }
    }


	void setRawImage(vtkImageData rawImage)
	{
		this.rawImage = rawImage;
	}

    void initializeMaskingAfterLoad()
    {
    	int[] masking = image.getMaskSizes();
        int topMask = masking[0];
        int rightMask = masking[1];
        int bottomMask = masking[2];
        int leftMask = masking[3];
        for (int i = 0; i < masking.length; ++i)
            currentMask[i] = masking[i];

        maskSource = new vtkImageCanvasSource2D();
        maskSource.SetScalarTypeToUnsignedChar();
        maskSource.SetNumberOfScalarComponents(1);
        // maskSource.SetExtent(0, imageWidth-1, 0, imageHeight-1, 0, imageDepth-1);
        maskSource.SetExtent(0, image.imageWidth - 1, 0, image.imageHeight - 1, 0, 0);
        // Initialize the mask to black which masks out the image
        maskSource.SetDrawColor(0.0, 0.0, 0.0, 0.0);
        maskSource.FillBox(0, image.imageWidth - 1, 0, image.imageHeight - 1);
        // Create a square inside mask which passes through the image.
        maskSource.SetDrawColor(255.0, 255.0, 255.0, 255.0);
        maskSource.FillBox(leftMask, image.imageWidth - 1 - rightMask, bottomMask, image.imageHeight - 1 - topMask);
        maskSource.Update();

        footprint.initializeMaskingAfterLoad();
        normalsFilter = new vtkPolyDataNormals();
    }

    /**
     * Give oppurtunity to subclass to do some processing on the raw image such as
     * resizing, flipping, masking, etc.
     *
     * @param rawImage
     */
    void processRawImage(vtkImageData rawImage)
    {
    	if (image.getFlip().equals("X"))
        {
            ImageDataUtil.flipImageXAxis(rawImage);
        }
        else if (image.getFlip().equals("Y"))
        {
            ImageDataUtil.flipImageYAxis(rawImage);
        }
        if (image.getRotation() != 0.0)
            ImageDataUtil.rotateImage(rawImage, 360.0 - image.getRotation());
    }

    vtkImageData createRawImage(int height, int width, int depth, float[][] array2D, float[][][] array3D)
    {
        return createRawImage(height, width, depth, true, array2D, array3D);
    }

    vtkImageData createRawImage(int height, int width, int depth, boolean transpose, float[][] array2D, float[][][] array3D)
    {
        // Allocate enough room to store min/max value at each layer
        image.maxValue = new float[depth];
        image.minValue = new float[depth];

        // Call
        return ImageDataUtil.createRawImage(height, width, depth, transpose, array2D, array3D, image.minValue, image.maxValue);
    }

	public PerspectiveImageFootprint getFootprint()
	{
		return footprint;
	}

	public PerspectiveImageFrustum getFrustum()
	{
		return frustum;
	}
}