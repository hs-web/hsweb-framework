package org.hswebframework.web.authorization.setting;

import java.util.List;
import java.util.Optional;

public interface SettingValueHolder {

    SettingValueHolder NULL = SettingNullValueHolder.INSTANCE;

    <T> Optional<List<T>> asList(Class<T> t);

    <T> Optional<T> as(Class<T> t);

    Optional<String> asString();

    Optional<Long> asLong();

    Optional<Integer> asInt();

    Optional<Double> asDouble();

    Optional<Object> getValue();

    UserSettingPermission getPermission();

}