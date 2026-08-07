package com.momentory.memo.presentation;

import com.momentory.auth.token.application.AccessTokenIssuer;
import com.momentory.memo.domain.DailyMemo;
import com.momentory.memo.infrastructure.DailyMemoRepository;
import com.momentory.user.domain.User;
import com.momentory.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(properties = {
        "JWT_SECRET=JZP9amP0y2bXk2LG9f9piS5jH3vK9B5w7qxgEriqMA4=",
        "JWT_REFRESH_EXPIRATION=30d",
        "KAKAO_APP_ID=123456789"
})
@Testcontainers(disabledWithoutDocker = true)
class DailyMemoControllerIntegrationTest {

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
    @Autowired DailyMemoRepository dailyMemoRepository;
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
        dailyMemoRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void getsOnlyCurrentUsersMemoAndReturnsNotFoundWhenNoMemoExists() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 7);
        dailyMemoRepository.saveAndFlush(DailyMemo.create(user.getId(), date, "내 메모"));

        mockMvc.perform(getDailyMemo(user, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-08-07"))
                .andExpect(jsonPath("$.content").value("내 메모"));
        mockMvc.perform(getDailyMemo(anotherUser, date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DAILY_MEMO_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("하루 메모를 찾을 수 없습니다."));
        mockMvc.perform(getDailyMemo(user, date.plusDays(1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DAILY_MEMO_NOT_FOUND"));
    }

    @Test
    void createsUpdatesAndRepeatsPutWithoutDuplicateRows() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 7);

        mockMvc.perform(putDailyMemo(user, date, " 첫 메모 "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-08-07"))
                .andExpect(jsonPath("$.content").value("첫 메모"));
        mockMvc.perform(putDailyMemo(user, date, "수정한 메모"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정한 메모"));
        mockMvc.perform(putDailyMemo(user, date, "수정한 메모"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정한 메모"));

        assertThat(dailyMemoRepository.countByUserIdAndMemoDate(user.getId(), date)).isEqualTo(1);
        assertThat(dailyMemoRepository.findByUserIdAndMemoDate(user.getId(), date).orElseThrow().getContent())
                .isEqualTo("수정한 메모");
    }

    @Test
    void rejectsEmptyOrBlankContent() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 7);

        mockMvc.perform(putDailyMemo(user, date, ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("메모 내용을 입력해주세요."));
        mockMvc.perform(putDailyMemo(user, date, "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("메모 내용을 입력해주세요."));

        assertThat(dailyMemoRepository.countByUserIdAndMemoDate(user.getId(), date)).isZero();
    }

    @Test
    void allowsDifferentDatesForOneUserAndSameDateForDifferentUsers() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 7);

        mockMvc.perform(putDailyMemo(user, date, "첫째 날")).andExpect(status().isOk());
        mockMvc.perform(putDailyMemo(user, date.plusDays(1), "둘째 날")).andExpect(status().isOk());
        mockMvc.perform(putDailyMemo(anotherUser, date, "다른 사용자 메모")).andExpect(status().isOk());

        assertThat(dailyMemoRepository.countByUserIdAndMemoDate(user.getId(), date)).isEqualTo(1);
        assertThat(dailyMemoRepository.countByUserIdAndMemoDate(user.getId(), date.plusDays(1))).isEqualTo(1);
        assertThat(dailyMemoRepository.countByUserIdAndMemoDate(anotherUser.getId(), date)).isEqualTo(1);
    }

    @Test
    void physicallyDeletesOnlyCurrentUsersMemoAndMakesRepeatedDeleteIdempotent() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        User anotherUser = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 7);
        dailyMemoRepository.saveAndFlush(DailyMemo.create(user.getId(), date, "내 메모"));
        dailyMemoRepository.saveAndFlush(DailyMemo.create(anotherUser.getId(), date, "다른 사용자 메모"));

        mockMvc.perform(deleteDailyMemo(anotherUser, date))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertThat(dailyMemoRepository.findByUserIdAndMemoDate(user.getId(), date)).isPresent();
        assertThat(dailyMemoRepository.findByUserIdAndMemoDate(anotherUser.getId(), date)).isEmpty();

        mockMvc.perform(deleteDailyMemo(user, date)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mockMvc.perform(deleteDailyMemo(user, date)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mockMvc.perform(getDailyMemo(user, date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DAILY_MEMO_NOT_FOUND"));
    }

    @Test
    void enforcesUserAndMemoDateUniqueConstraint() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        LocalDate date = LocalDate.of(2026, 8, 7);
        mockMvc.perform(putDailyMemo(user, date, "첫 메모")).andExpect(status().isOk());

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO daily_memos (user_id, memo_date, content, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                user.getId(), date, "중복 메모"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsInvalidMemoDates() throws Exception {
        User user = userRepository.saveAndFlush(User.create());

        mockMvc.perform(put("/api/v1/memos/not-a-date")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"메모\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
    }

    @Test
    void returnsAuthenticationRequiredForDeletedAuthenticatedUserAcrossDailyMemoUseCases() throws Exception {
        User user = userRepository.saveAndFlush(User.create());
        String token = bearerToken(user);
        userRepository.deleteById(user.getId());
        LocalDate date = LocalDate.of(2026, 8, 7);

        mockMvc.perform(get("/api/v1/memos/{date}", date).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
        mockMvc.perform(put("/api/v1/memos/{date}", date).header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"메모\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/memos/{date}", date).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());

        assertThat(dailyMemoRepository.countByUserIdAndMemoDate(user.getId(), date)).isZero();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder getDailyMemo(User user, LocalDate date) {
        return get("/api/v1/memos/{date}", date).header(HttpHeaders.AUTHORIZATION, bearerToken(user));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder putDailyMemo(User user, LocalDate date, String memoContent) {
        return put("/api/v1/memos/{date}", date)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"%s\"}".formatted(memoContent));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder deleteDailyMemo(User user, LocalDate date) {
        return delete("/api/v1/memos/{date}", date).header(HttpHeaders.AUTHORIZATION, bearerToken(user));
    }

    private String bearerToken(User user) {
        return "Bearer " + accessTokenIssuer.issueAccessToken(user.getId(), user.getRole());
    }
}
