
package com.vaultscale.organization;

import com.vaultscale.auth.dto.*;
import com.vaultscale.organization.dto.*;
import com.vaultscale.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationRbacIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    // Helper: registers a user and returns their JWT token
    private String registerAndGetToken(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("secret123");
        req.setFullName("User " + email);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", req, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).as("registration response").isNotNull();
        return response.getBody().getToken();
    }

    // Helper: builds HTTP headers with a Bearer token attached
    private HttpEntity<Object> authEntity(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void nonMember_shouldGet403_whenInvitingToSomeoneElsesOrg() {
        String suffix = UUID.randomUUID().toString().replace("-", "");

        // User A creates an org — becomes OWNER
        String ownerToken = registerAndGetToken("owner+" + suffix + "@vaultscale.com");
        CreateOrgRequest orgRequest = new CreateOrgRequest();
        orgRequest.setName("Owner's Org");
        orgRequest.setSlug("owners-org-" + suffix);

        ResponseEntity<OrgResponse> orgResponse = restTemplate.exchange(
                "/api/v1/orgs", HttpMethod.POST, authEntity(orgRequest, ownerToken), OrgResponse.class
        );
        assertThat(orgResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orgResponse.getBody()).as("organization response").isNotNull();
        String orgId = orgResponse.getBody().getId().toString();

        // User B registers but has NO membership in that org
        String outsiderToken = registerAndGetToken("outsider+" + suffix + "@vaultscale.com");
        InviteMemberRequest inviteRequest = new InviteMemberRequest();
        inviteRequest.setEmail("someone@vaultscale.com");
        inviteRequest.setRole(com.vaultscale.organization.entity.Role.MEMBER);

        // ACT: outsider tries to invite someone into an org they don't belong to
        ResponseEntity<String> result = restTemplate.exchange(
                "/api/v1/orgs/" + orgId + "/members", HttpMethod.POST,
                authEntity(inviteRequest, outsiderToken), String.class
        );

        // ASSERT: must be blocked with 403, proving RBAC works across the full stack
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
