package com.icoder0.websocket.core.utils;

import com.google.common.primitives.Bytes;
import lombok.experimental.UtilityClass;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * @author bofa1ex
 * @description 对称加密算法工具类
 * @since 2020/3/20
 */
@UtilityClass
public class AESUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * CBC 工作模式, 一个随机数作为iv参数, 这样即使是同一份明文, 每次生成的密文都不同.
     *
     * @param data      原文内容
     * @param key       密钥
     * @param isPadding 填充模式
     *
     * @return aes加密后的数据
     */
    public byte[] encrypt_cbc(byte[] data, byte[] key, boolean isPadding) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            // CBC模式需要生成一个16 bytes的initialization vector:
            SecureRandom sr = SecureRandom.getInstanceStrong();
            byte[] iv = sr.generateSeed(16);
            IvParameterSpec ivps = new IvParameterSpec(iv);
            Cipher cipher;
            if (isPadding) {
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", BouncyCastleProvider.PROVIDER_NAME);
            } else {
                cipher = Cipher.getInstance("AES/CBC/NoPadding");
            }
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivps);
            byte[] result = cipher.doFinal(data);
            // IV不需要保密，把IV和密文一起返回:
            return Bytes.concat(iv, result);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * CBC 工作模式, 一个随机数作为iv参数, 这样即使是同一份明文, 每次生成的密文都不同.
     *
     * @param data      加密内容
     * @param key       密钥
     * @param isPadding 填充模式
     *
     * @return aes解密后的数据
     */
    public byte[] decrypt_cbc(byte[] data, byte[] key, boolean isPadding) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            byte[] iv = Arrays.copyOfRange(data, 0, 16);
            byte[] realData = Arrays.copyOfRange(data, 16, data.length);
            IvParameterSpec ivps = new IvParameterSpec(iv);
            Cipher cipher;
            if (isPadding) {
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", BouncyCastleProvider.PROVIDER_NAME);
            } else {
                cipher = Cipher.getInstance("AES/CBC/NoPadding");
            }
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivps);
            return cipher.doFinal(realData);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * ECB 工作模式, 最简单的AES加密模式，它只需要一个固定长度的密钥，固定的明文会生成固定的密文
     *
     * @param data      原文内容
     * @param key       密钥
     * @param isPadding 填充模式
     *
     * @return aes加密后的数据
     */
    public byte[] encrypt_ecb(byte[] data, byte[] key, boolean isPadding) {
        try {
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher cipher;
            if (isPadding) {
                cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", BouncyCastleProvider.PROVIDER_NAME);
            } else {
                cipher = Cipher.getInstance("AES/ECB/NoPadding");
            }
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * ECB 工作模式, 最简单的AES加密模式，它只需要一个固定长度的密钥，固定的明文会生成固定的密文
     *
     * @param data      加密内容
     * @param key       密钥
     * @param isPadding 填充模式
     *
     * @return aes解密后的数据
     */
    public byte[] decrypt_ecb(byte[] data, byte[] key, boolean isPadding) {
        try {
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher cipher;
            if (isPadding) {
                cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", BouncyCastleProvider.PROVIDER_NAME);
            } else {
                cipher = Cipher.getInstance("AES/ECB/NoPadding");
            }
            cipher.init(Cipher.DECRYPT_MODE, spec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
