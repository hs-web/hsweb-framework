package org.hswebframework.web.organizational.authorization.simple;

import java.io.Serializable;
import java.util.Set;

/**
 * 自定义范围
 *
 * @author zhouhao
 */
public class CustomScope implements Serializable {

    private String type;

    private Set<String> ids;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    @Override
    public int hashCode() {
        return (type + "" + ids).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CustomScope && hashCode() == obj.hashCode();
    }
}
