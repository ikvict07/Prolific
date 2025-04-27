package org.nevertouchgrass.prolific.repository;

import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for repositories that provides common methods for repositories.
 * Like generating insert queries, finding table names, and so on.
 */
public class RepositoryUtils {
    private RepositoryUtils() {

    }

    @SuppressWarnings("java:S3011")
    public static <T> void prepareInsertQuery(T t, List<AbstractMap.SimpleEntry<Field, String>> fieldPairs, PreparedStatement preparedStatement) throws SQLException, IllegalAccessException {
        var i = 1;
        for (var fieldPair : fieldPairs) {
            fieldPair.getKey().setAccessible(true);
            preparedStatement.setObject(i, fieldPair.getKey().get(t));
            i++;
        }
    }

    public static <T> List<AbstractMap.SimpleEntry<Field, String>> getFieldPairs(Class<T> t) {
        var fields = t.getDeclaredFields();
        return Arrays.stream(fields).map(field -> new AbstractMap.SimpleEntry<>(field, toSnakeCase(field.getName()))).toList();
    }


    public static String getInsertQuery(String tableName, List<AbstractMap.SimpleEntry<Field, String>> fieldPairs) {
        String safeTableName = validateSqlIdentifier(tableName);
        List<String> safeColumns = fieldPairs.stream()
                .map(AbstractMap.SimpleEntry::getValue)
                .map(RepositoryUtils::validateSqlIdentifier)
                .toList();

        return "INSERT INTO " + safeTableName + " (" +
                StringUtils.collectionToCommaDelimitedString(safeColumns) +
                ") VALUES (" +
                StringUtils.collectionToCommaDelimitedString(fieldPairs.stream().map(_ -> "?").toList()) +
                ")";
    }


    public static String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static <T> String getTableName(Class<T> clazz) {
        return toSnakeCase(clazz.getSimpleName()) + "s";
    }

    public static String getFindAllQuery(List<AbstractMap.SimpleEntry<Field, String>> fieldPairs, String tableName) {
        String safeTableName = validateSqlIdentifier(tableName);
        List<String> safeColumns = fieldPairs.stream()
                .map(AbstractMap.SimpleEntry::getValue)
                .map(RepositoryUtils::validateSqlIdentifier)
                .toList();

        return "SELECT " + StringUtils.collectionToCommaDelimitedString(safeColumns) +
                " FROM " + safeTableName;
    }


    public static String getFindByIdQuery(List<AbstractMap.SimpleEntry<Field, String>> fieldPairs, String tableName) {
        String safeTableName = validateSqlIdentifier(tableName);
        List<String> safeColumns = fieldPairs.stream()
                .map(AbstractMap.SimpleEntry::getValue)
                .map(RepositoryUtils::validateSqlIdentifier)
                .toList();

        return "SELECT " + StringUtils.collectionToCommaDelimitedString(safeColumns) +
                " FROM " + safeTableName + " WHERE id = ?";
    }


    public static String getUpdateQuery(List<AbstractMap.SimpleEntry<Field, String>> fieldPairs, String tableName) {
        String safeTableName = validateSqlIdentifier(tableName);

        return "UPDATE " + safeTableName + " SET "
                + StringUtils.collectionToCommaDelimitedString(
                fieldPairs.stream()
                        .map(entry -> validateSqlIdentifier(entry.getValue()) + " = ?")
                        .toList()
        )
                + " WHERE id = ?";
    }

    public static String validateSqlIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("SQL identifier cannot be null or empty");
        }

        if (!identifier.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("SQL identifier contains invalid characters: " + identifier);
        }

        List<String> sqlKeywords = Arrays.asList("SELECT", "FROM", "WHERE", "INSERT",
                "UPDATE", "DELETE", "DROP", "CREATE", "TABLE", "INDEX", "ALTER",
                "ADD", "COLUMN", "SET", "INTO", "VALUES");

        if (sqlKeywords.contains(identifier.toUpperCase())) {
            throw new IllegalArgumentException("SQL identifier is a reserved keyword: " + identifier);
        }

        return identifier;
    }

    public static String getDeleteQuery(String tableName) {
        return "DELETE FROM " + validateSqlIdentifier(tableName) + " WHERE id = ?";
    }
}
