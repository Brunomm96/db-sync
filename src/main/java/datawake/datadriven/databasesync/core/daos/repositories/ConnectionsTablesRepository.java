package datawake.datadriven.databasesync.core.daos.repositories;

import datawake.datadriven.databasesync.core.models.ConnectionTable;
import datawake.datadriven.databasesync.core.models.keys.ConnectionTableKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionsTablesRepository extends JpaRepository<ConnectionTable, ConnectionTableKey> {
}
