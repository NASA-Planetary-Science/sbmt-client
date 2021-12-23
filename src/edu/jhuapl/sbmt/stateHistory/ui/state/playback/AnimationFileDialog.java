package edu.jhuapl.sbmt.stateHistory.ui.state.playback;

import java.awt.GridLayout;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author steelrj1
 *
 */
public class AnimationFileDialog extends JFileChooser
{
	/**
	 *
	 */
	private JLabel frames, fromLabel, toLabel, durationLabel, durationValue;
    /**
     *
     */
    private JSpinner numFrames;

	/**
	 * @param start
	 * @param end
	 */
	public AnimationFileDialog(String start, String end)
	{
		super();
		frames = new JLabel("Number of Frames: ");
        fromLabel = new JLabel("Start: " + start);
        toLabel = new JLabel("End: " + end);
        numFrames = new JSpinner();
        durationLabel = new JLabel("Total duration (30fps): ");
        durationValue = new JLabel("" + 100.0/30.0);
        SpinnerNumberModel model = new SpinnerNumberModel(100, 1, null, 1);
        model.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				durationValue.setText("" + 1.0*(Integer)model.getValue()/30.0);
			}
		});
        numFrames.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				durationValue.setText("" + 1.0*(Integer)model.getValue()/30.0);
			}
		});
        numFrames.setModel(model);
        setDialogTitle("Export Movie Frames as MP4");
        setDialogType(JFileChooser.SAVE_DIALOG);
        setApproveButtonText("Export");

        JPanel comp1 = (JPanel) getComponent(4);
        JPanel time = (JPanel) comp1.getComponent(0);

        time.setLayout(new GridLayout(0, 2));
        time.add(frames);
        time.add(numFrames);
        time.add(durationLabel);
        time.add(durationValue);
        time.add(fromLabel);
        time.add(toLabel);

	}

	/**
	 * @return
	 */
	public JSpinner getNumFrames()
	{
		return numFrames;
	}

}
