package edu.jhuapl.sbmt.gui.dtm.ui.creation;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import edu.jhuapl.saavtk.gui.JTextFieldDoubleVerifier;

public class DtmCreationControlPanel extends JPanel
{

    private JToggleButton selectRegionButton;
    private JFormattedTextField outputFolderTextField;
    private JCheckBox setSpecifyRegionManuallyCheckbox;
    private JCheckBox grotesqueModelCheckbox;
    private JTextField pixelScaleTextField;
    private JTextField latitudeTextField;
    private JTextField longitudeTextField;
    private JButton mapmakerSubmitButton;
    private JButton bigmapSubmitButton;
    private JButton loadButton;
    private JButton renameButton;
    private JSpinner halfSizeSpinner;
    private boolean hasBigmap = false;
    private boolean hasMapmaker = true;
    JButton clearRegionButton;
    private JButton deleteButton;
    private JLabel latitudeLabel;
    private JLabel longitudeLabel;
    private JLabel pixelScaleLabel;
//    public DtmCreationControlPanel()
//    {
//    	this.hasMapmaker = true;
//    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//		initControls();
//    }

	public DtmCreationControlPanel(boolean hasMapmaker, boolean hasBigmap)
	{
		this.hasBigmap = hasBigmap;
		this.hasMapmaker = hasMapmaker;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		initControls();
	}

	private void initControls()
	{
        JPanel selectRegionPanel = new JPanel();
        selectRegionPanel.setLayout(new BoxLayout(selectRegionPanel, BoxLayout.X_AXIS));

        loadButton = new JButton("Load...");
        selectRegionPanel.add(loadButton);

        renameButton = new JButton("Rename...");
        selectRegionPanel.add(renameButton);

        deleteButton = new JButton("Delete");
        selectRegionPanel.add(deleteButton);

        if (hasBigmap || hasMapmaker)
        {
	        selectRegionButton = new JToggleButton("Select Region");
	        selectRegionButton.setEnabled(true);
	        selectRegionPanel.add(selectRegionButton);

	        clearRegionButton = new JButton("Clear Region");
	        selectRegionPanel.add(clearRegionButton);

	        final JLabel halfSizeLabel = new JLabel("Half Size (pixels)");
	        halfSizeSpinner = new JSpinner(new SpinnerNumberModel(512, 1, 512, 1));
	        halfSizeSpinner.setPreferredSize(new Dimension(75, 23));
	        halfSizeSpinner.setMaximumSize(new Dimension(75, 23));

	        if(hasBigmap)
	        {
	            grotesqueModelCheckbox = new JCheckBox("Grotesque Model");
	            grotesqueModelCheckbox.setSelected(true);
	        }

	        pixelScaleLabel = new JLabel("Pixel Scale (meters)");
	        pixelScaleLabel.setEnabled(false);

	        latitudeLabel = new JLabel("Latitude (deg)");
	        latitudeLabel.setEnabled(false);
	        latitudeTextField = new JTextField();
	        latitudeTextField.setPreferredSize(new Dimension(200, 24));
	        latitudeTextField.setMaximumSize(new Dimension(200, 24));
	        latitudeTextField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(latitudeTextField, -90.0, 90.0));
	        latitudeTextField.setEnabled(false);

	        longitudeLabel = new JLabel("Longitude (deg)");
	        longitudeLabel.setEnabled(false);
	        longitudeTextField = new JTextField();
	        longitudeTextField.setPreferredSize(new Dimension(200, 24));
	        longitudeTextField.setMaximumSize(new Dimension(200, 24));
	        longitudeTextField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(longitudeTextField, -360.0, 360.0));
	        longitudeTextField.setEnabled(false);

	        final JPanel submitPanel = new JPanel();

	//        if(hasMapmaker)
	        {
	            mapmakerSubmitButton = new JButton("Run Mapmaker");
	            mapmakerSubmitButton.setEnabled(hasMapmaker);
	            submitPanel.add(mapmakerSubmitButton);
	        }
	//        else if (hasBigmap)
	//        {
	//            bigmapSubmitButton = new JButton("Run Bigmap");
	//            bigmapSubmitButton.setEnabled(true);
	//            submitPanel.add(bigmapSubmitButton);
	//        }

	        add(selectRegionPanel);

	        JPanel panel = new JPanel();
	        add(panel);
	        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	        setSpecifyRegionManuallyCheckbox = new JCheckBox("Enter Manual Region:");
	        panel.add(setSpecifyRegionManuallyCheckbox);
	        setSpecifyRegionManuallyCheckbox.setSelected(false);

