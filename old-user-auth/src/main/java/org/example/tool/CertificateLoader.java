package org.example.tool;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Base64;

public class CertificateLoader {
    public Certificate loadCertificate(String keystorePath, String password, String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, password.toCharArray());
        }
        return keyStore.getCertificate(alias);
    }

    public static String encodeCertificate(Certificate certificate) throws Exception {
        byte[] encoded = certificate.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }
}
