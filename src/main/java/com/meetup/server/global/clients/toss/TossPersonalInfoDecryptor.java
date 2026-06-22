package com.meetup.server.global.clients.toss;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
public class TossPersonalInfoDecryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final String keyBase64;
    private final String aad;

    public TossPersonalInfoDecryptor(
            @Value("${toss.decrypt.key-base64}") String keyBase64,
            @Value("${toss.decrypt.aad}") String aad
    ) {
        this.keyBase64 = keyBase64;
        this.aad = aad;
    }

    public String decryptOrNull(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return null;
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            validateKey(keyBytes);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedValue);

            if (encryptedBytes.length <= IV_LENGTH_BYTE) {
                log.warn("Invalid Toss encrypted value length.");
                return null;
            }

            byte[] iv = Arrays.copyOfRange(encryptedBytes, 0, IV_LENGTH_BYTE);
            byte[] cipherTextWithTag = Arrays.copyOfRange(
                    encryptedBytes,
                    IV_LENGTH_BYTE,
                    encryptedBytes.length
            );

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BIT, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));

            byte[] plainBytes = cipher.doFinal(cipherTextWithTag);

            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to decrypt Toss personal info.");
            return null;
        }
    }

    private void validateKey(byte[] keyBytes) {
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "Invalid Toss decrypt key. AES-256 key must be 32 bytes after Base64 decoding."
            );
        }
    }
}
