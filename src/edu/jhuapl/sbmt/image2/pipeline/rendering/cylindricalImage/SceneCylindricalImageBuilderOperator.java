package edu.jhuapl.sbmt.image2.pipeline.rendering.cylindricalImage;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkLookupTable;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.model.CylindricalImageRenderables;
import edu.jhuapl.sbmt.image2.model.RenderableCylindricalImage;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;


public class SceneCylindricalImageBuilderOperator extends BasePipelineOperator<Pair<SmallBodyModel, RenderableCylindricalImage>, Pair<List<vtkActor>, List<CylindricalImageRenderables>>>
{

	List<SmallBodyModel> smallBodyModels;
	List<RenderableCylindricalImage> renderableImages;

	public SceneCylindricalImageBuilderOperator()
	{
	}

	@Override
	public void processData() throws IOException, Exception
	{
		smallBodyModels = inputs.stream().map( item -> item.getLeft()).toList();
		renderableImages = inputs.stream().map( item -> item.getRight()).toList();
//		smallBodyModels = inputs.get(0).getLeft();
//		renderableImages = inputs.get(0).getRight();
		processImages();
	}


	private void processImages()
	{
        try
		{

        	List<CylindricalImageRenderables> renderables = Lists.newArrayList();
        	List<vtkActor> smallBodyActors = generateBodyModelActor(smallBodyModels);
        	for (RenderableCylindricalImage image : renderableImages)
        	{
        		CylindricalImageRenderables cylImgRenderable = new CylindricalImageRenderables(image, smallBodyModels);
        		renderables.add(cylImgRenderable);
//        		System.out.println("SceneCylindricalImageBuilderOperator: processImages: adding pair with image " + cylImgRenderable);
//        		System.out.println("SceneCylindricalImageBuilderOperator: processImages: adding " + Pair.of(smallBodyActors, List.of(cylImgRenderable)));
        		outputs.add(Pair.of(smallBodyActors, List.of(cylImgRenderable)));
        	}
//        	System.out.println("SceneCylindricalImageBuilderOperator: processImages: number of outputs " + outputs.size());
//        	System.out.println("SceneCylindricalImageBuilderOperator: processImages: output 15 " + outputs.get(15));
//        	outputs.add(Pair.of(Lists.newArrayList(), Lists.newArrayList()));
//        	outputs.get(0).getLeft().addAll(generateBodyModelActor(smallBodyModels));
//        	outputs.get(0).getRight().addAll(renderables);
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