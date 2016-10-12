package edu.jhuapl.sbmt.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import com.google.common.collect.Maps;

public class NISSunVectorProcessor
{
    public static void main(String[] args)
    {
        class StringVec
        {
            String x,y,z;

            public StringVec(String x, String y, String z)
            {
                this.x=x;
                this.y=y;
                this.z=z;
            }
        }

        Map<String, StringVec> timeToSunVectorMap=Maps.newHashMap();
        Path timeToSunVectorFile=Paths.get("eros_sunVectors.txt");    // this is a file I got from Lil
        Path fileToTimeFile=Paths.get("nisTimes.txt");
        try
        {
            Scanner scanner=new Scanner(timeToSunVectorFile.toFile());
            while (scanner.hasNextLine())
            {
                String line=scanner.nextLine();
                String[] tokens=line.replaceAll(",", "").trim().split("\\s+");
                String time=tokens[0];
                String sunx=tokens[1];
                String suny=tokens[2];
                String sunz=tokens[3];
                timeToSunVectorMap.put(time, new StringVec(sunx, suny, sunz));
            }
            scanner.close();
            //
            Path outFile=Paths.get("nisSunVectors.txt");
            FileWriter writer=new FileWriter(outFile.toFile());
            scanner=new Scanner(fileToTimeFile.toFile());
            while (scanner.hasNextLine())
            {
                String line=scanner.nextLine();
                String[] tokens=line.replaceAll(",", "").trim().split("\\s+");
                String file=tokens[0];
                String time=tokens[1];
                StringVec sunVector=timeToSunVectorMap.get(time);
                writer.write(file+" "+sunVector.x+" "+sunVector.y+" "+sunVector.z+"\n");
            }
            scanner.close();
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
