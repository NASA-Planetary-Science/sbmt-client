package edu.jhuapl.near.model;

import java.io.*;
import java.util.*;

import edu.jhuapl.near.util.Properties;

import vtk.*;

public class LineamentModel extends Model 
{
	private HashMap<Integer, Lineament> idToLineamentMap = new HashMap<Integer, Lineament>();
	private HashMap<Integer, Lineament> cellIdToLineamentMap = new HashMap<Integer, Lineament>();
	private vtkPolyData lineaments;
    private ArrayList<vtkActor> lineamentActors = new ArrayList<vtkActor>();
    private vtkActor lineamentActor;
	private int[] defaultColor = {255, 0, 255, 255}; // RGBA, default to purple
	
	public static class Lineament
	{
		public int cellId;
		public String name = "";
		public int id;
		public ArrayList<Double> lat = new ArrayList<Double>();
		public ArrayList<Double> lon = new ArrayList<Double>();
		public ArrayList<Double> rad = new ArrayList<Double>();
		public ArrayList<Double> x = new ArrayList<Double>();
		public ArrayList<Double> y = new ArrayList<Double>();
		public ArrayList<Double> z = new ArrayList<Double>();
		//public BoundingBox bb = new BoundingBox();
	}

	public LineamentModel()
	{
		try 
		{
			loadModel();

			createPolyData();
			
	        vtkPolyDataMapper lineamentMapper = new vtkPolyDataMapper();
	        lineamentMapper.SetInput(lineaments);
	        //lineamentMapper.SetResolveCoincidentTopologyToPolygonOffset();
	        //lineamentMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1000.0, -1000.0);
	        
	        lineamentActor = new vtkActor();
	        lineamentActor.SetMapper(lineamentMapper);
	        
	        // By default do not show the lineaments
	        //lineamentActors.add(lineamentActor);

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
            //lin.bb.update(x, y, z);
        }

        in.close();
	}
	
	/*
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
	*/
	
	private void createPolyData()
	{
		lineaments = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        
        colors.SetNumberOfComponents(4);
        
        vtkIdList idList = new vtkIdList();

        int c=0;
        int cellId = 0;
		for (Integer id : this.idToLineamentMap.keySet())
		{
			Lineament lin =	this.idToLineamentMap.get(id);
			lin.cellId = cellId;
			
            int size = lin.x.size();
            idList.SetNumberOfIds(size);
            
            for (int i=0;i<size;++i)
            {
            	points.InsertNextPoint(lin.x.get(i), lin.y.get(i), lin.z.get(i));
            	idList.SetId(i, c);
            	++c;
            }

            lines.InsertNextCell(idList);
        	colors.InsertNextTuple4(defaultColor[0],defaultColor[1],defaultColor[2],defaultColor[3]);
            
            cellIdToLineamentMap.put(cellId, lin);
            ++cellId;
		}
		
        lineaments.SetPoints(points);
        lineaments.SetLines(lines);
        lineaments.GetCellData().SetScalars(colors);
	}
	
	public vtkPolyData getLineamentsAsPolyData()
	{
		if (lineaments == null)
			createPolyData();
		
		return lineaments;
	}
	
	public Lineament getLineament(int cellId)
	{
		return this.cellIdToLineamentMap.get(cellId);
	}
		
	public void setLineamentColor(int cellId, int[] color)
	{
		lineaments.GetCellData().GetScalars().SetTuple4(cellId, color[0], color[1], color[2], color[3]);
		lineaments.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}

	public void setsAllLineamentsColor(int[] color)
	{
		int numLineaments = this.cellIdToLineamentMap.size();
		vtkDataArray colors = lineaments.GetCellData().GetScalars();
		
		for (int i=0; i<numLineaments; ++i)
			colors.SetTuple4(i, color[0], color[1], color[2], color[3]);
		
		lineaments.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}
	
	public void setMSIImageLineamentsColor(int cellId, int[] color)
	{
		int numLineaments = this.cellIdToLineamentMap.size();
		String name = cellIdToLineamentMap.get(cellId).name;
		vtkDataArray colors = lineaments.GetCellData().GetScalars();
		
		for (int i=0; i<numLineaments; ++i)
			if (cellIdToLineamentMap.get(i).name.equals(name))
					colors.SetTuple4(i, color[0], color[1], color[2], color[3]);

		lineaments.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}

	public void setRadialOffset(double offset)
	{
        int ptId=0;
        vtkPoints points = lineaments.GetPoints();
        
		for (Integer id : this.idToLineamentMap.keySet())
		{
			Lineament lin =	this.idToLineamentMap.get(id);

            int size = lin.x.size();

            for (int i=0;i<size;++i)
            {
                double x = (lin.rad.get(i)+offset) * Math.cos( lin.lon.get(i) ) * Math.cos( lin.lat.get(i) );
                double y = (lin.rad.get(i)+offset) * Math.sin( lin.lon.get(i) ) * Math.cos( lin.lat.get(i) );
                double z = (lin.rad.get(i)+offset) * Math.sin( lin.lat.get(i) );
            	points.SetPoint(ptId, x, y, z);
            	++ptId;
            }
		}		

		lineaments.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}

	public void setShowLineaments(boolean show)
	{
		if (show)
		{
			if (lineamentActors.isEmpty())
			{
				lineamentActors.add(lineamentActor);
				this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
			}
		}
		else
		{
			if (!lineamentActors.isEmpty())
			{
				lineamentActors.clear();
				this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
			}
		}
		
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		return lineamentActors;
	}
	
    public String getClickStatusBarText(vtkActor actor, int cellId)
    {
		LineamentModel.Lineament lin = getLineament(cellId);
		if (lin != null)
			return "Lineament " + lin.id + " mapped on MSI image " + lin.name + " contains " + lin.x.size() + " vertices";
		else
			return "";
    }

}
