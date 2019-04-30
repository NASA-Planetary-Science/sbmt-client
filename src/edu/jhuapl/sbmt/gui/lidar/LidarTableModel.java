package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.Track;

/**
 * Class that defines the model used in the table associated with the
 * {@link LidarListPanel}
 */
public class LidarTableModel implements TableModel, PropertyChangeListener
{
	// State vars
	private LidarSearchDataCollection lidarModel;
	private List<TableModelListener> listenerL;

	// Cache vars
	private Set<Track> cTrackS;

	/**
	 * Constructor
	 */
	public LidarTableModel(LidarSearchDataCollection aLidarModel)
	{
		lidarModel = aLidarModel;
		listenerL = new ArrayList<>();

		cTrackS = ImmutableSet.of();

		// Register for events of interest
		lidarModel.addPropertyChangeListener(this);
	}

	@Override
	public int getColumnCount()
	{
		return 7;
	}

	@Override
	public Class<?> getColumnClass(int aColIndex)
	{
		switch (aColIndex)
		{
			case 0:
				return Boolean.class;
			case 1:
				return Color.class;
			default:
				return String.class;
		}
	}

	@Override
	public String getColumnName(int aColIndex)
	{
		switch (aColIndex)
		{
			case 0:
				return "Show";
			case 1:
				return "Color";
			case 2:
				return "Track";
			case 3:
				return "# pts";
			case 4:
				return "Start Time";
			case 5:
				return "End Time";
			case 6:
				return "Data Source";
			default:
				return null;
		}
	}

	@Override
	public int getRowCount()
	{
		return lidarModel.getNumberOfTracks();
	}

	@Override
	public Object getValueAt(int aRowIndex, int aColIndex)
	{
		Track tmpTrack = lidarModel.getTrack(aRowIndex);

		switch (aColIndex)
		{
			case 0:
				return tmpTrack.getIsVisible();
			case 1:
				return tmpTrack.color;
			case 2:
				return "Trk " + aRowIndex;
			case 3:
				return tmpTrack.getNumberOfPoints();
			case 4:
				return tmpTrack.timeRange[0];
			case 5:
				return tmpTrack.timeRange[1];
			case 6:
				return getSourceFileString(tmpTrack);
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Index: " + aColIndex);
	}

	@Override
	public boolean isCellEditable(int aRowIndex, int aColumnIndex)
	{
		boolean retBool = false;
		retBool |= aColumnIndex == 0;
		retBool |= aColumnIndex == 1;

		return retBool;
	}

	@Override
	public void setValueAt(Object aValue, int aRowIndex, int aColIndex)
	{
		Track tmpTrack = lidarModel.getTrack(aRowIndex);
		if (aColIndex == 0)
			lidarModel.setTrackVisible(tmpTrack, (boolean) aValue);
		else if (aColIndex == 1)
			lidarModel.setTrackColor(tmpTrack, (Color) aValue);
		else
			throw new UnsupportedOperationException("Column is not supported. Index: " + aColIndex);
	}

	@Override
	public void addTableModelListener(TableModelListener aListener)
	{
		listenerL.add(aListener);
	}

	@Override
	public void removeTableModelListener(TableModelListener aListener)
	{
		listenerL.remove(aListener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent aEvent)
	{
		// Watch for model change events
		if (Properties.MODEL_CHANGED.equals(aEvent.getPropertyName()) == false)
			return;

		// Update our cache
		Set<Track> trackS = ImmutableSet.copyOf(lidarModel.getTracks());
		if (trackS.equals(cTrackS) == true)
			return;
		cTrackS = trackS;

		notifyListeners();
	}

	/**
	 * Utility method that returns the appropriate "Source Files" string for the
	 * specified track.
	 */
	public static String getSourceFileString(Track aTrack)
	{
		int srcFileCnt = aTrack.getNumberOfSourceFiles();
		if (srcFileCnt == 0)
			return "";

		StringBuffer tmpSB = new StringBuffer();
		for (int i = 0; i < srcFileCnt; i++)
		{
			tmpSB.append(" | " + aTrack.getSourceFileName(i));
			if (tmpSB.length() > 1000)
			{
				tmpSB.append("...");
				break;
			}
		}
		tmpSB.delete(0, 3);

		return tmpSB.toString();
	}

	/**
	 * Helper method to send out notification to the registered listeners.
	 */
	private void notifyListeners()
	{
		TableModelEvent tmpEvent = new TableModelEvent(this);
		for (TableModelListener aListener : listenerL)
			aListener.tableChanged(tmpEvent);
	}

}