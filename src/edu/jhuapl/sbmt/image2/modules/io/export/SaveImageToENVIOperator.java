package edu.jhuapl.sbmt.image2.modules.io.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;

import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;

//TODO This can eventually get replaced with a GDAL call, since it handles ENVI
public class SaveImageToENVIOperator extends BasePipelineOperator<PerspectiveImage, File>
{
	private PerspectiveImage image;
	private int imageDepth;
	private final String filename;

	public SaveImageToENVIOperator(String filename)
	{
		this.filename = filename;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		image = inputs.get(0);
		PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(image));
		RenderablePointedImage renderable = pipeline.getRenderableImages().get(0);
		String enviFilename = image.getFilename();
		String interleaveType = "bsq";
		boolean hostByteOrder = true;
		imageDepth = image.getNumberOfLayers();
		int imageWidth = renderable.getImageWidth();
		int imageHeight = renderable.getImageHeight();

		switch (interleaveType)
		{
			case "bsq":
			case "bil":
			case "bip":
				break;
			default:
				System.out.println("Interleave type " + interleaveType + " unrecognized, aborting exportAsEnvi()");
				return;
		}

		// Create output stream for header (.hdr) file
		FileOutputStream fs = null;
		try
		{
			fs = new FileOutputStream(enviFilename + ".hdr");
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}
		OutputStreamWriter osw = new OutputStreamWriter(fs);
		BufferedWriter out = new BufferedWriter(osw);

		// Write the fields of the header
		out.write("ENVI\n");
		out.write("samples = " + imageWidth + "\n");
		out.write("lines = " + imageHeight + "\n");
		out.write("bands = " + getImageDepth() + "\n");
		out.write("header offset = " + "0" + "\n");
		out.write("data type = " + "4" + "\n"); // 1 = byte, 2 = int, 3 = signed
												// int, 4 = float
		out.write("interleave = " + interleaveType + "\n"); // bsq = band
															// sequential, bil =
															// band interleaved
															// by line, bip =
															// band interleaved
															// by pixel
		out.write("byte order = "); // 0 = host(intel, LSB first), 1 = network
									// (IEEE, MSB first)
		if (hostByteOrder)
		{
			// Host byte order
			out.write("0" + "\n");
		} else
		{
			// Network byte order
			out.write("1" + "\n");
		}
		//TODO fix this
//		out.write(getEnviHeaderAppend());
		out.close();

		// Configure byte buffer & endianess
		ByteBuffer bb = ByteBuffer.allocate(4 * imageWidth * imageHeight * getImageDepth()); // 4
																								// bytes
																								// per
																								// float
		if (hostByteOrder)
		{
			// Little Endian = LSB stored first
			bb.order(ByteOrder.LITTLE_ENDIAN);
		} else
		{
			// Big Endian = MSB stored first
			bb.order(ByteOrder.BIG_ENDIAN);
		}

		// Write pixels to byte buffer
		// Remember, VTK origin is at bottom left while ENVI origin is at top
		// left
		//TODO FIX THIS
		float[][][] imageData = null;
//		float[][][] imageData = ImageDataUtil.vtkImageDataToArray3D(getRawImage());
		switch (interleaveType)
		{
		case "bsq":
			// Band sequential: col, then row, then depth
			for (int depth = 0; depth < getImageDepth(); depth++)
			{
				// for(int row = imageHeight-1; row >= 0; row--)
				for (int row = 0; row < imageHeight; row++)
				{
					for (int col = 0; col < imageWidth; col++)
					{
						bb.putFloat(imageData[depth][row][col]);
					}
				}
			}
			break;
		case "bil":
			// Band interleaved by line: col, then depth, then row
			// for(int row=imageHeight-1; row >= 0; row--)
			for (int row = 0; row < imageHeight; row++)
			{
				for (int depth = 0; depth < getImageDepth(); depth++)
				{
					for (int col = 0; col < imageWidth; col++)
					{
						bb.putFloat(imageData[depth][row][col]);
					}
				}
			}
			break;
		case "bip":
			// Band interleaved by pixel: depth, then col, then row
			// for(int row=imageHeight-1; row >= 0; row--)
			for (int row = 0; row < imageHeight; row++)
			{
				for (int col = 0; col < imageWidth; col++)
				{
					for (int depth = 0; depth < getImageDepth(); depth++)
					{
						bb.putFloat(imageData[depth][row][col]);
					}
				}
			}
			break;
		}

		// Create output stream and write contents of byte buffer
		try (FileOutputStream stream = new FileOutputStream(enviFilename))
		{
			FileChannel fc = stream.getChannel();
			bb.flip(); // flip() is a misleading name, nothing is being flipped.
						// Buffer end is set to
						// curr pos and curr pos set to beginning.
			fc.write(bb);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public int getImageDepth()
	{
		return imageDepth;
	}
}
