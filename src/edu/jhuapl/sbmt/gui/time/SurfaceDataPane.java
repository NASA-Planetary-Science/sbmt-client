package edu.jhuapl.sbmt.gui.time;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.time.AreaCalculation;
import edu.jhuapl.sbmt.model.time.ScalarRange;
import edu.jhuapl.sbmt.model.time.StateHistoryCollection;
import edu.jhuapl.sbmt.model.time.SurfacePatch;

public class SurfaceDataPane extends JPanel implements ItemListener
{
    private StateHistoryCollection simulationRunCollection;

    private JLabel paneLabel;
    private JSeparator paneSeparator;

    private JLabel surfaceDataTypeLabel;
    private JComboBox surfaceDataType;

    private JLabel scalarRangeTypeLabel;
    private JComboBox scalarRangeType;

    public SurfaceDataPane(ModelManager modelManager)
    {
        simulationRunCollection = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);

        JPanel labelPane = new JPanel();
        labelPane.setLayout(new FlowLayout(FlowLayout.LEFT));

        paneLabel = new JLabel();
        paneLabel.setText("Surface Data");
        paneSeparator = new JSeparator();
        labelPane.add(paneLabel);
        labelPane.add(paneSeparator);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createVerticalStrut(10));
        add(labelPane);

        surfaceDataTypeLabel = new JLabel("Field");
        String emptyDataType[] = { "<type>" };
        surfaceDataType = new JComboBox(emptyDataType);
        surfaceDataType.addItemListener(this);

        scalarRangeTypeLabel = new JLabel("Range");
        String emptyRangeType[] = { "<range>" };
        scalarRangeType = new JComboBox(emptyRangeType);
        scalarRangeType.addItemListener(this);

        add(Box.createVerticalStrut(10));
        JPanel dataPane = new JPanel();
        dataPane.setLayout(new BoxLayout(dataPane, BoxLayout.LINE_AXIS));

        dataPane.add(Box.createHorizontalStrut(10));
        dataPane.add(surfaceDataTypeLabel);
        dataPane.add(Box.createHorizontalStrut(15));
        dataPane.add(surfaceDataType);

        dataPane.add(Box.createHorizontalStrut(10));
        dataPane.add(scalarRangeTypeLabel);
        dataPane.add(Box.createHorizontalStrut(15));
        dataPane.add(scalarRangeType);

        add(dataPane);
    }

    public void setModel(SurfacePatch surfacePatch)
    {
        surfaceDataType.setModel(surfacePatch);
    }

    public void setModel(ScalarRange scalarRange)
    {
        scalarRangeType.setModel(scalarRange);
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            if (e.getSource() == surfaceDataType)
            {
                System.out.println("Data type has changed");
                AreaCalculation currentAreaCalculation = simulationRunCollection.getCurrentRun().getAreaCalculationCollection().getCurrentValue();
                currentAreaCalculation.setCurrentDataField((String)e.getItem());
                currentAreaCalculation.setCurrentPatch(currentAreaCalculation.getCurrentPatch());
                currentAreaCalculation.redraw();
                simulationRunCollection.getCurrentRun().updateActorVisibility();
                simulationRunCollection.getCurrentRun().updateScalarBar();
            }
            else if (e.getSource() == scalarRangeType)
            {
                System.out.println("Range type has changed");
                AreaCalculation currentAreaCalculation = simulationRunCollection.getCurrentRun().getAreaCalculationCollection().getCurrentValue();
                currentAreaCalculation.setCurrentPatch(currentAreaCalculation.getCurrentPatch());
                currentAreaCalculation.redraw();
                simulationRunCollection.getCurrentRun().updateActorVisibility();
                simulationRunCollection.getCurrentRun().updateScalarBar();
            }
        }
    }
}
