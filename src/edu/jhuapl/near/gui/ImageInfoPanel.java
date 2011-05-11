package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import vtk.vtkImageActor;
import vtk.vtkImageData;
import vtk.vtkInteractorStyleImage;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.ImageBoundaryCollection;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;

public class ImageInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
    private vtkEnhancedRenderWindowPanel renWin;
    private ContrastChanger contrastChanger;
    private Image image;
    private ImageCollection imageCollection;
    private ImageBoundaryCollection imageBoundaryCollection;

    public ImageInfoPanel(
            Image image,
            ImageCollection imageCollection,
            ImageBoundaryCollection imageBoundaryCollection)
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.image = image;
        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;

        renWin = new vtkEnhancedRenderWindowPanel();

        vtkInteractorStyleImage style =
            new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        vtkImageData displayedImage = image.getDisplayedImage();

        vtkImageActor actor = new vtkImageActor();
        actor.SetInput(displayedImage);

// for testing backplane generation
//        {
//            vtkImageData plane = new vtkImageData();
//            plane.DeepCopy(displayedImage);
//            float[] bp = image.generateBackplanes();
//            for (int i=0; i<image.getImageHeight(); ++i)
//                for (int j=0; j<image.getImageWidth(); ++j)
//                {
//                    plane.SetScalarComponentFromFloat(j, i, 0, 0, 1000*bp[image.index(j, i, 0)]);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 1, 1000*bp[image.index(j, i, 0)]);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 2, 1000*bp[image.index(j, i, 0)]);
//                }
//            actor.SetInput(plane);
//        }

        renWin.GetRenderer().AddActor(actor);

        renWin.setSize(image.getImageWidth(), image.getImageHeight());

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(renWin, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel,
                BoxLayout.PAGE_AXIS));

        // Add a text box for showing information about the image
        String[] columnNames = {"Property",
                "Value"};

        HashMap<String, String> properties = null;
        Object[][] data = {    {"", ""} };
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

        contrastChanger.setImage(image);

        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(scrollPane);
        bottomPanel.add(contrastChanger);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);

        createMenus();

        // Finally make the frame visible
        String name = new File(image.getFitFileFullPath()).getName();
        setTitle("Image " + name + " Properties");
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
         * The following is a bit of a hack. We want to reuse the PopupMenu
         * class, but instead of having a right-click popup menu, we want instead to use
         * it as an actual menu in a menu bar. Therefore we simply grab the menu items
         * from that class and put these in our new JMenu.
         */
        ImagePopupMenu imagesPopupMenu =
            new ImagePopupMenu(imageCollection, imageBoundaryCollection, null, null, this);

        imagesPopupMenu.setCurrentImage(image.getKey());

        JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        Component[] components = imagesPopupMenu.getComponents();
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
        return image;
    }

    public Model getCollectionModel()
    {
        return imageCollection;
    }

    public void propertyChange(PropertyChangeEvent arg0)
    {
        if (renWin.GetRenderWindow().GetNeverRendered() > 0)
            return;
        renWin.Render();
    }
}
