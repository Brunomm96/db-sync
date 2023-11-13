package datawake.datadriven.databasesync.core.events.publishers;

import datawake.datadriven.databasesync.core.events.DatabaseSyncEvent;
import datawake.datadriven.databasesync.core.models.Event;
import datawake.datadriven.databasesync.core.daos.repositories.EventsRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSyncEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    private final EventsRepository eventsRepository;
    public DatabaseSyncEventPublisher(ApplicationEventPublisher applicationEventPublisher, EventsRepository eventsRepository) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.eventsRepository = eventsRepository;
    }

    public void publish(String log, boolean success) {
        Event event = createEvent(success);
        event.setLog(log);

        applicationEventPublisher.publishEvent(new DatabaseSyncEvent(this, event));
    }

    private Event createEvent(boolean success) {
        Event event = new Event();
        event.setName(DatabaseSyncEvent.class.getSimpleName());
        event.setSuccess(success);
        return eventsRepository.save(event);
    }
}
