package org.hswebframework.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

@Getter
@AllArgsConstructor
@Dict("true-or-false")
public enum TrueOrFalse implements EnumDict<Byte> {

    TRUE((byte) 1, "是"),

    FALSE((byte) 0, "否");

    private Byte value;

    private String text;

}