	        Component horizontalGlue = Box.createHorizontalGlue();
	        panel.add(horizontalGlue);

	        JPanel latitudePanel = new JPanel();
	        add(latitudePanel);
	        latitudePanel.setLayout(new BoxLayout(latitudePanel, BoxLayout.X_AXIS));
	        latitudePanel.add(latitudeLabel);

	        Component horizontalGlue_3 = Box.createHorizontalGlue();
	        latitudePanel.add(horizontalGlue_3);
	        latitudePanel.add(latitudeTextField);

	        JPanel longitudePanel = new JPanel();
	        add(longitudePanel);
	        longitudePanel.setLayout(new BoxLayout(longitudePanel, BoxLayout.X_AXIS));
	        longitudePanel.add(longitudeLabel);

	        Component horizontalGlue_2 = Box.createHorizontalGlue();
	        longitudePanel.add(horizontalGlue_2);
	        longitudePanel.add(longitudeTextField);


	        JPanel pixelScalePanel = new JPanel();
	        add(pixelScalePanel);
	        pixelScalePanel.setLayout(new BoxLayout(pixelScalePanel, BoxLayout.X_AXIS));
	        pixelScalePanel.add(pixelScaleLabel);

	        Component horizontalGlue_4 = Box.createHorizontalGlue();
	        pixelScalePanel.add(horizontalGlue_4);
	        pixelScaleTextField = new JTextField();
	        pixelScalePanel.add(pixelScaleTextField);
	        pixelScaleTextField.setPreferredSize(new Dimension(200, 24));
	        pixelScaleTextField.setMaximumSize(new Dimension(200, 24));
	        pixelScaleTextField.setInputVerifier(JTextFieldDoubleVerifier.getVerifier(pixelScaleTextField, Double.MIN_VALUE, Double.MAX_VALUE));
	        pixelScaleTextField.setEnabled(false);

	        JPanel halfSizePanel = new JPanel();
	        add(halfSizePanel);
	        halfSizePanel.setLayout(new BoxLayout(halfSizePanel, BoxLayout.X_AXIS));
	        halfSizePanel.add(halfSizeLabel);

	        Component horizontalGlue_1 = Box.createHorizontalGlue();
	        halfSizePanel.add(horizontalGlue_1);
	        halfSizePanel.add(halfSizeSpinner);


	        add(submitPanel);
	        submitPanel.setLayout(new BoxLayout(submitPanel, BoxLayout.X_AXIS));
	        if(hasBigmap)
	        {
	        	grotesqueModelCheckbox.setVisible(true);
	        }
        }
        else
        {
        	add(selectRegionPanel);
        }
	}

	public JToggleButton getSelectRegionButton()
	{
		return selectRegionButton;
	}

	public JFormattedTextField getOutputFolderTextField()
	{
		return outputFolderTextField;
	}

	public JCheckBox getSetSpecifyRegionManuallyCheckbox()
	{
		return setSpecifyRegionManuallyCheckbox;
	}

	public JCheckBox getGrotesqueModelCheckbox()
	{
		return grotesqueModelCheckbox;
	}

	public JTextField getPixelScaleTextField()
	{
		return pixelScaleTextField;
	}

	public JTextField getLatitudeTextField()
	{
		return latitudeTextField;
	}

	public JTextField getLongitudeTextField()
	{
		return longitudeTextField;
	}

	public JButton getMapmakerSubmitButton()
	{
		return mapmakerSubmitButton;
	}

	public JButton getBigmapSubmitButton()
	{
		return bigmapSubmitButton;
	}

	public JButton getLoadButton()
	{
		return loadButton;
	}

	public JButton getRenameButton()
	{
		return renameButton;
	}

	public JSpinner getHalfSizeSpinner()
	{
		return halfSizeSpinner;
	}

	public boolean isHasBigmap()
	{
		return hasBigmap;
	}

	public boolean isHasMapmaker()
	{
		return hasMapmaker;
	}

	public JButton getClearRegionButton()
	{
		return clearRegionButton;
	}


	public JButton getDeleteButton() {
		return deleteButton;
	}

	public JLabel getLatitudeLabel()
	{
		return latitudeLabel;
	}

	public JLabel getLongitudeLabel()
	{
		return longitudeLabel;
	}

	public JLabel getPixelScaleLabel()
	{
		return pixelScaleLabel;
	}
}
