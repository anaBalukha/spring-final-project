package com.example.homework.controller;

import com.example.homework.dto.request.StudentRequest;
import com.example.homework.dto.response.StudentResponse;
import com.example.homework.exception.ResourceNotFoundException;
import com.example.homework.security.CustomUserDetailsService;
import com.example.homework.security.SecurityConfig;
import com.example.homework.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer slice test. Loads only the web layer plus the real
 * {@link SecurityConfig}, mocking the service and user-details beans, and
 * drives requests through {@link MockMvc} to verify routing, status codes,
 * validation and the security rules (authn/authz).
 */
@WebMvcTest(StudentController.class)
@Import({SecurityConfig.class, StudentControllerTest.TestMessageConfig.class})
class StudentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private StudentService studentService;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        // Build MockMvc with the Spring Security filter chain + test support so
        // @WithMockUser / @WithAnonymousUser drive the authn/authz rules.
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private StudentResponse sample() {
        return StudentResponse.builder()
                .id(1L).firstName("Nino").lastName("Beridze").email("nino@example.com")
                .courses(List.of()).build();
    }

    @Test
    @DisplayName("GET /api/students is public and returns 200")
    @WithAnonymousUser
    void getAll_isPublic() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("nino@example.com"));
    }

    @Test
    @DisplayName("GET /api/students/{id} returns 404 when missing")
    @WithAnonymousUser
    void getById_notFound() throws Exception {
        when(studentService.getStudentById(99L))
                .thenThrow(new ResourceNotFoundException("error.student.notfound", 99L));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/students without authentication returns 401")
    @WithAnonymousUser
    void create_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StudentRequest("Nino", "Beridze", "nino@example.com"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/students as an authenticated user returns 201")
    @WithMockUser(username = "user", roles = "USER")
    void create_authenticated_returns201() throws Exception {
        when(studentService.createStudent(any())).thenReturn(sample());

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StudentRequest("Nino", "Beridze", "nino@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/students with an invalid body returns 400")
    @WithMockUser(username = "user", roles = "USER")
    void create_invalidBody_returns400() throws Exception {
        // blank first name + invalid email -> bean validation rejects the request
        StudentRequest invalid = new StudentRequest("", "Beridze", "not-an-email");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(studentService, never()).createStudent(any());
    }

    @Test
    @DisplayName("DELETE /api/students/{id} as USER is forbidden (403)")
    @WithMockUser(username = "user", roles = "USER")
    void delete_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isForbidden());
        verify(studentService, never()).deleteStudent(any());
    }

    @Test
    @DisplayName("DELETE /api/students/{id} as ADMIN returns 204")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void delete_asAdmin_returns204() throws Exception {
        doNothing().when(studentService).deleteStudent(eq(1L));

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());
        verify(studentService).deleteStudent(1L);
    }

    /** Provides a real message bundle so the exception handler can localize. */
    @TestConfiguration
    static class TestMessageConfig {
        @Bean
        MessageSource messageSource() {
            ResourceBundleMessageSource source = new ResourceBundleMessageSource();
            source.setBasename("messages");
            source.setDefaultEncoding("UTF-8");
            return source;
        }
    }
}
