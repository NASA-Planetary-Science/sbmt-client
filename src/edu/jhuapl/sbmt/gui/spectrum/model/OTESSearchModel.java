package edu.jhuapl.sbmt.gui.spectrum.model;

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
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

public class OTESSearchModel extends SpectrumSearchModel
{
    String fileExtension = "";
//    private OREXSearchSpec spec;

    public OTESSearchModel(SmallBodyViewConfig smallBodyConfig, ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager, PickManager pickManager,
            Renderer renderer, SpectralInstrument instrument)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, pickManager, renderer, instrument);

        setRedMaxVal(0.000001);
        setGreenMaxVal(0.000001);
        setBlueMaxVal(0.000001);

        setRedIndex(50);
        setGreenIndex(100);
        setBlueIndex(150);
    }

    @Override
    public void setSpectrumRawResults(List<List<String>> spectrumRawResults)
    {
        //TODO This need to really be shifted to use classes and not string representation until the end

        List<String> matchedImages=Lists.newArrayList();
        if (matchedImages.size() > 0)
            fileExtension = FilenameUtils.getExtension(matchedImages.get(0));

        super.setSpectrumRawResults(spectrumRawResults);
        fireResultsChanged();
        fireResultsCountChanged(this.results.size());
    }

    @Override
    public String createSpectrumName(int index)
    {
        return getSpectrumRawResults().get(index).get(0);
    }

    @Override
    public void populateSpectrumMetadata(List<String> lines)
    {
        SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
        for (int i=0; i<lines.size(); ++i)
        {
            OREXSearchSpec spectrumSpec = new OREXSearchSpec();
            spectrumSpec.fromFile(lines.get(0));
            collection.tagSpectraWithMetadata(createSpectrumName(i), spectrumSpec);
        }
    }

//    public OREXSearchSpec getSpec()
//    {
//        return spec;
//    }
//
//    public void setSpec(OREXSearchSpec spec)
//    {
//        this.spec = spec;
//    }
}