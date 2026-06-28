package com.example.homework.controller;

import com.example.homework.config.AppSettingsProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/info")
@RequiredArgsConstructor
@Tag(name = "Application Info", description = "Metadata built from the custom app.settings.* configuration")
public class AppInfoController {

    private final AppSettingsProperties appSettings;
    private final MessageSource messageSource;
    private final Environment environment;

    @GetMapping
    @Operation(summary = "Application metadata — welcome message is localized via the Accept-Language header")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Locale locale = LocaleContextHolder.getLocale();
        log.debug("Serving /api/info for locale {}", locale);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", appSettings.getTitle());
        info.put("welcomeMessage", messageSource.getMessage(
                "app.welcome", new Object[]{appSettings.getTitle()}, locale));
        info.put("contactEmail", appSettings.getContactEmail());
        info.put("paginationLimit", appSettings.getPaginationLimit());
        info.put("locale", locale.toLanguageTag());

        // feature flag from the active profile's configuration
        if (appSettings.isShowEnvironmentInfo()) {
            info.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
            info.put("javaVersion", System.getProperty("java.version"));
        }
        return ResponseEntity.ok(info);
    }
}
