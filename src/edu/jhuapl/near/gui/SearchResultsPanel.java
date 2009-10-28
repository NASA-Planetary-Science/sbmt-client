package edu.jhuapl.near.gui;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

public class SearchResultsPanel extends JPanel implements 
			MouseListener
{
	private static class PopupMenu extends JPopupMenu 
	{
		//private Component invoker;
		
		public PopupMenu()
		{        
			JMenuItem mi; 
			mi = new JMenuItem(new ShowIn3DAction());
			mi.setText("Map image to 3D model of Eros");
			this.add(mi);
			mi = new JMenuItem(new ShowOutlineIn3DAction());
			mi.setText("Map image outline to 3D model of Eros");
			this.add(mi);
			//mi = new JMenuItem(new ShowInfoAction());
			//mi.setText("Show image information");
			//this.add(mi);
		}

		public void show(Component invoker, int x, int y, String imageName)
		{
			System.out.println(imageName);
			super.show(invoker, x, y);
		}
		

		private class ShowIn3DAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e) 
			{
			}
		}
		
		private class ShowOutlineIn3DAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e) 
			{
			}
		}
		
		private class ShowInfoAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e) 
			{
			}
		}
		
	}

    private JList resultList;
    private DefaultListModel resultListModel;
    private PopupMenu popupMenu;


	public SearchResultsPanel()
	{
		popupMenu = new PopupMenu();
		
        resultListModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        resultList = new JList(resultListModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(resultList);

        listScrollPane.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
                                   new TitledBorder("Query Results")));

        add(listScrollPane);

	}
	
	public void setResults(ArrayList<String> results)
	{
    	resultListModel.clear();
    	
    	// add the results to the list
    	for (String str : results)
    	{
    		resultListModel.addElement(str);
    	}

	}

	public void mouseClicked(MouseEvent arg0) 
	{
	}

	public void mouseEntered(MouseEvent arg0) 
	{
	}

	public void mouseExited(MouseEvent arg0) 
	{
	}

	public void mousePressed(MouseEvent e) 
	{
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) 
	{
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) 
	{
        if (e.isPopupTrigger()) 
        {
        	int index = resultList.locationToIndex(e.getPoint());

        	if (index >= 0 && resultList.getCellBounds(index, index).contains(e.getPoint()))
        	{
        		resultList.setSelectedIndex(index);
        		popupMenu.show(e.getComponent(), e.getX(), e.getY(), (String)resultListModel.get(index));
        	}
        }
    }
	
}
