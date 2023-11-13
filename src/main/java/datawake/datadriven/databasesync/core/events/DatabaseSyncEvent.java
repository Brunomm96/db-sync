package datawake.datadriven.databasesync.core.events;

import datawake.datadriven.databasesync.core.models.Event;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class DatabaseSyncEvent extends ApplicationEvent {
    private Event event;
    public DatabaseSyncEvent(Object source, Event event) {
        super(source);
        this.event = event;
    }
}
