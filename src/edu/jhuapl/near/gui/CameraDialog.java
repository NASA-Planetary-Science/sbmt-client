package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.math.geometry.NotARotationMatrixException;
import org.apache.commons.math.geometry.Rotation;

public class CameraDialog extends JDialog implements ActionListener
{
    private Renderer renderer;
    private JButton applyButton;
    private JButton resetButton;
    private JButton okayButton;
    private JButton cancelButton;
    private JFormattedTextField fovField;
    private String lastGood = "";
    private JLabel distanceLabel;
    private JFormattedTextField distanceField;
    private JLabel kmLabel;

    private void printCameraOrientation()
    {
        double[] position = new double[3];
        double[] cx = new double[3];
        double[] cy = new double[3];
        double[] cz = new double[3];
        double[] viewAngle = new double[1];
        renderer.getCameraOrientation(position, cx, cy, cz, viewAngle);

        try
        {
            double[][] m = {
                    {cx[0], cx[1], cx[2]},
                    {cy[0], cy[1], cy[2]},
                    {cz[0], cz[1], cz[2]}
            };

            Rotation rotation = new Rotation(m, 1.0e-6);

            String str = "Camera position and orientation (quaternion):\n";
            str += position[0] + " " + position[1] + " " + position[2] + "\n";
            str += rotation.getQ0() + " " + rotation.getQ1() + " " + rotation.getQ2() + " " + rotation.getQ3();

            //str += "\n" + m[0][0] + " " + m[0][1] + " " + m[0][2];
            //str += "\n" + m[1][0] + " " + m[1][1] + " " + m[1][2];
            //str += "\n" + m[2][0] + " " + m[2][1] + " " + m[2][2];

            System.out.println(str);
        }
        catch (NotARotationMatrixException e)
        {
            e.printStackTrace();
        }
    }

    public CameraDialog(Renderer renderer)
    {
        this.renderer = renderer;

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", "[][grow][]", "[][][][]"));

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(6);

        JLabel fovLabel = new JLabel("Vertical Field of View");
        fovField = new JFormattedTextField(nf);
        fovField.setPreferredSize(new Dimension(125, 23));
        fovField.setInputVerifier(new DoubleVerifier());
        JLabel degreesLabel = new JLabel("degrees");


        JPanel buttonPanel = new JPanel(new MigLayout());
        applyButton = new JButton("Apply");
        applyButton.addActionListener(this);
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        okayButton = new JButton("OK");
        okayButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(applyButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(okayButton);
        buttonPanel.add(cancelButton);

        panel.add(fovLabel, "cell 0 0");
        panel.add(fovField, "cell 1 0,growx");
        panel.add(degreesLabel, "cell 2 0");

        distanceLabel = new JLabel("Distance");
        panel.add(distanceLabel, "cell 0 1,alignx trailing");

        distanceField = new JFormattedTextField(nf);
        panel.add(distanceField, "cell 1 1,growx");

        kmLabel = new JLabel("km");
        panel.add(kmLabel, "cell 2 1");

        panel.add(buttonPanel, "cell 0 2 3 1,alignx right");

        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        getContentPane().add(panel, BorderLayout.CENTER);
        pack();

        printCameraOrientation();
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == applyButton || e.getSource() == okayButton)
        {
            try
            {
                double newFov = Double.parseDouble(fovField.getText());

                renderer.setCameraViewAngle(newFov);

                // Reset the text field in case the requested fov change was not
                // fully fulfilled (e.g. was negative)
                double fov = renderer.getCameraViewAngle();
                fovField.setValue(fov);

                double distance = Double.parseDouble(distanceField.getText());

                renderer.setCameraDistance(distance);
            }
            catch (NumberFormatException ex)
            {
                return;
            }
        }
        else if (e.getSource() == resetButton)
        {
            renderer.resetToDefaultCameraViewAngle();

            // Reset the text field in case the requested offset change was not
            // fully fulfilled.
            double fov = renderer.getCameraViewAngle();
            fovField.setValue(fov);
        }

        if (e.getSource() == okayButton || e.getSource() == cancelButton)
        {
            super.setVisible(false);
        }
    }

    public void setVisible(boolean b)
    {
        setTitle("Camera");

        fovField.setValue(renderer.getCameraViewAngle());
        lastGood = fovField.getText();

        distanceField.setValue(renderer.getCameraDistance());

        super.setVisible(b);
    }

    private class DoubleVerifier extends InputVerifier
    {
        public boolean verify(JComponent input)
        {
            JTextField text = (JTextField)input;
            String value = text.getText().trim();
            try
            {
                double v = Double.parseDouble(value);
                if (v < 0.00000001 || v > 179.0) // These limits are from vtkCamera.cxx
                    throw new NumberFormatException();
                lastGood = value;
            }
            catch (NumberFormatException e)
            {
                text.setText(lastGood);
                return false;
            }
            return true;
        }
    }
}
