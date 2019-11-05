package edu.jhuapl.sbmt.spectrum.model.statistics;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.google.common.collect.Lists;

import vtk.vtkProp;

import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

/**
 * Generates a series of spectrum statistics for display in a popup
 * @author steelrj1
 *
 */
public class SpectrumStatisticsCollection<S extends BasicSpectrum> extends AbstractModel implements PropertyChangeListener
{
    List<vtkProp> props=Lists.newArrayList();
    List<SpectrumStatistics<S>> stats=Lists.newArrayList();

    public void addStatistics(SpectrumStatistics<S> stats)
    {
        this.stats.add(stats);
    }

    public enum SpectrumOrdering
    {
        TH_MEAN,TH_VARIANCE,TH_SKEWNESS,TH_KURTOSIS;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {

    }

    @Override
    public List<vtkProp> getProps()
    {
        return props;
    }
}
