package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;

public interface DisplayableItem
{
	public boolean isVisible();

	public void setVisible(boolean isVisible);

	public String getLabel();

	public void setLabel(String text);

	public Color getColor();

	public void setColor(Color color);

	public double getSize();

	public void setSize(double size);
}
