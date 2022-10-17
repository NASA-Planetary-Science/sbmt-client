package edu.jhuapl.sbmt.image2.ui.custom.importer;

import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.core.image.IImagingInstrument;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image2.model.CylindricalBounds;
import edu.jhuapl.sbmt.image2.model.ImageOrigin;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.custom.importer.table.CustomImageImporterTableView;

import glum.item.BaseItemManager;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;

public class CustomImageImporterDialog2<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JDialog
{
	private boolean isEditMode;
	private boolean isEllipsoid;
	private PerspectiveImageCollection<G1> imageCollection;
	private IImagingInstrument instrument;
	private CustomImageImporterTableView<G1> table;
	private JComboBox<ImageType> imageTypeComboBox;
	private JComboBox<String> pointingTypeComboBox;
	private BaseItemManager<G1> tempCollection;

	public CustomImageImporterDialog2(Window parent, boolean isEditMode, IImagingInstrument instrument, boolean isEllipsoid, PerspectiveImageCollection<G1> imageCollection)
	{
		 super(parent, isEditMode ? "Edit Image" : "Import New Image", Dialog.ModalityType.APPLICATION_MODAL);
		 this.instrument = instrument;
		 this.isEditMode = isEditMode;
		 this.isEllipsoid = isEllipsoid;
		 this.imageCollection = imageCollection;
		 this.tempCollection = new BaseItemManager<G1>();
		 tempCollection.addListener(new ItemEventListener()
		 {
			@Override
			public void handleItemEvent(Object aSource, ItemEventType aEventType)
			{
				table.getDeleteImageButton().setEnabled(tempCollection.getSelectedItems().size() > 0);
				table.getEditImageButton().setEnabled(tempCollection.getSelectedItems().size() == 1);
			}
		 });
		 initGUI();
		 setSize(700, 400);
	}

	private void initGUI()
	{
		table = new CustomImageImporterTableView<G1>(tempCollection, null);
		table.setup();

		table.getLoadImageButton().addActionListener(e -> {
			File[] files = CustomFileChooser.showOpenDialog(table, "Select images...", null, true);
			if (files == null || files.length == 0)
	        {
	            return;
	        }
			List<G1> tempImages = Lists.newArrayList();
			int index = 1;
			boolean showPointingFileNotFoundDialog = false;
 			for (File file : files)
			{
 				G1 image;
				try
				{
					image = resolvePointingFilename(file.getAbsolutePath());
					if (image.getPointingSource().equals("FILE NOT FOUND"))
						showPointingFileNotFoundDialog = true;
					image.setIndex(index++);
					tempImages.add(image);
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
 			if (showPointingFileNotFoundDialog)
 			{
 				JOptionPane.showMessageDialog(this, "Pointing file(s) not found. Review table and edit the imported images to find the pointing(s).");
 			}
 			tempCollection.setAllItems(tempImages);
		});

		table.getDeleteImageButton().addActionListener(e -> {
			tempCollection.removeItems(tempCollection.getSelectedItems());
		});

		table.getEditImageButton().addActionListener(e -> {
			showEditPointingDialog();
		});


		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

		topPanel.add(buildImageTypeInput());
		topPanel.add(buildPointingInput());

		getContentPane().add(topPanel);
		getContentPane().add(table);
		getContentPane().add(buildSubmitCancelPanel());
	}

	private void showEditPointingDialog()
	{
		ImmutableSet<G1> selectedItems = tempCollection.getSelectedItems();
		if (selectedItems.size() != 1) return;
		G1 image = selectedItems.asList().get(0);
//		if (image.getImageType() == ImageType.GENERIC_IMAGE) return;
		if (image.getNumberOfLayers() == 1)	//editing custom single layer image
		{
			CustomImageImporterDialog<G1> dialog = new CustomImageImporterDialog<G1>(null, true, isEllipsoid, Optional.of(image));
	        dialog.setLocationRelativeTo(getContentPane());
	        dialog.setVisible(true);
	        ImageSource pointingSourceType = image.getPointingSource().endsWith("sum") || image.getPointingSource().endsWith("SUM") ? ImageSource.GASKELL : ImageSource.SPICE;
	        image.setPointingSourceType(pointingSourceType);
	        storeImage(image.getFilename(), image.getFilename(), image.getPointingSourceType(), image.getPointingSource());
		}
//		else if (image.getNumberOfLayers() == 3) //editing custom color image
//		{
//			ColorImageBuilderController controller = new ColorImageBuilderController(smallBodyModels, tempCollection, Optional.of(image));
//			controller.setImages(image.getImages());
//			BasicFrame frame = new BasicFrame();
//			frame.add(controller.getView());
//			frame.setSize(775, 900);
//			frame.setTitle("Edit Color Image");
//			frame.setVisible(true);
//			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		}
//		else //editing custom n > 1, n!=3 spectral image
//		{
//
//		}
	}

	private JPanel buildImageTypeInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new JLabel("Image Type:"));

		if (instrument != null)
			imageTypeComboBox = new JComboBox<ImageType>(new ImageType[] {instrument.getType(), ImageType.GENERIC_IMAGE});
		else
			imageTypeComboBox = new JComboBox<ImageType>(new ImageType[] {ImageType.GENERIC_IMAGE});
		imageTypeComboBox.setMaximumSize(new Dimension(350, 30));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageTypeComboBox);
		panel.add(Box.createHorizontalStrut(100));
		return panel;
	}

	private JPanel buildPointingInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new JLabel("Pointing Type:"));

		pointingTypeComboBox = new JComboBox<String>(new String[] {"Perspective Projection", "Simple Cylindrical Projection"});
		pointingTypeComboBox.setMaximumSize(new Dimension(350, 30));
		pointingTypeComboBox.addItemListener(new ItemListener()
		{

			@Override
			public void itemStateChanged(ItemEvent arg0)
			{
				table.setType((String)pointingTypeComboBox.getSelectedItem());
//				CardLayout cl = (CardLayout)(cardPanel.getLayout());
//				cl.show(cardPanel, arg0.getItem().toString());
			}
		});
		panel.add(Box.createHorizontalStrut(10));
		panel.add(pointingTypeComboBox);
		panel.add(Box.createHorizontalStrut(100));
		return panel;
	}

	private JPanel buildPointingInput2()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel cardPanel = new JPanel();
		CardLayout cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		cardPanel.setMinimumSize(new Dimension(500, 100));
		cardPanel.setPreferredSize(new Dimension(500, 100));
		cardPanel.setMaximumSize(new Dimension(500, 100));

