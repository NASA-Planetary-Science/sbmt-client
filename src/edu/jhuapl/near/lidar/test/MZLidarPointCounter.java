package edu.jhuapl.near.lidar.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MZLidarPointCounter
{
    public static class LidarCount
    {
        int cubeNumber,count;

        public LidarCount(int cubeNumber, int count)
        {
            this.cubeNumber=cubeNumber;
            this.count=count;
        }

        public int getCubeNumber()
        {
            return cubeNumber;
        }

        public int getCount()
        {
            return count;
        }
    }

    public static class PointCount implements Callable<LidarCount>
    {

        int cubeNumber;

        public PointCount(int cubeNumber)
        {
            this.cubeNumber=cubeNumber;
        }

        @Override
        public LidarCount call() throws Exception
        {
            MZLidarPointSet pointSet=new MZLidarPointSet();
            pointSet.appendPointsFromTextFile(getCubeFilePath(cubeNumber));
            return new LidarCount(cubeNumber, pointSet.getNumberOfPoints());
        }

    }

    public static Path getCubeFilePath(int cubeNumber)
    {
        String basePath="/Volumes/dumbledore/sbmt/cubes/";
        String whichFile=cubeNumber+".lidarcube";
        return Paths.get(basePath+whichFile);
    }

    public static void main(String[] args)  // spawn a number of threads to read number of points in each cube file
    {
        CompletionService completor=new ExecutorCompletionService<>(Executors.newFixedThreadPool(4));
        int remainingFutures=0;
        //
        int cubeMax=2404;
        for (int i=0; i<=cubeMax; i++)
        {
            remainingFutures++;
            completor.submit(new PointCount(i));
        }

        try
        {
            FileWriter writer=new FileWriter(new File("/Users/zimmemi1/cubedata.txt"));
            Future<LidarCount> completedFuture;
            LidarCount result=null;
            while (remainingFutures>0) {
                completedFuture=completor.take();
                remainingFutures--;
                result=completedFuture.get();
                writer.write(result.getCubeNumber()+" "+result.count+System.getProperty("line.separator"));
                System.out.println(remainingFutures+" cubes remaining...");
            }
            writer.close();
        }
        catch (IOException | InterruptedException | ExecutionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
