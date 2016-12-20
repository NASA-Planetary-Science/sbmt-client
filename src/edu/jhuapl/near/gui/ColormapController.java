package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import edu.jhuapl.near.colormap.Colormap;
import edu.jhuapl.near.colormap.Colormaps;

public class ColormapController extends JPanel implements ActionListener
{
    PropertyChangeSupport pcs=new PropertyChangeSupport(this);
    public static final String colormapChanged="Colormap changed";

    Colormap colormap=Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
    JComboBox colormapComboBox=new JComboBox<>();
    JCheckBox logScaleCheckbox=new JCheckBox("Log scale");
    JTextField lowTextField=new JTextField("0");
    JTextField highTextField=new JTextField("1");
    JTextField nLevelsTextField=new JTextField("32");

    JPanel panel1=new JPanel();
    JPanel panel2=new JPanel();
    JPanel panel3=new JPanel();

    public ColormapController()
    {
        setLayout(new BorderLayout());
        colormapComboBox.setRenderer(new ColormapComboBoxRenderer());
        for (String str : Colormaps.getAllBuiltInColormapNames())
        {
            Colormap cmap=Colormaps.getNewInstanceOfBuiltInColormap(str);
            colormapComboBox.addItem(cmap);
            if (cmap.getName().equals(Colormaps.getDefaultColormapName()))
                colormapComboBox.setSelectedItem(cmap);
        }
        //
        colormap=Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
        //
        panel1.add(colormapComboBox);
        panel2.add(new JLabel("Min"));
        panel2.add(lowTextField);
        panel2.add(new JLabel("Max"));
        panel2.add(highTextField);
        panel3.add(logScaleCheckbox);
        panel3.add(new JLabel("nLev"));
        panel3.add(nLevelsTextField);
        //
        this.add(panel1,BorderLayout.NORTH);
        this.add(panel2,BorderLayout.CENTER);
        this.add(panel3,BorderLayout.SOUTH);
        //

        colormapComboBox.addActionListener(this);
        nLevelsTextField.addActionListener(this);
        lowTextField.addActionListener(this);
        highTextField.addActionListener(this);
        logScaleCheckbox.addActionListener(this);

        refresh();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        colormapComboBox.setEnabled(enabled);
        lowTextField.setEnabled(enabled);
        highTextField.setEnabled(enabled);
        logScaleCheckbox.setEnabled(enabled);
        nLevelsTextField.setEnabled(enabled);
    }

    public void setMinMax(double min, double max)
    {
        lowTextField.setText(String.valueOf(min));
        highTextField.setText(String.valueOf(max));
        refresh();
    }

    public Colormap getColormap()
    {
        return colormap;
    }

    public boolean isLogScale()
    {
        return logScaleCheckbox.isSelected();
    }

    protected class ColormapComboBoxRenderer extends JLabel implements ListCellRenderer
    {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus)
        {
            if (isSelected)
            {
                setBackground(Color.DARK_GRAY);
                setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }
            else
            {
                setBackground(list.getBackground());
                setBorder(null);
            }

            setIcon(createIcon((Colormap)value));
            setText(((Colormap)value).getName());
            return this;
        }

    }

    private static ImageIcon createIcon(Colormap cmap)
    {
        int w=100;
        int h=30;
        cmap.setRangeMin(0);
        cmap.setRangeMax(1);
        BufferedImage image=new BufferedImage(w, h, java.awt.color.ColorSpace.TYPE_RGB);
        for (int i=0; i<w; i++)
        {
            double val=(double)i/(double)(image.getWidth()-1);
            for (int j=0; j<h; j++)
                image.setRGB(i, j, cmap.getColor(val).getRGB());
        }
        return new ImageIcon(image);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(colormapComboBox))
        {
            String name=((Colormap)colormapComboBox.getSelectedItem()).getName();
            colormap=Colormaps.getNewInstanceOfBuiltInColormap(name);
        }
        refresh();
        pcs.firePropertyChange(colormapChanged, null, null);
    }

    public void refresh()
    {
        if (colormap!=null)
        {
            colormap.setLogScale(logScaleCheckbox.isSelected());
            colormap.setRangeMin(Double.valueOf(lowTextField.getText()));
            colormap.setRangeMax(Double.valueOf(highTextField.getText()));
            colormap.setNumberOfLevels(Integer.valueOf(nLevelsTextField.getText()));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);

    }

    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }



}
