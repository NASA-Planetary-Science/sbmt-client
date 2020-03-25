package edu.jhuapl.sbmt.stateHistory.ui.version2;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author steelrj1
 *
 */
public class StateHistoryPanel2  extends JPanel
{
    /**
     *
     */
    private JScrollPane tableScrollPane;
    /**
     *
     */
    private JPanel panel_9;

    /**
     *
     */
    private JPanel intervalSelectionPanel;


    /**
     * Create the panel.
     */
    /**
     *
     */
    public StateHistoryPanel2()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);



//        Component verticalGlue_1 = Box.createVerticalGlue();
//        timeControlPanel.add(verticalGlue_1);
//
//        Component verticalGlue = Box.createVerticalGlue();
//        timeControlPanel.add(verticalGlue);
//
//        Component verticalStrut = Box.createVerticalStrut(10);
//        panel_18.add(verticalStrut);

    }





//    public JPanel getViewControlPanel()
//    {
//        return viewControlPanel;
//    }



    /**
     * @return
     */
    public JPanel getPanel_9()
    {
        return panel_9;
    }



    /**
     * @return
     */
    public JPanel getIntervalSelectionPanel() {
        return intervalSelectionPanel;
    }
}
