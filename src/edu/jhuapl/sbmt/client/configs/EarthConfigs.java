package edu.jhuapl.sbmt.client.configs;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.core.body.BodyType;
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;

/**
 * Configurations for the Earth-Moon system.
 *
 * @author James.Peachey@jhuapl.edu
 *
 */
public class EarthConfigs {

	/**
	 * Add all known configs to the specified {@link ConfigArrayList}.
	 *
	 * @param configArray list of configs to which to add the configs if they
	 * are are not already present
	 */
	public static void initialize(ConfigArrayList<IBodyViewConfig> configArray) {
		buildReinerGammaNacDtmConfig(configArray);
	}

	/**
	 * Configure the single Reiner Gamma DTM model delivered 2024-04-29. Number
	 * of facets determined by inspection of OBJ file. See GitLab issue
	 * sbmt/missions/sbmt-lunarvertex#2.
	 *
	 * @param configArray list of configs to which to add the configs if they
	 * are are not already present
	 */
	private static void buildReinerGammaNacDtmConfig(ConfigArrayList<IBodyViewConfig> configArray) {
		SmallBodyViewConfigBuilder builder = new SmallBodyViewConfigBuilder();

		String authorString = "lunar-vertex-reiner-gamma-nac-dtm";

		builder.body(ShapeModelBody.provide("Moon"), BodyType.PLANETS_AND_SATELLITES, ShapeModelPopulation.EARTH);
		builder.model(ShapeModelType.provide(authorString), ShapeModelDataUsed.IMAGE_BASED, "Reiner Gamma NAC DTM");

		builder.modelRes(ImmutableList.of(1098188));

		builder.modelTopDir("/moon/" + authorString);

		builder.clients(SmallBodyViewConfigBuilder.ProprietaryMissions);

		configArray.add(builder.build());
	}

	/**
	 * Prevent instantiation.
	 */
	private EarthConfigs() {
		throw new AssertionError("All static class");
	}

}
