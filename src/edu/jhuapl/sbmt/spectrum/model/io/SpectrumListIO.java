package edu.jhuapl.sbmt.spectrum.model.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumIOException;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

/**
 * Helper class for saving and loading spectra to file
 * @author steelrj1
 *
 */
public class SpectrumListIO
{
	//Saving standard spectra

	/**
	 * Saves the specified spectra list designated by the indices in <pre>selectedIndices</pre> to the requested file
	 * @param <S>
	 * @param customDir
	 * @param file
	 * @param results
	 * @param selectedIndices
	 * @throws Exception
	 */
	public static <S extends BasicSpectrum> void saveSelectedSpectrumListButtonActionPerformed(String customDir, File file, List<S> results, int[] selectedIndices) throws Exception
    {
		if (file == null) return;

        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String nl = System.getProperty("line.separator");

        out.write("#Spectrum_Name Spectrum_Time_UTC"  + nl);
        for (int selectedIndex : selectedIndices)
        {
            String dtStr = sdf.format(results.get(selectedIndex).getDateTime().toDate());
            out.write(results.get(selectedIndex).getServerpath() + "," + dtStr + nl);
        }

        out.close();

    }

    /**
     * Saves the entire spectra list to file, using saveSelectedSpectrumListButtonActionPerformed
     * @param <S>
     * @param customDir
     * @param file
     * @param results
     * @throws Exception
     */
    public static <S extends BasicSpectrum> void saveSpectrumListButtonActionPerformed(String customDir, File file, List<S> results) throws Exception
    {
    	int[] selectedIndices = new int[results.size()];
    	for (int i=0; i<results.size(); i++) selectedIndices[i] = i;
    	saveSelectedSpectrumListButtonActionPerformed(customDir, file, results, selectedIndices);
    }


    // Loading standard spectra
    /**
     * Loads standard spectra from the requested <pre>file</pre>, and fires off the <pre>completionBlock</pre> afterwards
     * @param <S>
     * @param file
     * @param results
     * @param instrument
     * @param completionBlock
     * @throws Exception
     */
    public static <S extends BasicSpectrum> void loadSpectrumListButtonActionPerformed(File file, List<S> results, BasicSpectrumInstrument instrument, Runnable completionBlock) throws SpectrumIOException, Exception
    {
    	if (file == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
        if (!lines.get(0).startsWith("#")) throw new SpectrumIOException("Improper file format; please ensure you're not loading a custom spectrum saved list");

        for (int i=0; i<lines.size(); ++i)
        {
            if (lines.get(i).startsWith("#")) continue;
            IBasicSpectrumRenderer<S> spectrumRenderer = null;
            try
            {
            	String filename = lines.get(i).split(",")[0];
            	spectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(filename, instrument, false);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (spectrumRenderer != null)
            	results.add(spectrumRenderer.getSpectrum());
        }
        completionBlock.run();

    }

    //Loading custom spectra

	/**
	 * Loads custom spectra from the requested <pre>file</pre>, and fires off the <pre>completionBlock</pre> afterwards.
	 *
	 * Note: the file format here uses the metadata format, which is how the custom spectra tab is backed in the cache
	 * @param <S>
	 * @param file
	 * @param results
	 * @param instrument
	 * @param completionBlock
	 * @throws Exception
	 */
	public static <S extends BasicSpectrum> void loadCustomSpectrumListButtonActionPerformed(File file, boolean append,
			List<CustomSpectrumKeyInterface> results, BasicSpectrumInstrument instrument, Runnable completionBlock)
			throws SpectrumIOException
	{
		if (file == null) return;

		FixedMetadata metadata;
		try
		{
			final Key<List<CustomSpectrumKeyInterface>> customSpectraKey = Key.of("SavedSpectra");
			metadata = Serializers.deserialize(file, "SavedSpectra");
			List<CustomSpectrumKeyInterface> customSpectraList = metadata.get(customSpectraKey);

			if (append == false)
			{
				results.clear();
				results.addAll(customSpectraList);
			}
			else	//appending
			{
				for (CustomSpectrumKeyInterface key : customSpectraList)
				{
					if (!results.contains(key)) results.add(key);
				}
			}
			completionBlock.run();

		}
		catch (IOException e)
		{
			throw new SpectrumIOException("There was a problem reading the custom spectrum list.  Please make sure it is the right format, and not the one for the main spectrum panel", e);
		}
	}

	//Saving custom spectra

	/**
	 * Saves the specified custom spectra list designated by the indices in <pre>selectedIndices</pre> to the requested file
	 * @param <S>
	 * @param customDir
	 * @param file
	 * @param results
	 * @param selectedIndices
	 * @throws Exception
	 */
	public static <S extends BasicSpectrum> void saveCustomSelectedSpectrumListButtonActionPerformed(String customDir,
			File file, List<CustomSpectrumKeyInterface> results, int[] selectedIndices) throws Exception
	{
		if (file == null)
			return;

		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));

		final Key<List<CustomSpectrumKeyInterface>> customSpectraKey = Key.of("SavedSpectra");

		configMetadata.put(customSpectraKey, results);
		try
		{
			Serializers.serialize("SavedSpectra", configMetadata, file);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    /**
     * Saves the entire spectra list to file, using saveSelectedSpectrumListButtonActionPerformed
     * @param <S>
     * @param customDir
     * @param file
     * @param results
     * @throws Exception
     */
    public static <S extends BasicSpectrum> void saveCustomSpectrumListButtonActionPerformed(String customDir, File file, List<CustomSpectrumKeyInterface> results) throws Exception
    {
    	int[] selectedIndices = new int[results.size()];
    	for (int i=0; i<results.size(); i++) selectedIndices[i] = i;
    	saveCustomSelectedSpectrumListButtonActionPerformed(customDir, file, results, selectedIndices);
    }
}
