package com.clinica.api.personal_service.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Sha256PasswordEncoder implements PasswordEncoder {

    private static final HexFormat HEX = HexFormat.of();

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        String normalized = normalize(rawPassword);
        return sha256(normalized);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        String hashed = encode(rawPassword);
        return hashed.equalsIgnoreCase(encodedPassword);
    }

    private static String normalize(CharSequence rawPassword) {
        String value = rawPassword.toString().trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(value, Normalizer.Form.NFC);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
