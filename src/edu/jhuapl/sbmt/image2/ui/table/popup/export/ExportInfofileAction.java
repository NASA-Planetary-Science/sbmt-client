package edu.jhuapl.sbmt.image2.ui.table.popup.export;

import java.io.File;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.io.export.SaveImagePointingFileFromCacheOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

import glum.gui.action.PopAction;

public class ExportInfofileAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	/**
	 *
	 */
	private PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public ExportInfofileAction(PerspectiveImageCollection<G1> aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		G1 aItem = (G1)aManager.getSelectedItems().asList().get(0);
		List<File> files = Lists.newArrayList();
		try
		{
			Just.of(aItem)
				.operate(new SaveImagePointingFileFromCacheOperator<G1>())
				.subscribe(Sink.of(files))
				.run();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}