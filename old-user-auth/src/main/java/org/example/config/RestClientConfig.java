package org.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * https://medium.com/coderbyte/using-resttemplate-with-client-certificates-a25feb2d9918
 **/
@Configuration
public class RestClientConfig {

    @Value("${ssl.client.keystore.path}")
    private String keyStoreResourcePath;

    @Value("${ssl.client.truststore.path}")
    private String trustStoreResourcePath;

    @Value("${ssl.client.keystore.password}")
    private String keyStorePassword;


    @Bean
    @Qualifier("mtlsRestTemplate")
    public RestTemplate restTemplate() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");

            // Load client certificate and private key
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            char[] keyStorePasswordArray = keyStorePassword.toCharArray();
            Resource keyStoreResource = new FileSystemResource(keyStoreResourcePath);
            URL keyStoreUrl = keyStoreResource.getURL();
            if (keyStoreUrl == null) {
                throw new FileNotFoundException("Keystore file not found on classpath");
            }
            keyStore.load(keyStoreUrl.openStream(), keyStorePasswordArray);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePasswordArray);

            // Load trust store
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Resource trustStoreResource = new FileSystemResource(trustStoreResourcePath);
            URL trustStoreUrl = trustStoreResource.getURL();
            if (trustStoreUrl == null) {
                throw new FileNotFoundException("Truststore file not found on classpath");
            }
            trustStore.load(trustStoreUrl.openStream(), keyStorePasswordArray);

            // Initialize TrustManagerFactory with the trust store
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Initialize SSL context
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

            return new RestTemplate(new CustomRequestFactory(sslContext));
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | KeyStoreException |
                 IOException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    // Other configurations...
    private static class CustomRequestFactory extends org.springframework.http.client.SimpleClientHttpRequestFactory {

        private final SSLContext sslContext;

        public CustomRequestFactory(SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        @Override
        protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws IOException {
            if (connection instanceof javax.net.ssl.HttpsURLConnection) {
                ((javax.net.ssl.HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                //((javax.net.ssl.HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);// In a secure production environment, hostname verification should be enabled to ensure that the server being accessed is the intended one and to prevent potential security vulnerabilities
            }
            super.prepareConnection(connection, httpMethod);
        }
    }

}
