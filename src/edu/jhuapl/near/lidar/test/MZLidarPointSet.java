package edu.jhuapl.near.lidar.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;

public class MZLidarPointSet
{
    List<MZLidarPoint> points=Lists.newArrayList();
    static final int TIMESTAMP_LENGTH=26;
    static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    public void clear() {
        points.clear();
    }

    public void appendPointsFromTextFile(Path filePath) {
        try
        {
            Scanner scanner=new Scanner(new BufferedInputStream(new FileInputStream(filePath.toFile())));
            while (scanner.hasNext()) {
                String time=scanner.next();
                //System.out.println(time);
                double tgx=Double.valueOf(scanner.next());
                double tgy=Double.valueOf(scanner.next());
                double tgz=Double.valueOf(scanner.next());
                double scx=Double.valueOf(scanner.next());
                double scy=Double.valueOf(scanner.next());
                double scz=Double.valueOf(scanner.next());
                double dum=Double.valueOf(scanner.next());
                points.add(new MZLidarPoint(time,scx,scy,scz,tgx,tgy,tgz));
            }
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void writePointsToBinaryFile(Path filePath) {
        try
        {
            BufferedDataOutputStream stream=new BufferedDataOutputStream(new FileOutputStream(filePath.toFile()));
           // System.out.println(filePath.toFile());
            stream.writeInt(points.size());
            for (MZLidarPoint p : points) {
                //stream.writeChars(p.timestamp);  // timestamps are assumed to be 26 characters long, 2019-04-22T11:07:13.796204; cf. declaration of TIMESTAMP_LENGTH
                stream.writeLong(sdf.parse(p.timestamp).getTime());   // this writes the number of milliseconds since Jan 1, 1970 as a long value; will need to replace with something that has microsecond precision
                stream.writeDouble(p.scx);
                stream.writeDouble(p.scy);
                stream.writeDouble(p.scz);
                stream.writeDouble(p.tgx);
                stream.writeDouble(p.tgy);
                stream.writeDouble(p.tgz);
            }
            stream.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void appendPointsFromBinaryFile(Path filePath) {
        try
        {
            BufferedDataInputStream stream=new BufferedDataInputStream(new FileInputStream(filePath.toFile()));
            System.out.print(filePath.toFile()+" !");
            int npts=stream.readInt();
            System.out.println("**********"+npts);
            for (int i=0; i<npts; i++) {
                //char[] timestamp=new char[TIMESTAMP_LENGTH];
                //stream.read(timestamp,0,TIMESTAMP_LENGTH);
                long time=stream.readLong();
                //System.out.println(time);
                double scx=stream.readDouble();
                double scy=stream.readDouble();
                double scz=stream.readDouble();
                double tgx=stream.readDouble();
                double tgy=stream.readDouble();
                double tgz=stream.readDouble();
                points.add(new MZLidarPoint(sdf.format(new Date(time)),scx,scy,scz,tgx,tgy,tgz));
            }
            stream.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int getNumberOfPoints() {
        return points.size();
    }

    public MZLidarPoint getPoint(int i) {
        return points.get(i);
    }

    public static class TextToBinaryConversion implements Runnable
    {

        int cubeNumber;

        public TextToBinaryConversion(int cubeNumber)
        {
            this.cubeNumber=cubeNumber;
        }

        @Override
        public void run()
        {
            MZLidarPointSet pointSet=new MZLidarPointSet();
            pointSet.appendPointsFromTextFile(getCubeFilePath(cubeNumber));
            Path binaryFilePath=Paths.get(getCubeFilePath(cubeNumber).toString()+".bin");
            pointSet.writePointsToBinaryFile(binaryFilePath);
        }

    }

    public static Path getCubeFilePath(int cubeNumber)
    {
        String basePath="/Volumes/dumbledore/sbmt/cubes/";
        String whichFile=cubeNumber+".lidarcube";
        return Paths.get(basePath+whichFile);
    }

    public static void main(String[] args)
    {

        Stopwatch sw=new Stopwatch();
        sw.start();

        int cubeMax=2404;
        for (int i=0; i<=cubeMax; i++)
        {
            System.out.println((i+1)+"/"+cubeMax);
            new TextToBinaryConversion(i).run();
        }

        System.out.println(sw.elapsedTime(TimeUnit.SECONDS)+" seconds elapsed.");

  /*      CompletionService completor=new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));
        int remainingFutures=0;
        //
        int cubeMax=2404;
        for (int i=0; i<=cubeMax; i++)
        {
            remainingFutures++;
            completor.submit(new TextToBinaryConversion(i), true);
        }

        try
        {
            while (remainingFutures>0) {
                Future completedFuture=completor.take();
                remainingFutures--;
                System.out.println(remainingFutures+" cubes remaining...");
            }
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

    }
}
