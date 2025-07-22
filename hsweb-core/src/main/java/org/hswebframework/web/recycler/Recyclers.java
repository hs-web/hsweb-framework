package org.hswebframework.web.recycler;

public class Recyclers {

    private static final int MAX_STRING_BUILDER_SIZE = Integer.getInteger(
        "hsweb.recycler.string-builder.max-size", 16 * 1024
    );

    public static final Recycler<StringBuilder> STRING_BUILDER =
        Recycler.create(StringBuilder::new, builder -> {
            // 缩容
            if (builder.capacity() >= MAX_STRING_BUILDER_SIZE) {
                builder.setLength(MAX_STRING_BUILDER_SIZE);
                builder.trimToSize();
            }
            builder.setLength(0);
        }, 1024);

}
