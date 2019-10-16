package org.hswebframework.web.authorization;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompositeAuthentication implements Authentication {

    private Map<String, Authentication> userAuthentication = new ConcurrentHashMap<>();

    private String currentUser;

    public boolean isEmpty() {
        return userAuthentication.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public User getUser() {

        return userAuthentication
                .get(currentUser)
                .getUser();
    }

    @Override
    public List<Dimension> getDimensions() {
        return userAuthentication.values()
                .stream()
                .map(Authentication::getDimensions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> getRoles() {
        return userAuthentication.values()
                .stream()
                .map(Authentication::getRoles)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getPermissions() {
        return userAuthentication.values()
                .stream()
                .map(Authentication::getPermissions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public <T extends Serializable> Optional<T> getAttribute(String name) {
        return userAuthentication.values()
                .stream()
                .map(a -> a.<T>getAttribute(name))
                .filter(Optional::isPresent)
                .findAny()
                .flatMap(Function.identity());
    }

    @Override
    public Map<String, Serializable> getAttributes() {
        return userAuthentication.values()
                .stream()
                .map(Authentication::getAttributes)
                .filter(Objects::nonNull)
                .reduce(new HashMap<>(),(r,s)->{r.putAll(s);return r;});
    }

    public CompositeAuthentication merge(Authentication authentication) {
        String userId = authentication.getUser().getId();
        if (currentUser == null) {
            currentUser = userId;
        }
        userAuthentication.put(userId, authentication);
        return this;
    }
}
