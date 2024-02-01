package org.hswebframework.web.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Qingsheng.Ning
 */
@Getter
@AllArgsConstructor
public enum TestEnumInteger implements EnumDict<Integer> {
    E1(1), E2(2)
    ;

    private final Integer value;

    @Override
    public String getText() {
        return name();
    }
}