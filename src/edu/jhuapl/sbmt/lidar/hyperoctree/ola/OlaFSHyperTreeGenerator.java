package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.nio.file.Path;

import edu.jhuapl.sbmt.core.io.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator;
import edu.jhuapl.sbmt.lidar.misc.RawLidarFile;
import edu.jhuapl.sbmt.query.hyperoctree.HyperBox;


public class OlaFSHyperTreeGenerator extends FSHyperTreeGenerator
{

    public OlaFSHyperTreeGenerator(Path outputDirectory,
            int maxNumberOfPointsPerLeaf, HyperBox bbox,
            int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        super(outputDirectory, maxNumberOfPointsPerLeaf, bbox,
                maxNumberOfOpenOutputFiles, pool);
        // TODO Auto-generated constructor stub
    }

    @Override
    public RawLidarFile openFile(Path file)
    {
        return new OlaLidarFile(file.toString());
    }

}
