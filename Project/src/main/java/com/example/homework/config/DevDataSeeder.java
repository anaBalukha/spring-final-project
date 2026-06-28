package com.example.homework.config;

import com.example.homework.entity.Course;
import com.example.homework.entity.Student;
import com.example.homework.repository.CourseRepository;
import com.example.homework.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fills the in-memory H2 database with sample students and courses.
 * Runs only when the "dev" profile is active.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Override
    public void run(String... args) {
        if (studentRepository.count() > 0) {
            log.debug("Dev seed skipped — students already present");
            return;
        }

        Student nino = studentRepository.save(Student.builder()
                .firstName("Nino").lastName("Beridze").email("nino.beridze@example.com").build());
        Student giorgi = studentRepository.save(Student.builder()
                .firstName("Giorgi").lastName("Kapanadze").email("giorgi.kapanadze@example.com").build());
        Student ana = studentRepository.save(Student.builder()
                .firstName("Ana").lastName("Lomidze").email("ana.lomidze@example.com").build());

        courseRepository.saveAll(List.of(
                Course.builder().name("Spring Boot Fundamentals")
                        .description("REST APIs, profiles, configuration and i18n").student(nino).build(),
                Course.builder().name("Database Systems")
                        .description("Relational modelling with JPA and PostgreSQL").student(nino).build(),
                Course.builder().name("Web Security")
                        .description("Authentication and authorization with Spring Security").student(giorgi).build(),
                Course.builder().name("Software Engineering")
                        .description("Clean code, logging and observability").student(ana).build()
        ));

        log.info("Dev profile test data seeded: {} students, {} courses",
                studentRepository.count(), courseRepository.count());
    }
}
