package edu.jhuapl.sbmt.dtm.service.events;

import edu.jhuapl.saavtk2.event.EventSource;
import edu.jhuapl.sbmt.dtm.model.DEMKey;

public class HideDEMBoundaryEvent extends DEMEvent<Void>
{

    public HideDEMBoundaryEvent(EventSource source, DEMKey key)
    {
        super(source, key, null);
    }

}
