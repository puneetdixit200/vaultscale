package com.vaultscale.security;

import com.vaultscale.auth.dto.AuthResponse;
import com.vaultscale.auth.dto.RegisterRequest;
import com.vaultscale.organization.dto.CreateOrgRequest;
import com.vaultscale.organization.dto.OrgResponse;
import com.vaultscale.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenantIsolationIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void nestedResourcesCannotBeReparentedThroughAnotherOrganizationsUrl() {
        String suffix = UUID.randomUUID().toString().replace("-", "");

        String tokenA = register("tenant-a-" + suffix + "@vaultscale.com");
        OrgResponse orgA = createOrg(tokenA, "Tenant A", "tenant-a-" + suffix);
        String collectionA = createCollection(tokenA, orgA.getId().toString());
        String endpointA = createEndpoint(tokenA, orgA.getId().toString(), collectionA);

        String tokenB = register("tenant-b-" + suffix + "@vaultscale.com");
        OrgResponse orgB = createOrg(tokenB, "Tenant B", "tenant-b-" + suffix);

        ResponseEntity<String> listThroughWrongOrg = restTemplate.exchange(
                "/api/v1/orgs/" + orgB.getId() + "/collections/" + collectionA + "/endpoints",
                HttpMethod.GET,
                authEntity(null, tokenB),
                String.class
        );
        assertThat(listThroughWrongOrg.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<String> runThroughWrongOrg = restTemplate.exchange(
                "/api/v1/orgs/" + orgB.getId() + "/collections/" + collectionA
                        + "/endpoints/" + endpointA + "/run",
                HttpMethod.POST,
                authEntity(null, tokenB),
                String.class
        );
        assertThat(runThroughWrongOrg.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void authMeRequiresJwt() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/auth/me", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String register(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("secret123");
        request.setFullName("Tenant Test");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getToken();
    }

    private OrgResponse createOrg(String token, String name, String slug) {
        CreateOrgRequest request = new CreateOrgRequest();
        request.setName(name);
        request.setSlug(slug);
        ResponseEntity<OrgResponse> response = restTemplate.exchange(
                "/api/v1/orgs", HttpMethod.POST, authEntity(request, token), OrgResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private String createCollection(String token, String orgId) {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/orgs/" + orgId + "/collections",
                HttpMethod.POST,
                authEntity(Map.of("name", "Tenant Collection", "description", "isolation test"), token),
                Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().get("id").toString();
    }

    private String createEndpoint(String token, String orgId, String collectionId) {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/orgs/" + orgId + "/collections/" + collectionId + "/endpoints",
                HttpMethod.POST,
                authEntity(Map.of("name", "Tenant Endpoint", "method", "GET", "url", "https://example.com"), token),
                Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().get("id").toString();
    }

    private HttpEntity<Object> authEntity(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
