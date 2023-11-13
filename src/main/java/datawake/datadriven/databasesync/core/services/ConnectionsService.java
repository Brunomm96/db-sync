package datawake.datadriven.databasesync.core.services;

import datawake.datadriven.databasesync.api.exceptions.EntityAlreadyExistsException;
import datawake.datadriven.databasesync.core.models.Connection;
import datawake.datadriven.databasesync.core.daos.repositories.ConnectionsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ConnectionsService extends BaseService<Connection, ConnectionsRepository> {
    private final PasswordEncoder passwordEncoder;

    public ConnectionsService(ConnectionsRepository connectionsRepository, PasswordEncoder passwordEncoder) {
        super(connectionsRepository);
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByName(String name) {
        Optional<Connection> connectionOpt = repository.findByName(name);

        return connectionOpt.isPresent();
    }

    @Override
    public Connection create(Connection connection) {
        Optional<Connection> connectionOpt = repository.findById(connection.getId());

        if (connectionOpt.isPresent())
            throw new EntityAlreadyExistsException(connection);

        String encodedPassword = passwordEncoder.encode(connection.getPassword());
        connection.setPassword(encodedPassword);

        return repository.save(connection);
    }
}
