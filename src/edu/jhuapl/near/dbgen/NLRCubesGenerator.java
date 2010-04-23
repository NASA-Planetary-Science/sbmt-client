package edu.jhuapl.near.dbgen;

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
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		ErosModel erosModel = new ErosModel();
		
		String nlrFileList = args[1];

		ArrayList<String> nlrFiles = null;
		try {
			nlrFiles = FileUtil.getFileLinesAsStringList(nlrFileList);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		ErosCubes cubes = new ErosCubes(erosModel, 1.0, 1.0);
		// First go through all the data and determine which cubes are not empty. We only need to
		// create files for nonempty cubes
		
		try
		{
    	for (String filename : nlrFiles)
    	{
    		ArrayList<String> lines = FileUtil.getFileLinesAsStringList(filename);
    		for (int i=2; i<lines.size(); ++i)
    		{
                String[] vals = lines.get(i).trim().split("\\s+");
                
    		}
    	}
		}
		catch (Exception e)
		{
			
		}
	}

}
