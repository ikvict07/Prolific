package org.nevertouchgrass.prolific.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class DatabaseConfig {
    @Bean
    @SneakyThrows
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        String userHome = System.getProperty("user.home");
        String dbPath = userHome + "/Prolific/data/prolific.sqlite";
        Files.createDirectories(Path.of(dbPath).getParent());
        var url = "jdbc:sqlite:" + dbPath;
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setMaximumPoolSize(10);

        return new HikariDataSource(hikariConfig);
    }
}
