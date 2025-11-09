package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class Encryptor {

    private final String secret;

    public Encryptor(@Value("${encryption.secret}") String secret) {
        this.secret = secret;
    }


    public String encrypt(String toEncode) {
        return Base64.getEncoder().encodeToString(
                (toEncode + secret).getBytes(StandardCharsets.UTF_8)
        );
    }


    public String decrypt(String toDecode) {
        byte[] bytes = Base64.getDecoder().decode(toDecode.getBytes(StandardCharsets.UTF_8));
        var result = new String(bytes);

        return result.replace(secret, "");
    }
}
