/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.image2.ui.custom.editing;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image2.controllers.preview.ImageContrastController;
import edu.jhuapl.sbmt.image2.controllers.preview.ImageFillValuesController;
import edu.jhuapl.sbmt.image2.controllers.preview.ImageMaskController;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.util.VtkENVIReader;

public class CustomImageEditingDialog<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
		extends JDialog
{
	private JTextField imagePathTextField;
	private JTextField imageNameTextField;
//	private JTextField fillValuesTextField;
	private JTextField pointingFilenameTextField;
	private JTextField minLatitudeTextField;
	private JTextField maxLatitudeTextField;
	private JTextField minLongitudeTextField;
	private JTextField maxLongitudeTextField;
	// private JComboBox<ImageType> imageTypeComboBox;
	private JComboBox<String> imageFlipComboBox;
	private JComboBox<String> imageRotationComboBox;
	private JComboBox<String> pointingTypeComboBox;
	private JCheckBox flipAboutXCheckBox;
	// private BaseItemManager<G1> imageCollection;
	private JButton browseButton;
	private JButton okButton;
//	private JButton fillValuesButton;

	ImageMaskController maskController;
	ImageContrastController contrastController;
	ImageFillValuesController fillValuesController;

	private JPanel layerPanel;
	private JPanel controlsPanel;
	private JSplitPane splitPane;
	private int displayedLayerIndex = 0;
	private boolean isPerspective;
	private JPanel appearancePanel;

	public CustomImageEditingDialog(Window parent, G1 existingImage, boolean isPerspective, Runnable completionBlock, ImageMaskController maskController,
	ImageContrastController contrastController, ImageFillValuesController fillController)
	{
		super(parent, "Edit Image", Dialog.ModalityType.APPLICATION_MODAL);

//		this.displayedLayerIndex = currentLayerIndex;
//		this.layer = layers.get(currentLayerIndex);
		this.isPerspective = isPerspective;
		this.contrastController = contrastController;
		this.fillValuesController = fillController;
		this.maskController = maskController;
		this.layerPanel = new JPanel();
		this.layerPanel.setLayout(new GridBagLayout());
		this.controlsPanel = new JPanel();
		this.controlsPanel.setLayout(new GridBagLayout());
		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlsPanel, layerPanel);
		splitPane.setMinimumSize(new Dimension(750, 550));
//		try
//		{
//			if (!existingImage.getPointingSource().equals("FILE NOT FOUND"))
//				getLayers();
//		}
//		catch (Exception e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		initGUI();
//		setSize(750, 550);
//		try
//		{
//			if (!existingImage.getPointingSource().equals("FILE NOT FOUND"))
//			{
//				renderLayer(layer);
//				setIntensity(existingImage.getIntensityRange());
//			}
//		}
//		catch (Exception e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(splitPane, gridBagConstraints);
		pack();
//		setVisible(true);

//		javax.swing.SwingUtilities.invokeLater(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				if (renWin != null)
//				{
//					renWin.resetCamera();
//					renWin.Render();
//				}
//			}
//		});

	}

	private void initGUI()
	{
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(1150, 550));
		setPreferredSize(new Dimension(1150, 550));
		getContentPane().setLayout(new GridBagLayout());

		controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));

		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		aboutPanel.setBorder(BorderFactory.createTitledBorder("About this image"));

		aboutPanel.add(buildImagePathInput());
		aboutPanel.add(buildImageNameInput());

		controlsPanel.add(aboutPanel);
		// getContentPane().add(buildImageTypeInput());

		JPanel projectionPanel = new JPanel();
		projectionPanel.setBorder(BorderFactory.createTitledBorder("Image Projection"));
		projectionPanel.add(buildPointingInput());

		controlsPanel.add(projectionPanel);


		appearancePanel = new JPanel();
		appearancePanel.setEnabled(false);
		appearancePanel.setLayout(new BoxLayout(appearancePanel, BoxLayout.Y_AXIS));
		appearancePanel.setBorder(BorderFactory.createTitledBorder("Image Appearance"));
		appearancePanel.add(buildContrastController());
//		appearancePanel.add(Box.createVerticalStrut(20));
		appearancePanel.add(buildTrimController());
		appearancePanel.add(buildFillValuesPanel());
		appearancePanel.add(Box.createVerticalGlue());

		controlsPanel.add(appearancePanel);

//		controlsPanel.add(Box.createVerticalGlue());
		controlsPanel.add(buildSubmitCancelPanel());




//		existingImage.ifPresent(image ->
//		{

