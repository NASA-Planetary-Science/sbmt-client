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
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

/**
 * Helper class for saving and loading spectra to file
 * @author steelrj1
 *
 */
public class SpectrumListIO
{

	public static <S extends BasicSpectrum> void saveSelectedSpectrumListButtonActionPerformed(String customDir, File file, List<S> results, int[] selectedIndices) throws Exception
    {
        String metadataFilename = customDir + File.separator + file.getName() + ".metadata";
        if (file != null)
        {
            FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            String nl = System.getProperty("line.separator");

            out.write("#Spectrum_Name Spectrum_Time_UTC"  + nl);
            for (int selectedIndex : selectedIndices)
            {
            	System.out.println("SpectrumListIO: saveSelectedSpectrumListButtonActionPerformed: result is " + results.get(selectedIndex).getDateTime());
                String dtStr = sdf.format(results.get(selectedIndex).getDateTime().toDate());
                out.write(results.get(selectedIndex).getFullPath() + "," + dtStr + nl);
            }

            out.close();
        }
    }

    public static <S extends BasicSpectrum> void saveSpectrumListButtonActionPerformed(String customDir, File file, List<S> results) throws Exception
    {
    	int[] selectedIndices = new int[results.size()];
    	for (int i=0; i<results.size(); i++) selectedIndices[i] = i;
    	saveSelectedSpectrumListButtonActionPerformed(customDir, file, results, selectedIndices);
    }

    public static <S extends BasicSpectrum> void loadSpectrumListButtonActionPerformed(File file, List<S> results, BasicSpectrumInstrument instrument, Runnable completionBlock) throws Exception
    {
    	if (file != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
            for (int i=0; i<lines.size(); ++i)
            {
                if (lines.get(i).startsWith("#")) continue;
                IBasicSpectrumRenderer<S> spectrumRenderer = null;
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
