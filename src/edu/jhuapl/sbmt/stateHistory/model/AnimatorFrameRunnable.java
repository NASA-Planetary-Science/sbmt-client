package edu.jhuapl.sbmt.stateHistory.model;

import edu.jhuapl.sbmt.stateHistory.model.animator.AnimationFrame;

public abstract class AnimatorFrameRunnable implements Runnable
{
	/**
	 *
	 */
	AnimationFrame frame;

	/**
	 *
	 */
	public AnimatorFrameRunnable()
	{

	}

	/**
	 * @param frame
	 */
	public void run(AnimationFrame frame)
	{
		this.frame = frame;
	}

	/**
	 * @return
	 */
	public AnimationFrame getFrame()
	{
		return frame;
	}
}