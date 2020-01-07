package edu.jhuapl.sbmt.stateHistory.model.animator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.jcodec.api.awt.AWTSequenceEncoder;

public class MovieGenerator
{
    public static void create(List<String> frames, File outputFile, int width, int height) throws IOException
    {
		AWTSequenceEncoder enc = AWTSequenceEncoder.create30Fps(outputFile);
		for (String filename : frames)
		{
			BufferedImage image = ImageIO.read(new File(filename));
			enc.encodeImage(image);
		}
		enc.finish();
    }
}