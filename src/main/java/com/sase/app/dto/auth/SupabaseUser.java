package com.sase.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupabaseUser(
        String id,
        String email,
        @JsonProperty("phone") String phone,
        @JsonProperty("created_at") String createdAt
) {}
