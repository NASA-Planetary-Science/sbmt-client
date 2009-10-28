package edu.jhuapl.near.gui;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.beans.*;

import edu.jhuapl.near.model.LineamentModel;
import edu.jhuapl.near.model.NearImage;
import edu.jhuapl.near.model.NearImage.Range;
import edu.jhuapl.near.util.ConvertToRealFile;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.Properties;

import java.awt.*;
import java.awt.event.*;


import nom.tam.fits.*;

import vtk.*;

public class ImageGLWidget extends JPanel implements 
			MouseListener, 
			MouseMotionListener, 
			MouseWheelListener,
			PropertyChangeListener
{
    private LineamentModel lineamentModel;
    private StatusBar statusBar;
    private vtkRenderWindowPanel renWin;

    private vtkActor lineamentActor;
    private vtkActor erosActor;
    
    //private ArrayList<vtkActor> nearImageActors = new ArrayList<vtkActor>();
    private HashMap<NearImage, ArrayList<vtkActor>> nearImageActors = new HashMap<NearImage, ArrayList<vtkActor>>();
    
    private HashMap<File, NearImage> fileToImageMap = new HashMap<File, NearImage>();
    
    private PopupMenu popupMenu;
    
    public ImageGLWidget(LineamentModel model, StatusBar statusBar) 
    {
    	setLayout(new BorderLayout());
    	
    	this.statusBar = statusBar;
    	
        // Create the buttons.
        renWin = new vtkRenderWindowPanel();

        lineamentModel = model;

        lineamentModel.addPropertyChangeListener(this);
        
        popupMenu = new PopupMenu(model);
        
    	vtkPolyDataReader erosReader = new vtkPolyDataReader();
        File file = ConvertToRealFile.convertResource(this, "/edu/jhuapl/near/data/Eros_Dec2006_0.vtk");
        erosReader.SetFileName(file.getAbsolutePath());
        erosReader.Update();

        vtkPolyDataMapper erosMapper = new vtkPolyDataMapper();
        erosMapper.SetInput(erosReader.GetOutput());

        erosActor = new vtkActor();
        //erosActor.GetProperty().SetRepresentationToWireframe();
        erosActor.SetMapper(erosMapper);

        renWin.GetRenderer().AddActor(erosActor);

        vtkPolyDataMapper lineamentMapper = new vtkPolyDataMapper();
        lineamentMapper.SetInput(lineamentModel.getLineamentsAsPolyData());
        //lineamentMapper.SetResolveCoincidentTopologyToPolygonOffset();
        //lineamentMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1000.0, -1000.0);
        
        lineamentActor = new vtkActor();
        lineamentActor.SetMapper(lineamentMapper);
        
        renWin.GetRenderer().AddActor(lineamentActor);

        
        vtkInteractorStyleTrackballCamera style =
            new vtkInteractorStyleTrackballCamera();
        renWin.setInteractorStyle(style);
        
        add(renWin, BorderLayout.CENTER);

        renWin.addMouseListener(this);
        renWin.addMouseMotionListener(this);
        renWin.addMouseWheelListener(this);
    }
    
    public void showModel(boolean show)
    {
    	if (show)
    	{
    		if (renWin.GetRenderer().HasViewProp(erosActor) == 0)
    			renWin.GetRenderer().AddActor(erosActor);
    	}
    	else
    	{
    		if (renWin.GetRenderer().HasViewProp(erosActor) > 0)
    			renWin.GetRenderer().RemoveActor(erosActor);
    	}
    	renWin.Render();
    }
    
    public void showLineaments(boolean show)
    {
    	if (show)
    	{
    		if (renWin.GetRenderer().HasViewProp(lineamentActor) == 0)
    			renWin.GetRenderer().AddActor(lineamentActor);
    	}
    	else
    	{
    		if (renWin.GetRenderer().HasViewProp(lineamentActor) > 0)
    			renWin.GetRenderer().RemoveActor(lineamentActor);
    	}
    	renWin.Render();
    }

    /*
    public void setImages(ArrayList<File> files) throws FitsException, IOException
    {
    	// First remove all actors
    	for (vtkActor act : this.nearImageActors)
    		renWin.GetRenderer().RemoveActor(act);

    	renWin.Render();
    	
    	nearImageActors.clear();
    	fileToImageMap.clear();
    	
    	int c = 1;
    	for (File file : files)
    	{
        	NearImage image = new NearImage(file.getAbsolutePath());

        	fileToImageMap.put(file, image);

        	// Now texture map this image onto the Eros model.
        	mapImage(image, -c*10);
        	++c;
    	}
    	
    	renWin.Render();
    }
    */
    
    public void addImage(File file) throws FitsException, IOException
    {
    	NearImage image = new NearImage(file.getAbsolutePath());

    	fileToImageMap.put(file, image);
    	nearImageActors.put(image, new ArrayList<vtkActor>());
    	
    	// Now texture map this image onto the Eros model.
    	mapImage(image, -10);
    }
    
    public void removeImage(File file)
    {
    	
    	for (vtkActor act : nearImageActors.get(fileToImageMap.get(file)))
    		renWin.GetRenderer().RemoveActor(act);

    	renWin.Render();
    	
    	nearImageActors.remove(fileToImageMap.get(file));
    	fileToImageMap.remove(file);
    }
    
    public NearImage getImage(File file)
    {
    	return fileToImageMap.get(file);
    }
    
    public vtkRenderWindowPanel getRenderWindowPanel()
    {
    	return renWin;
    }
    
    private void mapImage(NearImage nearImage, double offset)
    {
    	ArrayList<vtkActor> imagePieces = nearImage.getMappedImage(offset);
    	
    	for (vtkActor piece : imagePieces)
    	{
    		renWin.GetRenderer().AddActor(piece);
            
    		nearImageActors.get(nearImage).add(piece);
    	}
    }
    
	public void setDisplayedImageRange(NearImage image, Range range)
	{
		image.setDisplayedImageRange(range);
		renWin.Render();
	}

	public void mousePressed(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	public void mouseClicked(MouseEvent e)
	{
		
	}

	public void mouseEntered(MouseEvent e)
	{
		
	}

	public void mouseExited(MouseEvent e)
	{
		
	}

	public void mouseDragged(MouseEvent e)
	{
		
	}

	public void mouseMoved(MouseEvent e)
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
			return;

		LineamentModel.Lineament lin = pickLineament(e);

		if (lin != null)
			statusBar.setLeftText("Lineament " + lin.id + " mapped on MSI image " + lin.name + " contains " + lin.x.size() + " vertices");

		LatLon ll = pickEros(e);
		if (ll != null)
			statusBar.setRightText("Lineament " + lin.id + " mapped on MSI image " + lin.name + " contains " + lin.x.size() + " vertices");
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		int ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1
				: 0;
		int shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1
				: 0;

		renWin.getIren().SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed,
			        shiftPressed, '0', 0, "0");

		renWin.lock();
		if (e.getWheelRotation() > 0)
			renWin.getIren().MouseWheelBackwardEvent();
		else
			renWin.getIren().MouseWheelForwardEvent();
		renWin.unlock();
	}

	private void maybeShowPopup(MouseEvent e) 
	{
        if (e.isPopupTrigger()) 
        {
    		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
    			return;
    		
    		LineamentModel.Lineament lin = pickLineament(e);
    		
    		if (lin != null)
            	popupMenu.show(e.getComponent(), e.getX(), e.getY(), lin);
        }
    }

	private LineamentModel.Lineament pickLineament(MouseEvent e)
	{
		vtkCellPicker cellPicker = new vtkCellPicker();
		cellPicker.SetTolerance(0.002);
		int pickSucceeded = cellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded != 0 && cellPicker.GetActor() == this.lineamentActor)
			return this.lineamentModel.getLineament(cellPicker.GetCellId());
		else
			return null;
	}

	private LatLon pickEros(MouseEvent e)
	{
		vtkCellPicker cellPicker = new vtkCellPicker();
		cellPicker.SetTolerance(0.002);
		int pickSucceeded = cellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded != 0/* && cellPicker.GetActor() == this.erosActor*/)
		{
			vtkPoints pts = cellPicker.GetPickedPositions();
			System.out.println(pts.GetNumberOfPoints());
			for (int i=0;i<cellPicker.GetActors().GetNumberOfItems();++i)
				System.out.println("--"+(cellPicker.GetActors().GetItemAsObject(i)==this.erosActor));
			return null;
		}
		else
		{
			System.out.println(0);
			return null;
		}
	}

    public void propertyChange(PropertyChangeEvent e)
    {
    	if (Properties.LINEAMENT_MODEL_CHANGED.equals(e.getPropertyName()))
    		renWin.Render();
    }

    public LineamentModel getLineamentModel()
    {
    	return lineamentModel;
    }
    }
