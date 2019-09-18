package edu.jhuapl.sbmt.spectrum.model.hypertree;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton.Node;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException;
import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.BoundedObjectHyperTreeNode;
import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.BoundedObjectHyperTreeSkeleton;
import edu.jhuapl.sbmt.model.boundedobject.hyperoctree.HyperBoundedObject;
import edu.jhuapl.sbmt.spectrum.controllers.standard.SearchProgressListener;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.InstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchParametersModel;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class SpectrumHypertreeSearch
{
	SpectrumSearchParametersModel searchParameters;
	private SpectraSearchDataCollection spectraModel;
    private String spectraHypertreeDataSpecName;
    private boolean hasHypertreeBasedSpectraSearch;
    SpectraHierarchicalSearchSpecification spectraSpec = null;


	public SpectrumHypertreeSearch(SpectrumSearchParametersModel searchParameters,
									String spectraHypertreeDataSpecName, boolean hasHypertreeBasedSpectraSearch,
									SpectraHierarchicalSearchSpecification<?> searchSpec)
	{
		this.searchParameters = searchParameters;
		this.spectraHypertreeDataSpecName = spectraHypertreeDataSpecName;
		this.hasHypertreeBasedSpectraSearch = hasHypertreeBasedSpectraSearch;
		this.spectraSpec = searchSpec;
	}

	public List<BasicSpectrum> search(TreeSet<Integer> cubeList, BoundedObjectHyperTreeSkeleton skeleton, HyperBox hbb, BasicSpectrumInstrument instrument, SearchProgressListener progressListener)
	{
		List<Integer> productsSelected;
        List<BasicSpectrum> results = new ArrayList<BasicSpectrum>();
        Map<String, Double> fileDateMap = new HashMap<String, Double>();

        try
        {
            GregorianCalendar startDateGreg = new GregorianCalendar();
            GregorianCalendar endDateGreg = new GregorianCalendar();
            startDateGreg.setTime(searchParameters.getStartDate());
            endDateGreg.setTime(searchParameters.getEndDate());
            double startTime = searchParameters.getStartDate().getTime();
            double endTime = searchParameters.getEndDate().getTime();

            DateTime startDateJoda = new DateTime(
                    startDateGreg.get(GregorianCalendar.YEAR),
                    startDateGreg.get(GregorianCalendar.MONTH) + 1,
                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    startDateGreg.get(GregorianCalendar.MINUTE),
                    startDateGreg.get(GregorianCalendar.SECOND),
                    startDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);
            DateTime endDateJoda = new DateTime(
                    endDateGreg.get(GregorianCalendar.YEAR),
                    endDateGreg.get(GregorianCalendar.MONTH) + 1,
                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    endDateGreg.get(GregorianCalendar.MINUTE),
                    endDateGreg.get(GregorianCalendar.SECOND),
                    endDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);

            //Put this higher up in the call stack so the search method doesn't need to know about the body model
//            String spectraDatasourceName = spectraHypertreeSourceName;
//
//            this.spectraModel = (SpectraSearchDataCollection) modelManager
//                    .getModel(ModelNames.SPECTRA_HYPERTREE_SEARCH);
//            String spectraDatasourcePath = spectraModel
//                    .getSpectraDataSourceMap().get(spectraDatasourceName);
//
//            spectraModel.addDatasourceSkeleton(spectraDatasourceName,
//                    spectraDatasourcePath);
//            spectraModel
//                    .setCurrentDatasourceSkeleton(spectraDatasourceName);
//            spectraModel.readSkeleton();
//            BoundedObjectHyperTreeSkeleton skeleton = (BoundedObjectHyperTreeSkeleton) spectraModel
//                    .getCurrentSkeleton();
//
//            double[] selectionRegionCenter = null;
//            double selectionRegionRadius = 0.0;
//
//            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) modelManager
//                    .getModel(ModelNames.CIRCLE_SELECTION);
//            SmallBodyModel smallBodyModel = (SmallBodyModel) modelManager
//                    .getModel(ModelNames.SMALL_BODY);
//            EllipsePolygon region = null;
//            vtkPolyData interiorPoly = new vtkPolyData();
//            if (selectionModel.getNumberOfStructures() > 0)
//            {
//                region = (EllipsePolygon) selectionModel
//                        .getStructure(0);
//                selectionRegionCenter = region.getCenter();
//                selectionRegionRadius = region.radius;
//
//                // Always use the lowest resolution model for getting the
//                // intersection cubes list.
//                // Therefore, if the selection region was created using a
//                // higher resolution model,
//                // we need to recompute the selection region using the low
//                // res model.
//                if (smallBodyModel.getModelResolution() > 0)
//                    smallBodyModel.drawRegularPolygonLowRes(selectionRegionCenter,
//                            region.radius, region.numberOfSides,
//                            interiorPoly, null); // this sets interiorPoly
//                else
//                    interiorPoly = region.interiorPolyData;
//
//            }
//            else
//            {
//                vtkCubeSource box = new vtkCubeSource();
//                double[] bboxBounds = smallBodyModel.getBoundingBox()
//                        .getBounds();
//                BoundingBox bbox = new BoundingBox(bboxBounds);
//                bbox.increaseSize(0.01);
//                box.SetBounds(bbox.getBounds());
//                box.Update();
//                interiorPoly.DeepCopy(box.GetOutput());
//            }
//
//            Set<String> files = new HashSet<String>();
//            HashMap<String, HyperBoundedObject> fileSpecMap = new HashMap<String, HyperBoundedObject>();
//            double[] times = new double[] { startTime, endTime };
//            double[] spectraLims = new double[] { searchParameters.getMinEmissionQuery(), searchParameters.getMaxEmissionQuery(),
//            		searchParameters.getMinIncidenceQuery(), searchParameters.getMaxIncidenceQuery(), searchParameters.getMinPhaseQuery(), searchParameters.getMaxPhaseQuery(),
//            		searchParameters.getMinDistanceQuery(), searchParameters.getMaxDistanceQuery() };
//            double[] bounds = interiorPoly.GetBounds();
//            TreeSet<Integer> cubeList = ((SpectraSearchDataCollection) spectraModel)
//                    .getLeavesIntersectingBoundingBox(
//                            new BoundingBox(bounds), times, spectraLims);
//            HyperBox hbb = new HyperBox(
//                    new double[] { bounds[0], bounds[2], bounds[4],
//                            times[0], spectraLims[0], spectraLims[2],
//                            spectraLims[4], spectraLims[6] },
//                    new double[] { bounds[1], bounds[3], bounds[5],
//                            times[1], spectraLims[1], spectraLims[3],
//                            spectraLims[5], spectraLims[7] });


            Set<String> files = new HashSet<String>();
            HashMap<String, HyperBoundedObject> fileSpecMap = new HashMap<String, HyperBoundedObject>();
            for (Integer cubeid : cubeList)
            {
//                    System.out.println("cubeId: " + cubeid);
                Node currNode = skeleton.getNodeById(cubeid);
                Path path = currNode.getPath();
                Path dataPath = path.resolve("data");
                DataInputStream instream = new DataInputStream(
                        new BufferedInputStream(new FileInputStream(
                                FileCache.getFileFromServer(
                                        dataPath.toString()))));
                try
                {
                    while (instream.available() > 0)
                    {
                        HyperBoundedObject spectra = BoundedObjectHyperTreeNode
                                .createNewBoundedObject(instream, 8);
                        int fileNum = spectra.getFileNum();
                        double date_et = spectra.getDate();

                        Map<Integer, String> fileMap = skeleton
                                .getFileMap();
                        String file = fileMap.get(fileNum);
                        if (files.add(file))
                        {
                            fileSpecMap.put(file, spectra);
                            fileDateMap.put(file, date_et);
                        }
                    }
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }


            ArrayList<String> finalFiles = new ArrayList<String>();
            ArrayList<HyperBoundedObject> finalSpectra = new ArrayList<HyperBoundedObject>();

            // NOW CHECK WHICH SPECTRA ACTUALLY INTERSECT REGION
            for (String fi : files)
            {
                HyperBoundedObject spec = fileSpecMap.get(fi);
                HyperBox bbox = spec.getBbox();
                try
                {
                    if (hbb.intersects(bbox))
                    {
                        finalFiles.add(fi);
                        finalSpectra.add(spec);
                    }
                }
                catch (HyperException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // final list of spectra that intersect region
            // create a list of lists for the results
            List<BasicSpectrum> listoflist = new ArrayList<BasicSpectrum>(
                    finalFiles.size());

            finalFiles.sort(new Comparator<String>()
            {

                @Override
                public int compare(String o1, String o2)
                {
                    return o1.compareTo(o2);
                }
            });
            for (String file : finalFiles)
            {
            	IBasicSpectrumRenderer spectrumRenderer = null;
                try
                {
                	spectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(file, instrument);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                results.add(spectrumRenderer.getSpectrum());
//                ArrayList<String> currList = new ArrayList<String>();
//                currList.add(file);
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                String date = sdf.format(fileDateMap.get(file));
//                Long dateLong = sdf.parse(date).getTime();
//                currList.add(""+dateLong);
//                listoflist.add(currList);
            }
            results = listoflist;

             InstrumentMetadata<SearchSpec> instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
             List<SearchSpec> specs = instrumentMetadata.getSpecs();
             for (SearchSpec spec : specs)
             {
                 if (spec.getDataName().contains(spectraHypertreeDataSpecName))
                 {
                	 for (BasicSpectrum spectrum : results) spectrum.setMetadata(spec);
//                     collection.tagSpectraWithMetadata(results, spec);
                 }
             }
//             setSpectrumRawResults(results);
             return results;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return null;
        }
	}
}
