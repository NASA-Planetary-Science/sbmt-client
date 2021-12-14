package edu.jhuapl.sbmt.image2.ui.table.popup;

import java.awt.Component;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.table.popup.boundaryColor.BoundaryColorAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.export.ExportAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.properties.ShowInfoAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.properties.ShowSpectrumAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.rendering.CenterImageAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.rendering.ChangeNormalOffsetAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.rendering.ChangeOpacityAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.rendering.MapBoundaryAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.rendering.MapImageAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.rendering.ShowFrustumAction;
import edu.jhuapl.sbmt.image2.ui.table.popup.rendering.SimulateLightingAction;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

import glum.gui.action.PopupMenu;

public class ImageListPopupMenu extends PopupMenu<PerspectiveImage>
{

	public ImageListPopupMenu(
            ModelManager modelManager,
            PerspectiveImageCollection aManager,
            PerspectiveImageBoundaryCollection imageBoundaryCollection,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            Renderer renderer,
            Component invoker)
	{
		super(aManager);
		// TODO Auto-generated constructor stub

		MapImageAction mapAction = new MapImageAction<>(aManager);
		JCheckBoxMenuItem showHideCBMI = new JCheckBoxMenuItem(mapAction);
		showHideCBMI.setText("Map Image");
		installPopAction(mapAction, showHideCBMI);

		MapBoundaryAction mapBoundaryAction = new MapBoundaryAction(aManager);
		JCheckBoxMenuItem showHideBoundaryCBMI = new JCheckBoxMenuItem(mapBoundaryAction);
		showHideBoundaryCBMI.setText("Show Boundary");
		installPopAction(mapBoundaryAction, showHideBoundaryCBMI);

		ShowInfoAction<PerspectiveImage> showInfoAction = new ShowInfoAction<PerspectiveImage>(aManager, infoPanelManager);
		installPopAction(showInfoAction, "Properties...");

		if (spectrumPanelManager != null)
		{
			ShowSpectrumAction<PerspectiveImage> showSpectrumAction = new ShowSpectrumAction<PerspectiveImage>(aManager, spectrumPanelManager);
			installPopAction(showInfoAction, "Spectrum...");
		}

//		SaveBackplanesAction<PerspectiveImage> showBackplanesAction = new SaveBackplanesAction<PerspectiveImage>(aManager);
//		installPopAction(showBackplanesAction, "Generate Backplanes...");

		SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
		CenterImageAction<PerspectiveImage> centerImageAction = new CenterImageAction<PerspectiveImage>(aManager, renderer, List.of(smallBodyModel));
		installPopAction(centerImageAction, "Center in Window");

		ShowFrustumAction<PerspectiveImage> showFrustumAction = new ShowFrustumAction<PerspectiveImage>(aManager);
		JCheckBoxMenuItem showHideFrustumCBMI = new JCheckBoxMenuItem(showFrustumAction);
		showHideFrustumCBMI.setText("Show Frustum");
		installPopAction(showFrustumAction, showHideFrustumCBMI);

		JMenu exportMenu = new JMenu("Export as...");
		ExportAction<PerspectiveImage> exportAction = new ExportAction<>(aManager, invoker, exportMenu);
		installPopAction(exportAction, exportMenu);

//		ExportENVIImageAction<PerspectiveImage> exportENVIAction = new ExportENVIImageAction<PerspectiveImage>(aManager);
//		installPopAction(exportENVIAction, "Export ENVI Image...");
//
//		SaveImageAction<PerspectiveImage> saveImageAction = new SaveImageAction<PerspectiveImage>(aManager);
//		installPopAction(saveImageAction, "Export FITS Image...");
//
//		ExportInfofileAction<PerspectiveImage> exportInfofileAction = new ExportInfofileAction<PerspectiveImage>(aManager);
//		installPopAction(exportInfofileAction, "Export INFO File...");
//
//		ExportFitsInfoPairsAction<PerspectiveImage> exportFitsInfoPairsAction = new ExportFitsInfoPairsAction<PerspectiveImage>(aManager);
//		installPopAction(exportFitsInfoPairsAction, "Export FITS/Info File(s)...");

		ChangeNormalOffsetAction<PerspectiveImage> changeNormalOffsetAction = new ChangeNormalOffsetAction<PerspectiveImage>(aManager);
		installPopAction(changeNormalOffsetAction, "Change Normal Offset...");

		SimulateLightingAction<PerspectiveImage> simulateLightingAction = new SimulateLightingAction<PerspectiveImage>(aManager, renderer);
		JCheckBoxMenuItem simulateLightingCBMI = new JCheckBoxMenuItem(simulateLightingAction);
		simulateLightingCBMI.setText("Simulate Lighting");
		installPopAction(simulateLightingAction, simulateLightingCBMI);

		ChangeOpacityAction<PerspectiveImage> changeOpacityAction = new ChangeOpacityAction<PerspectiveImage>(aManager, renderer);
		installPopAction(changeOpacityAction, "Change Opacity...");

		JMenu colorMenu = new JMenu("Boundary Color");
		BoundaryColorAction<PerspectiveImage> boundaryColorAction = new BoundaryColorAction<>(aManager, invoker, colorMenu);
		installPopAction(boundaryColorAction, colorMenu);

//		SaveBackplanesAction<PerspectiveImage> showBackplanesAction = new SaveBackplanesAction<PerspectiveImage>(aManager);
//		installPopAction(showBackplanesAction, "Boundary Color");
//		colorMenu = new JMenu("Boundary Color");
//        this.add(colorMenu);
//        for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
//        {
//            JCheckBoxMenuItem colorMenuItem = new JCheckBoxMenuItem(new BoundaryColorAction(color.color()));
//            colorMenuItems.add(colorMenuItem);
//            colorMenuItem.setText(color.toString().toLowerCase().replace('_', ' '));
//            colorMenu.add(colorMenuItem);
//        }
//        colorMenu.addSeparator();
//        customColorMenuItem = new JMenuItem(new CustomBoundaryColorAction());
//        customColorMenuItem.setText("Custom...");
//        colorMenu.add(customColorMenuItem);
	}

}
