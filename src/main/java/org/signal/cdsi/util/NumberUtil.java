package org.signal.cdsi.util;

public class NumberUtil {
    public static String formatE164(long number) {
        return "+" + String.valueOf(number);
    }

    public static long parseE164(String number) {
        if (!number.startsWith("+")) {
            throw new IllegalArgumentException("E164 must start with +");
        }
        return Long.parseLong(number.substring(1));
    }
}