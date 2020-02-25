package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.ColormapUtil;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.util.ColorIcon;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.RendererLookDirection;

import glum.gui.GuiUtil;

public class StateHistoryViewControlsPanel extends JPanel implements ActionListener
{

    private JComboBox<RendererLookDirection> viewOptions;
    private JButton btnResetCameraTo;
//    private JButton saveAnimationButton;
    private JButton setViewAngle;
    private JSlider spacecraftSlider;
    private JCheckBox showSpacecraftMarker;
    private JSlider sunSlider;
    private JCheckBox showSunPointer;
    private JSlider earthSlider;
    private JCheckBox showEarthPointer;
    private JCheckBox showLighting;
    private JComboBox<String> distanceOptions;
    private JCheckBox showSpacecraft;
    private JLabel earthText;
    private JLabel sunText;
    private JLabel spacecraftText;
    private JLabel lblSelectView;
    private JLabel lblVerticalFov;
    private JTextField viewInputAngle;
    private JButton resizeEarthPointerButton;
    private JButton scLabelButton;
    private JButton resizeSunPointerButton;
    private JButton resizeSpacecraftPointerButton;
    private JPanel viewOptionsPanel;
    private JPanel widgetPanel;
    private JPanel coloringPanel;

    private JSlider scSizeSlider;

    private JCheckBox labelCheckBox;

    private Boolean scLabelShown = false;
    private Boolean earthResizeShown = false;
    private Boolean sunResizeShown = false;
    private Boolean scResizeShown = false;

    private JButton scPointerColorButton;
    private JButton sunPointerColorButton;
    private JButton earthPointerColorButton;
    private JButton spacecraftColorButton;


    private Color scPointerColor = Color.GREEN;
    private Color sunPointerColor = Color.YELLOW;
    private Color earthPointerColor;
    private Color spacecraftColor;

    private JLabel scPointerColorLabel;
    private JLabel sunPointerColorLabel;
    private JLabel earthPointerColorLabel;

    private Image questionImage = null;
    private Icon questionIcon;

    private JLabel colorFunctionLabel;
    private JLabel colorRampLabel;
    private JComboBox<StateHistoryColoringFunctions> colorFunctionComboBox;
    private JComboBox colormapComboBox;

    private JButton labelFontButton;

    private Font labelFont;

    private int iconW;

    @Override
    public void setEnabled(boolean enabled)
    {
    	viewOptions.setEnabled(enabled);
    	btnResetCameraTo.setEnabled(enabled);
//    	saveAnimationButton.setEnabled(enabled);
    	setViewAngle.setEnabled(enabled);
//    	spacecraftSlider.setEnabled(enabled);
    	showSpacecraftMarker.setEnabled(enabled);
//    	sunSlider.setEnabled(enabled);
    	showSunPointer.setEnabled(enabled);
//    	earthSlider.setEnabled(enabled);
    	showEarthPointer.setEnabled(enabled);
    	showLighting.setEnabled(enabled);
//    	distanceOptions.setEnabled(enabled);
    	showSpacecraft.setEnabled(enabled);
    	earthText.setEnabled(enabled);
    	sunText.setEnabled(enabled);
    	spacecraftText.setEnabled(enabled);
    	lblSelectView.setEnabled(enabled);
    	lblVerticalFov.setEnabled(enabled);
    	viewInputAngle.setEnabled(enabled);
    	scLabelButton.setEnabled(enabled);
    	resizeEarthPointerButton.setEnabled(enabled);
    	resizeSunPointerButton.setEnabled(enabled);
    	resizeSpacecraftPointerButton.setEnabled(enabled);
    	viewOptionsPanel.setEnabled(enabled);
    	widgetPanel.setEnabled(enabled);
    	labelCheckBox.setEnabled(enabled);
    	coloringPanel.setEnabled(enabled);
    	colorFunctionComboBox.setEnabled(enabled);
    	colorFunctionLabel.setEnabled(enabled);
    	colormapComboBox.setEnabled(enabled);
    	colorRampLabel.setEnabled(enabled);
    	super.setEnabled(enabled);
    }

	public StateHistoryViewControlsPanel()
	{
		earthPointerColor = Color.BLUE;
		spacecraftColor = new Color(1.0f, 0.7f, 0.4f, 1.0f);
		labelFont = getFont();
		initUI();
	}

