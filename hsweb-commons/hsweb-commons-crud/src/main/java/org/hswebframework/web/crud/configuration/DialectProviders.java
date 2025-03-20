package org.hswebframework.web.crud.configuration;

import lombok.SneakyThrows;

import java.util.*;

public class DialectProviders {
    private static final Map<String, DialectProvider> allSupportedDialect = new HashMap<>();

    static {
        for (EasyormProperties.DialectEnum value : EasyormProperties.DialectEnum.values()) {
            allSupportedDialect.put(value.name(), value);
        }

        for (DialectProvider dialectProvider : ServiceLoader.load(DialectProvider.class)) {
            allSupportedDialect.put(dialectProvider.name(), dialectProvider);
        }
    }

    public static List<DialectProvider> all(){
        return new ArrayList<>(allSupportedDialect.values());
    }

    @SneakyThrows
    public static DialectProvider lookup(String dialect) {
        DialectProvider provider = allSupportedDialect.get(dialect);
        if (provider == null) {
            if (dialect.contains(".")) {
                provider = (DialectProvider) Class.forName(dialect).newInstance();
                allSupportedDialect.put(dialect, provider);
            } else {
                throw new UnsupportedOperationException("unsupported dialect : " + dialect + ",all alive dialect :" + allSupportedDialect.keySet());
            }
        }
        return provider;
    }
}
