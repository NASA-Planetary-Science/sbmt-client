package edu.jhuapl.sbmt.stateHistory.model.interfaces;

/**
 * @author steelrj1
 *
 */
public interface HasTime
{
    /**
     * @return
     */
    public Double getPeriod();

    /**
     * @return
     */
    public Double getTimeFraction();

    /**
     * @param state
     * @param timeFraction
     */
    public void setTimeFraction(Double timeFraction);
}
