package edu.jhuapl.sbmt.gui.lidar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jidesoft.swing.RangeSlider;

import edu.jhuapl.saavtk.gui.RadialOffsetChanger;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.DownloadableFileInfo.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.model.lidar.LidarBrowseDataCollection;
import edu.jhuapl.sbmt.model.lidar.LidarBrowseDataCollection.LidarDataFileSpec;


public class LidarBrowsePanel extends JPanel implements ListSelectionListener
{
    private final String LIDAR_REMOVE_ALL_BUTTON_TEXT = "Remove All Lidar Data";

    protected LidarBrowseDataCollection lidarModel;
    protected JList resultList;
    protected DefaultListModel lidarResultListModel;
    //private List<LidarDataSpec> lidarRawResults = new ArrayList<LidarDataSpec>();
    protected JLabel resultsLabel;
    private JButton showHideButton;
    private JButton removeAllButton;
    private JButton saveButton;
    private LidarTimeIntervalChanger timeIntervalChanger;
    private RadialOffsetChanger radialOffsetChanger;
    protected JScrollPane listScrollPane;

    public class LidarTimeIntervalChanger extends JPanel implements ChangeListener
    {
        private RangeSlider slider;

        public LidarTimeIntervalChanger()
        {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

            setBorder(BorderFactory.createTitledBorder("Displayed Lidar Data"));

            slider = new RangeSlider(0, 255, 0, 255);
            slider.setPaintTicks(true);
            slider.setMajorTickSpacing(10);
            slider.setPaintTrack(true);
            slider.addChangeListener(this);
            add(slider);
        }

        public void stateChanged(ChangeEvent e)
        {
            double lowVal = (double)slider.getLowValue()/(double)slider.getMaximum();
            double highVal = (double)slider.getHighValue()/(double)slider.getMaximum();
            if (lidarModel != null)
                lidarModel.setPercentageShown(lowVal, highVal);
        }
    }

    protected DownloadableFileState getDataState()
    {
        return FileCache.instance().query(lidarModel.getBrowseFileResourcePath()).getState();
    }

    public LidarBrowsePanel(
            final ModelManager modelManager)
    {
        setLayout(new BoxLayout(this,
                BoxLayout.PAGE_AXIS));

        this.lidarModel = (LidarBrowseDataCollection)modelManager.getModel(ModelNames.LIDAR_BROWSE);


        JPanel resultsPanel = new JPanel(new BorderLayout());

        resultsLabel = new JLabel("Available Files");

        lidarResultListModel = new DefaultListModel();



        final JPanel resultControlsPanel = new JPanel(new BorderLayout());

        final JPanel resultSub1ControlsPanel = new JPanel();

        resultSub1ControlsPanel.setLayout(new BoxLayout(resultSub1ControlsPanel,
                BoxLayout.PAGE_AXIS));


        showHideButton = new JButton("Show");
        showHideButton.setActionCommand("Show");
        showHideButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int index = resultList.getSelectedIndex();
                if (index >= 0)
                {
                    try
                    {
                        if (showHideButton.getText().startsWith("Show"))
                        {
                            setCursor(new Cursor(Cursor.WAIT_CURSOR));
                            lidarModel.addLidarData(((LidarDataFileSpec) lidarResultListModel.get(index)).path);
                            setCursor(Cursor.getDefaultCursor());
                            showHideButton.setText("Remove");
                        }
                        else
                        {
                            lidarModel.removeLidarData(((LidarDataFileSpec)lidarResultListModel.get(index)).path);

                            showHideButton.setText("Show");
                        }
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        });
        showHideButton.setEnabled(false);


        saveButton = new JButton("Save...");
        saveButton.setActionCommand("Save...");
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int index = resultList.getSelectedIndex();
                if (index >= 0)
                {
                    File tmp = new File(((LidarDataFileSpec)lidarResultListModel.get(index)).path);
                    File file = CustomFileChooser.showSaveDialog(
                            saveButton.getParent(),
                            "Save Lidar data",
                            tmp.getName());
                    try
                    {
                        if (file != null)
                        {
                            File lidarFile = FileCache.getFileFromServer(((LidarDataFileSpec)lidarResultListModel.get(index)).path);

                            FileUtil.copyFile(lidarFile, file);
                        }
                    }
                    catch(Exception ex)
                    {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(saveButton),
                                "Unable to save file to " + file.getAbsolutePath(),
                                "Error Saving File",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }

                }
            }
        });
        saveButton.setEnabled(false);


        JPanel resultSub2ControlsPanel = new JPanel();
        resultSub2ControlsPanel.setLayout(new BoxLayout(resultSub2ControlsPanel,
                BoxLayout.LINE_AXIS));
        removeAllButton = new JButton(LIDAR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand(LIDAR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                LidarBrowseDataCollection model = (LidarBrowseDataCollection)modelManager.getModel(ModelNames.LIDAR_BROWSE);
                model.removeAllLidarData();

                showHideButton.setText("Show");
            }
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        resultSub2ControlsPanel.add(showHideButton);
        resultSub2ControlsPanel.add(removeAllButton);
        resultSub2ControlsPanel.add(saveButton);

        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);

        resultsPanel.add(resultControlsPanel, BorderLayout.SOUTH);

        timeIntervalChanger = new LidarTimeIntervalChanger();

        radialOffsetChanger = new RadialOffsetChanger();
        radialOffsetChanger.setModel(lidarModel);
        radialOffsetChanger.setOffsetScale(lidarModel.getOffsetScale());

        JPanel showSpacecraftPanel = new JPanel(new GridLayout());
        showSpacecraftPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        final JCheckBox showSpacecraftCheckBox = new JCheckBox("Show spacecraft position");
        showSpacecraftCheckBox.setSelected(true);
        showSpacecraftCheckBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                lidarModel.setShowSpacecraftPosition(showSpacecraftCheckBox.isSelected());
            }
        });
        showSpacecraftPanel.add(showSpacecraftCheckBox);

        DownloadableFileState dataState = getDataState();
        if (dataState.isAccessible())
        {
            List<LidarDataFileSpec> lidarPaths;
            try
            {
                lidarPaths = lidarModel.getAllLidarPaths();
                for (LidarDataFileSpec spec : lidarPaths)
                {
                    lidarResultListModel.addElement(spec);
                }
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (dataState.isUrlUnauthorized())
        {
            resultsLabel.setText("No Results Available: Access Not Authorized");
        }

        //Create the list and put it in a scroll pane.
        resultList = new JList(lidarResultListModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addListSelectionListener(this);
        listScrollPane = new JScrollPane(resultList);

        resultsPanel.add(resultsLabel, BorderLayout.NORTH);
        resultsPanel.add(listScrollPane, BorderLayout.CENTER);


        add(resultsPanel);
        add(timeIntervalChanger);
        add(showSpacecraftPanel);
        add(radialOffsetChanger);
    }


    public void valueChanged(ListSelectionEvent arg0)
    {
        int[] idx = {arg0.getFirstIndex(), arg0.getLastIndex()};
        for (int index : idx)
        {
            if (index >= 0 && resultList.isSelectedIndex(index))
            {

                showHideButton.setEnabled(true);
                saveButton.setEnabled(true);

                if (lidarModel.containsLidarData(((LidarDataFileSpec)lidarResultListModel.get(index)).path))
                {
                    showHideButton.setText("Remove");
                }
                else
                {
                    showHideButton.setText("Show");
                }
                break;
            }
            else
            {
                showHideButton.setEnabled(false);
                saveButton.setEnabled(false);
            }
        }
    }


}
