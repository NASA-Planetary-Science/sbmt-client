package edu.jhuapl.near;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.beans.*;

import edu.jhuapl.near.NearImage.Range;

import java.awt.*;
import java.awt.event.*;


import nom.tam.fits.*;

import vtk.*;

class ImageGLWidget extends JPanel implements 
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
    
    private static final int TEXTURE_SIZE = 128;

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
        File file = ConvertResourceToRealFile.convert(this, "/edu/jhuapl/near/data/Eros_Dec2006_0.vtk");
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
        int numTexturesWidth = (int)Math.ceil((double)(NearImage.IMAGE_WIDTH-1) / (double)(TEXTURE_SIZE-1));
        int numTexturesHeight = (int)Math.ceil((double)(NearImage.IMAGE_HEIGHT-1) / (double)(TEXTURE_SIZE-1));
        
        //int totalNumTextures = numTexturesWidth * numTexturesHeight;
        //System.out.println("totalNumTextures  " + totalNumTextures);

        float x, y, z, s, t;
        int i0, i1, i2, i3;
        
        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(3);
        
        for (int i=0; i<numTexturesHeight; ++i)
        	for (int j=0; j<numTexturesWidth; ++j)
        	{
                int corner1 = i*(TEXTURE_SIZE-1);
                int corner2 = j*(TEXTURE_SIZE-1);
        
                vtkPolyData piece = new vtkPolyData();
                vtkPoints points = new vtkPoints();
                vtkCellArray polys = new vtkCellArray();
                vtkFloatArray tcoords = new vtkFloatArray();
                int[][] indices;
                
                int c = 0;
                int maxM = TEXTURE_SIZE;
                if (i==numTexturesHeight-1)
                	maxM = (NearImage.IMAGE_HEIGHT) - i*(TEXTURE_SIZE-1);
                int maxN = TEXTURE_SIZE;
                if (j==numTexturesWidth-1)
                	maxN = (NearImage.IMAGE_WIDTH) - j*(TEXTURE_SIZE-1);

                //points.SetNumberOfPoints(maxM*maxN);
                tcoords.SetNumberOfComponents(2);
                //tcoords.SetNumberOfTuples(maxM*maxN);
                indices = new int[maxM][maxN];
                
                // First add points and texture coordinates to the vtkPoints array
                for (int m=0; m<maxM; ++m)
        			for (int n=0; n<maxN; ++n)
        			{
        				x = nearImage.getX(corner1+m, corner2+n);
        				y = nearImage.getY(corner1+m, corner2+n);
        				z = nearImage.getZ(corner1+m, corner2+n);
        		
        				if (isValidPoint(x, y, z))
        				{
        					s = (float)(n)/(float)(TEXTURE_SIZE-1);
        					t = (float)(m)/(float)(TEXTURE_SIZE-1);

        					//points.SetPoint(c, x, y, z);
        					//tcoords.SetTuple2(c, s, t);
        					points.InsertNextPoint(x, y, z);
        					tcoords.InsertNextTuple2(s, t);
        					
        					indices[m][n] = c;

        					++c;
        				}
        				else
        				{
        					indices[m][n] = -1;
        				}
        			}

                // Now add connectivity information
                for (int m=1; m<maxM; ++m)
        			for (int n=1; n<maxN; ++n)
        			{
        				// Get the indices of the 4 corners of the rectangle to the upper left
        				i0 = indices[m-1][n-1];
        				i1 = indices[m][n-1];
        				i2 = indices[m-1][n];
        				i3 = indices[m][n];

        				// Add upper left triangle
        				if (i0>=0 && i1>=0 && i2>=0)
        				{
        					idList.SetId(0, i0);
        					idList.SetId(1, i1);
        					idList.SetId(2, i2);
        					polys.InsertNextCell(idList);
        				}
        				// Add bottom right triangle
        				if (i2>=0 && i1>=0 && i3>=0)
        				{
        					idList.SetId(0, i2);
        					idList.SetId(1, i1);
        					idList.SetId(2, i3);
        					polys.InsertNextCell(idList);
        				}
        			}

                // Now map the data to 
                piece.SetPoints(points);
                piece.SetPolys(polys);
                piece.GetPointData().SetTCoords(tcoords);

                vtkTexture texture = new vtkTexture();
                texture.InterpolateOn();
                texture.RepeatOff();
                texture.EdgeClampOn();

                vtkImageData imagePiece = nearImage.getSubImage(TEXTURE_SIZE, corner1, corner2);
                //System.out.println("\n\n\n\nnext image piece " + ccc++ + "\n\n");
                //System.out.println(imagePiece.GetDimensions()[0]);
                //System.out.println(imagePiece.GetDimensions()[1]);
                //System.out.println(imagePiece.GetDimensions()[2]);

                
                texture.SetInput(imagePiece);
                
                vtkPolyDataMapper pieceMapper = new vtkPolyDataMapper();
                pieceMapper.SetResolveCoincidentTopologyToPolygonOffset();
                pieceMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, offset);
                pieceMapper.SetInput(piece);
                pieceMapper.Update();
                
                vtkActor pieceActor = new vtkActor();
                pieceActor.SetMapper(pieceMapper);
                pieceActor.SetTexture(texture);
                pieceActor.GetProperty().LightingOff();
                
                renWin.GetRenderer().AddActor(pieceActor);
                
                nearImageActors.get(nearImage).add(pieceActor);
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
			statusBar.setText("Lineament " + lin.id + " mapped on MSI image " + lin.name + " contains " + lin.x.size() + " vertices");
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

    public void propertyChange(PropertyChangeEvent e)
    {
    	if (Properties.LINEAMENT_MODEL_CHANGED.equals(e.getPropertyName()))
    		renWin.Render();
    }

    public LineamentModel getLineamentModel()
    {
    	return lineamentModel;
    }
    
    private boolean isValidPoint(float x, float y, float z)
    {
    	if (x <= NearImage.PDS_NA || y <= NearImage.PDS_NA || z <= NearImage.PDS_NA)
    		return false;
    	else
    		return true;
    }
    
}
