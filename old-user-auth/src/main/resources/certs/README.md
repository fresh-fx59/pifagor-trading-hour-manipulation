# RSA Key Generation Guide

This directory should contain RSA key pairs for JWT token signing. **DO NOT commit actual private keys to version control.**

## Generating RSA Keys

Use OpenSSL to generate a new RSA key pair:

### Generate Private Key (2048-bit)
```bash
openssl genrsa -out private-key.pem 2048
```

### Extract Public Key
```bash
openssl rsa -in private-key.pem -pubout -out public-key.pem
```

### Alternative: Generate with PKCS8 Format
```bash
# Generate private key in PKCS8 format
openssl genpkey -algorithm RSA -out private-key.pem -pkeyopt rsa_keygen_bits:2048

# Extract public key
openssl rsa -pubout -in private-key.pem -out public-key.pem
```

## Required Files

After generation, this directory should contain:
- `private-key.pem` - RSA private key (NEVER commit this)
- `public-key.pem` - RSA public key (can be shared)

## Security Notes

1. **Private keys must remain secret** - They should never be committed to version control
2. **Use strong keys** - Minimum 2048-bit RSA keys
3. **Rotate keys regularly** - Consider key rotation policies for production
4. **Protect file permissions** - `chmod 600 private-key.pem` on Unix systems
5. **Use different keys per environment** - Dev, test, and prod should have separate keys

## Integration

These keys are referenced in `application.properties`:
```properties
rsa.private-key=classpath:certs/private-key.pem
rsa.public-key=classpath:certs/public-key.pem
```

The application will fail to start if these files are missing.
