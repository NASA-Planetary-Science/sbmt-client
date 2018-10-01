package edu.jhuapl.sbmt.gui.spectrum.model;

import java.util.ArrayList;
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

    public OTESSearchModel(SmallBodyViewConfig smallBodyConfig, ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager, PickManager pickManager,
            Renderer renderer, SpectralInstrument instrument)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, pickManager, renderer, instrument);
    }

    @Override
    public void setSpectrumRawResults(List<List<String>> spectrumRawResults)
    {
//        view.getResultsLabel().setText(results.size() + " spectra matched");
        //TODO This need to really be shifted to use classes and not string representation until the end

        List<String> matchedImages=Lists.newArrayList();
        if (matchedImages.size() > 0)
            fileExtension = FilenameUtils.getExtension(matchedImages.get(0));
        for (List<String> res : results)
        {
            String basePath=FilenameUtils.getPath(res.get(0));
            String filename=FilenameUtils.getBaseName(res.get(0));

            matchedImages.add(basePath + filename + "." + FilenameUtils.getExtension(res.get(0)));
        }

//        setSpectrumRawResults(matchedImages);

        String[] formattedResults = new String[results.size()];

        // add the results to the list
        int i=0;
        for (String str : matchedImages)
        {
            formattedResults[i]=FilenameUtils.getBaseName(str) + "." + FilenameUtils.getExtension(str);
            ++i;
        }

//        view.getResultList().setListData(formattedResults);

        for (String res : formattedResults)
        {
            List<String> result = new ArrayList<String>();
            result.add(res);
            this.results.add(result);
        }


        // Show the first set of footprints
//        setResultIntervalCurrentlyShown(new IdPair(0, Integer.parseInt((String)view.getNumberOfFootprintsComboBox().getSelectedItem())));
//        this.showFootprints(getResultIntervalCurrentlyShown());
    }

    @Override
    public String createSpectrumName(int index)
    {
        return "/" + getSpectrumRawResults().get(index);
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

}
