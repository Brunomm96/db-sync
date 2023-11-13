package datawake.datadriven.databasesync.core.models;

import datawake.datadriven.databasesync.shared.enums.DriverClassNameEnum;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "database_sync_connections")
public class Connection extends BaseModel {
    @Enumerated(EnumType.STRING)
    @Column
    private DriverClassNameEnum driverClassname;

    @Column
    private String name;

    @Column
    private String url;

    @Column
    private String username;

    @Column
    private String password;


//    RETIRAR EAGER LOADING NO FUTURO
    @Cascade({
            org.hibernate.annotations.CascadeType.SAVE_UPDATE,
            org.hibernate.annotations.CascadeType.MERGE,
            org.hibernate.annotations.CascadeType.PERSIST
    })
    @OneToMany(mappedBy = "connection", fetch = FetchType.EAGER)
    private Set<ConnectionTable> connectionsTables = new HashSet<>();
}
