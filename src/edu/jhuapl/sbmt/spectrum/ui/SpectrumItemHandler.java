package edu.jhuapl.sbmt.spectrum.ui;

import edu.jhuapl.sbmt.gui.lidar.LookUp;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class SpectrumItemHandler extends BasicItemHandler<BasicSpectrum, LookUp>
{
	private final SpectraCollection spectrumCollection;
	private final SpectrumBoundaryCollection boundaryCollection;

	public SpectrumItemHandler(SpectraCollection aManager, SpectrumBoundaryCollection boundaryCollection, QueryComposer<LookUp> aComposer)
	{
		super(aComposer);

		spectrumCollection = aManager;
		this.boundaryCollection = boundaryCollection;
	}

	@Override
	public Object getColumnValue(BasicSpectrum spec, LookUp aEnum)
	{
		switch (aEnum)
		{
			case Map:
				return spectrumCollection.isSpectrumMapped(spec);
			case Show:
				return spectrumCollection.getVisibility(spec);
			case Frus:
				return spectrumCollection.getFrustumVisibility(spec);
			case Bndr:
				return boundaryCollection.getVisibility(spec);
			case Id:
				return spectrumCollection.getAllItems().indexOf(spec);
			case Filename:
				return spec.getDataName();
			case Date:
				return spec.getDateTime();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(BasicSpectrum spec, LookUp aEnum, Object aValue)
	{
		if (aEnum == LookUp.Map)
		{
			if (!spectrumCollection.isSpectrumMapped(spec))
				spectrumCollection.addSpectrum(spec, false);
			else
				spectrumCollection.removeSpectrum(spec);
		}
		else if (aEnum == LookUp.Show)
			spectrumCollection.setVisibility(spec, (boolean) aValue);
		else if (aEnum == LookUp.Frus)
			spectrumCollection.setFrustumVisibility(spec, (boolean) aValue);
		else if (aEnum == LookUp.Bndr)
			boundaryCollection.setVisibility(spec, (boolean) aValue);
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}