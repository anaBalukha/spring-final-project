package com.example.homework.repository;

import com.example.homework.entity.Course;
import com.example.homework.entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-layer slice test for the custom {@code findByStudentId} query.
 */
@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByStudentId returns only the given student's courses")
    void findByStudentId_filtersByOwner() {
        Student owner = entityManager.persist(Student.builder()
                .firstName("Ana").lastName("Lomidze").email("ana@example.com").build());
        Student other = entityManager.persist(Student.builder()
                .firstName("Giorgi").lastName("Kapanadze").email("giorgi@example.com").build());

        entityManager.persist(Course.builder().name("DB").student(owner).build());
        entityManager.persist(Course.builder().name("Security").student(owner).build());
        entityManager.persist(Course.builder().name("Other").student(other).build());
        entityManager.flush();

        List<Course> courses = courseRepository.findByStudentId(owner.getId());

        assertThat(courses).hasSize(2)
                .extracting(Course::getName)
                .containsExactlyInAnyOrder("DB", "Security");
    }

    @Test
    @DisplayName("findByStudentId returns empty when the student has no courses")
    void findByStudentId_noCourses() {
        Student lonely = entityManager.persist(Student.builder()
                .firstName("Solo").lastName("Student").email("solo@example.com").build());
        entityManager.flush();

        assertThat(courseRepository.findByStudentId(lonely.getId())).isEmpty();
    }
}
