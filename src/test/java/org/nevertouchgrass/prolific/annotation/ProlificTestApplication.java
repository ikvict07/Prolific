package org.nevertouchgrass.prolific.annotation;


import org.nevertouchgrass.prolific.TestApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SpringBootTest(
        classes = {
                TestApplication.class,
        }
)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProlificTestApplication {
}
