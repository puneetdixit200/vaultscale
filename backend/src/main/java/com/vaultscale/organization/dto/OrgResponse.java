package com.vaultscale.organization.dto;

import lombok.*;
import java.util.UUID;

// What we send back to the client after creating/fetching an org
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrgResponse {
    private UUID id;
    private String name;
    private String slug;
    private String yourRole;   // the CURRENT logged-in user's role in this org
}
