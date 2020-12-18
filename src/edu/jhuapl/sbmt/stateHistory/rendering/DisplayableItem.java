package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;

public interface DisplayableItem
{
	/**
	 * @return
	 */
	public boolean isVisible();

	/**
	 * @param isVisible
	 */
	public void setVisible(boolean isVisible);

	/**
	 * @return
	 */
	public boolean isLabelVisible();

	/**
	 * @param isVisible
	 */
	public void setLabelVisible(boolean isVisible);

	/**
	 * @return
	 */
	public String getLabel();

	/**
	 * @param text
	 */
	public void setLabel(String text);

	/**
	 * @return
	 */
	public Color getColor();

	/**
	 * @param color
	 */
	public void setColor(Color color);

	/**
	 * @return
	 */
	public double getPointerRadius();

	/**
	 * @param radius
	 */
	public void setPointerRadius(double radius);
}
