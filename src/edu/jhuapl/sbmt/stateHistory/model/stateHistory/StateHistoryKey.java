package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import glum.item.IdGenerator;
import glum.item.IncrIdGenerator;

/**
 * A StateHistoryKey should be used to uniquely distinguish one trajectory from another.
 * No two trajectories will have the same values for the fields of this class.
 */
public class StateHistoryKey
{
    /**
     *
     */
//    private static final Random RAND = new Random();
    private static IdGenerator RAND = new IncrIdGenerator(0);

    /**
     *
     */
    private Integer value;

    //Metadata Information
    private static final Key<StateHistoryKey> STATE_HISTORY_KEY = Key.of("StateHistoryKey");

	private static final Key<Integer> VALUE_KEY = Key.of("value");

    /**
     * @param runs
     */
    public StateHistoryKey(StateHistoryCollection runs)
    {
//        value = RAND.nextInt(1000);
    	RAND = new IncrIdGenerator(runs.getSimRuns().get(runs.getSimRuns().size()-1).getMetadata().getKey().getValue() + 1);
    	value = RAND.getNextId();
//        while (runs.getKeys().contains(value)) {
//            value = RAND.nextInt(1000);
//        }
    }

    public StateHistoryKey(Integer value)
    {
    	this.value = value;
    }

    @Override
    public boolean equals(Object obj)
    {
        return value.equals(((StateHistoryKey)obj).value);
    }

    public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(STATE_HISTORY_KEY, (source) -> {

    		Integer value = source.get(VALUE_KEY);
    		StateHistoryKey stateHistory = new StateHistoryKey(value);
    		return stateHistory;

    	}, StateHistoryKey.class, stateHistoryKey -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(VALUE_KEY, stateHistoryKey.value);
    		return result;
    	});
	}

	/**
	 * @return the value
	 */
	public Integer getValue()
	{
		return value;
	}
}