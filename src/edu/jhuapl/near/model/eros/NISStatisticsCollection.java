package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.google.common.collect.Lists;

import vtk.vtkProp;

import edu.jhuapl.near.model.Model;

public class NISStatisticsCollection extends Model implements PropertyChangeListener
{
    List<vtkProp> props=Lists.newArrayList();
    List<NISStatistics> stats=Lists.newArrayList();

    public void addStatistics(NISStatistics stats)
    {
        this.stats.add(stats);
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
