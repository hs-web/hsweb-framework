package org.hswebframework.web.service.authorization.simple.totp;

public class HexEncoding {

    /** Hidden constructor to prevent instantiation. */
    private HexEncoding() {}

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Encodes the provided data as a hexadecimal string.
     */
    public static String encode(byte[] data) {
        StringBuilder result = new StringBuilder(data.length * 2);
        for (byte b : data) {
            result.append(HEX_DIGITS[(b >>> 4) & 0x0f]);
            result.append(HEX_DIGITS[b & 0x0f]);
        }
        return result.toString();
    }

    /**
     * Decodes the provided hexadecimal string into an array of bytes.
     */
    public static byte[] decode(String encoded) {
        // IMPLEMENTATION NOTE: Special care is taken to permit odd number of hexadecimal digits.
        int resultLengthBytes = (encoded.length() + 1) / 2;
        byte[] result = new byte[resultLengthBytes];
        int resultOffset = 0;
        int encodedCharOffset = 0;
        if ((encoded.length() % 2) != 0) {
            // Odd number of digits -- the first digit is the lower 4 bits of the first result byte.
            result[resultOffset++] = (byte) getHexadecimalDigitValue(encoded.charAt(encodedCharOffset));
            encodedCharOffset++;
        }
        for (int len = encoded.length(); encodedCharOffset < len; encodedCharOffset += 2) {
            result[resultOffset++] = (byte)
                    ((getHexadecimalDigitValue(encoded.charAt(encodedCharOffset)) << 4)
                            | getHexadecimalDigitValue(encoded.charAt(encodedCharOffset + 1)));
        }
        return result;
    }

    private static int getHexadecimalDigitValue(char c) {
        if ((c >= 'a') && (c <= 'f')) {
            return (c - 'a') + 0x0a;
        } else if ((c >= 'A') && (c <= 'F')) {
            return (c - 'A') + 0x0a;
        } else if ((c >= '0') && (c <= '9')) {
            return c - '0';
        } else {
            throw new IllegalArgumentException(
                    "Invalid hexadecimal digit at position : '" + c + "' (0x" + Integer.toHexString(c) + ")");
        }
    }
}