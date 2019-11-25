package edu.jhuapl.sbmt.stateHistory.ui;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class AnimationFileDialog extends JFileChooser
{
	private JLabel frames, fromLabel, toLabel;
    private JSpinner numFrames;
    private JFileChooser chooser;

	public AnimationFileDialog(String start, String end)
	{
		frames = new JLabel("Number of Frames: ");
        fromLabel = new JLabel("Start: " + start);
        toLabel = new JLabel("End: " + end);
        numFrames = new JSpinner();
        SpinnerNumberModel model = new SpinnerNumberModel(100, 1, null, 1);
        numFrames.setModel(model);
        setDialogTitle("Export Movie Frames as PNG");
        setDialogType(JFileChooser.SAVE_DIALOG);
        setApproveButtonText("Export");

        JPanel comp1 = (JPanel) chooser.getComponent(4);
        JPanel time = (JPanel) comp1.getComponent(0);

        time.setLayout(new GridLayout(0, 2));
        time.add(frames);
        time.add(numFrames);
        time.add(fromLabel);
        time.add(toLabel);

	}

	public int showSaveDialog(Component component)
	{
        return chooser.showSaveDialog(JOptionPane.getFrameForComponent(component));
	}

	public JSpinner getNumFrames()
	{
		return numFrames;
	}

}
