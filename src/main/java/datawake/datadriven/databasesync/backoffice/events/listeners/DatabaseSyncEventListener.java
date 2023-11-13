package datawake.datadriven.databasesync.backoffice.events.listeners;

import datawake.datadriven.databasesync.core.events.DatabaseSyncEvent;
import datawake.datadriven.databasesync.core.models.Event;
import datawake.datadriven.databasesync.core.daos.repositories.EventsRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSyncEventListener {
    private final EventsRepository eventsRepository;

    public DatabaseSyncEventListener(EventsRepository eventsRepository) {
        this.eventsRepository = eventsRepository;
    }

    @Async
    @EventListener
    public void onDatabaseSyncEvent(DatabaseSyncEvent event) {
        Event eventObject = event.getEvent();
        eventsRepository.save(eventObject);
    }
}
