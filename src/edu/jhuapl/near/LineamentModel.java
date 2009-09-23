package edu.jhuapl.near;

import java.io.*;
import java.util.*;

import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class LineamentModel 
{
	private HashMap<Integer, Lineament> idToLineamentMap = new HashMap<Integer, Lineament>();
	private vtkPolyData lineaments;
	
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
	
	public LineamentModel()
	{
		try {
			loadModel();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Number of lineaments: " + this.idToLineamentMap.size());
	}
	
	private void loadModel() throws NumberFormatException, IOException
	{
		InputStream is = getClass().getResourceAsStream("/edu/jhuapl/near/data/LinearFeatures.txt");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isr);

		String line;
        while ((line = in.readLine()) != null)
        {
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
            double lat = Double.parseDouble(tokens[2]) * Math.PI / 180.0;
            double lon = (360.0-Double.parseDouble(tokens[3])) * Math.PI / 180.0;
            double rad = Double.parseDouble(tokens[4])+0.075;
            
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
	
	private void createPolyData()
	{
		lineaments = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();

        vtkIdList idList = new vtkIdList();

        int c=0;
		for (Integer id : this.idToLineamentMap.keySet())
		{
			Lineament lin =	this.idToLineamentMap.get(id);
			
            int size = lin.x.size();
            idList.SetNumberOfIds(size);
            
            for (int i=0;i<size;++i)
            {
            	points.InsertNextPoint(lin.x.get(i), lin.y.get(i), lin.z.get(i));
            	idList.SetId(i, c);
            	++c;
            }

            lines.InsertNextCell(idList);
		}
		
        lineaments.SetPoints(points);
        lineaments.SetLines(lines);
	}
	
	public vtkPolyData getLineamentsAsPolyData()
	{
		if (lineaments == null)
			createPolyData();
		
		return lineaments;
	}
}
