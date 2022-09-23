package edu.jhuapl.sbmt.image2.pipelineComponents.operators.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.core.image.IImagingInstrument;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image2.model.ImageOrigin;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class CreateImageFromSavedListOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<Triple<List<List<String>>, ImagingInstrument, List<String>>, G1>
{
	@Override
	public void processData() throws IOException, Exception
	{
		List<List<String>> results = inputs.get(0).getLeft();
		IImagingInstrument instrument = inputs.get(0).getMiddle();
		List<String> infoBaseNames = inputs.get(0).getRight();
		outputs = new ArrayList<G1>();

		int i=1;
        for (List<String> imageInfo : results)
        {
        	ImageSource imageSource = ImageSource.valueFor(imageInfo.get(2).replace("_", " "));
        	PerspectiveImage image = new PerspectiveImage(imageInfo.get(0), instrument.getType(), imageSource, infoBaseNames.get(results.indexOf(imageInfo)), new double[] {});
        	image.setFlip(instrument.getFlip());
        	image.setRotation(instrument.getRotation());
        	image.setImageOrigin(ImageOrigin.SERVER);
        	image.setLinearInterpolatorDims(instrument.getLinearInterpolationDims());
        	image.setMaskValues(instrument.getMaskValues());
        	image.setFillValues(instrument.getFillValues());
        	image.setLongTime(Long.parseLong(imageInfo.get(1)));
        	CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
        	compImage.setIndex(i++);
        	outputs.add((G1)compImage);
        }
	}
}
