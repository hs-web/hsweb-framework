package org.hswebframework.web.organizational.authorization.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhouhao
 */
public class SimpleRelations implements Relations {
    private List<Relation> all;

    @Override
    public List<Relation> getAll() {
        if (null == all) all = new ArrayList<>();
        return all;
    }

    public void setAll(List<Relation> all) {
        Objects.requireNonNull(all);
        this.all = all;
    }

    public SimpleRelations() {
    }

    public SimpleRelations(List<Relation> all) {
        this.all = all;
    }
}
