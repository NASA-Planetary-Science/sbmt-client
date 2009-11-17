package edu.jhuapl.near.model;

import java.io.File;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import edu.jhuapl.near.util.ConvertToRealFile;
import edu.jhuapl.near.util.Properties;

public class ErosModel extends Model 
{
    private vtkActor erosActor;
    private boolean showLighting = true;
    private ArrayList<vtkActor> erosActors = new ArrayList<vtkActor>();
    
	public ErosModel()
	{
    	vtkPolyDataReader erosReader = new vtkPolyDataReader();
        File file = ConvertToRealFile.convertResourceToTempFile(this, "/edu/jhuapl/near/data/Eros_Dec2006_0.vtk");
        erosReader.SetFileName(file.getAbsolutePath());
        erosReader.Update();

        vtkPolyDataMapper erosMapper = new vtkPolyDataMapper();
        erosMapper.SetInput(erosReader.GetOutput());

        erosActor = new vtkActor();
        //erosActor.GetProperty().SetRepresentationToWireframe();
        erosActor.SetMapper(erosMapper);

        erosActors.add(erosActor);
	}
	
	public void setShowEros(boolean show)
	{
		if (show)
		{
			if (erosActors.isEmpty())
			{
				erosActors.add(erosActor);
				this.pcs.firePropertyChange(Properties.EROS_MODEL_CHANGED, null, null);
			}
		}
		else
		{
			if (!erosActors.isEmpty())
			{
				erosActors.clear();
				this.pcs.firePropertyChange(Properties.EROS_MODEL_CHANGED, null, null);
			}
		}
		
	}
	
	public void setShowLighting(boolean lighting)
	{
		this.showLighting = lighting;
		this.erosActor.GetProperty().SetLighting(showLighting);
		this.pcs.firePropertyChange(Properties.EROS_MODEL_CHANGED, null, null);
	}

	public ArrayList<vtkActor> getActors() 
	{
		return erosActors;
	}
	
}
