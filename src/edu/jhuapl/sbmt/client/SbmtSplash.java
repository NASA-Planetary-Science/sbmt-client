package edu.jhuapl.sbmt.client;

import java.awt.Dimension;
import java.awt.Toolkit;

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
        ImageIcon splashImage;

        //            splashImage = ImageIO.read(new File(imageDir, splashImageName));
        splashImage = new ImageIcon(getClass().getResource("/edu/jhuapl/sbmt/data/splashLogo.png"));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (screenSize.width - splashImage.getIconWidth()*2) / 2;
        int y = (screenSize.height - splashImage.getIconHeight()) / 2;

        setBounds(x, y, splashImage.getIconWidth(), splashImage.getIconHeight());

        JPanel content = new JPanel();
        RelativeLayout layout = new RelativeLayout();
        content.setLayout(layout);

        BindingFactory bf = new BindingFactory();
        Binding leftEdge = bf.leftEdge();
        Binding topEdge = bf.topEdge();
        Binding bottomEdge = bf.bottomEdge();
        Binding rightEdge = bf.rightEdge();


        JLabel splashImageLabel = new JLabel(splashImage);

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
