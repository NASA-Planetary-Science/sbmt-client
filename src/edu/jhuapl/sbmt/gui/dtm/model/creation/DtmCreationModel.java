package edu.jhuapl.sbmt.gui.dtm.model.creation;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.model.dem.DEM;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DtmCreationModel
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

    private boolean initialized = false;

    public static class DEMInfo
    {
        public String name = ""; // name to call this image for display purposes
        public String demfilename = ""; // filename of image on disk

        @Override
        public String toString()
        {
            DecimalFormat df = new DecimalFormat("#.#####");
            return name;
        }
    }

	public DtmCreationModel()
	{
		 // Get collections
        dems = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        boundaries = (DEMBoundaryCollection)modelManager.getModel(ModelNames.DEM_BOUNDARY);
        infoList = new ArrayList<DEMInfo>();
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
    private void initializeDEMList() throws IOException
    {
        if (initialized)
            return;

        MapUtil configMap = new MapUtil(getDEMConfigFilename());

        if (configMap.containsKey(DEM.DEM_NAMES))
        {
            boolean needToUpgradeConfigFile = false;
            String[] demNames = configMap.getAsArray(DEM.DEM_NAMES);
            String[] demFilenames = configMap.getAsArray(DEM.DEM_FILENAMES);
            if (demFilenames == null)
            {
                // for backwards compatibility
                demFilenames = configMap.getAsArray(DEM.DEM_MAP_PATHS);
                demNames = new String[demFilenames.length];
                for (int i=0; i<demFilenames.length; ++i)
                {
                    demNames[i] = new File(demFilenames[i]).getName();
                    demFilenames[i] = "dem" + i + ".FIT";
                }

                // Mark that we need to upgrade config file to latest version
                // which we'll do at end of function.
                needToUpgradeConfigFile = true;
            }

            int numDems = demNames != null ? demNames.length : 0;
            for (int i=0; i<numDems; ++i)
            {
                DEMInfo demInfo = new DEMInfo();
                demInfo.name = demNames[i];
                demInfo.demfilename = demFilenames[i];
                infoList.add(demInfo);
//                ((DefaultListModel)imageList.getModel()).addElement(demInfo);
            }

            if (needToUpgradeConfigFile)
            {
                updateConfigFile();
            }
        }

        initialized = true;
    }

    public void updateConfigFile()
    {
        MapUtil configMap = new MapUtil(getDEMConfigFilename());

        String demNames = "";
        String demFilenames = "";

//        DefaultListModel demListModel = (DefaultListModel)imageList.getModel();
        for (int i=0; i<infoList.size(); ++i)
        {
            DEMInfo demInfo = infoList.get(i);

            demFilenames += demInfo.demfilename;
            demNames += demInfo.name;
            if (i < infoList.size()-1)
            {
                demNames += CustomShapeModel.LIST_SEPARATOR;
                demFilenames += CustomShapeModel.LIST_SEPARATOR;
            }
        }

        Map<String, String> newMap = new LinkedHashMap<String, String>();

        newMap.put(DEM.DEM_NAMES, demNames);
        newMap.put(DEM.DEM_FILENAMES, demFilenames);

        configMap.put(newMap);
    }

    // Removes all DEMs
    private void removeAllDEMsFromRenderer()
    {
        DEMCollection demCollection = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        demCollection.removeDEMs();
    }

    // Removes a DEM
    private void removeDEM(int index)
    {
        // Get the DEM info
        DEMInfo demInfo = infoList.get(index);

        // Remove from cache
        String name = getCustomDataFolder() + File.separator + demInfo.demfilename;
        new File(name).delete();

        // Remove the DEM from the renderer
        DEMCollection demCollection = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        DEMKey demKey = new DEMKey(name, demInfo.name);
        demCollection.removeDEM(demKey);

        // Remove from the list
        infoList.remove(index);
        DEMBoundaryCollection boundaries =
                (DEMBoundaryCollection)modelManager.getModel(getDEMBoundaryCollectionModelName());
        boundaries.removeBoundary(demKey);
    }


    public void saveDEM(DEMInfo demInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

//        if(demInfo.demfilename.endsWith(".fit") || demInfo.demfilename.endsWith(".fits") ||
//                demInfo.demfilename.endsWith(".FIT") || demInfo.demfilename.endsWith(".FITS"))
        {
            // Copy FIT file to cache
            String newFilename = "dem-" + uuid + ".fit";
            String newFilepath = getCustomDataFolder() + File.separator + newFilename;
            FileUtil.copyFile(demInfo.demfilename,  newFilepath);
            // Change demInfo.demfilename to the new location of the file
            demInfo.demfilename = newFilename;

//            DefaultListModel model = (DefaultListModel)imageList.getModel();
//            model.addElement(demInfo);
            infoList.add(demInfo);
            updateConfigFile();
        }
//        else
//        {
//            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
//                    "DEM file does not have valid FIT extension.",
//                    "Error",
//                    JOptionPane.ERROR_MESSAGE);
//        }
    }


    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int[] selectedIndices = getSelectedIndices();
        Arrays.sort(selectedIndices);
        for (int i=selectedIndices.length-1; i>=0; --i)
        {
            removeDEM(selectedIndices[i]);
        }

        updateConfigFile();
    }//GEN-LAST:event_deleteButtonActionPerformed

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

}
