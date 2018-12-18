package edu.jhuapl.sbmt.gui.dtm.model.creation;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.metadata.serialization.Serializers;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DtmCreationModel implements MetadataManager
{
	private ModelManager modelManager;
    private Renderer renderer;
    private DEMCollection dems;
    private DEMBoundaryCollection boundaries;
    private double latitude;
    private double longitude;
    private double pixelScale;
    private int halfSize;
    private String mapmakerPath;
    private String bigmapPath;
    private List<DEMInfo> infoList;
    private int[] selectedIndices;
    private Vector<DEMCreationModelChangedListener> listeners;

    private boolean initialized = false;

    public static class DEMInfo implements MetadataManager
    {
        public String name = ""; // name to call this image for display purposes
        public String demfilename = ""; // filename of image on disk

        @Override
        public String toString()
        {
            DecimalFormat df = new DecimalFormat("#.#####");
            return name;
        }

        Key<String> nameKey = Key.of("name");
        Key<String> demfilenameKey = Key.of("demfilename");

		@Override
		public Metadata store()
		{
			SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
			write(nameKey, name, configMetadata);
			write(demfilenameKey, demfilename, configMetadata);
			return configMetadata;
		}

		@Override
		public void retrieve(Metadata source)
		{
			name = read(nameKey, source);
			demfilename = read(demfilenameKey, source);
		}

		protected <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
	    {
	        if (value != null)
	        {
	            configMetadata.put(key, value);
	        }
	    }

	    protected <T> T read(Key<T> key, Metadata configMetadata)
	    {
	        T value = configMetadata.get(key);
	        if (value != null)
	            return value;
	        return null;
	    }
    }

	public DtmCreationModel(ModelManager modelManager)
	{
		this.modelManager = modelManager;
		 // Get collections
        dems = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        boundaries = (DEMBoundaryCollection)modelManager.getModel(ModelNames.DEM_BOUNDARY);
        infoList = new ArrayList<DEMInfo>();
        this.listeners = new Vector<DEMCreationModelChangedListener>();
	}

    private ModelNames getDEMBoundaryCollectionModelName()
    {
        return ModelNames.DEM_BOUNDARY;
    }

    public String getCustomDataFolder()
    {
        return modelManager.getPolyhedralModel().getCustomDataFolder();
    }

    public String getCustomDataRootFolder()
    {
        return modelManager.getPolyhedralModel().getCustomDataRootFolder();
    }

    private String getDEMConfigFilename()
    {
        return modelManager.getPolyhedralModel().getDEMConfigFilename();
    }

    // Initializes the DEM list from config
    public void initializeDEMList() throws IOException
    {
        if (initialized)
            return;

        if (new File(getDEMConfigFilename()).exists())
        {
	        FixedMetadata metadata = Serializers.deserialize(new File(getDEMConfigFilename()), "CustomDEMs");
	        retrieve(metadata);

	        for (DEMInfo info : infoList)
	        {
	        	fireInfoChangedListeners(info);
	        }
        }

        initialized = true;
    }

    public void updateConfigFile()
    {
    	try
		{
			Serializers.serialize("CustomDEMs", this, new File(getDEMConfigFilename()));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    // Removes all DEMs
    private void removeAllDEMsFromRenderer()
    {
        dems.removeDEMs();
    }

    // Removes a DEM
    public void removeDEM(int[] index)
    {

        // Get the DEM info
    	for (int idx : index)
    	{
    		DEMInfo demInfo = infoList.get(idx);
    		removeDEM(new DEMInfo[] { demInfo });
    	}

    }

    public void removeDEM(DEMInfo[] demInfos)
    {
    	// Remove from cache
    	for (DEMInfo demInfo : demInfos)
    	{
	        String name = getCustomDataFolder() + File.separator + demInfo.demfilename;
	        new File(name).delete();

	        // Remove the DEM from the renderer
	        DEMKey demKey = dems.getDEMKeyFromInfo(demInfo);
	        dems.removeDEM(demKey);

	        // Remove from the list
	        infoList.remove(demInfo);
	        boundaries.removeBoundary(demKey);

	        updateConfigFile();
    	}
    }


    public void saveDEM(DEMInfo demInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        // Copy FIT file to cache
        String newFilename = "dem-" + uuid + ".fit";
        String newFilepath = getCustomDataFolder() + File.separator + newFilename;
        FileUtil.copyFile(demInfo.demfilename,  newFilepath);
        // Change demInfo.demfilename to the new location of the file
        demInfo.demfilename = newFilepath;

        infoList.add(demInfo);
        fireInfoChangedListeners(demInfo);
        updateConfigFile();

    }


//    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
//        int[] selectedIndices = getSelectedIndices();
//        Arrays.sort(selectedIndices);
//        for (int i=selectedIndices.length-1; i>=0; --i)
//        {
//            removeDEM(selectedIndices[i]);
//        }
//
//        updateConfigFile();
//    }//GEN-LAST:event_deleteButtonActionPerformed

//    private void imageListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageListMousePressed
//        imageListMaybeShowPopup(evt);
//    }//GEN-LAST:event_imageListMousePressed
//
//    private void imageListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageListMouseReleased
//        imageListMaybeShowPopup(evt);
//    }//GEN-LAST:event_imageListMouseReleased
//
//    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
//        removeAllDEMsFromRenderer();
//    }//GEN-LAST:event_removeAllButtonActionPerformed
//
//    private void imageListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_imageListValueChanged
//        int[] indices = imageList.getSelectedIndices();
//        if (indices == null || indices.length == 0)
//        {
//            moveUpButton.setEnabled(false);
//            moveDownButton.setEnabled(false);
//            deleteButton.setEnabled(false);
//        }
//        else
//        {
//            deleteButton.setEnabled(true);
//            int minSelectedItem = imageList.getMinSelectionIndex();
//            int maxSelectedItem = imageList.getMaxSelectionIndex();
//            moveUpButton.setEnabled(minSelectedItem > 0);
//            moveDownButton.setEnabled(maxSelectedItem < imageList.getModel().getSize()-1);
//        }
//    }

	public ModelManager getModelManager()
	{
		return modelManager;
	}

	public Renderer getRenderer()
	{
		return renderer;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public double getPixelScale()
	{
		return pixelScale;
	}

	public void setPixelScale(double pixelScale)
	{
		this.pixelScale = pixelScale;
	}

	public int getHalfSize()
	{
		return halfSize;
	}

	public void setHalfSize(int halfSize)
	{
		this.halfSize = halfSize;
	}

	public String getMapmakerPath()
	{
		return mapmakerPath;
	}

	public void setMapmakerPath(String mapmakerPath)
	{
		this.mapmakerPath = mapmakerPath;
	}

	public String getBigmapPath()
	{
		return bigmapPath;
	}

	public void setBigmapPath(String bigmapPath)
	{
		this.bigmapPath = bigmapPath;
	}

	public int[] getSelectedIndices()
	{
		if (selectedIndices == null) return null;
		Arrays.sort(selectedIndices);
		return selectedIndices;
	}

	public void setSelectedIndex(int[] selectedIndices)
	{
		this.selectedIndices = selectedIndices;
	}

	public DEMInfo getSelectedItem()
	{
		return infoList.get(selectedIndices[0]);
	}

	public DEMInfo[] getSelectedItems()
	{
		DEMInfo[] infos = new DEMInfo[getSelectedIndices().length];
		for (int i=0; i<infos.length; i++)
		{
			infos[i] = infoList.get(getSelectedIndices()[i]);
		}
		return infos;
	}

	public List<DEMInfo> getInfoList()
	{
		return infoList;
	}

	public void addModelChangedListener(DEMCreationModelChangedListener listener)
	{
		listeners.add(listener);
	}

	public void fireInfoChangedListeners(DEMInfo info)
	{
		for (DEMCreationModelChangedListener listener : listeners)
		{
			listener.demInfoListChanged(info);
		}
	}

	public void fireInfoChangedListeners(List<DEMInfo> infos)
	{
		for (DEMCreationModelChangedListener listener : listeners)
		{
			listener.demInfoListChanged(infos);
		}
	}

	public void fireInfoRemovedListeners(DEMInfo info)
	{
		for (DEMCreationModelChangedListener listener : listeners)
		{
			listener.demInfoRemoved(info);
		}
	}

	public void fireInfosRemovedListeners(DEMInfo[] info)
	{
		for (DEMCreationModelChangedListener listener : listeners)
		{
			listener.demInfosRemoved(info);
		}
	}

	Key<Metadata[]> demInfosKey = Key.of("demInfos");

	@Override
	public Metadata store()
	{
		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
		Metadata[] infoArray = new Metadata[infoList.size()];
		int i=0;
		for (DEMInfo info : infoList)
		{
			infoArray[i++] = info.store();
		}
		write(demInfosKey, infoArray, configMetadata);
		return configMetadata;
	}

	@Override
	public void retrieve(Metadata source)
	{
		Metadata[] metadataArray = read(demInfosKey, source);
		for (Metadata meta : metadataArray)
		{
			DEMInfo info = new DEMInfo();
			info.retrieve(meta);
			infoList.add(info);
		}
	}

	protected <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value);
        }
    }

    protected <T> T read(Key<T> key, Metadata configMetadata)
    {
        T value = configMetadata.get(key);
        if (value != null)
            return value;
        return null;
    }


}
