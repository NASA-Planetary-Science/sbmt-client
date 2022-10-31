package edu.jhuapl.sbmt.image2.pipelineComponents.subscribers.preview;

import java.awt.Container;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image2.ui.LayerPreviewPanel;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class VtkLayerPreview implements IPipelineSubscriber<Pair<Layer, List<HashMap<String, String>>>>
{
	private IPipelinePublisher<Pair<Layer, List<HashMap<String, String>>>> publisher;
	private LayerPreviewPanel preview;
	private String title;
	private Runnable completionBlock;
	private int currentLayerIndex;
	private IntensityRange currentIntensityRange;
	private int[] currentMaskValues;

	public VtkLayerPreview(String title, int currentLayerIndex, IntensityRange currentIntensityRange, int[] maskValues)
	{
		this.title = title;
		this.currentLayerIndex = currentLayerIndex;
		this.currentIntensityRange = currentIntensityRange;
		this.currentMaskValues = maskValues;
	}

	public VtkLayerPreview(String title, int currentLayerIndex, IntensityRange currentIntensityRange, int[] currentMaskValues, Runnable completionBlock)
	{
		this(title, currentLayerIndex, currentIntensityRange, currentMaskValues);
		this.completionBlock = completionBlock;
	}

	@Override
	public void receive(List<Pair<Layer, List<HashMap<String, String>>>> items)
	{
		try
		{
			List<Layer> layers = items.stream().map( item -> item.getLeft()).toList();
			List<List<HashMap<String, String>>> metadata = items.stream().map( item -> item.getRight()).toList();
			preview = new LayerPreviewPanel(title, layers, currentLayerIndex, currentIntensityRange, currentMaskValues, metadata, completionBlock);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receive(Pair<Layer, List<HashMap<String, String>>> item) throws IOException, Exception
	{
		receive(List.of(item));
	}

	public void setCompletionBlock(Runnable completionBlock)
	{
		this.completionBlock = completionBlock;
	}

	public Container getPanel()
	{
		return preview.getContentPane();
	}

	@Override
	public void setPublisher(IPipelinePublisher<Pair<Layer, List<HashMap<String, String>>>> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public VtkLayerPreview run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

	public IntensityRange getIntensityRange()
	{
		if (preview == null) return currentIntensityRange;
		return preview.getIntensityRange();
	}

	public int[] getMaskValues()
	{
		if (preview == null) return currentMaskValues;
		return preview.getMaskValues();
	}

	public int getDisplayedLayerIndex()
	{
		if (preview == null) return currentLayerIndex;
		return preview.getDisplayedLayerIndex();
	}
}
