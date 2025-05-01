package org.nevertouchgrass.prolific.constants.profile;

public class NoMetricsUser implements User {
    public static final String PROFILE = "no_metrics_user";
    @Override
    public String getProfile() {
        return PROFILE;
    }
}
