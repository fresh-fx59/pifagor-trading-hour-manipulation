package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.AuthDTO;
import org.example.model.AuthUser;
import org.example.service.AuthService;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // required to thymeleaf to work
@RequestMapping("/api/secured")
@Validated
@RequiredArgsConstructor
public class SecuredController {
    private static final Logger log = LoggerFactory.getLogger(SecuredController.class);

    @GetMapping
    public String secured() {
        return "securedPage"; // Thymeleaf template name for login page
    }
}
