package edu.jhuapl.near.gui.eros;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.jhuapl.near.model.eros.NLRDataEverything;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;


public class NLRSummaryPanel extends JPanel
{
    private NLRDataEverything nlrModel;
    private JList resultList;
    private ArrayList<String> nlrRawResults = new ArrayList<String>();
    private JCheckBox showHideButton;
    private JButton saveButton;
    //private RadialOffsetChanger radialOffsetChanger;


    public NLRSummaryPanel(
            final ModelManager modelManager)
    {
        setLayout(new BoxLayout(this,
                BoxLayout.PAGE_AXIS));

        //this.modelManager = modelManager;
        this.nlrModel = (NLRDataEverything)modelManager.getModel(ModelNames.LIDAR_SUMMARY);

        showHideButton = new JCheckBox("Show all NLR data");
        showHideButton.setActionCommand("Show all NLR data");
        showHideButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                nlrModel.setVisible(showHideButton.isSelected());
            }
        });
        showHideButton.setEnabled(true);


        saveButton = new JButton("Save...");
        saveButton.setActionCommand("Save...");
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int index = resultList.getSelectedIndex();
                if (index >= 0)
                {
                    File tmp = new File(nlrRawResults.get(index));
                    File file = CustomFileChooser.showSaveDialog(
                            saveButton.getParent(),
                            "Save NLR data",
                            tmp.getName().substring(0, tmp.getName().length()-3));

                    try
                    {
                        if (file != null)
                        {
                            File nlrFile = FileCache.getFileFromServer(nlrRawResults.get(index));

                            FileUtil.copyFile(nlrFile, file);
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
        saveButton.setEnabled(true);


        add(Box.createVerticalStrut(15));
        add(showHideButton);
        add(Box.createVerticalStrut(15));
//        add(saveButton);
//        add(Box.createVerticalStrut(15));

//        radialOffsetChanger = new RadialOffsetChanger(nlrModel, "Radial Offset");

//       add(radialOffsetChanger);
    }

}
