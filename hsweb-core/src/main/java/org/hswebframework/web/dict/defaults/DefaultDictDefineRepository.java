package org.hswebframework.web.dict.defaults;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.dict.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class DefaultDictDefineRepository implements DictDefineRepository {
    protected final Map<String, DictDefine> parsedDict = new ConcurrentHashMap<>();

    public DefaultDictDefineRepository() {
    }

    public void registerDefine(DictDefine define) {
        if (define == null || define.getId() == null) {
            return;
        }
        parsedDict.put(define.getId(), define);
    }

    @SuppressWarnings("all")
    public static DictDefine parseEnumDict(Class<?> type) {

        try {
            Dict dict = type.getAnnotation(Dict.class);
            if (!type.isEnum()) {
                return null;
            }

            Object[] constants = type.getEnumConstants();
            List<EnumDict<?>> items = new ArrayList<>(constants.length);

            for (Object enumConstant : constants) {
                if (enumConstant instanceof EnumDict) {
                    items.add((EnumDict) enumConstant);
                } else {
                    Enum e = ((Enum) enumConstant);
                    items.add(
                        DefaultItemDefine
                            .builder()
                            .value(e.name())
                            .text(e.name())
                            .ordinal(e.ordinal())
                            .build());
                }
            }

            DefaultDictDefine define = new DefaultDictDefine();
            if (dict != null) {
                define.setId(dict.value());
                define.setComments(dict.comments());
                define.setAlias(dict.alias());
            } else {

                String id = StringUtils.camelCase2UnderScoreCase(type.getSimpleName()).replace("_", "-");
                if (id.startsWith("-")) {
                    id = id.substring(1);
                }
                define.setId(id);
                define.setAlias(type.getSimpleName());
//            define.setComments();
            }
            define.setItems(items);
            log.trace("parse enum dict : {} as : {}", type, define.getId());
            return define;
        } catch (Throwable e) {
            log.warn("parse enum class [{}] error", type, e);
            return null;
        }

    }

    @Override
    public Mono<DictDefine> getDefine(String id) {
        return Mono.justOrEmpty(parsedDict.get(id));
    }

    @Override
    public Flux<DictDefine> getAllDefine() {
        return Flux.fromIterable(parsedDict.values());
    }

    @Override
    public void addDefine(DictDefine dictDefine) {
        registerDefine(dictDefine);
    }
}
