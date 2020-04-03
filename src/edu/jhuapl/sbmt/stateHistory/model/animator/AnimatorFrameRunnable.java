package edu.jhuapl.sbmt.stateHistory.model.animator;

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