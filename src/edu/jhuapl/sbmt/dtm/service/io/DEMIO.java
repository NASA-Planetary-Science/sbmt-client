package edu.jhuapl.sbmt.dtm.service.io;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.ShapeModelImporterDialog;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.client.SmallBodyViewConfigMetadataIO;
import edu.jhuapl.sbmt.dtm.model.DEM;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.DEMKey;

public class DEMIO
{

	public DEMIO()
	{
		// TODO Auto-generated constructor stub
	}

	public static void exportToFits(List<DEMKey> demKeys, DEMCollection demCollection, Component invoker)
	{
		if (demKeys.size() != 1)
            return;
        DEMKey demKey = demKeys.get(0);

        File file = null;
        try
        {
            demCollection.addDEM(demKey);
            String imageFileName = new File(demKey.demfilename).getName();

            file = CustomFileChooser.showSaveDialog(invoker, "Save FITS file", imageFileName, "fit");
            if (file != null)
            {
                File fitFile = FileCache.getFileFromServer("file://" + demKey.demfilename);
                FileUtil.copyFile(fitFile, file);
            }
        }
        catch(IOException ex)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                    "Unable to save file to " + file.getAbsolutePath(),
                    "Error Saving File",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
	}

	public static void exportDEMToCustomModel(List<DEMKey> demKeys, DEMCollection demCollection, PolyhedralModel smallBodyModel)
	{
		DEMKey demKey = demKeys.get(0);

        DEM dem = demCollection.getDEM(demKey);
        if (dem == null) return;

        vtkPolyData demPolydata = dem.getDem();
        String demFilename = dem.getKey().demfilename;
        final int extensionLength = FilenameUtils.getExtension(demFilename).length();

        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetFileName(demFilename.substring(0, demFilename.length()-extensionLength) + "vtk");
        writer.SetFileTypeToBinary();
        writer.SetInputData(demPolydata);
        writer.Write();

        //write out copy of current model's smallbody view config here
        Vector<ViewConfig> config = new Vector<ViewConfig>();
        config.add(smallBodyModel.getConfig().clone());

        ShapeModelImporterDialog dialog = new ShapeModelImporterDialog(null);

        String extension = FilenameUtils.getExtension(demFilename);
        dialog.populateCustomDEMImport(demFilename.substring(0, demFilename.length()-extensionLength) + extension, extension);
        dialog.beforeOKRunner = new Runnable()
        {
            final String filename = demFilename;
            @Override
            public void run()
            {
                try
                {
                    SmallBodyViewConfig config2 = (SmallBodyViewConfig)(config.get(0));
                    config2.modelLabel = dialog.getNameOfImportedShapeModel();
                    config2.customTemporary = false;
                    config2.author = ShapeModelType.CUSTOM;
                    SmallBodyViewConfigMetadataIO metadataIO = new SmallBodyViewConfigMetadataIO(new Vector<ViewConfig>(config));
                    File file = SafeURLPaths.instance().get(demFilename.substring(6, demFilename.length()-extensionLength)+ "json").toFile();
                    metadataIO.write(file, dialog.getNameOfImportedShapeModel());
                    SmallBodyViewConfig config = (SmallBodyViewConfig)metadataIO.getConfigs().get(0);
                    dialog.setDisplayName(dialog.getNameOfImportedShapeModel());
                }
                catch (IOException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        };

        dialog.setVisible(true);
	}

}
