package datawake.datadriven.databasesync.api.exceptions;

import jakarta.persistence.PersistenceException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityAlreadyExistsException extends PersistenceException {
    private Object entity;

    public EntityAlreadyExistsException(Object entity) {
        super(String.format(
                "Entity that is trying to be created already exists: %s",
                entity
        ));
        this.entity = entity;
    }
}
