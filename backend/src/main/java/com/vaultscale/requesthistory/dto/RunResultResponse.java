package com.vaultscale.requesthistory.dto;

import lombok.*;

// What we send back to the client immediately after running a request
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunResultResponse {
    private Integer statusCode;
    private String responseBody;
    private long responseTimeMs;
    private String errorMessage; // null if successful
}
