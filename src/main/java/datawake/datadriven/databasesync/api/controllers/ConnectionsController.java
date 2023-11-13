package datawake.datadriven.databasesync.api.controllers;

import datawake.datadriven.databasesync.api.exceptions.EntityAlreadyExistsException;
import datawake.datadriven.databasesync.core.models.Connection;
import datawake.datadriven.databasesync.core.services.ConnectionsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connections")
public class ConnectionsController {

    private final ConnectionsService connectionsService;

    public ConnectionsController(ConnectionsService connectionsService) {
        this.connectionsService = connectionsService;
    }


    @PostMapping()
    public ResponseEntity<Connection> post(@RequestBody Connection body) {
        try {
            Connection response = connectionsService.create(body);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (EntityAlreadyExistsException e) {
            return ResponseEntity.unprocessableEntity().build();
        }
    }
}
