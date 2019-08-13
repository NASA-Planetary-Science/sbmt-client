package edu.jhuapl.sbmt.spectrum.model.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class SpectrumListIO
{

	public static void saveSelectedSpectrumListButtonActionPerformed(String customDir, File file, List<BasicSpectrum> results, int[] selectedIndices) throws Exception
    {
        String metadataFilename = customDir + File.separator + file.getName() + ".metadata";
        if (file != null)
        {
            FileWriter fstream = new FileWriter(file);
            FileWriter fstream2 = new FileWriter(metadataFilename);
            BufferedWriter out = new BufferedWriter(fstream);
            BufferedWriter out2 = new BufferedWriter(fstream2);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            String nl = System.getProperty("line.separator");

            SearchSpec spectrumSpec = results.get(0).getSpec();

            if (spectrumSpec != null)
                spectrumSpec.toFile(out);

            out.write("#Spectrum_Name Spectrum_Time_UTC"  + nl);
            for (int selectedIndex : selectedIndices)
            {
                String dtStr = sdf.format(results.get(selectedIndex).getDateTime().toDate());
                out.write(results.get(selectedIndex).getFullPath() + "," + dtStr + nl);
            }

            out.close();
            out2.close();
        }
    }

    public static void saveSpectrumListButtonActionPerformed(String customDir, File file, List<BasicSpectrum> results) throws Exception
    {
    	int[] selectedIndices = new int[results.size()];
    	for (int i=0; i<results.size(); i++) selectedIndices[i] = i;
    	saveSelectedSpectrumListButtonActionPerformed(customDir, file, results, selectedIndices);
    }

    public static void loadSpectrumListButtonActionPerformed(File file, List<BasicSpectrum> results, BasicSpectrumInstrument instrument, Runnable completionBlock) throws Exception
    {
    	if (file != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
            for (int i=0; i<lines.size(); ++i)
            {
                if (lines.get(i).startsWith("#")) continue;
                IBasicSpectrumRenderer spectrumRenderer = null;
                try
                {
                	spectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(file.getAbsolutePath(), instrument);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                results.add(spectrumRenderer.getSpectrum());
            }
            completionBlock.run();
        }
    }
}
