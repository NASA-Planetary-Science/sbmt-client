package edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkLookupTable;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.modules.rendering.CylindricalImageRenderables;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;


public class SceneCylindricalImageBuilderOperator extends BasePipelineOperator<Pair<List<SmallBodyModel>, List<RenderableCylindricalImage>>, Pair<List<vtkActor>, List<CylindricalImageRenderables>>>
{

	List<SmallBodyModel> smallBodyModels;
	List<RenderableCylindricalImage> renderableImages;

	public SceneCylindricalImageBuilderOperator()
	{
	}

	@Override
	public void processData() throws IOException, Exception
	{
		smallBodyModels = inputs.get(0).getLeft();
		renderableImages = inputs.get(0).getRight();
		processImages();
	}


	private void processImages()
	{
        try
		{
        	outputs.add(Pair.of(Lists.newArrayList(), Lists.newArrayList()));
        	outputs.get(0).getLeft().addAll(generateBodyModelActor(smallBodyModels));
        	List<CylindricalImageRenderables> renderables = Lists.newArrayList();
        	for (RenderableCylindricalImage image : renderableImages)
        		renderables.add(new CylindricalImageRenderables(image, smallBodyModels));
        	outputs.get(0).getRight().addAll(renderables);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<vtkActor> generateBodyModelActor(List<SmallBodyModel> smallBodyModels)
	{
		List<vtkActor> smallBodyActors = Lists.newArrayList();
		smallBodyModels.forEach(smallBodyModel -> {
			vtkPolyDataMapper  smallBodyMapper = new vtkPolyDataMapper();
	        smallBodyMapper.SetInputData(smallBodyModel.getSmallBodyPolyData());
	        vtkLookupTable lookupTable = new vtkLookupTable();
	        smallBodyMapper.SetLookupTable(lookupTable);
	        smallBodyMapper.UseLookupTableScalarRangeOn();

			VtkLodActor smallBodyActor = new VtkLodActor(this);
			smallBodyActor.setDefaultMapper(smallBodyMapper);
			smallBodyActor.setLodMapper(LodMode.MaxQuality, smallBodyMapper);
			smallBodyActors.add(smallBodyActor);
		});

		return smallBodyActors;
	}
}