package com.springbootacademy.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.seed-data=false",
        "spring.datasource.url=jdbc:h2:mem:academydb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
class AcademyApiIntegrationTest {

    private static final AtomicInteger SEQ = new AtomicInteger(1000);

    @Autowired
    private MockMvc mockMvc;

    // ---------------- helpers ----------------

    private long createStudent(String email) throws Exception {
        return createAndGetId("/api/v1/students", studentJson(email));
    }

    private long createCourse(String code, int seats, String fee) throws Exception {
        return createAndGetId("/api/v1/courses", courseJson(code, seats, fee));
    }

    private long createAndGetId(String url, String json) throws Exception {
        String body = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    private String studentJson(String email) {
        return "{\"fullName\":\"Test Student\",\"email\":\"" + email + "\",\"contactNumber\":\"0771234567\"}";
    }

    private String courseJson(String code, int seats, String fee) {
        return "{\"name\":\"Course " + code + "\",\"code\":\"" + code + "\",\"description\":\"desc\","
                + "\"fee\":" + fee + ",\"totalSeats\":" + seats + "}";
    }

    private String enrollJson(long studentId, long courseId) {
        return "{\"studentId\":" + studentId + ",\"courseId\":" + courseId + ",\"paid\":true}";
    }

    private long enroll(long studentId, long courseId) throws Exception {
        return createAndGetId("/api/v1/enrollments", enrollJson(studentId, courseId));
    }

    private String uniq(String prefix) {
        return prefix + SEQ.incrementAndGet();
    }

    // ---------------- tests ----------------

    @Test
    void fullEnrollmentFlow_seatsAreTrackedAndRolledBackCorrectly() throws Exception {
        long courseId = createCourse(uniq("FLOW-"), 1, "1000.00");
        long studentA = createStudent(uniq("a") + "@example.com");
        long studentB = createStudent(uniq("b") + "@example.com");

        // enroll A -> success, seats 1 -> 0
        long enrollmentId = enroll(studentA, courseId);
        mockMvc.perform(get("/api/v1/courses/" + courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableSeats").value(0));

        // enrollment details contain basic student & course info
        mockMvc.perform(get("/api/v1/enrollments/" + enrollmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ENROLLED"))
                .andExpect(jsonPath("$.data.studentId").value(studentA))
                .andExpect(jsonPath("$.data.studentName").value("Test Student"))
                .andExpect(jsonPath("$.data.courseId").value(courseId));

        // enroll B -> no seats -> 400, nothing saved
        mockMvc.perform(post("/api/v1/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content(enrollJson(studentB, courseId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.message", containsString("No seats")));
        mockMvc.perform(get("/api/v1/enrollments").param("courseId", String.valueOf(courseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        // cancel A -> seats back to 1
        mockMvc.perform(patch("/api/v1/enrollments/" + enrollmentId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        mockMvc.perform(get("/api/v1/courses/" + courseId))
                .andExpect(jsonPath("$.data.availableSeats").value(1));

        // cancel again -> 400, seats unchanged
        mockMvc.perform(patch("/api/v1/enrollments/" + enrollmentId + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
        mockMvc.perform(get("/api/v1/courses/" + courseId))
                .andExpect(jsonPath("$.data.availableSeats").value(1));

        // B can enroll now
        enroll(studentB, courseId);
        mockMvc.perform(get("/api/v1/courses/" + courseId))
                .andExpect(jsonPath("$.data.availableSeats").value(0));
    }

    @Test
    void sameStudentCannotEnrollTwiceInSameCourse_andSeatCountIsNotAffected() throws Exception {
        long courseId = createCourse(uniq("DUP-"), 5, "500.00");
        long studentId = createStudent(uniq("dup") + "@example.com");

        enroll(studentId, courseId);

        mockMvc.perform(post("/api/v1/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content(enrollJson(studentId, courseId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already enrolled")));

        mockMvc.perform(get("/api/v1/courses/" + courseId))
                .andExpect(jsonPath("$.data.availableSeats").value(4));
    }

    @Test
    void duplicateEmailAndDuplicateCourseCode_areRejected() throws Exception {
        String email = uniq("same") + "@example.com";
        createStudent(email);
        mockMvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(studentJson(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("FAIL"));

        String code = uniq("CODE-");
        createCourse(code, 10, "100.00");
        mockMvc.perform(post("/api/v1/courses").contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson(code.toLowerCase(), 10, "100.00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    void validationErrors_return400WithFieldDetails() throws Exception {
        mockMvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"not-an-email\",\"contactNumber\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.fullName").exists());

        mockMvc.perform(post("/api/v1/courses").contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson(uniq("BAD-"), 0, "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.fee").exists())
                .andExpect(jsonPath("$.data.totalSeats").exists());
    }

    @Test
    void notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/students/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("FAIL"));
        mockMvc.perform(get("/api/v1/courses/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/enrollments/999999")).andExpect(status().isNotFound());
    }

    @Test
    void softDeletedStudent_isInactive_andCannotEnroll() throws Exception {
        long studentId = createStudent(uniq("soft") + "@example.com");
        long courseId = createCourse(uniq("SOFT-"), 3, "100.00");

        mockMvc.perform(delete("/api/v1/students/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        mockMvc.perform(get("/api/v1/students/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(post("/api/v1/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content(enrollJson(studentId, courseId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inactiveCourse_cannotBeEnrolled_andCanBeReactivated() throws Exception {
        long studentId = createStudent(uniq("inact") + "@example.com");
        long courseId = createCourse(uniq("INACT-"), 3, "100.00");

        mockMvc.perform(patch("/api/v1/courses/" + courseId + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(post("/api/v1/enrollments").contentType(MediaType.APPLICATION_JSON)
                        .content(enrollJson(studentId, courseId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/courses/" + courseId + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());
        enroll(studentId, courseId);
    }

    @Test
    void studentUpdate_andSearchWithPagination() throws Exception {
        String marker = uniq("zzsearch");
        long id = createStudent(marker + "@example.com");

        mockMvc.perform(put("/api/v1/students/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Updated Name\",\"email\":\"" + marker
                                + "@example.com\",\"contactNumber\":\"0712345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/students").param("search", marker).param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
    }

    @Test
    void courseListing_supportsSortingFilteringAndRejectsBadSort() throws Exception {
        createCourse(uniq("SORT-"), 5, "70001.00");
        createCourse(uniq("SORT-"), 5, "70003.00");
        createCourse(uniq("SORT-"), 5, "70002.00");

        mockMvc.perform(get("/api/v1/courses")
                        .param("minFee", "70000").param("maxFee", "70010")
                        .param("status", "ACTIVE").param("sort", "fee,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.content[0].fee").value(70003.0))
                .andExpect(jsonPath("$.data.content[2].fee").value(70001.0));

        mockMvc.perform(get("/api/v1/courses").param("sort", "doesNotExist,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));

        mockMvc.perform(get("/api/v1/courses").param("status", "WRONG"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/courses").param("minFee", "10").param("maxFee", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void courseUpdate_adjustsAvailableSeatsAndProtectsEnrolledSeats() throws Exception {
        String code = uniq("UPD-");
        long courseId = createCourse(code, 5, "100.00");
        long studentId = createStudent(uniq("upd") + "@example.com");
        enroll(studentId, courseId);                       // 4 of 5 seats free

        // grow to 10 seats -> 9 available
        mockMvc.perform(put("/api/v1/courses/" + courseId).contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson(code, 10, "150.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSeats").value(10))
                .andExpect(jsonPath("$.data.availableSeats").value(9));

        // shrink to exactly the number of seats already taken (1) -> 0 available
        mockMvc.perform(put("/api/v1/courses/" + courseId).contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson(code, 1, "150.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableSeats").value(0));
    }

    @Test
    void enrollmentFilters_byStudentAndStatus() throws Exception {
        long courseId = createCourse(uniq("FIL-"), 5, "100.00");
        long studentId = createStudent(uniq("fil") + "@example.com");
        long enrollmentId = enroll(studentId, courseId);
        mockMvc.perform(patch("/api/v1/enrollments/" + enrollmentId + "/cancel")).andExpect(status().isOk());
        enroll(studentId, courseId);

        mockMvc.perform(get("/api/v1/enrollments").param("studentId", String.valueOf(studentId)))
                .andExpect(jsonPath("$.data.content.length()").value(2));
        mockMvc.perform(get("/api/v1/enrollments")
                        .param("studentId", String.valueOf(studentId)).param("status", "CANCELLED"))
                .andExpect(jsonPath("$.data.content.length()").value(1));
        mockMvc.perform(get("/api/v1/enrollments")
                        .param("courseId", String.valueOf(courseId)).param("status", "ENROLLED"))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void rootRedirectsToSwaggerUi() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui.html"));

        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/favicon-32x32.png"));
    }

    @Test
    void openApiDocsAreAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Springboot Academy Course Management API"));
    }
}
