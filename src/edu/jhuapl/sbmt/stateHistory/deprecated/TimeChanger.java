///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
///*
// * RadialOffsetChanger.java
// *
// * Created on Apr 5, 2011, 4:36:57 PM
// */
//
//package edu.jhuapl.sbmt.stateHistory.deprecated;
//
//import java.awt.BorderLayout;
//import java.awt.FlowLayout;
//import java.awt.Image;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
//
//import javax.imageio.ImageIO;
//import javax.swing.Icon;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JTextField;
//import javax.swing.Timer;
//
//import edu.jhuapl.sbmt.stateHistory.model.HasTime;
//
//
//public class TimeChanger extends javax.swing.JPanel
//
//    implements ItemListener, ActionListener
//{
//    private HasTime model;
//    private double offsetScale = 1.0; // 0.025;
//    private int defaultValue = 0; // 15;
//    private int finalValue = 900;
//    private int sliderMin = 0;
//    private int sliderMax = 900;
//    private int sliderMinorTick = 30;
//    private int sliderMajorTick = 150;
//    private boolean playChecked = false;
//    private boolean manualSetTime = false;
//
//    /** Creates new form RadialOffsetChanger */
//    public TimeChanger()
//    {
//        initComponents();
//        createTimer();
//        rewind();
//    }
//
//    public void setModel(HasTime model)
//    {
//        this.model = model;
//    }
//
//    public void rewind()
//    {
//        slider.setValue(defaultValue);
//        currentOffsetTime = 0.0;
//        if (model != null)
//            model.setTimeFraction(currentOffsetTime);
////        System.out.println("Current Offset Time: " + currentOffsetTime);
//    }
//
//    public void fastforward()
//    {
//        slider.setValue(finalValue);
//        currentOffsetTime = 1.0;
//        if (model != null)
//            model.setTimeFraction(currentOffsetTime);
//    }
//
//    /** This method is called from within the constructor to
//     * initialize the form.
//     */
//    private void initComponents() {
//
//        slider = new javax.swing.JSlider();
//        jLabel1 = new javax.swing.JLabel();
//        jSeparator1 = new javax.swing.JSeparator();
//        rewindButton = new javax.swing.JButton();
//
//        setPreferredSize(new java.awt.Dimension(300, 75));
//
//        playCheckBox = new JButton();
//        //playCheckBox.setText("Play");
//        //playCheckBox.setSelected(false);
//        //playCheckBox.addItemListener(this);
//        try
//        {
//            Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
//            play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
//            Icon playIcon = new ImageIcon(play);
//            playCheckBox.setIcon(playIcon);
//        }catch (Exception e)
//        {
//            playCheckBox.setText("Play");
//        }
//        playCheckBox.setActionCommand("PlayButton");
//        playCheckBox.addActionListener(new ActionListener(){
//            public void actionPerformed(ActionEvent e)
//            {
//                playButtonActionPerformed(e);
//            }
//        });
//
//        fastforwardButton = new JButton();
//        try
//        {
//            Image fast = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/FastforwardButton.png"));
//            Icon fastforwardIcon = new ImageIcon(fast);
//            fastforwardButton.setIcon(fastforwardIcon);
//        }catch (Exception e)
//        {
//              fastforwardButton.setText("Fast Forward");
//        }
//        //fastforwardButton.setText("Fast-Forward");
//        fastforwardButton.setActionCommand("FastForwardButton");
//        fastforwardButton.addActionListener(new ActionListener(){
//            public void actionPerformed(ActionEvent e)
//            {
//                fastforwardActionPerformed(e);
//            }
//        });
//
//        rateTextField = new JTextField("60.0    ");
//
//        slider.setMinimum(sliderMin);
//        slider.setMaximum(sliderMax);
//        slider.setMinorTickSpacing(sliderMinorTick);
//        slider.setMajorTickSpacing(sliderMajorTick);
//        slider.setPaintTicks(true);
//        slider.setSnapToTicks(false);
//        slider.setValue(defaultValue);
//        slider.addChangeListener(new javax.swing.event.ChangeListener() {
//            public void stateChanged(javax.swing.event.ChangeEvent evt) {
//                sliderStateChanged(evt);
//            }
//        });
//
//        jLabel1.setText("Time");
//
//
//        try
//        {
//            Image rewind = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/RewindButton.png"));
//            Icon rewindIcon = new ImageIcon(rewind);
//            rewindButton.setIcon(rewindIcon);
//        }catch (Exception e)
//        {
//            rewindButton.setText("Rewind");
//        }
//        rewindButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                resetButtonActionPerformed(evt);
//            }
//        });
//
//        setLayout(new BorderLayout());
//
////        JPanel separator = new JPanel();
////        separator.setLayout(new FlowLayout(FlowLayout.TRAILING));
////        separator.add(jLabel1);
////        separator.add(jSeparator1);
////        add(separator, BorderLayout.NORTH);
//
//        try
//        {
//            Image questionMark = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/questionMark.png"));
//            Icon question = new ImageIcon(questionMark);
//            JLabel questionRate = new JLabel(question);
//            questionRate.setToolTipText("<html>The speed of the animation is X times <br>faster than 1 second of real time. Ex. <br>60 means 1 minute of the interval is <br>traveled per second</html>");
//
//            JPanel controls = new JPanel();
//            controls.setLayout(new FlowLayout());
//            controls.add(new JLabel("Play Speed:"));
//            controls.add(questionRate);
//            controls.add(rateTextField);
//            controls.add(rewindButton);
//            controls.add(playCheckBox);
//            controls.add(fastforwardButton);
//            add(controls, BorderLayout.NORTH);
//        }catch (Exception e)
//        {
//            //System.out.println(e);
//        }
//
//        add(slider, BorderLayout.SOUTH);
//
////        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
////        this.setLayout(layout);
////        layout.setHorizontalGroup(
////            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
////            .addGroup(layout.createSequentialGroup()
////                .addContainerGap()
////                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
////                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
////                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
////                            .addComponent(rateTextField)
////                            .addComponent(playCheckBox)
////                            .addComponent(rewindButton))
////                    .addGroup(layout.createSequentialGroup()
////                            .addComponent(slider, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
////                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
////                    .addGroup(layout.createSequentialGroup()
////                        .addComponent(jLabel1)
////                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
////                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)))
////                .addContainerGap())
////        );
////        layout.setVerticalGroup(
////            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
////            .addGroup(layout.createSequentialGroup()
////                .addGap(15, 15, 15)
////                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
////                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
////                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
////                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
////                        .addGroup(layout.createSequentialGroup()
////                            .addGap(6, 6, 6)
////                            .addComponent(slider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
////                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
////                        .addGroup(layout.createSequentialGroup()
////                                .addGap(3, 3, 3)
////                                .addComponent(rateTextField))
////                        .addGroup(layout.createSequentialGroup()
////                            .addGap(3, 3, 3)
////                            .addComponent(playCheckBox))
////                        .addGroup(layout.createSequentialGroup()
////                            .addGap(3, 3, 3)
////                            .addComponent(rewindButton)))
////                .addContainerGap(16, Short.MAX_VALUE))
////        );
//    }// </editor-fold>//GEN-END:initComponents
//
//    private void sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderStateChanged
//
//        //System.out.println(slider.getValueIsAdjusting());
//        if(slider.getValueIsAdjusting()){
//            int val = slider.getValue();
//            int max = slider.getMaximum();
//            int min = slider.getMinimum();
//            currentOffsetTime = (double)(val - min) / (double)(max-min) * offsetScale;
//            ((HasTime)model).setTimeFraction(currentOffsetTime);
//        }
//    }//GEN-LAST:event_sliderStateChanged
//
//    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
//        rewind();
//    }//GEN-LAST:event_resetButtonActionPerformed
//
//    private void playButtonActionPerformed(ActionEvent e)
//    {
//        if (e.getActionCommand().equals("PlayButton")){
//            if(playButtonChecked()){
//                try
//                {
//                    Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
//                    play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
//                    Icon playIcon = new ImageIcon(play);
//                    playCheckBox.setIcon(playIcon);
//                }catch (Exception ex)
//                {
////                    System.out.println(ex);
//                }
//                timer.stop();
//                playChecked = false;
//            }
//            else
//            {
//                try
//                {
//                    Image pause = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PauseButton.png"));
//                    Icon pauseIcon = new ImageIcon(pause);
//                    playCheckBox.setIcon(pauseIcon);
//                }catch (Exception ex)
//                {
////                    System.out.println(ex);
//                }
//                timer.start();
//                playChecked = true;
//            }
//        }
//    }
//
//    private void fastforwardActionPerformed(ActionEvent e)
//    {
//        fastforward();
//    }
//
//    public void itemStateChanged(ItemEvent e)
//    {
//        if (e.getItemSelectable() == this.playCheckBox)
//        {
//            if (e.getStateChange() == ItemEvent.SELECTED)
//            {
//                timer.start();
//                playChecked = true;
//            }
//            else
//            {
//                timer.stop();
//                playChecked = false;
//            }
//        }
//    }
//
//    public void setOffsetScale(double scale)
//    {
//        this.offsetScale = scale;
//    }
//
//    private Timer timer;
//    public double currentOffsetTime = 0.0;
//    public void createTimer()
//    {
//        timer = new Timer(timerInterval, this);
//        timer.setDelay(timerInterval);
////        timer.start();
//    }
//
////    public static final double flybyInterval = 1800.0;
//    public static final int timerInterval = 100;
//
//    public void actionPerformed(ActionEvent e)
//    {
//       double period = model.getPeriod();
//       double deltaRealTime = timer.getDelay() / 1000.0;
//       double playRate = 1.0;
//       try {
//          playRate = Double.parseDouble(rateTextField.getText());
//       } catch (Exception ex) { playRate = 1.0; }
//
//       double deltaSimulationTime = deltaRealTime * playRate;
//       double deltaOffsetTime = deltaSimulationTime / period;
//       //System.out.println("Delta time: " + deltaSimulationTime + " Delta offset time: " + deltaOffsetTime);
//
//       currentOffsetTime += deltaOffsetTime;
//       // time looping
//       if (currentOffsetTime > 1.0)
//           currentOffsetTime = 0.0;
//
//       model.setTimeFraction(currentOffsetTime);
//
//       int max = slider.getMaximum();
//       int min = slider.getMinimum();
//
//       int val = (int)Math.round((currentOffsetTime / offsetScale) * ((double)(max - min)) + min);
//       slider.setValue(val);
//
//       //System.out.println("Current Offset Time: " + currentOffsetTime);
//    }
//
//    public boolean playButtonChecked()
//    {
//        return playChecked;
//    }
//
//    public void setSliderValue(double tf){
//        manualSetTime = true;
//        int max = slider.getMaximum();
//
//        int val = (int)Math.round(max * tf);
//
//        slider.setValue(val);
//    }
//
//    private javax.swing.JLabel jLabel1;
//    private javax.swing.JSeparator jSeparator1;
//    private JButton playCheckBox;
//    private JTextField rateTextField;
//    private javax.swing.JButton rewindButton;
//    private javax.swing.JSlider slider;
//    private JButton fastforwardButton;
//}