//		////////////////
		JPanel cylindricalPanel = new JPanel();
//		cylindricalPanel.setLayout(new BoxLayout(cylindricalPanel, BoxLayout.Y_AXIS));
//
//		JPanel latitudePanel = new JPanel();
//		latitudePanel.setLayout(new BoxLayout(latitudePanel, BoxLayout.X_AXIS));
//		latitudePanel.add(Box.createHorizontalStrut(10));
//		latitudePanel.add(new JLabel("Latitude Range (deg):"));
//		minLatitudeTextField = new JTextField("-90.0");
//		minLatitudeTextField.setMinimumSize(new Dimension(100, 30));
//		minLatitudeTextField.setPreferredSize(new Dimension(100, 30));
//		minLatitudeTextField.setMaximumSize(new Dimension(100, 30));
//
//		maxLatitudeTextField = new JTextField("90.0");
//		maxLatitudeTextField.setMinimumSize(new Dimension(100, 30));
//		maxLatitudeTextField.setPreferredSize(new Dimension(100, 30));
//		maxLatitudeTextField.setMaximumSize(new Dimension(100, 30));
//
//		latitudePanel.add(Box.createHorizontalStrut(10));
//		latitudePanel.add(minLatitudeTextField);
//		latitudePanel.add(Box.createHorizontalStrut(10));
//		latitudePanel.add(new JLabel(" to "));
//		latitudePanel.add(Box.createHorizontalStrut(10));
//
//		latitudePanel.add(Box.createHorizontalStrut(10));
//		latitudePanel.add(maxLatitudeTextField);
//		latitudePanel.add(Box.createHorizontalStrut(100));
//
//		JPanel longitudePanel = new JPanel();
//		longitudePanel.setLayout(new BoxLayout(longitudePanel, BoxLayout.X_AXIS));
//		longitudePanel.add(new JLabel("Longitude Range (deg east):"));
//		minLongitudeTextField = new JTextField("0.0");
//		minLongitudeTextField.setMinimumSize(new Dimension(100, 30));
//		minLongitudeTextField.setPreferredSize(new Dimension(100, 30));
//		minLongitudeTextField.setMaximumSize(new Dimension(100, 30));
//
//		maxLongitudeTextField = new JTextField("360.0");
//		maxLongitudeTextField.setMinimumSize(new Dimension(100, 30));
//		maxLongitudeTextField.setPreferredSize(new Dimension(100, 30));
//		maxLongitudeTextField.setMaximumSize(new Dimension(100, 30));
//
//		longitudePanel.add(Box.createHorizontalStrut(10));
//		longitudePanel.add(minLongitudeTextField);
//		longitudePanel.add(Box.createHorizontalStrut(10));
//		longitudePanel.add(new JLabel(" to "));
//		longitudePanel.add(Box.createHorizontalStrut(10));
//
//		longitudePanel.add(Box.createHorizontalStrut(10));
//		longitudePanel.add(maxLongitudeTextField);
//		longitudePanel.add(Box.createHorizontalStrut(100));
//
//		cylindricalPanel.add(latitudePanel);
//		cylindricalPanel.add(longitudePanel);
//
//		////////////////
		JPanel perspectivePanel = new JPanel();
		perspectivePanel.setLayout(new BoxLayout(perspectivePanel, BoxLayout.Y_AXIS));

