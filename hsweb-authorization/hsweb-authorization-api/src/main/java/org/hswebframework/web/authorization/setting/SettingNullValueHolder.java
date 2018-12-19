package org.hswebframework.web.authorization.setting;

import java.util.List;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SettingNullValueHolder implements SettingValueHolder {

    public static final SettingNullValueHolder INSTANCE = new SettingNullValueHolder();

    private SettingNullValueHolder() {
    }

    @Override
    public <T> Optional<List<T>> asList(Class<T> t) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> as(Class<T> t) {
        return Optional.empty();
    }

    @Override
    public Optional<String> asString() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> asLong() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> asInt() {
        return Optional.empty();
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getValue() {
        return Optional.empty();
    }

    @Override
    public UserSettingPermission getPermission() {
        return UserSettingPermission.NONE;
    }
}
