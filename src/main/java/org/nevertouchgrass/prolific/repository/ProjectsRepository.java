package org.nevertouchgrass.prolific.repository;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Repository
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ProjectsRepository extends BasicRepositoryImplementationProvider<Project> {
    public ProjectsRepository(DataSource dataSource) {
        super(dataSource);
    }


    @SneakyThrows
    public Project findByPath(String path) {
        try {
            String sql = "SELECT * FROM projects WHERE path = ?";

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, path);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Project project = new Project();
                        project.setId(resultSet.getInt("id"));
                        project.setTitle(resultSet.getString("title"));
                        project.setType(resultSet.getString("type"));
                        project.setPath(resultSet.getString("path"));
                        project.setIsManuallyAdded(resultSet.getBoolean("is_manually_added"));
                        project.setIsStarred(resultSet.getBoolean("is_starred"));
                        return project;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while finding project by path", e);
            throw e;
        }
        return null;
    }

    @Override
    public Project save(Project project) {
        var existingProject = findByPath(project.getPath());
        if (existingProject != null) {
            return existingProject;
        }
        return super.save(project);
    }

    @SneakyThrows
    public List<Project> deleteWhereIsStarredIsFalseAndIsManuallyAddedIsFalse() {
        String sql = "DELETE FROM projects WHERE is_starred = 0 AND is_manually_added = 0 RETURNING *";

        List<Project> deletedProject = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Project project = new Project();
                project.setId(resultSet.getInt("id"));
                project.setTitle(resultSet.getString("title"));
                project.setType(resultSet.getString("type"));
                project.setPath(resultSet.getString("path"));
                project.setIsManuallyAdded(resultSet.getBoolean("is_manually_added"));
                project.setIsStarred(resultSet.getBoolean("is_starred"));
                deletedProject.add(project);

            }
        }

        return deletedProject;
    }

}
