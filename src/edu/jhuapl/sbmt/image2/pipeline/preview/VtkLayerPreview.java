package edu.jhuapl.sbmt.image2.pipeline.preview;

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

public class VtkLayerPreview implements IPipelineSubscriber<Pair<Layer, HashMap<String, String>>>
{
	private IPipelinePublisher<Pair<Layer, HashMap<String, String>>> publisher;
	private LayerPreviewPanel preview;
	private String title;
	private Runnable completionBlock;

	public VtkLayerPreview(String title)
	{
		this.title = title;
	}

	public VtkLayerPreview(String title, Runnable completionBlock)
	{
		this.title = title;
		this.completionBlock = completionBlock;
	}

	@Override
	public void receive(List<Pair<Layer, HashMap<String, String>>> items)
	{
		try
		{
			preview = new LayerPreviewPanel(title, items.get(0).getLeft(), items.get(0).getRight(), completionBlock);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receive(Pair<Layer, HashMap<String, String>> item) throws IOException, Exception
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
	public void setPublisher(IPipelinePublisher<Pair<Layer, HashMap<String, String>>> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public VtkLayerPreview run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

//	@Override
//	public VtkLayerPreview run(Runnable completion) throws IOException, Exception
//	{
//		publisher.run();
//		completion.run();
//		return this;
//	}

	public IntensityRange getIntensityRange()
	{
		if (preview == null) return new IntensityRange(0, 255);
		return preview.getIntensityRange();
	}

	public int[] getMaskValues()
	{
		if (preview == null) return new int[] {0, 0, 0, 0};
		return preview.getMaskValues();
	}
}
