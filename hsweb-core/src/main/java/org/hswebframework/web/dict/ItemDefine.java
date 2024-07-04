package org.hswebframework.web.dict;


/**
 * @author zhouhao
 * @since 3.0
 */
public interface ItemDefine extends EnumDict<String> {
    String getText();

    String getValue();

    String getComments();

    int getOrdinal();

    @Override
    default int ordinal() {
        return getOrdinal();
    }


}
