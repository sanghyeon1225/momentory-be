package com.momentory.auth.security;

import com.momentory.auth.token.application.AccessTokenIssuer;
import com.momentory.auth.security.JwtProperties;
import com.momentory.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(properties = {
        "JWT_SECRET=JZP9amP0y2bXk2LG9f9piS5jH3vK9B5w7qxgEriqMA4=",
        "JWT_REFRESH_EXPIRATION=30d",
        "KAKAO_APP_ID=123456789"
})
@Import(AuthenticationTestConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class JwtAuthenticationIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg17")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AccessTokenIssuer accessTokenIssuer;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void publicHealthEndpointIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/test-auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void validTokenAuthenticatesAndInjectsLoginPrincipal() throws Exception {
        String accessToken = accessTokenIssuer.issueAccessToken(42L, UserRole.USER);

        mockMvc.perform(get("/test-auth/me").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        String expiredToken = issueToken(jwtProperties.issuer(), jwtProperties.audience(), Instant.now().minusSeconds(120));

        expectAuthenticationRequired(expiredToken);
    }

    @Test
    void tamperedTokenReturns401() throws Exception {
        String token = accessTokenIssuer.issueAccessToken(42L, UserRole.USER);
        String[] parts = token.split("\\.");
        String signature = parts[2];
        char firstCharacter = signature.charAt(0);
        String tamperedSignature = (firstCharacter == 'A' ? 'B' : 'A') + signature.substring(1);
        String tamperedToken = parts[0] + "." + parts[1] + "." + tamperedSignature;

        expectAuthenticationRequired(tamperedToken);
    }

    @Test
    void tokenWithDifferentIssuerReturns401() throws Exception {
        String token = issueToken("another-issuer", jwtProperties.audience(), Instant.now().plusSeconds(300));

        expectAuthenticationRequired(token);
    }

    @Test
    void tokenWithDifferentAudienceReturns401() throws Exception {
        String token = issueToken(jwtProperties.issuer(), "another-audience", Instant.now().plusSeconds(300));

        expectAuthenticationRequired(token);
    }

    @Test
    void userTokenCannotAccessAdminEndpoint() throws Exception {
        String accessToken = accessTokenIssuer.issueAccessToken(42L, UserRole.USER);

        mockMvc.perform(get("/test-auth/admin").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    private void expectAuthenticationRequired(String token) throws Exception {
        mockMvc.perform(get("/test-auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    private String issueToken(String issuer, String audience, Instant expiresAt) {
        Instant now = Instant.now();
        Instant issuedAt = expiresAt.isBefore(now) ? expiresAt.minusSeconds(60) : now.minusSeconds(60);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("42")
                .claim("role", UserRole.USER.name())
                .issuer(issuer)
                .audience(List.of(audience))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
