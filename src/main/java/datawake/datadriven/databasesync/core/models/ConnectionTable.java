package datawake.datadriven.databasesync.core.models;

import datawake.datadriven.databasesync.core.models.keys.ConnectionTableKey;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "database_sync_connections_tables")
public class ConnectionTable {
    @EmbeddedId
    private ConnectionTableKey id;

    @Size(max = 5000)
    @Column
    private String customQuery;

    @Column
    private LocalDateTime lastSyncDate;

    @Column
    private Integer timeInterval = 3600;

    @Column
    private Boolean isRunning = false;

    @Column
    private Boolean isActive = false;

    @Column
    private Boolean isCleanRun = false;

    @ManyToOne
    @MapsId("connectionId")
    @JoinColumn(name = "connection_id")
    private Connection connection;

    @ManyToOne
    @MapsId("tableId")
    @JoinColumn(name = "table_id")
    private datawake.datadriven.databasesync.core.models.Table table;
}
