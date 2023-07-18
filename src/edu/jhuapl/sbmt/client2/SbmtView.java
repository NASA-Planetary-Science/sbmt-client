package edu.jhuapl.sbmt.client2;

import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.sbmt.config.BasicConfigInfo;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.BaseView;
import edu.jhuapl.sbmt.model.lineament.LineamentModel;

/**
 * A view is a container which contains a control panel and renderer as well as
 * a collection of managers. A view is unique to a specific body. This class is
 * used to build all built-in and custom views. All the configuration details of
 * all the built-in and custom views are contained in this class.
 */
public class SbmtView extends BaseView //View implements PropertyChangeListener
{
	public SbmtView(StatusNotifier aStatusNotifier, BasicConfigInfo configInfo)
	{
		super(aStatusNotifier, configInfo);
	}

    /**
	 * By default a view should be created empty. Only when the user requests to
	 * show a particular View, should the View's contents be created in order to
	 * reduce memory and startup time. Therefore, this function should be called
	 * prior to first time the View is shown in order to cause it
     */
	public SbmtView(StatusNotifier aStatusNotifier, SmallBodyViewConfig smallBodyConfig)
    {
		super(aStatusNotifier, smallBodyConfig);
    }

    @Override
    protected void setupTabs()
    {
		setupModelTab();
        setupNormalImagingTabs();
        setupSpectrumTabs();
		setupLidarTabs();
		setupLineamentTab();
		setupStructuresTab();
		setupCustomDataTab();
		setupDEMTab();
    	setupStateHistoryTab();
    }

	static public LineamentModel createLineament()
    {
        return new LineamentModel();
    }

	public BasicConfigInfo getConfigInfo()
	{
		return configInfo;
	}
}