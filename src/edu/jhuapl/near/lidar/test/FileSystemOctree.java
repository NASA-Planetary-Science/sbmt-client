package edu.jhuapl.near.lidar.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class FileSystemOctree
{

    final Path outputDirectory;
    final int maxNumberOfPointsPerLeaf;
    final BoundingBox boundingBox;
    Node root;

    public FileSystemOctree(Path outputDirectory, int maxNumberOfPointsPerLeaf, BoundingBox bbox) throws IOException
    {
        this.outputDirectory=outputDirectory;
        this.maxNumberOfPointsPerLeaf=maxNumberOfPointsPerLeaf;
        this.boundingBox=bbox;
        this.root=new Node(outputDirectory, bbox);
    }

    public void addPointsFromFile(Path inputFilePath) throws IOException {
        OlaPointList pointList=new OlaPointList();
        pointList.appendFromPath(inputFilePath);
        for (int i=0; i<pointList.getNumberOfPoints(); i++) {
            if ((i%20000)==0)
                System.out.println((double)i/(double)pointList.getNumberOfPoints()*100+"%");
            root.addPoint(new OlaOctreePoint(pointList.getPoint(i)));
        }
    }

    interface OctreePoint {
        public Vector3D getPosition();
        public void writeToStream(DataOutputStream stream) throws IOException;
        public void readFromStream(DataInputStream stream) throws IOException;
    }

    class OlaOctreePoint extends OlaPoint implements OctreePoint {

        boolean fullyRead=false;

        public OlaOctreePoint(DataInputStream stream)
        {
            super(null, null, 0, 0);
            try
            {
                readFromStream(stream);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public OlaOctreePoint(OlaPoint point)
        {
            super(point.scpos, point.tgpos, point.time, point.intensity);
        }

        @Override
        public Vector3D getPosition()
        {
            return tgpos;
        }

        @Override
        public void writeToStream(DataOutputStream stream) throws IOException
        {
            stream.writeDouble(time);
            stream.writeDouble(tgpos.getX());
            stream.writeDouble(tgpos.getY());
            stream.writeDouble(tgpos.getZ());
            stream.writeDouble(intensity);
            stream.writeDouble(scpos.getX());
            stream.writeDouble(scpos.getY());
            stream.writeDouble(scpos.getZ());
        }

        @Override
        public void readFromStream(DataInputStream stream) throws IOException
        {
            try {
                time=stream.readDouble();
                tgpos=new Vector3D(stream.readDouble(),stream.readDouble(),stream.readDouble());
                intensity=stream.readDouble();
                scpos=new Vector3D(stream.readDouble(),stream.readDouble(),stream.readDouble());
            } catch (EOFException e){
                return;
            }
            fullyRead=true;
        }

        public boolean isFullyRead() {
            return fullyRead;
        }

    }

    enum SplitDirection {
        X,Y,Z;
    }

    static BoundingBox createBoundingBox(Node parent, SplitDirection splitDir, double val, boolean isLeft) {
        BoundingBox bbox=new BoundingBox(parent.getBounds());
        if (isLeft) {
            if (splitDir.equals(SplitDirection.X))
                bbox.xmax=val;
            else if (splitDir.equals(SplitDirection.Y))
                bbox.ymax=val;
            else if (splitDir.equals(SplitDirection.Z))
                bbox.zmax=val;
        } else {
            if (splitDir.equals(SplitDirection.X))
                bbox.xmin=val;
            else if (splitDir.equals(SplitDirection.Y))
                bbox.ymin=val;
            else if (splitDir.equals(SplitDirection.Z))
                bbox.zmin=val;
        }
        //System.out.println(splitDir+" "+bbox.xmin+" "+bbox.xmax+" "+bbox.ymin+" "+bbox.ymax+" "+bbox.zmin+" "+bbox.zmax+" "+parent.xmin+" "+parent.xmax+" "+parent.ymin+" "+parent.ymax+" "+parent.zmin+" "+parent.zmax);
        return bbox;
    }

    static SplitDirection getNextSplitDirection(SplitDirection dir) {
        int ndir=SplitDirection.values().length;
        int ord=dir.ordinal();
        return SplitDirection.values()[(ord+1)%ndir];
    }

    class Node extends BoundingBox {
        final Path selfPath;
        boolean isLeaf=true;
        Node[] children=new Node[]{null,null};
        int numPoints=0;
        SplitDirection splitDir;
        DataOutputStream dataOutput;
        boolean isLeft=false;

        public double getVolume() {
            return (xmax-xmin)*(ymax-ymin)*(zmax-zmin);
        }

        public Node(Path rootPath, BoundingBox bbox) throws IOException
        {
            super(bbox.getBounds());
            splitDir=SplitDirection.X;
            selfPath=rootPath.resolve(splitDir.name()+'0');
            getSelfPath().toFile().mkdir();
            writeBounds();
            openDataFileForOutput();
        }

        Node(Node parent, double val, boolean isLeft) throws IOException
        {
            super(createBoundingBox(parent, parent.splitDir, val, isLeft).getBounds());
            splitDir=getNextSplitDirection(parent.splitDir);
            this.isLeft=isLeft;
//            System.out.println(getVolume()/parent.getVolume());
            //
            selfPath=parent.getSelfPath().resolve(splitDir.name()+(isLeft?'P':'M'));
            getSelfPath().toFile().mkdir();
            writeBounds();
            openDataFileForOutput();
            //
            System.out.println(getSelfPath());
        }

        boolean isInside(OctreePoint point) {
            Vector3D vec=point.getPosition();
            return contains(new double[]{vec.getX(),vec.getY(),vec.getZ()});
        }

        boolean addPoint(OctreePoint point) throws IOException {
            if (!isLeaf) {
                if (children[0].addPoint(point))
                    return true;
                else
                    return children[1].addPoint(point);
            } else {
                if (isInside(point)) {
                    point.writeToStream(dataOutput);
                    numPoints++;
                    if (numPoints>maxNumberOfPointsPerLeaf)
                        split();
                    return true;
                }
            }
            return false;
        }

        private Path getSelfPath() {
            return selfPath;
        }

        private Path getBoundsFilePath() {
            return getSelfPath().resolve("bounds");
        }

        private void writeBounds() throws IOException {
            //System.out.println(getBoundsFilePath());
            DataOutputStream stream=new DataOutputStream(new FileOutputStream(getBoundsFilePath().toFile()));
            stream.writeDouble(xmin);
            stream.writeDouble(xmax);
            stream.writeDouble(ymin);
            stream.writeDouble(ymax);
            stream.writeDouble(zmin);
            stream.writeDouble(zmax);
            stream.close();
        }

/*        private void gatherPointsFromParent(Path parentDataFilePath) throws IOException {
            DataInputStream parentStream=new DataInputStream(new FileInputStream(parentDataFilePath.toFile()));
            try
            {
                while (parentStream.skipBytes(0)==0) {    // this is a dangerous trick to keep the loop running until all data has been read (0 bytes will always be skipped here); we expect a EOFException to be thrown when the end of the file is encountered
                    OlaKdPoint pt=new OlaKdPoint(parentStream);
                    if (!pt.isFullyRead())
                        break;
                    if (isInside(pt)) {
                        pt.writeToStream(dataOutput);
                        numPoints++;
                    }
                }
                parentStream.close();
            } catch (EOFException e) {
            }
            System.out.println("   "+getSelfPath()+" "+numPoints);
        }*/

        private void openDataFileForOutput() throws FileNotFoundException {
            dataOutput=new DataOutputStream(new FileOutputStream(getDataFilePath().toFile()));
        }

        private void closeDataFileForOutput() throws IOException {
            dataOutput.close();
        }

        private Path getDataFilePath() {
            return getSelfPath().resolve("data");
        }

        void split() throws IOException {
            List<Double> coords=Lists.newArrayList();
            closeDataFileForOutput();
            DataInputStream selfStream=new DataInputStream(new FileInputStream(getDataFilePath().toFile()));
            while (selfStream.skipBytes(0)==0) {  // dirty trick to keep reading until EOF
                OlaOctreePoint pt=new OlaOctreePoint(selfStream);
                if (!pt.isFullyRead())
                    break;
                if (splitDir.equals(SplitDirection.X))
                    coords.add(pt.getPosition().getX());
                else if (splitDir.equals(SplitDirection.Y))
                    coords.add(pt.getPosition().getY());
                 else if (splitDir.equals(SplitDirection.Z))
                    coords.add(pt.getPosition().getZ());
            }
            selfStream.close();
            //
            double[] array=new double[coords.size()];
            for (int i=0; i<array.length; i++)
                array[i]=coords.get(i);
            double median=new Median().evaluate(array);
            children[0]=new Node(this, median, true);
            children[1]=new Node(this, median, false);
            //
            selfStream=new DataInputStream(new FileInputStream(getDataFilePath().toFile()));
            while (selfStream.skipBytes(0)==0) {  // dirty trick to keep reading until EOF
                OlaOctreePoint pt=new OlaOctreePoint(selfStream);
                if (!pt.isFullyRead())
                    break;
                double[] p=new double[]{pt.getPosition().getX(),pt.getPosition().getY(),pt.getPosition().getZ()};
                if (children[0].contains(p))
                    children[0].addPoint(pt);   // TODO: make sure > and < in the contains(...) method is not falsely rejecting points on the boundary of the children boxes
                if (children[1].contains(p))
                    children[1].addPoint(pt);
            }
            selfStream.close();
            //
            isLeaf=false;
            deleteDataFile();
        }

        private void deleteDataFile() {
            getDataFilePath().toFile().delete();
        }
}

    public static void main(String[] args) throws IOException
    {
        NativeLibraryLoader.loadVtkLibraries();
        Path filePath=Paths.get("/Volumes/dumbledore/sbmt/OLA/OBJLIST182.l2");
        OlaPointList list=new OlaPointList();
        list.appendFromPath(filePath);
        int megaByte=1048576;
        int dataFileByteLimit=5*megaByte;
        int maxPointsPerLeaf=dataFileByteLimit/(8*4);   // three doubles for scpos, three doubles for tgpos, one double for time, and one double for intensity
        System.out.println("Total points="+list.getNumberOfPoints()+"  Approximate number of leaves="+list.getNumberOfPoints()/maxPointsPerLeaf+"  Max points per leaf="+maxPointsPerLeaf);
        //
        BoundingBox bbox=new BoundingBox(new double[]{-100,100,-100,100,-100,100});
        FileSystemOctree tree=new FileSystemOctree(Paths.get("/Volumes/dumbledore/sbmt/tree"), maxPointsPerLeaf, bbox);
        Path rootDirectory=Paths.get("/Volumes/dumbledore/sbmt/OLA/");
        List<File> fileList=Lists.newArrayList();
        Collection<File> fileCollection=FileUtils.listFiles(rootDirectory.toFile(), new WildcardFileFilter("OBJLIST*.l2"), null);
        for (File f : fileCollection)
            fileList.add(f);
        //
        Stopwatch sw=new Stopwatch();
        for (int i=0; i<fileList.size(); i++) {
            sw.start();
            Path inputPath=Paths.get(fileList.get(i).toString());
            System.out.println(inputPath);
            tree.addPointsFromFile(inputPath);
            System.out.println(sw.elapsedTime(TimeUnit.SECONDS)+"s elapsed");
            sw.stop();
            sw.reset();
        }
    }

}
