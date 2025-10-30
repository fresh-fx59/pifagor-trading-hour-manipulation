package config;

import org.example.config.ConfigLoader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class SslConfig {
    public static SSLContext getSslContext() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, KeyManagementException {
        final String keystorePath = ConfigLoader.getRequiredEnv("WS_KEYSTORE_PATH");
        final String keystorePassword = ConfigLoader.getRequiredEnv("WS_KEYSTORE_PASSWORD");

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());

        // Initialize the TrustManager
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Create an SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }
}
