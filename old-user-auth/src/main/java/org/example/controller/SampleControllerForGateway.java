package org.example.controller;

import org.example.tool.CertificateLoader;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.Certificate;
import java.util.Base64;

import static org.example.tool.CertificateLoader.encodeCertificate;

@RestController
public class SampleControllerForGateway {
        @GetMapping("/proxy")
        public ResponseEntity<?> proxy(ProxyExchange<byte[]> proxy) throws Exception {
            String certificatePath = "/Users/a/Documents/projects/keystore/mtls-certs/clientBob.p12";
            String certificatePassword = "gBnf1dbV!d";
            CertificateLoader certificateLoader = new CertificateLoader();
            Certificate certificate = certificateLoader.loadCertificate(certificatePath, certificatePassword, "clientbob");
            String encodedCert = encodeCertificate(certificate);
            return proxy
                    .uri("https://localhost:8444/api/sample")
                    .header("SSL_CLIENT_CERT", encodedCert)
                    .get();
        }
}
