package com.example.homework.integration;

import com.example.homework.dto.request.StudentRequest;
import com.example.homework.dto.response.StudentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test that boots the whole application on a random
 * port (default "dev" profile: in-memory H2 seeded with sample data and the
 * default admin/user accounts) and drives it over real HTTP with
 * {@link TestRestTemplate}. Exercises routing, security and persistence
 * together.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class StudentApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /api/students is public and returns the seeded students")
    void getStudents_isPublic() {
        ResponseEntity<StudentResponse[]> response =
                restTemplate.getForEntity("/api/students", StudentResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("GET /api/info exposes localized application metadata")
    void getInfo_returnsMetadata() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/info", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("title", "welcomeMessage");
    }

    @Test
    @DisplayName("POST /api/students without credentials is rejected with 401")
    void createStudent_unauthenticated_returns401() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/students", jsonEntity(new StudentRequest("Test", "User", "unauth@example.com")),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Authenticated create then admin delete round-trips successfully")
    void createAndDeleteStudent_fullRoundTrip() {
        StudentRequest request = new StudentRequest("Lasha", "Tabidze", "lasha.integration@example.com");

        // USER may create
        ResponseEntity<StudentResponse> created = restTemplate.withBasicAuth("user", "user123")
                .postForEntity("/api/students", jsonEntity(request), StudentResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = created.getBody().getId();
        assertThat(id).isNotNull();

        // USER may NOT delete -> 403
        ResponseEntity<String> forbidden = restTemplate.withBasicAuth("user", "user123")
                .exchange("/api/students/" + id, HttpMethod.DELETE, null, String.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // ADMIN may delete -> 204
        ResponseEntity<Void> deleted = restTemplate.withBasicAuth("admin", "admin123")
                .exchange("/api/students/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Actuator health endpoint is public and reports UP")
    void actuatorHealth_isUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Actuator metrics endpoint requires ADMIN authentication")
    void actuatorMetrics_requiresAdmin() {
        // anonymous -> 401
        assertThat(restTemplate.getForEntity("/actuator/metrics", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        // admin -> 200
        assertThat(restTemplate.withBasicAuth("admin", "admin123")
                .getForEntity("/actuator/metrics", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    private HttpEntity<StudentRequest> jsonEntity(StudentRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
