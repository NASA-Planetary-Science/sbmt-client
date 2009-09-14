package edu.jhuapl.near;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;


import nom.tam.fits.*;

import vtk.*;

class ImageGLWidget extends JPanel
{

    private ArrayList<NearImage> nearImages = new ArrayList<NearImage>();
    private LineamentModel lineamentModel;
    private vtkPolyDataReader polyReader;
    private vtkRenderWindowPanel renWin;

    private static final int TEXTURE_SIZE = 128;

    private ArrayList<vtkActor> nearImageActors = new ArrayList<vtkActor>();
    
    public ImageGLWidget(LineamentModel model) 
    {
    	setLayout(new BorderLayout());
    	
        // Create the buttons.
        renWin = new vtkRenderWindowPanel();

        lineamentModel = model;

        polyReader = new vtkPolyDataReader();
        polyReader.SetFileName("src/edu/jhuapl/near/data/Eros_Dec2006_0.vtk");
        polyReader.Update();
        
        vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
        coneMapper.SetInput(polyReader.GetOutput());

        vtkActor polyActor = new vtkActor();
        //polyActor.GetProperty().SetRepresentationToWireframe();
        //polyActor.GetProperty().SetOpacity(0.0);
        polyActor.SetMapper(coneMapper);

        renWin.GetRenderer().AddActor(polyActor);

        vtkInteractorStyleTrackballCamera style =
            new vtkInteractorStyleTrackballCamera();
        renWin.setInteractorStyle(style);
          
        add(renWin, BorderLayout.CENTER);

    }

    public void setImages(ArrayList<File> files) throws FitsException, IOException
    {
    	// First remove all actors
    	for (vtkActor act : this.nearImageActors)
    		renWin.GetRenderer().RemoveActor(act);
    	
    	nearImageActors.clear();

    	int c = 1;
    	for (File file : files)
    	{
        	NearImage image = new NearImage(file.getAbsolutePath());

        	nearImages.add(image);
    	
        	// Now texture map this image onto the Eros model.
        	mapImage(image);
    	}
    }
    
    private void mapImage(NearImage nearImage)
    {
        int numTexturesWidth = (int)Math.ceil((double)(NearImage.IMAGE_WIDTH-1) / (double)(TEXTURE_SIZE-1));
        int numTexturesHeight = (int)Math.ceil((double)(NearImage.IMAGE_HEIGHT-1) / (double)(TEXTURE_SIZE-1));
        int totalNumTextures = numTexturesWidth * numTexturesHeight;
        
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
                	maxM = (NearImage.IMAGE_HEIGHT) - (numTexturesHeight-1)*(TEXTURE_SIZE);
                int maxN = TEXTURE_SIZE;
                if (j==numTexturesWidth-1)
                	maxN = (NearImage.IMAGE_WIDTH) - (numTexturesWidth-1)*(TEXTURE_SIZE);

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
                
                vtkImageData imagePiece = nearImage.getSubImage2(TEXTURE_SIZE, corner1, corner2);
                System.out.println("\n\n\n\nnext image piece\n\n");
                System.out.println(imagePiece);
                
                texture.SetInput(imagePiece);
                
                vtkPolyDataMapper pieceMapper = new vtkPolyDataMapper();
                pieceMapper.SetResolveCoincidentTopologyToPolygonOffset();
                pieceMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, -1.0);
                pieceMapper.SetInput(piece);
                pieceMapper.Update();
                
                vtkActor pieceActor = new vtkActor();
                pieceActor.SetMapper(pieceMapper);
                pieceActor.SetTexture(texture);
                
                renWin.GetRenderer().AddActor(pieceActor);
                
                nearImageActors.add(pieceActor);
        	}
    }
    
    private boolean isValidPoint(float x, float y, float z)
    {
    	if (x <= NearImage.PDS_NA || y <= NearImage.PDS_NA || z <= NearImage.PDS_NA)
    		return false;
    	else
    		return true;
    }
}

