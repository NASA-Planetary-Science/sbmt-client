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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import vtk.vtkCellArray;
import vtk.vtkHexahedron;
import vtk.vtkPoints;
import vtk.vtkUnstructuredGrid;
import vtk.vtkUnstructuredGridWriter;

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
        addPointsFromFile(inputFilePath, Integer.MAX_VALUE);
    }

    public void addPointsFromFile(Path inputFilePath, int nmax) throws IOException {
        OlaPointList pointList=new OlaPointList();
        pointList.appendFromPath(inputFilePath);
        for (int i=0; i<Math.min(pointList.getNumberOfPoints(),nmax); i++) {
            if ((i%20000)==0)
                System.out.println((double)i/(double)pointList.getNumberOfPoints()*100+"%");
            root.addPoint(new OlaOctreePoint(pointList.getPoint(i)));
        }
    }

    public vtkUnstructuredGrid getAllNonEmptyLeavesAsUnstructuredGrid() {
        List<Node> nodeList=getAllNonEmptyLeafNodes();
        vtkPoints points=new vtkPoints();
        vtkCellArray cells=new vtkCellArray();
        for (Node node : nodeList) {
            vtkHexahedron hex=new vtkHexahedron();
            for (int i=0; i<8; i++) {
                Vector3D crn=node.getCorner(i);
                int id=points.InsertNextPoint(crn.getX(),crn.getY(),crn.getZ());
                hex.GetPointIds().SetId(i,id);
            }
            cells.InsertNextCell(hex);
        }
        //
        vtkUnstructuredGrid grid=new vtkUnstructuredGrid();
        grid.SetPoints(points);
        grid.SetCells(new vtkHexahedron().GetCellType(), cells);
        return grid;
    }

    public List<Node> getAllNonEmptyLeafNodes() {
        List<Node> nodeList=Lists.newArrayList();
        getAllNonEmptyLeafNodes(root, nodeList);
        return nodeList;
    }

    void getAllNonEmptyLeafNodes(Node node, List<Node> nodeList) {
        if (!node.isLeaf)
            for (int i=0; i<8; i++)
                getAllNonEmptyLeafNodes(node.children[i], nodeList);
        else if (node.numPoints>0)
            nodeList.add(node);
    }

