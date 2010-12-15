package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import vtk.vtkGenericCell;
import vtk.vtkPolyData;
import vtk.vtkProp;
import vtk.vtksbCellLocator;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Frustum;
import edu.jhuapl.near.util.MathUtil;

public class MSIColorImage extends Model implements PropertyChangeListener
{
    private SmallBodyModel erosModel;
    private MSIImage redImage;
    private MSIImage greenImage;
    private MSIImage blueImage;

    public MSIColorImage(MSIImage redImage, MSIImage greenImage, MSIImage blueImage, SmallBodyModel eros)
    {
        this.redImage = redImage;
        this.greenImage = greenImage;
        this.blueImage = blueImage;
        this.erosModel = eros;

        Frustum redFrustum = redImage.getFrustum();
        Frustum greenFrustum = greenImage.getFrustum();
        Frustum blueFrustum = blueImage.getFrustum();


        ArrayList<Frustum> frustums = new ArrayList<Frustum>();
        frustums.add(redFrustum);
        frustums.add(greenFrustum);
        frustums.add(blueFrustum);

        vtkPolyData footprint = eros.computeMultipleFrustumIntersection(frustums);

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

        for (int i=0; i<MSIImage.IMAGE_HEIGHT; ++i)
        {
            // Compute the vector on the left of the row.
            double fracHeight = ((double)i / (double)(MSIImage.IMAGE_HEIGHT-1));
            double[] left = {
                    corner1[0] + fracHeight*vec13[0],
                    corner1[1] + fracHeight*vec13[1],
                    corner1[2] + fracHeight*vec13[2]
            };

            for (int j=0; j<MSIImage.IMAGE_WIDTH; ++j)
            {
                double fracWidth = ((double)j / (double)(MSIImage.IMAGE_WIDTH-1));
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

                //cellLocator.IntersectWithLine(spacecraftPosition, lookPt, intersectPoints, intersectCells);
                double tol = 1e-6;
                double[] t = new double[1];
                double[] x = new double[3];
                double[] pcoords = new double[3];
                int[] subId = new int[1];
                int[] cellId = new int[1];
                int result = cellLocator.IntersectWithLine(spacecraftPosition, lookPt, tol, t, x, pcoords, subId, cellId, cell);

                if (result > 0)
                {
                    double[] greenUv = new double[2];
                    redFrustum.computeTextureCoordinates(x, greenUv);
                    double[] blueUv = new double[2];
                    redFrustum.computeTextureCoordinates(x, blueUv);
                }
            }
        }

    }

    @Override
    public ArrayList<vtkProp> getProps()
    {
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {

    }

}
