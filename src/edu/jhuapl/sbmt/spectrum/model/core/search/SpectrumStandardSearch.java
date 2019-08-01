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
import edu.jhuapl.sbmt.core.InstrumentMetadata;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.query.IQueryBase;
import edu.jhuapl.sbmt.query.database.DatabaseQueryBase;
import edu.jhuapl.sbmt.query.database.SpectraDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;
import edu.jhuapl.sbmt.spectrum.model.rendering.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;

public class SpectrumStandardSearch
{
	SpectrumSearchParametersModel searchParameters;
    private boolean hasHierarchicalSpectraSearch;
    private TreePath[] selectedPaths;
    protected SpectraHierarchicalSearchSpecification spectraSpec;
//    private SpectraCollection collection;

	public SpectrumStandardSearch(SpectrumSearchParametersModel searchParameters, boolean hasHierarchicalSpectraSearch, SpectraHierarchicalSearchSpecification<?> searchSpec)
	{
		this.searchParameters = searchParameters;
		this.hasHierarchicalSpectraSearch = hasHierarchicalSpectraSearch;
//		this.collection = collection;
		this.spectraSpec = searchSpec;
	}

	public List<BasicSpectrum> search(ISpectralInstrument instrument, TreeSet<Integer> cubeList, TreePath[] selectedPaths)
	{
		List<BasicSpectrum> tempResults = new ArrayList<BasicSpectrum>();
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
            	spectraSpec.loadMetadata();
            	spectraSpec.readHierarchyForInstrument(instrument.getDisplayName());
                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
//                sumOfProductsSearch = true;
//                SpectraCollection collection = (SpectraCollection)getModelManager().getModel(ModelNames.SPECTRA);
                // Process the user's selections
                spectraSpec.processTreeSelections(selectedPaths);

                // Get the selected (camera,filter) pairs

                productsSelected = spectraSpec.getSelectedDatasets();
                InstrumentMetadata<SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
                TreeModel tree = spectraSpec.getTreeModel();
                List<SearchSpec> specs = instrumentMetadata.getSpecs();
                for (Integer selected : productsSelected)
                {
                    String name = tree.getChild(tree.getRoot(), selected).toString();
                    SearchSpec spec = specs.get(selected);
                    FixedListSearchMetadata searchMetadata = FixedListSearchMetadata.of(spec.getDataName(),
                                                                                        spec.getDataListFilename(),
                                                                                        spec.getDataPath(),
                                                                                        spec.getDataRootLocation(),
                                                                                        spec.getSource());

                    List<List<String>> thisResult = instrument.getQueryBase().runQuery(searchMetadata).getResultlist();
                    IBasicSpectrumRenderer spectrumRenderer = null;

                    for (List<String> str : thisResult)
                    {
                    	 try
                         {
                         	spectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(str.get(0), instrument);
                         	spectrumRenderer.getSpectrum().setMetadata(spec);
                         	tempResults.add(spectrumRenderer.getSpectrum());
                         }
                         catch (Exception e) {
                        	 System.out.println("SpectrumStandardSearch: search: " + e.getLocalizedMessage());
//                             e.printStackTrace();
                         }
                    }
//                    collection.tagSpectraWithMetadata(thisResult, spec);
//                    tempResults.addAll(thisResult);
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
                    tempResults = query.runQuery(searchMetadata).getResultlist();
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