/*    public List<Path> getAllNonEmptyLeafDirectories() {
        List<Path> pathList=Lists.newArrayList();
        getAllNonEmptyLeafDirectories(root, pathList);
        return pathList;
    }

    void getAllNonEmptyLeafDirectories(Node node, List<Path> pathList) {
        if (!node.isLeaf)
            for (int i=0; i<8; i++)
                getAllNonEmptyLeafDirectories(node.children[i], pathList);
        else if (node.getDataFilePath().toFile().exists())
            pathList.add(node.getSelfPath());
    }*/

    public void finalCommit() throws IOException {  // TODO: change Node data files to RandomAccessFile (to avoid having too many files open when tree becomes large)
        finalCommit(root);
    }

    void finalCommit(Node node) throws IOException {
        if (!node.isLeaf)
            for (int i=0; i<8; i++)
                finalCommit(node.children[i]);
        else {
            node.closeDataFileForOutput();  // close any DataOutputStreams (corresponding to leaves) that are still open
            File dataFile=node.getDataFilePath().toFile();  // clean up any data files with zero points
            if (dataFile.length()==0l)
                dataFile.delete();
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

    static BoundingBox createBoundingBox(Node parent, int whichChild) {
        BoundingBox bbox=new BoundingBox(parent.getBounds());
        double xmid=bbox.getCenterPoint()[0];
        double ymid=bbox.getCenterPoint()[1];
        double zmid=bbox.getCenterPoint()[2];
        switch (whichChild) {
        case 0:
            bbox.xmax=xmid;
            bbox.ymax=ymid;
            bbox.zmax=zmid;
            break;
        case 1:
            bbox.xmin=xmid;
            bbox.ymax=ymid;
            bbox.zmax=zmid;
            break;
        case 2:
            bbox.xmin=xmid;
            bbox.ymin=ymid;
            bbox.zmax=zmid;
            break;
        case 3:
            bbox.xmax=xmid;
            bbox.ymin=ymid;
            bbox.zmax=zmid;
            break;
        case 4:
            bbox.xmax=xmid;
            bbox.ymax=ymid;
            bbox.zmin=zmid;
            break;
        case 5:
            bbox.xmin=xmid;
            bbox.ymax=ymid;
            bbox.zmin=zmid;
            break;
        case 6:
            bbox.xmin=xmid;
            bbox.ymin=ymid;
            bbox.zmin=zmid;
            break;
        case 7:
            bbox.xmax=xmid;
            bbox.ymin=ymid;
            bbox.zmin=zmid;
            break;
        }
        //System.out.println(bbox);
        return bbox;
    }

    class Node extends BoundingBox {
        final Path selfPath;
        boolean isLeaf=true;
        Node[] children=new Node[8];
        int numPoints=0;
        DataOutputStream dataOutput;

        public Vector3D getCorner(int i) {
            switch(i) {
            case 0:
                return new Vector3D(xmin,ymin,zmin);
            case 1:
                return new Vector3D(xmax,ymin,zmin);
            case 2:
                return new Vector3D(xmax,ymax,zmin);
            case 3:
                return new Vector3D(xmin,ymax,zmin);
            case 4:
                return new Vector3D(xmin,ymin,zmax);
            case 5:
                return new Vector3D(xmax,ymin,zmax);
            case 6:
                return new Vector3D(xmax,ymax,zmax);
            case 7:
                return new Vector3D(xmin,ymax,zmax);
            }
            return null;
        }

        public double getVolume() {
            return (xmax-xmin)*(ymax-ymin)*(zmax-zmin);
        }

        public Node(Path rootPath, BoundingBox bbox) throws IOException
        {
            super(bbox.getBounds());
            selfPath=rootPath;
            getSelfPath().toFile().mkdir();
            writeBounds();
            openDataFileForOutput();
        }

        Node(Node parent, int whichChild) throws IOException
        {
            super(createBoundingBox(parent, whichChild).getBounds());
            //
            selfPath=parent.getSelfPath().resolve(String.valueOf(whichChild));
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
                for (int i=0; i<8; i++)
                    if (children[i].addPoint(point))
                        return true;
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

        public double[] readBounds() throws IOException {
            DataInputStream stream=new DataInputStream(new FileInputStream(getBoundsFilePath().toFile()));
            double[] bounds=new double[6];
            for (int i=0; i<6; i++)
                bounds[i]=stream.readDouble();
            stream.close();
            return bounds;
        }

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
            closeDataFileForOutput();
            for (int i=0; i<8; i++)
                children[i]=new Node(this, i);
            //
            DataInputStream selfStream=new DataInputStream(new FileInputStream(getDataFilePath().toFile()));
            while (selfStream.skipBytes(0)==0) {  // dirty trick to keep reading until EOF
                OlaOctreePoint pt=new OlaOctreePoint(selfStream);
                if (!pt.isFullyRead())
                    break;
/*                Vector3D pos=pt.getPosition();
                double[] cen=getCenterPoint();
                boolean xlt=pos.getX()<cen[0];
                boolean ylt=pos.getY()<cen[1];
                boolean zlt=pos.getZ()<cen[2];
                int i=-1;
                if (zlt) {
                    if (ylt) {
                        if (xlt)
                            i=0;
                        else
                            i=1;
                    } else {
                        if (xlt)
                            i=3;
                        else
                            i=2;
                    }
                } else  {
                    if (ylt) {
                        if (xlt)
                            i=4;
                        else
                            i=5;
                    } else {
                        if (xlt)
                            i=7;
                        else
                            i=6;
                    }
                }
        //        if (!children[i].contains(new double[]{pos.getX(),pos.getY(),pos.getZ()}))
        //            System.out.println("!");

                children[i].addPoint(pt);*/
                double[] p=new double[]{pt.getPosition().getX(),pt.getPosition().getY(),pt.getPosition().getZ()};
                boolean found=false;
                for (int i=0; i<8 && !found; i++)
                    if (children[i].contains(p)) {
                        children[i].addPoint(pt);   // TODO: make sure > and < in the contains(...) method is not falsely rejecting points on the boundary of the children boxes
                        found=true;
                    }
            }
//            for (int i=0; i<8; i++)
//                System.out.println("  "+children[i].numPoints);
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
        int dataFileByteLimit=10*megaByte;
        int maxPointsPerLeaf=dataFileByteLimit/(8*4);   // three doubles for scpos, three doubles for tgpos, one double for time, and one double for intensity
        System.out.println("Total points="+list.getNumberOfPoints()+"  Max points per leaf="+maxPointsPerLeaf);
        //
        BoundingBox bbox=new BoundingBox(new double[]{-1,1,-1,1,-1,1});
        FileSystemOctree tree=new FileSystemOctree(Paths.get("/Volumes/dumbledore/sbmt/tree"), maxPointsPerLeaf, bbox);
        Path rootDirectory=Paths.get("/Volumes/dumbledore/sbmt/OLA/");
        List<File> fileList=Lists.newArrayList();
        Collection<File> fileCollection=FileUtils.listFiles(rootDirectory.toFile(), new WildcardFileFilter("OBJLIST*.l2"), null);
        for (File f : fileCollection)
            fileList.add(f);
        //
        Stopwatch sw=new Stopwatch();
        int i=0;
//        for (int i=0; i<fileList.size(); i++) {
            sw.start();
            Path inputPath=Paths.get(fileList.get(i).toString());
            System.out.println(inputPath);
            tree.addPointsFromFile(inputPath);
                System.out.println(sw.elapsedTime(TimeUnit.SECONDS)+"s elapsed");// TODO: close down all DataOutputStreams
            sw.reset();
//        }
        tree.finalCommit();

        //
        vtkUnstructuredGrid grid=tree.getAllNonEmptyLeavesAsUnstructuredGrid();
        vtkUnstructuredGridWriter writer=new vtkUnstructuredGridWriter();
        writer.SetFileName(tree.outputDirectory.resolve("tree.vtk").toString());
        writer.SetFileTypeToBinary();
        writer.SetInputData(grid);
        writer.Write();

        System.out.println("Total # of cells="+grid.GetNumberOfCells());
    }

}
