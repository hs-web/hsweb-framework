package org.hswebframework.web.organizational.authorization.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhouhao
 */
public class SimpleRelations implements Relations {
    private List<Relation> relations;

    @Override
    public List<Relation> getAllRelations() {
        if (null == relations) relations = new ArrayList<>();
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        Objects.requireNonNull(relations);
        this.relations = relations;
    }

    public SimpleRelations() {
    }

    public SimpleRelations(List<Relation> relations) {
        this.relations = relations;
    }
}
