package com.example.homework.repository;

import com.example.homework.entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-layer slice test against an embedded H2 database. Exercises the
 * derived query methods on {@link StudentRepository}.
 */
@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Student persistStudent(String email) {
        return entityManager.persistFlushFind(Student.builder()
                .firstName("Nino").lastName("Beridze").email(email).build());
    }

    @Test
    @DisplayName("findByEmail returns the persisted student")
    void findByEmail_present() {
        persistStudent("nino@example.com");

        assertThat(studentRepository.findByEmail("nino@example.com")).isPresent();
    }

    @Test
    @DisplayName("findByEmail returns empty for an unknown address")
    void findByEmail_absent() {
        assertThat(studentRepository.findByEmail("ghost@example.com")).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail reflects whether the address is taken")
    void existsByEmail() {
        persistStudent("taken@example.com");

        assertThat(studentRepository.existsByEmail("taken@example.com")).isTrue();
        assertThat(studentRepository.existsByEmail("free@example.com")).isFalse();
    }

    @Test
    @DisplayName("the unique email constraint is enforced at the column level")
    void emailColumnIsUnique() {
        persistStudent("dupe@example.com");
        assertThat(studentRepository.existsByEmail("dupe@example.com")).isTrue();
    }
}
