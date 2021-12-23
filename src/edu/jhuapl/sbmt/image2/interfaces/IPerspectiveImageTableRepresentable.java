package edu.jhuapl.sbmt.image2.interfaces;

public interface IPerspectiveImageTableRepresentable
{
	public boolean isMapped();

	public void setMapped(boolean mapped);

	public boolean isFrustumShowing();
	public void setFrustumShowing(boolean frustumShowing);

	public boolean isOfflimbShowing();

	public void setOfflimbShowing(boolean offlimbShowing);

	public boolean isBoundaryShowing();

	public void setBoundaryShowing(boolean boundaryShowing);

	public String getStatus();

	public void setStatus(String status);

	public int getIndex();

	public void setIndex(int index);

	public String getFilename();
}
