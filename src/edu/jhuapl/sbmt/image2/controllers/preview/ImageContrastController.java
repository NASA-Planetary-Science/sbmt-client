package edu.jhuapl.sbmt.image2.controllers.preview;

import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

import com.jidesoft.swing.RangeSlider;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.rendering.vtk.VtkImageContrastPipeline;

/**
 * Controller to help govern the contrast values (via <pre>IntensityRange</pre> for a <pre>vtkImageData</pre> object.
 *
 * Consists of a <pre>ImageContrastSlider</pre> (private class in this file) to draw the UI element and change the image data accordingly.
 *
 * @author steelrj1
 *
 */
public class ImageContrastController
{
	ImageContrastSlider slider;
	JLabel label;

	public ImageContrastController(vtkImageData imageData, IntensityRange intensityRange, Function<vtkImageData, Void> completionBlock)
	{
		slider = new ImageContrastSlider(imageData, intensityRange, completionBlock);
		label = new JLabel("Contrast");
	}

	public JPanel getView()
	{
		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(slider);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		return panel;
	}

	public void setImageData(vtkImageData imageData)
	{
		slider.setImageData(imageData);
	}

	public int getLowValue()
	{
		return slider.getLowValue();
	}

	public int getHighValue()
	{
		return slider.getHighValue();
	}

	public IntensityRange getIntensityRange()
	{
		return new IntensityRange(getLowValue(), getHighValue());
	}
}

class ImageContrastSlider extends RangeSlider
{
	private VtkImageContrastPipeline pipeline;
	private IntensityRange intensityRange;
	private Function<vtkImageData, Void> completionBlock;
	private vtkImageData imageData;

	public ImageContrastSlider(vtkImageData imageData, IntensityRange intensityRange, Function<vtkImageData, Void> completionBlock)
	{
		this.completionBlock = completionBlock;
		this.imageData = imageData;
		setMinimum(0);
		setMaximum(255);
		int lowValue = 0;
		int hiValue = 255;
		lowValue = intensityRange.min;
		hiValue  = intensityRange.max;
		this.setHighValue(hiValue);
		this.setLowValue(lowValue);
		setPaintTicks(true);
		setMajorTickSpacing(10);
		addChangeListener(evt -> {
           sliderStateChanged(evt);
        });
	}

	public void setImageData(vtkImageData imageData)
	{
		this.imageData = imageData;
	}

	public void applyContrastToImage()
	{
		try
		{
			intensityRange = new IntensityRange(getLowValue(), getHighValue());
			pipeline = new VtkImageContrastPipeline(imageData, intensityRange);
			completionBlock.apply(pipeline.getUpdatedData().get(0));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sliderStateChanged(ChangeEvent evt)
	{
		if (getValueIsAdjusting())
			return;

		applyContrastToImage();
	}
}