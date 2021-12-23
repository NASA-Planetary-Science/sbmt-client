package edu.jhuapl.sbmt.image2.ui.table.popup.export;

import java.io.File;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.modules.io.export.SaveImagePointingFileFromCacheOperator;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

import glum.gui.action.PopAction;

public class ExportInfofileAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	/**
	 *
	 */
	private PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	public ExportInfofileAction(PerspectiveImageCollection aManager)
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
				.operate(new SaveImagePointingFileFromCacheOperator())
				.subscribe(Sink.of(files))
				.run();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}