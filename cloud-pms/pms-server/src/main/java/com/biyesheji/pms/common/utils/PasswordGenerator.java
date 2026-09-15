package com.biyesheji.pms.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成/校验工具（开发期使用，不参与业务逻辑）
 * <p>
 * 用法：
 * 生成密文：mvn exec:java -Dexec.mainClass=com.biyesheji.pms.common.utils.PasswordGenerator -Dexec.args="admin123"
 * 校验密文：mvn exec:java -Dexec.mainClass=com.biyesheji.pms.common.utils.PasswordGenerator -Dexec.args="verify admin123 密文"
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (args.length >= 3 && "verify".equals(args[0])) {
            String raw = args[1];
            String hash = args[2];
            System.out.println("VERIFY_MATCH=" + encoder.matches(raw, hash));
            return;
        }

        String raw = args.length > 0 ? args[0] : "admin123";
        String hash = encoder.encode(raw);
        System.out.println("RAW=" + raw);
        System.out.println("HASH=" + hash);
        System.out.println("SELF_MATCH=" + encoder.matches(raw, hash));
    }
}
