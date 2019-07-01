package edu.jhuapl.sbmt.dtm.controller;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.dialog.OpacityChanger;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.camera.Camera;
import edu.jhuapl.saavtk.gui.render.camera.CameraUtil;
import edu.jhuapl.saavtk.gui.render.camera.CoordinateSystem;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.sbmt.dtm.model.DEM;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.dtm.service.io.DEMIO;
import edu.jhuapl.sbmt.dtm.ui.menu.IDEMPopupMenuActionListener;
import edu.jhuapl.sbmt.dtm.ui.properties.DEMView;

public class DEMPopupMenuActionListener implements IDEMPopupMenuActionListener
{
	private DEMCollection demCollection;
    private DEMBoundaryCollection demBoundaryCollection;

	public DEMPopupMenuActionListener(DEMCollection demCollection,
            DEMBoundaryCollection demBoundaryCollection)
	{
		this.demBoundaryCollection = demBoundaryCollection;
		this.demCollection = demCollection;
	}

	@Override
	public void mapDEM(List<DEMKey> demKeys, boolean isSelected)
	{
		for (DEMKey demKey : demKeys)
        {
            if (isSelected)
            {
                demCollection.addDEM(demKey);
            }
            else
            {
                demCollection.removeDEM(demKey);
            }
        }
	}

	@Override
	public void showDEM(List<DEMKey> demKeys, boolean selected)
	{
		 for (DEMKey demKey : demKeys)
         {
             demCollection.addDEM(demKey);
             DEM dem = demCollection.getDEM(demKey);
             dem.setVisible(selected);
         }
	}

	@Override
	public void showDEMBoundary(List<DEMKey> demKeys, boolean selected)
	{
		 for (DEMKey demKey : demKeys)
         {
             if (selected)
                 demBoundaryCollection.addBoundary(demKey);
             else
                 demBoundaryCollection.removeBoundary(demKey);
         }
	}

	@Override
	public void centerDEM(List<DEMKey> demKeys, Renderer renderer)
	{
		Map<DEMKey, CoordinateSystem> cacheMap = new HashMap<>();
		if (demKeys.size() != 1) return;

        DEMKey demKey = demKeys.get(0);
        DEM dem = demCollection.getDEM(demKey);

        // Calculate cache vars
        CoordinateSystem tmpCoordinateSystem = cacheMap.get(demKey);
        if (tmpCoordinateSystem == null)
        {
            // Form a CoordinateSystem relative to the DEM
            Vector3D centerVect = dem.getGeometricCenterPoint();
            Vector3D normalVect = dem.getAverageSurfaceNormal();
            tmpCoordinateSystem = CameraUtil.formCoordinateSystem(normalVect, centerVect);

            // Update the cache
            cacheMap.put(demKey, tmpCoordinateSystem);
        }

        // Compute the appropriate view vectors
        Vector3D focalVect = tmpCoordinateSystem.getOrigin();

        double zMag = dem.getBoundingBoxDiagonalLength() * 2.0;
        Vector3D targVect = tmpCoordinateSystem.getAxisZ().scalarMultiply(zMag).add(focalVect);

        Vector3D viewUpVect = tmpCoordinateSystem.getAxisY();

        // Update the camera to reflect the new view
        Camera tmpCamera = renderer.getCamera();
        tmpCamera.setView(focalVect, targVect, viewUpVect);
	}

	@Override
	public void showDEMProperties(List<DEMKey> demKeys, PolyhedralModel smallBodyModel)
	{
		if (demKeys.size() != 1)
            return;

        DEMKey demKey = demKeys.get(0);

        try
        {
            DEM macroDEM = demCollection.getDEM(demKey);
            if(macroDEM.hasView())
            {
                // View already exists, just bring it to the front
                macroDEM.getView().toFront();
            }
            else
            {
                // No view currently exists, create one and associate it to the DEM
                DEMView view = new DEMView(demKey, demCollection, smallBodyModel);
                macroDEM.setView(view);

                // ugh... this is a hack to get the renderer to wake up and look at the DEM, otherwise it is by default looking at the origin and the user might think that there is a bug since the DEM is usually not visible from that viewpoint
                // ....  It would be nice if instead we could call getRenderer().resetCamera() but there is some synchronization issue between the 3d view and DEM actors that breaks this approach -- so just making the camera at least look in the direction of the DEM seems to get around the issue for now -- zimmemi1
                view.getRenderer().getRenderWindowPanel().getActiveCamera().SetFocalPoint(macroDEM.getBoundingBox().getCenterPoint());
            }

        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

	}

	@Override
	public void saveDEMToFITS(List<DEMKey> demKeys, Component invoker)
	{
		DEMIO.exportToFits(demKeys, demCollection, invoker);
	}

	@Override
	public void changeDEMOpacity(List<DEMKey> demKeys, Renderer renderer)
	{
		if (demKeys.size() != 1)
            return;
        DEMKey demKey = demKeys.get(0);
        DEM dem = demCollection.getDEM(demKey);
        if (dem == null) return;
        OpacityChanger opacityChanger = new OpacityChanger(dem);
        opacityChanger.setLocationRelativeTo(renderer);
        opacityChanger.setVisible(true);
	}

	@Override
	public void setDEMBoundaryColor(List<DEMKey> keys, Color color)
	{
		demBoundaryCollection.setColors(keys, color);

	}

	@Override
	public String exportDEMToCustomModel(List<DEMKey> demKeys, PolyhedralModel smallBodyModel)
	{
        String modelName = DEMIO.exportDEMToCustomModel(demKeys, demCollection, smallBodyModel);
        return modelName;
	}


}
