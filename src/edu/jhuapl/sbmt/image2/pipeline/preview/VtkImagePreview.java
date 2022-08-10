package edu.jhuapl.sbmt.image2.pipeline.preview;

import java.io.IOException;
import java.util.List;

import vtk.vtkImageData;

import edu.jhuapl.sbmt.image2.ui.ImagePreviewPanel;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class VtkImagePreview implements IPipelineSubscriber<vtkImageData>
{
	private IPipelinePublisher<vtkImageData> publisher;
	private String title;

	public VtkImagePreview(String title)
	{
		this.title = title;
	}

	@Override
	public void receive(List<vtkImageData> items)
	{
		try
		{
			new ImagePreviewPanel(title, items.get(0));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receive(vtkImageData item) throws IOException, Exception
	{
		receive(List.of(item));
	}

	@Override
	public void setPublisher(IPipelinePublisher<vtkImageData> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public VtkImagePreview run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

//	@Override
//	public VtkImagePreview run(Runnable completion) throws IOException, Exception
//	{
//		publisher.run();
//		completion.run();
//		return this;
//	}
}
