package org.nevertouchgrass.prolific.constants.profile;

public class PowerUser implements User {
    public static final String PROFILE = "power_user";

    @Override
    public String getProfile() {
        return PROFILE;
    }
}