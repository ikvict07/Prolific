package org.nevertouchgrass.prolific.repository;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
@Log4j2
public class ProjectsRepository {

    private final DataSource dataSource;

    public ProjectsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    public List<Project> findAll() {
        List<Project> results = new ArrayList<>();
        String query = "SELECT title, type, path, is_manually_added FROM projects";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String type = resultSet.getString("type");
                String path = resultSet.getString("path");
                boolean isManuallyAdded = resultSet.getBoolean("is_manually_added");
                results.add(new Project(title, type, path, isManuallyAdded));
            }
        }

        log.info("Found {} projects", results.size());
        return results;
    }

    @SneakyThrows
    public void save(Project param) {
        String query = "INSERT INTO projects (title, type, path, is_manually_added) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, param.getTitle());
            preparedStatement.setString(2, param.getType());
            preparedStatement.setString(3, param.getPath());
            preparedStatement.setBoolean(4, param.getIsManuallyAdded());
            preparedStatement.executeUpdate();
        }

        log.info("Saved param: {}", param);
    }

    @SneakyThrows
    public void saveAll(List<Project> param) {
        String query = "INSERT INTO projects (title, type, path, is_manually_added) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            for (Project project : param) {
                preparedStatement.setString(1, project.getTitle());
                preparedStatement.setString(2, project.getType());
                preparedStatement.setString(3, project.getPath());
                preparedStatement.setBoolean(4, project.getIsManuallyAdded());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            connection.commit();
            log.info("Saved {} params", param.size());
        } catch (Exception e) {
            log.error("Failed to save all params, rolling back transaction", e);
        }
    }


}
