package org.hswebframework.web.service.authorization.simple.totp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 */
public class TotpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TotpUtil.class);

    private TotpUtil() {
    }

    /**
     * This method uses the JCE to provide the crypto algorithm.
     * HMAC computes a Hashed Message Authentication Code with the
     * crypto hash algorithm as a parameter.
     *
     * @param crypto:   the crypto algorithm (HmacSHA1, HmacSHA256,
     *                  HmacSHA512)
     * @param keyBytes: the bytes to use for the HMAC key
     * @param text:     the message or text to be authenticated
     */
    private static byte[] hmac_sha(String crypto, byte[] keyBytes,
                                   byte[] text) {
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey =
                    new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    /**
     * This method converts a HEX string to Byte[]
     *
     * @param hex: the HEX string
     * @return: a byte array
     */
    private static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++)
            ret[i] = bArray[i + 1];
        return ret;
    }

    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    /**
     * This method generates a TOTP value for the given
     * set of parameters.
     *
     * @param key:          the shared secret, HEX encoded
     * @param time:         a value that reflects a time
     * @param returnDigits: number of digits to return
     * @return: a numeric String in base 10 that includes truncationDigits digits
     */
    public static String generateTOTP(String key,
                                      String time,
                                      String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA1");
    }

    /**
     * This method generates a TOTP value for the given
     * set of parameters.
     *
     * @param key:          the shared secret, HEX encoded
     * @param time:         a value that reflects a time
     * @param returnDigits: number of digits to return
     * @return: a numeric String in base 10 that includes truncationDigits digits
     */
    public static String generateTOTP256(String key,
                                         String time,
                                         String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA256");
    }

    /**
     * This method generates a TOTP value for the given
     * set of parameters.
     *
     * @param key:          the shared secret, HEX encoded
     * @param time:         a value that reflects a time
     * @param returnDigits: number of digits to return
     * @return: a numeric String in base 10 that includes truncationDigits digits
     */
    public static String generateTOTP512(String key,
                                         String time,
                                         String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA512");
    }

    /**
     * This method generates a TOTP value for the given
     * set of parameters.
     *
     * @param key:          the shared secret, HEX encoded
     * @param time:         a value that reflects a time
     * @param returnDigits: number of digits to return
     * @param crypto:       the crypto function to use
     * @return: a numeric String in base 10 that includes truncationDigits digits
     */
    public static String generateTOTP(String key,
                                      String time,
                                      String returnDigits,
                                      String crypto) {
        int codeDigits = Integer.decode(returnDigits);
        StringBuilder result;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        StringBuilder timeBuilder = new StringBuilder(time);
        while (timeBuilder.length() < 16)
            timeBuilder.insert(0, "0");
        time = timeBuilder.toString();

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);

        byte[] hash = hmac_sha(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < codeDigits) {
            result.insert(0, "0");
        }
        return result.toString();
    }

    /**
     * 验证动态口令是否正确
     *
     * @param secretBase32 密钥
     * @param code         待验证的动态口令
     * @return
     */
    public static boolean verify(String secretBase32, String code) {
        return generate(secretBase32).equals(code);
    }

    /**
     * 生成totp协议字符串
     *
     * @param accoName
     * @param domain
     * @param secretBase32
     * @return
     */
    public static String generateTotpString(String accoName, String domain, String secretBase32) {
        return "otpauth://totp/" + accoName + "@" + domain + "?secret=" + secretBase32;
    }

    /**
     * 根据密钥生成动态口令
     *
     * @param secretBase32 base32编码格式的密钥
     * @return
     */
    public static String generate(String secretBase32) {

        String secretHex;
        try {
            secretHex = HexEncoding.encode(Base32String.decode(secretBase32));
        } catch (Base32String.DecodingException e) {
            LOGGER.error("解码" + secretBase32 + "出错，", e);
            throw new RuntimeException("解码Base32出错");
        }

        long X = 30;

        StringBuilder steps;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        long currentTime = System.currentTimeMillis() / 1000L;
        try {
            long t = currentTime / X;
            steps = new StringBuilder(Long.toHexString(t).toUpperCase());
            while (steps.length() < 16) steps.insert(0, "0");

            return generateTOTP(secretHex, steps.toString(), "6",
                    "HmacSHA1");
        } catch (final Exception e) {
            LOGGER.error("生成动态口令出错：" + secretBase32, e);
            throw new RuntimeException("生成动态口令出错");
        }
    }

    /**
     * 生成base32编码的随机密钥
     *
     * @param length
     * @return
     */
    public static String getRandomSecretBase32(int length) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length / 2];
        random.nextBytes(salt);
        return Base32String.encode(salt);
    }
}