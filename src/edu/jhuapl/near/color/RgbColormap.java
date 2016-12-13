package edu.jhuapl.near.color;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.google.common.collect.Lists;

import vtk.vtkColorTransferFunction;
import vtk.vtkLookupTable;

import edu.jhuapl.near.util.MathUtil;

public class RgbColormap extends ListenableColormap
{

	vtkLookupTable lut=new vtkLookupTable();
	vtkColorTransferFunction ctf;
	double dataMin,dataMax;
	double logDataMin,logDataMax;

	List<Double> interpLevels;
	List<Color> colors;
	int nLevels;
	Color nanColor;
	ColorSpace colorSpace;
	String name="";
	boolean isLog;

	List<PropertyChangeListener> listeners=Lists.newArrayList();

	public enum ColorSpace
	{
		RGB,HSV,LAB,DIVERGING;
	}

	public static final int defaultNumberOfLevels=32;


	public RgbColormap(List<Double> interpLevels, List<Color> colors, int nLevels)	// the key in this Map must lie between 0 and 1
	{
		this(interpLevels, colors, nLevels, Color.white);
	}

	public RgbColormap(List<Double> interpLevels, List<Color> colors, int nLevels, Color nanColor)
	{
		this(interpLevels, colors, nLevels, nanColor, ColorSpace.RGB);
	}

	public RgbColormap(List<Double> interpLevels, List<Color> colors, int nLevels, Color nanColor, ColorSpace colorSpace)
	{
		this.interpLevels=interpLevels;
		this.colors=colors;
		this.nLevels=nLevels;
		this.nanColor=nanColor;
		this.colorSpace=colorSpace;
		isLog=false;
	}

	private void rebuildLookupTable()
	{
		if (dataMax<=dataMin)
			return;

		ctf=new vtkColorTransferFunction();
		switch (colorSpace)
		{
		case RGB:
			ctf.SetColorSpaceToRGB();
			break;
		case HSV:
			ctf.SetColorSpaceToHSV();
			break;
		case LAB:
			ctf.SetColorSpaceToLab();
			break;
		case DIVERGING:
			ctf.SetColorSpaceToDiverging();
			break;
		default:
			ctf.SetColorSpaceToRGB();
		}
		double rangeMin=Float.POSITIVE_INFINITY;
		double rangeMax=Float.NEGATIVE_INFINITY;
		for (int i=0; i<interpLevels.size(); i++)
		{
			if (interpLevels.get(i)<rangeMin)
				rangeMin=interpLevels.get(i);
			if (interpLevels.get(i)>rangeMax)
				rangeMax=interpLevels.get(i);
		}
		for (int i=0; i<interpLevels.size(); i++)
		{
			Color c=colors.get(i);
			float[] comp=c.getRGBComponents(null);
			ctf.AddRGBPoint((interpLevels.get(i)-rangeMin)/(rangeMax-rangeMin), comp[0], comp[1], comp[2]);
		}

		float[] comp=nanColor.getRGBColorComponents(null);
		lut.SetNanColor(comp[0],comp[1],comp[2],1);

		nLevels=defaultNumberOfLevels;
		lut.SetNumberOfTableValues(nLevels);
		lut.ForceBuild();
		if (isLog)
		{
            lut.SetTableRange(logDataMin, logDataMax);
            lut.SetValueRange(logDataMin, logDataMax);
            lut.SetRange(logDataMin, logDataMax);
		}
		else
		{
		    lut.SetTableRange(dataMin, dataMax);
		    lut.SetValueRange(dataMin, dataMax);
		    lut.SetRange(dataMin, dataMax);
		}
		for (int i=0; i<getNumberOfLevels(); i++)
		{
			double val=(double)i/(double)getNumberOfLevels();//*(dataMax-dataMin)+dataMin;
			lut.SetTableValue(i, ctf.GetColor(val));
		}
	}

	@Override
	public void setNumberOfLevels(int n)
	{
		nLevels=n;
		rebuildLookupTable();
		firePropertyChangeEvent();
	}

	@Override
	public int getNumberOfLevels()
	{
		return nLevels;
	}

	@Override
	public vtkLookupTable getLookupTable()
	{
		return lut;
	}

	@Override
	public Color getColor(double val)
	{
		if (Double.isNaN(val) || !Double.isFinite(val))
			return getNanColor();
		else
		{
		    if (isLog)
		        val=MathUtil.log10clamped(val);
			double[] c=lut.GetColor(val);
			return new Color((float)c[0],(float)c[1],(float)c[2]);
		}
	}

	@Override
	public Color getNanColor()
	{
		double[] nanColor=lut.GetNanColor();
		return new Color((float)nanColor[0],(float)nanColor[1],(float)nanColor[2]);
	}

	@Override
	public double getRangeMin()
	{
		return dataMin;
	}

	@Override
	public double getRangeMax()
	{
		return dataMax;
	}

	@Override
	public void setRangeMin(double val)
	{
		this.dataMin=val;
		this.logDataMin=MathUtil.log10clamped(val);
		rebuildLookupTable();
		firePropertyChangeEvent();
	}

	@Override
	public void setRangeMax(double val)
	{
		this.dataMax=val;
        this.logDataMax=MathUtil.log10clamped(val);
		rebuildLookupTable();
		firePropertyChangeEvent();
	}

	@Override
	public void setLog(boolean flag)
	{
	    isLog=flag;
		rebuildLookupTable();
		firePropertyChangeEvent();
	}

	@Override
	public boolean getLog()
	{
		return isLog;
	}

	public static RgbColormap copy(RgbColormap cmap)
	{
		RgbColormap newCmap=new RgbColormap(cmap.interpLevels,cmap.colors,cmap.nLevels,cmap.nanColor,cmap.colorSpace);
		newCmap.setLog(cmap.getLog());
		newCmap.setRangeMin(cmap.getRangeMin());
		newCmap.setRangeMax(cmap.getRangeMax());
		newCmap.setName(cmap.getName());
		return newCmap;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}

}
