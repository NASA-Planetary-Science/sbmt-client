package edu.jhuapl.sbmt.gui.spectrum.model;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.bennu.OREXSearchSpec;
import edu.jhuapl.sbmt.model.bennu.OREXSpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

public class OVIRSSearchModel extends SpectrumSearchModel
{
    String fileExtension = "";

    public OVIRSSearchModel(SmallBodyViewConfig smallBodyConfig, ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager, PickManager pickManager,
            Renderer renderer, SpectralInstrument instrument)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, pickManager, renderer, instrument);

        if (smallBodyConfig.hierarchicalSpectraSearchSpecification == null)
        {
            try
            {
                //TODO: eventually point this to a URL
                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
                specIO.setPathString(smallBodyConfig.spectrumMetadataFile);
                specIO.loadMetadata();
                smallBodyConfig.hierarchicalSpectraSearchSpecification = specIO;


            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.spectraSpec = getSmallBodyConfig().hierarchicalSpectraSearchSpecification;

        setRedMaxVal(0.00005);
        setGreenMaxVal(0.0001);
        setBlueMaxVal(0.002);

        setRedIndex(736);
        setGreenIndex(500);
        setBlueIndex(50);
    }

    @Override
    public void setSpectrumRawResults(List<List<String>> spectrumRawResults)
    {
        //TODO This need to really be shifted to use classes and not string representation until the end
        List<String> matchedImages=Lists.newArrayList();
        if (matchedImages.size() > 0)
            fileExtension = FilenameUtils.getExtension(matchedImages.get(0));
        super.setSpectrumRawResults(spectrumRawResults);
//        fireResultsChanged();
//        fireResultsCountChanged(this.results.size());
    }

    @Override
    public String createSpectrumName(int index)
    {
        return getSpectrumRawResults().get(index).get(0);
    }

    @Override
    public void populateSpectrumMetadata(String line)
    {
        SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
        for (int i=0; i<results.size(); ++i)
        {
            OREXSearchSpec spectrumSpec = new OREXSearchSpec();
            spectrumSpec.fromFile(line);
            collection.tagSpectraWithMetadata(createSpectrumName(i), spectrumSpec);
        }
    }
}