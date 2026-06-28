package com.example.homework.monitoring;

import com.example.homework.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Actuator health indicator. Reports UP together with the current
 * number of students when the database is reachable, and DOWN (with the
 * failure cause) when the repository cannot be queried.
 *
 * <p>Spring derives the health component name from the bean name, so this
 * surfaces under the "studentDatabase" key of {@code /actuator/health}.
 */
@Slf4j
@Component("studentDatabase")
@RequiredArgsConstructor
public class StudentDatabaseHealthIndicator implements HealthIndicator {

    private final StudentRepository studentRepository;

    @Override
    public Health health() {
        try {
            long count = studentRepository.count();
            return Health.up()
                    .withDetail("studentCount", count)
                    .withDetail("source", "student-repository")
                    .build();
        } catch (Exception ex) {
            log.error("Student database health check failed", ex);
            return Health.down(ex)
                    .withDetail("source", "student-repository")
                    .build();
        }
    }
}
