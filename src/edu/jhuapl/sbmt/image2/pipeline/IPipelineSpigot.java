package edu.jhuapl.sbmt.image2.pipeline;

import java.util.List;

@FunctionalInterface
public interface IPipelineSpigot<O>
{
	/**
	 * @param data
	 * @return
	 */
	List<O> flowData();
}