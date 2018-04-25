package edu.jhuapl.sbmt.gui.lidar.v2;

import java.util.Date;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.BodyViewConfig;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;

public class LidarSearchModel
{
    protected final ModelManager modelManager;
    protected PickManager pickManager;
    protected LidarSearchDataCollection lidarModel;
    private java.util.Date startDate = null;
    private java.util.Date endDate = null;
    protected BodyViewConfig smallBodyConfig;


    public LidarSearchModel(BodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            final PickManager pickManager,
            Renderer renderer,
            ModelNames lidarModelName)
    {
        this.modelManager = modelManager;
        this.pickManager = pickManager;
        this.smallBodyConfig=smallBodyConfig;
        this.lidarModel = (LidarSearchDataCollection)modelManager.getModel(lidarModelName);
    }

    public ModelManager getModelManager()
    {
        return modelManager;
    }


    public PickManager getPickManager()
    {
        return pickManager;
    }


    public Date getStartDate()
    {
        return startDate;
    }


    public Date getEndDate()
    {
        return endDate;
    }


    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }


    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }


    public BodyViewConfig getSmallBodyConfig()
    {
        return smallBodyConfig;
    }

    public LidarSearchDataCollection getLidarModel()
    {
        return lidarModel;
    }
}