	public StateHistoryViewControlsPanel(LayoutManager layout)
	{
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryViewControlsPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryViewControlsPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private void initUI()
	{
		setBorder(new TitledBorder(null, "View Controls",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        configureDisplayItemsPanel();
        configureViewOptionsPanel();
        configureColoringPanel();

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(viewOptionsPanel);
        rightPanel.add(coloringPanel);

        add(widgetPanel);
        add(rightPanel);

        iconW = (int) (scPointerColorLabel.getWidth() * 1.20);
        iconW = 30;

//        JPanel panel_16 = new JPanel();
//        panel_15.add(panel_16);
//
//        saveAnimationButton = new JButton("Save Movie Frames");
//        saveAnimationButton.setEnabled(false);
//        panel_16.add(saveAnimationButton);

//        updateGui();
	}

	private void configureShowSpacecraftControls()
	{
		//Show spacecraft panel
        JPanel panel_3 = new JPanel();
        widgetPanel.add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.Y_AXIS));

        JPanel scPanel1 = new JPanel();
        panel_3.add(scPanel1);
        scPanel1.setLayout(new BoxLayout(scPanel1, BoxLayout.X_AXIS));

        JPanel scPanel2 = new JPanel();
        panel_3.add(scPanel2);
        scPanel2.setLayout(new BoxLayout(scPanel2, BoxLayout.X_AXIS));

        JPanel scPanel3 = new JPanel();
        panel_3.add(scPanel3);
        scPanel3.setLayout(new BoxLayout(scPanel3, BoxLayout.X_AXIS));

        JPanel scPanel4 = new JPanel();
        panel_3.add(scPanel4);
        scPanel4.setLayout(new BoxLayout(scPanel4, BoxLayout.X_AXIS));

        showSpacecraft = new JCheckBox("Show Spacecraft");
        showSpacecraft.setEnabled(false);
        scPanel1.add(showSpacecraft);

        Component horizontalStrut = Box.createHorizontalStrut(25);
        scPanel1.add(horizontalStrut);


        scLabelButton = new JButton(questionIcon);
        scLabelButton.addActionListener(e -> {
        	updateGui();
        	scPanel2.setVisible(!scLabelShown);
        	scPanel3.setVisible(!scLabelShown);
        	scPanel4.setVisible(!scLabelShown);
        	scLabelShown = !scLabelShown;
        	scLabelButton.setText("");
        	if (scLabelShown)
        	{
        		scLabelButton.setText("Done");
        		scLabelButton.setIcon(null);
        	}
        	else
        		scLabelButton.setIcon(questionIcon);
        });
        scPanel1.add(scLabelButton);
        scPanel1.add(Box.createHorizontalGlue());

//        JLabel scText = new JLabel("Label:");
        labelCheckBox = new JCheckBox("Label:");
        labelCheckBox.setEnabled(false);
        scPanel2.add(labelCheckBox);

        distanceOptions = new JComboBox<String>();
        distanceOptions.setEnabled(false);
        distanceOptions.setPreferredSize(new Dimension(200, 30));
		distanceOptions.setMaximumSize(new Dimension(200, 30));
        scPanel2.add(distanceOptions);

        labelFontButton = new JButton(questionIcon);
        labelFontButton.setEnabled(false);

        scPanel2.add(labelFontButton);

        scPanel2.add(Box.createHorizontalGlue());
        scPanel2.setVisible(false);


        JLabel scResizeText = new JLabel("Resize:");
//        scResizeText.setEnabled(false);
        scPanel3.add(scResizeText);

        scSizeSlider = new JSlider(0, 100);
        scSizeSlider.setEnabled(false);
        scPanel3.add(scSizeSlider);

        scPanel3.add(Box.createHorizontalGlue());
        scPanel3.setVisible(false);


        JLabel spacecraftColorLabel = new JLabel("Color:");
		scPanel4.add(spacecraftColorLabel);

        spacecraftColorButton = GuiUtil.formButton(this, "");
        scPanel4.add(spacecraftColorButton);

        scPanel4.add(Box.createHorizontalGlue());
        scPanel4.setVisible(false);
	}

	private void configureShowLightingControls()
	{
		 //Show lighting panel

        JPanel panel_10 = new JPanel();
        widgetPanel.add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

        showLighting = new JCheckBox("Show Lighting");
        showLighting.setEnabled(false);
        panel_10.add(showLighting);

        Component horizontalGlue_3 = Box.createHorizontalGlue();
        panel_10.add(horizontalGlue_3);
	}

	private void configureShowEarthControls()
	{
		 //Show Earth Pointer Panel
        JPanel panel_11 = new JPanel();
        widgetPanel.add(panel_11);
        panel_11.setLayout(new BoxLayout(panel_11, BoxLayout.Y_AXIS));

        JPanel earthPanel1 = new JPanel();
        panel_11.add(earthPanel1);
        earthPanel1.setLayout(new BoxLayout(earthPanel1, BoxLayout.X_AXIS));

        JPanel earthPanel2 = new JPanel();
        panel_11.add(earthPanel2);
        earthPanel2.setLayout(new BoxLayout(earthPanel2, BoxLayout.X_AXIS));

        showEarthPointer = new JCheckBox("Show Earth Pointer");
        showEarthPointer.setEnabled(false);
        earthPanel1.add(showEarthPointer);

        Component horizontalStrut_5 = Box.createHorizontalStrut(25);
        earthPanel1.add(horizontalStrut_5);

        resizeEarthPointerButton = new JButton(questionIcon);
        resizeEarthPointerButton.addActionListener(e -> {
        	updateGui();
        	earthPanel2.setVisible(!earthResizeShown);
        	earthResizeShown = !earthResizeShown;
        	resizeEarthPointerButton.setText("");
        	if (earthResizeShown)
        	{
        		resizeEarthPointerButton.setText("Done");
        		resizeEarthPointerButton.setIcon(null);
        	}
        	else
        		resizeEarthPointerButton.setIcon(questionIcon);
        });
        earthPanel1.add(resizeEarthPointerButton);
        earthPanel1.add(Box.createHorizontalGlue());

        earthText = new JLabel("Resize:");
        earthText.setEnabled(false);
        earthPanel2.add(earthText);

        earthSlider = new JSlider();
        earthSlider.setEnabled(false);
        earthPanel2.add(earthSlider);
        earthPanel2.setVisible(false);

        earthPointerColorLabel = new JLabel("Color:");
		earthPanel2.add(earthPointerColorLabel);

        earthPointerColorButton = GuiUtil.formButton(this, "");
        earthPanel2.add(earthPointerColorButton);
	}

	private void configureShowSunControls()
	{
		//Show Sun Pointer Panel
        JPanel panel_12 = new JPanel();
        widgetPanel.add(panel_12);
        panel_12.setLayout(new BoxLayout(panel_12, BoxLayout.Y_AXIS));

        JPanel sunPanel1 = new JPanel();
        panel_12.add(sunPanel1);
        sunPanel1.setLayout(new BoxLayout(sunPanel1, BoxLayout.X_AXIS));

        JPanel sunPanel2 = new JPanel();
        panel_12.add(sunPanel2);
        sunPanel2.setLayout(new BoxLayout(sunPanel2, BoxLayout.X_AXIS));


        showSunPointer = new JCheckBox("Show Sun Pointer");
        showSunPointer.setEnabled(false);
        sunPanel1.add(showSunPointer);

        Component horizontalStrut_2 = Box.createHorizontalStrut(30);
        sunPanel1.add(horizontalStrut_2);

        resizeSunPointerButton = new JButton(questionIcon);
        resizeSunPointerButton.addActionListener(e -> {
        	updateGui();
        	sunPanel2.setVisible(!sunResizeShown);
        	sunResizeShown = !sunResizeShown;
        	resizeSunPointerButton.setText("");
        	if (sunResizeShown)
        	{
        		resizeSunPointerButton.setText("Done");
        		resizeSunPointerButton.setIcon(null);
        	}
        	else
        		resizeSunPointerButton.setIcon(questionIcon);
        });
        sunPanel1.add(resizeSunPointerButton);
        sunPanel1.add(Box.createHorizontalGlue());

        sunText = new JLabel("Resize:");
        sunText.setEnabled(false);
        sunPanel2.add(sunText);

        sunSlider = new JSlider();
        sunSlider.setEnabled(false);
        sunPanel2.add(sunSlider);
        sunPanel2.setVisible(false);

        sunPointerColorLabel = new JLabel("Color:");
        sunPanel2.add(sunPointerColorLabel);

        sunPointerColorButton = GuiUtil.formButton(this, "");
        sunPanel2.add(sunPointerColorButton);
//        Icon sunIcon = new ColorIcon(sunPointerColor, Color.BLACK, iconW, iconH);
//        sunPointerColorButton.setIcon(sunIcon);
	}

	private void configureShowSCPointerControls()
	{
		//Show S/C pointer panel
        JPanel panel_13 = new JPanel();
        widgetPanel.add(panel_13);
        panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.Y_AXIS));

