package com.momentory.auth;

import com.momentory.MomentoryApplication;
import com.momentory.auth.application.RefreshTokenIssuer;
import com.momentory.auth.application.RefreshTokenReissueService;
import com.momentory.auth.domain.RefreshToken;
import com.momentory.auth.infrastructure.RefreshTokenRepository;
import com.momentory.user.domain.User;
import com.momentory.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "JWT_SECRET=JZP9amP0y2bXk2LG9f9piS5jH3vK9B5w7qxgEriqMA4=",
        "JWT_REFRESH_EXPIRATION=30d",
        "KAKAO_APP_ID=123456789"
})
@Import(AuthenticationTestConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class RefreshTokenReissueIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg17"));
    @DynamicPropertySource static void db(DynamicPropertyRegistry r) { r.add("spring.datasource.url", POSTGRES::getJdbcUrl); r.add("spring.datasource.username", POSTGRES::getUsername); r.add("spring.datasource.password", POSTGRES::getPassword); }
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @MockitoSpyBean RefreshTokenRepository refreshTokens;
    @Autowired RefreshTokenIssuer issuer;
    @Autowired RefreshTokenReissueService reissueService;
    @Autowired ObjectMapper objectMapper;
    MockMvc mockMvc;
    @BeforeEach void setup() { mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }
    @AfterEach void cleanup() { reset(refreshTokens); refreshTokens.deleteAllInBatch(); users.deleteAllInBatch(); }

    @Test
    void reissuesAndRotatesTokenAndAuthenticatesNewAccessToken() throws Exception {
        User user = users.saveAndFlush(User.create());
        String old = saveToken(user, Instant.now().plusSeconds(3600));
        MvcResult result = mockMvc.perform(reissue(old)).andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer")).andExpect(jsonPath("$.accessTokenExpiresIn").value(1800)).andReturn();
        JsonNode body = body(result); String next = body.get("refreshToken").stringValue(); String access = body.get("accessToken").stringValue();
        List<RefreshToken> stored = refreshTokens.findAll(); String nextHash = sha256(next);
        assertThat(stored).hasSize(2);
        assertThat(stored.stream().filter(RefreshToken::isRevoked).count()).isEqualTo(1);
        assertThat(stored.stream().anyMatch(t -> t.getTokenHash().equals(nextHash))).isTrue();
        assertThat(stored.stream().noneMatch(t -> t.getTokenHash().equals(next))).isTrue();
        mockMvc.perform(get("/test-auth/me").header("Authorization", "Bearer " + access)).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(user.getId())).andExpect(jsonPath("$.role").value("USER"));
        mockMvc.perform(reissue(old)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REVOKED"));
    }

    @Test
    void mapsInvalidExpiredRevokedAndBlankRequestsWithoutLeakingSecrets() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reissue").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        MvcResult invalid = mockMvc.perform(reissue("not-a-real-token")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("REFRESH_TOKEN_INVALID")).andReturn();
        assertThat(invalid.getResponse().getContentAsString()).doesNotContain("not-a-real-token");
        User user = users.saveAndFlush(User.create());
        String expired = saveToken(user, Instant.now().minusSeconds(1));
        mockMvc.perform(reissue(expired)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("REFRESH_TOKEN_EXPIRED"));
        String revoked = saveToken(user, Instant.now().plusSeconds(60));
        RefreshToken token = refreshTokens.findAll().stream().filter(value -> value.getTokenHash().equals(issuer.hash(revoked))).findFirst().orElseThrow(); token.revoke(); refreshTokens.saveAndFlush(token);
        mockMvc.perform(reissue(revoked)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REVOKED"));
        assertThat(refreshTokens.count()).isEqualTo(2);
    }

    @Test
    void rejectsEmptyAndBlankRefreshToken() throws Exception {
        mockMvc.perform(reissue("")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(reissue("   ")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void rollsBackRevocationWhenNewRefreshTokenStorageFails() {
        User user = users.saveAndFlush(User.create());
        String old = saveToken(user, Instant.now().plusSeconds(3600));
        String oldHash = issuer.hash(old);
        doThrow(new org.springframework.dao.DataIntegrityViolationException("simulated save failure"))
                .when(refreshTokens).save(argThat(token -> !token.getTokenHash().equals(oldHash)));

        assertThatThrownBy(() -> reissueService.reissue(old))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);

        reset(refreshTokens);
        RefreshToken persisted = refreshTokens.findAll().getFirst();
        assertThat(persisted.getRevokedAt()).isNull();
        assertThat(refreshTokens.count()).isEqualTo(1);
    }

    @Test
    void allowsOnlyOneConcurrentReissueForSameTokenButDoesNotBlockDifferentTokens() throws Exception {
        User user = users.saveAndFlush(User.create());
        String same = saveToken(user, Instant.now().plusSeconds(3600));
        String other = saveToken(user, Instant.now().plusSeconds(3600));
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            List<Future<Integer>> results = new ArrayList<>();
            for (String value : List.of(same, same, other)) results.add(pool.submit(() -> { start.await(); return mockMvc.perform(reissue(value)).andReturn().getResponse().getStatus(); }));
            start.countDown();
            List<Integer> statuses = new ArrayList<>(); for (Future<Integer> f : results) statuses.add(f.get(15, TimeUnit.SECONDS));
            assertThat(statuses.stream().filter(s -> s == 200).count()).isEqualTo(2);
            assertThat(statuses.stream().filter(s -> s == 401).count()).isEqualTo(1);
        }
        assertThat(refreshTokens.findAll().stream().filter(RefreshToken::isRevoked).count()).isEqualTo(2);
        assertThat(refreshTokens.count()).isEqualTo(4);
    }

    @Test
    void logsOutOnlyRequestedDeviceAndBlocksItsReissueWithoutAccessToken() throws Exception {
        User user = users.saveAndFlush(User.create());
        String currentDevice = saveToken(user, Instant.now().plusSeconds(3600));
        String otherDevice = saveToken(user, Instant.now().plusSeconds(3600));

        mockMvc.perform(logout(currentDevice))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        RefreshToken current = refreshTokens.findAll().stream()
                .filter(token -> token.getTokenHash().equals(issuer.hash(currentDevice)))
                .findFirst().orElseThrow();
        RefreshToken other = refreshTokens.findAll().stream()
                .filter(token -> token.getTokenHash().equals(issuer.hash(otherDevice)))
                .findFirst().orElseThrow();
        assertThat(current.getRevokedAt()).isNotNull();
        assertThat(other.getRevokedAt()).isNull();

        mockMvc.perform(reissue(currentDevice))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REVOKED"));
        mockMvc.perform(reissue(otherDevice)).andExpect(status().isOk());
    }

    @Test
    void logoutIsIdempotentForRevokedExpiredAndUnknownTokens() throws Exception {
        User user = users.saveAndFlush(User.create());
        String revoked = saveToken(user, Instant.now().plusSeconds(3600));
        String expired = saveToken(user, Instant.now().minusSeconds(1));
        RefreshToken revokedToken = refreshTokens.findAll().stream()
                .filter(token -> token.getTokenHash().equals(issuer.hash(revoked)))
                .findFirst().orElseThrow();
        revokedToken.revoke();
        refreshTokens.saveAndFlush(revokedToken);

        mockMvc.perform(logout(revoked)).andExpect(status().isNoContent());
        mockMvc.perform(logout(expired)).andExpect(status().isNoContent());
        mockMvc.perform(logout("unknown-token")).andExpect(status().isNoContent());

        RefreshToken expiredToken = refreshTokens.findAll().stream()
                .filter(token -> token.getTokenHash().equals(issuer.hash(expired)))
                .findFirst().orElseThrow();
        assertThat(expiredToken.getRevokedAt()).isNotNull();
        assertThat(refreshTokens.count()).isEqualTo(2);
    }

    @Test
    void rejectsMissingEmptyAndBlankLogoutRefreshTokenWithoutLeakingIt() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        MvcResult empty = mockMvc.perform(logout(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andReturn();
        MvcResult blank = mockMvc.perform(logout("   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andReturn();
        assertThat(empty.getResponse().getContentAsString()).doesNotContain(issuer.hash(""));
        assertThat(blank.getResponse().getContentAsString()).doesNotContain(issuer.hash("   "));
    }

    @Test
    void safelyHandlesConcurrentLogoutForSameToken() throws Exception {
        User user = users.saveAndFlush(User.create());
        String raw = saveToken(user, Instant.now().plusSeconds(3600));
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            List<Future<Integer>> results = new ArrayList<>();
            for (int index = 0; index < 2; index++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return mockMvc.perform(logout(raw)).andReturn().getResponse().getStatus();
                }));
            }
            start.countDown();
            for (Future<Integer> result : results) assertThat(result.get(15, TimeUnit.SECONDS)).isEqualTo(204);
        }

        RefreshToken token = refreshTokens.findAll().getFirst();
        assertThat(token.getRevokedAt()).isNotNull();
        assertThat(refreshTokens.count()).isEqualTo(1);
    }

    private String saveToken(User user, Instant expiresAt) { String raw = "refresh-" + java.util.UUID.randomUUID(); refreshTokens.saveAndFlush(RefreshToken.create(user, issuer.hash(raw), expiresAt)); return raw; }
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder reissue(String token) { return post("/api/v1/auth/reissue").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"%s\"}".formatted(token)); }
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder logout(String token) { return post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"%s\"}".formatted(token)); }
    private JsonNode body(MvcResult result) throws Exception { return objectMapper.readTree(result.getResponse().getContentAsString()); }
    private String sha256(String value) throws Exception { return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
}
