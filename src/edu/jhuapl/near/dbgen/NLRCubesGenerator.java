package edu.jhuapl.near.dbgen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.jhuapl.near.model.ErosModel;
import edu.jhuapl.near.query.ErosCubes;
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
		javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
        	public void run()
        	{
        		// do nothing
        	}
        });
		
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		ErosModel erosModel = new ErosModel();
		System.out.println(args[0]);
		String nlrFileList = args[0];
		String outputFolder = args[1];
		
		ArrayList<String> nlrFiles = null;
		try {
			nlrFiles = FileUtil.getFileLinesAsStringList(nlrFileList);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		ErosCubes cubes = new ErosCubes(erosModel, 1.0, 1.0);
		
		// First go through all the data and determine which cubes are not empty. We only need to
		// create files for nonempty cubes
		
		double[] pt = new double[3];
		
		try
		{
			for (String filename : nlrFiles)
			{
				ArrayList<String> lines = FileUtil.getFileLinesAsStringList(filename);
				for (int i=2; i<lines.size(); ++i)
				{
					String[] vals = lines.get(i).trim().split("\\s+");

	       			pt[0] = Double.parseDouble(vals[14])/1000.0;
        			pt[1] = Double.parseDouble(vals[15])/1000.0;
        			pt[2] = Double.parseDouble(vals[16])/1000.0;

        			int cubeid = cubes.getCubeId(pt);

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

		}
	}
}
