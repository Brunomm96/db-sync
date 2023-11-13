package datawake.datadriven.databasesync.core.services;

import datawake.datadriven.databasesync.api.exceptions.EntityAlreadyExistsException;
import datawake.datadriven.databasesync.core.models.BaseModel;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public abstract class BaseService<Model extends BaseModel, Repository extends JpaRepository<Model, UUID>> {

    protected final Repository repository;

    protected BaseService(Repository repository) {
        this.repository = repository;
    }

    public Model get(UUID id){
        Optional<Model> optional = repository.findById(id);

        if (optional.isEmpty())
            throw new EntityNotFoundException();

        return optional.get();
    }

    public Page<Model> getAll(Pageable pageable){
        return repository.findAll(pageable);
    }

    public Model create(Model model) {
        Optional<Model> optional = repository.findById(model.getId());

        if (optional.isPresent())
            throw new EntityAlreadyExistsException(model.getId());

        return repository.save(model);
    }

    public Model save(Model model) {
        return repository.save(model);
    }
}
