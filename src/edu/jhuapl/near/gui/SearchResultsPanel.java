package edu.jhuapl.near.gui;

import javax.swing.*;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.*;

public class SearchResultsPanel extends JPanel implements 
			MouseListener, ActionListener
{
	private static class PopupMenu extends JPopupMenu 
	{
		//private Component invoker;
	    private ModelManager modelManager;
		String currentImage;
	    
		public PopupMenu(ModelManager modelManager)
		{
	    	this.modelManager = modelManager;

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
			currentImage = imageName;
			super.show(invoker, x, y);
		}
		

		private class ShowIn3DAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e) 
			{
				NearImageCollection model = (NearImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
				try 
				{
					model.addImage(currentImage);
				} 
				catch (FitsException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		private class ShowOutlineIn3DAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent e) 
			{
				MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
				try 
				{
					model.addBoundary(currentImage);
				} 
				catch (FitsException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		//private class ShowInfoAction extends AbstractAction
		//{
		//	public void actionPerformed(ActionEvent e) 
		//	{
		//	}
		//}
		
	}

    private JList resultList;
    private DefaultListModel resultListModel;
    private PopupMenu popupMenu;
    private ArrayList<String> rawResults;
    private JLabel label;
    private JCheckBox showBoundariesCheckBox;
    private JButton nextButton;
    private JButton prevButton;
    private JComboBox numberOfBoundariesComboBox;

	public SearchResultsPanel(ModelManager modelManager)
	{
		super(new BorderLayout());
				
		popupMenu = new PopupMenu(modelManager);

		label = new JLabel(" ");

        resultListModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        resultList = new JList(resultListModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(resultList);

        //listScrollPane.setBorder(
        //       new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
        //                           new TitledBorder("Query Results")));

        add(label, BorderLayout.NORTH);
        add(listScrollPane, BorderLayout.CENTER);

        JPanel resultControlsPane = new JPanel();
        
        showBoundariesCheckBox = new JCheckBox();
        showBoundariesCheckBox.setText("Show Image Boundaries");
        showBoundariesCheckBox.setSelected(true);

        resultControlsPane.add(showBoundariesCheckBox);
        
        nextButton = new JButton(">");
        nextButton.setActionCommand(">");
        nextButton.addActionListener(this);
        nextButton.setEnabled(true);

        prevButton = new JButton("<");
        prevButton.setActionCommand("<");
        prevButton.addActionListener(this);
        prevButton.setEnabled(true);

        resultControlsPane.add(prevButton);
        resultControlsPane.add(nextButton);
        
        add(resultControlsPane, BorderLayout.SOUTH);
	}
	
	public void setResults(ArrayList<String> results)
	{
    	resultListModel.clear();
    	rawResults = results;
    	label.setText(results.size() + " images matched");
    	
    	// add the results to the list
    	for (String str : results)
    	{
    		resultListModel.addElement( 
    				str.substring(19, 28) 
    				+ ", day: " + str.substring(6, 9) + "/" + str.substring(1, 5)
    				+ ", type: " + str.substring(10, 16)
    				+ ", filter: " + str.substring(29, 30)
    				);
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
        		popupMenu.show(e.getComponent(), e.getX(), e.getY(), rawResults.get(index));
        	}
        }
    }

	public void actionPerformed(ActionEvent e) 
	{
		
	}
	
}
