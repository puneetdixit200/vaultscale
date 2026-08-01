package com.vaultscale.collection.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionResponse {
    private UUID id;
    private String name;
    private String description;
}
