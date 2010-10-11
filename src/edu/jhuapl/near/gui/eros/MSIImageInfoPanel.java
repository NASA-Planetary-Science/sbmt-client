package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

import edu.jhuapl.near.gui.ModelInfoWindow;
import edu.jhuapl.near.gui.vtkEnhancedRenderWindowPanel;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.popupmenus.eros.MSIPopupMenu;

import vtk.*;

public class MSIImageInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
	private vtkEnhancedRenderWindowPanel renWin;
    private ContrastChanger contrastChanger;
	private MSIImage msiImage;
	private ModelManager modelManager;
	
	public MSIImageInfoPanel(MSIImage image, ModelManager modelManager)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		msiImage = image;
		this.modelManager = modelManager;
		
		image.addPropertyChangeListener(this);
		
		renWin = new vtkEnhancedRenderWindowPanel();
		
        vtkInteractorStyleImage style =
            new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        vtkImageData displayedImage = image.getDisplayedImage();
        
        vtkImageActor actor = new vtkImageActor();
        //actor.SetDisplayExtent(0, MSIImage.IMAGE_WIDTH-1, 0, MSIImage.IMAGE_HEIGHT-1, 0, 0);
        actor.SetInput(displayedImage);                     

// for testing backplane generation        
//        {
//        	vtkImageData plane = new vtkImageData();
//        	plane.DeepCopy(displayedImage);
//        	float[] bp = image.generateBackplanes();
//        	for (int i=0; i<MSIImage.IMAGE_HEIGHT; ++i)
//            	for (int j=0; j<MSIImage.IMAGE_WIDTH; ++j)
//            	{
//            		plane.SetScalarComponentFromFloat(j, i, 0, 0, 1000*bp[image.index(j, i, 0)]);
//            		plane.SetScalarComponentFromFloat(j, i, 0, 1, 1000*bp[image.index(j, i, 0)]);
//            		plane.SetScalarComponentFromFloat(j, i, 0, 2, 1000*bp[image.index(j, i, 0)]);
//            	}
//        	actor.SetInput(plane);
//        }
        
        renWin.GetRenderer().AddActor(actor);
        
		//renWin.GetRenderWindow().SetSize(MSIImage.IMAGE_WIDTH, MSIImage.IMAGE_HEIGHT);
		renWin.setSize(MSIImage.IMAGE_WIDTH, MSIImage.IMAGE_HEIGHT);

        JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(renWin, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel,
        		BoxLayout.PAGE_AXIS));

		// Add a text box for showing information about the image
		String[] columnNames = {"Property",
                "Value"};

		HashMap<String, String> properties = null;
		Object[][] data = {	{"", ""} };
		try 
		{
			properties = image.getProperties();
			TreeMap<String, String> sortedProperties = new TreeMap<String, String>(properties);
			int size = properties.size();
			data = new Object[size][2];
			
			int i=0;
			for (String key : sortedProperties.keySet())
			{
				data[i][0] = key;
				data[i][1] = sortedProperties.get(key);
				
				++i;
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		JTable table = new JTable(data, columnNames)
		{
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};

		table.setBorder(BorderFactory.createTitledBorder(""));
		table.setPreferredScrollableViewportSize(new Dimension(500, 130));

		JScrollPane scrollPane = new JScrollPane(table);
		
		// Add the contrast changer
        contrastChanger = new ContrastChanger();

        contrastChanger.setMSIImage(image);
        
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(scrollPane);
        bottomPanel.add(contrastChanger);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(panel, BorderLayout.CENTER);
        
        createMenus();
        
        // Finally make the frame visible
        setTitle("MSI Image " + image.getName() + " Properties");
        //this.set
        pack();
        setVisible(true);
	}

    private void createMenus()
    {
    	JMenuBar menuBar = new JMenuBar();

    	JMenu fileMenu = new JMenu("File");
        JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
        {
            public void actionPerformed(ActionEvent e)
            {
                renWin.saveToFile();
            }
        });
        fileMenu.add(mi);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

    	/**
    	 * The following is a bit of a hack. We want to reuse the MSIPopupMenu
    	 * class, but instead of having a right-click popup menu, we want instead to use
    	 * it as an actual menu in a menu bar. Therefore we simply grab the menu items
    	 * from that class and put these in our new JMenu.
    	 */
    	MSIPopupMenu msiImagesPopupMenu = 
			new MSIPopupMenu(modelManager, null, null, this);
    	
    	msiImagesPopupMenu.setCurrentImage(msiImage.getKey());
    	
    	JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        Component[] components = msiImagesPopupMenu.getComponents();
        for (Component item : components)
        {
        	if (item instanceof JMenuItem)
        		menu.add(item);
        }
        
        menuBar.add(menu);
        
        setJMenuBar(menuBar);
    }

	public Model getModel()
	{
		return msiImage;
	}
	
	public void propertyChange(PropertyChangeEvent arg0) 
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
			return;
		renWin.Render();
	}
}
