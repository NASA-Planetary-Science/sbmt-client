package edu.jhuapl.sbmt.stateHistory.model;

import edu.jhuapl.sbmt.stateHistory.model.animator.AnimationFrame;

public abstract class AnimatorFrameRunnable implements Runnable
{
	AnimationFrame frame;

	public AnimatorFrameRunnable()
	{

	}

	public void run(AnimationFrame frame)
	{
		this.frame = frame;
	}

	public AnimationFrame getFrame()
	{
		return frame;
	}
}