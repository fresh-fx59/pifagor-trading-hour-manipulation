package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * https://medium.com/coderbyte/using-resttemplate-with-client-certificates-a25feb2d9918
 */
@Service
public class CallMtlsApiService {
    private final RestTemplate purchaseRestTemplate;

    @Value("${external.purchapi.username}")
    private String username;

    @Value("${external.purchapi.password}")
    private String password;

    // Other fields and constructors...

    @Autowired
    public CallMtlsApiService(@Qualifier("mtlsRestTemplate") RestTemplate restClientConfig) {
        this.purchaseRestTemplate = restClientConfig;
    }
}
