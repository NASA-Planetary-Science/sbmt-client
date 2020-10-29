package edu.jhuapl.sbmt.stateHistory.model.liveColoring;

import java.util.HashMap;

import vtk.vtkDataArray;
import vtk.vtkFloatArray;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;

public class LiveColorableManager
{
	private static HashMap<String, ICalculatedPlateValues> plateValueCalculators = new HashMap<String, ICalculatedPlateValues>();
	public static StateHistoryTimeModel timeModel = StateHistoryTimeModel.getInstance();

	public static void addICalculatedPlateValues(String name, ICalculatedPlateValues plateValueCalculator)
	{
		plateValueCalculators.put(name, plateValueCalculator);
	}

	public static ICalculatedPlateValues getCalculatedPlateValuesFor(String name)
	{
		return plateValueCalculators.get(name);
	}

//	public static void setStateHistoryTimeModel(StateHistoryTimeModel timeModel)
//	{
//		LiveColorableManager.timeModel = timeModel;
//	}

	public static void updateFootprint(PerspectiveImageFootprint footprint)
	{
		for (String name : plateValueCalculators.keySet())
		{
			ICalculatedPlateValues plateValueCalculator = plateValueCalculators.get(name);
			System.out.println("LiveColorableManager: updateFootprint: updating " + name);
			if (plateValueCalculator instanceof IFootprintConfinedPlateValues)
			{
				((IFootprintConfinedPlateValues)plateValueCalculator).setFacetColoringDataForFootprint(footprint);
			}
		}
	}


	public static void main(String[] args)
	{
		NativeLibraryLoader.loadAllVtkLibraries();

		vtkFloatArray values = new vtkFloatArray();
		values.SetNumberOfComponents(1);

		values.SetNumberOfTuples(10);

		LiveColorableManager.addICalculatedPlateValues("test", new ITimeCalculatedPlateValues()
		{

			@Override
			public vtkFloatArray getValues()
			{
				return values;
			}

			@Override
			public vtkDataArray getPlateValuesForTime(double time)
			{
				values.InsertTuple1((int)time, 100+time);
				return values;
			}

			@Override
			public void setNumberOfDimensions(int numDimensions)
			{
				values.SetNumberOfComponents(numDimensions);
			}

			@Override
			public void setNumberOfValues(int numValues)
			{
				values.SetNumberOfTuples(numValues);
			}
		});

		ITimeCalculatedPlateValues testCalculator = (ITimeCalculatedPlateValues)LiveColorableManager.getCalculatedPlateValuesFor("test");

		for (int i=0; i<10; i++)
		{
			testCalculator.getPlateValuesForTime(i);
		}

		for (int i=0; i<10; i++)
		{
			System.out.println("LiveColorableManager: main: " + values.GetTuple1(i));
		}
	}
}
