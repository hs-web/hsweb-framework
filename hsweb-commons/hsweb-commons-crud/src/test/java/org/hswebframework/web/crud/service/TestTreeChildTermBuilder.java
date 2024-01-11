package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.sql.terms.TreeChildTermBuilder;
import org.springframework.stereotype.Component;

@Component
public class TestTreeChildTermBuilder extends TreeChildTermBuilder {
    public TestTreeChildTermBuilder() {
        super("test-child", "测试子节点");
    }

    @Override
    protected String tableName() {
        return "test_tree_sort";
    }
}
