package edu.jhuapl.near.gui.eros;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.jhuapl.near.gui.RadialOffsetChanger;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.LineamentModel;

public class LineamentControlPanel extends JPanel implements ItemListener
{
    private JCheckBox lineamentCheckBox;
    private LineamentModel lineamentModel;

    public LineamentControlPanel(ModelManager modelManager)
    {
        lineamentModel = (LineamentModel)modelManager.getModel(ModelNames.LINEAMENT);
        RadialOffsetChanger radialChanger = new RadialOffsetChanger();
        radialChanger.setModel(lineamentModel);

        lineamentCheckBox = new JCheckBox();
        lineamentCheckBox.setText("Show Lineaments");
        lineamentCheckBox.setSelected(false);
        lineamentCheckBox.addItemListener(this);

        setLayout(new BoxLayout(this,
                BoxLayout.PAGE_AXIS));

        add(Box.createVerticalStrut(10));
        add(lineamentCheckBox);
        add(Box.createVerticalStrut(15));
        add(radialChanger);
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getItemSelectable() == this.lineamentCheckBox)
        {
            if (e.getStateChange() == ItemEvent.DESELECTED)
                lineamentModel.setShowLineaments(false);
            else
                lineamentModel.setShowLineaments(true);
        }
    }
}
