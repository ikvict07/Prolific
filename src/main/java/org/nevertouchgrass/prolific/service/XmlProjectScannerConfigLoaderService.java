package org.nevertouchgrass.prolific.service;

import lombok.NonNull;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class XmlProjectScannerConfigLoaderService {

    public List<ProjectTypeModel> loadProjectTypes(@NonNull String path) {
        List<ProjectTypeModel> projectTypeModels = new ArrayList<>();

        return null;
    }
}
