package datawake.datadriven.databasesync.core.daos.repositories;

import datawake.datadriven.databasesync.core.models.Table;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TablesRepository extends JpaRepository<Table, UUID> {
}
