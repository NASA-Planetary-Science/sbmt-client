package edu.jhuapl.near;

import java.util.*;

import javax.swing.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;


public class ControlPanel extends JTabbedPane
{
    private ImageGLWidget viewer;
	private SearchPanel searchPanel;
	private ErosPanel erosPanel;
	private MSIPanel msiPanel;
	private LineamentPanel lineamentPanel;
    
	public ControlPanel(ImageGLWidget viewer)
	{
		this.viewer = viewer;

		searchPanel = new SearchPanel(viewer);
		erosPanel = new ErosPanel(viewer);
		msiPanel = new MSIPanel(viewer);
		lineamentPanel = new LineamentPanel(viewer);
		
		addTab("Search", searchPanel);
		addTab("Eros", erosPanel);
		addTab("MSI", msiPanel);
		addTab("Lineament", lineamentPanel);
	}
}
