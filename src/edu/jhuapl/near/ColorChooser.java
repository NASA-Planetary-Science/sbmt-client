package edu.jhuapl.near;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ColorChooser 
{
	private static JColorChooser cc = new JColorChooser();
	private static Color chosenColor = null;

    static public Color showColorChooser(Component parent)
    {
        final JDialog frame = new JDialog();
        frame.setTitle("Color Chooser Dialog");
        frame.setModal(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        cc.setBorder(BorderFactory.createTitledBorder("Choose Color"));
        panel.add(cc);

        chosenColor = null;

        JPanel buttonPanel = new JPanel();

        JButton okButton = new JButton();
        okButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    chosenColor = cc.getColor();
                    frame.dispose();
                }
            });
        okButton.setEnabled(true);
        okButton.setText("OK");

        buttonPanel.add(okButton);

        JButton cancelButton = new JButton();
        cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    frame.dispose();
                }
            });
        cancelButton.setEnabled(true);
        cancelButton.setText("Cancel");

        buttonPanel.add(cancelButton);

        panel.add(buttonPanel);
        frame.setContentPane(panel);

        //Display the window.
        frame.pack();
        frame.setVisible(true);

        return chosenColor;
    }
}
