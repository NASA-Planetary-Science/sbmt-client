package edu.jhuapl.sbmt.query.database;

import java.util.List;
import java.util.TreeSet;

import org.joda.time.DateTime;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.SettableMetadata;

public class SpectraDatabaseSearchMetadata extends DatabaseSearchMetadata
{
    public static final Key<TreeSet<Integer>> CUBE_LIST = Key.of("Cube List");

    protected SpectraDatabaseSearchMetadata(FixedMetadata metadata)
    {
        super(metadata);
        // TODO Auto-generated constructor stub
    }

    public static SpectraDatabaseSearchMetadata of(String name, DateTime startDate, DateTime stopDate,
            Range<Double> distanceRange, String searchString,
            List<Integer> polygonTypes, Range<Double> incidenceRange,
            Range<Double> emissionRange, Range<Double> phaseRange, TreeSet<Integer> cubeList)
    {
        FixedMetadata metadata = FixedMetadata.of(createSettableMetadata(name, startDate, stopDate, distanceRange, searchString,
                                                polygonTypes, incidenceRange, emissionRange, phaseRange, cubeList));
        return new SpectraDatabaseSearchMetadata(metadata);
    }

    protected static SettableMetadata createSettableMetadata(String name, DateTime startDate, DateTime stopDate,
            Range<Double> distanceRange, String searchString,
            List<Integer> polygonTypes, Range<Double> incidenceRange,
            Range<Double> emissionRange, Range<Double> phaseRange, TreeSet<Integer> cubeList)
    {
        SettableMetadata metadata = createSettableMetadata(name, startDate, stopDate, distanceRange, searchString, polygonTypes, incidenceRange, emissionRange, phaseRange);
        metadata.put(CUBE_LIST, cubeList);
        return metadata;
    }

}
