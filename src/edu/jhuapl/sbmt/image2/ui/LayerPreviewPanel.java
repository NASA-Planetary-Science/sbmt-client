package edu.jhuapl.sbmt.image2.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkImageSlice;
import vtk.vtkImageSliceMapper;
import vtk.vtkInteractorStyleImage;
import vtk.vtkPropPicker;
import vtk.vtkTransform;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image2.controllers.preview.ImageContrastController;
import edu.jhuapl.sbmt.image2.controllers.preview.ImageMaskController;
import edu.jhuapl.sbmt.image2.controllers.preview.ImagePropertiesController;
import edu.jhuapl.sbmt.image2.controllers.preview.ImageTrimController;
import edu.jhuapl.sbmt.image2.model.ImageProperty;
import edu.jhuapl.sbmt.image2.pipeline.rendering.vtk.VtkImageContrastPipeline;
import edu.jhuapl.sbmt.image2.pipeline.rendering.vtk.VtkImageMaskingPipeline;
import edu.jhuapl.sbmt.image2.pipeline.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class LayerPreviewPanel extends ModelInfoWindow implements MouseListener, MouseMotionListener, PropertyChangeListener
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

	VtkImageMaskingPipeline maskPipeline;
	ImageTrimController trimController;
	ImageMaskController maskController;
	ImageContrastController contrastController;
	private List<Layer> layers;
	private Layer layer;
	private vtkJoglPanelComponent renWin;
	private vtkImageSlice actor = new vtkImageSlice();
	private vtkImageReslice reslice;
	private vtkPropPicker imagePicker;
	private boolean initialized = false;
	private boolean centerFrustumMode = false;
	private JScrollPane jScrollPane1;
	private JPanel tablePanel;
	private int[] previousLevels = null;
	private vtkImageData displayedImage;
	private HashMap<String, String> metadata;
	private List<HashMap<String, String>> metadatas;
	private Runnable completionBlock;
	private JComboBox<String> layerComboBox;

	public LayerPreviewPanel(String title, final List<Layer> layers, List<HashMap<String, String>> metadatas, Runnable completionBlock) throws IOException, Exception
	{
		this.layers = layers;
		this.layer = layers.get(0);
		this.metadatas = metadatas;
		this.metadata = metadatas.get(0);
		this.maskPipeline = new VtkImageMaskingPipeline();
		this.completionBlock = completionBlock;
		initComponents();
		renderLayer(layer);
		setIntensity(null);

		createMenus();
		setTitle(title);

		pack();
		setVisible(true);

		initialized = true;

		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				renWin.resetCamera();
				renWin.Render();
			}
		});
	}

	public void setCompletionBlock(Runnable completionBlock)
	{
		this.completionBlock = completionBlock;
	}

