package edu.jhuapl.near.dbgen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.jhuapl.near.model.eros.ErosModel;
import edu.jhuapl.near.util.GeometryUtil;
import edu.jhuapl.near.util.SmallBodyCubes;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

/**
 * This program goes through all the NLR data and divides all the data
 * up into cubes and saves each cube to a separate file.
 * @author kahneg1
 *
 */
public class NLRCubesGenerator
{
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		ErosModel erosModel = new ErosModel();

		String nlrFileList = args[0];
		String outputFolder = args[1];
		
		ArrayList<String> nlrFiles = null;
		try {
			nlrFiles = FileUtil.getFileLinesAsStringList(nlrFileList);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		SmallBodyCubes cubes = new SmallBodyCubes(erosModel, 1.0, 1.0, true);
		
		double[] pt = new double[3];
		
		// If a point is farther than MAX_DIST from the asteroid, then throw it out.
		final double MAX_DIST = 1.0;
		
		try
		{
		    int count = 1;
			for (String filename : nlrFiles)
			{
			    System.out.println("Begin processing file " + filename + " - " + count++ + " / " + nlrFiles.size());
			    
				ArrayList<String> lines = FileUtil.getFileLinesAsStringList(filename);
				for (int i=2; i<lines.size(); ++i)
				{
					String[] vals = lines.get(i).trim().split("\\s+");

	       			pt[0] = Double.parseDouble(vals[14])/1000.0;
        			pt[1] = Double.parseDouble(vals[15])/1000.0;
        			pt[2] = Double.parseDouble(vals[16])/1000.0;

        			double[] closestPt = erosModel.findClosestPoint(pt);
        			
        			double dist = GeometryUtil.distanceBetween(pt, closestPt);
        			
        			if (dist > MAX_DIST)
        				continue;
        			
        			int cubeid = cubes.getCubeId(closestPt);

        			if (cubeid >= 0)
        			{
            			// Open the file for appending
            			FileWriter fstream = new FileWriter(outputFolder + "/" + cubeid + ".nlr", true);
            	        BufferedWriter out = new BufferedWriter(fstream);

            			out.write(lines.get(i) + "\n");
            			
            			out.close();
        			}
				}
			}
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
	}
}
