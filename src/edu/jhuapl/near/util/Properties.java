package edu.jhuapl.near.util;

/**
 * This interface lists all properties that get fired in firePropertyChange calls.
 * All such properties should be listed here rather than in the
 * class that does the firing.
 */
public interface Properties 
{
	// Property names:
	final String MODEL_CHANGED = "model-changed";
	final String MODEL_PICKED = "model-picked";
	final String STRUCTURE_ADDED = "structure-added";
	final String MSI_IMAGE_BACKPLANE_GENERATION_UPDATE = "msi-image-backplane-generation-update";
	final String MODEL_RESOLUTION_CHANGED = "model-resolution-changed";

	// Property values:
	final String VERTEX_INSERTED_INTO_LINE = "vertex-inserted-into-line";
	final String VERTEX_POSITION_CHANGED = "vertex-position-changed";
	final String LINE_SELECTED = "line-selected";
}
