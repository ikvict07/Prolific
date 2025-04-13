package org.nevertouchgrass.prolific.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Files;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {
    private final PathService pathService;
    @Bean
    @SneakyThrows
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        var dbPath = pathService.getProjectFilesPath().resolve("data/prolific.sqlite");
        Files.createDirectories(dbPath.getParent());
        var url = "jdbc:sqlite:" + dbPath;
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setMaximumPoolSize(10);

        return new HikariDataSource(hikariConfig);
    }
}
