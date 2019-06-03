package edu.jhuapl.sbmt.gui.lidar;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.model.lidar.LidarQueryUtil.DataType;
import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;

import net.miginfocom.swing.MigLayout;

public class LidarPanel extends JTabbedPane
{
	public LidarPanel(SmallBodyViewConfig aBodyViewConfig, ModelManager aModelManager, PickManager aPickManager,
			Renderer aRenderer)
	{
		JPanel browsePanel = formBrowsePanel(aBodyViewConfig, aModelManager, aPickManager);
		JPanel searchPanel = formSearchPanel(aBodyViewConfig, aModelManager, aPickManager, aRenderer);

		addTab("Browse", browsePanel);
		if (searchPanel != null)
			addTab("Search", searchPanel);
	}

	/**
	 * Helper utility method that forms the 'browse' tabbed panel.
	 */
	private static JPanel formBrowsePanel(SmallBodyViewConfig aBodyViewConfig, ModelManager aModelManager,
			PickManager aPickManager)
	{
		// Determine the proper data source name
		Instrument tmpInstrument = aBodyViewConfig.lidarInstrumentName;
		String dataSourceName = null;
		if (tmpInstrument == Instrument.LASER)
			dataSourceName = "Hayabusa2";
		else if (tmpInstrument == Instrument.OLA)
			dataSourceName = "Default";

		LidarFileSpecManager tmpFileSpecManager = (LidarFileSpecManager) aModelManager.getModel(ModelNames.LIDAR_BROWSE);
		LidarFileSpecPanel retPanel = new LidarFileSpecPanel(tmpFileSpecManager, aBodyViewConfig, dataSourceName);
		return retPanel;
	}

	/**
	 * Helper utility method that forms the 'search' tabbed panel.
	 */
	private static JPanel formSearchPanel(SmallBodyViewConfig aBodyViewConfig, ModelManager aModelManager,
			PickManager aPickManager, Renderer aRenderer)
	{
		// MOLA search isn't working, so disable it for now.
		if (aBodyViewConfig.lidarInstrumentName.equals(Instrument.MOLA))
			return null;

		boolean hasHyperTreeSearch = aBodyViewConfig.hasHypertreeBasedLidarSearch;
		Instrument tmpInstrument = aBodyViewConfig.lidarInstrumentName;
		LidarTrackManager tmpTrackManager;

		// Form the appropriate 'search' panel
		LidarSearchPanel searchPanel;
		if (tmpInstrument == Instrument.MOLA && hasHyperTreeSearch == true)
		{
			tmpTrackManager = (LidarTrackManager) aModelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
			searchPanel = new LidarHyperTreeSearchPanel(aBodyViewConfig, aModelManager, aPickManager, tmpTrackManager,
					DataType.Mola);
		}
		else if (tmpInstrument == Instrument.LASER && hasHyperTreeSearch)
		{
			tmpTrackManager = (LidarTrackManager) aModelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
			searchPanel = new LidarHyperTreeSearchPanel(aBodyViewConfig, aModelManager, aPickManager, tmpTrackManager,
					DataType.Hayabusa);
		}
		else if (tmpInstrument == Instrument.OLA && hasHyperTreeSearch == true)
		{
			tmpTrackManager = (LidarTrackManager) aModelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
			searchPanel = new LidarHyperTreeSearchPanel(aBodyViewConfig, aModelManager, aPickManager, tmpTrackManager,
					DataType.Ola);
		}
		else
		{
			tmpTrackManager = (LidarTrackManager) aModelManager.getModel(ModelNames.LIDAR_SEARCH);
			searchPanel = new LidarSearchPanel(aBodyViewConfig, aModelManager, aPickManager, tmpTrackManager);
		}

		// Form the 'list' panel
		JPanel trackPanel = new LidarTrackPanel(aModelManager, tmpTrackManager, aPickManager, aRenderer);

		JPanel retPanel = new JPanel(new MigLayout("", "0[]0", "0[]0"));
		retPanel.add(searchPanel, "growx,span,wrap");
		retPanel.add(trackPanel, "growx,growy,pushx,pushy");

		return retPanel;
	}
}
