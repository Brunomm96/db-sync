package datawake.datadriven.databasesync.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@jakarta.persistence.Table(name = "database_sync_tables")
public class Table extends BaseModel {
    @Column(nullable = false, unique = true)
    private String label;

    @Column
    private String name;

    @Column
    private String primaryKeys;

    @Column
    private String syncField;

    @OneToMany(mappedBy = "table")
    private Set<ConnectionTable> connectionsTables = new HashSet<>();
}