//			imagePathTextField.setText(existingImage.getFilename());
//			imageNameTextField.setText(existingImage.getName());
//			if (existingImage.getImageType() != ImageType.GENERIC_IMAGE)
//			{
//				if (!existingImage.getPointingSource().isEmpty())
//				{
//					pointingTypeComboBox.setSelectedIndex(0);
//					pointingFilenameTextField.setText(existingImage.getPointingSource());
//					imageFlipComboBox.setSelectedItem(existingImage.getFlip());
//					imageRotationComboBox.setSelectedItem("" + (int) (existingImage.getRotation()));
//				}
//				else
//				{
//					pointingTypeComboBox.setSelectedIndex(1);
//					minLatitudeTextField.setText("" + existingImage.getBounds().minLatitude());
//					maxLatitudeTextField.setText("" + existingImage.getBounds().maxLatitude());
//					minLongitudeTextField.setText("" + existingImage.getBounds().minLongitude());
//					maxLongitudeTextField.setText("" + existingImage.getBounds().maxLongitude());
//				}
//			}
//			else // cylindrical
//			{
//				pointingTypeComboBox.setSelectedIndex(1);
//				minLatitudeTextField.setText("" + existingImage.getBounds().minLatitude());
//				maxLatitudeTextField.setText("" + existingImage.getBounds().maxLatitude());
//				minLongitudeTextField.setText("" + existingImage.getBounds().minLongitude());
//				maxLongitudeTextField.setText("" + existingImage.getBounds().maxLongitude());
//			}

//		});


	}






	private JPanel buildImagePathInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(30));
		panel.add(new JLabel("Path:"));

		imagePathTextField = new JTextField();
		imagePathTextField.setMinimumSize(new Dimension(350, 30));
		imagePathTextField.setPreferredSize(new Dimension(350, 30));
		imagePathTextField.setMaximumSize(new Dimension(350, 30));

		panel.add(Box.createHorizontalStrut(20));
		panel.add(imagePathTextField);

		JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(e ->
		{
			File[] files = CustomFileChooser.showOpenDialog(this, "Select Image", List.of("fits", "fit", "FIT", "FITS", "png", "PNG", "JPG", "jpg"), false);
			if (files == null || files.length == 0)
	        {
	            return;
	        }

			String filename = files[0].getAbsolutePath();
			imagePathTextField.setText(filename);
			String imageFileName = files[0].getName();
			String extension = FilenameUtils.getExtension(imageFileName).toLowerCase();

			imageNameTextField.setText(imageFileName);
		});

		panel.add(browseButton);
		panel.add(Box.createGlue());
		return panel;
	}

	private JPanel buildImageNameInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(30));
		panel.add(new JLabel("Name:"));

		imageNameTextField = new JTextField();
		imageNameTextField.setMinimumSize(new Dimension(350, 30));
		imageNameTextField.setPreferredSize(new Dimension(350, 30));
		imageNameTextField.setMaximumSize(new Dimension(350, 30));

		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageNameTextField);
		panel.add(Box.createGlue());
