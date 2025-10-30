package org.example.service;

import org.example.entity.AppUser;
import org.example.model.AuthDTO;
import org.example.model.AuthUser;

public interface UserService {
    default void registerUser(AuthDTO.RegisterRequest registerRequest) {
        final AppUser appUser = new AppUser(
                registerRequest.username(),
                registerRequest.email(),
                registerRequest.password()
        );

        registerUser(appUser);
    }

    void registerUser(AppUser appUser);
}
