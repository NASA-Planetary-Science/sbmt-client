package edu.jhuapl.sbmt.stateHistory.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

public class TimeIntervalTable extends JTable
{

    public static enum columns {
            MAP("Map"),
            SHOW("Show"),
            COLOR("Color"),
            LINE("Line"),
            NAME("Name"),
            DESC("Description"),
            START("Start Time"),
            END("End Time");

        private String columnName;

        columns(String cn) {
            this.columnName = cn;
        }

        public String columnName(){
            return columnName;
        }

        public static String[] valuesAsStrings()
        {
            columns[] values = values();
            String[] asStrings = new String[values.length];
            for (int ix = 0; ix < values.length; ix++) {
                asStrings[ix] = values[ix].columnName();
            }
            return asStrings;
        }
    }



    Set<StateHistoryKey> allKeys = Sets.newHashSet();
    StateHistoryCollection intervals;
    SmallBodyModel bodyModel;
    Renderer renderer;


    /**
     * Create the Table
     */
    public TimeIntervalTable(StateHistoryCollection intervals, SmallBodyModel model, Renderer renderer)
    {
        this.intervals = intervals;
        this.renderer = renderer;
        this.bodyModel = model;

        setModel(new TimeIntervalTableModel(columns.valuesAsStrings(), intervals));
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setDefaultRenderer(String.class, new StringRenderer());
        setDefaultRenderer(Color.class, new ColorRenderer());
        getColumnModel().getColumn(columns.MAP.ordinal()).setPreferredWidth(31);
        getColumnModel().getColumn(columns.SHOW.ordinal()).setPreferredWidth(35);
        getColumnModel().getColumn(columns.COLOR.ordinal()).setPreferredWidth(70);
        getColumnModel().getColumn(columns.LINE.ordinal()).setPreferredWidth(40);
        getColumnModel().getColumn(columns.NAME.ordinal()).setPreferredWidth(80);
        getColumnModel().getColumn(columns.DESC.ordinal()).setMinWidth(80);
        getColumnModel().getColumn(columns.START.ordinal()).setResizable(true);
        getColumnModel().getColumn(columns.END.ordinal()).setResizable(true);

    }



    public class TimeIntervalTableModel extends DefaultTableModel
    {

        public TimeIntervalTableModel(String[] columnNames, StateHistoryCollection intervals)
        {
            super(new Object[0][columnNames.length], columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            if (column == columns.MAP.ordinal() || column == columns.SHOW.ordinal() ||
                    column == columns.DESC.ordinal() || column == columns.NAME.ordinal() ||
                    column == columns.LINE.ordinal() || column == columns.COLOR.ordinal())
                return true;
            else
                return false;
        }

        public Class<?> getColumnClass(int column)
        {
            if (column == columns.MAP.ordinal() || column == columns.SHOW.ordinal())
                return Boolean.class;
            else if (column == columns.COLOR.ordinal())
                return Color.class;
            else if (column == columns.START.ordinal() || column == columns.START.ordinal())
                return DateTime.class;
            else
                return String.class;
        }

        // remove rows from last index to first index so that the indices don't
        // change and throw an error (if we remove 0 first, then 1 becomes 0)
        public void removeRows(int[] rowIndexes) {
            Arrays.sort(rowIndexes);
            for (int iR = rowIndexes.length - 1; iR > -1; iR--)
            {
                removeRow(rowIndexes[iR]);
                intervals.removeRun(intervals.getKeyFromRow(rowIndexes[iR]));
            }
        }

        public StateHistory getStateHistoryAtRow(int selectedRow)
        {
        	StateHistoryKey key = intervals.getKeyFromRow(selectedRow);
        	return intervals.getRun(key);
        }

//        public void saveRowToFile(int selectedRow, File file)
//        {
//            StateHistoryKey key = intervals.getKeyFromRow(selectedRow);
//            StateHistory thisRow = intervals.getRun(key);
//            StateHistoryModel shm = new StateHistoryModel(bodyModel, renderer);
//            shm.saveIntervalToFile(bodyModel.getConfig().getShapeModelName(), thisRow, file.getAbsolutePath());
//
//        }
//
//        public void loadIntervalFromFile(File runFile, SmallBodyModel bodyModel)
//        {
//            StateHistoryKey key = new StateHistoryKey(intervals);
//            StateHistoryModel shm = new StateHistoryModel(bodyModel, renderer);
//            StateHistory newRow = shm.loadStateHistoryFromFile(runFile, bodyModel.getConfig().getShapeModelName());
//            addInterval(newRow, renderer);
//        }

    }

    public class StringRenderer extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            Component co = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            return co;
        }
    }

    class ColorRenderer extends JLabel implements TableCellRenderer
    {
        private Border unselectedBorder = null;
        private Border selectedBorder = null;

        public ColorRenderer()
        {
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            Color newColor = (Color)color;
            setBackground(newColor);

            if (isSelected)
            {
                if (selectedBorder == null)
                {
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            }
            else
            {
                if (unselectedBorder == null)
                {
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getBackground());
                }
                setBorder(unselectedBorder);
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", "
                    + newColor.getGreen() + ", "
                    + newColor.getBlue());

            return this;
        }
    }

    // Add an interval to the table.  Use ImageTable as an example
    public void addInterval(StateHistory interval, Renderer renderer)
    {
        StateHistoryKey key = interval.getKey();
        if (allKeys.contains(key))
            return;
        allKeys.add(key);

        // add to model
        intervals.addRun(interval);


        // add row to table
        int i=getModel().getRowCount();
        org.joda.time.format.DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        double[] trajColor = interval.getTrajectoryColor();
        DateTime startTime = new DateTime(interval.getMinTime());
        DateTime endTime = new DateTime(interval.getMaxTime());
        Color intervalColor = new Color((int)trajColor[0], (int)trajColor[1], (int)trajColor[2], (int)trajColor[3]);
        ((DefaultTableModel)getModel()).addRow(new Object[]{
                false,
                false,
                intervalColor,
                interval.getTrajectoryThickness(), // TODO line thickness
                interval.getTrajectoryName(),
                interval.getTrajectoryDescription(),
                startTime.toString(dtf),
                endTime.toString(dtf)
                });

            setValueAt(true, i, columns.MAP.ordinal());
            setValueAt(true, i, columns.SHOW.ordinal());
            setRowSelectionInterval(i, i);
    }



}
