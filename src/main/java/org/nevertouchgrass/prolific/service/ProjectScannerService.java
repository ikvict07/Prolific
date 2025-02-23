package org.nevertouchgrass.prolific.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nevertouchgrass.prolific.configuration.PluginConfigProvider;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectScannerService {

	private static final Logger LOGGER = LogManager.getLogger();

	private final List<ProjectTypeModel> projectTypeModels;
	private final Set<Path> projects = new HashSet<>();

	@Autowired
	public ProjectScannerService(@NonNull XmlProjectScannerConfigLoaderService configLoaderService) {
		this.projectTypeModels = configLoaderService.loadProjectTypes();
	}

	public Set<Path> scanForProjects(String rootDirectory) {
		File file = new File(rootDirectory);

		if (file.isDirectory()) {
			try {
				Files.walkFileTree(file.toPath(), new ProjectFinder());
			} catch (Exception e) {
				LOGGER.error("Error occurred while trying to access file", e);
			}
		}

		return projects;
	}

	private class ProjectFinder extends SimpleFileVisitor<Path> {

		private final PathMatcher pathMatcher;

		private ProjectFinder() {
			List<String> matchers = new ArrayList<>();
			for (ProjectTypeModel projectTypeModel : projectTypeModels) {
				matchers.addAll(projectTypeModel.getIdentifiers());
			}

			String pattern = String.format("glob:**/{%s}", String.join(",", matchers));

			this.pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
		}

		@Override
		@NonNull
		public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
			if (pathMatcher.matches(file)) {
                try {
                    projects.add(file.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
                } catch (IOException e) {
                    LOGGER.error(e);
                }
                return FileVisitResult.SKIP_SIBLINGS;
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		@NonNull
		public FileVisitResult preVisitDirectory(Path dir, @NonNull BasicFileAttributes attrs) {
			if (pathMatcher.matches(dir)) {
                try {
                    projects.add(dir.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
                } catch (IOException e) {
                    LOGGER.error(e);
                }
                return FileVisitResult.SKIP_SUBTREE;
			}

			return FileVisitResult.CONTINUE;
		}
	}
}
