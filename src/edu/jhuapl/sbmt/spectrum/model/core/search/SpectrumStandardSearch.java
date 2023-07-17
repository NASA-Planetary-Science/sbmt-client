package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Range;

import edu.jhuapl.sbmt.core.listeners.SearchProgressListener;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.query.database.SpectraDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;
import edu.jhuapl.sbmt.query.v2.FetchedResults;
import edu.jhuapl.sbmt.query.v2.IDataQuery;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumIOException;
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

	public FetchedResults search(BasicSpectrumInstrument instrument, TreeSet<Integer> cubeList, TreePath[] selectedPaths, SearchProgressListener progressListener) throws SpectrumIOException
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
            FetchedResults thisResult = null;
            if(hasHierarchicalSpectraSearch)
            {
            	spectraSpec.readHierarchyForInstrument(instrument.getDisplayName());
                // Process the user's selections
                spectraSpec.processTreeSelections(selectedPaths);

                productsSelected = spectraSpec.getSelectedDatasets();
                InstrumentMetadata<SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
                TreeModel tree = spectraSpec.getTreeModel();
                List<SearchSpec> specs = instrumentMetadata.getSpecs();
                tempResults = new ArrayList<S>();
                for (Integer selected : productsSelected)
                {
                    SearchSpec spec = specs.get(selected);
                    System.out.println("SpectrumStandardSearch: search: spec " + spec.getDataDescription());
                    searchParameters.setDataType(spec.getDataPath());
                    FixedListSearchMetadata searchMetadata = FixedListSearchMetadata.of(spec.getDataName(),
                                                                                        spec.getDataListFilename(),
                                                                                        spec.getDataPath(),
                                                                                        spec.getDataRootLocation(),
                                                                                        spec.getSource());

//                    SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
//                            Range.closed(searchParameters.getMinDistanceQuery(), searchParameters.getMaxDistanceQuery()),
//                            searchParameters.getSearchByFilename(), searchParameters.getPolygonTypesChecked(),
//                            Range.closed(searchParameters.getMinIncidenceQuery(), searchParameters.getMaxIncidenceQuery()),
//                            Range.closed(searchParameters.getMinEmissionQuery(), searchParameters.getMaxEmissionQuery()),
//                            Range.closed(searchParameters.getMinPhaseQuery(), searchParameters.getMaxPhaseQuery()),
//                            cubeList, searchParameters.getModelName(), searchParameters.getDataType());


                    progressListener.searchIndeterminate();
                    progressListener.searchNoteUpdated("Getting results from server....");
//                    List<S> thisResult = instrument.getQueryBase().runQuery(searchMetadata).getResultlist();
//                    tempResults.addAll(thisResult);
                    thisResult = instrument.getQueryBase().runQuery(searchMetadata);
                    progressListener.searchEnded();
                }
            }
            else
            {
                IDataQuery queryType = instrument.getQueryBase();
                if (queryType instanceof FixedListQuery)
                {
                    FixedListQuery query = (FixedListQuery)queryType;
                    thisResult = instrument.getQueryBase().runQuery(FixedListSearchMetadata.of("Spectrum Search", "spectrumlist", "spectra", query.getRootPath(), PointingSource.CORRECTED_SPICE));
                }
                else
                {
                    SpectraDatabaseSearchMetadata searchMetadata = SpectraDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                            Range.closed(searchParameters.getMinDistanceQuery(), searchParameters.getMaxDistanceQuery()),
                            searchParameters.getSearchByFilename(), searchParameters.getPolygonTypesChecked(),
                            Range.closed(searchParameters.getMinIncidenceQuery(), searchParameters.getMaxIncidenceQuery()),
                            Range.closed(searchParameters.getMinEmissionQuery(), searchParameters.getMaxEmissionQuery()),
                            Range.closed(searchParameters.getMinPhaseQuery(), searchParameters.getMaxPhaseQuery()),
                            cubeList, searchParameters.getModelName(), searchParameters.getDataType());
//                    DatabaseQueryBase query = (DatabaseQueryBase)queryType;
                    progressListener.searchIndeterminate();
                    thisResult = instrument.getQueryBase().runQuery(searchMetadata);
                    progressListener.searchEnded();
                }
            }
            System.out.println("SpectrumStandardSearch: search: number of results " + thisResult.size());
            return thisResult;
        }
        catch (RuntimeException re)
        {
        	System.out.println("SpectrumStandardSearch: search: rethrow");
        	re.printStackTrace();
        	throw new SpectrumIOException(re.getMessage());
        }
        catch (Exception e)
        {
        	System.out.println("SpectrumStandardSearch: search: exception");
            e.printStackTrace();
            System.out.println(e);
            return null;
        }
	}
}
