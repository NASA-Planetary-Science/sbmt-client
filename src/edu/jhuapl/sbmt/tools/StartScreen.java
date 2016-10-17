package edu.jhuapl.sbmt.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import edu.jhuapl.saavtk.util.Configuration;

class StartScreen extends JWindow
{
    public StartScreen()
    {
        super();
        System.out.println(Configuration.getApplicationDataDir());
        JLabel image = new JLabel(new ImageIcon(Configuration.getApplicationDataDir()+File.separator+"Splash2.png"));//"C:\\Users\\belyamu1\\workspace\\sbmt\\Splash.png"));
        getContentPane().add(image, BorderLayout.CENTER);
        pack();

        Dimension size1 = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((size1.width-870)/2,(size1.height-800)/2);

        setVisible(true);

        final Runnable close = new Runnable()
        {
            public void run()
            {
                setVisible(false);
                dispose();
            }
        };
        Runnable open = new Runnable()
        {
            public void run()
            {

                try
                {
                    Thread.sleep(5500);
                    SwingUtilities.invokeLater(close);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(open, "Start");
        thread.start();
    }
}