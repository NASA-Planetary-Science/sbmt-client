package edu.jhuapl.near;

import java.util.*;
import com.trolltech.qt.core.*;

public class LineamentModel 
{
	private static class Lineament
	{
		public String filename;
		public ArrayList<Double> lat = new ArrayList<Double>();
		public ArrayList<Double> lon = new ArrayList<Double>();
		double minLat = Double.MAX_VALUE;
		double maxLat = Double.MIN_VALUE;
		double minLon = Double.MAX_VALUE;
		double maxLon = Double.MIN_VALUE;
	}
	
	private HashMap<Integer, Lineament> idToLineamentMap = new HashMap<Integer, Lineament>();
	
	public LineamentModel()
	{
		loadModel();
	}
	
	private void loadModel()
	{
        QFile file = new QFile("classpath:edu/jhuapl/near/data/LinearFeatures.txt");
        if (!file.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly,
                                              QIODevice.OpenModeFlag.Text)))
        {
        	System.out.println("Could not load file");
            return;
        }

        QTextStream in = new QTextStream(file);
        while (!in.atEnd()) 
        {
            String line = in.readLine();
            String [] tokens = line.split("\t");
            
            if (tokens.length < 4)
            {
                System.out.println(tokens.length);
                for (int i=0;i<tokens.length;++i)
                	System.out.println(tokens[i]);
                continue;
            }

            String filename = tokens[0];
            Integer id = Integer.parseInt(tokens[1]);
            double lat = Double.parseDouble(tokens[2]);
            double lon = Double.parseDouble(tokens[3]);
            
            if (!this.idToLineamentMap.containsKey(id))
            {
            	this.idToLineamentMap.put(id, new Lineament());
            }            
            
            Lineament lin = this.idToLineamentMap.get(id);
            lin.filename = filename;
            lin.lat.add(lat);
            lin.lon.add(lon);
            
            // Update the bounds of the lineaments
            if (lin.minLat > lat)
            	lin.minLat = lat;
            if (lin.maxLat < lat)
            	lin.maxLat = lat;
            if (lin.minLon > lon)
            	lin.minLon = lon;
            if (lin.maxLon < lon)
            	lin.maxLon = lon;
        }

	}
}
