package edu.jhuapl.near;

import java.util.*;

import javax.swing.*;

import nom.tam.fits.FitsException;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;


public class ControlPanel extends JPanel implements ListSelectionListener
{
//    private JButton iedSelectButton;
//    private JTextField iedDatabaseTextField;
    private ImageGLWidget viewer;
	
    private JList list;
    private DefaultListModel listModel;

    private static final String addString = "Add...";
    private static final String deleteString = "Delete";
    private static final String upString = "Up";
    private static final String downString = "Down";
    private JButton addButton;
    private JButton deleteButton;
    private JButton upButton;
    private JButton downButton;

    class DeleteListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {
            //This method can be called only if
            //there's a valid selection
            //so go ahead and remove whatever's selected.
            int index = list.getSelectedIndex();
            listModel.remove(index);

            int size = listModel.getSize();

            if (size == 0) 
            { 
            	//Nobody's left, disable firing.
                addButton.setEnabled(false);

            } 
            else 
            { //Select an index.
                if (index == listModel.getSize()) 
                {
                    //removed item in last position
                    index--;
                }

                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }
    }

    class AddListener implements ActionListener 
    {
    	JPanel panel;
    	AddListener(JPanel panel)
    	{
    		this.panel = panel;
    	}
        public void actionPerformed(ActionEvent e) 
        {
        	File file = FITFileChooser.showOpenDialog(panel, "Select FIT File");
        	System.out.println(file);
        	
        	ArrayList<File> fileList = new ArrayList<File>();
        	fileList.add(file);
        	
        	if (file != null)
        	{
        		try {
        			viewer.setImages(fileList);
    			} catch (FitsException e2) {
    				// TODO Auto-generated catch block
    				e2.printStackTrace();
    			} catch (IOException e2) {
    				// TODO Auto-generated catch block
    				e2.printStackTrace();
    			}
        	}
        }
    }
    
    class UpListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {
        }
    }
    
    class DownListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {
        }
    }
    
	public ControlPanel(ImageGLWidget viewer)
	{
		super(new BorderLayout());

		this.viewer = viewer;

        listModel = new DefaultListModel();
        //listModel.addElement("Debbie Scott");
        //listModel.addElement("Scott Hommel");
        //listModel.addElement("Sharon Zakhour");

        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        //list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);

        addButton = new JButton(addString);
        AddListener addListener = new AddListener(this);
        addButton.setActionCommand(addString);
        addButton.addActionListener(addListener);
        addButton.setEnabled(true);

        deleteButton = new JButton(deleteString);
        DeleteListener deleteListener = new DeleteListener();
        deleteButton.setActionCommand(deleteString);
        deleteButton.addActionListener(deleteListener);
        deleteButton.setEnabled(false);

        upButton = new JButton(upString);
        UpListener upListener = new UpListener();
        upButton.setActionCommand(upString);
        upButton.addActionListener(upListener);
        upButton.setEnabled(false);

        downButton = new JButton(downString);
        DownListener downListener = new DownListener();
        downButton.setActionCommand(downString);
        downButton.addActionListener(downListener);
        downButton.setEnabled(false);


        //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,
                                           BoxLayout.LINE_AXIS));
        buttonPane.add(addButton);
        buttonPane.add(deleteButton);
        buttonPane.add(upButton);
        buttonPane.add(downButton);
        //buttonPane.add(Box.createHorizontalStrut(5));
        //buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        //buttonPane.add(Box.createHorizontalStrut(5));
        //buttonPane.add(employeeName);
        //buttonPane.add(hireButton);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(buttonPane, BorderLayout.PAGE_START);
        add(listScrollPane, BorderLayout.CENTER);

//        this.setLayout(new GridBagLayout());
//        
//    	GridBagConstraints c = new GridBagConstraints();
//        c.gridwidth = GridBagConstraints.REMAINDER;
//    	c.fill = GridBagConstraints.BOTH;
//    	c.weightx = 0.0;
//    	c.weighty = 0.0;
//
//    	JPanel fitFileLoader = new JPanel();
//    	
//    	fitFileLoader.setBorder(
//        		new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
//                                   new TitledBorder("Databases")));
//
//        //JPanel loadDatabasesPanel = new JPanel();
//        //loadDatabasesPanel.setLayout(new BoxLayout(loadDatabasesPanel, BoxLayout.Y_AXIS));
//        JPanel loadDatabasesPanel = new JPanel(new GridBagLayout());
//
//        this.iedSelectButton = new JButton("Select IED Database...");
//        this.iedSelectButton.setEnabled(true);
//        this.iedSelectButton.addActionListener(this);
//
//        this.iedDatabaseTextField = new JTextField();
//        this.iedDatabaseTextField.setEnabled(true);
//        this.iedDatabaseTextField.setEditable(true);
//        this.iedDatabaseTextField.setPreferredSize(new java.awt.Dimension(150, 22));
//        
//        GridBagConstraints c2 = new GridBagConstraints();
//        c2.gridx = 0;
//        c2.gridy = 0;
//        c2.gridwidth = 1;
//        c2.gridheight = 1;
//        c2.fill = GridBagConstraints.HORIZONTAL;
//        
//        loadDatabasesPanel.add(this.iedSelectButton, c2);
//        c2.gridy = 1;
//        //loadDatabasesPanel.add(this.cacheSelectButton, c2);
//
//        c2.gridx = 1;
//        c2.gridy = 0;
//        c2.gridwidth = 2;
//        c2.gridheight = 1;
//        loadDatabasesPanel.add(this.iedDatabaseTextField, c2);
//        c2.gridy = 1;
//        //loadDatabasesPanel.add(this.cacheDatabaseTextField, c2);
//
//        fitFileLoader.add(loadDatabasesPanel);
	}

    //This method is required by ListSelectionListener.
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {

            if (list.getSelectedIndex() == -1) {
            //No selection, disable fire button.
                deleteButton.setEnabled(false);

            } else {
            //Selection, enable the fire button.
                deleteButton.setEnabled(true);
            }
        }
    }


    public void actionPerformed(ActionEvent actionEvent)
    {
    	File file = FITFileChooser.showOpenDialog(this, "Select FIT File");
    	
    	if (file != null)
    	{
//    		try {
//				//viewer.addImage(file);
//			} catch (FitsException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
    	}
    }

}
