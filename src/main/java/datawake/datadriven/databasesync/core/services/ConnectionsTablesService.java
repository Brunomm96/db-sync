package datawake.datadriven.databasesync.core.services;

import datawake.datadriven.databasesync.core.models.ConnectionTable;
import datawake.datadriven.databasesync.core.daos.repositories.ConnectionsTablesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConnectionsTablesService {
    private final ConnectionsTablesRepository connectionsTablesRepository;

    public ConnectionsTablesService(ConnectionsTablesRepository connectionsTablesRepository) {
        this.connectionsTablesRepository = connectionsTablesRepository;
    }

    public ConnectionTable save(ConnectionTable model) {
        return connectionsTablesRepository.save(model);
    }
}