        JPanel scPanel1 = new JPanel();
        panel_13.add(scPanel1);
        scPanel1.setLayout(new BoxLayout(scPanel1, BoxLayout.X_AXIS));

        JPanel scPanel2 = new JPanel();
        panel_13.add(scPanel2);
        scPanel2.setLayout(new BoxLayout(scPanel2, BoxLayout.X_AXIS));

        showSpacecraftMarker = new JCheckBox("Show S/C Pointer");
        showSpacecraftMarker.setEnabled(false);
        scPanel1.add(showSpacecraftMarker);

        Component horizontalStrut_3 = Box.createHorizontalStrut(30);
        scPanel1.add(horizontalStrut_3);

        resizeSpacecraftPointerButton = new JButton(questionIcon);

        resizeSpacecraftPointerButton.addActionListener(e -> {
        	updateGui();
        	scPanel2.setVisible(!scResizeShown);
        	scResizeShown = !scResizeShown;
        	resizeSpacecraftPointerButton.setText("");
        	if (scResizeShown)
        	{
        		resizeSpacecraftPointerButton.setText("Done");
        		resizeSpacecraftPointerButton.setIcon(null);
        	}
        	else
        		resizeSpacecraftPointerButton.setIcon(questionIcon);

        });
        scPanel1.add(resizeSpacecraftPointerButton);
        scPanel1.add(Box.createHorizontalGlue());

