package datawake.datadriven.databasesync.core.daos.repositories;

import datawake.datadriven.databasesync.core.models.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectionsRepository extends JpaRepository<Connection, UUID> {
    Optional<Connection> findByName(String name);
}
