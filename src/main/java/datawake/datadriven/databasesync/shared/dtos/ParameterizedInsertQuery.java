package datawake.datadriven.databasesync.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterizedInsertQuery {
    private String query;
    private List<Object> parameters;
}
