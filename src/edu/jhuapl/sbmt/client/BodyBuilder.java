package edu.jhuapl.sbmt.client;

@FunctionalInterface
public interface BodyBuilder<BodyViewConfig> 
{
	SmallBodyModel buildBody(BodyViewConfig config);
}
