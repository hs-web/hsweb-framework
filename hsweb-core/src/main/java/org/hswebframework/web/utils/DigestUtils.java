package org.hswebframework.web.utils;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;

public class DigestUtils {

    public static final MessageDigest md5 = org.apache.commons.codec.digest.DigestUtils.getMd5Digest();
    public static final MessageDigest sha256 = org.apache.commons.codec.digest.DigestUtils.getSha256Digest();
    public static final MessageDigest sha1 = org.apache.commons.codec.digest.DigestUtils.getSha1Digest();

    public static byte[] md5(byte[] data) {
        return org.apache.commons.codec.digest.DigestUtils.digest(md5, data);
    }

    public static byte[] md5(String str) {
        return md5(str.getBytes());
    }

    public static String md5Hex(String str) {
        return Hex.encodeHexString(md5(str.getBytes()));
    }

    public static byte[] sha256(byte[] data) {
        return org.apache.commons.codec.digest.DigestUtils.digest(sha256, data);
    }

    public static byte[] sha256(String str) {
        return sha256(str.getBytes());
    }

    public static String sha256Hex(String str) {
        return Hex.encodeHexString(sha256(str.getBytes()));
    }

    public static byte[] sha1(byte[] data) {
        return org.apache.commons.codec.digest.DigestUtils.digest(sha1, data);
    }

    public static byte[] sha1(String str) {
        return sha1(str.getBytes());
    }

    public static String sha1Hex(String str) {
        return Hex.encodeHexString(sha1(str.getBytes()));
    }

    public static byte[] digest(MessageDigest digest, byte[] data) {
        return org.apache.commons.codec.digest.DigestUtils.digest(digest, data);
    }

    public static byte[] digest(MessageDigest digest, String str) {
        return digest(digest, str.getBytes());
    }

    public static String digestHex(MessageDigest digest, String str) {
        return Hex.encodeHexString(digest(digest, str));
    }
}