//		panel.add(Box.createHorizontalStrut(100));

		return panel;
	}

	// private JPanel buildImageTypeInput()
	// {
	// JPanel panel = new JPanel();
	// panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	// panel.add(new JLabel("Image Type:"));
	//
	// imageTypeComboBox = new JComboBox<ImageType>(new ImageType[]
	// {instrument.getType(), ImageType.GENERIC_IMAGE});
	// imageTypeComboBox.setMaximumSize(new Dimension(350, 30));
	// panel.add(Box.createHorizontalStrut(10));
	// panel.add(imageTypeComboBox);
	// panel.add(Box.createHorizontalStrut(100));
	// return panel;
	// }

	private JPanel buildFlipAboutXCheckBoxInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		// panel.add(new JLabel("Flip "));
		flipAboutXCheckBox = new JCheckBox("Flip about X Axis");


		panel.add(flipAboutXCheckBox);
		return panel;
	}

	private JPanel buildImageRotationInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(0));
		panel.add(new JLabel("Image Rotation:"));

		imageRotationComboBox = new JComboBox<String>(new String[]
		{ "0", "90", "180", "270" });
		imageRotationComboBox.setMaximumSize(new Dimension(350, 30));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageRotationComboBox);
		panel.add(Box.createHorizontalStrut(100));
		return panel;
	}

	private JPanel buildImageFlipInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(0));
		panel.add(new JLabel("Image Flip:"));
		panel.add(Box.createHorizontalStrut(30));
		imageFlipComboBox = new JComboBox<String>(new String[]
		{ "None", "X", "Y" });
		imageFlipComboBox.setMaximumSize(new Dimension(350, 30));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageFlipComboBox);
		panel.add(Box.createHorizontalStrut(100));
		return panel;
	}

	private JPanel buildPointingInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel cardPanel = new JPanel();
		CardLayout cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		cardPanel.setMinimumSize(new Dimension(500, 100));
		cardPanel.setPreferredSize(new Dimension(500, 100));
		cardPanel.setMaximumSize(new Dimension(500, 100));

		////////////////
		JPanel cylindricalPanel = new JPanel();
		cylindricalPanel.setLayout(new BoxLayout(cylindricalPanel, BoxLayout.Y_AXIS));

		JPanel latitudePanel = new JPanel();
		latitudePanel.setLayout(new BoxLayout(latitudePanel, BoxLayout.X_AXIS));
		latitudePanel.add(Box.createHorizontalStrut(10));
		latitudePanel.add(new JLabel("Latitude Range (deg):"));
		minLatitudeTextField = new JTextField("-90.0");
		minLatitudeTextField.setMinimumSize(new Dimension(100, 30));
		minLatitudeTextField.setPreferredSize(new Dimension(100, 30));
		minLatitudeTextField.setMaximumSize(new Dimension(100, 30));

		maxLatitudeTextField = new JTextField("90.0");
		maxLatitudeTextField.setMinimumSize(new Dimension(100, 30));
		maxLatitudeTextField.setPreferredSize(new Dimension(100, 30));
		maxLatitudeTextField.setMaximumSize(new Dimension(100, 30));

		latitudePanel.add(Box.createHorizontalStrut(10));
		latitudePanel.add(minLatitudeTextField);
		latitudePanel.add(Box.createHorizontalStrut(10));
		latitudePanel.add(new JLabel(" to "));
		latitudePanel.add(Box.createHorizontalStrut(10));

		latitudePanel.add(Box.createHorizontalStrut(10));
		latitudePanel.add(maxLatitudeTextField);
		latitudePanel.add(Box.createHorizontalStrut(100));

		JPanel longitudePanel = new JPanel();
		longitudePanel.setLayout(new BoxLayout(longitudePanel, BoxLayout.X_AXIS));
		longitudePanel.add(new JLabel("Longitude Range (deg east):"));
		minLongitudeTextField = new JTextField("0.0");
		minLongitudeTextField.setMinimumSize(new Dimension(100, 30));
		minLongitudeTextField.setPreferredSize(new Dimension(100, 30));
		minLongitudeTextField.setMaximumSize(new Dimension(100, 30));

		maxLongitudeTextField = new JTextField("360.0");
		maxLongitudeTextField.setMinimumSize(new Dimension(100, 30));
		maxLongitudeTextField.setPreferredSize(new Dimension(100, 30));
		maxLongitudeTextField.setMaximumSize(new Dimension(100, 30));

		longitudePanel.add(Box.createHorizontalStrut(10));
		longitudePanel.add(minLongitudeTextField);
		longitudePanel.add(Box.createHorizontalStrut(10));
		longitudePanel.add(new JLabel(" to "));
		longitudePanel.add(Box.createHorizontalStrut(10));

		longitudePanel.add(Box.createHorizontalStrut(10));
		longitudePanel.add(maxLongitudeTextField);
		longitudePanel.add(Box.createHorizontalStrut(100));

		cylindricalPanel.add(latitudePanel);
		cylindricalPanel.add(longitudePanel);
		cylindricalPanel.add(buildFlipAboutXCheckBoxInput());

		////////////////
		JPanel perspectivePanel = new JPanel();
		perspectivePanel.setLayout(new BoxLayout(perspectivePanel, BoxLayout.Y_AXIS));

		JPanel fileInputPanel = new JPanel();
		fileInputPanel.setLayout(new BoxLayout(fileInputPanel, BoxLayout.X_AXIS));
		fileInputPanel.add(Box.createHorizontalStrut(0));
		fileInputPanel.add(new JLabel("Pointing File:"));
		fileInputPanel.add(Box.createHorizontalStrut(20));
		pointingFilenameTextField = new JTextField("FILE NOT FOUND");
		pointingFilenameTextField.setMinimumSize(new Dimension(275, 30));
		pointingFilenameTextField.setPreferredSize(new Dimension(275, 30));
		pointingFilenameTextField.setMaximumSize(new Dimension(275, 30));

		fileInputPanel.add(Box.createHorizontalStrut(10));
		fileInputPanel.add(pointingFilenameTextField);
		fileInputPanel.add(Box.createHorizontalStrut(10));
		browseButton = new JButton("Browse");

