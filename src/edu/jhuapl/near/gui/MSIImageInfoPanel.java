package edu.jhuapl.near.gui;

import java.awt.BorderLayout;

import javax.swing.*;

import edu.jhuapl.near.model.NearImage;

import vtk.*;

public class MSIImageInfoPanel extends JFrame
{
	private vtkRenderWindowPanel renWin;
    private ContrastChanger contrastChanger;
	
	public MSIImageInfoPanel(NearImage image)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		renWin = new vtkRenderWindowPanel();

        vtkInteractorStyleImage style =
            new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        vtkImageData displayedImage = image.getDisplayedImage();
        
        vtkImageActor actor = new vtkImageActor();                                                    
        actor.SetInput(displayedImage);                     
        
        renWin.GetRenderer().AddActor(actor);
        
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,
        		BoxLayout.PAGE_AXIS));
		
		panel.add(renWin);
		
		// Add a text box for showing information about the image
		
        contrastChanger = new ContrastChanger();

        panel.add(contrastChanger);
        
        add(panel, BorderLayout.CENTER);
	}
}
