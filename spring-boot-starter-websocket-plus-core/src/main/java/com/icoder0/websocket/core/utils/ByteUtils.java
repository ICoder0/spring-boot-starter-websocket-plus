package com.icoder0.websocket.core.utils;

import lombok.experimental.UtilityClass;

/**
 * @author bofa1ex
 * @since 2020/7/14
 */
@UtilityClass
public class ByteUtils {

    private final String HEX_CHAR = "0123456789ABCDEF";

    public String bytes2Hex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final StringBuilder stringBuffer = new StringBuilder();
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                stringBuffer.append("0");
            }
            stringBuffer.append(hex);
        }
        return stringBuffer.toString().toUpperCase();
    }

    public byte[] hex2Bytes(String hex) {
        if (hex == null) {
            return null;
        }
        hex = hex.toUpperCase();
        int length = hex.length() / 2;
        byte[] dst = new byte[length];
        final char[] hexChars = hex.toCharArray();
        for (int i = 0, pos; i < hexChars.length / 2; i++) {
            pos = i * 2;
            dst[i] = (byte) (char2Byte(hexChars[pos]) << 4 | char2Byte(hexChars[pos + 1]));
        }
        return dst;
    }

    public byte char2Byte(char c) {
        return (byte) HEX_CHAR.indexOf(c);
    }
}
