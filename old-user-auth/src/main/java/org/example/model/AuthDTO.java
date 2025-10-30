package org.example.model;

import lombok.Data;

public class AuthDTO {
    public record LoginRequest(String username, String password) {
    }
    public record RegisterRequest(String username, String password, String email) {
    }
    public record Response(String message, String token) {
    }
}
