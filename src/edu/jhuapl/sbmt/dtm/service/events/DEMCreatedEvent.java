package edu.jhuapl.sbmt.dtm.service.events;

import edu.jhuapl.saavtk2.event.BasicEvent;
import edu.jhuapl.saavtk2.event.EventSource;
import edu.jhuapl.sbmt.dtm.model.DEMKey;

public class DEMCreatedEvent extends BasicEvent<DEMKey>
{

    public DEMCreatedEvent(EventSource source, DEMKey key)
    {
        super(source, key);
    }

}
