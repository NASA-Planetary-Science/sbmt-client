package edu.jhuapl.near.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.jhuapl.near.gui.Renderer;

public class SaveImageAction extends AbstractAction
{
	private Renderer renderer;

	public SaveImageAction(Renderer renderer)
    {
        super("Export to Image...");
        this.renderer = renderer;
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        renderer.saveToFile();
    }
}
