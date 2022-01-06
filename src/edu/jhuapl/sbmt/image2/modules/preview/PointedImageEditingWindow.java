package edu.jhuapl.sbmt.image2.modules.preview;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.view.light.LightUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.modules.io.export.SaveModifiedImagePointingFileToCacheOperator;
import edu.jhuapl.sbmt.image2.modules.pointing.offset.PointedImageEditingPipeline;
import edu.jhuapl.sbmt.image2.modules.pointing.offset.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image2.modules.pointing.offset.SpacecraftPointingState;
import edu.jhuapl.sbmt.image2.modules.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipeline.active.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.active.pointedImages.RenderablePointedImageActorPipeline;
import edu.jhuapl.sbmt.image2.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image2.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.IPipelineSubscriber;
import edu.jhuapl.sbmt.image2.pipeline.subscriber.Sink;

public class PointedImageEditingWindow<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> implements IPipelineSubscriber<vtkActor>
{
	private IPipelinePublisher<vtkActor> publisher;
	private SmallBodyModel smallBodyModel;
	private PointedImageEditingPanel preview;
	private G1 image;

	public PointedImageEditingWindow(G1 image, SmallBodyModel smallBodyModel)
	{
		this.image = image;
		this.smallBodyModel = smallBodyModel;
	}

