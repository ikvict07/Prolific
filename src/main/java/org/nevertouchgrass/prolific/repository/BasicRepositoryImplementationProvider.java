package org.nevertouchgrass.prolific.repository;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static org.nevertouchgrass.prolific.repository.RepositoryUtils.getFieldPairs;
import static org.nevertouchgrass.prolific.repository.RepositoryUtils.getFindAllQuery;
import static org.nevertouchgrass.prolific.repository.RepositoryUtils.getFindByIdQuery;
import static org.nevertouchgrass.prolific.repository.RepositoryUtils.getInsertQuery;
import static org.nevertouchgrass.prolific.repository.RepositoryUtils.getTableName;
import static org.nevertouchgrass.prolific.repository.RepositoryUtils.getUpdateQuery;
import static org.nevertouchgrass.prolific.repository.RepositoryUtils.prepareInsertQuery;
import static org.nevertouchgrass.prolific.repository.RepositoryUtils.toSnakeCase;

/**
 * Simple implementation of a basic repository.
 * Inherit this class and you will have a basic repository implementation.
 *
 * @param <T> The type of the entity managed by this repository implementation.
 */

@Log4j2
@Repository
@SuppressWarnings({"java:S3011", "java:S1192", "unused"})
public abstract class BasicRepositoryImplementationProvider<T> implements BasicRepository<T> {
    protected final DataSource dataSource;

    protected BasicRepositoryImplementationProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("java:S3011")
    public T save(T t) {
        try {
            var tableName = t.getClass().getSimpleName().toLowerCase() + "s";
            var fieldPairs = getFieldPairs(t.getClass());
            String query = getInsertQuery(tableName, fieldPairs);

            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                prepareInsertQuery(t, fieldPairs, preparedStatement);
                preparedStatement.executeUpdate();
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer id = generatedKeys.getInt(1);
                        log.info("Saved: {} with ID: {}", t, id);

                        Field idField = t.getClass().getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(t, id);
                    } else {
                        log.warn("No ID obtained for {}", t);
                    }
                } catch (Exception e) {
                    log.error("Error while saving entity: {}", t, e);
                }
            }
            log.info("Saved: {}", t);
            return t;
        } catch (Exception e) {
            log.error("Error while saving entity: {}", t, e);
            throw e;
        }
    }


    @Override
    @SneakyThrows
    public Iterable<T> saveAll(Iterable<T> t) {
        try {
            var iter = t.iterator();
            if (!iter.hasNext()) {
                return t;
            }
            var first = iter.next();
            var tableName = first.getClass().getSimpleName().toLowerCase() + "s";
            var fieldPairs = getFieldPairs(first.getClass());
            String query = getInsertQuery(tableName, fieldPairs);

            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);

                for (var entity : t) {
                    prepareInsertQuery(entity, fieldPairs, preparedStatement);
                    preparedStatement.addBatch();
                }

                preparedStatement.executeBatch();
                connection.commit();
            }
            log.info("Saved: {}", t);
            return t;
        } catch (Exception e) {
            log.error("Error while saving entities: {}", t, e);
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public Iterable<T> findAll(Class<T> clazz) {
        try {
            List<T> results = new ArrayList<>();
            var tableName = getTableName(clazz);
            var fieldPairs = getFieldPairs(clazz);
            var query = getFindAllQuery(fieldPairs, tableName);

            log.info("Executing query: {}", query);
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {

                    List<Object> values = extractValuesFromResultSet(fieldPairs, resultSet);
                    results.add(clazz.getConstructor(values.stream().map(Object::getClass).toArray(Class[]::new)).newInstance(values.toArray()));
                }
            }
            results.forEach(r -> log.info("Found: {}", r));
            log.info("Found {} projects", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error while finding all entities", e);
            throw e;
        }
    }

    private static List<Object> extractValuesFromResultSet(List<AbstractMap.SimpleEntry<Field, String>> fieldPairs, ResultSet resultSet) throws SQLException {
        List<Object> values = new ArrayList<>();
        for (var fieldPair : fieldPairs) {
            Object value = resultSet.getObject(toSnakeCase(fieldPair.getValue()));

            if (value instanceof Integer integer && fieldPair.getKey().getType().equals(Boolean.class)) {
                value = (integer != 0);
            }

            values.add(value);
        }
        return values;
    }

    @Override
    @SneakyThrows
    public T findById(Long id, Class<T> clazz) {
        try {
            var tableName = getTableName(clazz);
            var fieldPairs = getFieldPairs(clazz);
            var query = getFindByIdQuery(fieldPairs, tableName);

            log.info("Executing query: {}", query);
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        List<Object> values = extractValuesFromResultSet(fieldPairs, resultSet);
                        return clazz.getConstructor(values.stream().map(Object::getClass).toArray(Class[]::new)).newInstance(values.toArray());
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error while finding entity by id: {}", id, e);
            throw e;
        }
    }

    @SuppressWarnings({"SqlSourceToSinkFlow"})
    public void execute(String query) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            log.error("Error executing query: {}", query, e);
        }
    }

    @Override
    @SneakyThrows
    public T update(T t) {
        try {
            var tableName = t.getClass().getSimpleName().toLowerCase() + "s";
            var fieldPairs = getFieldPairs(t.getClass());
            var query = getUpdateQuery(fieldPairs, tableName);

            log.info("Executing query: {}", query);
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                prepareInsertQuery(t, fieldPairs, preparedStatement);
                var id = t.getClass().getDeclaredField("id");
                id.setAccessible(true);
                preparedStatement.setLong(fieldPairs.size() + 1, (Integer) id.get(t));
                preparedStatement.executeUpdate();
            }
            return t;
        } catch (Exception e) {
            log.error("Error while updating entity: {}", t, e);
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public T delete(T t) {
        try {
            var tableName = t.getClass().getSimpleName().toLowerCase() + "s";
            var id = t.getClass().getDeclaredField("id");
            id.setAccessible(true);
            var idValue = (Integer) id.get(t);
            var query = "DELETE FROM " + tableName + " WHERE id = ?";
            log.info("Executing query: {}", query);
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, idValue);
                preparedStatement.executeUpdate();
            }
            log.info("Deleted: {}", t);
        } catch (Exception e) {
            log.error("Error while deleting entity: {}", t, e);
            throw e;
        }
        return t;
    }
}
