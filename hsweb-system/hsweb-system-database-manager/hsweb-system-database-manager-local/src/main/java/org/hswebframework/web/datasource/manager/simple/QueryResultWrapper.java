package org.hswebframework.web.datasource.manager.simple;

import org.hswebframework.ezorm.rdb.executor.wrapper.ColumnWrapperContext;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapperContext;

import java.util.ArrayList;
import java.util.List;

public class QueryResultWrapper implements ResultWrapper<QueryResult,QueryResult> {
   private QueryResult result = new QueryResult();

    private List<Object> temp = new ArrayList<>();

    @Override
    public void beforeWrap(ResultWrapperContext context) {
        result.setColumns(context.getColumns());
    }


    @Override
    public QueryResult newRowInstance() {
        return result;
    }

    @Override
    public void wrapColumn(ColumnWrapperContext<QueryResult> context) {
        temp.add(context.getResult());
    }

    @Override
    public boolean completedWrapRow(QueryResult result) {
        result.getData().add(new ArrayList<>(temp));
        temp.clear();
        return true;
    }

    public QueryResult getResult() {
        return result;
    }


}
