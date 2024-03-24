package org.example.shortlink.admin.toolkit;

import java.security.SecureRandom;

/**
 * 分组ID随机生成器
 */
public class RandomGenerator {
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 随机字符串生成
     * @return
     */
    public static String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            codeBuilder.append(randomChar);
        }
        return codeBuilder.toString();
    }
}