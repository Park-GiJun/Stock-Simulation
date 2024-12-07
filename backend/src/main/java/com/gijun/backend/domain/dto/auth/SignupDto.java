package com.gijun.backend.domain.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SignupDto {

    @Getter
    @NoArgsConstructor
    public static class SignupRequest {
        private String userId;
        private String password;
        private String username;
        private String email;

        @Builder
        public SignupRequest(String userId, String password, String username, String email) {
            this.userId = userId;
            this.password = password;
            this.username = username;
            this.email = email;
        }
    }

    @Getter
    @Builder
    public static class SignupResponse {
        private String userId;
        private String username;
    }
}