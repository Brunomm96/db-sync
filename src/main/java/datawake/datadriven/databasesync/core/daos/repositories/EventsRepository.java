package datawake.datadriven.databasesync.core.daos.repositories;

import datawake.datadriven.databasesync.core.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventsRepository extends JpaRepository<Event, UUID> {
}
