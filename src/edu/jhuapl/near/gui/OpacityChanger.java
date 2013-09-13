package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import edu.jhuapl.near.model.Image;

public class OpacityChanger extends JDialog implements ChangeListener
{
    private JLabel opacityLabel;
    private JSpinner imageMapOpacitySpinner;
    private JButton btnNewButton;
    private Image image;

    public OpacityChanger(Image image)
    {
        this.image = image;

        opacityLabel = new JLabel("Opacity");
        imageMapOpacitySpinner = new JSpinner(new SpinnerNumberModel(image.getImageOpacity(), 0.0, 1.0, 0.1));
        imageMapOpacitySpinner.setEditor(new JSpinner.NumberEditor(imageMapOpacitySpinner, "0.00"));
        imageMapOpacitySpinner.setPreferredSize(new Dimension(80, 21));
        imageMapOpacitySpinner.addChangeListener(this);

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", "[]", "[][][]"));

        panel.add(opacityLabel, "cell 0 0");
        panel.add(imageMapOpacitySpinner, "cell 0 0");

        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        getContentPane().add(panel, BorderLayout.CENTER);

        btnNewButton = new JButton("Close");
        btnNewButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });
        panel.add(btnNewButton, "cell 0 1,alignx center");
        pack();
    }

    public void stateChanged(ChangeEvent e)
    {
        double val = (Double)imageMapOpacitySpinner.getValue();
        image.setImageOpacity(val);
    }

}
