package com.example.homework.monitoring;

import com.example.homework.repository.CourseRepository;
import com.example.homework.repository.StudentRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers custom Micrometer gauges that expose the live row counts of the
 * core entities. They are scraped through {@code /actuator/metrics}, e.g.
 * {@code /actuator/metrics/app.students.total}.
 *
 * <p>Counters for creation events are incremented directly in the service
 * layer (see {@code app.students.created} / {@code app.courses.created}).
 */
@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Bean
    public MeterBinder entityCountGauges() {
        return registry -> {
            Gauge.builder("app.students.total", studentRepository, repo -> repo.count())
                    .description("Current number of students stored in the database")
                    .register(registry);
            Gauge.builder("app.courses.total", courseRepository, repo -> repo.count())
                    .description("Current number of courses stored in the database")
                    .register(registry);
        };
    }
}
