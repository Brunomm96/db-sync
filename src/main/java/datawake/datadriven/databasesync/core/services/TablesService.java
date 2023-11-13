package datawake.datadriven.databasesync.core.services;

import datawake.datadriven.databasesync.core.models.Table;
import datawake.datadriven.databasesync.core.daos.repositories.TablesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TablesService extends BaseService<Table, TablesRepository> {
    protected TablesService(TablesRepository repository) {
        super(repository);
    }
}
