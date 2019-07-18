package edu.jhuapl.sbmt.model.bennu.ovirs;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.client.BodyViewConfig;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.model.bennu.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.ui.AbstractSpectrumSearchController;

public class OVIRSSearchPanel extends AbstractSpectrumSearchController
{
    String fileExtension = "";

    public OVIRSSearchPanel(BodyViewConfig smallBodyConfig, ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager, PickManager pickManager,
            Renderer renderer, ISpectralInstrument instrument, boolean isSearchView)
    {
        super(smallBodyConfig.hasHierarchicalSpectraSearch, smallBodyConfig.hierarchicalSpectraSearchSpecification,
        		smallBodyConfig.hasHypertreeBasedSpectraSearch, modelManager, infoPanelManager, pickManager, renderer, instrument, isSearchView);

        setupComboBoxes();
        setColoringComboBox();

        List<JSpinner> spinners=Lists.newArrayList(view.getBlueMaxSpinner(), view.getBlueMinSpinner(), view.getRedMaxSpinner(), view.getRedMinSpinner(),
                view.getGreenMaxSpinner(), view.getGreenMinSpinner());

        for (JSpinner spinner : spinners)
        {
            spinner.setModel(new SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.00001d)));
            NumberEditor editor = (NumberEditor)spinner.getEditor();
            DecimalFormat format = editor.getFormat();
            format.setMinimumFractionDigits(6);
        }

        view.getRedMaxSpinner().setValue(0.00005);
        view.getGreenMaxSpinner().setValue(0.0001);
        view.getBlueMaxSpinner().setValue(0.002);

        view.getRedComboBox().setSelectedIndex(736);
        view.getGreenComboBox().setSelectedIndex(500);
        view.getBlueComboBox().setSelectedIndex(50);

    }

    @Override
    protected void setSpectrumSearchResults(List<List<String>> results)
    {

        view.getResultsLabel().setText(results.size() + " spectra matched");
        List<String> matchedImages=Lists.newArrayList();
        String[] matched = new String[results.size()];

        int j = 0;
        for (List<String> res : results)
        {
            String basePath=FilenameUtils.getPath(res.get(0));
            String filename=FilenameUtils.getBaseName(res.get(0));

            matched[j] = basePath + filename + "." + FilenameUtils.getExtension(res.get(0));
            ++j;
        }

        Arrays.sort(matched);
        matchedImages = Arrays.asList(matched);
        model.setSpectrumRawResults(matchedImages);


        String[] formattedResults = new String[results.size()];

        // add the results to the list
        int i=0;
        for (String str : matchedImages)
        {
            formattedResults[i] = FilenameUtils.getBaseName(str) + "." + FilenameUtils.getExtension(str);
            ++i;
        }

        Arrays.sort(formattedResults);

        view.getResultList().setListData(formattedResults);


        // Show the first set of footprints
        model.setResultIntervalCurrentlyShown(new IdPair(0, Integer.parseInt((String)view.getNumberOfFootprintsComboBox().getSelectedItem())));
        this.showFootprints(model.getResultIntervalCurrentlyShown());

//        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
//        collection.deselectAll();


    }

    @Override
    public String createSpectrumName(int index)
    {
        return "/" + model.getSpectrumRawResults().get(index);
//        return "/earth/osirisrex/ovirs/spectra/"+FilenameUtils.getBaseName(currentSpectrumRaw)+".spect";
    }

    public void populateSpectrumMetadata(List<String> lines)
    {
        SpectraCollection collection = (SpectraCollection)model.getModelManager().getModel(ModelNames.SPECTRA);
        for (int i=0; i<lines.size(); ++i)
        {
            SpectrumSearchSpec spectrumSpec = new SpectrumSearchSpec();
            spectrumSpec.fromFile(lines.get(0));
            collection.tagSpectraWithMetadata(createSpectrumName(i), spectrumSpec);
        }
    }

}
