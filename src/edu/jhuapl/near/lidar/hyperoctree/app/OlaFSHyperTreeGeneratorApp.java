package edu.jhuapl.near.lidar.hyperoctree.app;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class OlaFSHyperTreeGeneratorApp extends JFrame implements ActionListener
{
    JPanel mainPanel=new JPanel();
    L2FileChooser l2FileChooser=new L2FileChooser();
    FileListPane l2FileListing=new FileListPane();

    public OlaFSHyperTreeGeneratorApp()
    {
        l2FileChooser.addActionListener(this);
        l2FileListing.setPreferredSize(l2FileChooser.getPreferredSize());

        mainPanel.setLayout(new FlowLayout());
        mainPanel.add(l2FileChooser);
        mainPanel.add(l2FileListing);
        this.add(mainPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
            System.out.println("!");
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                OlaFSHyperTreeGeneratorApp app=new OlaFSHyperTreeGeneratorApp();
                app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                app.setSize(1200,800);
                app.setVisible(true);
            }
        });
    }
}
