package org.hswebframework.web.utils;

import io.seruco.encoding.base62.Base62;
import org.apache.commons.codec.binary.Hex;
import org.hswebframework.web.recycler.Recycler;

import java.security.MessageDigest;
import java.util.function.Consumer;

public class DigestUtils {

    private static final Recycler<MessageDigest> md5 =
        Recycler.create(org.apache.commons.codec.digest.DigestUtils::getMd5Digest, MessageDigest::reset, 1024);

    private static final Recycler<MessageDigest> sha256 =
        Recycler.create(org.apache.commons.codec.digest.DigestUtils::getSha256Digest, MessageDigest::reset, 1024);

    private static final Recycler<MessageDigest> sha1 =
        Recycler.create(org.apache.commons.codec.digest.DigestUtils::getSha1Digest, MessageDigest::reset, 1024);

    private static final Base62 base62 = Base62.createInstance();


    public static Base62 base62() {
        return base62;
    }

    public static byte[] md5(Consumer<MessageDigest> digestHandler) {
        return digest(md5, digestHandler);
    }

    public static String md5Hex(Consumer<MessageDigest> digestHandler) {
        return digestHex(md5, digestHandler);
    }

    public static byte[] sha1(Consumer<MessageDigest> digestHandler) {
        return digest(sha1, digestHandler);
    }

    public static String sha1Hex(Consumer<MessageDigest> digestHandler) {
        return digestHex(sha1, digestHandler);
    }

    public static byte[] sha256(Consumer<MessageDigest> digestHandler) {
        return digest(sha256, digestHandler);
    }

    public static String sha256Hex(Consumer<MessageDigest> digestHandler) {
        return digestHex(sha1, digestHandler);
    }

    public static byte[] md5(byte[] data) {
        return digest(md5,digest->digest.update(data));
    }

    public static byte[] md5(String str) {
        return md5(str.getBytes());
    }

    public static String md5Hex(String str) {
        return Hex.encodeHexString(md5(str.getBytes()));
    }

    public static String md5Base62(String str) {
        return new String(base62.encode(md5(str.getBytes())));
    }

    public static byte[] sha256(byte[] data) {
        return digest(sha256,digest->digest.update(data));
    }

    public static byte[] sha256(String str) {
        return sha256(str.getBytes());
    }

    public static String sha256Hex(String str) {
        return Hex.encodeHexString(sha256(str.getBytes()));
    }

    public static byte[] sha1(byte[] data) {
        return digest(sha1,digest->digest.update(data));
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

    private static byte[] digest(Recycler<MessageDigest> digestSupplier,
                                 Consumer<MessageDigest> digestHandler) {
        return digestSupplier.doWith(
            digestHandler,
            (digest, handler) -> {
                handler.accept(digest);
                return digest.digest();
            });
    }

    private static String digestHex(Recycler<MessageDigest> digestSupplier,
                                    Consumer<MessageDigest> digestHandler) {
        return Hex.encodeHexString(digest(digestSupplier, digestHandler));
    }
}
