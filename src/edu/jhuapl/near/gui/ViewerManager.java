package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.jhuapl.near.gui.deimos.DeimosViewer;
import edu.jhuapl.near.gui.eros.ErosViewer;
import edu.jhuapl.near.gui.itokawa.ItokawaViewer;
import edu.jhuapl.near.gui.vesta.VestaViewer;

public class ViewerManager extends JPanel
{
	private ArrayList<Viewer> viewers = new ArrayList<Viewer>();
	private Viewer currentViewer;
	
	public ViewerManager(StatusBar statusBar)
	{
		super(new CardLayout());
		setBorder(BorderFactory.createEmptyBorder());
		
        viewers.add(new ErosViewer(statusBar));
        viewers.add(new DeimosViewer(statusBar));
        viewers.add(new ItokawaViewer(statusBar));
        viewers.add(new VestaViewer(statusBar));
        
        currentViewer = viewers.get(0);
        
        for (Viewer viewer : viewers)
        	add(viewer, viewer.getName());
	}
	
	public Viewer getCurrentViewer()
	{
		return currentViewer;
	}
	
	public void setCurrentViewer(Viewer viewer)
	{
		// defer initialization of Viewer until we show it.
		viewer.initialize();
		
    	CardLayout cardLayout = (CardLayout)(getLayout());
    	cardLayout.show(this, viewer.getName());

		currentViewer = viewer;
	}
	
	public Viewer getViewer(int i)
	{
		return viewers.get(i);
	}
	
	public int getNumberOfViewers()
	{
		return viewers.size();
	}
}
