package com.example.homework.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Externalized application settings bound from the "app.settings.*" keys
 * of the active profile's properties file. Invalid values fail startup
 * thanks to JSR-303 validation + @Validated.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.settings")
public class AppSettingsProperties {

    /** Human-readable application title shown in /api/info and Swagger. */
    @NotBlank
    private String title;

    /** Contact e-mail published in API metadata. */
    @NotBlank
    @Email
    private String contactEmail;

    /** Feature flag: include environment details in the /api/info response. */
    private boolean showEnvironmentInfo;
}
