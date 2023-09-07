package br.psi.giganet.stockapi.common.utils;

import java.nio.charset.StandardCharsets;

public class DecoderUtil {

    public static String toUTF8(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        return new String(bytes, StandardCharsets.UTF_8)
                .replaceAll("\\\\", "")
                .replaceAll("%5C", "");
    }
}
