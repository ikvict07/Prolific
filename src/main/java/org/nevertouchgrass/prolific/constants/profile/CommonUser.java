package org.nevertouchgrass.prolific.constants.profile;

public class CommonUser implements User {
    public static final String PROFILE = "common_user";

    @Override
    public String getProfile() {
        return PROFILE;
    }
}