        spacecraftText = new JLabel("Resize:");
        spacecraftText.setEnabled(false);
        scPanel2.add(spacecraftText);

        spacecraftSlider = new JSlider();
        spacecraftSlider.setEnabled(false);
        scPanel2.add(spacecraftSlider);
        scPanel2.setVisible(false);
        scPanel2.add(Box.createHorizontalGlue());

        scPointerColorLabel = new JLabel("Color:");
        scPanel2.add(scPointerColorLabel);

        scPointerColorButton = GuiUtil.formButton(this, "");
		scPointerColorButton.repaint();
        scPanel2.add(scPointerColorButton);
        scPanel2.add(Box.createHorizontalGlue());
	}

	private void configureDisplayItemsPanel()
	{

		try
		{
			questionImage = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/questionMark.png"));
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        questionIcon = new ImageIcon(questionImage);

        widgetPanel = new JPanel();
        widgetPanel.setBorder(new TitledBorder(null, "Display Items",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        widgetPanel.setLayout(new BoxLayout(widgetPanel, BoxLayout.Y_AXIS));

        configureShowSpacecraftControls();

        configureShowEarthControls();

        configureShowSunControls();

        configureShowLightingControls();

        configureShowSCPointerControls();

	}

	private void configureViewOptionsPanel()
	{
		 //*********************
        // View Options Panel
        //*********************
        viewOptionsPanel = new JPanel();
        viewOptionsPanel.setBorder(new TitledBorder(null, "View Options",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        viewOptionsPanel.setLayout(new BoxLayout(viewOptionsPanel, BoxLayout.Y_AXIS));

        //Select view panel
        JPanel panel_2 = new JPanel();
        viewOptionsPanel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        lblSelectView = new JLabel("Select View:");
        lblSelectView.setEnabled(false);
        panel_2.add(lblSelectView);

        viewOptions = new JComboBox<RendererLookDirection>();
        viewOptions.setEnabled(false);
        panel_2.add(viewOptions);
        panel_2.add(Box.createHorizontalGlue());

        Component horizontalStrut_4 = Box.createHorizontalStrut(50);
        panel_2.add(horizontalStrut_4);

        btnResetCameraTo = new JButton("Reset Camera to Nadir");
        btnResetCameraTo.setEnabled(false);
        btnResetCameraTo.setVisible(false);
        panel_2.add(btnResetCameraTo);



        //FOV panel
        JPanel panel_14 = new JPanel();
        viewOptionsPanel.add(panel_14);
        panel_14.setLayout(new BoxLayout(panel_14, BoxLayout.X_AXIS));

        lblVerticalFov = new JLabel("FOV (deg):");
        lblVerticalFov.setEnabled(false);
        panel_14.add(lblVerticalFov);

        viewInputAngle = new JTextField();
        viewInputAngle.setMaximumSize( new Dimension(150, viewInputAngle.getPreferredSize().height) );
        viewInputAngle.setText("30.0");
        viewInputAngle.setEnabled(false);
        panel_14.add(viewInputAngle);
        viewInputAngle.setColumns(10);

        setViewAngle = new JButton("Set");
        setViewAngle.setEnabled(false);
        panel_14.add(setViewAngle);
        panel_14.add(Box.createHorizontalGlue());

        JPanel panel_15 = new JPanel();
        viewOptionsPanel.add(panel_15);
        panel_15.setLayout(new BoxLayout(panel_15, BoxLayout.X_AXIS));
	}

	private void configureColoringPanel()
	{
		coloringPanel = new JPanel();
        coloringPanel.setBorder(new TitledBorder(null, "Coloring Options",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        coloringPanel.setLayout(new BoxLayout(coloringPanel, BoxLayout.Y_AXIS));

        colorFunctionLabel = new JLabel("Function:");
        colorFunctionComboBox = new JComboBox<StateHistoryColoringFunctions>(StateHistoryColoringFunctions.values());

        JPanel horizPanel1 = new JPanel();
        horizPanel1.setLayout(new BoxLayout(horizPanel1, BoxLayout.X_AXIS));

        horizPanel1.add(colorFunctionLabel);
        horizPanel1.add(colorFunctionComboBox);

        colorRampLabel = new JLabel("Color:");



        colormapComboBox = new JComboBox<>();
		ListCellRenderer<Colormap> tmpRenderer = ColormapUtil.getFancyColormapRender();
		((Component) tmpRenderer).setEnabled(true);
		colormapComboBox.setRenderer(tmpRenderer);
		for (String aStr : Colormaps.getAllBuiltInColormapNames())
		{
			Colormap cmap = Colormaps.getNewInstanceOfBuiltInColormap(aStr);
			colormapComboBox.addItem(cmap);
			if (cmap.getName().equals(Colormaps.getCurrentColormapName()))
				colormapComboBox.setSelectedItem(cmap);
		}
		colormapComboBox.addActionListener(this);
		colormapComboBox.setEnabled(true);

		colormapComboBox.setPreferredSize(new Dimension(300, 30));
		colormapComboBox.setMaximumSize(new Dimension(300, 30));

    	JPanel horizPanel2 = new JPanel();
        horizPanel2.setLayout(new BoxLayout(horizPanel2, BoxLayout.X_AXIS));

	    horizPanel2.add(colorRampLabel);
	    horizPanel2.add(colormapComboBox);

	    horizPanel1.add(Box.createHorizontalGlue());
	    horizPanel2.add(Box.createHorizontalGlue());

        coloringPanel.add(horizPanel1);
        coloringPanel.add(horizPanel2);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
//		// Process the event
//		Object source = aEvent.getSource();
//		if (source == scPointerColorButton)
//			doUpdateColor(source, scPointerColor);
//		else if (source == sunPointerColorButton)
//			doUpdateColor(source, sunPointerColor);
//		else
//			doUpdateColor(source, earthPointerColor);

//		// Notify our refListener
//		refListener.actionPerformed(new ActionEvent(this, 0, ""));
	}

//	/**
//	 * Helper method that handles the action for srcColorB
//	 */
//	private void doUpdateColor(Object source, Color colorToSet)
//	{
//		System.out.println("StateHistoryViewControlsPanel: doUpdateColor: color to set " + Integer.toHexString(colorToSet.hashCode()));
//		// Prompt the user for a color
//		Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", colorToSet);
//		System.out.println("StateHistoryViewControlsPanel: doUpdateColor: tmp color " + Integer.toHexString(tmpColor.hashCode()));
//		if (tmpColor == null)
//			return;
//		colorToSet = new Color(tmpColor.getRGB());
//		if (source == earthPointerColorButton)
//			earthPointerColor = tmpColor;
//		else if (source == sunPointerColorButton)
//			sunPointerColor = tmpColor;
//		else
//			scPointerColor = tmpColor;
//		System.out.println("StateHistoryViewControlsPanel: doUpdateColor: color to set now " + colorToSet + " " + Integer.toHexString(colorToSet.hashCode()));
//		updateGui();
//	}

	/**
	 * Helper method that will update the UI to reflect the user selected colors.
	 */
	private void updateGui()
	{

		if (scPointerColorLabel == null) return;
//		int iconW = (int) (scPointerColorLabel.getWidth() * 1.20);
		int iconH = (int) (scPointerColorLabel.getHeight() * 0.80);
		iconH = 10;

		Icon spacecraftIcon = new ColorIcon(spacecraftColor, Color.BLACK, iconW, iconH);
		spacecraftColorButton.setIcon(spacecraftIcon);

		Icon scIcon = new ColorIcon(scPointerColor, Color.BLACK, iconW, iconH);
		scPointerColorButton.setIcon(scIcon);

		Icon sunIcon = new ColorIcon(sunPointerColor, Color.BLACK, iconW, iconH);
		sunPointerColorButton.setIcon(sunIcon);

		Icon earthIcon = new ColorIcon(earthPointerColor, Color.BLACK, iconW, iconH);
		earthPointerColorButton.setIcon(earthIcon);
	}

	public void updateUI()
	{
		updateGui();
	}

    public JComboBox<RendererLookDirection> getViewOptions()
    {
        return viewOptions;
    }

    public JButton getBtnResetCameraTo()
    {
        return btnResetCameraTo;
    }

//    public JButton getSaveAnimationButton()
//    {
//        return saveAnimationButton;
//    }

    public JButton getSetViewAngle()
    {
        return setViewAngle;
    }

    public JTextField getViewInputAngle()
    {
        return viewInputAngle;
    }

    public JSlider getSpacecraftSlider()
    {
        return spacecraftSlider;
    }

    public JCheckBox getShowSpacecraftMarker()
    {
        return showSpacecraftMarker;
    }

    public JSlider getSunSlider()
    {
        return sunSlider;
    }

    public JCheckBox getShowSunPointer()
    {
        return showSunPointer;
    }

    public JSlider getEarthSlider()
    {
        return earthSlider;
    }

    public JCheckBox getShowEarthPointer()
    {
        return showEarthPointer;
    }

    public JCheckBox getShowLighting()
    {
        return showLighting;
    }

    public JComboBox<String> getDistanceOptions()
    {
        return distanceOptions;
    }

    public JCheckBox getShowSpacecraft()
    {
        return showSpacecraft;
    }

    public JLabel getEarthText()
    {
        return earthText;
    }

    public JLabel getSunText()
    {
        return sunText;
    }

    public JLabel getSpacecraftText()
    {
        return spacecraftText;
    }

    public JLabel getLblSelectView()
    {
        return lblSelectView;
    }

    public JLabel getLblVerticalFov()
    {
        return lblVerticalFov;
    }

	public JComboBox<StateHistoryColoringFunctions> getColorFunctionComboBox()
	{
		return colorFunctionComboBox;
	}

	public JComboBox getColormapComboBox()
	{
		return colormapComboBox;
	}

	public JSlider getScSizeSlider()
	{
		return scSizeSlider;
	}

	public Color getScPointerColor()
	{
		return scPointerColor;
	}

	public Color getSunPointerColor()
	{
		return sunPointerColor;
	}

	public Color getEarthPointerColor()
	{
		return earthPointerColor;
	}

	public Color getSpacecraftColor()
	{
		return spacecraftColor;
	}

	public void setScPointerColor(Color scPointerColor)
	{
		this.scPointerColor = new Color(scPointerColor.getRGB());
	}

	public void setSunPointerColor(Color sunPointerColor)
	{
		this.sunPointerColor = new Color(sunPointerColor.getRGB());
	}

	public void setEarthPointerColor(Color earthPointerColor)
	{
		this.earthPointerColor = new Color(earthPointerColor.getRGB());
	}

	public void setSpacecraftColor(Color spacecraftColor)
	{
		this.spacecraftColor = new Color(spacecraftColor.getRGB());
	}

	public JButton getSpacecraftColorButton()
	{
		return spacecraftColorButton;
	}

	public JButton getScPointerColorButton()
	{
		return scPointerColorButton;
	}

	public JButton getSunPointerColorButton()
	{
		return sunPointerColorButton;
	}

	public JButton getEarthPointerColorButton()
	{
		return earthPointerColorButton;
	}

	public int getIconW()
	{
		return iconW;
	}

	public JButton getLabelFontButton()
	{
		return labelFontButton;
	}

	public Font getLabelFont()
	{
		return labelFont;
	}

	public void setLabelFont(Font labelFont)
	{
		this.labelFont = labelFont;
	}

	public JCheckBox getLabelCheckBox()
	{
		return labelCheckBox;
	}
}
