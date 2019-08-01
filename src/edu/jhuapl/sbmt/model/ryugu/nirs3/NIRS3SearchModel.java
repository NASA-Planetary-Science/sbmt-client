package edu.jhuapl.sbmt.model.ryugu.nirs3;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;

public class NIRS3SearchModel extends BaseSpectrumSearchModel
{
    public NIRS3SearchModel(ModelManager modelManager, ISpectralInstrument instrument)
    {
        super(modelManager, instrument);

        getColoringModel().setRedMaxVal(0.00005);
        getColoringModel().setGreenMaxVal(0.0001);
        getColoringModel().setBlueMaxVal(0.002);

        getColoringModel().setRedIndex(100);
        getColoringModel().setGreenIndex(70);
        getColoringModel().setBlueIndex(40);
    }
}
