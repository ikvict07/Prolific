package org.nevertouchgrass.prolific.service;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
@Data
public class DatabaseService {
    private final DataSource dataSource;

    @SneakyThrows
    public DatabaseService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    @SneakyThrows
    private void initializeDb() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createProjectsTable());
        }
    }


    private String createProjectsTable() {
        return "CREATE TABLE IF NOT EXISTS projects ("
                + "title TEXT NOT NULL, "
                + "type TEXT NOT NULL, "
                + "path TEXT NOT NULL, "
                + "is_manually_added BOOLEAN DEFAULT 0"
                + ");";
    }
}