//		browseButton.addActionListener(e ->
//		{
//			File file = CustomFileChooser.showOpenDialog(this, "Select Pointing File...");
//			if (file == null)
//			{
//				return;
//			}
//
//			String filename = file.getAbsolutePath();
//			pointingFilenameTextField.setText(filename);
//			existingImage.setPointingSource(filename);
//			renderLayerAndAddAttributes();
//		});

		fileInputPanel.add(browseButton);
		fileInputPanel.add(Box.createHorizontalStrut(100));

		perspectivePanel.add(fileInputPanel);

		perspectivePanel.add(buildImageRotationInput());
		perspectivePanel.add(buildImageFlipInput());

		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS));
//		optionPanel.add(new JLabel("Projection:"));

		if (isPerspective)
		{
			CardLayout cl = (CardLayout) (cardPanel.getLayout());
			cl.show(cardPanel, "Perspective Projection");
		}
		else
		{
			CardLayout cl = (CardLayout) (cardPanel.getLayout());
			cl.show(cardPanel, "Simple Cylindrical Projection");
		}
		// optionPanel.add(Box.createHorizontalGlue());
		pointingTypeComboBox = new JComboBox<String>(new String[]
		{ "Perspective Projection", "Simple Cylindrical Projection" });
		pointingTypeComboBox.addItemListener(new ItemListener()
		{

			@Override
			public void itemStateChanged(ItemEvent arg0)
			{
				CardLayout cl = (CardLayout) (cardPanel.getLayout());
				cl.show(cardPanel, arg0.getItem().toString());
			}
		});
//		optionPanel.add(pointingTypeComboBox);
		optionPanel.add(Box.createHorizontalStrut(100));

		panel.add(optionPanel);
		cardPanel.add(perspectivePanel, "Perspective Projection");
		cardPanel.add(cylindricalPanel, "Simple Cylindrical Projection");
		panel.add(cardPanel);
		return panel;
	}

	private JPanel buildContrastController()
	{
//		GridBagConstraints gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.gridwidth = controlsPanel.getWidth();
//		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.weighty = 0.0;
//		gridBagConstraints.insets = new Insets(3, 30, 3, 0);
//		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//		contrastController = new ImageContrastController(displayedImage, existingImage.getIntensityRange(), new Function<vtkImageData, Void>() {
//
//			@Override
//			public Void apply(vtkImageData t)
//			{
//				try
//				{
//					displayedImage = t;
//					updateImage(displayedImage);
//					setIntensity(null);
//					renWin.Render();
//					if (completionBlock != null) completionBlock.run();
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				return null;
//			}
//		});
		contrastController.getView().setBackground(Color.red);
		return contrastController.getView();
	}

	private JPanel buildTrimController()
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 6, 3, 0);
//		trimController = new ImageTrimController(layer, existingImage.getMaskValues(), new Function<Layer, Void>()
//		{
//
//			@Override
//			public Void apply(Layer t)
//			{
//				try
//				{
//					generateVtkImageData(t);
//					updateImage(displayedImage);
//					setIntensity(null);
//					if (renWin == null) return null;
//					renWin.Render();
//					layer = t;
//					if (completionBlock != null) completionBlock.run();
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				return null;
//			}
//		});
		maskController.getView().setMaximumSize(new Dimension(550, 60));
		return maskController.getView();
	}

	private JPanel buildSubmitCancelPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

