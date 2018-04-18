package edu.jhuapl.sbmt.gui.image;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.collect.Lists;

import vtk.vtkDataArray;
import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkUnsignedCharArray;

import edu.jhuapl.saavtk.util.VtkDataTypes;

public class ImageDataJPanel extends JPanel
{

    vtkImageData imageData;
    ImageIcon visibleSliceData;
    int visibleSliceIndex;
    JLabel paintingSurface=new JLabel();


    public ImageDataJPanel(vtkImageData imageData)
    {
        this.imageData=imageData;
    }

    PixelInterleavedSampleModel model;
    BufferedImage img;

    protected static List<ImageIcon> generateImageIcons(vtkImageData imageData)
    {
        List<ImageIcon> imageIcons=Lists.newArrayList();
        for (int i = 0; i < imageData.GetDimensions()[2]; i++)
        {
            vtkImageReslice slicer = new vtkImageReslice();
            slicer.SetInputData(imageData);
            slicer.SetOutputExtent(0, imageData.GetDimensions()[0] - 1, 0,
                    imageData.GetDimensions()[1] - 1, i, i);
            slicer.Update();

            vtkImageData imageDataSlice = slicer.GetOutput();
            vtkDataArray arr = imageDataSlice.GetCellData().GetScalars();

            switch (arr.GetDataType())
            {
            /*
             * case VtkDataTypes.VTK_DOUBLE:
             * buffer=Buffers.newDirectDoubleBuffer(((vtkDoubleArray)imageData.
             * GetCellData().GetScalars()).GetJavaArray()); break; case
             * VtkDataTypes.VTK_FLOAT:
             * buffer=Buffers.newDirectFloatBuffer(((vtkFloatArray)imageData.
             * GetCellData().GetScalars()).GetJavaArray()); break;
             */
            case VtkDataTypes.VTK_UNSIGNED_CHAR:
                byte[] data = ((vtkUnsignedCharArray) imageDataSlice.GetCellData()
                        .GetScalars()).GetJavaArray();
                SampleModel model = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,
                        imageDataSlice.GetDimensions()[0],
                        imageDataSlice.GetDimensions()[1], 4,
                        4 * imageDataSlice.GetDimensions()[0],
                        new int[] { 0, 1, 2 });
                BufferedImage img = new BufferedImage(imageDataSlice.GetDimensions()[0],
                        imageDataSlice.GetDimensions()[1],
                        BufferedImage.TYPE_3BYTE_BGR);
                img.setAccelerationPriority(1);

                Raster raster = Raster.createRaster(model,
                        new DataBufferByte(data, data.length), new Point());
                img.setData(raster);
                AffineTransform transform = AffineTransform.getScaleInstance(1,
                        -1);
                transform.translate(0, -img.getHeight());
                AffineTransformOp op = new AffineTransformOp(transform,
                        AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                img = op.filter(img, null);
                //paintingSurface.setSize(imageData.GetDimensions()[0],
                //        imageData.GetDimensions()[1]);
                //paintingSurface.setIcon(new ImageIcon(img));
                imageIcons.add(new ImageIcon(img));
                break;
            default:
                throw new Error("Unsupported array type #" + arr.GetDataType()
                        + "; cf. VtkDataTypes enum");
            }

        }
        return imageIcons;
    }


}