//		perspectivePanel.add(table);
//
//		JPanel fileInputPanel = new JPanel();
//		fileInputPanel.setLayout(new BoxLayout(fileInputPanel, BoxLayout.X_AXIS));
//		fileInputPanel.add(new JLabel("Pointing File:"));
//		fileInputPanel.add(Box.createHorizontalStrut(20));
//		pointingFilenameTextField = new JTextField();
//		pointingFilenameTextField.setMinimumSize(new Dimension(275, 30));
//		pointingFilenameTextField.setPreferredSize(new Dimension(275, 30));
//		pointingFilenameTextField.setMaximumSize(new Dimension(275, 30));
//
//		fileInputPanel.add(Box.createHorizontalStrut(10));
//		fileInputPanel.add(pointingFilenameTextField);
//		fileInputPanel.add(Box.createHorizontalStrut(10));
//		JButton browseButton = new JButton("Browse");
//
//		browseButton.addActionListener(e -> {
//			File file = CustomFileChooser.showOpenDialog(this, "Select Pointing File...");
//	        if (file == null)
//	        {
//	            return;
//	        }
//
//	        String filename = file.getAbsolutePath();
//	        pointingFilenameTextField.setText(filename);
//		});
//
//		fileInputPanel.add(browseButton);
//		fileInputPanel.add(Box.createHorizontalStrut(100));
//
//
//		perspectivePanel.add(fileInputPanel);
//
//		perspectivePanel.add(buildImageRotationInput());
//		perspectivePanel.add(buildImageFlipInput());
//
//		/////////////////
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS));
		optionPanel.add(new JLabel("Pointing Type:"));