//		okButton.addActionListener(e ->
//		{
//			String errorString = validateInput();
//			if (errorString != null)
//			{
//				JOptionPane.showMessageDialog(this, errorString, "Error", JOptionPane.ERROR_MESSAGE);
//				return;
//			}
//
//			storeImage();
//			setVisible(false);
//			System.out.println("CustomImageEditingDialog: buildSubmitCancelPanel: setting visible false ");
//		});

		cancelButton.addActionListener(e -> setVisible(false));

		panel.add(okButton);
		panel.add(cancelButton);
		return panel;
	}

	private JPanel buildFillValuesPanel()
	{
		fillValuesController.getView().setMaximumSize(new Dimension(550, 100));
		return fillValuesController.getView();

//		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//
//		JPanel labelPanel = new JPanel();
//		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
//		labelPanel.add(Box.createHorizontalStrut(30));
//		labelPanel.add(new JLabel("Fill Value(s):"));
//		labelPanel.add(Box.createHorizontalGlue());
//		panel.add(labelPanel);
//
//		JPanel textFieldPanel = new JPanel();
//		textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.X_AXIS));
//		fillValuesButton = new JButton("Apply");
//		fillValuesTextField = new JTextField();
//		fillValuesTextField.setMinimumSize(new Dimension(350, 30));
//		fillValuesTextField.setPreferredSize(new Dimension(400, 30));
//		fillValuesTextField.setMaximumSize(new Dimension(550, 30));
//		textFieldPanel.add(Box.createHorizontalStrut(50));
//		textFieldPanel.add(fillValuesTextField);
//		textFieldPanel.add(fillValuesButton);
//
//
//		panel.add(textFieldPanel);
//
//
//		return panel;
	}







	private String checkForEnviFile(String imagePath, boolean isEnviSupported)
	{
		if (VtkENVIReader.isENVIFilename(imagePath))
		{
			// Both header and binary files must exist
			if (VtkENVIReader.checkFilesExist(imagePath))
			{
				// SBMT supports ENVI
				isEnviSupported = true;
			}
			else
			{
				// Error message
				return "Was not able to locate a corresponding .hdr file for ENVI image binary";
			}
		}
		return "";
	}



	/**
	 * @return the imagePathTextField
	 */
	public JTextField getImagePathTextField()
	{
		return imagePathTextField;
	}

	/**
	 * @return the imageNameTextField
	 */
	public JTextField getImageNameTextField()
	{
		return imageNameTextField;
	}

//	/**
//	 * @return the fillValuesTextField
//	 */
//	public JTextField getFillValuesTextField()
//	{
//		return fillValuesTextField;
//	}

	/**
	 * @return the pointingFilenameTextField
	 */
	public JTextField getPointingFilenameTextField()
	{
		return pointingFilenameTextField;
	}

	/**
	 * @return the minLatitudeTextField
	 */
	public JTextField getMinLatitudeTextField()
	{
		return minLatitudeTextField;
	}

	/**
	 * @return the maxLatitudeTextField
	 */
	public JTextField getMaxLatitudeTextField()
	{
		return maxLatitudeTextField;
	}

	/**
	 * @return the minLongitudeTextField
	 */
	public JTextField getMinLongitudeTextField()
	{
		return minLongitudeTextField;
	}

	/**
	 * @return the maxLongitudeTextField
	 */
	public JTextField getMaxLongitudeTextField()
	{
		return maxLongitudeTextField;
	}

	/**
	 * @return the imageFlipComboBox
	 */
	public JComboBox<String> getImageFlipComboBox()
	{
		return imageFlipComboBox;
	}

	/**
	 * @return the imageRotationComboBox
	 */
	public JComboBox<String> getImageRotationComboBox()
	{
		return imageRotationComboBox;
	}

	/**
	 * @return the pointingTypeComboBox
	 */
	public JComboBox<String> getPointingTypeComboBox()
	{
		return pointingTypeComboBox;
	}

	/**
	 * @return the trimController
	 */
	public ImageMaskController getMaskController()
	{
		return maskController;
	}

	/**
	 * @return the contrastController
	 */
	public ImageContrastController getContrastController()
	{
		return contrastController;
	}

	/**
	 * @return the flipAboutXCheckBox
	 */
	public JCheckBox getFlipAboutXCheckBox()
	{
		return flipAboutXCheckBox;
	}

	/**
	 * @return the browseButton
	 */
	public JButton getBrowseButton()
	{
		return browseButton;
	}

	/**
	 * @return the okButton
	 */
	public JButton getOkButton()
	{
		return okButton;
	}

	/**
	 * @return the layerPanel
	 */
	public JPanel getLayerPanel()
	{
		return layerPanel;
	}

	/**
	 * @return the appearancePanel
	 */
	public JPanel getAppearancePanel()
	{
		return appearancePanel;
	}

//	/**
//	 * @return the fillValuesButton
//	 */
//	public JButton getFillValuesButton()
//	{
//		return fillValuesButton;
//	}

//	/**
//	 * @param trimController the trimController to set
//	 */
//	public void setTrimController(ImageTrimController trimController)
//	{
//		this.trimController = trimController;
//	}
//
//	/**
//	 * @param contrastController the contrastController to set
//	 */
//	public void setContrastController(ImageContrastController contrastController)
//	{
//		this.contrastController = contrastController;
//	}
}
