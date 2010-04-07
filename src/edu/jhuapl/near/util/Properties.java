package edu.jhuapl.near.util;

/**
 * This interface lists all properties that get fired in firePropertyChange calls.
 * All such properties should be listed here rather than in the
 * class that does the firing.
 */
public interface Properties 
{
	final String MODEL_CHANGED = "model-changed";
	final String LINEAMENT_MODEL_CHANGED = "lineament-model-changed";
	final String CIRCLE_MODEL_CHANGED = "circle-model-changed";
	final String EROS_MODEL_CHANGED = "eros-model-changed";
	final String BOUNDARIES_CHANGED = "msi-image-boundaries-changed";
	final String MSI_CONTRAST_CHANGED = "msi-contrast-changed";
	final String FINISHED_DRAWING_LINE = "finished-drawing-line";
	final String MODEL_PICKED = "model-picked";
	final String STRUCTURE_ADDED = "structure-added";
}