	@Override
	public void receive(List<vtkActor> items)
	{
		try
		{
			preview = new PointedImageEditingPanel(image, smallBodyModel, items);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Container getPanel()
	{
		return preview.getContentPane();
	}

	@Override
	public void setPublisher(IPipelinePublisher<vtkActor> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public void run() throws IOException, Exception
	{
		publisher.run();
	}
}

class PointedImageEditingPanel<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends ModelInfoWindow
		implements MouseListener, MouseMotionListener, PropertyChangeListener
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

	// private vtkJoglPanelComponent renWin;
	private Renderer renderer;

	private JButton rotateLeftButton;
	private JButton rotateRightButton;
	private JButton upButton;
	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton leftButton;
	private JButton resetFrameAdjustmentsButton;
	private JButton rightButton;
	private JPanel pointingPanel;
	private JButton downButton;
	private JLabel factorLabel;
	private JLabel factorLabel1;
	private JTextField factorTextField1;
	private JCheckBox interpolateCheckBox1;
	private JCheckBox adjustFrameCheckBox3;
	private GridBagConstraints gridBagConstraints;
	private G1 image;
	private boolean resetOnNextUpdate;
	private double currentZoomFactor = 1.0;
	private double currentSampleOffset = 0.0;
	private double currentLineOffset = 0.0;
	private double currentRotationAngle = 0.0;
	private boolean currentInterpolateState = false;
	private SmallBodyModel smallBodyModel;
	List<vtkProp> props = Lists.newArrayList();
	VtkPropProvider propProvider;
	List<vtkActor> inputs;

	public PointedImageEditingPanel(G1 image, SmallBodyModel smallBodyModel, List<vtkActor> inputs)
	{
		this.image = image;
		this.inputs = inputs;
		this.smallBodyModel = smallBodyModel;
		this.renderer = new Renderer(smallBodyModel);
		renderer.setLightCfg(LightUtil.getSystemLightCfg());
		propProvider = new VtkPropProvider()
		{

			@Override
			public Collection<vtkProp> getProps()
			{
				return props;
			}
		};
		renderer.addVtkPropProvider(propProvider);
		initComponents();
		loadActors(inputs);
		createMenus();

		setTitle("Renderer Preview");

		pack();
		setVisible(true);

		// initialized = true;

		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				renderer.getRenderWindowPanel().resetCamera();
				renderer.getRenderWindowPanel().Render();
			}
		});
	}

	private void loadActors(List<vtkActor> inputs)
	{
		props.clear();
		for (vtkActor actor : inputs)
		{
			if (inputs.indexOf(actor) == 1) actor.SetVisibility(0);
			props.add(actor);
		}
		if (renderer.hasVtkPropProvider(propProvider))
			renderer.addVtkPropProvider(propProvider);
		renderer.notifySceneChange();
	}

	@Override
	public Model getModel()
	{
		return null;
		// return image;
	}

	@Override
	public Model getCollectionModel()
	{
		return null; // imageCollection;
	}

	private void initComponents()
	{
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(775, 900));
		getContentPane().setLayout(new GridBagLayout());

		initRenderer();

		factorLabel = new JLabel();
        pointingPanel = new JPanel();
        leftButton = new JButton();
        rightButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        rotateLeftButton = new JButton();
        zoomOutButton = new JButton();
        zoomInButton = new JButton();
        rotateRightButton = new JButton();
        interpolateCheckBox1 = new JCheckBox();
        resetFrameAdjustmentsButton = new JButton();
        adjustFrameCheckBox3 = new JCheckBox();
        factorLabel1 = new JLabel();
        factorTextField1 = new JTextField();

        pointingPanel.setLayout(new GridBagLayout());

		leftButton.setText("<");
		leftButton.setToolTipText("left");
		leftButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					leftButtonActionPerformed(evt);
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
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(leftButton, gridBagConstraints);

		rightButton.setText(">");
		rightButton.setToolTipText("right");
		rightButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					rightButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		pointingPanel.add(rightButton, gridBagConstraints);

		upButton.setText("^");
		upButton.setToolTipText("up");
		upButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					upButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(upButton, gridBagConstraints);

		downButton.setText("v");
		downButton.setToolTipText("down");
		downButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					downButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(downButton, gridBagConstraints);

		rotateLeftButton.setText("\\");
		rotateLeftButton.setToolTipText("rotate left");
		rotateLeftButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					rotateLeftButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(rotateLeftButton, gridBagConstraints);

		zoomOutButton.setText("-><-");
		zoomOutButton.setToolTipText("zoom out");
		zoomOutButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					zoomOutButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(zoomOutButton, gridBagConstraints);

		zoomInButton.setText("<-->");
		zoomInButton.setToolTipText("zoom in");
		zoomInButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					zoomInButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		pointingPanel.add(zoomInButton, gridBagConstraints);

		rotateRightButton.setText("/");
		rotateRightButton.setToolTipText("rotate right");
		rotateRightButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					rotateRightButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 7;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(rotateRightButton, gridBagConstraints);

		interpolateCheckBox1.setSelected(true);
		interpolateCheckBox1.setText("Interpolate Pixels");
		interpolateCheckBox1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					interpolateCheckBox1ActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.ipadx = 15;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		pointingPanel.add(interpolateCheckBox1, gridBagConstraints);

		resetFrameAdjustmentsButton.setText("Reset Pointing");
		resetFrameAdjustmentsButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					resetFrameAdjustmentsButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(resetFrameAdjustmentsButton, gridBagConstraints);

		adjustFrameCheckBox3.setText("Select Target");
		adjustFrameCheckBox3.setName(""); // NOI18N
		adjustFrameCheckBox3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					adjustFrameCheckBox3ActionPerformed(evt);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.ipadx = 15;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(adjustFrameCheckBox3, gridBagConstraints);

		factorLabel1.setText("Factor");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 15;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(factorLabel1, gridBagConstraints);

		// factorTextField1.setColumns(5);
		factorTextField1.setText("1.0");
		factorTextField1.setPreferredSize(new Dimension(14, 28));
		factorTextField1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				factorTextField1ActionPerformed(evt);
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(factorTextField1, gridBagConstraints);

		TitledBorder pointingBorder = BorderFactory.createTitledBorder("Pointing Adjustments");
		pointingBorder.setTitleJustification(TitledBorder.CENTER);
		pointingPanel.setBorder(pointingBorder);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;

		getContentPane().add(pointingPanel, gridBagConstraints);

		pack();
	}

	private void initRenderer()
	{
		// renWin = new vtkJoglPanelComponent();
		// renWin.getComponent().setPreferredSize(new Dimension(550, 550));
		//
		// vtkInteractorStyleImage style = new vtkInteractorStyleImage();
		// renWin.setInteractorStyle(style);
		//
		//// renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent",
		// this,
		//// "levelsChanged");
		//
		//// updateImage(displayedImage);
		//
		//
		//// renWin.getRenderer().AddActor(actor);
		//
		// renWin.setSize(550, 550);
		//// renWin.getRenderer().SetBackground(new double[] {0.5f, 0.5f,
		// 0.5f});
		//
		//// imagePicker = new vtkPropPicker();
		//// imagePicker.PickFromListOn();
		//// imagePicker.InitializePickList();
		//// vtkPropCollection smallBodyPickList = imagePicker.GetPickList();
		//// smallBodyPickList.RemoveAllItems();
		//// imagePicker.AddPickList(actor);
		// renWin.getComponent().addMouseListener(this);
		// renWin.getComponent().addMouseMotionListener(this);
		// renWin.getRenderer().GetActiveCamera().Dolly(0.2);
		// renWin.addKeyListener(this);

		// Trying to add a vtksbmtJoglCanvasComponent in the netbeans gui
		// does not seem to work so instead add it here.
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(renderer, gridBagConstraints);
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
				File file = CustomFileChooser.showSaveDialog(renderer.getRenderWindowPanel().getComponent(),
						"Export to PNG Image...", "image.png", "png");
				RenderIoUtil.saveToFile(file, renderer.getRenderWindowPanel(), null);
			}
		});
		fileMenu.add(mi);
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		/**
		 * The following is a bit of a hack. We want to reuse the PopupMenu
		 * class, but instead of having a right-click popup menu, we want
		 * instead to use it as an actual menu in a menu bar. Therefore we
		 * simply grab the menu items from that class and put these in our new
		 * JMenu.
		 */
		// ImagePopupMenu imagesPopupMenu = new ImagePopupMenu(null,
		// imageCollection, imageBoundaryCollection, null, null,
		// null, this);
		//
		// imagesPopupMenu.setCurrentImage(image.getKey());
		//
		// JMenu menu = new JMenu("Options");
		// menu.setMnemonic('O');
		//
		// Component[] components = imagesPopupMenu.getComponents();
		// for (Component item : components)
		// {
		// if (item instanceof JMenuItem)
		// {
		// // Do not show the "Show Image" option since that creates
		// // problems
		// // since it's supposed to close this window also.
		// if (!(((JMenuItem) item).getAction() instanceof
		// ImagePopupMenu.MapImageAction))
		// menu.add(item);
		// }
		// }
		//
		// menuBar.add(menu);

		setJMenuBar(menuBar);
	}

	private void runEditingPipeline() throws Exception
	{
		List<RenderablePointedImage> renderableImages;
		PerspectiveImageToRenderableImagePipeline pipeline =
				new PerspectiveImageToRenderableImagePipeline(List.of(image));
		renderableImages = pipeline.getRenderableImages();
		SpacecraftPointingState origState =
				new SpacecraftPointingState(renderableImages.get(0).getPointing(), renderableImages.get(0).getImageWidth(), renderableImages.get(0).getImageHeight());
		SpacecraftPointingDelta delta = generateDelta();

		PointedImageEditingPipeline editingPipeline =
				new PointedImageEditingPipeline(origState, delta);
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> updatedState = editingPipeline.getFinalState();
//		System.out.println("PointedImageEditingPanel: runEditingPipeline: original state " + origState);
//		System.out.println("PointedImageEditingPanel: runEditingPipeline: delta is " + updatedState.getRight());
//		System.out.println("PointedImageEditingPanel: runEditingPipeline: new state is " + updatedState.getLeft());

		//set modified pointing object to image

		//save that to disk
		List<File> updatedPointingFiles = Lists.newArrayList();
		Triple<G1, SpacecraftPointingState, SpacecraftPointingDelta> input =
				Triple.of(image, updatedState.getLeft(), delta);
		Just.of(input)
			.operate(new SaveModifiedImagePointingFileToCacheOperator<G1>())
			.subscribe(Sink.of(updatedPointingFiles))
			.run();
//		System.out.println("PointedImageEditingPanel: runEditingPipeline: setting modified path to " + updatedPointingFiles.get(0).getAbsolutePath());
		image.setModifiedPointingSource(Optional.of(updatedPointingFiles.get(0).getAbsolutePath()));

		RenderablePointedImageActorPipeline<G1> actorPipeline =
				new RenderablePointedImageActorPipeline<G1>(image, List.of(smallBodyModel));

		List<vtkActor> allActors = Lists.newArrayList();
		allActors.add(inputs.get(0));

		//TODO move this to properties pane; doesn't belong in pointing pane; should impact imageslice as well as rendered 3d actor
//		List<vtkActor> regularActors = actorPipeline.getRenderableImageActors();
//		regularActors.forEach(actor -> {
//			if (currentInterpolateState) actor.GetProperty().SetInterpolationTypeToLinear();
//			else actor.GetProperty().SetInterpolationTypeToNearest();
//		} );
		allActors.addAll(actorPipeline.getRenderableImageActors());
		allActors.addAll(actorPipeline.getRenderableModifiedImageActors());
		loadActors(allActors);
	}

	private SpacecraftPointingDelta generateDelta()
	{
		SpacecraftPointingDelta delta = new SpacecraftPointingDelta();
		if (resetOnNextUpdate)
		{
			factorTextField1.setText("1.0");
			currentLineOffset = 0.0;
			currentSampleOffset = 0.0;
			currentRotationAngle = 0.0;
			currentZoomFactor = 1.0;
			resetOnNextUpdate = false;
			return delta;
		}
		delta.setLineOffset(currentLineOffset);
//		delta.setPitchOffset(ROTATION_DELTA);
		delta.setRotationOffset(currentRotationAngle);
		delta.setSampleOffset(currentSampleOffset);
//		delta.setYawOffset(ROTATION_DELTA);
		delta.setZoomFactor(currentZoomFactor);

		return delta;
	}


	private void zoomInButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentZoomFactor *= Math.pow(1.1, -getAdjustFactor());
		runEditingPipeline();
	}

	private void leftButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentLineOffset += -getAdjustFactor()/1000.0;
		runEditingPipeline();
	}

	private void rightButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentLineOffset += getAdjustFactor()/1000.0;
		runEditingPipeline();
	}

	private void upButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentSampleOffset += -getAdjustFactor()/1000.0;
		runEditingPipeline();
	}

	private void downButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentSampleOffset += getAdjustFactor()/1000.0;
		runEditingPipeline();
	}

	private void rotateLeftButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentRotationAngle += -getAdjustFactor();
		runEditingPipeline();
	}

	private void rotateRightButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentRotationAngle += getAdjustFactor();
		runEditingPipeline();
	}

	private void interpolateCheckBox1ActionPerformed(ActionEvent evt) throws Exception
	{
		currentInterpolateState = interpolateCheckBox1.isSelected();
		image.setInterpolateState(currentInterpolateState);
		runEditingPipeline();
//		if (image instanceof PerspectiveImage)
//		{
//			boolean interpolate = interpolateCheckBox1.isSelected();
//			((PerspectiveImage) image).setInterpolate(interpolate);
//			if (interpolate)
//				actor.GetProperty().SetInterpolationTypeToLinear();
//			else
//				actor.GetProperty().SetInterpolationTypeToNearest();
//			((PerspectiveImage) image).firePropertyChange();
//		}
	}

	private void resetFrameAdjustmentsButtonActionPerformed(ActionEvent evt) throws Exception
	{
		resetOnNextUpdate = true;
		runEditingPipeline();
	}

	private void adjustFrameCheckBox3ActionPerformed(ActionEvent evt) throws Exception
	{
		//TODO I HAVE NO CLUE WHAT THIS DID ORIGINALLY
		runEditingPipeline();
		// System.out.println("Adjust frame...");
//		centerFrustumMode = adjustFrameCheckBox3.isSelected();
	}

	private void zoomOutButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentZoomFactor *= Math.pow(1.1, getAdjustFactor());
		runEditingPipeline();
	}

	private double getAdjustFactor()
	{
		return Double.parseDouble(factorTextField1.getText());
	}

	private void factorTextField1ActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    }

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void propertyChange(PropertyChangeEvent arg0)
	{
		if (renderer.getRenderWindowPanel().getRenderWindow().GetNeverRendered() > 0)
			return;
		renderer.getRenderWindowPanel().Render();
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		// if (centerFrustumMode && e.getButton() == 1)
		// {
		// if (e.isAltDown())
		// {
		// // System.out.println("Resetting pointing...");
		// // ((PerspectiveImage)image).resetSpacecraftState();
		// }
		// else
		// {
		// centerFrustumOnPixel(e);
		//
		// ((PerspectiveImage) image).loadFootprint();
		// // ((PerspectiveImage)image).calculateFrustum();
		// }
		// // PerspectiveImageBoundary boundary =
		// // imageBoundaryCollection.getBoundary(image.getKey());
		// // boundary.update();
		// // ((PerspectiveImageBoundary)boundary).firePropertyChange();
		//
		// ((PerspectiveImage) image).firePropertyChange();
		// }

		// int pickSucceeded = doPick(e, imagePicker, renWin);
		// if (pickSucceeded == 1)
		{
			// double[] p = imagePicker.GetPickPosition();
			//
			// // Display selected pixel coordinates in console output
			// // Note we reverse x and y so that the pixel is in the form the
			// // camera
			// // position/orientation program expects.
			// System.out.println(p[1] + " " + p[0]);
			//
			// // Display status bar message upon being picked
			// refStatusHandler.setLeftTextSource(image, null, 0, p);
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
		// if (centerFrustumMode && !e.isAltDown())
		// {
		//// ((PerspectiveImage) image).calculateFrustum();
		//// ((PerspectiveImage) image).firePropertyChange();
		// }
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		// if (centerFrustumMode && e.getButton() == 1)
		// {
		// if (!e.isAltDown())
		// {
		// centerFrustumOnPixel(e);
		// ((PerspectiveImage) image).loadFootprint();
		// }
		//
		// ((PerspectiveImage) image).firePropertyChange();
		//
		// }
		// else
		// updateSpectrumRegion(e);
	}

}