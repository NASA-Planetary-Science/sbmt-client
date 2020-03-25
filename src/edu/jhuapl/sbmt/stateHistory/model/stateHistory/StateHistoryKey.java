package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.util.Random;

/**
 * A StateHistoryKey should be used to uniquely distinguish one trajectory from another.
 * No two trajectories will have the same values for the fields of this class.
 */
public class StateHistoryKey
{
    /**
     *
     */
    public static final Random RAND = new Random();
    /**
     *
     */
    public Integer value;

    /**
     * @param runs
     */
    public StateHistoryKey(StateHistoryCollection runs)
    {
        value = RAND.nextInt(1000);
        while (runs.getKeys().contains(value)) {
            value = RAND.nextInt(1000);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return value.equals(((StateHistoryKey)obj).value);
    }

}