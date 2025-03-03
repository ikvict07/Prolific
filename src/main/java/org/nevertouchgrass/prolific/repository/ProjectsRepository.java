package org.nevertouchgrass.prolific.repository;

import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Log4j2
@Component
public class ProjectsRepository extends BasicRepositoryImplementationProvider<Project> {
    public ProjectsRepository(DataSource dataSource) {
        super(dataSource);
    }
}
