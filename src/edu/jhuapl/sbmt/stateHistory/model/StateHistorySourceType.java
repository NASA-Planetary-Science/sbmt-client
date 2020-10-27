/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.model;

import lombok.Getter;

/**
 * @author steelrj1
 *
 */
public enum StateHistorySourceType
{
	PREGEN("Pregenerated Pointing"),
	SPICE("SPICE Pointing");

	@Getter
	private String name;

	private StateHistorySourceType(String name)
	{
		this.name = name;
	}
}
