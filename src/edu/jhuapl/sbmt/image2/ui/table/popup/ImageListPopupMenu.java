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
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
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

import glum.gui.action.PopupMenu;

public class ImageListPopupMenu<G1 extends IPerspectiveImage  & IPerspectiveImageTableRepresentable> extends PopupMenu<G1>
{

	public ImageListPopupMenu(
            ModelManager modelManager,
            PerspectiveImageCollection aManager,
//            PerspectiveImageBoundaryCollection imageBoundaryCollection,
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

		ShowInfoAction<G1> showInfoAction = new ShowInfoAction<G1>(aManager, infoPanelManager);
		installPopAction(showInfoAction, "Properties...");

		if (spectrumPanelManager != null)
		{
			ShowSpectrumAction<G1> showSpectrumAction = new ShowSpectrumAction<G1>(aManager, spectrumPanelManager);
			installPopAction(showSpectrumAction, "Spectrum...");
		}

//		SaveBackplanesAction<PerspectiveImage> showBackplanesAction = new SaveBackplanesAction<PerspectiveImage>(aManager);
//		installPopAction(showBackplanesAction, "Generate Backplanes...");

		SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
		CenterImageAction<G1> centerImageAction = new CenterImageAction<G1>(aManager, renderer, List.of(smallBodyModel));
		installPopAction(centerImageAction, "Center in Window");

		ShowFrustumAction<G1> showFrustumAction = new ShowFrustumAction<G1>(aManager);
		JCheckBoxMenuItem showHideFrustumCBMI = new JCheckBoxMenuItem(showFrustumAction);
		showHideFrustumCBMI.setText("Show Frustum");
		installPopAction(showFrustumAction, showHideFrustumCBMI);

		JMenu exportMenu = new JMenu("Export as...");
		ExportAction<G1> exportAction = new ExportAction<>(aManager, invoker, exportMenu);
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

		ChangeNormalOffsetAction<G1> changeNormalOffsetAction = new ChangeNormalOffsetAction<G1>(aManager);
		installPopAction(changeNormalOffsetAction, "Change Normal Offset...");

		SimulateLightingAction<G1> simulateLightingAction = new SimulateLightingAction<G1>(aManager, renderer);
		JCheckBoxMenuItem simulateLightingCBMI = new JCheckBoxMenuItem(simulateLightingAction);
		simulateLightingCBMI.setText("Simulate Lighting");
		installPopAction(simulateLightingAction, simulateLightingCBMI);

		ChangeOpacityAction<G1> changeOpacityAction = new ChangeOpacityAction<G1>(aManager, renderer);
		installPopAction(changeOpacityAction, "Change Opacity...");

		JMenu colorMenu = new JMenu("Boundary Color");
		BoundaryColorAction<G1> boundaryColorAction = new BoundaryColorAction<>(aManager, invoker, colorMenu);
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
