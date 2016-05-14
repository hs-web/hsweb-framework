package org.hsweb.web.bean.po;


import org.webbuilder.utils.common.MD5;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用的PO对象，实现基本的属性和方法。新建的PO都应继承该类
 * Created by 浩 on 2015-07-20 0020.
 */
public class GenericPo<PK> implements Serializable {
    private static final long serialVersionUID = 9197157871004374522L;
    /**
     * 主键
     */
    private PK uId;

    public PK getUId() {
        return uId;
    }

    private Map<String, Object> customAttr = new LinkedHashMap<>();

    public <T> T attr(String attr, T value) {
        customAttr.put(attr, value);
        return value;
    }

    public <T> T attr(String attr) {
        return ((T) customAttr.get(attr));
    }

    @Override
    public int hashCode() {
        if (getUId() == null) return 0;
        return getUId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return this.hashCode() == obj.hashCode();
    }

    public void setUId(PK uId) {
        this.uId = uId;
    }

    /**
     * 创建一个主键
     *
     * @return
     */
    public static String createUID() {
        return MD5.encode(UUID.randomUUID().toString());
    }

    public Map<String, Object> getCustomAttr() {
        return customAttr;
    }

    public void setCustomAttr(Map<String, Object> customAttr) {
        this.customAttr = customAttr;
    }
}
