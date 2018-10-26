package edu.jhuapl.sbmt.gui.dtm.ui.creation;

import edu.jhuapl.saavtk2.event.BasicEvent;
import edu.jhuapl.saavtk2.event.EventSource;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DEMCreatedEvent extends BasicEvent<DEMKey>
{

    public DEMCreatedEvent(EventSource source, DEMKey key)
    {
        super(source, key);
    }

}
