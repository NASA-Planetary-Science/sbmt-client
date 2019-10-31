package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Ranges;

import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.core.listeners.SearchProgressListener;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.query.IQueryBase;
import edu.jhuapl.sbmt.query.database.DatabaseQueryBase;
import edu.jhuapl.sbmt.query.database.SpectraDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.InstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;

/**
 * Standard spectrum search class.  May be hierarchical, fixed list, or database driven.  Can also accept cube list defining the region being searched
 * @author steelrj1
 *
 */
public class SpectrumStandardSearch<S extends BasicSpectrum>
{
	SpectrumSearchParametersModel searchParameters;
    private boolean hasHierarchicalSpectraSearch;
    protected SpectraHierarchicalSearchSpecification spectraSpec;

	public SpectrumStandardSearch(SpectrumSearchParametersModel searchParameters, boolean hasHierarchicalSpectraSearch, SpectraHierarchicalSearchSpecification<?> searchSpec)
	{
		this.searchParameters = searchParameters;
		this.hasHierarchicalSpectraSearch = hasHierarchicalSpectraSearch;
		this.spectraSpec = searchSpec;
	}

	public List<S> search(BasicSpectrumInstrument instrument, TreeSet<Integer> cubeList, TreePath[] selectedPaths, SearchProgressListener progressListener)
	{
		List<S> tempResults = null;
        try
        {
            GregorianCalendar startDateGreg = new GregorianCalendar();
            GregorianCalendar endDateGreg = new GregorianCalendar();
            startDateGreg.setTime(searchParameters.getStartDate());
            endDateGreg.setTime(searchParameters.getEndDate());
            DateTime startDateJoda = new DateTime(
                    startDateGreg.get(GregorianCalendar.YEAR),
                    startDateGreg.get(GregorianCalendar.MONTH)+1,
                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    startDateGreg.get(GregorianCalendar.MINUTE),
                    startDateGreg.get(GregorianCalendar.SECOND),
                    startDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);
            DateTime endDateJoda = new DateTime(
                    endDateGreg.get(GregorianCalendar.YEAR),
                    endDateGreg.get(GregorianCalendar.MONTH)+1,
                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    endDateGreg.get(GregorianCalendar.MINUTE),
                    endDateGreg.get(GregorianCalendar.SECOND),
                    endDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);

            List<Integer> productsSelected;
            if(hasHierarchicalSpectraSearch)
            {
            	spectraSpec.readHierarchyForInstrument(instrument.getDisplayName());
//                SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
                // Process the user's selections
                spectraSpec.processTreeSelections(selectedPaths);

                productsSelected = spectraSpec.getSelectedDatasets();
                InstrumentMetadata<SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
                TreeModel tree = spectraSpec.getTreeModel();
                List<SearchSpec> specs = instrumentMetadata.getSpecs();
                for (Integer selected : productsSelected)
                {
                    SearchSpec spec = specs.get(selected);
                    FixedListSearchMetadata searchMetadata = FixedListSearchMetadata.of(spec.getDataName(),
                                                                                        spec.getDataListFilename(),
                                                                                        spec.getDataPath(),
                                                                                        spec.getDataRootLocation(),
                                                                                        spec.getSource());
//                    progressListener.searchStarted();
                    progressListener.searchIndeterminate();
                    progressListener.searchNoteUpdated("Getting results from server....");
                    List<List<String>> thisResult = instrument.getQueryBase().runQuery(searchMetadata).getResultlist();
                    int i=0;

                    BasicSpectrum spectrum = null;
                    tempResults = new ArrayList<S>(thisResult.size());
                    progressListener.searchNoteUpdated("Processing results....");
                    for (List<String> str : thisResult)
                    {
                    	 try
                         {
                         	spectrum = SbmtSpectrumModelFactory.createSpectrum(str.get(0), instrument, str.get(1));
                         	spectrum.setMetadata(spec);
                         	tempResults.add((S)spectrum);
                         	i++;
                         	progressListener.searchProgressChanged((int)((((double)i/(double)thisResult.size())*100)));
                         }
                         catch (Exception e) {
                        	 System.out.println("SpectrumStandardSearch: search error when building spectrum: " + e.getLocalizedMessage());
                        	 e.printStackTrace();
                         }
                    }
                    progressListener.searchEnded();
                }
            }
            else
            {
                IQueryBase queryType = instrument.getQueryBase();
                if (queryType instanceof FixedListQuery)
                {
                    FixedListQuery query = (FixedListQuery)queryType;
                    tempResults = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist", "spectra", query.getRootPath(), ImageSource.CORRECTED_SPICE)).getResultlist();
                }
                else
                {
                    SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                            Ranges.closed(searchParameters.getMinDistanceQuery(), searchParameters.getMaxDistanceQuery()),
                            "", searchParameters.getPolygonTypesChecked(),
                            Ranges.closed(searchParameters.getMinIncidenceQuery(), searchParameters.getMaxIncidenceQuery()),
                            Ranges.closed(searchParameters.getMinEmissionQuery(), searchParameters.getMaxEmissionQuery()),
                            Ranges.closed(searchParameters.getMinPhaseQuery(), searchParameters.getMaxPhaseQuery()),
                            cubeList);
                    DatabaseQueryBase query = (DatabaseQueryBase)queryType;
                    progressListener.searchIndeterminate();
                    tempResults = query.runQuery(searchMetadata).getResultlist();
                    progressListener.searchEnded();
                }
            }
            return tempResults;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return null;
        }
	}
}
