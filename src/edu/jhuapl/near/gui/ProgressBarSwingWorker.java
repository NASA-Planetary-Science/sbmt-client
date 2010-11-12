package edu.jhuapl.near.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

abstract public class ProgressBarSwingWorker extends SwingWorker<Void, Void>
		implements PropertyChangeListener
{
	private JProgressBar progressBar;
	private JButton cancelButton;
	private JLabel label;
	private ProgressDialog dialog;
	private volatile boolean indeterminate = false;
	private volatile String labelText = " ";
	
	private class ProgressDialog extends JDialog implements ActionListener
	{

		public ProgressDialog(Component c)
		{
			super(JOptionPane.getFrameForComponent(c));
			JPanel panel = new JPanel(new MigLayout());
			setPreferredSize(new Dimension(275, 150));
			
			label = new JLabel(" ");
			
			progressBar = new JProgressBar(0, 100);
			progressBar.setPreferredSize(new Dimension(250, 20));
			panel.add(label, "wrap");
			panel.add(progressBar, "wrap");
			
			cancelButton = new JButton("Cancel");
	        cancelButton.addActionListener(this);

	        //setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
	        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	        panel.add(cancelButton, "align center");

	        setLocationRelativeTo(c);

	        add(panel);
	        pack();
		}

		public void actionPerformed(ActionEvent e)
		{
    		cancel(true);
			
	        dialog.setVisible(false);
			dialog.dispose();
		}
	}
	
	public ProgressBarSwingWorker(Component c, String title)
	{
		dialog = new ProgressDialog(c);
		dialog.setTitle(title);
		
        addPropertyChangeListener(this);
	}
	
	public void showDialog()
	{
    	label.setText(labelText);

    	dialog.setVisible(true);
	}
	
	protected void setIndeterminate(boolean indeterminate)
	{
		this.indeterminate = indeterminate;
	}

	protected void setLabelText(String labelText)
	{
		this.labelText = labelText;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
        if ("progress".equals(evt.getPropertyName()))
        {
        	int progress = (Integer) evt.getNewValue();
        	System.out.println("progress property change " + progress);
        	
        	if (indeterminate)
        		progressBar.setIndeterminate(true);
        	else
        		progressBar.setValue(progress);
        	
        	label.setText(labelText);
        }
        
        if (evt.getNewValue().equals(SwingWorker.StateValue.DONE))
		{
			System.out.println("Compeled task!");
	        dialog.setVisible(false);
			dialog.dispose();
		}
    }

}