//		optionPanel.add(Box.createHorizontalGlue());
		pointingTypeComboBox = new JComboBox<String>(new String[] {"Perspective Projection", "Simple Cylindrical Projection"});
		pointingTypeComboBox.setMaximumSize(new Dimension(350, 30));
		pointingTypeComboBox.addItemListener(new ItemListener()
		{

			@Override
			public void itemStateChanged(ItemEvent arg0)
			{
				CardLayout cl = (CardLayout)(cardPanel.getLayout());
				cl.show(cardPanel, arg0.getItem().toString());
			}
		});
		optionPanel.add(pointingTypeComboBox);
		optionPanel.add(Box.createHorizontalStrut(100));

		panel.add(optionPanel);
		cardPanel.add(perspectivePanel, "Perspective Projection");
		cardPanel.add(cylindricalPanel, "Simple Cylindrical Projection");
		panel.add(cardPanel);
		return panel;
	}

	private JPanel buildSubmitCancelPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(e -> {
//			String errorString = validateInput();
//	        if (errorString != null)
//	        {
//	            JOptionPane.showMessageDialog(this,
//	                    errorString,
//	                    "Error",
//	                    JOptionPane.ERROR_MESSAGE);
//	            return;
//	        }

	        saveImagesToCollection();
	        setVisible(false);
		});

		cancelButton.addActionListener(e -> setVisible(false));

		panel.add(okButton);
		panel.add(cancelButton);
		return panel;
	}

	private void saveImagesToCollection()
	{
		List<G1> images = tempCollection.getAllItems();
//		System.out.println("CustomImageImporterDialog2: saveImagesToCollection: number of images to save to collection " + images.size());
//		System.out.println("CustomImageImporterDialog2: saveImagesToCollection: image collection size " + imageCollection.size());
//		System.out.println("CustomImageImporterDialog2: saveImagesToCollection: type flip " + instrument.getFlip() + " and rotation " + instrument.getRotation());
		for (G1 image : images)
		{
			imageCollection.addUserImage(image);
		}
		imageCollection.loadUserList();
		imageCollection.setImagingInstrument(null);
	}

	private G1 resolvePointingFilename(String filename) throws IOException
	{
		String newFilepath = imageCollection.getSmallBodyModels().get(0).getCustomDataFolder() + File.separator + new File(filename).getName();
		FileUtil.copyFile(filename,  newFilepath);


		String withoutExtension = FilenameUtils.removeExtension(filename);
		String pointingSource = "";
		ImageSource pointingSourceType = null;
		if (pointingTypeComboBox.getSelectedItem() == "Perspective Projection")
		{
			if (new File(withoutExtension + ".SUM").exists())
			{
				pointingSource = new File(withoutExtension + ".SUM").getAbsolutePath();
				pointingSourceType = ImageSource.GASKELL;
			}
			else if (new File(withoutExtension + ".INFO").exists())
			{
				pointingSource = new File(withoutExtension + ".INFO").getAbsolutePath();
				pointingSourceType = ImageSource.SPICE;
			}
			else
			{
				pointingSource = new File(withoutExtension + ".LBL").getAbsolutePath();
				pointingSourceType = ImageSource.LABEL;
			}
		}
		else
			pointingSourceType = ImageSource.LOCAL_CYLINDRICAL;

		String newPointingFilepath = "";
		if (!pointingSource.isEmpty())
		{
			newPointingFilepath = imageCollection.getSmallBodyModels().get(0).getCustomDataFolder() + File.separator + new File(pointingSource).getName();
			if (new File(pointingSource).exists())
				FileUtil.copyFile(pointingSource,  newPointingFilepath);
			else
			{
				newPointingFilepath = "FILE NOT FOUND";
//				JOptionPane.showMessageDialog(this, "No pointing found; please click OK to select a file");
//				File newPointingFile = CustomFileChooser.showOpenDialog(this, "Can't determine pointing file - please choose one");
//		        if (newPointingFile != null)
//		        {
//		            newPointingFilepath = newPointingFile.getAbsolutePath();
//		        }
			}
			String extension = FilenameUtils.getExtension(pointingSource).toLowerCase();
			pointingSourceType = extension.equals("sum") ? ImageSource.GASKELL : ImageSource.SPICE;
			return storeImage(filename, newFilepath, pointingSourceType, newPointingFilepath);
		}
		return storeImage(filename, newFilepath, pointingSourceType, newPointingFilepath);
	}

	private G1 storeImage(String filename, String newFilepath, ImageSource pointingSourceType, String newPointingFilepath)
	{
		ImageType imageType = (ImageType)imageTypeComboBox.getSelectedItem();

		double[] fillValues = new double[] {};
		PerspectiveImage image = new PerspectiveImage(newFilepath, imageType, pointingSourceType, newPointingFilepath, fillValues);

		image.setName(getName());
		image.setImageOrigin(ImageOrigin.LOCAL);
		image.setLongTime(new Date().getTime());
		if (pointingSourceType == ImageSource.LOCAL_CYLINDRICAL)
		{
			image.setBounds(new CylindricalBounds(-90,90,0,360));
		}
		else
		{
			if (instrument != null)
			{
				image.setLinearInterpolatorDims(instrument.getLinearInterpolationDims());
				image.setMaskValues(instrument.getMaskValues());
				image.setFillValues(instrument.getFillValues());
				image.setFlip(instrument.getFlip());
				image.setRotation(instrument.getRotation());
			}
		}
		CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
		compImage.setName(FilenameUtils.getBaseName(filename));
		return (G1)compImage;
	}
}
