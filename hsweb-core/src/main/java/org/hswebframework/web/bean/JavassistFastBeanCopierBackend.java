package org.hswebframework.web.bean;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.Extendable;
import org.hswebframework.web.proxy.Proxy;

import java.util.Map;

@Slf4j
final class JavassistFastBeanCopierBackend implements FastBeanCopierBackend {

    @Override
    public Copier createCopier(Class<?> source, Class<?> target) {
        String sourceName = source.getName();
        String targetName = target.getName();
        if (sourceName.startsWith("package ")) {
            sourceName = sourceName.substring("package ".length());
        }
        if (targetName.startsWith("package ")) {
            targetName = targetName.substring("package ".length());
        }
        boolean targetIsExtendable = Extendable.class.isAssignableFrom(target);
        boolean sourceIsExtendable = Extendable.class.isAssignableFrom(source);
        boolean targetIsMap = Map.class.isAssignableFrom(target);
        boolean sourceIsMap = Map.class.isAssignableFrom(source);

        String method = "public void copy(Object s, Object t, java.util.Set ignore, " +
            "org.hswebframework.web.bean.Converter converter){\n" +
            "try{\n\t" +
            sourceName + " $$__source=(" + sourceName + ")s;\n\t" +
            targetName + " $$__target=(" + targetName + ")t;\n\t" +
            FastBeanCopierPropertySupport.createCopierCode(source, target) +
            "}catch(Throwable e){\n" +
            "\tthrow e;" +
            "\n}\n" +
            "\n}";
        try {
            @SuppressWarnings("all")
            Proxy<Copier> proxy = Proxy
                .create(Copier.class, new Class[]{source, target})
                .addMethod(method);
            Copier copier = proxy.newInstance();
            if (sourceIsExtendable && targetIsMap) {
                copier = new ExtendableToMapCopier(copier);
            } else if (sourceIsMap && targetIsExtendable) {
                copier = new MapToExtendableCopier(copier);
            } else if (sourceIsExtendable) {
                copier = new ExtendableToBeanCopier(copier);
            }
            return copier;
        } catch (Throwable e) {
            log.error("create bean copy {} =>{} failed :\n{}", source, target, method, e);
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }
}
