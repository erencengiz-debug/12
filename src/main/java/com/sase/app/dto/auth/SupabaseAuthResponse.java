package com.sase.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupabaseAuthResponse(
        @JsonProperty("access_token")  String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type")    String tokenType,
        @JsonProperty("expires_in")    Integer expiresIn,
        @JsonProperty("user")          SupabaseUser user
) {}
