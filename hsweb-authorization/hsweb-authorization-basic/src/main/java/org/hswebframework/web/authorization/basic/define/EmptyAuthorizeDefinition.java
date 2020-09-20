package org.hswebframework.web.authorization.basic.define;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hswebframework.web.authorization.define.*;

import java.lang.reflect.Method;

/**
 * @author zhouhao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmptyAuthorizeDefinition implements AopAuthorizeDefinition {

    public static EmptyAuthorizeDefinition instance = new EmptyAuthorizeDefinition();


    @Override
    public ResourcesDefinition getResources() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DimensionsDefinition getDimensions() {

        throw new UnsupportedOperationException();
    }

    @Override
    public String getMessage() {

        throw new UnsupportedOperationException();
    }

    @Override
    public Phased getPhased() {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Class<?> getTargetClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Method getTargetMethod() {
        throw new UnsupportedOperationException();
    }
}
