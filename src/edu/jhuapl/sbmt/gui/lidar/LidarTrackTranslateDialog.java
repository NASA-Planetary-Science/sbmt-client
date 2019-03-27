package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.GNumberField;
import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.Track;

import net.miginfocom.swing.MigLayout;

/**
 * Dialog which allows the user to specify the translation vector for selected
 * lidar Tracks.
 * <P>
 * The dialog has the following action buttons:
 * <UL>
 * <LI>Apply: Apply user specified input.
 * <LI>Reset: Reset user input to the zero vector and apply the changes.
 * <LI>Close: Dismiss the dialog.
 * </UL>
 */
public class LidarTrackTranslateDialog extends JDialog implements ActionListener, TrackEventListener
{
	// Constants
	private final Color failColor = Color.RED.darker();

	// Reference vars
	private LidarSearchDataCollection refModel;

	// GUI vars
	private JLabel infoL, warnL;
	private JButton applyB;
	private JButton resetB;
	private JButton closeB;
	private GNumberField xTranslateNF;
	private GNumberField yTranslateNF;
	private GNumberField zTranslateNF;

	// State vars
	private Vector3D currVect;

	/**
	 * Standard Constructor
	 */
	public LidarTrackTranslateDialog(Component aParent, LidarSearchDataCollection aModel)
	{
		super(JOptionPane.getFrameForComponent(aParent));

		refModel = aModel;

		currVect = Vector3D.ZERO;

		formGui();
		pack();
		setLocationRelativeTo(aParent);

		// Register for events of interest
		refModel.addListener(this);
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

	@Override
	public void handleTrackEvent(Object aSource, ItemEventType aEventType)
	{
		// Ignore events while we are not displayed
		if (isVisible() == false)
			return;

		if (aEventType == ItemEventType.ItemsMutated)
			doHandleEventTrackMutation();
		if (aEventType == ItemEventType.ItemsSelected)
			doHandleEventTrackSelectionChanged();
	}

	@Override
	public void setVisible(boolean aBool)
	{
		// Update our internal UI if we are not currently visible
		if (isVisible() == false)
		{
			currVect = getTranslationVectorFromSelectedTracks();
			setInputVector(currVect);
			updateGui();
		}

		super.setVisible(aBool);
	}

	/**
	 * Helper method that handles the apply action.
	 */
	private void doActionApply()
	{
		List<Track> trackL = refModel.getSelectedTracks();

		Vector3D tmpVect = getTranslationVectorFromGui();
		refModel.setTranslation(trackL, tmpVect);
	}

	/**
	 * Helper method that handles the reset action.
	 */
	private void doActionReset()
	{
		List<Track> trackL = refModel.getSelectedTracks();

		Vector3D tmpVect = Vector3D.ZERO;
		setInputVector(tmpVect);
		refModel.setTranslation(trackL, tmpVect);
	}

	/**
	 * Helper method that handles the track mutation event.
	 */
	private void doHandleEventTrackMutation()
	{
		// Update our currVect to reflect the selected tracks.
		currVect = getTranslationVectorFromSelectedTracks();

		// Update the input UI components to reflect the currVect
		setInputVector(currVect);

		updateGui();
	}

	/**
	 * Helper method that handles the track selection change event.
	 */
	private void doHandleEventTrackSelectionChanged()
	{
		// Update our currVect to reflect the selected tracks.
		currVect = getTranslationVectorFromSelectedTracks();

		updateGui();
	}

	/**
	 * Helper method that returns the translation vector as specified by the
	 * relevant gui components.
	 */
	private Vector3D getTranslationVectorFromGui()
	{
		double xVal = xTranslateNF.getValue();
		double yVal = yTranslateNF.getValue();
		double zVal = zTranslateNF.getValue();
		Vector3D retVect = new Vector3D(xVal, yVal, zVal);
		return retVect;
	}

	/**
	 * Helper method that returns the translation vector of the selected Tracks.
	 * <P>
	 * If there are no selected Tracks or the selected Tracks have different
	 * translation vectors then the invalid vector (Vector3D.NaN) will be
	 * returned.
	 */
	private Vector3D getTranslationVectorFromSelectedTracks()
	{
		// Retrieve the list of selected Tracks
		List<Track> trackL = refModel.getSelectedTracks();
		if (trackL.size() == 0)
			return Vector3D.NaN;

		// Retrieve the 1st Track's translation vector
		Vector3D retVect = refModel.getTranslation(trackL.get(0));

		// Check that all translation vectors are equal
		boolean tmpBool = true;
		for (Track aTrack : trackL)
		{
			Vector3D evalVect = refModel.getTranslation(aTrack);
			tmpBool &= retVect.equals(evalVect);
		}

		// Return the appropriate vector
		if (tmpBool == false)
			retVect = Vector3D.NaN;

		return retVect;
	}

	/**
	 * Helper method that forms the GUI.
	 */
	private void formGui()
	{
		setTitle("Translate Lidar Tracks");
		setLayout(new MigLayout());

		// Info area
		infoL = new JLabel("Selected Tracks: 0");
		add(infoL, "span,wrap");

		// Input area: x, y, z
		xTranslateNF = new GNumberField(this);
		yTranslateNF = new GNumberField(this);
		zTranslateNF = new GNumberField(this);
		add(new JLabel("x-translate:"), "");
		add(xTranslateNF, "growx,pushx,span,wrap");
		add(new JLabel("y-translate:"), "");
		add(yTranslateNF, "growx,span,wrap");
		add(new JLabel("z-translate:"), "");
		add(zTranslateNF, "growx,span,wrap");

		// Warn area
		warnL = new JLabel("Changes have not been applied.");
		warnL.setPreferredSize(warnL.getPreferredSize());
		add(warnL, "growx,w 0:0:,span,wrap");

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
		List<Track> trackL = refModel.getSelectedTracks();

		// Update the applyB
		boolean isValidInput = xTranslateNF.isValidInput();
		isValidInput &= yTranslateNF.isValidInput();
		isValidInput &= zTranslateNF.isValidInput();

		boolean tmpBool = isValidInput;
		tmpBool &= trackL.size() > 0;
		applyB.setEnabled(tmpBool);

		// Update the resetB
		tmpBool = trackL.size() > 0;
		resetB.setEnabled(tmpBool);

		// Update the infoL / warnL
		infoL.setText("Selected Tracks: " + trackL.size());

		String regMsg = "";
		String errMsg = null;
		if (trackL.size() == 0)
			errMsg = "There are no selected tracks.";
		else if (isValidInput == false)
			errMsg = "Please enter valid input.";
		else if (currVect.equals(getTranslationVectorFromGui()) == false)
			regMsg = "Changes have not been applied.";

		warnL.setText(regMsg);
		if (errMsg != null)
			warnL.setText(errMsg);

		Color fgColor = Color.BLACK;
		if (errMsg != null)
			fgColor = failColor;
		warnL.setForeground(fgColor);
	}

}