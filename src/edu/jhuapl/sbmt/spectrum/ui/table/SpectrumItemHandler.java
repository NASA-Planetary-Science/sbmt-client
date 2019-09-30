package edu.jhuapl.sbmt.spectrum.ui.table;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.sbmt.gui.lidar.LookUp;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * Item handler for displaying spectra in a table
 * @author steelrj1
 *
 */
public class SpectrumItemHandler<S extends BasicSpectrum> extends BasicItemHandler<S, LookUp>
{
	private final SpectraCollection<S> spectrumCollection;
	private final SpectrumBoundaryCollection<S> boundaryCollection;

	public SpectrumItemHandler(SpectraCollection<S> aManager, SpectrumBoundaryCollection<S> boundaryCollection, QueryComposer<LookUp> aComposer)
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
				return spec.getSpectrumName();
			case Date:
				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
				fmt.withZone(DateTimeZone.UTC);
				return fmt.print(spec.getDateTime());
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(S spec, LookUp aEnum, Object aValue)
	{
		if (aEnum == LookUp.Map)
		{
			if (!spectrumCollection.isSpectrumMapped(spec))
				spectrumCollection.addSpectrum(spec, spec.isCustomSpectra);
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