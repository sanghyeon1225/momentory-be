package com.momentory.schedule.presentation;

import com.momentory.auth.token.application.AccessTokenIssuer;
import com.momentory.schedule.domain.Schedule;
import com.momentory.schedule.infrastructure.ScheduleRepository;
import com.momentory.user.domain.User;
import com.momentory.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(properties = {
        "JWT_SECRET=JZP9amP0y2bXk2LG9f9piS5jH3vK9B5w7qxgEriqMA4=",
        "JWT_REFRESH_EXPIRATION=30d",
        "KAKAO_APP_ID=123456789"
})
@Testcontainers(disabledWithoutDocker = true)
class ScheduleControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg17"));

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired UserRepository userRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired AccessTokenIssuer accessTokenIssuer;
    @Autowired JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void cleanUp() {
        scheduleRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void createsManualScheduleWithTrimmedTitleAndNextDisplayOrder() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 10);

        mockMvc.perform(create(user, "{\"date\":\"2026-08-10\",\"title\":\" 운동하기 \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-08-10"))
                .andExpect(jsonPath("$.title").value("운동하기"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.emotion").doesNotExist())
                .andExpect(jsonPath("$.displayOrder").value(0));
        mockMvc.perform(create(user, "{\"date\":\"2026-08-10\",\"title\":\"독서하기\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayOrder").value(1));

        assertThat(scheduleRepository.findByUser_IdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(user.getId(), date))
                .extracting(Schedule::getTitle)
                .containsExactly("운동하기", "독서하기");
    }

    @Test
    void returnsOnlyCurrentUsersActiveSchedulesOrderedByDisplayOrderThenId() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 10);
        Schedule first = scheduleRepository.saveAndFlush(Schedule.createManual(user, date, "첫 번째", 0L));
        Schedule sameOrder = scheduleRepository.saveAndFlush(Schedule.createManual(user, date, "같은 순서", 0L));
        Schedule later = scheduleRepository.saveAndFlush(Schedule.createManual(user, date, "나중 순서", 1L));
        Schedule deleted = scheduleRepository.saveAndFlush(Schedule.createManual(user, date, "삭제됨", 2L));
        deleted.delete(java.time.Instant.now());
        scheduleRepository.saveAndFlush(deleted);
        scheduleRepository.saveAndFlush(Schedule.createManual(anotherUser, date, "다른 사용자", 0L));

        mockMvc.perform(getSchedules(user, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedules.length()").value(3))
                .andExpect(jsonPath("$.schedules[0].id").value(first.getId()))
                .andExpect(jsonPath("$.schedules[1].id").value(sameOrder.getId()))
                .andExpect(jsonPath("$.schedules[2].id").value(later.getId()));
    }

    @Test
    void updatesOnlyOwnedActiveSchedule() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user, LocalDate.of(2026, 8, 10), "운동하기", 0L));
        Schedule deleted = scheduleRepository.saveAndFlush(Schedule.createManual(user, LocalDate.of(2026, 8, 10), "삭제된 일정", 1L));
        Schedule external = scheduleRepository.saveAndFlush(Schedule.createManual(user, LocalDate.of(2026, 8, 10), "외부 일정", 2L));
        deleted.delete(java.time.Instant.now());
        scheduleRepository.saveAndFlush(deleted);
        jdbcTemplate.update("UPDATE schedules SET external_id = ? WHERE id = ?", "external-1", external.getId());

        mockMvc.perform(update(user, schedule.getId(), "{\"date\":\"2026-08-11\",\"title\":\" 저녁 운동하기 \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-08-11"))
                .andExpect(jsonPath("$.title").value("저녁 운동하기"));
        mockMvc.perform(update(anotherUser, schedule.getId(), "{\"date\":\"2026-08-11\",\"title\":\"변경\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
        mockMvc.perform(update(user, deleted.getId(), "{\"date\":\"2026-08-11\",\"title\":\"변경\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
        mockMvc.perform(update(user, external.getId(), "{\"date\":\"2026-08-11\",\"title\":\"변경\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));

        Schedule updated = scheduleRepository.findById(schedule.getId()).orElseThrow();
        assertThat(updated.getScheduleDate()).isEqualTo(LocalDate.of(2026, 8, 11));
        assertThat(updated.getTitle()).isEqualTo("저녁 운동하기");
    }

    @Test
    void softDeletesOnlyOwnedScheduleAndMakesRepeatDeleteIdempotent() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user, LocalDate.of(2026, 8, 10), "운동하기", 0L));

        mockMvc.perform(deleteSchedule(anotherUser, schedule.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
        mockMvc.perform(deleteSchedule(user, schedule.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(deleteSchedule(user, schedule.getId()))
                .andExpect(status().isNoContent());

        Schedule deleted = scheduleRepository.findById(schedule.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
        mockMvc.perform(getSchedules(user, LocalDate.of(2026, 8, 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedules").isEmpty());
    }

    @Test
    void rejectsBlankTooLongAndInvalidDates() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        String tooLongTitle = "a".repeat(256);

        mockMvc.perform(create(user, "{\"date\":\"2026-08-10\",\"title\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(create(user, "{\"date\":\"2026-08-10\",\"title\":\"%s\"}".formatted(tooLongTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(get("/api/v1/schedules?date=2026-08-40").header(HttpHeaders.AUTHORIZATION, bearerToken(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(delete("/api/v1/schedules/not-a-number").header(HttpHeaders.AUTHORIZATION, bearerToken(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder create(User user, String body) {
        return post("/api/v1/schedules")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder update(User user, Long scheduleId, String body) {
        return patch("/api/v1/schedules/{scheduleId}", scheduleId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder deleteSchedule(User user, Long scheduleId) {
        return delete("/api/v1/schedules/{scheduleId}", scheduleId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(user));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder getSchedules(User user, LocalDate date) {
        return get("/api/v1/schedules")
                .param("date", date.toString())
                .header(HttpHeaders.AUTHORIZATION, bearerToken(user));
    }

    private String bearerToken(User user) {
        return "Bearer " + accessTokenIssuer.issueAccessToken(user.getId(), user.getRole());
    }
}
