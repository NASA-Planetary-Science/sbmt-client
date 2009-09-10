package edu.jhuapl.near;

import java.util.*;
import com.trolltech.qt.core.*;

public class LineamentModel 
{
	public static class Lineament
	{
		public String name = "";
		public int id;
		public ArrayList<Double> lat = new ArrayList<Double>();
		public ArrayList<Double> lon = new ArrayList<Double>();
		public ArrayList<Double> rad = new ArrayList<Double>();
		public ArrayList<Double> x = new ArrayList<Double>();
		public ArrayList<Double> y = new ArrayList<Double>();
		public ArrayList<Double> z = new ArrayList<Double>();
		public BoundingBox bb = new BoundingBox();
	}
	
	private HashMap<Integer, Lineament> idToLineamentMap = new HashMap<Integer, Lineament>();
	
	public LineamentModel()
	{
		loadModel();
		System.out.println("Number of lineaments: " + this.idToLineamentMap.size());
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
            
            if (tokens.length < 5)
            {
                System.out.println(tokens.length);
                for (int i=0;i<tokens.length;++i)
                	System.out.println(tokens[i]);
                continue;
            }

            String name = tokens[0];
            Integer id = Integer.parseInt(tokens[1]);
            double lat = Double.parseDouble(tokens[2]);
            double lon = Double.parseDouble(tokens[3]);
            double rad = Double.parseDouble(tokens[4]);
            
            if (!this.idToLineamentMap.containsKey(id))
            {
            	this.idToLineamentMap.put(id, new Lineament());
            }            
            
            Lineament lin = this.idToLineamentMap.get(id);
            lin.name = name;
            lin.id = id;
            lin.lat.add(lat);
            lin.lon.add(lon);
            lin.rad.add(rad);

            // Convert to xyz
            double x = rad * Math.cos( lon ) * Math.cos( lat );
            double y = rad * Math.sin( lon ) * Math.cos( lat );
            double z = rad * Math.sin( lat );

            lin.x.add(x);
            lin.y.add(y);
            lin.z.add(z);

            // Update the bounds of the lineaments
            lin.bb.update(x, y, z);
        }

	}
	
	public List<Lineament> getLineamentsWithinBox(BoundingBox box)
	{
		ArrayList<Lineament> array = new ArrayList<Lineament>();
		for (Integer id : this.idToLineamentMap.keySet())
		{
			Lineament lin =	this.idToLineamentMap.get(id);
			if (lin.bb.intersects(box))
				array.add(lin);
		}
		return array;
	}
}
