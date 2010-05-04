package edu.jhuapl.near.util;

/**
 * This interface lists all properties that get fired in firePropertyChange calls.
 * All such properties should be listed here rather than in the
 * class that does the firing.
 */
public interface Properties 
{
	final String MODEL_CHANGED = "model-changed";
	final String MODEL_PICKED = "model-picked";
	final String STRUCTURE_ADDED = "structure-added";
	final String MSI_IMAGE_BACKPLANE_GENERATION_UPDATE = "msi-image-backplane-generation-update";
}
