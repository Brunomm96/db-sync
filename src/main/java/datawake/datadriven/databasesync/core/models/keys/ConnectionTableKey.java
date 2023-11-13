package datawake.datadriven.databasesync.core.models.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;


@Data
@Embeddable
public class ConnectionTableKey implements Serializable {
    @Column(name = "connection_id")
    private UUID connectionId;

    @Column(name = "table_id")
    private UUID tableId;

    public static ConnectionTableKey generate(UUID connectionId, UUID tableId) {
        ConnectionTableKey key = new ConnectionTableKey();
        key.setConnectionId(connectionId);
        key.setTableId(tableId);

        return key;
    }

}
