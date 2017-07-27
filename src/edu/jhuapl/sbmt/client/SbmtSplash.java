package edu.jhuapl.sbmt.client;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import edu.cmu.relativelayout.Binding;
import edu.cmu.relativelayout.BindingFactory;
import edu.cmu.relativelayout.RelativeConstraints;
import edu.cmu.relativelayout.RelativeLayout;

//TODO: Convert this to extend off SAASplash once the saaGUI support library gets in place
public class SbmtSplash extends JWindow
{
    public SbmtSplash(String imageDir, String splashImageName)
    {
        BufferedImage splashImage;

        try
        {
            splashImage = ImageIO.read(new File(imageDir, splashImageName));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            splashImage = new BufferedImage(500, 1, BufferedImage.TYPE_INT_ARGB);
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (screenSize.width - splashImage.getWidth()*2) / 2;
        int y = (screenSize.height - splashImage.getHeight()) / 2;

        setBounds(x, y, splashImage.getWidth(), splashImage.getHeight());

        JPanel content = new JPanel();
        RelativeLayout layout = new RelativeLayout();
        content.setLayout(layout);

        BindingFactory bf = new BindingFactory();
        Binding leftEdge = bf.leftEdge();
        Binding topEdge = bf.topEdge();
        Binding bottomEdge = bf.bottomEdge();
        Binding rightEdge = bf.rightEdge();


        JLabel splashImageLabel = new JLabel(new ImageIcon(splashImage));

        RelativeConstraints splashImageConstraints = new RelativeConstraints();
        splashImageConstraints.addBinding(leftEdge);
        splashImageConstraints.addBinding(rightEdge);
        splashImageConstraints.addBinding(bottomEdge);
        splashImageConstraints.addBinding(topEdge);

        content.add(splashImageLabel, splashImageConstraints);

        setContentPane(content);
    }

    public static void main(String[] args)
    {
//        SbmtSplash splash = new SbmtSplash();
//        splash.setVisible(true);
    }
}
