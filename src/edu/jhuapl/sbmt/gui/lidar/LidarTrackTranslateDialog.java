package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.GNumberField;
import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.Track;

import net.miginfocom.swing.MigLayout;

/**
 * Dialog which allows the user to specify the translation vector for lidar
 * Tracks.
 * <P>
 * The dialog has the following action buttons:
 * <UL>
 * <LI>Apply: Apply user specified input.
 * <LI>Reset: Reset user input to the zero vector and apply the changes.
 * <LI>Close: Dismiss the dialog.
 * </UL>
 */
public class LidarTrackTranslateDialog extends JDialog implements ActionListener
{
	// Reference vars
	private LidarSearchDataCollection refModel;

	// GUI vars
	private JButton applyB;
	private JButton resetB;
	private JButton closeB;
	private GNumberField xTranslateNF;
	private GNumberField yTranslateNF;
	private GNumberField zTranslateNF;

	// State vars
	private ImmutableList<Track> trackL;

	/**
	 * Standard Constructor
	 */
	public LidarTrackTranslateDialog(Component aParent, LidarSearchDataCollection aModel)
	{
		super(JOptionPane.getFrameForComponent(aParent));

		refModel = aModel;

		trackL = ImmutableList.of();

		formGui();
		pack();
		setLocationRelativeTo(aParent);
	}

	/**
	 * Sets in the list of Tracks that will need to be updated.
	 */
	public void setTracks(List<Track> aTrackL)
	{
		trackL = ImmutableList.copyOf(aTrackL);

		Vector3D tmpVect = refModel.getTranslation(aTrackL.get(0));

		// Check that all translation vectors are equal
		boolean tmpBool = true;
		for (Track aTrack : aTrackL)
		{
			Vector3D evalVect = refModel.getTranslation(aTrack);
			tmpBool &= tmpVect.equals(evalVect);
		}

		// Install the appropriate vector
		if (tmpBool == false)
			tmpVect = Vector3D.NaN;
		setInputVector(tmpVect);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == applyB)
			doActionApply();
		if (source == resetB)
			doActionReset();
		if (source == closeB)
			setVisible(false);

		updateGui();
	}

	/**
	 * Helper method that handles the apply action.
	 */
	private void doActionApply()
	{
		double xVal = xTranslateNF.getValue();
		double yVal = yTranslateNF.getValue();
		double zVal = zTranslateNF.getValue();
		Vector3D tmpVect = new Vector3D(xVal, yVal, zVal);
		refModel.setTranslation(trackL, tmpVect);
	}

	/**
	 * Helper method that handles the reset action.
	 */
	private void doActionReset()
	{
		Vector3D tmpVect = Vector3D.ZERO;
		setInputVector(tmpVect);
		refModel.setTranslation(trackL, tmpVect);
	}

	/**
	 * Helper method that forms the GUI.
	 */
	private void formGui()
	{
		setTitle("Translate Lidar Tracks");
		setLayout(new MigLayout());

		// Input area: x, y, z
		xTranslateNF = new GNumberField(this);
		yTranslateNF = new GNumberField(this);
		zTranslateNF = new GNumberField(this);
		add(new JLabel("x-translate:"), "");
		add(xTranslateNF, "growx,span,wrap");
		add(new JLabel("y-translate:"), "");
		add(yTranslateNF, "growx,span,wrap");
		add(new JLabel("z-translate:"), "");
		add(zTranslateNF, "growx,span,wrap");

		// Action area: Apply, Reset, Close
		applyB = GuiUtil.formButton(this, "Apply");
		resetB = GuiUtil.formButton(this, "Reset");
		closeB = GuiUtil.formButton(this, "Close");
		add(applyB, "span,split,align right");
		add(resetB);
		add(closeB);
	}

	/**
	 * Sets our internal UI input fields to reflect the specified Vector.
	 */
	private void setInputVector(Vector3D aVect)
	{
		xTranslateNF.setValue(aVect.getX());
		yTranslateNF.setValue(aVect.getY());
		zTranslateNF.setValue(aVect.getZ());
		updateGui();
	}

	/**
	 * Helper method to keep the GUI synchornized
	 */
	private void updateGui()
	{
		boolean tmpBool = xTranslateNF.isValidInput();
		tmpBool &= yTranslateNF.isValidInput();
		tmpBool &= zTranslateNF.isValidInput();
		applyB.setEnabled(tmpBool);
	}

}