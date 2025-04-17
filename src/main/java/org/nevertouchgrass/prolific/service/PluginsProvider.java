package org.nevertouchgrass.prolific.service;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PluginsProvider {
    private final PathService pathService;


}
