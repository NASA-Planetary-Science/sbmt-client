package edu.jhuapl.sbmt.gui.lidar.v2;

import java.awt.Color;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.Track;

public class LidarTableModel extends DefaultTableModel
{
    boolean hideOrShowAllInProgress=false;
    private LidarSearchDataCollection lidarModel;

    public LidarTableModel(LidarSearchDataCollection model)
    {
        this.lidarModel = model;
        this.addTableModelListener(new TableModelListener()
        {

            @Override
            public void tableChanged(TableModelEvent e)
            {
                if (hideOrShowAllInProgress)
                    return;
                //
                int r=e.getFirstRow();
                int c=e.getColumn();
                TableModel model=(TableModel)e.getSource();
                if (c==0)   // hardcoded wiring into hide column
                {
                    Boolean data=(Boolean)model.getValueAt(r,c);
                    lidarModel.hideTrack(r, data);
                }
            }
        });

    }

    public Color getColor(int row)
    {
        int[] rgb=(int[])getValueAt(row, 6);
        return new Color(rgb[0],rgb[1],rgb[2]);
    }

    public void hideAllTracks()
    {
        hideOrShowAllInProgress=true;
        for (int r=0; r<getRowCount(); r++)
            setValueAt(true, r, 0);    // set checkbox value to true (0 is hardcoded as the hide column)
        lidarModel.hideAllTracks();
        hideOrShowAllInProgress=false;
    }

    public void showAllTracks()
    {
        hideOrShowAllInProgress=true;
        for (int r=0; r<getRowCount(); r++)
            setValueAt(false, r, 0);    // set checkbox value to false (0 is hardcoded as the hide column)
        lidarModel.showAllTracks();
        hideOrShowAllInProgress=false;
    }

    public void removeAllTracks()
    {
        hideOrShowAllInProgress=true;
        int cnt=getRowCount();
        for (int r=0; r<cnt; r++)
            removeRow(0);
        hideOrShowAllInProgress=false;
    }

    @Override
    public int getColumnCount()
    {
        return 7;
    }

    public void addTrack(Track track, int id)
    {
        addTrack(track, id, false);
    }

    public void addTrack(Track track, int id, boolean hidden)
    {
        String sourceFiles="";
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<track.getNumberOfSourceFiles(); i++)
        {
            builder.append(track.getSourceFileName(i));
//            sourceFiles+=track.getSourceFileName(i);
            if (i<track.getNumberOfSourceFiles()-1)
            {
                builder.append(" | ");
//                sourceFiles+=" | ";
            }
        }
        addRow(new Object[]{
                hidden,
                "Trk "+id,
                track.getNumberOfPoints(),
                track.timeRange[0],
                track.timeRange[1],
                sourceFiles,
                track.color
                });
    }

    @Override
    public String getColumnName(int column)
    {
        switch (column)
        {
        case 0:
            return "Hide";
        case 1:
            return "Track";
        case 2:
            return "# pts";
        case 3:
            return "Start Time";
        case 4:
            return "End Time";
        case 5:
            return "Data Source";
        default:
            return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
        case 0:
            return Boolean.class;
        case 6:
            return int[].class;
        default:
            return String.class;
        }
    }

}