package org.hswebframework.web.datasource.switcher;

import java.util.Optional;

public interface Switcher {

    void useLast();

    void use(String id);

    void useDefault();

    Optional<String> current();

    void reset();

}
