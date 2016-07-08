package edu.jhuapl.near.lidar.hyperoctree;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import edu.jhuapl.near.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;

public class OlaFSHyperTreeQuerier
{
    OlaFSHyperTreeNodeReadOnly rootNode;
//    Path dataRootPath=Paths.get(Configuration.getDataRootURL());

    public OlaFSHyperTreeQuerier()
    {
        try
        {
            rootNode=new OlaFSHyperTreeNodeReadOnly("/GASKELL/RQ36_V3/OLA/hypertree/");   // root directory of cache
        }
        catch (HyperDimensionMismatchException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<String> getAllIntersectingCacheRelativePaths(HyperBox searchBox)
    {
        List<String> paths=Lists.newArrayList();
        try
        {
            search(searchBox,rootNode,paths);
        }
        catch (HyperException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return paths;
    }

    private void search(HyperBox sbox, OlaFSHyperTreeNodeReadOnly node, List<String> paths) throws HyperException
    {
        if (node.bbox.intersects(sbox))
        {
            paths.add(node.getCacheRelativePath());
            System.out.println(node.getCacheRelativePath());
            for (int i=0; i<node.getNumberOfChildren(); i++)
                if (node.childExists(i))
                    search(sbox,new OlaFSHyperTreeNodeReadOnly(node.getChildPath(i)),paths);
        }
    }

/*    public List<OlaFSHyperPoint> search(HyperBox sbox)   // find all points inside sbox
    {
        List<OlaFSHyperPoint> pointList=Lists.newArrayList();
        try
        {
            search(sbox,root,pointList);
        }
        catch (HyperException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pointList;
    }

    private void search(HyperBox sbox, OlaFSHyperTreeNodeReadOnly node, List<OlaFSHyperPoint> pts) throws HyperException
    {
        if (!node.bbox.intersects(sbox))
            return;
        else
            if (node.isNonEmptyLeaf())
            {
                Iterator<OlaFSHyperPoint> iter=node.iterator();
                while (iter.hasNext())
                {
                    OlaFSHyperPoint pt=iter.next();
                    if (sbox.contains(pt))
                        pts.add(pt);
                }
            }
            else
                for (int i=0; i<node.getNumberOfChildren(); i++)
                    if (node.childExists(i))
                        search(sbox,new OlaFSHyperTreeNodeReadOnly(node.getChildPath(i)),pts);

    }*/

    public class OlaFSHyperTreeNodeReadOnly implements Iterable<OlaFSHyperPoint>
    {
        String cacheRelativePath;
        HyperBox bbox;
        //
        int dimension=new OlaFSHyperPoint().getDimension();

        public OlaFSHyperTreeNodeReadOnly(String cacheRelativePath) throws HyperDimensionMismatchException
        {
            this.cacheRelativePath=cacheRelativePath;   // given by parent
            File f=FileCache.getFileFromServer(getBoundsFilePath());
            System.out.println(f.getAbsolutePath());
            double[] bounds=FSHyperTreeNode.readBoundsFile(f.toPath(), dimension);
            double[] min=new double[dimension];
            double[] max=new double[dimension];
            for (int i=0; i<dimension; i++)
            {
                min[i]=bounds[2*i+0];
                max[i]=bounds[2*i+1];
            }
            bbox=new HyperBox(min, max);
        }

        public boolean isNonEmptyLeaf()
        {
            //File f=FileCache.getFileFromServer(getDataFilePath());
            return false;//f.exists(); // if the "data" file exists then this is a non empty leaf
        }

        public boolean childExists(int i)
        {
            String childBoundsFile=getChildPath(i)+"/bounds";
            try
            {
                URL url=new URL(Configuration.getDataRootURL()+"/"+childBoundsFile);
                url.openStream();
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
                return false;
            }
            catch (IOException e)
            {
                return false;
            }
            return true;
        }

        public int getNumberOfChildren()
        {
            return (int)Math.pow(2, bbox.getDimension());
        }

        public String getCacheRelativePath()
        {
            return cacheRelativePath;
        }

        public String getChildPath(int i)
        {
            return cacheRelativePath+"/"+String.valueOf(i);
        }

        public String getDataFilePath()
        {
            return cacheRelativePath+"/data";
        }

        public String getBoundsFilePath()
        {
            return cacheRelativePath+"/bounds";
        }

        public boolean contains(HyperPoint pt) throws HyperException
        {
            return bbox.contains(pt);
        }

        public boolean intersects(HyperBox otherBox) throws HyperException
        {
            return bbox.intersects(otherBox);
        }

        @Override
        public Iterator<OlaFSHyperPoint> iterator()
        {
            return new Iterator<OlaFSHyperPoint>()
            {
                DataInputStream instream=null;
                OlaFSHyperPoint pt=null;

                @Override
                public boolean hasNext()
                {
                    try
                    {
                        if (instream==null)
                        {
                            File f=FileCache.getFileFromServer(getDataFilePath());
                            instream=new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
                        }
                        pt=new OlaFSHyperPoint(instream);
                        return true;
                    }
                    catch (IOException e)
                    {
                        pt=null;
                        return false;
                    }
                }

                @Override
                public OlaFSHyperPoint next()
                {
                    return pt;
                }
            };
        }

    }

    public static void main(String[] args)
    {
        OlaFSHyperTreeQuerier querier=new OlaFSHyperTreeQuerier();
        long elapsed=0;
//        for (int i=0; i<100; i++)
 //       {
            Stopwatch sw=new Stopwatch();
            sw.start();
            List<String> paths=querier.getAllIntersectingCacheRelativePaths(querier.rootNode.bbox);
//            List<OlaFSHyperPoint> result=querier.search(querier.rootNode.bbox);
            //System.out.println(result.size());
            long ms=sw.elapsedMillis();
            elapsed+=ms;
            //System.out.println(ms+" ms elapsed, avg="+elapsed/(i+1)+" ms, heap="+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024/1024+" MB");
            System.gc();
        }
   // }

}
