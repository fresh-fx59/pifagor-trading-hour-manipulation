package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-proxy-exchange.html
 */
@Controller // required to thymeleaf to work
@RequestMapping("/api/sample")
@Validated
@RequiredArgsConstructor
public class SampleController {
    private static final Logger log = LoggerFactory.getLogger(SampleController.class);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping
    public String sample() {
        return "samplePage"; // Thymeleaf template name for login page
    }
}
