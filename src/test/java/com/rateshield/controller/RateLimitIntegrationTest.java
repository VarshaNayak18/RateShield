package com.rateshield.controller;

import com.rateshield.entity.ApiKey;
import com.rateshield.entity.EndpointRateLimitPolicy;
import com.rateshield.entity.RateLimitPolicy;
import com.rateshield.entity.Role;
import com.rateshield.entity.RoleName;
import com.rateshield.entity.User;
import com.rateshield.repository.ApiKeyRepository;
import com.rateshield.repository.EndpointRateLimitPolicyRepository;
import com.rateshield.repository.RateLimitPolicyRepository;
import com.rateshield.repository.RoleRepository;
import com.rateshield.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class RateLimitIntegrationTest {

    private static final String RAW_API_KEY =
        "rs_live_test_integration_key_123456789";
    
    @Autowired
private StringRedisTemplate redisTemplate;

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate =
            new TestRestTemplate();

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RateLimitPolicyRepository policyRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private EndpointRateLimitPolicyRepository endpointPolicyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {

        redisTemplate.getConnectionFactory()
        .getConnection()
        .serverCommands()
        .flushDb();

        endpointPolicyRepository.deleteAll();
        apiKeyRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        policyRepository.deleteAll();

        Role developerRole = new Role();
        developerRole.setName(RoleName.DEVELOPER);
        developerRole = roleRepository.save(developerRole);

        User user = new User();
        user.setUsername("integration-user");
        user.setEmail("integration@example.com");
        user.setPassword(
                passwordEncoder.encode("Test12345")
        );
        user.setRole(developerRole);
        user = userRepository.save(user);

        RateLimitPolicy policy = new RateLimitPolicy();
        policy.setName("INTEGRATION");
        policy.setMaxRequests(5);
        policy.setWindowSeconds(60L);
        policy.setActive(true);
        policy = policyRepository.save(policy);

        ApiKey apiKey = new ApiKey();

        apiKey.setKeyPrefix(
                RAW_API_KEY.substring(
                        0,
                        Math.min(16, RAW_API_KEY.length())
                )
        );

        apiKey.setKeyHash(
                passwordEncoder.encode(RAW_API_KEY)
        );

        apiKey.setActive(true);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(
                LocalDateTime.now().plusDays(1)
        );
        apiKey.setUser(user);
        apiKey.setRateLimitPolicy(policy);

        apiKeyRepository.save(apiKey);

        EndpointRateLimitPolicy endpointPolicy =
                new EndpointRateLimitPolicy();

        endpointPolicy.setHttpMethod("GET");
        endpointPolicy.setPathPattern("/api/rate-test");
        endpointPolicy.setRateLimitPolicy(policy);
        endpointPolicy.setActive(true);

        endpointPolicyRepository.save(endpointPolicy);
    }

    @Test
    void shouldCallRateLimitEndpoint() {

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "X-API-Key",
                RAW_API_KEY
        );

        HttpEntity<Void> request =
                new HttpEntity<>(headers);

        String url =
                "http://localhost:"
                        + port
                        + "/api/rate-test";

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        String.class
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );
    }

    @Test
void shouldRejectRequestAfterRateLimitExceeded() {

    HttpHeaders headers = new HttpHeaders();

    headers.set(
            "X-API-Key",
            RAW_API_KEY
    );

    HttpEntity<Void> request =
            new HttpEntity<>(headers);

    String url =
            "http://localhost:"
                    + port
                    + "/api/rate-test";

    for (int i = 1; i <= 5; i++) {

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        String.class
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );
    }

    ResponseEntity<String> blockedResponse =
            restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

    assertEquals(
            429,
            blockedResponse.getStatusCode().value()
    );
}
}