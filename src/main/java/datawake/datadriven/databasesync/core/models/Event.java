package datawake.datadriven.databasesync.core.models;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "database_sync_events")
public class Event extends BaseModel {

    @Column
    private String name;

    @Column
    private Boolean success;

    @Column
    private String log;
}
