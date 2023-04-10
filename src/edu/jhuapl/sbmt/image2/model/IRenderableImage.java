package edu.jhuapl.sbmt.image2.model;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.layer.api.Layer;

public interface IRenderableImage
{

	/**
	 * @return the layer
	 */
	Layer getLayer();

	LayerMasking getMasking();

	IntensityRange getIntensityRange();

	/**
	 * @return the isLinearInterpolation
	 */
	boolean isLinearInterpolation();

	public CylindricalBounds getBounds();

	public double getOffset();

	public PointingFileReader getPointing();

	public String getFilename();

	public void setFilename(String filename);

}