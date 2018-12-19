package org.hswebframework.web.service.authorization.simple.totp; /**
 */
import java.util.HashMap;
import java.util.Locale;

/**
 * Encodes arbitrary byte arrays as case-insensitive base-32 strings.
 * <p>
 * The implementation is slightly different than in RFC 4648. During encoding,
 * padding is not added, and during decoding the last incomplete chunk is not
 * taken into account. The result is that multiple strings decode to the same
 * byte array, for example, string of sixteen 7s ("7...7") and seventeen 7s both
 * decode to the same byte array.
 * TODO(sarvar): Revisit this encoding and whether this ambiguity needs fixing.
 *
 * @author sweis@google.com (Steve Weis)
 * @author Neal Gafter
 */
public class Base32String {
    // singleton

    private static final Base32String INSTANCE =
            new Base32String("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"); // RFC 4648/3548

    static Base32String getInstance() {
        return INSTANCE;
    }

    // 32 alpha-numeric characters.
    private String ALPHABET;
    private char[] DIGITS;
    private int MASK;
    private int SHIFT;
    private HashMap<Character, Integer> CHAR_MAP;

    static final String SEPARATOR = "-";

    protected Base32String(String alphabet) {
        this.ALPHABET = alphabet;
        DIGITS = ALPHABET.toCharArray();
        MASK = DIGITS.length - 1;
        SHIFT = Integer.numberOfTrailingZeros(DIGITS.length);
        CHAR_MAP = new HashMap<>();
        for (int i = 0; i < DIGITS.length; i++) {
            CHAR_MAP.put(DIGITS[i], i);
        }
    }

    public static byte[] decode(String encoded) throws DecodingException {
        return getInstance().decodeInternal(encoded);
    }

    protected byte[] decodeInternal(String encoded) throws DecodingException {
        // Remove whitespace and separators
        encoded = encoded.trim().replaceAll(SEPARATOR, "").replaceAll(" ", "");

        // Remove padding. Note: the padding is used as hint to determine how many
        // bits to decode from the last incomplete chunk (which is commented out
        // below, so this may have been wrong to start with).
        encoded = encoded.replaceFirst("[=]*$", "");

        // Canonicalize to all upper case
        encoded = encoded.toUpperCase(Locale.US);
        if (encoded.length() == 0) {
            return new byte[0];
        }
        int encodedLength = encoded.length();
        int outLength = encodedLength * SHIFT / 8;
        byte[] result = new byte[outLength];
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        for (char c : encoded.toCharArray()) {
            if (!CHAR_MAP.containsKey(c)) {
                throw new DecodingException("Illegal character: " + c);
            }
            buffer <<= SHIFT;
            buffer |= CHAR_MAP.get(c) & MASK;
            bitsLeft += SHIFT;
            if (bitsLeft >= 8) {
                result[next++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        // We'll ignore leftover bits for now.
        //
        // if (next != outLength || bitsLeft >= SHIFT) {
        //  throw new DecodingException("Bits left: " + bitsLeft);
        // }
        return result;
    }

    public static String encode(byte[] data) {
        return getInstance().encodeInternal(data);
    }

    protected String encodeInternal(byte[] data) {
        if (data.length == 0) {
            return "";
        }

        // SHIFT is the number of bits per output character, so the length of the
        // output is the length of the input multiplied by 8/SHIFT, rounded up.
        if (data.length >= (1 << 28)) {
            // The computation below will fail, so don't do it.
            throw new IllegalArgumentException();
        }

        int outputLength = (data.length * 8 + SHIFT - 1) / SHIFT;
        StringBuilder result = new StringBuilder(outputLength);

        int buffer = data[0];
        int next = 1;
        int bitsLeft = 8;
        while (bitsLeft > 0 || next < data.length) {
            if (bitsLeft < SHIFT) {
                if (next < data.length) {
                    buffer <<= 8;
                    buffer |= (data[next++] & 0xff);
                    bitsLeft += 8;
                } else {
                    int pad = SHIFT - bitsLeft;
                    buffer <<= pad;
                    bitsLeft += pad;
                }
            }
            int index = MASK & (buffer >> (bitsLeft - SHIFT));
            bitsLeft -= SHIFT;
            result.append(DIGITS[index]);
        }
        return result.toString();
    }

    @Override
    // enforce that this class is a singleton
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static class DecodingException extends Exception {
        public DecodingException(String message) {
            super(message);
        }
    }
}