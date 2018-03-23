package org.hswebframework.web.datasource.manager.simple;

import org.hswebframework.ezorm.core.ObjectWrapper;

import java.util.ArrayList;
import java.util.List;

public class QueryResultWrapper implements ObjectWrapper<QueryResult> {
   private QueryResult result = new QueryResult();

    private List<Object> temp = new ArrayList<>();

    @Override
    public void setUp(List<String> columns) {
        result.setColumns(columns);
    }

    @Override
    public Class<QueryResult> getType() {
        return QueryResult.class;
    }

    @Override
    public QueryResult newInstance() {
        return result;
    }

    @Override
    public void wrapper(QueryResult instance, int index, String attr, Object value) {
        temp.add(value);

    }

    @Override
    public boolean done(QueryResult instance) {
        instance.getData().add(new ArrayList<>(temp));
        temp.clear();
        return false;
    }

    public QueryResult getResult() {
        return result;
    }


}