//	private void maskingChanged()
//	{
//		try
//		{
//			maskPipeline.run(layer,
//					(int)leftSpinner.getValue(), (int)rightSpinner.getValue(),
//					(int)bottomSpinner.getValue(), (int)topSpinner.getValue());
//
//			Layer layer = maskPipeline.getUpdatedData().get(0);
//			generateVtkImageData(layer);
//			updateImage(displayedImage);
//			setIntensity(null);
//			renWin.Render();
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	private void setIntensity(IntensityRange range) throws IOException, Exception
	{
		VtkImageContrastPipeline pipeline = new VtkImageContrastPipeline(displayedImage, range);
		displayedImage = pipeline.getUpdatedData().get(0);
		updateImage(displayedImage);
		if (completionBlock != null) completionBlock.run();
	}

	private void generateVtkImageData(Layer layer) throws IOException, Exception
	{
		List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
		IPipelinePublisher<Layer> reader = new Just<Layer>(layer);
		reader.
			operate(new VtkImageRendererOperator()).
			subscribe(new Sink<vtkImageData>(displayedImages)).run();
		displayedImage = displayedImages.get(0);
		contrastController.setImageData(displayedImage);
	}

	private void renderLayer(Layer layer) throws IOException, Exception
	{
		generateVtkImageData(layer);

		renWin = new vtkJoglPanelComponent();
		renWin.getComponent().setPreferredSize(new Dimension(550, 550));

		vtkInteractorStyleImage style = new vtkInteractorStyleImage();
		renWin.setInteractorStyle(style);

//		renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent", this,
//				"levelsChanged");

		updateImage(displayedImage);

		renWin.getRenderer().AddActor(actor);

		renWin.setSize(550, 550);
		renWin.getRenderer().SetBackground(new double[] {0.5f, 0.5f, 0.5f});

		renWin.getComponent().addMouseListener(this);
		renWin.getComponent().addMouseMotionListener(this);
		renWin.getRenderer().GetActiveCamera().Dolly(0.2);
		// renWin.addKeyListener(this);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(renWin.getComponent(), gridBagConstraints);
	}

	private void updateImage(vtkImageData displayedImage)
	{
		double[] center = displayedImage.GetCenter();
		int[] dims = displayedImage.GetDimensions();
		// Rotate image by 90 degrees so it appears the same way as when you
		// use the Center in Image option.
		vtkTransform imageTransform = new vtkTransform();
		imageTransform.Translate(center[0], center[1], 0.0);
		imageTransform.RotateZ(0.0);
		imageTransform.Translate(-center[1], -center[0], 0.0);

		reslice = new vtkImageReslice();
		reslice.SetInputData(displayedImage);
//		reslice.SetResliceTransform(imageTransform);
		reslice.SetInterpolationModeToNearestNeighbor();
		reslice.SetOutputSpacing(1.0, 1.0, 1.0);
		reslice.SetOutputOrigin(0.0, 0.0, 0.0);
		reslice.SetOutputExtent(0, dims[0] - 1, 0, dims[1] - 1, 0, dims[2]);
		reslice.Update();

		vtkImageSliceMapper imageSliceMapper = new vtkImageSliceMapper();
		imageSliceMapper.SetInputConnection(reslice.GetOutputPort());
		imageSliceMapper.Update();

		actor.SetMapper(imageSliceMapper);
		actor.GetProperty().SetInterpolationTypeToLinear();
	}

	@Override
	public Model getModel()
	{
		return null;
//		return image;
	}

	@Override
	public Model getCollectionModel()
	{
		return null; //imageCollection;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{
		GridBagConstraints gridBagConstraints;
		List<ImageProperty> properties = new ArrayList<ImageProperty>();
		for (String str : metadata.keySet())
			properties.add(new ImageProperty(str, metadata.get(str)));
		ImagePropertiesController propertiesController = new ImagePropertiesController(properties);
		tablePanel = propertiesController.getView();
		jScrollPane1 = new JScrollPane();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(775, 900));
		getContentPane().setLayout(new GridBagLayout());

		if (layers.size() > 1)
		{
			String[] layerNames = new String[layers.size()];
			metadatas.get(0).keySet().stream().filter(item -> item.contains("PLANE")).map(key  -> key + " - " + metadatas.get(0).get(key)).sorted().toList().toArray(layerNames);

			layerComboBox = new JComboBox<String>(layerNames);
			layerComboBox.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					String title = (String)layerComboBox.getSelectedItem();
					int index = Integer.parseInt(title.split(" ")[0].replace("PLANE", "")) - 1;
					try
					{
						generateVtkImageData(layers.get(index));
						updateImage(displayedImage);
						setIntensity(null);
						renWin.Render();
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.weightx = 1.0;
			getContentPane().add(layerComboBox, gridBagConstraints);
		}

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		getContentPane().add(tablePanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = getContentPane().getWidth();
		gridBagConstraints.weightx = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		contrastController = new ImageContrastController(displayedImage, new IntensityRange(0, 255), new Function<vtkImageData, Void>() {

			@Override
			public Void apply(vtkImageData t)
			{
				try
				{
					displayedImage = t;
					updateImage(displayedImage);
					setIntensity(null);
					renWin.Render();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});
		getContentPane().add(contrastController.getView(), gridBagConstraints);



		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(3, 6, 3, 0);
		trimController = new ImageTrimController(layer, new Function<Layer, Void>()
		{

			@Override
			public Void apply(Layer t)
			{
				try
				{
					generateVtkImageData(t);
					updateImage(displayedImage);
					setIntensity(null);
					renWin.Render();
					layer = t;
//					maskController.setLayer(t);
//					trimController.setLayer(t);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});

		getContentPane().add(trimController.getView(), gridBagConstraints);

		pack();
	}

	public IntensityRange getIntensityRange()
	{
		return contrastController.getIntensityRange();
	}

	public int[] getMaskValues()
	{
		return trimController.getMaskValues();
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (centerFrustumMode && e.getButton() == 1)
		{
//			if (e.isAltDown())
//			{
//				// System.out.println("Resetting pointing...");
//				// ((PerspectiveImage)image).resetSpacecraftState();
//			}
//			else
//			{
//				centerFrustumOnPixel(e);
//
//				((PerspectiveImage) image).loadFootprint();
//				// ((PerspectiveImage)image).calculateFrustum();
//			}
//			// PerspectiveImageBoundary boundary =
//			// imageBoundaryCollection.getBoundary(image.getKey());
//			// boundary.update();
//			// ((PerspectiveImageBoundary)boundary).firePropertyChange();
//
//			((PerspectiveImage) image).firePropertyChange();
		}

//		int pickSucceeded = doPick(e, imagePicker, renWin);
//		if (pickSucceeded == 1)
		{
//			double[] p = imagePicker.GetPickPosition();
//
//			// Display selected pixel coordinates in console output
//			// Note we reverse x and y so that the pixel is in the form the
//			// camera
//			// position/orientation program expects.
//			System.out.println(p[1] + " " + p[0]);
//
//			// Display status bar message upon being picked
//			refStatusHandler.setLeftTextSource(image, null, 0, p);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (centerFrustumMode && !e.isAltDown())
		{
//			((PerspectiveImage) image).calculateFrustum();
//			((PerspectiveImage) image).firePropertyChange();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (centerFrustumMode && e.getButton() == 1)
		{
//			if (!e.isAltDown())
//			{
//				centerFrustumOnPixel(e);
//				((PerspectiveImage) image).loadFootprint();
//			}
//
//			((PerspectiveImage) image).firePropertyChange();

		}
//		else
//			updateSpectrumRegion(e);
	}

//	private void interpolateCheckBox1ActionPerformed(ActionEvent evt) throws Exception
//	{
//		currentInterpolateState = interpolateCheckBox1.isSelected();
//		image.setInterpolateState(currentInterpolateState);
//		runEditingPipeline();
//	}

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void propertyChange(PropertyChangeEvent arg0)
	{
		if (renWin.getRenderWindow().GetNeverRendered() > 0)
			return;
		renWin.Render();
	}

	private void createMenus()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File file = CustomFileChooser.showSaveDialog(renWin.getComponent(), "Export to PNG Image...",
						"image.png", "png");
				RenderIoUtil.saveToFile(file, renWin, null);
			}
		});
		fileMenu.add(mi);
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
	}
}