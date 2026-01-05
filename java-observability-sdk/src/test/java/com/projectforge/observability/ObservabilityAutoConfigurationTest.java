package com.projectforge.observability;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ObservabilityAutoConfiguration.
 */
class ObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ObservabilityAutoConfiguration.class));

    @Test
    void autoConfigurationShouldBeEnabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ObservabilityProperties.class);
        });
    }

    @Test
    void autoConfigurationShouldBeDisabledWhenPropertyIsFalse() {
        contextRunner
                .withPropertyValues("projectforge.observability.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ObservabilityAutoConfiguration.class);
                });
    }

    @Test
    void propertiesShouldHaveDefaultValues() {
        contextRunner.run(context -> {
            ObservabilityProperties properties = context.getBean(ObservabilityProperties.class);
            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getApplicationName()).isEqualTo("project-forge-app");
            assertThat(properties.getEnvironment()).isEqualTo("dev");
        });
    }

    @Test
    void propertiesShouldBeConfigurable() {
        contextRunner
                .withPropertyValues(
                        "projectforge.observability.application-name=my-app",
                        "projectforge.observability.environment=prod"
                )
                .run(context -> {
                    ObservabilityProperties properties = context.getBean(ObservabilityProperties.class);
                    assertThat(properties.getApplicationName()).isEqualTo("my-app");
                    assertThat(properties.getEnvironment()).isEqualTo("prod");
                });
    }
}

