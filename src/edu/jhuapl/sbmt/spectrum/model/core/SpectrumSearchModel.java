package edu.jhuapl.sbmt.spectrum.model.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.IdPair;

public class SpectrumSearchModel
{

    protected final ModelManager modelManager;
    protected final PickManager pickManager;
    protected Date startDate = new GregorianCalendar(2000, 0, 11, 0, 0, 0).getTime();
    protected Date endDate = new GregorianCalendar(2000, 4, 14, 0, 0, 0).getTime();
    protected List<String> spectrumRawResults = new ArrayList<String>();
    protected IdPair resultIntervalCurrentlyShown = null;
//    ISmallBodyViewConfig smallBodyConfig;
    protected Renderer renderer;
    protected boolean currentlyEditingUserDefinedFunction = false;


    public SpectrumSearchModel(/*ISmallBodyViewConfig smallBodyConfig,*/ final ModelManager modelManager,
            final PickManager pickManager, final Renderer renderer)
    {
//        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.pickManager = pickManager;
        this.renderer = renderer;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public List<String> getSpectrumRawResults()
    {
        return spectrumRawResults;
    }

    public void setSpectrumRawResults(List<String> spectrumRawResults)
    {
        this.spectrumRawResults = spectrumRawResults;
    }

    public IdPair getResultIntervalCurrentlyShown()
    {
        return resultIntervalCurrentlyShown;
    }

    public void setResultIntervalCurrentlyShown(IdPair resultIntervalCurrentlyShown)
    {
        this.resultIntervalCurrentlyShown = resultIntervalCurrentlyShown;
    }

    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public PickManager getPickManager()
    {
        return pickManager;
    }
//
//    public ISmallBodyViewConfig getSmallBodyConfig()
//    {
//        return smallBodyConfig;
//    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    public boolean isCurrentlyEditingUserDefinedFunction()
    {
        return currentlyEditingUserDefinedFunction;
    }

    public void setCurrentlyEditingUserDefinedFunction(
            boolean currentlyEditingUserDefinedFunction)
    {
        this.currentlyEditingUserDefinedFunction = currentlyEditingUserDefinedFunction;
    }

}
