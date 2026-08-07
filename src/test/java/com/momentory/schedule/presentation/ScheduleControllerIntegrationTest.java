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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

        assertThat(scheduleRepository.findByUserIdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(user.getId(), date))
                .extracting(Schedule::getTitle)
                .containsExactly("운동하기", "독서하기");
    }

    @Test
    void returnsOnlyCurrentUsersActiveSchedulesOrderedByDisplayOrderThenId() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 10);
        Schedule first = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), date, "첫 번째", 0L));
        Schedule sameOrder = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), date, "같은 순서", 0L));
        Schedule later = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), date, "나중 순서", 1L));
        Schedule deleted = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), date, "삭제됨", 2L));
        deleted.delete(java.time.Instant.now());
        scheduleRepository.saveAndFlush(deleted);
        scheduleRepository.saveAndFlush(Schedule.createManual(anotherUser.getId(), date, "다른 사용자", 0L));

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
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "운동하기", 0L));
        Schedule deleted = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "삭제된 일정", 1L));
        Schedule external = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "외부 일정", 2L));
        deleted.delete(java.time.Instant.now());
        scheduleRepository.saveAndFlush(deleted);
        jdbcTemplate.update("UPDATE schedules SET external_id = ? WHERE id = ?", "external-1", external.getId());

        mockMvc.perform(update(user, schedule.getId(), "{\"date\":\"2026-08-11\",\"title\":\" 저녁 운동하기 \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-08-11"))
                .andExpect(jsonPath("$.title").value("저녁 운동하기"));
        mockMvc.perform(update(anotherUser, schedule.getId(), "{\"date\":\"2026-08-11\",\"title\":\"변경\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정을 찾을 수 없습니다."));
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
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "운동하기", 0L));

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
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        mockMvc.perform(delete("/api/v1/schedules/not-a-number").header(HttpHeaders.AUTHORIZATION, bearerToken(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void completesScheduleWithEmotionAndReturnsItInDailyScheduleList() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 10);
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), date, "운동하기", 0L));

        mockMvc.perform(changeCompletion(user, schedule.getId(), "{\"completed\":true,\"emotion\":\"PROUD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(schedule.getId()))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.emotion").value("PROUD"));

        Schedule completed = scheduleRepository.findById(schedule.getId()).orElseThrow();
        assertThat(completed.isCompleted()).isTrue();
        assertThat(completed.getEmotion()).isEqualTo(com.momentory.schedule.domain.ScheduleEmotion.PROUD);
        mockMvc.perform(getSchedules(user, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedules[0].completed").value(true))
                .andExpect(jsonPath("$.schedules[0].emotion").value("PROUD"));
    }

    @Test
    void completesScheduleWithoutEmotionAndChangesEmotionAfterCompletion() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "운동하기", 0L));

        mockMvc.perform(changeCompletion(user, schedule.getId(), "{\"completed\":true,\"emotion\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
        assertThat(scheduleRepository.findById(schedule.getId()).orElseThrow().getEmotion()).isNull();

        mockMvc.perform(changeCompletion(user, schedule.getId(), "{\"completed\":true,\"emotion\":\"HAPPY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emotion").value("HAPPY"));
        assertThat(scheduleRepository.findById(schedule.getId()).orElseThrow().getEmotion())
                .isEqualTo(com.momentory.schedule.domain.ScheduleEmotion.HAPPY);
    }

    @Test
    void cancellingCompletionClearsEmotion() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "운동하기", 0L));
        mockMvc.perform(changeCompletion(user, schedule.getId(), "{\"completed\":true,\"emotion\":\"PROUD\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(changeCompletion(user, schedule.getId(), "{\"completed\":false,\"emotion\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false));

        Schedule cancelled = scheduleRepository.findById(schedule.getId()).orElseThrow();
        assertThat(cancelled.isCompleted()).isFalse();
        assertThat(cancelled.getEmotion()).isNull();
    }

    @Test
    void rejectsEmotionForIncompleteScheduleAndUnsupportedEmotionCode() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "운동하기", 0L));

        mockMvc.perform(changeCompletion(user, schedule.getId(), "{\"completed\":false,\"emotion\":\"PROUD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("미완료 상태에서는 감정을 선택할 수 없습니다."));
        mockMvc.perform(changeCompletion(user, schedule.getId(), "{\"completed\":true,\"emotion\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        Schedule unchanged = scheduleRepository.findById(schedule.getId()).orElseThrow();
        assertThat(unchanged.isCompleted()).isFalse();
        assertThat(unchanged.getEmotion()).isNull();
    }

    @Test
    void blocksCompletionChangeForAnotherUsersOrDeletedSchedule() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "운동하기", 0L));
        Schedule deleted = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "삭제됨", 1L));
        deleted.delete(java.time.Instant.now());
        scheduleRepository.saveAndFlush(deleted);

        mockMvc.perform(changeCompletion(anotherUser, schedule.getId(), "{\"completed\":true,\"emotion\":\"PROUD\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
        mockMvc.perform(changeCompletion(user, deleted.getId(), "{\"completed\":true,\"emotion\":\"PROUD\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
    }

    @Test
    void completesExternalScheduleWithEmotion() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        Schedule external = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "외부 일정", 0L));
        jdbcTemplate.update("UPDATE schedules SET external_id = ? WHERE id = ?", "external-1", external.getId());

        mockMvc.perform(changeCompletion(user, external.getId(), "{\"completed\":true,\"emotion\":\"CALM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emotion").value("CALM"));
        assertThat(scheduleRepository.findById(external.getId()).orElseThrow().getEmotion())
                .isEqualTo(com.momentory.schedule.domain.ScheduleEmotion.CALM);
    }

    @Test
    void changesScheduleOrderAndDailyLookupUsesChangedOrder() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 10);
        Schedule first = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), date, "첫 번째", 0L));
        Schedule second = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), date, "두 번째", 1L));
        Schedule third = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), date, "세 번째", 2L));

        mockMvc.perform(changeOrder(user, "{\"date\":\"2026-08-10\",\"scheduleIds\":[%d,%d,%d]}".formatted(third.getId(), first.getId(), second.getId())))
                .andExpect(status().isNoContent());

        assertThat(scheduleRepository.findById(third.getId()).orElseThrow().getDisplayOrder()).isZero();
        assertThat(scheduleRepository.findById(first.getId()).orElseThrow().getDisplayOrder()).isEqualTo(1L);
        assertThat(scheduleRepository.findById(second.getId()).orElseThrow().getDisplayOrder()).isEqualTo(2L);
        mockMvc.perform(getSchedules(user, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedules[0].id").value(third.getId()))
                .andExpect(jsonPath("$.schedules[1].id").value(first.getId()))
                .andExpect(jsonPath("$.schedules[2].id").value(second.getId()));
    }

    @Test
    void rejectsDuplicateOrMissingScheduleIdsWhenChangingOrder() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        Schedule first = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "첫 번째", 0L));
        Schedule second = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "두 번째", 1L));

        mockMvc.perform(changeOrder(user, "{\"date\":\"2026-08-10\",\"scheduleIds\":[%d,%d,%d]}".formatted(first.getId(), first.getId(), second.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("일정 순서 변경 요청이 올바르지 않습니다."));
        mockMvc.perform(changeOrder(user, "{\"date\":\"2026-08-10\",\"scheduleIds\":[%d]}".formatted(first.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNonexistentAndDifferentDateScheduleIdsWhenChangingOrder() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        Schedule schedule = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "첫 번째", 0L));
        Schedule anotherDate = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 11), "다른 날짜", 0L));

        mockMvc.perform(changeOrder(user, "{\"date\":\"2026-08-10\",\"scheduleIds\":[%d,999999]}".formatted(schedule.getId())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(changeOrder(user, "{\"date\":\"2026-08-10\",\"scheduleIds\":[%d,%d]}".formatted(schedule.getId(), anotherDate.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAnotherUsersAndDeletedSchedulesWhenChangingOrder() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        Schedule owned = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "내 일정", 0L));
        Schedule anotherUsers = scheduleRepository.saveAndFlush(Schedule.createManual(anotherUser.getId(), LocalDate.of(2026, 8, 10), "다른 사용자", 0L));
        Schedule deleted = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "삭제됨", 1L));
        deleted.delete(java.time.Instant.now());
        scheduleRepository.saveAndFlush(deleted);

        mockMvc.perform(changeOrder(user, "{\"date\":\"2026-08-10\",\"scheduleIds\":[%d,%d]}".formatted(owned.getId(), anotherUsers.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
        mockMvc.perform(changeOrder(user, "{\"date\":\"2026-08-10\",\"scheduleIds\":[%d,%d]}".formatted(owned.getId(), deleted.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void keepsAllDisplayOrdersWhenOrderValidationFails() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        Schedule first = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "첫 번째", 0L));
        Schedule second = scheduleRepository.saveAndFlush(Schedule.createManual(user.getId(), LocalDate.of(2026, 8, 10), "두 번째", 1L));

        mockMvc.perform(changeOrder(user, "{\"date\":\"2026-08-10\",\"scheduleIds\":[%d,999999]}".formatted(second.getId())))
                .andExpect(status().isBadRequest());

        assertThat(scheduleRepository.findById(first.getId()).orElseThrow().getDisplayOrder()).isZero();
        assertThat(scheduleRepository.findById(second.getId()).orElseThrow().getDisplayOrder()).isEqualTo(1L);
    }

    @Test
    void returnsAuthenticationRequiredForDeletedAuthenticatedUserAcrossScheduleUseCases() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        String token = bearerToken(user);
        userRepository.deleteById(user.getId());
        LocalDate date = LocalDate.of(2026, 8, 10);

        mockMvc.perform(get("/api/v1/schedules").param("date", date.toString()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
        mockMvc.perform(post("/api/v1/schedules").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-08-10\",\"title\":\"운동하기\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/v1/schedules/{scheduleId}", 1L).header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"date\":\"2026-08-10\",\"title\":\"운동하기\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/schedules/{scheduleId}", 1L).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/schedules/{scheduleId}/completion", 1L).header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"completed\":true,\"emotion\":\"PROUD\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/v1/schedules/order").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"date\":\"2026-08-10\",\"scheduleIds\":[1]}"))
                .andExpect(status().isUnauthorized());

        assertThat(scheduleRepository.findByUserIdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(user.getId(), date))
                .isEmpty();
    }

    @Test
    void documentsCompletionAndOrderEndpointsInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/schedules/{scheduleId}/completion'].put.responses.200.content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ScheduleCompletionResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/schedules/order'].patch.responses.204").exists())
                .andExpect(jsonPath("$.paths['/api/v1/schedules/order'].patch.responses.400.content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ApiErrorResponse"));
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

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder changeCompletion(User user, Long scheduleId, String body) {
        return put("/api/v1/schedules/{scheduleId}/completion", scheduleId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder changeOrder(User user, String body) {
        return patch("/api/v1/schedules/order")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
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
