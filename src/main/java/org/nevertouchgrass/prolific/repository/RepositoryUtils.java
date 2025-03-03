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
        return "INSERT INTO " + tableName + " (" + StringUtils.collectionToCommaDelimitedString(fieldPairs.stream().map(AbstractMap.SimpleEntry::getValue).toList()) + ") VALUES (" + StringUtils.collectionToCommaDelimitedString(fieldPairs.stream().map(_ -> "?").toList()) + ")";
    }

    public static String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static <T> String getTableName(Class<T> clazz) {
        return toSnakeCase(clazz.getSimpleName()) + "s";
    }

    public static String getFindAllQuery(List<AbstractMap.SimpleEntry<Field, String>> fieldPairs, String tableName) {
        return "SELECT " + StringUtils.collectionToCommaDelimitedString(fieldPairs.stream().map(AbstractMap.SimpleEntry::getValue).toList()) + " FROM " + tableName;
    }

    public static String getFindByIdQuery(List<AbstractMap.SimpleEntry<Field, String>> fieldPairs, String tableName) {
        return "SELECT " + StringUtils.collectionToCommaDelimitedString(fieldPairs.stream().map(AbstractMap.SimpleEntry::getValue).toList()) + " FROM " + tableName + " WHERE id = ?";
    }
}
