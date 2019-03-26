package edu.jhuapl.sbmt.gui.lidar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.TrackFileType;

import net.miginfocom.swing.MigLayout;

/**
 * Panel used to provide a load (import) lidar tracks capability.
 */
public class LidarLoadPanel extends JPanel implements ActionListener
{
	// Ref vars
	private LidarSearchDataCollection refModel;

	// GUI vars
	private JButton loadB;
	private JComboBox<String> fileTypeBox;

	public LidarLoadPanel(LidarSearchDataCollection aModel)
	{
		refModel = aModel;

		setLayout(new MigLayout("", "", ""));

		loadB = new JButton("Load Tracks");
		loadB.addActionListener(this);
		loadB.setToolTipText("Select lidar files to import.");
		JLabel tmpL = new JLabel("File Type:");
		fileTypeBox = new JComboBox<>();
//		fileTypeBox.setToolTipText(
//				"<html>\nTrack file can be in either text or binary format.<br><br>\nIf text, file may contain 3 or more space-delimited columns.<br>\nDepending on the number of columns, the file is interpreted the following way:<br>\n - 3 columns: X, Y, and Z target position. Time and spacecraft position set to zero.<br> \n - 4 columns: time, X, Y, and Z target position. Spacecraft position set to zero.<br>\n - 5 columns: time, X, Y, and Z target position. Spacecraft position set to zero. 5th column ignored.<br>\n - 6 columns: X, Y, Z target position, X, Y, Z spacecraft position. Time set to zero.<br>\n - 7 or more columns: time, X, Y, and Z target position, X, Y, Z spacecraft position. Additional columns ignored.<br>\nNote that time is expressed either as a UTC string such as 2000-04-06T13:19:12.153<br>\nor as a floating point ephemeris time such as 9565219.901.<br>\n<br>\nIf binary, each record must consist of 7 double precision values:<br>\n1. ET<br>\n2. X target<br>\n3. Y target<br>\n4. Z target<br>\n5. X spacecraft position<br>\n6. Y spacecraft position<br>\n7. Z spacecraft position<br>\n");
		fileTypeBox.setModel(new DefaultComboBoxModel<>(TrackFileType.names()));
		add(tmpL, "span,split");
		add(fileTypeBox, "growx");
		add(loadB, "");
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == loadB)
			doLoadAction();
	}

	/**
	 * Helper method that handles the load action.
	 */
	private void doLoadAction()
	{
		// Bail if no file(s) specified
		File[] fileArr = CustomFileChooser.showOpenDialog(this, "Select Lidar Files", null, true);
		if (fileArr == null)
			return;
		List<File> tmpFileL = Arrays.asList(fileArr);
		tmpFileL.sort(null);

		// Load the file
		TrackFileType trackFileType = TrackFileType.find(fileTypeBox.getSelectedItem().toString());
		try
		{
			refModel.loadTracksFromFiles(fileArr, trackFileType);
		}
		catch (IOException aExp)
		{
			String errMsg = "There was an error reading the file.";
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), errMsg, "Error",
					JOptionPane.ERROR_MESSAGE);

			aExp.printStackTrace();
		}

	}

}
