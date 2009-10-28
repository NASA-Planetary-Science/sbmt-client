package edu.jhuapl.near.model;

import java.io.File;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import edu.jhuapl.near.util.ConvertToRealFile;
import edu.jhuapl.near.util.Properties;

public class ErosModel extends Model 
{
    private vtkActor erosActor;
    private boolean showLighting = true;
    //private boolean showWireframe = false;
    
	public ErosModel()
	{
    	vtkPolyDataReader erosReader = new vtkPolyDataReader();
        File file = ConvertToRealFile.convertResource(this, "/edu/jhuapl/near/data/Eros_Dec2006_0.vtk");
        erosReader.SetFileName(file.getAbsolutePath());
        erosReader.Update();

        vtkPolyDataMapper erosMapper = new vtkPolyDataMapper();
        erosMapper.SetInput(erosReader.GetOutput());

        erosActor = new vtkActor();
        //erosActor.GetProperty().SetRepresentationToWireframe();
        erosActor.SetMapper(erosMapper);

	}
	
	public void setShowLighting(boolean lighting)
	{
		this.showLighting = lighting;
		this.erosActor.GetProperty().SetLighting(showLighting);
		this.pcs.firePropertyChange(Properties.EROS_MODEL_CHANGED, null, null);
	}
	
}
