package edu.jhuapl.sbmt.gui.eros;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.gui.spectrum.SpectrumSearchPanel;
import edu.jhuapl.sbmt.model.eros.NisQuery;
import edu.jhuapl.sbmt.model.spectrum.SpectralInstrument;

public class NISSearchPanel extends SpectrumSearchPanel
{

    Map<String,String> nisFileToObservationTimeMap=Maps.newHashMap();
    static Map<String,Vector3D> nisFileToSunPositionMap=Maps.newHashMap();

    public NISSearchPanel(ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager, PickManager pickManager,
            Renderer renderer, SpectralInstrument instrument)
    {
        super(modelManager, infoPanelManager, pickManager, renderer, instrument);
        // TODO Auto-generated constructor stub

        File nisTimesFile=FileCache.getFileFromServer("/NIS/2000/nisTimes.txt");
        try
        {
            Scanner scanner=new Scanner(nisTimesFile);
            boolean found=false;
            while (scanner.hasNextLine() && !found)
            {
                String line=scanner.nextLine();
                String[] tokens=line.replaceAll(",", "").trim().split("\\s+");
                String file=tokens[0];
                String time=tokens[1];
                nisFileToObservationTimeMap.put(file,time);
            }
            scanner.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setupComboBoxes();
    }

    static
    {
        File nisSunFile=FileCache.getFileFromServer("/NIS/nisSunVectors.txt");
        try
        {
            Scanner scanner=new Scanner(nisSunFile);
            boolean found=false;
            while (scanner.hasNextLine() && !found)
            {
                String line=scanner.nextLine();
                String[] tokens=line.replaceAll(",", "").trim().split("\\s+");
                String file=tokens[0];
                String x=tokens[1];
                String y=tokens[2];
                String z=tokens[3];
                nisFileToSunPositionMap.put(file,new Vector3D(Double.valueOf(x),Double.valueOf(y),Double.valueOf(z)).normalize());
            }
            scanner.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Vector3D getToSunUnitVector(String fileName)    // file name is taken relative to /project/nearsdc/data/NIS/2000
    {
        return nisFileToSunPositionMap.get(fileName);
    }


    @Override
    protected void setSpectrumSearchResults(List<List<String>> results)
    {
        spectrumResultsLabelText = results.size() + " spectra matched";
        resultsLabel.setText(spectrumResultsLabelText);

        List<String> matchedImages=Lists.newArrayList();
        for (List<String> res : results)
        {
            String path = NisQuery.getNisPath(res);
            matchedImages.add(path);
        }


        spectrumRawResults = matchedImages;

        String[] formattedResults = new String[results.size()];

        // add the results to the list
        int i=0;
        for (String str : matchedImages)
        {
            String fileNum=str.substring(16,25);
            String strippedFileName=str.replace("/NIS/2000/", "");
            String detailedTime=nisFileToObservationTimeMap.get(strippedFileName);
            formattedResults[i] = new String(
                    fileNum
                    + ", day: " + str.substring(10, 13) + "/" + str.substring(5, 9)+" ("+detailedTime+")"
                    );

            ++i;
        }

        resultList.setListData(formattedResults);


        // Show the first set of footprints
        this.resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)this.numberOfFootprintsComboBox.getSelectedItem()));
        this.showFootprints(resultIntervalCurrentlyShown);
    }

    @Override
    public String createSpectrumName(String currentSpectrumRaw)
    {
            return currentSpectrumRaw.substring(0,currentSpectrumRaw.length()-4) + ".NIS";
    }
}
