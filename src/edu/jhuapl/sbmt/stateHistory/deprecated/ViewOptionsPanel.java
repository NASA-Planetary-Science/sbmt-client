//package edu.jhuapl.sbmt.stateHistory.deprecated;
//
//import java.awt.Dimension;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.GridLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
//
//import javax.swing.ButtonGroup;
//import javax.swing.DefaultComboBoxModel;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JSlider;
//import javax.swing.JTextField;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryCollection;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
//
//public class ViewOptionsPanel extends JPanel implements ItemListener
//{
//
//    private JCheckBox showEarthMarker;
//    private JCheckBox showSunMarker;
//    private JCheckBox showSpacecraftMarker;
//    private JCheckBox showLighting;
//    private ButtonGroup viewGroup;
//    private JComboBox distanceOptions;
//    private JSlider earthSlider;
//    private JSlider sunSlider;
//    private JSlider spacecraftSlider;
//    private JComboBox viewOptions;
//    private JLabel lblSelectView;
//    private JLabel viewAngle;
//    private JTextField viewAngleInput;
//    private JButton setViewAngle;
//    private Renderer renderer;
//    private StateHistoryCollection runs;
//    private JCheckBox showSpacecraft;
//    private JLabel earthText;
//    private JLabel sunText;
//    private JLabel spacecraftText;
//    private JButton btnResetCameraTo;
//
//    private enum viewChoices {
//        FREE("Free View"),
//        SPACECRAFT("Spacecraft View"),
//        EARTH("Earth View"),
//        SUN("Sun View");
//
//        private final String text;
//
//        private viewChoices(final String text)
//        {
//            this.text = text;
//        }
//
//        public String toString()
//        {
//            return text;
//        }
//
//        public static String[] valuesAsStrings()
//        {
//            viewChoices[] values = values();
//            String[] asStrings = new String[values.length];
//            for (int ix = 0; ix < values.length; ix++) {
//                asStrings[ix] = values[ix].toString();
//            }
//            return asStrings;
//        }
//    }
//
//    /**
//     * Create the panel.
//     * @param renderer
//     * @param runs
//     */
//    public ViewOptionsPanel(final StateHistoryCollection runs, final Renderer renderer)
//    {
//        this.runs = runs;
//        this.renderer = renderer;
//
//
//        viewGroup = new ButtonGroup();
//        String[] distanceChoices = {"Distance to Center", "Distance to Surface"};
//
//        DefaultComboBoxModel<String> comboModelDistance = new DefaultComboBoxModel<String>(distanceChoices);
//
//
////        viewChoices = {"Free View", "Earth View", "Sun View", "Spacecraft View"};
//
//        DefaultComboBoxModel<String> comboModelView = new DefaultComboBoxModel<String>(viewChoices.valuesAsStrings());
//
//        GridBagLayout gridBagLayout = new GridBagLayout();
//        gridBagLayout.columnWidths = new int[]{150, 0, 150, 150, 0};
//        gridBagLayout.rowHeights = new int[]{50, 0, 0, 36, 36, 36, 50, 0};
//        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
//        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
//        setLayout(gridBagLayout);
//
//        lblSelectView = new JLabel("Select View:");
//        lblSelectView.setEnabled(false);
//        GridBagConstraints gbc_lblSelectView = new GridBagConstraints();
//        gbc_lblSelectView.insets = new Insets(0, 0, 5, 5);
//        gbc_lblSelectView.gridx = 0;
//        gbc_lblSelectView.gridy = 0;
//        add(lblSelectView, gbc_lblSelectView);
//
//
//        viewOptions = new JComboBox(comboModelView);
//        viewOptions.setEnabled(false);
//
//        // action listener for view options
//        viewOptions.addActionListener(new ActionListener()
//        {
//
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                StateHistoryModel currentRun = runs.getCurrentRun();
//                if (currentRun != null) { // can't do any view things if we don't have a trajectory / time history
//                    String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
////                    showSpacecraft.setEnabled(true);
//                    if(selectedItem.equals(viewChoices.FREE.toString())){
//
//                    } else if(selectedItem.equals(viewChoices.EARTH.toString())){
//                        showSpacecraft.setEnabled(true);
//                        currentRun.setSpacecraftMovement(false);
//                        currentRun.setEarthView(true, showSpacecraft.isSelected());
//                        viewAngleInput.setText(Double.toString(renderer.getCameraViewAngle()));
//                    } else if(selectedItem.equals(viewChoices.SUN.toString())){
//                        showSpacecraft.setEnabled(true);
//                        currentRun.setSpacecraftMovement(false);
//                        currentRun.setEarthView(false, showSpacecraft.isSelected());
//                        currentRun.setSunView(true, showSpacecraft.isSelected());
//                        viewAngleInput.setText(Double.toString(renderer.getCameraViewAngle()));
//                    } else if(selectedItem.equals(viewChoices.SPACECRAFT.toString())){
//                        currentRun.setSpacecraftMovement(true);
//                        currentRun.setActorVisibility("Spacecraft", false);
//                        showSpacecraft.setSelected(false);
//                        showSpacecraft.setEnabled(false);
//                        distanceOptions.setEnabled(false);
//                        viewAngleInput.setText(Double.toString(currentRun.getRenderer().getCameraViewAngle()));
//                        renderer.setCameraViewAngle(currentRun.getRenderer().getCameraViewAngle());
//                    }
//                }
//            }
//        });
//
//
//        GridBagConstraints gbc_viewOptions = new GridBagConstraints();
//        gbc_viewOptions.gridwidth = 2;
//        gbc_viewOptions.fill = GridBagConstraints.BOTH;
//        gbc_viewOptions.insets = new Insets(0, 0, 5, 5);
//        gbc_viewOptions.gridx = 1;
//        gbc_viewOptions.gridy = 0;
//        add(viewOptions, gbc_viewOptions);
//
//        btnResetCameraTo = new JButton("Reset Camera to Nadir");
//        btnResetCameraTo.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                StateHistoryModel currentRun = runs.getCurrentRun();
//                if( currentRun != null){
//                    currentRun.getRenderer().setCameraFocalPoint(new double[] {0,0,0});
//                }
//            }
//        });
//
//        GridBagConstraints gbc_btnResetCameraTo = new GridBagConstraints();
//        gbc_btnResetCameraTo.insets = new Insets(0, 0, 5, 0);
//        gbc_btnResetCameraTo.gridx = 3;
//        gbc_btnResetCameraTo.gridy = 0;
//        add(btnResetCameraTo, gbc_btnResetCameraTo);
//
//
//
//
//        showSpacecraft = new javax.swing.JCheckBox("Show Spacecraft");
//        showSpacecraft.setEnabled(false);
//        GridBagConstraints gbc_showSpacecraftMarker = new GridBagConstraints();
//        gbc_showSpacecraftMarker.fill = GridBagConstraints.BOTH;
//        gbc_showSpacecraftMarker.insets = new Insets(0, 0, 5, 5);
//        gbc_showSpacecraftMarker.gridx = 0;
//        gbc_showSpacecraftMarker.gridy = 1;
//        add(showSpacecraft, gbc_showSpacecraftMarker);
//
//        distanceOptions = new JComboBox(comboModelDistance);
//        distanceOptions.setEnabled(false);
//
//        // action listener for distance Options
//        distanceOptions.addActionListener(new ActionListener()
//        {
//
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                StateHistoryModel currentRun = runs.getCurrentRun();
//                String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
//                if(selectedItem.equals("Distance to Center")){
//                    currentRun.setDistanceText("Distance to Center");
//                }else if(selectedItem.equals("Distance to Surface")){
//                    currentRun.setDistanceText("Distance to Surface");
//                }
//            }
//        });
//
//
//        GridBagConstraints gbc_distanceOptions = new GridBagConstraints();
//        gbc_distanceOptions.gridwidth = 2;
//        gbc_distanceOptions.fill = GridBagConstraints.BOTH;
//        gbc_distanceOptions.insets = new Insets(0, 0, 5, 5);
//        gbc_distanceOptions.gridx = 1;
//        gbc_distanceOptions.gridy = 1;
//        add(distanceOptions, gbc_distanceOptions);
//        showLighting = new javax.swing.JCheckBox("Show Lighting");
//        showLighting.setEnabled(false);
//        GridBagConstraints gbc_showLighting = new GridBagConstraints();
//        gbc_showLighting.fill = GridBagConstraints.BOTH;
//        gbc_showLighting.insets = new Insets(0, 0, 5, 5);
//        gbc_showLighting.gridx = 0;
//        gbc_showLighting.gridy = 2;
//        add(showLighting, gbc_showLighting);
//
//        showEarthMarker = new javax.swing.JCheckBox("Show Earth Pointer");
//
//        showEarthMarker.setEnabled(false);
//
//
//
//        GridBagConstraints gbc_showEarthMarker = new GridBagConstraints();
//        gbc_showEarthMarker.fill = GridBagConstraints.BOTH;
//        gbc_showEarthMarker.insets = new Insets(0, 0, 5, 5);
//        gbc_showEarthMarker.gridx = 0;
//        gbc_showEarthMarker.gridy = 3;
//        add(showEarthMarker, gbc_showEarthMarker);
//
//
//        //
//        // resize pointers sliders
//        //
//        earthText = new JLabel("Resize: ");
//        earthText.setEnabled(false);
//
//
//
//        GridBagConstraints gbc_earthText = new GridBagConstraints();
//        gbc_earthText.fill = GridBagConstraints.BOTH;
//        gbc_earthText.insets = new Insets(0, 0, 5, 5);
//        gbc_earthText.gridx = 1;
//        gbc_earthText.gridy = 3;
//        add(earthText, gbc_earthText);
//        earthSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
//        earthSlider.setPreferredSize(new Dimension(40, earthSlider.getHeight()));
//
//        earthSlider.setEnabled(false);
//        GridBagConstraints gbc_earthSlider = new GridBagConstraints();
//        gbc_earthSlider.gridwidth = 2;
//        gbc_earthSlider.fill = GridBagConstraints.BOTH;
//        gbc_earthSlider.insets = new Insets(0, 0, 5, 0);
//        gbc_earthSlider.gridx = 2;
//        gbc_earthSlider.gridy = 3;
//        add(earthSlider, gbc_earthSlider);
//
//        showSunMarker = new javax.swing.JCheckBox("Show Sun Pointer");
//        showSunMarker.setEnabled(false);
//        GridBagConstraints gbc_showSunMarker = new GridBagConstraints();
//        gbc_showSunMarker.fill = GridBagConstraints.BOTH;
//        gbc_showSunMarker.insets = new Insets(0, 0, 5, 5);
//        gbc_showSunMarker.gridx = 0;
//        gbc_showSunMarker.gridy = 4;
//        add(showSunMarker, gbc_showSunMarker);
//        sunText = new JLabel("Resize: ");
//        sunText.setEnabled(false);
//        GridBagConstraints gbc_sunText = new GridBagConstraints();
//        gbc_sunText.fill = GridBagConstraints.BOTH;
//        gbc_sunText.insets = new Insets(0, 0, 5, 5);
//        gbc_sunText.gridx = 1;
//        gbc_sunText.gridy = 4;
//        add(sunText, gbc_sunText);
//
//        sunSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
//        sunSlider.setPreferredSize(new Dimension(40, earthSlider.getHeight()));
//        sunSlider.setEnabled(false);
//        GridBagConstraints gbc_sunSlider = new GridBagConstraints();
//        gbc_sunSlider.gridwidth = 2;
//        gbc_sunSlider.fill = GridBagConstraints.BOTH;
//        gbc_sunSlider.insets = new Insets(0, 0, 5, 0);
//        gbc_sunSlider.gridx = 2;
//        gbc_sunSlider.gridy = 4;
//        add(sunSlider, gbc_sunSlider);
//        showSpacecraftMarker = new javax.swing.JCheckBox("Show Spacecraft Pointer");
//        showSpacecraftMarker.setEnabled(false);
//        GridBagConstraints gbc_showSpacecraftMarkerHead = new GridBagConstraints();
//        gbc_showSpacecraftMarkerHead.fill = GridBagConstraints.BOTH;
//        gbc_showSpacecraftMarkerHead.insets = new Insets(0, 0, 5, 5);
//        gbc_showSpacecraftMarkerHead.gridx = 0;
//        gbc_showSpacecraftMarkerHead.gridy = 5;
//        add(showSpacecraftMarker, gbc_showSpacecraftMarkerHead);
//        spacecraftText = new JLabel("Resize: ");
//        spacecraftText.setEnabled(false);
//        GridBagConstraints gbc_spacecraftText = new GridBagConstraints();
//        gbc_spacecraftText.fill = GridBagConstraints.BOTH;
//        gbc_spacecraftText.insets = new Insets(0, 0, 5, 5);
//        gbc_spacecraftText.gridx = 1;
//        gbc_spacecraftText.gridy = 5;
//        add(spacecraftText, gbc_spacecraftText);
//        spacecraftSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
//        spacecraftSlider.setPreferredSize(new Dimension(40, earthSlider.getHeight()));
//
//        spacecraftSlider.setEnabled(false);
//        GridBagConstraints gbc_spacecraftSlider = new GridBagConstraints();
//        gbc_spacecraftSlider.ipadx = 2;
//        gbc_spacecraftSlider.gridwidth = 2;
//        gbc_spacecraftSlider.fill = GridBagConstraints.BOTH;
//        gbc_spacecraftSlider.insets = new Insets(0, 0, 5, 0);
//        gbc_spacecraftSlider.gridx = 2;
//        gbc_spacecraftSlider.gridy = 5;
//        add(spacecraftSlider, gbc_spacecraftSlider);
//
//
//        // add slider action listeners
//        earthSlider.addChangeListener(new ChangeListener()
//        {
//
//            @Override
//            public void stateChanged(ChangeEvent e)
//            {
//                StateHistoryModel currentRun = runs.getCurrentRun();
//                if (currentRun != null)
//                {
//                    JSlider slider = (JSlider) e.getSource();
//                    currentRun.setEarthPointerSize(slider.getValue());
//                }
//
//            }
//        });
//
//        sunSlider.addChangeListener(new ChangeListener()
//        {
//
//            @Override
//            public void stateChanged(ChangeEvent e)
//            {
//                StateHistoryModel currentRun = runs.getCurrentRun();
//                if (currentRun != null)
//                {
//                    JSlider slider = (JSlider) e.getSource();
//                    currentRun.setSunPointerSize(slider.getValue());
//                }
//
//            }
//        });
//
//        spacecraftSlider.addChangeListener(new ChangeListener()
//        {
//
//            @Override
//            public void stateChanged(ChangeEvent e)
//            {
//                StateHistoryModel currentRun = runs.getCurrentRun();
//                if (currentRun != null)
//                {
//                    JSlider slider = (JSlider)e.getSource();
//                    currentRun.setSpacecraftPointerSize(slider.getValue());
//                }
//            }
//        });
//
//        //
//        // set view angle panel
//        //
//
//        JPanel viewAnglePanel = new JPanel();
//        viewAnglePanel.setLayout(new GridLayout(0,3,5,5));
//        viewAngle = new JLabel("Vertical Field of View (°):");
//        viewAngleInput = new JTextField(Double.toString(renderer.getCameraViewAngle()));
//        setViewAngle = new JButton("Set");
//
//        GridBagConstraints gbc_viewAngle = new java.awt.GridBagConstraints();
//        gbc_viewAngle.gridx = 0;
//        gbc_viewAngle.gridy = 6;
//        gbc_viewAngle.gridwidth = 2;
//        gbc_viewAngle.insets = new Insets(5, 5, 5, 0);
//        gbc_viewAngle.anchor = java.awt.GridBagConstraints.WEST;
//
//        viewAngleInput.setSize(new Dimension(50, 50));
//        setViewAngle.addActionListener(new ActionListener()
//        {
//
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                if(e.getSource() == setViewAngle){
//                    StateHistoryModel currentRun = runs.getCurrentRun();
//                    if(!(Double.parseDouble(viewAngleInput.getText())>120.0 || Double.parseDouble(viewAngleInput.getText())<1.0)){
//                        renderer.setCameraViewAngle(Double.parseDouble(viewAngleInput.getText()));
//                    }else if(Double.parseDouble(viewAngleInput.getText())>120){
//                        viewAngleInput.setText("120.0");
//                        currentRun.setViewAngle(120.0);
//                    }else{
//                        viewAngleInput.setText("1.0");
//                        currentRun.setViewAngle(1.0);
//                    }
//                }
//
//            }
//        });
//
//        viewAnglePanel.add(viewAngle);
//        viewAnglePanel.add(viewAngleInput);
//        viewAnglePanel.add(setViewAngle);
//
//        gbc_viewAngle = new GridBagConstraints();
//        gbc_viewAngle.gridx = 0;
//        gbc_viewAngle.gridy = 6;
//        gbc_viewAngle.gridwidth = 4;
//        gbc_viewAngle.insets = new Insets(5, 5, 0, 0);
//        add(viewAnglePanel, gbc_viewAngle);
//
//
//        // add action listeners for the checkboxes
//        showEarthMarker.addItemListener(this);
//        showSunMarker.addItemListener(this);
//        showSpacecraftMarker.addItemListener(this);
//        showSpacecraft.addItemListener(this);
//        showLighting.addItemListener(this);
//
//    }
//
//    @Override
//    public void setEnabled(boolean enabled) {
//        lblSelectView.setEnabled(enabled);
//        showEarthMarker.setEnabled(enabled);
//        showEarthMarker.setSelected(false);
//        showSunMarker.setEnabled(enabled);
//        showSunMarker.setSelected(false);
//        showSpacecraftMarker.setEnabled(enabled);
//        showSpacecraftMarker.setSelected(false);
//        showSpacecraft.setEnabled(enabled);
//        showSpacecraft.setSelected(false);
//        showLighting.setEnabled(enabled);
//        showLighting.setSelected(false);
//        viewOptions.setEnabled(enabled);
//    }
//
//    @Override
//    public void itemStateChanged(ItemEvent e) throws NullPointerException
//    {
//        Object source = e.getItemSelectable();
//        StateHistoryModel currentRun = runs.getCurrentRun();
//
//        //
//        // handles changes in options to show/hide different parts of the model. Ex. pointers, lighting, trajectory
//        //
//
//        try
//        {
//            if(e.getStateChange() == ItemEvent.SELECTED){
//                if(source == showEarthMarker){
//                    currentRun.setActorVisibility("Earth", true);
//                    earthSlider.setEnabled(true);
//                    earthText.setEnabled(true);
//                } else if(source == showSunMarker){
//                    currentRun.setActorVisibility("Sun", true);
//                    sunSlider.setEnabled(true);
//                    sunText.setEnabled(true);
//                } else if(source == showSpacecraftMarker){
//                    currentRun.setActorVisibility("SpacecraftMarker", true);
//                    spacecraftSlider.setEnabled(true);
//                    spacecraftText.setEnabled(true);
//                } else if(source == showSpacecraft){
//                    distanceOptions.setEnabled(true);
//                    currentRun.setDistanceText(distanceOptions.getSelectedItem().toString());
//                    currentRun.setActorVisibility("Spacecraft", true);
//                } else if(source == showLighting){
//                    currentRun.setActorVisibility("Lighting", true);
//                    renderer.setFixedLightDirection(currentRun.getSunPosition());
//                    renderer.setLighting(LightingType.FIXEDLIGHT);
//                }
//
//            }
//            if(e.getStateChange() == ItemEvent.DESELECTED){
//                if(source == showEarthMarker){
//                    currentRun.setActorVisibility("Earth", false);
//                    earthSlider.setEnabled(false);
//                    earthText.setEnabled(false);
//                } else if(source == showSunMarker){
//                    currentRun.setActorVisibility("Sun", false);
//                    sunSlider.setEnabled(false);
//                    sunText.setEnabled(false);
//                } else if(source == showSpacecraftMarker){
//                    currentRun.setActorVisibility("SpacecraftMarker", false);
//                    spacecraftSlider.setEnabled(false);
//                    spacecraftText.setEnabled(false);
//                } else if(source == showSpacecraft){
//                    distanceOptions.setEnabled(false);
//                    currentRun.setActorVisibility("Spacecraft", false);
//                } else if(source == showLighting){
//                    currentRun.setActorVisibility("Lighting", false);
//                    renderer.setLighting(LightingType.LIGHT_KIT);
//                }
//            }
//            currentRun.updateActorVisibility();
//        }catch(Exception ex){
//
//        }
//
//    }
//
//}
